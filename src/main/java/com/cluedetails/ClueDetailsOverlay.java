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
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.GameState;
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
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.ui.JagexColors;
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

	protected Multimap<Tile, Integer> tileHighlights = ArrayListMultimap.create();

	protected static final int MAX_DISTANCE = 2350;

	@Inject
	public ClueDetailsOverlay(Client client, ClueDetailsConfig config, TooltipManager tooltipManager, ModelOutlineRenderer modelOutlineRenderer, ConfigManager configManager)
	{
		this.client = client;
		this.config = config;
		this.tooltipManager = tooltipManager;
		this.modelOutlineRenderer = modelOutlineRenderer;
		this.configManager = configManager;

		tileHighlights.clear();
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (client.isMenuOpen())
		{
			showMenuItem(graphics);
		}
		else
		{
			showHoveredItem();
		}

		tileHighlights.keySet().forEach(tile -> checkAllTilesForHighlighting(tile, tileHighlights.get(tile), graphics));

		return super.render(graphics);
	}

	private void showHoveredItem()
	{
		MenuEntry[] menuEntries = client.getMenuEntries();
		int last = menuEntries.length - 1;

		if (last < 0)
		{
			return;
		}

		MenuEntry menuEntry = menuEntries[last];

		if (!isTakeClue(menuEntry))
		{
			return;
		}

		String clueText = getText(menuEntry);

		tooltipManager.add(new Tooltip(clueText));
	}

	private void showMenuItem(Graphics2D graphics)
	{
		MenuEntry[] currentMenuEntries = client.getMenuEntries();

		if (currentMenuEntries != null)
		{
			Point mousePosition = client.getMouseCanvasPosition();
			int menuX = client.getMenuX();
			int menuY = client.getMenuY();
			int menuWidth = client.getMenuWidth();

			int menuEntryHeight = 15;
			int headerHeight = menuEntryHeight + 3;

			for (int i = 0; i < currentMenuEntries.length; i++)
			{
				MenuEntry hoveredEntry = currentMenuEntries[i];

				if (!isTakeClue(hoveredEntry)) continue;

				int realPos = currentMenuEntries.length - i - 1;
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

	private boolean isTakeClue(MenuEntry entry)
	{
		String target = entry.getTarget();
		String option = entry.getOption();
		MenuAction type = entry.getType();

		return type == MenuAction.GROUND_ITEM_THIRD_OPTION && target.contains("Clue scroll") && option.equals("Take");
	}

	private boolean shouldHighlight(int id)
	{
		String shouldHighlight = configManager.getConfiguration("clue-details-highlights", String.valueOf(id));
		return "true".equals(shouldHighlight);
	}

	private String getText(MenuEntry menuEntry)
	{
		Clues matchingClue = Clues.get(menuEntry.getIdentifier());
		if (matchingClue == null)
		{
			return "Can't determine clue.";
		}

		return matchingClue.getClueText();
	}

	@Subscribe
	public void onGameStateChanged(final GameStateChanged event)
	{
		if (event.getGameState() == GameState.LOADING)
		{
			tileHighlights.clear();
		}
	}

	// TODO: Need a on config changed to remove from list. OR each time we go to highlight check config but that seems bad
	// TODO: Need sidebar config to only show when logged in

	@Subscribe
	public void onItemSpawned(ItemSpawned itemSpawned)
	{
		TileItem item = itemSpawned.getItem();
		Tile tile = itemSpawned.getTile();
		if (shouldHighlight(item.getId()))
		{
			tileHighlights.get(tile).add(item.getId());
		}
	}

	@Subscribe
	public void onItemDespawned(ItemDespawned itemDespawned)
	{
		Tile tile = itemDespawned.getTile();
		if (tileHighlights.containsKey(tile))
		{
			// This fails
			for (Integer priorItem : tileHighlights.get(tile))
			{
				if (itemDespawned.getItem() == null) continue;
				if (priorItem == itemDespawned.getItem().getId())
				{
					tileHighlights.get(tile).remove(itemDespawned.getItem().getId());
				}
			}
		}
	}

	protected void addItemTiles()
	{
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

	private void checkAllTilesForHighlighting(Tile tile, Collection<Integer> ids, Graphics2D graphics)
	{
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
				10,
				JagexColors.CHAT_PUBLIC_TEXT_OPAQUE_BACKGROUND,
				10);
		}
	}
}
