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

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.runelite.api.Client;
import net.runelite.client.config.ConfigManager;

public class ClueBankSaveDataManager
{
	private final ConfigManager configManager;

	private static final String CONFIG_GROUP = "clue-details";
	private static final String BANK_CLUES_KEY = "bank-clues";

	private final Gson gson = new Gson();
	private final List<ClueInstanceData> clueInstanceData = new ArrayList<>();

	public ClueBankSaveDataManager(ConfigManager configManager)
	{
		this.configManager = configManager;
	}

	public void saveStateToConfig(Client client, Map<Integer, ClueInstance> bankClues)
	{
		// Serialize groundClues save to config
		updateData(client, bankClues);
		String bankCluesData = gson.toJson(clueInstanceData);
		configManager.setConfiguration(CONFIG_GROUP, BANK_CLUES_KEY, bankCluesData);
	}

	private void updateData(Client client, Map<Integer, ClueInstance> bankClues)
	{
		int currentTick = client.getTickCount();

		List<ClueInstanceData> newData = new ArrayList<>();
		for (Map.Entry<Integer, ClueInstance> entry : bankClues.entrySet())
		{
			ClueInstance data = entry.getValue();
			newData.add(new ClueInstanceData(data, currentTick));
		}
		clueInstanceData.clear();
		clueInstanceData.addAll(newData);
	}

	public Map<Integer, ClueInstance> loadStateFromConfig(Client client)
	{
		String groundCluesJson = configManager.getConfiguration(CONFIG_GROUP, BANK_CLUES_KEY);
		clueInstanceData.clear();

		Map<Integer, ClueInstance> bankClues = new HashMap<>();
		if (groundCluesJson != null)
		{
			try
			{
				Type groundCluesType = new TypeToken<List<ClueInstanceData>>()
				{
				}.getType();

				List<ClueInstanceData> loadedGroundCluesData = gson.fromJson(groundCluesJson, groundCluesType);

				// Convert ClueInstanceData back to ClueInstance
				for (ClueInstanceData clueData : loadedGroundCluesData)
				{
					clueInstanceData.add(clueData);

					Integer itemId = clueData.getItemId();
					ClueInstance clue = new ClueInstance(clueData);

					bankClues.put(itemId, clue);
				}
			} catch (Exception err)
			{
				bankClues.clear();
				saveStateToConfig(client, bankClues);
			}
		}

		return bankClues;
	}
}
