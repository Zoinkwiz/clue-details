/*
 * Copyright (c) 2026, TheLope <https://github.com/TheLope>
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
package com.cluedetails.bank.banktab;

import com.cluedetails.ClueBankManager;
import com.cluedetails.ClueDetailsConfig;
import com.cluedetails.ClueDetailsPlugin;
import com.cluedetails.ClueInstance;
import com.cluedetails.ClueInventoryManager;
import com.cluedetails.Clues;
import com.cluedetails.filters.ClueTier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import net.runelite.api.Client;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.gameval.InventoryID;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.game.ItemManager;
import org.apache.commons.text.WordUtils;

/**
 * Builds the list of {@link BankTabItems} to show in the clue bank tab, one per clue
 * currently in the player's inventory (and optionally bank), based on the items configured
 * against each clue.
 */
@Singleton
public class ClueBankTagService
{
	@Inject
	private ClueDetailsPlugin plugin;

	@Inject
	private ClueDetailsConfig config;

	@Inject
	private Client client;

	@Inject
	private ClueInventoryManager clueInventoryManager;

	@Inject
	private ClueBankManager clueBankManager;

	@Inject
	private ConfigManager configManager;

	@Inject
	private ItemManager itemManager;

	@Inject
	private PotionStorage potionStorage;

	public List<BankTabItems> getBankTabSections()
	{
		List<BankTabItems> sections = new ArrayList<>();

		for (Integer clueItemId : clueInventoryManager.getCluesInInventory())
		{
			addSectionForClueInstance(sections, clueInventoryManager.getClueByClueItemId(clueItemId), false);
		}

		if (config.includeBankCluesInBankTab())
		{
			for (Integer clueItemId : getClueItemIdsInBank())
			{
				// A bank clue still needs withdrawing, so include the scroll itself in its section.
				addSectionForClueInstance(sections, resolveClueForBankItem(clueItemId), true);
			}
		}

		return sections;
	}

	private Set<Integer> getClueItemIdsInBank()
	{
		ItemContainer bank = client.getItemContainer(InventoryID.BANK);
		if (bank == null) return Collections.emptySet();

		Set<Integer> clueItemIds = new LinkedHashSet<>();
		for (Item item : bank.getItems())
		{
			int itemId = item.getId();
			if (Clues.isClue(itemId, plugin.isDeveloperMode()) || Clues.isTrackedClueOrTornClue(itemId, plugin.isDeveloperMode()))
			{
				clueItemIds.add(itemId);
			}
		}
		return clueItemIds;
	}

	// Beginner/Master/Elite Challenge clues share one item id per tier, so their specific text can
	// only come from ClueBankManager's tracked state. Fall back to a bare instance either way, so
	// an unresolved clue still gets a generic tier section instead of vanishing from the filter.
	private ClueInstance resolveClueForBankItem(int itemId)
	{
		if (Clues.isTrackedClueOrTornClue(itemId, plugin.isDeveloperMode()))
		{
			ClueInstance trackedInstance = clueBankManager.getClueByClueItemId(itemId);
			if (trackedInstance != null)
			{
				return trackedInstance;
			}
		}
		return new ClueInstance(new ArrayList<>(), itemId);
	}

	private void addSectionForClueInstance(List<BankTabItems> sections, ClueInstance instance, boolean includeClueItself)
	{
		if (instance == null || !instance.isEnabled(config)) return;

		LinkedHashSet<Integer> itemIds = new LinkedHashSet<>();
		for (Integer clueId : instance.getClueIds())
		{
			Clues clue = Clues.forClueIdFiltered(clueId);
			if (clue == null) continue;

			List<Integer> clueItems = clue.getItems(plugin, configManager);
			if (clueItems != null)
			{
				for (Integer itemId : clueItems)
				{
					itemIds.add(resolveDisplayItemId(itemId));
				}
			}
		}

		if (includeClueItself)
		{
			itemIds.add(instance.getItemId());
		}

		if (itemIds.isEmpty()) return;

		BankTabItems section = new BankTabItems(getSectionName(instance));
		for (Integer itemId : itemIds)
		{
			section.addItems(makeBankTabItem(itemId, section.getName()));
		}
		sections.add(section);
	}

	// Substitute the actual stored dose if the configured one is only in potion storage.
	private int resolveDisplayItemId(int itemId)
	{
		ItemContainer bank = client.getItemContainer(InventoryID.BANK);
		if (bank != null && bank.count(itemId) > 0)
		{
			return itemId;
		}
		return potionStorage.resolveActualItemId(itemId);
	}

	private String getSectionName(ClueInstance instance)
	{
		List<Integer> clueIds = instance.getClueIds();

		if (clueIds.size() == 1)
		{
			Clues clue = Clues.forClueIdFiltered(clueIds.get(0));
			if (clue != null)
			{
				return clue.getDetail(configManager);
			}
		}

		ClueTier tier = instance.getTier();
		String tierName = tier == null ? "Clue" : WordUtils.capitalizeFully(tier.toString().replace("_", " "));

		return clueIds.size() > 1 ? tierName + " clue (" + clueIds.size() + " steps)" : tierName + " clue";
	}

	private BankTabItem makeBankTabItem(int itemId, String sectionName)
	{
		String name = itemManager.getItemComposition(itemId).getName();
		return new BankTabItem(itemId, name, sectionName);
	}
}
