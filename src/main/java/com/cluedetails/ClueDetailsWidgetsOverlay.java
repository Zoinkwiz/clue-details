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

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.runelite.api.Client;
import net.runelite.api.Point;
import net.runelite.api.widgets.Widget;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;

import javax.inject.Inject;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Area;
import java.util.*;

public class ClueDetailsWidgetsOverlay extends OverlayPanel
{
	private final Client client;
	private final ClueDetailsConfig config;
	private final ConfigManager configManager;

	private ClueInventoryManager clueInventoryManager;
	private CluePreferenceManager cluePreferenceManager;

	private final Cache<Integer, Color> clueColorCache;

	@Inject
	public ClueDetailsWidgetsOverlay(Client client, ClueDetailsConfig config, ConfigManager configManager)
	{
		setPriority(PRIORITY_HIGHEST);
		setLayer(OverlayLayer.ABOVE_WIDGETS);
		setDragTargetable(false);
		setPosition(OverlayPosition.DYNAMIC);

		this.client = client;
		this.config = config;
		this.configManager = configManager;

		this.clueColorCache = CacheBuilder.newBuilder()
			.concurrencyLevel(1)
			.maximumSize(100)
			.build();
	}

	public void startUp(ClueInventoryManager clueInventoryManager, CluePreferenceManager cluePreferenceManager)
	{
		this.clueInventoryManager = clueInventoryManager;
		this.cluePreferenceManager = cluePreferenceManager;
	}

	public void populateInventoryCluesWidgetColors()
	{
		Color defaultHighlightColor = config.widgetHighlightColor();
		Map<Integer, Color> clueWidgetColors = new HashMap<>();
		if (config.highlightInventoryClueWidgets())
		{
			for (Integer itemID : clueInventoryManager.getCluesInInventory())
			{
				if (itemID == null) continue;
				ClueInstance instance = clueInventoryManager.getClueByClueItemId(itemID);
				if (instance == null)
				{
					continue;
				}
				for (int clueId : instance.getClueIds())
				{
					Clues clue = Clues.forClueIdFiltered(clueId);
					if (clue == null || !clue.isEnabled(config))
					{
						continue;
					}
					Color clueColor = clue.getDetailColor(configManager);
					Color widgetColor = config.colorInventoryClueWidgets()
						? new Color(clueColor.getRed(), clueColor.getGreen(), clueColor.getBlue(), defaultHighlightColor.getAlpha())
						: defaultHighlightColor;
					List<Integer> widgetsPreference = cluePreferenceManager.getWidgetsPreference(clueId);
					if (widgetsPreference != null)
					{
						for (int widgetId : widgetsPreference) {
							clueWidgetColors.put(widgetId, widgetColor);
						}
					}
				}
			}
		}
		clueColorCache.invalidateAll();
		clueColorCache.putAll(clueWidgetColors);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (config.highlightInventoryClueWidgets() && clueColorCache.size() > 0)
		{
			for (Map.Entry<Integer, Color> entry : clueColorCache.asMap().entrySet())
			{
				int widgetId = entry.getKey();
				Color widgetColor = entry.getValue();

				Widget widget = client.getWidget(widgetId);
				if (widget == null || widget.isHidden())
				{
					continue;
				}

				graphics.setColor(widgetColor);

				// Highlight as much of the widget as is visible of it inside its parent
				Widget parent = widget.getParent();
				if (parent != null)
				{
					Point widgetLocation = widget.getCanvasLocation();
					if (widgetLocation == null)
					{
						continue;
					}

					Point parentLocation = parent.getCanvasLocation();
					if (parentLocation.getY() > widgetLocation.getY() + widget.getHeight()
							|| parentLocation.getY() + parent.getHeight() < widgetLocation.getY())
					{
						continue;
					}

					int x = widgetLocation.getX(); // no horizontal scrolling affecting location
					int y = Math.max(widgetLocation.getY(), parentLocation.getY());
					int width = widget.getWidth(); // no horizontal scrolling affecting width
					int height = Math.min(widget.getHeight(), parent.getHeight());

					Area widgetArea = new Area(new Rectangle(x, y, width, height));
					graphics.fill(widgetArea);
				}
				else
				{
					graphics.fill(widget.getBounds());
				}
			}
		}
		return super.render(graphics);
	}
}
