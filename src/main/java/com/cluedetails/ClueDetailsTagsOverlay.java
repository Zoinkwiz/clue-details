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

import javax.inject.Inject;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import net.runelite.api.ItemID;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.overlay.WidgetItemOverlay;
import net.runelite.client.ui.overlay.components.TextComponent;

public class ClueDetailsTagsOverlay extends WidgetItemOverlay
{
	private final ClueDetailsPlugin clueDetailsPlugin;
	private final ClueDetailsConfig config;
	private final ConfigManager configManager;

	@Inject
	public ClueDetailsTagsOverlay(ClueDetailsPlugin clueDetailsPlugin, ClueDetailsConfig config, ConfigManager configManager)
	{
		this.clueDetailsPlugin = clueDetailsPlugin;
		this.config = config;
		this.configManager = configManager;
		showOnInventory();
		showOnBank();
	}

	@Override
	public void renderItemOverlay(Graphics2D graphics, int itemId, WidgetItem widgetItem)
	{
		if (config.showInventoryClueTags())
		{
			Clues clue = Clues.get(itemId);
			String itemTag = null;

			// If clue can't be found by Clue ID, check if it can be found by Clue text
			if (clue == null)
			{
				if (itemId == ItemID.CLUE_SCROLL_MASTER &&
					clueDetailsPlugin.foundTrackedClue())
				{
					String openedText = clueDetailsPlugin.getReadClueText();

					// Handle master clues
					String getText = ClueText.forText(openedText);

					if (getText != null)
					{
						itemTag = getText;
					}

					// Handle three step cryptic clues
					final ThreeStepCrypticClue threeStepCrypticClue = ThreeStepCrypticClue.forText(openedText);

					if (threeStepCrypticClue != null)
					{
						threeStepCrypticClue.update(clueDetailsPlugin.getTrackedCluesInInventory());
						itemTag = threeStepCrypticClue.getTag();
					}
					renderText(graphics, widgetItem.getCanvasBounds(), itemTag);
				}
			}
			else
			{
				itemTag = clue.getDisplayText(configManager);
				renderText(graphics, widgetItem.getCanvasBounds(), itemTag);
			}
		}
	}

	public int textPosition(Graphics2D graphics, Rectangle bounds, int i, boolean threeStep)
	{
		// Return text position in the middle of item
		if (threeStep && i == 1)
		{
			return (bounds.height + graphics.getFontMetrics().getHeight()) / 2;
		}

		boolean bottomText =
			(config.clueTagLocation() == ClueDetailsConfig.ClueTagLocation.SPLIT  && i == 1) ||
			config.clueTagLocation() == ClueDetailsConfig.ClueTagLocation.BOTTOM ||
			i == 2;

		// Return text position at bottom of item, otherwise top
		return bottomText ? bounds.height : graphics.getFontMetrics().getHeight();
	}

	private void renderText(Graphics2D graphics, Rectangle bounds, String itemTag)
	{
		graphics.setFont(FontManager.getRunescapeSmallFont());

		final TextComponent textComponent = new TextComponent();
		textComponent.setColor(Color.white);

		String[] itemTags = new String [] {itemTag};
		boolean threeStep = false;
		// Handle Three Step Cryptic Clues
		if (itemTag.contains("<br>"))
		{
			threeStep = true;
			itemTags = itemTag.split("<br>");
		}
		// Handle all other Clues
		else if (config.clueTagLocation() == ClueDetailsConfig.ClueTagLocation.SPLIT
				&& !config.clueTagSplit().isEmpty())
		{
			itemTags = itemTag.split(config.clueTagSplit(), 2);
		}

		int i = 0;
		for (String tag : itemTags)
		{
			textComponent.setPosition(new Point(
				bounds.x - 1,
				bounds.y - 1 + textPosition(graphics, bounds, i, threeStep)
			));
			textComponent.setText(tag);
			textComponent.render(graphics);
			i++;
		}
	}
}
