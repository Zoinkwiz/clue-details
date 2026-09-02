/*
 * Copyright (c) 2024, Zoinkwiz <https://www.github.com/Zoinkwiz>
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

import static com.cluedetails.ClueDetailsConfig.CLUE_ITEMS_CONFIG;
import static com.cluedetails.ClueDetailsConfig.CLUE_WIDGETS_CONFIG;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.awt.Color;
import java.util.List;
import lombok.Data;
import net.runelite.client.config.ConfigManager;

@Data
public class ClueIdToDetails
{
	int id;
	String text;
	Color color;
	List<Integer> itemIds;
	List<WidgetId> widgetIds;

	public ClueIdToDetails(int id, String text, Color color, List<Integer> itemIds, List<WidgetId> widgetIds)
	{
		this.id = id;
		this.text = text;
		this.color = color;
		this.itemIds = itemIds;
		this.widgetIds = widgetIds;
	}

	public static ClueIdToDetails generateDetail(int clueID, ConfigManager configManager, Gson gson, boolean exportText, boolean exportColors, boolean exportItems, boolean exportWidgets) {
		String clueText = exportText ? configManager.getConfiguration("clue-details-text", String.valueOf(clueID)) : null;
		String clueColor = exportColors ? configManager.getConfiguration("clue-details-color", String.valueOf(clueID)) : null;
		String clueItems = exportItems ? configManager.getConfiguration(CLUE_ITEMS_CONFIG, String.valueOf(clueID)) : null;
		String clueWidgets = exportWidgets ? configManager.getConfiguration(CLUE_WIDGETS_CONFIG, String.valueOf(clueID)) : null;

		// Try to export text, color, and items. Export where valid configurations are returned
		List<Integer> loadedClueItemsData = clueItems != null
			? gson.fromJson(clueItems, new TypeToken<List<Integer>>(){}.getType())
			: null;

		List<WidgetId> loadedClueWidgetsData = clueWidgets != null
			? gson.fromJson(clueWidgets, new TypeToken<List<WidgetId>>(){}.getType())
			: null;

		Color exportedColor = clueColor != null ? Color.decode(clueColor) : null;
		return new ClueIdToDetails(clueID, clueText, exportedColor, loadedClueItemsData, loadedClueWidgetsData);
	}

	public static ClueIdToDetails generateDetail(int clueID, ConfigManager configManager, Gson gson) {
		return generateDetail(clueID, configManager, gson, true, true, true, true);
	}

	public static boolean equalRGB(Color color1, Color color2)
	{
		boolean equalRed = color1.getRed() == color2.getRed();
		boolean equalGreen = color1.getGreen() == color2.getGreen();
		boolean equalBlue = color1.getBlue() == color2.getBlue();

		return equalRed && equalGreen && equalBlue;
	}
}
