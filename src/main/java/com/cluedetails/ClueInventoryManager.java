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

import com.cluedetails.panels.ClueDetailsParentPanel;

import java.util.*;
import javax.inject.Singleton;
import net.runelite.api.Client;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.ItemID;
import net.runelite.api.KeyCode;
import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.widgets.InterfaceID;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetUtil;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.game.chatbox.ChatboxPanelManager;

@Singleton
public class ClueInventoryManager
{
    private final Client client;
	private final ConfigManager configManager;
	private final ClueGroundManager clueGroundManager;
	private final ChatboxPanelManager chatboxPanelManager;
    private final Map<Integer, ClueInstance> trackedCluesInInventory = new HashMap<>();
    private final Map<Integer, ClueInstance> previousTrackedCluesInInventory = new HashMap<>();

    private static final Collection<Integer> TRACKED_CLUE_IDS = Arrays.asList(
        ItemID.CLUE_SCROLL_MASTER,
		ItemID.CLUE_SCROLL_BEGINNER,
		ItemID.TORN_CLUE_SCROLL_PART_1,
		ItemID.TORN_CLUE_SCROLL_PART_2,
		ItemID.TORN_CLUE_SCROLL_PART_3
    );

    public ClueInventoryManager(Client client, ConfigManager configManager, ClueGroundManager clueGroundManager, ChatboxPanelManager chatboxPanelManager)
    {
        this.client = client;
		this.configManager = configManager;
		this.clueGroundManager = clueGroundManager;
		this.chatboxPanelManager = chatboxPanelManager;
    }

    public void updateInventory(ItemContainer inventoryContainer)
    {
        // Copy current tracked clues to previous
        previousTrackedCluesInInventory.clear();
        previousTrackedCluesInInventory.putAll(trackedCluesInInventory);

        // Clear current tracked clues
        trackedCluesInInventory.clear();

        Item[] inventoryItems = inventoryContainer.getItems();

        for (Item item : inventoryItems)
		{
			if (item == null || !TRACKED_CLUE_IDS.contains(item.getId())) continue;
			int itemId = item.getId();

			// If clue is already in previous, keep the same ClueInstance
			ClueInstance clueInstance = previousTrackedCluesInInventory.get(itemId);
			if (clueInstance != null)
			{
				trackedCluesInInventory.put(itemId, clueInstance);
				continue;
			}

			// Wasn't in inventory. Now see if it was an item we picked up we know about
			for (ClueInstance clueFromFloor : clueGroundManager.getDespawnedClueQueue())
			{
				if (clueFromFloor.getItemId() == item.getId())
				{
					clueInstance = new ClueInstance(clueFromFloor.getClueIds(), itemId);
				}
			}
			if (clueInstance != null)
			{
				trackedCluesInInventory.put(itemId, clueInstance);
				continue;
			}

			clueInstance = new ClueInstance(new ArrayList<>(), itemId);
			trackedCluesInInventory.put(itemId, clueInstance);
		}

		clueGroundManager.getDespawnedClueQueue().clear();

        // Compare previous and current to find removed clues
        for (Integer itemId : previousTrackedCluesInInventory.keySet())
        {
            if (!trackedCluesInInventory.containsKey(itemId))
            {
                // Clue was removed from inventory (possibly dropped)
                ClueInstance removedClue = previousTrackedCluesInInventory.get(itemId);
                if (removedClue != null)
                {
					clueGroundManager.processPendingGroundCluesFromInventoryChanged(removedClue);
                }
            }
        }
    }

    public void updateClueText(String clueText)
    {
		List<Integer> clueIds = new ArrayList<>();

		ThreeStepCrypticClue threeStepCrypticClue = ThreeStepCrypticClue.forText(clueText);
		if (threeStepCrypticClue != null)
		{
			for (Map.Entry<ClueText, Boolean> clueStep : threeStepCrypticClue.getClueSteps())
			{
				clueIds.add(clueStep.getKey().getFakeId());
			}
		}
		else
		{
			clueIds.add(ClueText.forTextGetId(clueText));
		}

		Set<Integer> itemIDs = trackedCluesInInventory.keySet();
		for (Integer itemID : itemIDs)
		{
			ClueInstance clueInstance = trackedCluesInInventory.get(itemID);
			ClueText clueInfo = ClueText.getById(clueIds.get(0));
			if (clueInfo == null) continue;
			if (!Objects.equals(clueInfo.getClueTier(), itemID)) continue;
			clueInstance.setClueIds(clueIds);
			break;
		}
    }

	public ClueInstance getTrackedClueByClueId(Integer clueItemID)
	{
		return trackedCluesInInventory.get(clueItemID);
	}

    public boolean hasTrackedClues()
    {
        return !trackedCluesInInventory.isEmpty();
    }

    public void onMenuEntryAdded(MenuEntryAdded event, CluePreferenceManager cluePreferenceManager, ClueDetailsParentPanel panel)
	{
		if (!client.isKeyPressed(KeyCode.KC_SHIFT))
		{
			return;
		}

		if (!event.getTarget().contains("Clue scroll")) return;
		if (!isExamineClue(event.getMenuEntry())) return;

		MenuEntry entry = event.getMenuEntry();
		final Widget w = entry.getWidget();
		boolean isInventoryMenu = w != null && WidgetUtil.componentToInterface(w.getId()) == InterfaceID.INVENTORY;
		int itemId = isInventoryMenu ? event.getItemId() : event.getIdentifier();
		boolean isMarked = cluePreferenceManager.getPreference(itemId);

		client.getMenu().createMenuEntry(-1)
			.setOption(isMarked ? "Unmark" : "Mark")
			.setTarget(event.getTarget())
			.setIdentifier(itemId)
			.setType(MenuAction.RUNELITE)
			.onClick(e ->
			{
				boolean currentValue = cluePreferenceManager.getPreference(e.getIdentifier());
				cluePreferenceManager.savePreference(e.getIdentifier(), !currentValue);
				panel.refresh();
			});

		if (!isInventoryMenu) return;

		client.getMenu().createMenuEntry(-1)
			.setOption("Clue details")
			.setTarget(entry.getTarget())
			.setType(MenuAction.RUNELITE)
			.onClick(e ->
			{
				Clues clue = Clues.get(itemId);
				chatboxPanelManager.openTextInput("Enter new clue text:")
					.value(clue.getDisplayText(configManager))
					.onDone((newTag) -> {
						configManager.setConfiguration("clue-details-text", String.valueOf(clue.getClueID()), newTag);
						panel.refresh();
					})
					.build();
			});
	}

	public boolean isExamineClue(MenuEntry entry)
	{
		String[] textOptions = new String[] { "Clue scroll", "Challenge scroll", "Key (" };
		String target = entry.getTarget();
		String option = entry.getOption();
		return Arrays.stream(textOptions).anyMatch(target::contains) && option.equals("Examine");
	}
}
