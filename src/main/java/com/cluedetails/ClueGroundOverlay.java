/*
 * Copyright (c) 2024, TheLope <https://github.com/TheLope>
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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.Perspective;
import net.runelite.api.Player;
import net.runelite.api.Point;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.TextComponent;
import net.runelite.client.util.QuantityFormatter;
import org.apache.commons.text.WordUtils;

// Heavily lifted from net.runelite.client.plugins.grounditems.GroundItemsOverlay
public class ClueGroundOverlay extends Overlay
{
	private static final int MAX_DISTANCE = 2500;
	// We must offset the text on the z-axis such that
	// it doesn't obscure the ground items below it.
	private static final int OFFSET_Z = 20;
	// Clue item height
	private static final int CLUE_ITEM_HEIGHT = 0;
	// The 15 pixel gap between each drawn ground item.
	private static final int STRING_GAP = 15;

	private final Client client;
	private final ClueDetailsConfig config;
	private final StringBuilder itemStringBuilder = new StringBuilder();
	private final TextComponent textComponent = new TextComponent();
	private final Map<WorldPoint, Integer> offsetMap = new HashMap<>();
	private final ConfigManager configManager;
	private final ClueDetailsPlugin plugin;
	private ClueGroundManager clueGroundManager;

	@Inject
	private ClueGroundOverlay(ClueDetailsPlugin plugin, Client client, ClueDetailsConfig config, ConfigManager configManager)
	{
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_SCENE);
		this.plugin = plugin;
		this.client = client;
		this.config = config;
		this.configManager = configManager;
	}

	public void startUp(ClueGroundManager clueGroundManager)
	{
		this.clueGroundManager = clueGroundManager;
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if(clueGroundManager == null) return null;

		if (!config.showGroundClues())
		{
			return null;
		}

		final FontMetrics fm = graphics.getFontMetrics();
		final Player player = client.getLocalPlayer();

		if (player == null)
		{
			return null;
		}

		offsetMap.clear();
		final LocalPoint localLocation = player.getLocalLocation();

		// Handle beginner and master clues
		if (clueGroundManager.getGroundClues().keySet().isEmpty()
			|| (!config.beginnerDetails() && !config.masterDetails()))
		{
			return null;
		}

		for (WorldPoint wp : clueGroundManager.getGroundClues().keySet())
		{
			// Check if wp in clueGroundManager is within range of the player
			final LocalPoint groundPoint = LocalPoint.fromWorld(client, wp);

			if (groundPoint == null || localLocation.distanceTo(groundPoint) > MAX_DISTANCE)
			{
				continue;
			}

			// Get list of ClueInstances at wp with optionally collapsed quantities
			Map<ClueInstance, Integer> clueInstancesAtWpMap = getClueInstancesAtWpMap(wp, client.getTickCount());

			if (clueInstancesAtWpMap == null)
			{
				continue;
			}

			for (Map.Entry<ClueInstance, Integer> entry : clueInstancesAtWpMap.entrySet())
			{
				ClueInstance item = entry.getKey();

				if(item.isEnabled(config))
				{
					int quantity = entry.getValue();
					renderClueInstanceGroundOverlay(graphics, item, quantity, groundPoint, fm);
				}
			}
		}

		return null;
	}

	private Map<ClueInstance, Integer> getClueInstancesAtWpMap(WorldPoint wp, int currentTick)
	{
		if (clueGroundManager.getGroundClues().get(wp).isEmpty()) return null;

		List<ClueInstance> groundItemList = clueGroundManager.getGroundClues().get(wp);
		Map<ClueInstance, Integer> groundItemMap = new HashMap<>();

		if (config.collapseGroundClues())
		{
			groundItemMap = keepOldestUniqueClues(groundItemList, currentTick);
		}
		else
		{
			for (ClueInstance item : groundItemList)
			{
				groundItemMap.put(item, 1);
			}
		}
		return groundItemMap;
	}

	private void renderClueInstanceGroundOverlay(Graphics2D graphics, ClueInstance item, int quantity, LocalPoint groundPoint, FontMetrics fm)
	{
		Color color = Color.WHITE;

		if (item.getClueIds().isEmpty())
		{
			itemStringBuilder.append(item.getItemName(plugin));
		}
		else
		{
			int clueId = item.getClueIds().get(0);
			Clues clueDetails = Clues.forClueIdFiltered(clueId);

			if (clueDetails == null)
			{
				return;
			}

			String clueText;
			if (config.changeGroundClueText())
			{
				if (item.getClueIds().size() > 1)
				{
					clueText = "Three-step (master)";
				}
				else
				{
					clueText = clueDetails.getDetail(configManager);
				}
			}
			else
			{
				clueText = WordUtils.capitalizeFully(clueDetails.getClueTier().toString());
			}

			itemStringBuilder.append(clueText);

			if (config.colorGroundClues())
			{
				color = clueDetails.getDetailColor(configManager);
			}
		}

		if (config.collapseGroundClues() && quantity > 1)
		{
			itemStringBuilder.append(" (")
				.append(QuantityFormatter.quantityToStackSize(quantity))
				.append(')');
		}

		final String itemString = itemStringBuilder.toString();
		itemStringBuilder.setLength(0);

		final Point textPoint = Perspective.getCanvasTextLocation(client,
			graphics,
			groundPoint,
			itemString,
			CLUE_ITEM_HEIGHT + OFFSET_Z);

		if (textPoint == null)
		{
			return;
		}

		final int offset = offsetMap.compute(item.getLocation(), (k, v) -> v != null ? v + 1 : 0);

		final int textX = textPoint.getX();
		final int textY = textPoint.getY() - (STRING_GAP * offset);

		if (config.showGroundCluesDespawn())
		{
			Integer despawnTime = item.getDespawnTick(client.getTickCount()) - client.getTickCount();
			Color timerColor = Color.WHITE;

			final String timerText = String.format(" - %d", despawnTime);

			// The timer text is drawn separately to have its own color, and is intentionally not included
			// in the getCanvasTextLocation() call because the timer text can change per frame and we do not
			// use a monospaced font, which causes the text location on screen to jump around slightly each frame.
			textComponent.setText(timerText);
			textComponent.setColor(timerColor);
			textComponent.setPosition(new java.awt.Point(textX + fm.stringWidth(itemString), textY));
			textComponent.render(graphics);
		}

		textComponent.setText(itemString);
		textComponent.setColor(color);
		textComponent.setPosition(new java.awt.Point(textX, textY));
		textComponent.render(graphics);
	}

	// Remove duplicate clues, maintaining a count of the original amount of each
	public static Map<ClueInstance, Integer> keepOldestUniqueClues(List<ClueInstance> items, int currentTick) {
		Map<List<Integer>, ClueInstance> lowestValueItems = new HashMap<>();
		Map<List<Integer>, Integer> uniqueCount = new HashMap<>();

		for (ClueInstance item : items) {
			if (!lowestValueItems.containsKey(item.getClueIds()) || item.getDespawnTick(currentTick) < lowestValueItems.get(item.getClueIds()).getDespawnTick(currentTick)) {
				lowestValueItems.put(item.getClueIds(), item);
				uniqueCount.put(item.getClueIds(), 1);
			} else {
				uniqueCount.put(item.getClueIds(), uniqueCount.get(item.getClueIds()) + 1);
			}
		}

		return lowestValueItems.values().stream()
			.collect(Collectors.toMap(item -> item, item -> uniqueCount.get(item.getClueIds())));
	}
}
