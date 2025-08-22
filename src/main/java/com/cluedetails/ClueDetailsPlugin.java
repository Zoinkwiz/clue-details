/*
 * Copyright (c) 2024, Zoinkwiz <https://github.com/Zoinkwiz>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.cluedetails;

import com.cluedetails.panels.ClueDetailsParentPanel;
import com.google.gson.Gson;
import com.google.inject.Provides;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.TreeMap;
import javax.inject.Inject;
import javax.inject.Named;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.InventoryID;
import net.runelite.api.ItemID;
import net.runelite.api.MenuEntry;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ClientTick;
import net.runelite.api.events.FocusChanged;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.ItemDespawned;
import net.runelite.api.events.ItemSpawned;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.events.MenuOpened;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.SpriteID;
import net.runelite.api.widgets.ComponentID;
import net.runelite.api.widgets.InterfaceID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.Notifier;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.events.ClientShutdown;
import net.runelite.client.events.PluginMessage;
import net.runelite.client.game.chatbox.ChatboxItemSearch;
import net.runelite.client.game.chatbox.ChatboxPanelManager;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.events.RuneScapeProfileChanged;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.cluescrolls.clues.hotcold.HotColdLocation;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.ui.components.colorpicker.ColorPickerManager;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.ui.overlay.infobox.InfoBoxManager;
import net.runelite.client.util.ImageUtil;

@Slf4j
@PluginDescriptor(
		name = "Clue Details",
		description = "Provides details and highlighting for clues on the floor",
		tags = {"clue", "overlay"}
)
public class ClueDetailsPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private ClueDetailsConfig config;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private ClueDetailsOverlay infoOverlay;

	@Inject
	private ClueGroundOverlay groundOverlay;

	@Inject
	private ClueDetailsTagsOverlay tagsOverlay;

	@Inject
	private ClueDetailsInventoryOverlay inventoryOverlay;

	@Getter
	@Inject
	private ClueDetailsItemsOverlay itemsOverlay;

	@Getter
	@Inject
	private ClueDetailsWidgetsOverlay widgetsOverlay;

	@Inject
	private ClueThreeStepSaverWidgetOverlay clueThreeStepSaverWidgetOverlay;

	@Inject
	private EventBus eventBus;

	@Inject
	private ClientToolbar clientToolbar;

	@Inject
	private ChatboxPanelManager chatboxPanelManager;

	@Inject
	private ClueDetailsSharingManager clueDetailsSharingManager;

	@Inject
	private ConfigManager configManager;

	@Getter
	@Inject
	private ItemManager itemManager;

	@Getter
	@Inject
	@Named("developerMode")
	private boolean developerMode;

	@Getter
	@Inject
	Gson gson;

	@Getter
	@Inject
	private ClueInventoryManager clueInventoryManager;

	@Getter
	@Inject
	private ClueWidgetManager clueWidgetManager;

	@Getter
	@Inject
	private ClueGroundManager clueGroundManager;

	@Getter
	@Inject
	private ClueBankManager clueBankManager;

	@Getter
	@Inject
	private CluePreferenceManager cluePreferenceManager;

	@Inject
	private ClueThreeStepSaver clueThreeStepSaver;

	@Getter
	@Inject
	private ChatMessageManager chatMessageManager;

	@Getter
	@Inject
	private ColorPickerManager colorPickerManager;

	@Inject
	@Getter
	private ChatboxItemSearch itemSearch;

	@Inject
	@Getter
	private Notifier notifier;

	@Inject
	private InfoBoxManager infoBoxManager;

	@Getter
	private final List<ClueGroundTimer> clueGroundTimers = new ArrayList<>();

	@Getter
	private ClueDetailsParentPanel panel;

	private NavigationButton navButton;

	private boolean profileChanged;

	@Getter
	public static int currentTick;

	@Getter
	public static int currentPlane;

	@Override
	protected void startUp() throws Exception
	{
		startUpOverlays();

		clueThreeStepSaver.startUp();
		clueGroundManager.startUp();

		Clues.rebuildFilteredCluesCache();

		final BufferedImage icon = ImageUtil.loadImageResource(getClass(), "/icon.png");

		panel = new ClueDetailsParentPanel(configManager, cluePreferenceManager, config, chatboxPanelManager, clueDetailsSharingManager, this);
		navButton = NavigationButton.builder()
				.tooltip("Clue Details")
				.icon(icon)
				.priority(7)
				.panel(panel)
				.build();

		if (config.showSidebar())
		{
			clientToolbar.addNavigation(navButton);
		}
	}

	@Override
	protected void shutDown() throws Exception
	{
		shutDownOverlays();

		clueGroundManager.shutDown();

		clientToolbar.removeNavigation(navButton);

		clueGroundManager.saveStateToConfig();
		clueBankManager.saveStateToConfig();

		for (ClueGroundTimer timer : clueGroundTimers)
		{
			infoBoxManager.removeInfoBox(timer);
		}

		resetIdleTimeout();
	}

	private void startUpOverlays()
	{
		overlayManager.add(infoOverlay);
		eventBus.register(infoOverlay);

		overlayManager.add(groundOverlay);
		eventBus.register(groundOverlay);

		overlayManager.add(clueThreeStepSaverWidgetOverlay);
		overlayManager.add(tagsOverlay);
		overlayManager.add(widgetsOverlay);

		overlayManager.add(inventoryOverlay);
		eventBus.register(inventoryOverlay);

		overlayManager.add(itemsOverlay);
		eventBus.register(itemsOverlay);
	}

	private void shutDownOverlays()
	{
		overlayManager.remove(infoOverlay);
		eventBus.unregister(infoOverlay);

		overlayManager.remove(groundOverlay);
		eventBus.unregister(groundOverlay);

		overlayManager.remove(tagsOverlay);

		overlayManager.remove(inventoryOverlay);
		eventBus.unregister(inventoryOverlay);

		overlayManager.remove(itemsOverlay);
		eventBus.unregister(itemsOverlay);

		overlayManager.remove(widgetsOverlay);

		overlayManager.remove(clueThreeStepSaverWidgetOverlay);

		clientToolbar.removeNavigation(navButton);

		clueGroundManager.saveStateToConfig();
		clueBankManager.saveStateToConfig();

		resetClueGroundTimers();
	}

	@Subscribe
	public void onItemContainerChanged(ItemContainerChanged event)
	{
		if (event.getContainerId() == InventoryID.INVENTORY.getId())
		{
			itemsOverlay.invalidateCache();
			clueInventoryManager.updateInventory(event.getItemContainer());
			clueThreeStepSaver.scanInventory();
		}
		else if (event.getContainerId() == InventoryID.BANK.getId())
		{
			clueBankManager.handleBankChange(event.getItemContainer());
		}

	}

	@Subscribe
	public void onWidgetLoaded(WidgetLoaded event)
	{
		if (event.getGroupId() >= InterfaceID.CLUE_BEGINNER_MAP_CHAMPIONS_GUILD
			&& event.getGroupId() <= InterfaceID.CLUE_BEGINNER_MAP_WIZARDS_TOWER)
		{
			clueInventoryManager.updateClueText(event.getGroupId(), ItemID.CLUE_SCROLL_BEGINNER);
		}
		else if (event.getGroupId() == ComponentID.CLUESCROLL_TEXT >> 16)
		{
			clientThread.invokeLater(() ->
			{
				Widget clueScrollText = client.getWidget(ComponentID.CLUESCROLL_TEXT);
				if (clueScrollText != null)
				{
					String text = clueScrollText.getText();
					clueInventoryManager.updateClueText(text);
					clueThreeStepSaver.scanInventory();
				}
			});
		}
		else if (event.getGroupId() == InterfaceID.FAIRY_RING_PANEL)
		{
			if (config.fairyRingAutoScroll())
			{
				clientThread.invokeLater(this::handleFairyRingPanel);
			}
		}
	}

	@Subscribe
	public void onFocusChanged(FocusChanged e)
	{
		if (e.isFocused())
		{
			resetIdleTimeout();
		}
	}

	@Subscribe
	public void onPluginMessage(PluginMessage event)
	{
		// Subscribe to Hot Cold Helper for HotColdLocation
		if ("hot-cold-helper".equals(event.getNamespace()))
		{
			if ("location-solved".equals(event.getName()))
			{
				Map<String, Object> data = event.getData();
				if (data == null) return;

				HotColdLocation solvedLocation = HotColdLocation.valueOf(data.get("location").toString());
				if (solvedLocation != null)
				{
					clueInventoryManager.updateClueText(solvedLocation.ordinal(),
						solvedLocation.isBeginnerClue() ? ItemID.CLUE_SCROLL_BEGINNER : ItemID.CLUE_SCROLL_MASTER);
				}
			}
		}
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		if (event.getGameState() == GameState.LOGIN_SCREEN)
		{
			clueGroundManager.saveStateToConfig();
			clueBankManager.saveStateToConfig();
			profileChanged = true;
		}

		if (event.getGameState() == GameState.LOGGED_IN)
		{
			if (profileChanged)
			{
				profileChanged = false;
				clueGroundManager.loadStateFromConfig();
				clueBankManager.loadStateFromConfig();
			}
		}
	}

	@Subscribe
	public void onRuneScapeProfileChanged(RuneScapeProfileChanged event)
	{
		profileChanged = true;
	}

	@Subscribe
	public void onClientTick(ClientTick event)
	{
		currentTick = client.getTickCount();
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		clueGroundManager.onGameTick();
		clueInventoryManager.onGameTick();

		currentPlane = client.getTopLevelWorldView().getPlane();

		renderGroundClueTimers(); // TODO: Call more efficiently
		infoBoxManager.cull(); // Explict call to clean up timers faster
		// Ground clue timers notifications
		if (!clueGroundTimers.isEmpty())
		{
			if (showNotifications())
			{
				for (ClueGroundTimer timer : clueGroundTimers)
				{
					if (!timer.isNotified() && timer.shouldNotify() && !timer.isRenotifying())
					{
						notifier.notify("Your clue scroll is about to disappear!");
						if (config.decreaseIdleTimeout())
						{
							client.setIdleTimeout(1); // client forces this to be minimum 5 minutes
						}
						if (config.groundClueTimersRenotificationTime() != 0)
						{
							timer.startRenotification();
						}
						else
						{
							timer.setNotified(true);
						}
					}
					else if (timer.isNotified() && !timer.shouldNotify())
					{
						timer.setNotified(false);
					}
				}
			}
		}
	}

	/* This gets called when:
	   Player logs in
	   Player enters from outside 3 zones distance to 3 or closer (teleport in, run in)
	   Player turns on plugin (and seems onItemSpawned is called for all existing items in scene, including
	     ones outside the 3 zone limit which're rendered
	 */
	@Subscribe
	public void onItemSpawned(ItemSpawned event)
	{
		clueGroundManager.onItemSpawned(event);
	}

	@Subscribe
	public void onItemDespawned(ItemDespawned event)
	{
		clueGroundManager.onItemDespawned(event);
	}

	@Subscribe(priority = -1) // run after ground items
	public void onMenuEntryAdded(MenuEntryAdded event)
	{
		clueInventoryManager.onMenuEntryAdded(event, cluePreferenceManager, panel);
		clueThreeStepSaver.onMenuEntryAdded(event);
	}

	@Subscribe
	public void onMenuOpened(MenuOpened event)
	{
		MenuEntry[] entries = event.getMenuEntries();
		clueThreeStepSaver.onMenuOpened(event);
		clueWidgetManager.addHighlightWidgetSubmenus(entries);
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (event.getGroup().equals("clue-details-highlights"))
		{
			infoOverlay.refreshHighlights();
		}

		if (event.getKey().equals("beginnerDetails")
			|| event.getKey().equals("easyDetails")
			|| event.getKey().equals("mediumDetails")
			|| event.getKey().equals("hardDetails")
			|| event.getKey().equals("eliteDetails")
			|| event.getKey().equals("masterDetails"))
		{
			Clues.rebuildFilteredCluesCache();
			clueInventoryManager.updateLastInventoryRefreshTime();
		}

		if (event.getGroup().equals("clue-details-color")
			|| event.getGroup().equals("clue-details-items")
			|| event.getKey().equals("highlightInventoryClueScrolls")
			|| event.getKey().equals("highlightInventoryClueItems")
			|| event.getKey().equals("colorInventoryClueItems"))
		{
			itemsOverlay.invalidateCache();
		}

		if (event.getGroup().equals(config.CLUE_WIDGETS_CONFIG)
			|| event.getKey().equals("highlightInventoryClueWidgets")
			|| event.getKey().equals("widgetHighlightColor")
			|| event.getKey().equals("colorInventoryClueWidgets")
			|| event.getGroup().equals("clue-details-color"))
		{
			clueInventoryManager.updateLastInventoryRefreshTime();
		}

		if (!event.getGroup().equals(ClueDetailsConfig.class.getAnnotation(ConfigGroup.class).value()))
		{
			return;
		}

		if ("groundClueTimersDecreaseIdleTimeout".equals(event.getKey()))
		{
			String minutes_config = configManager.getConfiguration("logouttimer", "idleTimeout");

			if (minutes_config != null)
			{
				client.setIdleTimeout(50 * 60 * Integer.parseInt(minutes_config));
			}
		}

		if ("showSidebar".equals(event.getKey()))
		{
			if ("true".equals(event.getNewValue()))
			{
				clientToolbar.addNavigation(navButton);
			}
			else
			{
				clientToolbar.removeNavigation(navButton);
			}
		}

		// Reset clueGroundTimers when showGroundClueTimers toggled off
		if ("showGroundClueTimers".equals(event.getKey()) && "false".equals(event.getNewValue()))
		{
			resetClueGroundTimers();
		}

		panel.refresh();
	}

	@Subscribe(priority = 100)
	private void onClientShutdown(ClientShutdown event)
	{
		clueGroundManager.saveStateToConfig();
		clueBankManager.saveStateToConfig();
	}

	@Provides
	ClueDetailsConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(ClueDetailsConfig.class);
	}

	private boolean showNotifications()
	{
		return config.groundClueTimersNotificationTime() > 0;
	}

	public void renderGroundClueTimers()
	{
		if (!config.showGroundClueTimers()) return;

		Set<WorldPoint> worldPoints = clueGroundManager.getTrackedWorldPoints();

		// Remove timers if worldPoint not managed by clueGroundManager
		clueGroundTimers.removeIf(timer-> !worldPoints.contains(timer.getWorldPoint()));

		// Populate timers
		for (WorldPoint worldPoint : worldPoints)
		{
			TreeMap<ClueInstance, Integer> clueInstancesWithQuantityAtWp = clueGroundManager.getClueInstancesWithQuantityAtWp(config, worldPoint);

			if (clueInstancesWithQuantityAtWp != null && clueInstancesWithQuantityAtWp.firstEntry() != null)
			{
				// Find oldest enabled clue instance at the world point
				ClueInstance oldestEnabledClueInstance = clueInstancesWithQuantityAtWp.firstEntry().getKey();
				for (ClueInstance clueInstance : clueInstancesWithQuantityAtWp.keySet())
				{
					if (clueInstance.isEnabled(config))
					{
						oldestEnabledClueInstance =  clueInstance;
						break;
					}
				}

				int despawnTick = oldestEnabledClueInstance.getDespawnTick();

				boolean createNewTimer = true;

				// Update existing timers
				for (ClueGroundTimer timer : clueGroundTimers)
				{
					if (worldPoint.equals(timer.getWorldPoint()))
					{
						timer.setClueInstancesWithQuantity(clueInstancesWithQuantityAtWp);
						timer.setDespawnTick(despawnTick);
						createNewTimer = false;
						break;
					}
				}

				if (createNewTimer)
				{
					ClueGroundTimer timer = new ClueGroundTimer(
						client,
						this,
						config,
						configManager,
						despawnTick,
						worldPoint,
						clueInstancesWithQuantityAtWp,
						itemManager.getImage(ItemID.CLUE_SCROLL_23815)
					);
					clueGroundTimers.add(timer);
					infoBoxManager.addInfoBox(timer);
				}
			}
		}
	}

	private void resetClueGroundTimers()
	{
		for (ClueGroundTimer timer : clueGroundTimers)
		{
			infoBoxManager.removeInfoBox(timer);
		}
		clueGroundTimers.clear();
	}

	private void resetIdleTimeout()
	{
		String minutes_config = configManager.getConfiguration("logouttimer", "idleTimeout");
		int minutes_parsed = 25;
		if (minutes_config != null)
		{
			minutes_parsed = Integer.parseInt(minutes_config);
		}
		client.setIdleTimeout(50 * 60 * minutes_parsed);
	}

	/**
	 * Taken from Hunter Rumours Plugin
	 * Called when the fairy ring dialog is opened.
	 * Responsible for scrolling to the relevant code and highlighting it, if found in clue detail.
	 */
	private void handleFairyRingPanel()
	{
		List<String> cluesInInventoryText = clueInventoryManager.getCluesInInventory().stream()
			.filter(Objects::nonNull)
			.map(clueInventoryManager::getClueByClueItemId)
			.filter(Objects::nonNull)
			.flatMap(instance -> instance.getClueIds().stream())
			.map(Clues::forClueIdFiltered)
			.filter(clue -> clue != null && clue.isEnabled(config))
			.map(clue -> clue.getDetail(configManager))
			.collect(Collectors.toList());

		if (cluesInInventoryText.isEmpty()) return;

		// Find all the necessary widgets
		Widget panelList = client.getWidget(ComponentID.FAIRY_RING_PANEL_LIST);
		Widget favoritesList = client.getWidget(ComponentID.FAIRY_RING_PANEL_FAVORITES);
		Widget scrollBar = client.getWidget(ComponentID.FAIRY_RING_PANEL_SCROLLBAR);

		if (panelList == null || scrollBar == null || favoritesList == null) return;

		Widget scrollBarContainer = null, scrollBarHandle = null, scrollBarHandleTop = null,
			scrollBarHandleBottom = null, scrollBarUpButton = null, scrollBarDownButton = null;
		for (var scrollChild : scrollBar.getDynamicChildren())
		{
			switch (scrollChild.getSpriteId())
			{
				case SpriteID.SCROLLBAR_ARROW_DOWN:
					scrollBarDownButton = scrollChild;
					break;
				case SpriteID.SCROLLBAR_ARROW_UP:
					scrollBarUpButton = scrollChild;
					break;
				case SpriteID.SCROLLBAR_THUMB_MIDDLE:
					scrollBarHandle = scrollChild;
					break;
				case SpriteID.SCROLLBAR_THUMB_TOP:
					scrollBarHandleTop = scrollChild;
					break;
				case SpriteID.SCROLLBAR_THUMB_BOTTOM:
					scrollBarHandleBottom = scrollChild;
					break;
				case SpriteID.SCROLLBAR_THUMB_MIDDLE_DARK:
					scrollBarContainer = scrollChild;
					break;
			}
		}

		if (scrollBarContainer == null || scrollBarHandle == null || scrollBarHandleTop == null
			|| scrollBarHandleBottom == null || scrollBarUpButton == null || scrollBarDownButton == null)
		{
			return;
		}

		// Construct a list of all widgets that are the fairy ring code texts
		var codeWidgets = new ArrayList<Widget>();

		// Add in all children from the big list
		codeWidgets.addAll(Arrays.asList(panelList.getDynamicChildren()));

		// Add in all children from the favorites list
		codeWidgets.addAll(Arrays.asList(favoritesList.getStaticChildren()));

		Widget foundCodeWidget = null;

		// Check each clue in inventory
		for (String clueDetail : cluesInInventoryText)
		{
			// Find the widget corresponding to the fairy ring code
			for (var codeWidget : codeWidgets)
			{
				if (!codeWidget.getText().isEmpty()
					&& clueDetail.contains(codeWidget.getText().replace(" ", "")))
				{
					foundCodeWidget = codeWidget;
					break;
				}
			}
			if (foundCodeWidget != null) break;
		}

		// If no widget found, bail out
		if (foundCodeWidget == null) return;

		// Scroll to the code entry and highlight it
		int panelScrollY = Math.min(foundCodeWidget.getRelativeY(), panelList.getScrollHeight() - panelList.getHeight());
		panelList.setScrollY(panelScrollY);
		panelList.revalidateScroll();
		foundCodeWidget.setTextColor(0x00FF00);
		foundCodeWidget.setText("(Clue) " + foundCodeWidget.getText());

		// Determine scrollbar placement
		double codeEntryPlacement = (double) foundCodeWidget.getRelativeY() / (double) panelList.getScrollHeight();
		final int ENTRY_PADDING = 4;
		int maxHandleY = scrollBarContainer.getHeight() - ENTRY_PADDING;
		int handleY = (int) ((double) scrollBarContainer.getHeight() * codeEntryPlacement) + scrollBarUpButton.getHeight();
		handleY = Math.min(handleY, maxHandleY);
		int handleBottomY = handleY + (scrollBarHandle.getHeight() - scrollBarHandleBottom.getHeight());

		scrollBarHandle.setOriginalY(handleY);
		scrollBarHandleTop.setOriginalY(handleY);
		scrollBarHandleBottom.setOriginalY(handleBottomY);
		scrollBarHandle.revalidateScroll();
		scrollBarHandleTop.revalidateScroll();
		scrollBarHandleBottom.revalidateScroll();
	}
}
