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

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Client;
import net.runelite.api.InventoryID;
import net.runelite.api.ItemContainer;
import net.runelite.api.ItemID;
import net.runelite.api.widgets.InterfaceID;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.inventorytags.InventoryTagsConfig;
import net.runelite.client.ui.overlay.WidgetItemOverlay;
import net.runelite.client.util.ColorUtil;
import net.runelite.client.util.ImageUtil;

@Singleton
public class ClueDetailsItemsOverlay extends WidgetItemOverlay
{
	private final Client client;
	private final ClueDetailsPlugin clueDetailsPlugin;
	private final ClueDetailsConfig config;
	private final ConfigManager configManager;
	private final ClueInventoryManager clueInventoryManager;
	private final ItemManager itemManager;
	private final Cache<Long, Image> fillCache;
	private final Cache<Integer, Clues> clueCache;

	@Inject
	public ClueDetailsItemsOverlay(Client client, ClueDetailsPlugin clueDetailsPlugin, ClueDetailsConfig config,
	                               ConfigManager configManager, ItemManager itemManager, ClueInventoryManager clueInventoryManager)
	{
		this.clueDetailsPlugin = clueDetailsPlugin;
		this.itemManager = itemManager;
		this.client = client;
		this.config = config;
		this.configManager = configManager;
		this.clueInventoryManager = clueInventoryManager;
		showOnBank();
		showOnEquipment();
		showOnInventory();
		fillCache = CacheBuilder.newBuilder()
			.concurrencyLevel(1)
			.maximumSize(32)
			.build();
		clueCache = CacheBuilder.newBuilder()
			.concurrencyLevel(1)
			.maximumSize(39)
			.build();
		setPriority(-1); // run before inventory tags
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (config.highlightInventoryClueScrolls() || config.highlightInventoryClueItems())
		{
			populateCache();
		}

		return super.render(graphics);
	}

	private void populateCache()
	{
		ItemContainer inventory = client.getItemContainer(InventoryID.INVENTORY);
		if (inventory == null || clueInventoryManager == null ) return;

		for (Integer itemID : clueInventoryManager.getCluesInInventory())
		{
			if (itemID == null) continue;
			ClueInstance instance = clueInventoryManager.getClueByClueItemId(itemID);
			if (instance == null) continue;

			instance.getClueIds().forEach((clueId) ->
			{
				Clues clue = Clues.forClueIdFiltered(clueId);
				if (clue == null) return;
				if (clue.isEnabled(config))
				{
					if (config.highlightInventoryClueScrolls())
					{
						cacheClueScrolls(clue);
					}
					if (config.highlightInventoryClueItems())
					{
						cacheClueItems(clue);
					}
				}
			});
		}
	}

	private void cacheClueScrolls(Clues clue)
	{
		int clueItemID = clue.getItemID();

		// Convert beginner map clues
		if (clueItemID >= InterfaceID.CLUE_BEGINNER_MAP_CHAMPIONS_GUILD
			&& clueItemID <= InterfaceID.CLUE_BEGINNER_MAP_WIZARDS_TOWER)
		{
			clueItemID = ItemID.CLUE_SCROLL_BEGINNER;
		}

		clueCache.put(clueItemID, clue);
	}

	private void cacheClueItems(Clues clue)
	{
		List<Integer> highlightItems = clue.getItems(clueDetailsPlugin, configManager);

		if (highlightItems == null)
		{
			return;
		}

		highlightItems.forEach(item -> clueCache.put(item, clue));
	}

	@Override
	public void renderItemOverlay(Graphics2D graphics, int itemId, WidgetItem widgetItem)
	{
		final Clues clue = clueCache.getIfPresent(itemId);
		if (clue == null || clue.getClueDetailColor() == null)
		{
			return;
		}

		Color color = clue.getDetailColor(configManager);

		// Apply default item highlight color
		if (!Clues.isClue(itemId, clueDetailsPlugin.isDeveloperMode()) && !config.colorInventoryClueItems())
		{
			color = config.itemHighlightColor();
		}

		inventoryTagsOverlay(graphics, itemId, widgetItem, color);
	}

	public void inventoryTagsOverlay(Graphics2D graphics, int itemId, WidgetItem widgetItem, Color color)
	{
		Rectangle bounds = widgetItem.getCanvasBounds();
		if (Boolean.TRUE.equals(configManager.getConfiguration(InventoryTagsConfig.GROUP,
			"showTagOutline", Boolean.class)))
		{
			final BufferedImage outline = itemManager.getItemOutline(itemId, widgetItem.getQuantity(), color);
			graphics.drawImage(outline, (int) bounds.getX(), (int) bounds.getY(), null);
		}

		if (Boolean.TRUE.equals(configManager.getConfiguration(InventoryTagsConfig.GROUP,
			"tagFill", Boolean.class)))
		{
			final Image image = getFillImage(color, widgetItem.getId(), widgetItem.getQuantity());
			graphics.drawImage(image, (int) bounds.getX(), (int) bounds.getY(), null);
		}

		if (Boolean.TRUE.equals(configManager.getConfiguration(InventoryTagsConfig.GROUP,
			"tagUnderline", Boolean.class)))
		{
			int heightOffSet = (int) bounds.getY() + (int) bounds.getHeight() + 2;
			graphics.setColor(color);
			graphics.drawLine((int) bounds.getX(), heightOffSet, (int) bounds.getX() + (int) bounds.getWidth(), heightOffSet);
		}
	}

	private Image getFillImage(Color color, int itemId, int qty)
	{
		long key = (((long) itemId) << 32) | qty;
		Image image = fillCache.getIfPresent(key);
		if (image == null)
		{
			final Color fillColor = ColorUtil.colorWithAlpha(color,
				Integer.parseInt(configManager.getConfiguration(InventoryTagsConfig.GROUP,  "fillOpacity")));
			image = ImageUtil.fillImage(itemManager.getImage(itemId, qty, false), fillColor);
			fillCache.put(key, image);
		}
		return image;
	}

	void invalidateCache()
	{
		fillCache.invalidateAll();
		clueCache.invalidateAll();
	}
}
