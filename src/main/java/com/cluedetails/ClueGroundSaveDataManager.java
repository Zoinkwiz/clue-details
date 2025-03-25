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
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.config.ConfigManager;

public class ClueGroundSaveDataManager
{
	private final ConfigManager configManager;
	private final Gson gson;
	private static final String CONFIG_GROUP = "clue-details";
	private static final String GROUND_CLUES_KEY = "ground-clues";
	private final List<ClueInstanceData> clueInstanceData = new ArrayList<>();

	public ClueGroundSaveDataManager(ConfigManager configManager, Gson gson)
	{
		this.configManager = configManager;
		this.gson = gson;
	}

	public void saveStateToConfig(Client client, List<ClueInstance> groundClues)
	{
		// Serialize groundClues save to config
		updateData(client, groundClues);
		String groundCluesJson = gson.toJson(clueInstanceData);
		configManager.setConfiguration(CONFIG_GROUP, GROUND_CLUES_KEY, groundCluesJson);
	}

	private void updateData(Client client, List<ClueInstance> groundClues)
	{
		int currentTick = client.getTickCount();

		List<ClueInstanceData> newData = new ArrayList<>();
		for (ClueInstance groundClue : groundClues)
		{
			newData.add(new ClueInstanceData(groundClue, currentTick));
		}
		clueInstanceData.clear();
		clueInstanceData.addAll(newData);
	}

	public Map<WorldPoint, List<ClueInstance>> loadStateFromConfig(Client client)
	{
		String groundCluesJson = configManager.getConfiguration(CONFIG_GROUP, GROUND_CLUES_KEY);
		clueInstanceData.clear();

		Map<WorldPoint, List<ClueInstance>> groundClues = new HashMap<>();
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

					WorldPoint location = clueData.getLocation();
					ClueInstance clue = new ClueInstance(clueData);
					if (groundClues.containsKey(location))
					{
						groundClues.get(location).add(clue);
						continue;
					}

					List<ClueInstance> clueInstances = new ArrayList<>();
					clueInstances.add(clue);

					groundClues.put(location, clueInstances);
				}
			} catch (Exception err)
			{
				groundClues.clear();
				saveStateToConfig(client, new ArrayList<>());
			}
		}

		return groundClues;
	}
}
