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
import com.google.inject.Provides;
import java.awt.image.BufferedImage;
import java.util.*;
import javax.inject.Inject;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.*;
import net.runelite.api.widgets.ComponentID;
import net.runelite.api.widgets.InterfaceID;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetUtil;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ClientShutdown;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.events.RuneScapeProfileChanged;
import net.runelite.client.game.chatbox.ChatboxPanelManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.ImageUtil;

@Slf4j
@PluginDescriptor(
	name = "Clue Details",
	description = "Provides details and highlighting for clues on the floor",
	tags = { "clue", "overlay" }
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
	private ClueDetailsTagsOverlay tagsOverlay;

	@Inject
	private ClueDetailsWidgetOverlay widgetOverlay;

	@Inject
	private EventBus eventBus;

	@Inject
	private ClientToolbar clientToolbar;

	@Inject
	private ChatboxPanelManager chatboxPanelManager;

	@Inject
	public ClueDetailsSharingManager clueDetailsSharingManager;

	@Inject
	ConfigManager configManager;

	@Inject
	ClueFloorManager clueFloorManager;

	public ClueDetailsParentPanel panel;

	NavigationButton navButton;

	CluePreferenceManager cluePreferenceManager;

	private final Collection<String> configEvents = Arrays.asList("filterListByTier", "filterListByRegion", "orderListBy", "onlyShowMarkedClues");

	private final Collection<Integer> trackedClues = Arrays.asList(ItemID.CLUE_SCROLL_MASTER, ItemID.TORN_CLUE_SCROLL_PART_1, ItemID.TORN_CLUE_SCROLL_PART_2, ItemID.TORN_CLUE_SCROLL_PART_3);

	@Getter
	public Set<Integer> trackedCluesInInventory = new HashSet<>();

	@Getter
	public String readClueText = null;

	private Integer fakeClueId;
	boolean profileChanged;

	@Override
	protected void startUp() throws Exception
	{
		overlayManager.add(infoOverlay);
		eventBus.register(infoOverlay);

		overlayManager.add(tagsOverlay);

		overlayManager.add(widgetOverlay);
		eventBus.register(widgetOverlay);

		cluePreferenceManager = new CluePreferenceManager(configManager);

		final BufferedImage icon = ImageUtil.loadImageResource(ClueDetailsPlugin.class, "/icon.png");

		panel = new ClueDetailsParentPanel(configManager, cluePreferenceManager, config, chatboxPanelManager, clueDetailsSharingManager);
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
		overlayManager.remove(infoOverlay);
		eventBus.unregister(infoOverlay);

		overlayManager.remove(tagsOverlay);

		overlayManager.remove(widgetOverlay);
		eventBus.unregister(widgetOverlay);

		clientToolbar.removeNavigation(navButton);

		clueFloorManager.saveFloorCluesToConfig();
	}

	@Subscribe
	public void onItemContainerChanged(final ItemContainerChanged event)
	{
		final ItemContainer itemContainer = event.getItemContainer();

		if (event.getContainerId() != InventoryID.INVENTORY.getId())
		{
			return;
		}

		System.out.println(readClueText);

		// Check if clues were added to inventory
		trackedCluesInInventory = getTrackedCluesInInventory(itemContainer);
	}

	private Set<Integer> getTrackedCluesInInventory(ItemContainer inventoryContainer)
	{
		// Remove clues no longer in inventory
		trackedCluesInInventory.removeIf(clue -> !inventoryContainer.contains(clue));
		if (!trackedCluesInInventory.contains(ItemID.CLUE_SCROLL_MASTER))
		{
			// If removed, let's check floor items to see if new master about under player
			if (readClueText != null) checkForDroppedClue();
			setClueTextIfClueDissapeared();
		}

		// Add new clues
		Item[] inventoryItems = inventoryContainer.getItems();
		Set<Integer> cluesFound = new HashSet<>();

		for (Item item : inventoryItems)
		{
			int invItemId = item.getId();
			if (trackedClues.contains(invItemId))
			{
				cluesFound.add(invItemId);
			}
		}

		return cluesFound;
	}

	public Boolean foundTrackedClue()
	{
		return trackedCluesInInventory.stream().anyMatch(trackedClues::contains);
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		if (gameStateChanged.getGameState() == GameState.LOGIN_SCREEN)
		{
			clueFloorManager.saveFloorCluesToConfig();
			profileChanged = true;
		}
		if (gameStateChanged.getGameState() == GameState.LOGGED_IN && profileChanged)
		{
			profileChanged = false;
			clueFloorManager.setUnknownInitialState();
		}
	}

	@Subscribe
	private void onRuneScapeProfileChanged(RuneScapeProfileChanged ev)
	{
		profileChanged = true;
	}

	@Subscribe
	public void onGameTick(final GameTick event)
	{
		clueFloorManager.loadInitialStateFromConfig(client);
		// Reset clueOpenedText when receiving a new beginner or master clue
		// These clues use a single item ID, so we cannot detect step changes based on the item ID changing
		final Widget chatDialogClueItem = client.getWidget(ComponentID.DIALOG_SPRITE_SPRITE);
		if (chatDialogClueItem != null
			&& (chatDialogClueItem.getItemId() == ItemID.CLUE_SCROLL_BEGINNER || chatDialogClueItem.getItemId() == ItemID.CLUE_SCROLL_MASTER))
		{
			readClueText = null;
			fakeClueId = null;
		}

		// TODO: Don't do this every tick, be smarter with removal?
		clueFloorManager.clearClues();
	}

	@Subscribe
	public void onWidgetLoaded(WidgetLoaded event)
	{
		// Set clueOpenedText when reading a beginner or master clue
		// These clues use a single item ID, so we cannot detect step changes based on the item ID changing
		if (event.getGroupId() == InterfaceID.CLUESCROLL && foundTrackedClue())
		{
			clientThread.invokeLater(() ->
			{
				final Widget clueScrollText = client.getWidget(ComponentID.CLUESCROLL_TEXT);
				if (clueScrollText != null)
				{
					readClueText = clueScrollText.getText();
					fakeClueId = ClueText.forTextGetId(readClueText);
				}
			});
		}
	}

	@Provides
	ClueDetailsConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(ClueDetailsConfig.class);
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (event.getGroup().equals("clue-details-highlights"))
		{
			infoOverlay.refreshHighlights();
		}

		if (!event.getGroup().equals(ClueDetailsConfig.class.getAnnotation(ConfigGroup.class).value()))
		{
			return;
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

		if (configEvents.contains(event.getKey()))
		{
			panel.refresh();
		}
	}

	@Subscribe
	public void onMenuEntryAdded(MenuEntryAdded event)
	{
		final boolean hotKeyPressed = client.isKeyPressed(KeyCode.KC_SHIFT);
		if (hotKeyPressed && event.getTarget().contains("Clue scroll"))
		{
			if (!infoOverlay.isTakeClue(event.getMenuEntry()) && !infoOverlay.isReadClue(event.getMenuEntry()))
			{
				return;
			}

			int identifier = event.getIdentifier();
			if (infoOverlay.isReadClue(event.getMenuEntry()))
			{
				identifier = event.getMenuEntry().getItemId();
			}

			boolean isMarked = cluePreferenceManager.getPreference(identifier);

			client.createMenuEntry(-1)
				.setOption(isMarked ? "Unmark" : "Mark")
				.setTarget(event.getTarget())
				.setIdentifier(identifier)
				.setType(MenuAction.RUNELITE)
				.onClick(e ->
				{
					boolean currentValue = cluePreferenceManager.getPreference(e.getIdentifier());
					cluePreferenceManager.savePreference(e.getIdentifier(), !currentValue);
					panel.refresh();
				});
		}
	}

	@Subscribe
	public void onMenuOpened(final MenuOpened event)
	{
		if (!client.isKeyPressed(KeyCode.KC_SHIFT))
		{
			return;
		}

		final MenuEntry[] entries = event.getMenuEntries();
		for (int idx = entries.length - 1; idx >= 0; --idx)
		{
			final MenuEntry entry = entries[idx];
			final Widget w = entry.getWidget();

			if (w != null && WidgetUtil.componentToInterface(w.getId()) == InterfaceID.INVENTORY
					&& "Examine".equals(entry.getOption()) && entry.getIdentifier() == 10
					&& (w.getName().contains("Clue scroll")
					|| w.getName().contains("Challenge scroll")
					|| w.getName().contains("Key (")))
			{
				final int itemId = w.getItemId();

				client.createMenuEntry(idx)
					.setOption("Clue details")
					.setTarget(entry.getTarget())
					.setType(MenuAction.RUNELITE)
					.onClick(e ->
					{
						Clues clue = Clues.get(itemId);
						chatboxPanelManager.openTextInput("Enter new clue text:")
							.value(clue.getDisplayText(configManager))
							.onDone((newTag) -> {
								configManager.setConfiguration("clue-details-text", String.valueOf(clue.getClueID()), newTag);
								panel.refresh();
							})
							.build();
					});
			}
		}
	}

	@Subscribe(priority = 100)
	private void onClientShutdown(ClientShutdown e)
	{
		clueFloorManager.saveFloorCluesToConfig();
	}

	public void setClueTextIfClueDissapeared()
	{
		WorldPoint wp = client.getLocalPlayer().getWorldLocation();
		for (FloorClue dissapearedClue : dissapearedClues)
		{
			if (dissapearedClue == null) continue;
			// TODO: This is trash currently. need way to better map clue to disappearing one.
			if (wp.distanceTo(dissapearedClue.getWorldPoint()) == 0)
			{
				fakeClueId = dissapearedClue.clueID;
                readClueText = ClueText.getById(fakeClueId).getText();
				dissapearedClues.remove(dissapearedClue);
				return;
			}
		}

		readClueText = null;
		fakeClueId = null;
	}

	public void checkForDroppedClue()
	{
		WorldPoint wp = client.getLocalPlayer().getWorldLocation();
		if (wp == null) return;
		LocalPoint lp = LocalPoint.fromWorld(client.getTopLevelWorldView(), wp);
		if (lp == null) return;

		Tile tile = client.getTopLevelWorldView().getScene().getTiles()[wp.getPlane()][lp.getSceneX()][lp.getSceneY()];
		if (tile == null) return;

		List<TileItem> tileItems = tile.getGroundItems();
		if (tileItems == null || tileItems.isEmpty()) return;

		for (TileItem tileItem : tileItems)
		{
			if (tileItem.getId() == ItemID.CLUE_SCROLL_MASTER)
			{
				//
				if (clueFloorManager.isNewClue(tileItem))
				{
					System.out.println("NEW CLUE BEING ADDED");
					FloorClue newFloorClue = new FloorClue(fakeClueId, tileItem.getDespawnTime(), wp);
					clueFloorManager.addFloorClue(newFloorClue);
					floorClues.put(tileItem, newFloorClue);
				}
			}
		}
	}

	public Map<TileItem, FloorClue> floorClues = new HashMap<>();
	public List<FloorClue> dissapearedClues = new ArrayList<>();

	@Subscribe
	public void onItemSpawned(ItemSpawned itemSpawned)
	{
		if (itemSpawned.getItem().getId() == ItemID.CLUE_SCROLL_MASTER)
		{
			FloorClue floorClue = clueFloorManager.getExistingFloorClue(itemSpawned.getItem(), itemSpawned.getTile());
			if (floorClue == null) return;
			floorClues.put(itemSpawned.getItem(), floorClue);
		}
	}

	@Subscribe
	public void onItemDespawned(ItemDespawned itemDespawned)
	{
		if (itemDespawned.getItem().getId() == ItemID.CLUE_SCROLL_MASTER)
		{
			dissapearedClues.add(floorClues.get(itemDespawned.getItem()));
			floorClues.remove(itemDespawned.getItem());
		}
	}
}
