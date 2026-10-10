/*
 * Copyright (c) 2021, Adam <Adam@sigterm.info>
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

import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import java.awt.Color;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.swing.*;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.grounditems.GroundItemsConfig;
import net.runelite.client.plugins.inventorytags.InventoryTagsConfig;

@Slf4j
public class ClueDetailsSharingManager
{
	private final ClueDetailsPlugin plugin;
	private final ClueDetailsConfig config;
	private final Gson gson;

	private final ConfigManager configManager;

	@Inject
	private ClueDetailsSharingManager(ClueDetailsPlugin plugin, ClueDetailsConfig config, Gson gson, ConfigManager configManager)
	{
		this.plugin = plugin;
		this.config = config;
		this.gson = gson;
		this.configManager = configManager;
	}

	public void resetClueDetails(boolean resetText, boolean resetColors, boolean resetItems, boolean resetWidgets)
	{
		if (!SwingUtilities.isEventDispatchThread())
		{
			SwingUtilities.invokeLater(() -> resetClueDetails(resetText, resetColors, resetItems, resetWidgets));
			return;
		}
		// Capture the selected rows before the worker starts or the user changes filters.
		final List<Clues> filteredClues = getFilteredClues();
		SwingWorker<Integer, String> worker = new SwingWorker<>()
		{
			@Override
			protected Integer doInBackground() throws Exception
			{
				int counter = 0;

				for (Clues clue : filteredClues)
				{
					// Adds the data to the chunk which was processed
					publish("Resetting clue details... (" + ++counter + "/" + filteredClues.size() + ")");
					int id = clue.getClueID();
					if (resetText) configManager.unsetConfiguration("clue-details-text", String.valueOf(id));
					if (resetColors) configManager.unsetConfiguration("clue-details-color", String.valueOf(id));
					if (resetItems) configManager.unsetConfiguration(CLUE_ITEMS_CONFIG, String.valueOf(id));
					if (resetWidgets) configManager.unsetConfiguration(CLUE_WIDGETS_CONFIG, String.valueOf(id));
				}
				return filteredClues.size();
			}

			@Override
			protected void process(List<String> chunks)
			{
				if (plugin.getPanel() != null && plugin.getPanel().isVisible())
				{
					// Work is now in chunks, so grab the last processed value from the chunk
					plugin.getPanel().updateStatus(chunks.get(chunks.size() - 1));
				}
			}

			@Override
			protected void done()
			{
				try
				{
					plugin.getPanel().updateStatusTemporarily(get() + " clue details were reset.", 5000);
					sendChatMessage(get() + " clue details were reset.");
				}
				catch (Exception exception)
				{
					log.error("Error resetting clue details", exception);
				}
			}
		};
		worker.execute();
	}

	public void exportClueDetails(boolean exportText, boolean exportColors, boolean exportItems, boolean exportWidgets)
	{
		if (!SwingUtilities.isEventDispatchThread())
		{
			SwingUtilities.invokeLater(() -> exportClueDetails(exportText, exportColors, exportItems, exportWidgets));
			return;
		}
		final List<Clues> filteredClues = getFilteredClues();
		SwingWorker<List<ClueIdToDetails>, String> worker = new SwingWorker<>()
		{
			@Override
			protected List<ClueIdToDetails> doInBackground() throws Exception
			{
				List<ClueIdToDetails> clueIdToDetailsList = new ArrayList<>();

				int counter = 0;

				for (Clues clue : filteredClues)
				{
					publish("Exporting clue details... (" + ++counter + "/" + filteredClues.size() + ")");
					int id = clue.getClueID();

					ClueIdToDetails clueDetails = ClueIdToDetails.generateDetail(id, configManager, gson, exportText, exportColors, exportItems, exportWidgets);
					if (clueDetails.getText() != null || clueDetails.getColor() != null || clueDetails.getItemIds() != null || clueDetails.getWidgetIds() != null)
					{
						clueIdToDetailsList.add(clueDetails);
					}
				}

				if (clueIdToDetailsList.isEmpty())
				{
					publish("You have no updated clue details to export.");
					return clueIdToDetailsList;
				}
				return clueIdToDetailsList;
			}

			@Override
			protected void process(List<String> chunks)
			{
				if (plugin.getPanel() != null && plugin.getPanel().isVisible())
				{
					plugin.getPanel().updateStatus(chunks.get(chunks.size() - 1));
				}
			}

			@Override
			protected void done()
			{
				try
				{
					List<ClueIdToDetails> details = get();
					if (details.isEmpty())
					{
						plugin.getPanel().updateStatusTemporarily("No clue details to export.", 5000);
						sendChatMessage("You have no updated clue details to export.");
						return;
					}

					final String exportDump = details.size() == 1
						? gson.toJson(details.get(0)) : sortJsonArrayById(gson, gson.toJson(details));

					log.debug("Exported clue details: {}", exportDump);

					Toolkit.getDefaultToolkit()
						.getSystemClipboard()
						.setContents(new StringSelection(exportDump), null);
					plugin.getPanel().updateStatusTemporarily(details.size() + " clue details were copied.", 5000);
					sendChatMessage(details.size() + " clue details were copied to your clipboard.");
				}
				catch (Exception exception)
				{
					log.error("Error exporting clue details", exception);
				}
			}
		};
		worker.execute();
	}

	public static String sortJsonArrayById(Gson gson, String jsonString)
	{
		try
		{
			JsonArray jsonArray = gson.fromJson(jsonString, JsonArray.class);
			List<JsonObject> jsonList = new ArrayList<>();

			// Convert JsonArray to a List of JsonObjects
			for (JsonElement element : jsonArray)
			{
				if (element.isJsonObject())
				{
					jsonList.add(element.getAsJsonObject());
				}
			}

			// Sort the list based on the "id" key
			jsonList.sort(Comparator.comparingInt(obj -> obj.get("id").getAsInt()));

			// Convert the sorted list back to a JsonArray
			JsonArray sortedJsonArray = new JsonArray();
			for (JsonObject jsonObject : jsonList)
			{
				sortedJsonArray.add(jsonObject);
			}

			return gson.toJson(sortedJsonArray);
		}
		catch (Exception e)
		{
			log.error("Error processing JSON export.", e);
			return null;
		}
	}

	private int showImportConfirmDialog(int amount)
	{
		String message = "Are you sure you want to import " + amount + " clue detail(s)?";

		return JOptionPane.showConfirmDialog(
				plugin.getPanel(),
				message,
				"Warning",
				JOptionPane.YES_NO_OPTION
		);
	}

	public void promptForImport(boolean filtered)
	{
		if (!SwingUtilities.isEventDispatchThread())
		{
			SwingUtilities.invokeLater(() -> promptForImport(filtered));
			return;
		}
		final String clipboardText;
		try
		{
			clipboardText = Toolkit.getDefaultToolkit()
				.getSystemClipboard()
				.getData(DataFlavor.stringFlavor)
				.toString();
		}
		catch (IOException | UnsupportedFlavorException | IllegalStateException ex)
		{
			reportImportError("Unable to read system clipboard.");
			log.warn("error reading clipboard", ex);
			return;
		}

		log.debug("Clipboard contents: {}", clipboardText);
		if (Strings.isNullOrEmpty(clipboardText))
		{
			reportImportError("You do not have any clue details copied in your clipboard.");
			return;
		}

		List<ClueIdToDetails> importClueDetails;
		try
		{
			importClueDetails = parseClueDetails(gson, clipboardText);
		}
		catch (JsonSyntaxException e)
		{
			log.debug("Malformed JSON for clipboard import", e);
			reportImportError("Your clue detail(s) are improperly formatted.");
			return;
		}
		catch (NumberFormatException e)
		{
			log.debug("Malformed JSON for clipboard import", e);
			reportImportError("Your clue detail(s) color is not properly formatted.");
			return;
		}

		if (importClueDetails.isEmpty())
		{
			reportImportError("You do not have any clue detail(s) copied in your clipboard.");
			return;
		}

		if (filtered)
		{
			Set<Integer> visibleClueIds = getFilteredClues().stream()
				.map(Clues::getClueID)
				.collect(Collectors.toSet());

			importClueDetails = importClueDetails.stream()
				.filter(detail -> visibleClueIds.contains(detail.getId()))
				.collect(Collectors.toList());

			if (importClueDetails.isEmpty())
			{
				reportImportError("You do not have any clue detail(s) copied to your clipboard that match your filtered clues.");
				return;
			}
		}

		if (showImportConfirmDialog(importClueDetails.size()) == JOptionPane.YES_OPTION)
		{
			importClueDetails(importClueDetails);
		}
	}

	static List<ClueIdToDetails> parseClueDetails(Gson gson, String json)
	{
		JsonElement element = new JsonParser().parse(json);
		List<ClueIdToDetails> details;
		if (element.isJsonArray())
		{
			details = gson.fromJson(element, new TypeToken<List<ClueIdToDetails>>(){}.getType());
		}
		else if (element.isJsonObject())
		{
			details = new ArrayList<>();
			details.add(gson.fromJson(element, ClueIdToDetails.class));
		}
		else
		{
			throw new JsonSyntaxException("Expected a clue detail object or array");
		}
		// Validate the whole input before filtering or making any configuration changes.
		if (details.stream().anyMatch(detail -> detail == null))
		{
			throw new JsonSyntaxException("Clue details cannot contain null entries");
		}
		return details;
	}

	private void reportImportError(String message)
	{
		if (plugin.getPanel() != null)
		{
			plugin.getPanel().updateStatusTemporarily(message, 5000);
		}
		sendChatMessage(message);
	}

	private void importClueDetails(Collection<ClueIdToDetails> importPoints)
	{
		SwingWorker<Integer, String> worker = new SwingWorker<>()
		{
			@Override
			protected Integer doInBackground() throws Exception
			{
				int counter = 0;
				for (ClueIdToDetails importPoint : importPoints)
				{
					publish("Importing clue details... (" + ++counter + "/" + importPoints.size() + ")");
					if (importPoint.text != null)
					{
						configManager.setConfiguration("clue-details-text", String.valueOf(importPoint.id), importPoint.text);
					}
					if (importPoint.color != null)
					{
						setClueColour(importPoint.color, importPoint.id);
					}
					if (importPoint.itemIds != null)
					{
						if (importPoint.itemIds.isEmpty())
						{
							configManager.unsetConfiguration(CLUE_ITEMS_CONFIG, String.valueOf(importPoint.id));
						}
						else
						{
							configManager.setConfiguration(CLUE_ITEMS_CONFIG, String.valueOf(importPoint.id), importPoint.itemIds);
						}
					}
					if (importPoint.widgetIds != null)
					{
						if (importPoint.widgetIds.isEmpty())
						{
							configManager.unsetConfiguration(CLUE_WIDGETS_CONFIG, String.valueOf(importPoint.id));
						}
						else
						{
							configManager.setConfiguration(CLUE_WIDGETS_CONFIG, String.valueOf(importPoint.id), gson.toJson(importPoint.widgetIds));
						}
					}
				}
				return importPoints.size();
			}

			@Override
			protected void process(List<String> chunks)
			{
				if (plugin.getPanel() != null && plugin.getPanel().isVisible())
				{
					plugin.getPanel().updateStatus(chunks.get(chunks.size() - 1));
				}
			}

			@Override
			protected void done()
			{
				try
				{
					plugin.getPanel().updateStatusTemporarily(get() + " clue details were imported.", 5000);
					sendChatMessage(get() + " clue details were imported from the clipboard.");
					plugin.getPanel().refresh();
				}
				catch (Exception error)
				{
					log.error("Error importing clue details", error);
				}
			}
		};
		worker.execute();
	}

	private void sendChatMessage(final String message)
	{
		plugin.getChatMessageManager().queue(QueuedMessage.builder()
			.type(ChatMessageType.CONSOLE)
			.runeLiteFormattedMessage(message)
			.build());
	}

	private List<Clues> getFilteredClues()
	{
		if (plugin.getPanel() != null)
		{
			return plugin.getPanel().getVisibleClues();
		}
		return Clues.CLUES.stream()
			.filter(config.filterListByTier())
			.filter(config.filterListByRegion())
			.collect(Collectors.toList());
	}

	public void setClueColour(Color colour, int clueId)
	{
		if (ClueIdToDetails.equalRGB(colour, Color.WHITE))
		{
			// Replace the following with
			// resetClueDetail(clueId, false, true, false, false);
			// after #237 is merged
			configManager.unsetConfiguration("clue-details-color", String.valueOf(clueId));

			if (clueId >= 2677)
			{
				if (config.colorGroundItems())
				{
					configManager.unsetConfiguration(GroundItemsConfig.GROUP, "highlight_" + clueId);
				}
				if (config.colorInventoryTags())
				{
					configManager.unsetConfiguration(InventoryTagsConfig.GROUP, "tag_" + clueId);
				}
			}
		}
		else
		{
			{
				configManager.setConfiguration("clue-details-color", String.valueOf(clueId), colour);

				// Apply color to Ground Items and Inventory Tags
				// Beginner & master clues are not supported by these plugins
				if (clueId >= 2677)
				{
					// Ensure ARGB format
					Color color = Color.decode(configManager.getConfiguration("clue-details-color", String.valueOf(clueId)));

					if (config.colorGroundItems())
					{
						configManager.setConfiguration(GroundItemsConfig.GROUP, "highlight_" + clueId, color);
					}
					if (config.colorInventoryTags())
					{
						configManager.setConfiguration(InventoryTagsConfig.GROUP, "tag_" + clueId,
								gson.toJson(Map.of("color", color)));
					}
				}
			}
		}
	}
}
