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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.Perspective;
import net.runelite.api.Player;
import net.runelite.api.Point;
import net.runelite.api.Tile;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.ui.JagexColors;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.TextComponent;
import net.runelite.client.ui.overlay.outline.ModelOutlineRenderer;

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
	private final TextComponent textComponent = new TextComponent();
	private final Map<WorldPoint, Integer> offsetMap = new HashMap<>();
	private final ConfigManager configManager;
	private final ClueDetailsPlugin plugin;
	private ClueGroundManager clueGroundManager;
	protected ModelOutlineRenderer modelOutlineRenderer;

	@Inject
	private ClueGroundOverlay(ClueDetailsPlugin plugin, Client client, ClueDetailsConfig config, ConfigManager configManager, ModelOutlineRenderer modelOutlineRenderer)
	{
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.UNDER_WIDGETS);
		this.plugin = plugin;
		this.client = client;
		this.config = config;
		this.configManager = configManager;
		this.modelOutlineRenderer = modelOutlineRenderer;
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

		if (clueGroundManager.getTrackedWorldPoints().isEmpty())
		{
			return null;
		}

		for (WorldPoint wp : clueGroundManager.getTrackedWorldPoints())
		{
			// Check if wp in clueGroundManager is within range of the player
			final LocalPoint groundPoint = LocalPoint.fromWorld(client.getTopLevelWorldView(), wp);

			if (groundPoint == null || localLocation.distanceTo(groundPoint) > MAX_DISTANCE)
			{
				continue;
			}

			if (config.highlightMarkedClues())
			{
				checkWorldPointForHighlighting(wp);
			}

			// Get list of ClueInstances at wp with optionally collapsed quantities
			Map<ClueInstance, Integer> clueInstancesWithQuantityAtWp = clueGroundManager.getClueInstancesWithQuantityAtWp(config, wp, client.getTickCount());

			if (clueInstancesWithQuantityAtWp == null)
			{
				continue;
			}

			List<Map.Entry<ClueInstance, Integer>> entrySet = new ArrayList<>(clueInstancesWithQuantityAtWp.entrySet());
			for (Map.Entry<ClueInstance, Integer> entry : entrySet)
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

	private int getSecondsLeft(ClueInstance item)
	{
		int ticksLeft = item.getDespawnTick(client.getTickCount()) - client.getTickCount();
		int millisLeft = ticksLeft * 600;
		return (int) (millisLeft / 1000L);
	}

	public String getDespawnText(ClueInstance item)
	{
		int seconds = getSecondsLeft(item);
		int minutes = seconds / 60;
		int secs = seconds % 60;
		if (minutes < 10)
		{
			if (minutes < 1)
			{
				return String.format("%ds", secs);
			}
			return String.format("%d:%02d", minutes, secs);
		}
		return String.format("%dm", minutes);
	}

	public Color getTextColor(ClueInstance item)
	{
		return getSecondsLeft(item) < config.groundClueTimersNotificationTime()
			? Color.RED
			: Color.WHITE;
	}

	private void renderClueInstanceGroundOverlay(Graphics2D graphics, ClueInstance item, int quantity, LocalPoint groundPoint, FontMetrics fm)
	{
		final String itemString = item.getGroundText(plugin, config, configManager, quantity);

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
			Color timerColor = getTextColor(item);

			final String timerText = String.format(" - %s", getDespawnText(item));

			// The timer text is drawn separately to have its own color, and is intentionally not included
			// in the getCanvasTextLocation() call because the timer text can change per frame and we do not
			// use a monospaced font, which causes the text location on screen to jump around slightly each frame.
			textComponent.setText(timerText);
			textComponent.setColor(timerColor);
			textComponent.setPosition(new java.awt.Point(textX + fm.stringWidth(itemString), textY));
			textComponent.render(graphics);
		}

		textComponent.setText(itemString);
		textComponent.setColor(item.getGroundColor(config, configManager));
		textComponent.setPosition(new java.awt.Point(textX, textY));
		textComponent.render(graphics);
	}

	private void checkWorldPointForHighlighting(WorldPoint worldPoint)
	{
		SortedSet<ClueInstance> clueInstances = clueGroundManager.getAllGroundCluesOnWp(worldPoint);
		if (!clueInstances.isEmpty())
		{
			if (clueInstances.stream().anyMatch(clueInstance
				-> clueInstance.getClueIds().stream().anyMatch(clueId
					-> Clues.forClueId(clueId).getMarked(configManager))))
			{
				Tile[][][] tiles = client.getScene().getTiles();
				LocalPoint local = LocalPoint.fromWorld(client, worldPoint);
				Tile tile = tiles[worldPoint.getPlane()][local.getSceneX()][local.getSceneY()];

				// TODO: Currently outlines all items in the tile
				modelOutlineRenderer.drawOutline(
					tile.getItemLayer(),
					config.outlineWidth(),
					JagexColors.CHAT_PUBLIC_TEXT_OPAQUE_BACKGROUND,
					config.highlightFeather());
			}
		}
	}
}
