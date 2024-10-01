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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.inject.Inject;
import lombok.Setter;
import net.runelite.api.Client;
import net.runelite.api.widgets.ComponentID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.components.LineComponent;

public class ClueDetailsWidgetOverlay extends OverlayPanel
{
	private final Client client;
	private final ClueDetailsPlugin clueDetailsPlugin;
	private final ClueDetailsConfig config;
	private final ConfigManager configManager;
	private final ItemManager itemManager;

	List<Widget> widgetsToHighlight = new ArrayList<>();

	@Setter
	private boolean inventoryChanged;

	private static final Color TITLED_CONTENT_COLOR = new Color(190, 190, 190);

	@Inject
	public ClueDetailsWidgetOverlay(Client client, ClueDetailsPlugin clueDetailsPlugin, ClueDetailsConfig config, ConfigManager configManager, ItemManager itemManager)
	{
		setPriority(PRIORITY_HIGHEST);
		setLayer(OverlayLayer.ABOVE_WIDGETS);

		this.client = client;
		this.clueDetailsPlugin = clueDetailsPlugin;
		this.config = config;
		this.configManager = configManager;
		this.itemManager = itemManager;
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (config.showInventoryCluesOverlay())
		{
			createInventoryCluesOverlay(graphics);
		}

		return super.render(graphics);
	}

	private void createInventoryCluesOverlay(Graphics2D graphics)
	{
		for (Clues clue : clueDetailsPlugin.getCluesInInventory())
		{
			if (clue == null) continue;

			panelComponent.getChildren().add(LineComponent.builder()
				.left(clue.getDisplayText(configManager))
				.leftColor(TITLED_CONTENT_COLOR)
				.build());

			if (inventoryChanged)
			{
				checkInvAndHighlightItems(graphics, clue);
				inventoryChanged = false;
				continue;
			}

			for (Widget widget : widgetsToHighlight)
			{
				highlightItem(graphics, widget);
			}
		}
	}

	protected Widget getInventoryWidget()
	{
		return client.getWidget(ComponentID.INVENTORY_CONTAINER);
	}

	private void checkInvAndHighlightItems(Graphics2D graphics, Clues clue)
	{
		List<Integer> highlightItems = clue.getItems(configManager);
		if (highlightItems == null) return;

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
				highlightItem(graphics, item);
			}
		}
	}
	private void highlightItem(Graphics2D graphics, Widget item)
		{
			Rectangle slotBounds = item.getBounds();
			BufferedImage outlined = itemManager.getItemOutline(item.getItemId(), item.getItemQuantity(), Color.YELLOW.darker());
			graphics.drawImage(outlined, (int) slotBounds.getX(), (int) slotBounds.getY(), null);
		}
}
