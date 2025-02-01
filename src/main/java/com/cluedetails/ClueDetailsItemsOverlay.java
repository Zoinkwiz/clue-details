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

import com.cluedetails.filters.ClueTier;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.util.List;
import javax.inject.Inject;
import lombok.Setter;
import net.runelite.api.Client;
import net.runelite.api.InventoryID;
import net.runelite.api.ItemContainer;
import net.runelite.api.widgets.ComponentID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.util.ColorUtil;
import net.runelite.client.util.ImageUtil;

public class ClueDetailsItemsOverlay extends OverlayPanel
{
	private final Client client;
	private final ClueDetailsPlugin clueDetailsPlugin;
	private final ClueDetailsConfig config;
	private final ConfigManager configManager;
	@Setter
	private ClueInventoryManager clueInventoryManager;
	private final ItemManager itemManager;

	@Inject
	public ClueDetailsItemsOverlay(Client client, ClueDetailsPlugin clueDetailsPlugin, ClueDetailsConfig config, ConfigManager configManager, ItemManager itemManager)
	{
		this.clueDetailsPlugin = clueDetailsPlugin;
		this.itemManager = itemManager;
		setPriority(PRIORITY_HIGHEST);
		setLayer(OverlayLayer.ABOVE_WIDGETS);
		setPosition(OverlayPosition.DYNAMIC);

		this.client = client;
		this.config = config;
		this.configManager = configManager;
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (config.highlightInventoryClueItems())
		{
			createHighlightInventoryClueItems(graphics);
		}

		return super.render(graphics);
	}

	private void createHighlightInventoryClueItems(Graphics2D graphics)
	{
		ItemContainer inventory = client.getItemContainer(InventoryID.INVENTORY);
		if (inventory == null || clueInventoryManager == null ) return;

		// Highlight for easy-elite clues
		for (Clues clue : clueInventoryManager.getCluesInInventory())
		{
			if (clue == null) continue;

			if (isEnabled(clue))
			{
				checkInvAndHighlightItems(graphics, clue);
			}
		}

		// Highlight for beginner and master clues
		for (Integer itemID : clueInventoryManager.getTrackedCluesInInventory())
		{
			if (itemID == null) continue;
			ClueInstance instance = clueInventoryManager.getTrackedClueByClueItemId(itemID);
			if (instance == null) continue;

			instance.getClueIds().forEach((clueId) -> {
				Clues clue = Clues.forClueIdFiltered(clueId);
				if (clue == null) return;
				if (isEnabled(clue))
				{
					checkInvAndHighlightItems(graphics, clue);
				}
			});
		}
	}

	private boolean isEnabled(Clues clue)
	{
		ClueTier tier = clue.getClueTier();

		if (config == null) return true;

		if (tier == ClueTier.BEGINNER)
		{
			return config.beginnerDetails();
		}
		if (tier == ClueTier.EASY)
		{
			return config.easyDetails();
		}
		if (tier == ClueTier.MEDIUM || tier == ClueTier.MEDIUM_CHALLENGE || tier == ClueTier.MEDIUM_KEY)
		{
			return config.mediumDetails();
		}
		if (tier == ClueTier.HARD || tier == ClueTier.HARD_CHALLENGE)
		{
			return config.hardDetails();
		}
		if (tier == ClueTier.ELITE || tier == ClueTier.ELITE_CHALLENGE)
		{
			return config.eliteDetails();
		}
		if (tier == ClueTier.MASTER)
		{
			return config.masterDetails();
		}
		return true;
	}

	protected Widget getInventoryWidget()
	{
		return client.getWidget(ComponentID.INVENTORY_CONTAINER);
	}

	private void checkInvAndHighlightItems(Graphics2D graphics, Clues clue)
	{
		List<Integer> highlightItems = clue.getItems(clueDetailsPlugin, configManager);

		if (highlightItems == null)
		{
			return;
		}

		Widget inventoryWidget = getInventoryWidget();
		if (inventoryWidget == null || inventoryWidget.isHidden())
		{
			return;
		}

		if (inventoryWidget.getDynamicChildren() == null)
		{
			return;
		}

		for (Widget item : inventoryWidget.getDynamicChildren())
		{
			if (highlightItems.contains(item.getItemId()))
			{
				Color itemHighlightColor = config.itemHighlightColor();

				Color clueColor = clue.getDetailColor(configManager);
				if (config.colorInventoryClueItems() && clueColor != Color.WHITE)
				{
					itemHighlightColor = clueColor;
				}
				renderItemOverlay(graphics, item, itemHighlightColor);
			}
		}
	}

	public void renderItemOverlay(Graphics2D graphics, Widget item, Color color)
	{
		Rectangle bounds = item.getBounds();
		final Image image = getFillImage(color, item.getItemId(), item.getItemQuantity());
		graphics.drawImage(image, (int) bounds.getX(), (int) bounds.getY(), null);
	}

	private Image getFillImage(Color color, int itemId, int qty)
	{
		final Color fillColor = ColorUtil.colorWithAlpha(color, 100);

		return ImageUtil.fillImage(itemManager.getImage(itemId, qty, false), fillColor);
	}
}
