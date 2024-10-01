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

import static com.cluedetails.ClueDetailsConfig.CLUE_ITEM_HIGHLIGHT_CONFIG;
import com.cluedetails.panels.ClueDetailsParentPanel;
import com.google.inject.Provides;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import javax.inject.Inject;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.KeyCode;
import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.events.MenuOpened;
import net.runelite.api.widgets.InterfaceID;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetUtil;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
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

	public ClueDetailsParentPanel panel;

	NavigationButton navButton;

	CluePreferenceManager cluePreferenceManager;

	private final Collection<String> configEvents = Arrays.asList("filterListByTier", "filterListByRegion", "orderListBy", "onlyShowMarkedClues");

	@Getter
	private final Clues[] cluesInInventory = new Clues[4];

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

		setupInventoryClues();
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

		Arrays.fill(cluesInInventory, null);
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

	private void setupInventoryClues()
	{
		ItemContainer inventory = client.getItemContainer(InventoryID.INVENTORY);
		if (inventory == null) return;

		for (Item item : inventory.getItems())
		{
			Clues clue = Clues.get(item.getId());
			if (clue != null)
			{
				cluesInInventory[clue.getClueTier().ordinal()] = clue;
			}
		}
	}

	@Subscribe
	public void onItemContainerChanged(ItemContainerChanged itemContainerChanged)
	{
		if (itemContainerChanged.getContainerId() != InventoryID.INVENTORY.getId()) return;

		widgetOverlay.setInventoryChanged(true);

		for (Item item : itemContainerChanged.getItemContainer().getItems())
		{
			Clues clue = Clues.get(item.getId());
			if (clue != null)
			{
				cluesInInventory[clue.getClueTier().ordinal()] = clue;
			}
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
					&& w.getName().contains("Clue scroll"))
			{
				final int itemId = w.getItemId();

				client.createMenuEntry(idx)
					.setOption("Clue details text")
					.setTarget(entry.getTarget())
					.setType(MenuAction.RUNELITE)
					.onClick(e ->
					{
						Clues clue = Clues.get(itemId);
						chatboxPanelManager.openTextInput("Enter new clue text:")
							.onDone((newTag) -> {
								configManager.setConfiguration("clue-details-text", String.valueOf(clue.getClueID()), newTag);
								panel.refresh();
							})
							.build();
					});
			}

			if (w != null && WidgetUtil.componentToInterface(w.getId()) == InterfaceID.INVENTORY
				&& "Examine".equals(entry.getOption()) && entry.getIdentifier() == 10)
			{
				for (Clues clue : cluesInInventory)
				{
					if (clue == null) continue;
					final int itemId = w.getItemId();
					final String text = "Add to " + clue.getClueTier().name() + " clue";

					// Add menu to item for clue
					client.getMenu().createMenuEntry(idx)
						.setOption(text)
						.setTarget(entry.getTarget())
						.setType(MenuAction.RUNELITE)
						.onClick(e ->
						{
							addItemToClueHighlights(clue, itemId);
						});
				}
			}
		}
	}

	private void addItemToClueHighlights(Clues clue, int itemId)
	{
		int[] clueItemHighlightIds = configManager.getConfiguration(CLUE_ITEM_HIGHLIGHT_CONFIG, String.valueOf(clue.getClueID()), int[].class);
		List<Integer> itemIds;
		if (clueItemHighlightIds == null)
		{
			itemIds = new ArrayList<>(itemId);
		}
		else
		{
			itemIds = Arrays.stream(clueItemHighlightIds).boxed().collect(Collectors.toList());
			itemIds.add(itemId);
		}

		System.out.println(clue.getDisplayText(configManager));
		System.out.println(clue.getClueID());
		configManager.setConfiguration(CLUE_ITEM_HIGHLIGHT_CONFIG, String.valueOf(clue.getClueID()), itemIds);
	}
}
