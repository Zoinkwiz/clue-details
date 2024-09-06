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

import javax.inject.Inject;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.Collection;
import java.util.List;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.overlay.WidgetItemOverlay;
import net.runelite.client.ui.overlay.components.TextComponent;

public class ClueDetailsTagsOverlay extends WidgetItemOverlay
{
	private final ClueDetailsConfig config;
	private final ConfigManager configManager;

	static final Collection<Clues> clues = List.of(Clues.values());

	@Inject
	public ClueDetailsTagsOverlay(ClueDetailsConfig config, ConfigManager configManager)
	{
		this.config = config;
		this.configManager = configManager;
		showOnInventory();
		showOnBank();
	}

	@Override
	public void renderItemOverlay(Graphics2D graphics, int itemId, WidgetItem widgetItem)
	{
		if (config.showInventoryClueTags()){
			for (Clues clue : clues)
			{
				if (clue.getClueID() == itemId)
				{
					graphics.setFont(FontManager.getRunescapeSmallFont());
					renderText(graphics, widgetItem.getCanvasBounds(), clue.getDisplayText(configManager));
					break;
				}
			}
		}
	}

	private void renderText(Graphics2D graphics, Rectangle bounds, String itemTag)
	{
		// Specific format due to backwards compatibility
		String[] itemTags = itemTag.split(": ", 2);
		final TextComponent textComponent = new TextComponent();
		textComponent.setColor(Color.white);

		int i = 0;
		for (String tag : itemTags){
			// Draw the text in the bottom left first, and top left second
			textComponent.setPosition(new Point(bounds.x - 1, bounds.y - 1 + (i == 1
					? bounds.height
					: graphics.getFontMetrics().getHeight())));
			textComponent.setText(tag);
			textComponent.render(graphics);
			i++;
		}
	}
}
