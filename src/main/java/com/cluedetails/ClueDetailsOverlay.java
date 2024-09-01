/*
 * Copyright (c) 2024, Zoinkwiz <https://www.github.com/Zoinkwiz>
 * Copyright (c) 2017, Aria <aria@ar1as.space>
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

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.Menu;
import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;
import net.runelite.api.Perspective;
import net.runelite.api.Player;
import net.runelite.api.Point;
import net.runelite.api.Tile;
import net.runelite.api.TileItem;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.ItemDespawned;
import net.runelite.api.events.ItemSpawned;
import net.runelite.client.Notifier;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.ui.JagexColors;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.outline.ModelOutlineRenderer;
import net.runelite.client.ui.overlay.tooltip.Tooltip;
import net.runelite.client.ui.overlay.tooltip.TooltipManager;

public class ClueDetailsOverlay extends OverlayPanel
{
	private final Client client;
	private final ClueDetailsConfig config;
	private final TooltipManager tooltipManager;

	protected ModelOutlineRenderer modelOutlineRenderer;
	private final ConfigManager configManager;

	private final Notifier notifier;

	protected Multimap<Tile, Integer> tileHighlights = ArrayListMultimap.create();

	protected static final int MAX_DISTANCE = 2350;

	@Inject
	public ClueDetailsOverlay(Client client, ClueDetailsConfig config, TooltipManager tooltipManager, ModelOutlineRenderer modelOutlineRenderer, ConfigManager configManager, Notifier notifier)
	{
		setPriority(PRIORITY_HIGHEST);
		setLayer(OverlayLayer.ABOVE_WIDGETS);

		this.client = client;
		this.config = config;
		this.tooltipManager = tooltipManager;
		this.modelOutlineRenderer = modelOutlineRenderer;
		this.configManager = configManager;
		this.notifier = notifier;

		tileHighlights.clear();
		if (client.getGameState() == GameState.LOGGING_IN)
		{
			refreshHighlights();
		}
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (client.isMenuOpen())
		{
			showMenuItem();
		}
		else
		{
			showHoveredItem();
		}

		tileHighlights.keySet().forEach(tile -> checkAllTilesForHighlighting(tile, tileHighlights.get(tile)));

		return super.render(graphics);
	}

	private void showHoveredItem()
	{
		Menu menu = client.getMenu();
		MenuEntry[] menuEntries = menu.getMenuEntries();
		int last = menuEntries.length - 1;

		if (last < 0)
		{
			return;
		}

		MenuEntry menuEntry = menuEntries[last];

		if (!isTakeClue(menuEntry) && !isReadClue(menuEntry))
		{
			return;
		}

		String clueText = getText(menuEntry);

		tooltipManager.add(new Tooltip(clueText));
	}

	private void showMenuItem()
	{
		Menu menu = client.getMenu();
		MenuEntry[] currentMenuEntries = menu.getMenuEntries();

		if (currentMenuEntries != null)
		{
			Point mousePosition = client.getMouseCanvasPosition();
			int menuX = menu.getMenuX();
			int menuY = menu.getMenuY();
			int menuWidth = menu.getMenuWidth();

			int menuEntryHeight = 15;
			int headerHeight = menuEntryHeight + 3;

			int numberNotInMainMenu = 0;

			if (config.changeClueText())
			{
				// Change text of actual clue
				for (int i = currentMenuEntries.length - 1; i >= 0; i--)
				{
					MenuEntry hoveredEntry = currentMenuEntries[i];
					Clues clue = Clues.get(hoveredEntry.getIdentifier());
					if (clue != null)
					{
						hoveredEntry.setTarget("<col=ff9146>" + clue.getDisplayText(configManager) + "<col=FFA07A>");
					}
				}
			}

			for (int i = currentMenuEntries.length - 1; i >= 0; i--)
			{
				MenuEntry hoveredEntry = currentMenuEntries[i];

				int realPos = currentMenuEntries.length - (i + numberNotInMainMenu) - 1;

				if (!isTakeOrMarkClue(hoveredEntry) && !isReadClue(hoveredEntry)) continue;

				int entryTopY = menuY + headerHeight + realPos * menuEntryHeight;
				int entryBottomY = entryTopY + menuEntryHeight;

				if (mousePosition.getX() > menuX && mousePosition.getX() < menuX + menuWidth &&
					mousePosition.getY() > entryTopY && mousePosition.getY() <= entryBottomY)
				{
					String text = getText(hoveredEntry);
					panelComponent.setPreferredLocation(new java.awt.Point(menuX + menuWidth, entryTopY - menuEntryHeight));
					panelComponent.getChildren().add(LineComponent.builder().left(text).build());
					break;
				}
			}
		}
	}

	public boolean isTakeClue(MenuEntry entry)
	{
		String target = entry.getTarget();
		String option = entry.getOption();
		MenuAction type = entry.getType();

		return type == MenuAction.GROUND_ITEM_THIRD_OPTION && target.contains("Clue scroll") && option.equals("Take");
	}

	public boolean isTakeOrMarkClue(MenuEntry entry)
	{
		String target = entry.getTarget();
		String option = entry.getOption();
		MenuAction type = entry.getType();

		return target.contains("Clue scroll") && (
			(type == MenuAction.GROUND_ITEM_THIRD_OPTION && option.equals("Take")) ||
				(type == MenuAction.RUNELITE && (option.equals("Unmark") || option.equals("Mark"))));
	}

	public boolean isReadClue(MenuEntry entry)
	{
		String target = entry.getTarget();
		String option = entry.getOption();
		return target.contains("Clue scroll") && option.equals("Read");
	}

	private boolean shouldHighlight(int id)
	{
		String shouldHighlight = configManager.getConfiguration("clue-details-highlights", String.valueOf(id));
		return "true".equals(shouldHighlight);
	}

	private String getText(MenuEntry menuEntry)
	{
		int scrollID = menuEntry.getIdentifier();
		if (isReadClue(menuEntry))
		{
			scrollID = menuEntry.getItemId();
		}
		Clues matchingClue = Clues.get(scrollID);
		if (matchingClue == null)
		{
			return "Can't determine clue.";
		}

		return matchingClue.getDisplayText(configManager);
	}

	@Subscribe
	public void onGameStateChanged(final GameStateChanged event)
	{
		if (event.getGameState() == GameState.LOADING)
		{
			tileHighlights.clear();
		}
    
		if (event.getGameState() == GameState.LOGGED_IN)
		{
			addItemTiles();
		}
	}
  
	@Subscribe
	public void onItemSpawned(ItemSpawned itemSpawned)
	{
		TileItem item = itemSpawned.getItem();
		Tile tile = itemSpawned.getTile();
		if (shouldHighlight(item.getId()))
		{
			notifier.notify(config.markedClueDroppedNotification(), "A highlighted clue has dropped!");
			tileHighlights.get(tile).add(item.getId());
		}
	}

	@Subscribe
	public void onItemDespawned(ItemDespawned itemDespawned)
	{
		Tile tile = itemDespawned.getTile();
		if (tileHighlights.containsKey(tile))
		{
			tileHighlights.get(tile).removeIf((i) -> i == itemDespawned.getItem().getId());
		}
	}

	protected void addItemTiles()
	{
		tileHighlights.clear();

		Tile[][] squareOfTiles = client.getScene().getTiles()[client.getTopLevelWorldView().getPlane()];

		// Reduce the two-dimensional array into a single list for processing.
		List<Tile> tiles = Stream.of(squareOfTiles)
			.flatMap(Arrays::stream)
			.filter(Objects::nonNull)
			.collect(Collectors.toList());

		for (Tile tile : tiles)
		{
			List<TileItem> items = tile.getGroundItems();
			if (items != null)
			{
				for (TileItem item : items)
				{
					if (item == null)
					{
						continue;
					}

					if (shouldHighlight(item.getId()))
					{
						tileHighlights.get(tile).add(item.getId());
						break;
					}
				}
			}
		}
	}

	public void refreshHighlights()
	{
		if (client.getGameState() != GameState.LOGGED_IN)
		{
			return;
		}

		addItemTiles();
	}

	private void checkAllTilesForHighlighting(Tile tile, Collection<Integer> ids)
	{
		if (!config.highlightMarkedClues())
		{
			return;
		}
		Player player = client.getLocalPlayer();

		if (player == null)
		{
			return;
		}

		LocalPoint playerLocation = player.getLocalLocation();
		if (!ids.isEmpty())
		{
			LocalPoint location = tile.getLocalLocation();

			if (location == null)
			{
				return;
			}

			if (location.distanceTo(playerLocation) > MAX_DISTANCE)
			{
				return;
			}

			Polygon poly = Perspective.getCanvasTilePoly(client, location);
			if (poly == null)
			{
				return;
			}

			modelOutlineRenderer.drawOutline(
				tile.getItemLayer(),
				config.outlineWidth(),
				JagexColors.CHAT_PUBLIC_TEXT_OPAQUE_BACKGROUND,
				config.highlightFeather());
		}
	}
}
