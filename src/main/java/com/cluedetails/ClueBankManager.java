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

import static com.cluedetails.ClueInventoryManager.TRACKED_CLUE_IDS;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import net.runelite.api.Client;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.client.config.ConfigManager;

public class ClueBankManager
{
	private final Client client;
	private ClueBankSaveDataManager clueBankSaveDataManager;
	private ClueInventoryManager clueInventoryManager;

	Item[] lastBankItems;

	private Map<Integer, ClueInstance> cluesInBank = new HashMap<>();

	private Map<Integer, ClueInstance> cluesGoneFromInventory = new HashMap<>();

	public ClueBankManager(Client client, ConfigManager configManager)
	{
		this.client = client;
		this.clueBankSaveDataManager = new ClueBankSaveDataManager(configManager);
	}

	public void startUp(ClueInventoryManager clueInventoryManager)
	{
		this.clueInventoryManager = clueInventoryManager;
	}

	public void handleBankChange(ItemContainer bankContainer)
	{
		if (lastBankItems != null) System.out.println(lastBankItems.length);
		System.out.println(bankContainer.size());
		if (lastBankItems == null)
		{
			lastBankItems = bankContainer.getItems();
			return;
		}

		for (Integer trackedClueId : TRACKED_CLUE_IDS)
		{
			if (bankContainer.contains(trackedClueId))
			{
				handleClueDeposited(trackedClueId);
			}
			else
			{
				handleClueTaken(trackedClueId);
			}
		}
		lastBankItems = bankContainer.getItems();
	}

	private void handleClueDeposited(int trackedClueId)
	{
		// Bank didn't contain it, and still doesn't. Return.
		if (Arrays.stream(lastBankItems).anyMatch((item) -> item.getId() == trackedClueId)) return;

		if (!cluesGoneFromInventory.containsKey(trackedClueId)) return;

		ClueInstance clue = cluesGoneFromInventory.get(trackedClueId);
		cluesGoneFromInventory.remove(trackedClueId);

		cluesInBank.put(trackedClueId, clue);
	}

	private void handleClueTaken(int trackedClueId)
	{
		// Bank did contain it, and still does. Return.
		if (Arrays.stream(lastBankItems).noneMatch((item) -> item.getId() == trackedClueId)) return;

		// Inventory updates before bank.
		ClueInstance clueFromBank = cluesInBank.get(trackedClueId);
		if (clueFromBank == null) return;

		ClueInstance clue = clueInventoryManager.getTrackedClueByClueId(trackedClueId);
		clue.setClueIds(clueFromBank.getClueIds());

		cluesInBank.remove(trackedClueId);
	}

	public void addToRemovedClues(ClueInstance clueInstance)
	{
		cluesGoneFromInventory.put(clueInstance.getItemId(), clueInstance);
	}

	public void saveStateToConfig()
	{
		clueBankSaveDataManager.saveStateToConfig(client, cluesInBank);
	}

	public void loadStateFromConfig()
	{
		cluesInBank.clear();
		cluesGoneFromInventory.clear();
		cluesInBank.putAll(clueBankSaveDataManager.loadStateFromConfig(client));
	}
}
