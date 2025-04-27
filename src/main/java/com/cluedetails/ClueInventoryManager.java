/*
 * Copyright (c) 2024, Zoinkwiz <https://www.github.com/Zoinkwiz>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *	  list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *	  this list of conditions and the following disclaimer in the documentation
 *	  and/or other materials provided with the distribution.
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

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.ItemID;
import net.runelite.api.KeyCode;
import net.runelite.api.Menu;
import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;
import net.runelite.api.NpcID;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.widgets.ComponentID;
import net.runelite.api.widgets.InterfaceID;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetUtil;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.game.chatbox.ChatboxPanelManager;
import net.runelite.client.util.Text;

@Slf4j
@Singleton
public class ClueInventoryManager
{
	private final Client client;
	private final ConfigManager configManager;
	private final ClueDetailsPlugin clueDetailsPlugin;
	private final ClueGroundManager clueGroundManager;
	private final ClueBankManager clueBankManager;
	private final ChatboxPanelManager chatboxPanelManager;
	private final Map<Integer, ClueInstance> cluesInInventory = new HashMap<>();
	private final Map<Integer, ClueInstance> previousCluesInInventory = new HashMap<>();

	@Getter
	private long lastInventoryUpdate = 0;

	// To be initialized to avoid passing around
	@Setter
	public static ClueDetailsConfig config;

	public ClueInventoryManager(Client client, ConfigManager configManager, ClueDetailsPlugin clueDetailsPlugin, ClueGroundManager clueGroundManager,
								ClueBankManager clueBankManager, ChatboxPanelManager chatboxPanelManager)
	{
		this.client = client;
		this.configManager = configManager;
		this.clueDetailsPlugin = clueDetailsPlugin;
		this.clueGroundManager = clueGroundManager;
		this.clueBankManager = clueBankManager;
		this.chatboxPanelManager = chatboxPanelManager;
	}

	public void updateLastInventoryRefreshTime()
	{
		this.lastInventoryUpdate = System.currentTimeMillis();
	}

	public void updateInventory(ItemContainer inventoryContainer)
	{
		// Copy current clues to previous
		previousCluesInInventory.clear();
		previousCluesInInventory.putAll(cluesInInventory);

		// Clear current clues
		cluesInInventory.clear();

		Item[] inventoryItems = inventoryContainer.getItems();

		for (Item item : inventoryItems)
		{
			if (item == null) continue;

			checkItemAsClueInstance(item.getId());
		}

		clueGroundManager.getDespawnedClueQueueForInventoryCheck().clear();

		// Compare previous and current to find removed clues
		for (Integer itemId : previousCluesInInventory.keySet())
		{
			if (!cluesInInventory.containsKey(itemId) || cluesInInventory.get(itemId) != previousCluesInInventory.get(itemId))
			{
				// Clue was removed from inventory (possibly dropped)
				ClueInstance removedClue = previousCluesInInventory.get(itemId);
				if (removedClue != null)
				{
					clueBankManager.addToRemovedClues(removedClue);
				}
			}
		}

		clueDetailsPlugin.getClueInventoryManager().updateLastInventoryRefreshTime();
	}

	private void checkItemAsClueInstance(int itemId)
	{
		ClueInstance clueInstance;

		if (!Clues.isClue(itemId, clueDetailsPlugin.isDeveloperMode())
			&& !Clues.isTrackedClueOrTornClue(itemId, clueDetailsPlugin.isDeveloperMode())) return;

		// If we have a clue we've picked up this tick, we've probably dropped and picked up a clue same tick
		Optional<ClueInstance> clueFromFloorInInv = clueGroundManager.getDespawnedClueQueueForInventoryCheck().stream()
			.filter(clueFromFloor ->  clueFromFloor.getItemId() == itemId)
			.findFirst();
		if (clueFromFloorInInv.isPresent())
		{
			cluesInInventory.put(itemId, new ClueInstance(clueFromFloorInInv.get().getClueIds(), itemId));
			return;
		}

		// If clue is already in previous, keep the same ClueInstance
		// This check is after the floor check as you could drop and pick up a clue in the same tick
		clueInstance = previousCluesInInventory.get(itemId);
		if (clueInstance != null && Clues.isClue(clueInstance.getItemId(), clueDetailsPlugin.isDeveloperMode()))
		{
			cluesInInventory.put(itemId, clueInstance);
		}
		else
		{
			clueInstance = new ClueInstance(new ArrayList<>(), itemId);
			cluesInInventory.put(itemId, clueInstance);
		}
	}

	public void updateClueText(String clueText)
	{
		List<Integer> clueIds = new ArrayList<>();

		// Allow for fake items to have info attached to them in dev mode
		if (clueDetailsPlugin.isDeveloperMode())
		{
			for (Integer devModeId : Clues.DEV_MODE_IDS)
			{
				int randomTestId = (int) (Math.random() * 20);
				cluesInInventory.put(devModeId, new ClueInstance(List.of(randomTestId), devModeId));
			}
		}

		ThreeStepCrypticClue threeStepCrypticClue = ThreeStepCrypticClue.forText(clueText);
		if (threeStepCrypticClue != null)
		{
			for (Map.Entry<Clues, Boolean> clueStep : threeStepCrypticClue.getClueSteps())
			{
				clueIds.add(clueStep.getKey().getClueID());
			}
		}
		else
		{
			clueIds.add(Clues.forTextGetId(clueText));
		}

		if (clueIds.get(0) == null) return;

		Set<Integer> itemIDs = cluesInInventory.keySet();
		for (Integer itemID : itemIDs)
		{
			ClueInstance clueInstance = cluesInInventory.get(itemID);
			// Check that at least one part of the clue text matches the clue tier we're looking at
			if (clueInstance == null) continue;
			Clues clueInfo = Clues.forClueIdFiltered(clueIds.get(0));
			if (clueInfo == null) continue;
			if (!Objects.equals(clueInfo.getItemID(), itemID)) continue;
			clueInstance.setClueIds(clueIds);
			clueDetailsPlugin.getClueInventoryManager().updateLastInventoryRefreshTime();
			break;
		}
	}

	// Only used for Beginner Map Clues
	public void updateClueText(Integer interfaceId)
	{
		List<Integer> clueIds = new ArrayList<>();

		// Beginner Map Clues all use the same ItemID, but the InterfaceID used to display them is unique
		clueIds.add(Clues.forInterfaceIdGetId(interfaceId));

		// Assume can only be beginner for now
		ClueInstance beginnerClueInInv = cluesInInventory.get(ItemID.CLUE_SCROLL_BEGINNER);
		if (beginnerClueInInv == null) return;
		beginnerClueInInv.setClueIds(clueIds);
		clueDetailsPlugin.getClueInventoryManager().updateLastInventoryRefreshTime();
	}

	public Set<Integer> getCluesInInventory()
	{
		return cluesInInventory.keySet();
	}

	public ClueInstance getClueByClueItemId(Integer clueItemID)
	{
		return cluesInInventory.get(clueItemID);
	}

	public void onMenuEntryAdded(MenuEntryAdded event, CluePreferenceManager cluePreferenceManager, ClueDetailsParentPanel panel)
	{
		MenuEntry entry = event.getMenuEntry();

		// Ensure clue ground items are not deprioritized
		if (hasClueName(event.getMenuEntry().getTarget()) && config.showGroundClues())
		{
			MenuAction type = MenuAction.of(event.getType());
			if (type == MenuAction.GROUND_ITEM_FIRST_OPTION || type == MenuAction.GROUND_ITEM_SECOND_OPTION ||
				type == MenuAction.GROUND_ITEM_THIRD_OPTION || type == MenuAction.GROUND_ITEM_FOURTH_OPTION ||
				type == MenuAction.GROUND_ITEM_FIFTH_OPTION || type == MenuAction.WIDGET_TARGET_ON_GROUND_ITEM)
			{
				MenuEntry[] menuEntries = client.getMenu().getMenuEntries();
				MenuEntry lastEntry = menuEntries[menuEntries.length - 1];

				if (isEnabled(lastEntry.getItemId()))
				{
					lastEntry.setDeprioritized(false);
				}
			}
		}

		if (!client.isKeyPressed(KeyCode.KC_SHIFT))
		{
			return;
		}

		// We only run on examine as this should be on all items only once
		if (!"Examine".equals(event.getMenuEntry().getOption())) return;

		final Widget w = entry.getWidget();
		boolean isInventoryMenu = w != null && WidgetUtil.componentToInterface(w.getId()) == InterfaceID.INVENTORY;

		int itemId = isInventoryMenu ? event.getItemId() : event.getIdentifier();
		// Runs on both inventory and ground clues
		if (hasClueName(event.getMenuEntry().getTarget()))
		{
			handleMarkClue(cluePreferenceManager, panel, event.getTarget(), itemId);
		}

		if (isInventoryMenu)
		{
			handleInventory(cluePreferenceManager, event, panel);
		}
	}

	private void handleInventory(CluePreferenceManager cluePreferenceManager, MenuEntryAdded event, ClueDetailsParentPanel panel)
	{
		MenuEntry menuEntry = event.getMenuEntry();
		int itemId = event.getItemId();

		// Add item highlight menu
		if (!hasClueName(menuEntry.getTarget()))
		{
			if (cluesInInventory.isEmpty()) return;

			MenuEntry clueDetailsEntry = client.getMenu().createMenuEntry(-1)
				.setOption("Clue details")
				.setTarget(menuEntry.getTarget())
				.setType(MenuAction.RUNELITE);
			Menu submenu = clueDetailsEntry.createSubMenu();
			cluesInInventory.forEach((id, instance) -> instance.getClueIds().forEach((clueId) -> addHighlightItemMenu(cluePreferenceManager, submenu, Clues.forClueIdFiltered(clueId), itemId, event)));
			return;
		}

		// Is a clue item, add clue item menu entries
		handleClueDetailsMenuEntry(panel, menuEntry, itemId);
	}

	private void handleMarkClue(CluePreferenceManager cluePreferenceManager, ClueDetailsParentPanel panel, String name, int itemId)
	{
		boolean isMarked = cluePreferenceManager.getHighlightPreference(itemId);

		// Mark Option
		if (!Clues.isBeginnerOrMasterClue(itemId, clueDetailsPlugin.isDeveloperMode()))
		{
			toggleMarkClue(cluePreferenceManager, panel, itemId, isMarked, name);
		}
	}

	private void handleClueDetailsMenuEntry(ClueDetailsParentPanel panel, MenuEntry entry, int itemId)
	{
		List<Integer> clueIds = new ArrayList<>();
		Menu menu;
		String option = null;
		String target = null;

		if (Clues.isBeginnerOrMasterClue(itemId, clueDetailsPlugin.isDeveloperMode()))
		{
			ClueInstance clueSelected = cluesInInventory.get(itemId);
			if (clueSelected == null || clueSelected.getClueIds().isEmpty()) return;

			clueIds.addAll(clueSelected.getClueIds());

			// Only create submenu when needed
			if (clueIds.size() > 1)
			{
				MenuEntry parent = client.getMenu().createMenuEntry(-1)
					.setOption("Clue details")
					.setTarget(entry.getTarget())
					.setType(MenuAction.RUNELITE);

				menu = parent.createSubMenu();
			}
			else
			{
				menu = client.getMenu();
				target = entry.getTarget();
				option = "Clue details";
			}
		}
		else
		{
			menu = client.getMenu();
			clueIds.add(itemId);
			target = entry.getTarget();
			option = "Clue details";
		}

		for (int id : clueIds)
		{
			Clues clue = Clues.forClueIdFiltered(id);
			if (clue == null)
			{
				log.debug("Failed to find clue " + id);
				return;
			}
			String newOption = option == null ? clue.getDetail(configManager) : option;
			String newTarget = target == null ? "" : target;
			addClueDetailsMenuEntry(panel, menu, newOption, newTarget, clue);
		}
	}

	private void addClueDetailsMenuEntry(ClueDetailsParentPanel panel, Menu menu, String option, String target, Clues clue)
	{
		menu.createMenuEntry(-1)
			.setOption(option)
			.setTarget(target)
			.setType(MenuAction.RUNELITE)
			.onClick(e ->
				chatboxPanelManager.openTextInput("Enter new clue detail:")
					.value(clue.getDetail(configManager))
					.onDone((newDetail) ->
					{
						configManager.setConfiguration("clue-details-text", String.valueOf(clue.getClueID()), newDetail);
						panel.refresh();
					})
					.build());
	}

	private void addHighlightItemMenu(CluePreferenceManager cluePreferenceManager, Menu menu, Clues clue, int itemId, MenuEntryAdded event)
	{
		if (clue == null) return;

		boolean itemInCluePreference = cluePreferenceManager.itemsPreferenceContainsItem(clue.getClueID(), itemId);

		String action = itemInCluePreference ? "Remove from " : "Add to ";
		String clueDetail = clue.getDetail(configManager);
		final String text = action + "'" + clueDetail + "'";

		// Add menu to item for clue
		menu.createMenuEntry(-1)
			.setOption(text)
			.setTarget(event.getTarget())
			.setIdentifier(itemId)
			.setType(MenuAction.RUNELITE)
			.onClick(e ->
				updateClueItems(clue, itemId, cluePreferenceManager));
	}

	private void toggleMarkClue(CluePreferenceManager cluePreferenceManager, ClueDetailsParentPanel panel, int clueId, boolean isMarked, String target)
	{
		// We don't want to have marking on masters I think
		client.getMenu().createMenuEntry(-1)
			.setOption(isMarked ? "Unmark" : "Mark")
			.setTarget(target)
			.setIdentifier(clueId)
			.setType(MenuAction.RUNELITE)
			.onClick(e ->
			{
				boolean currentValue = cluePreferenceManager.getHighlightPreference(e.getIdentifier());
				cluePreferenceManager.saveHighlightPreference(e.getIdentifier(), !currentValue);
				panel.refresh();
			});
	}

	private void updateClueItems(Clues clue, int itemId, CluePreferenceManager cluePreferenceManager)
	{
		// Get existing Clue itemIds
		int clueId = clue.getClueID();
		List<Integer> clueItemIds = cluePreferenceManager.getItemsPreference(clueId);

		if (clueItemIds == null)
		{
			clueItemIds = new ArrayList<>();
		}

		// Remove if already present
		if (clueItemIds.contains(itemId))
		{
			clueItemIds.remove(Integer.valueOf(itemId));
		}
		// Add if not present
		else
		{
			clueItemIds.add(itemId);
		}

		// Save Clue itemIds
		cluePreferenceManager.saveItemsPreference(clueId, clueItemIds);
	}

	private boolean hasClueName(String name)
	{
		return name.contains("Clue scroll")
			|| name.contains("Challenge scroll")
			|| name.contains("Key (medium)")
			|| (clueDetailsPlugin.isDeveloperMode() && name.contains("Daeyalt essence"));
	}

	public void onGameTick()
	{
		// Reset clue when receiving a new beginner or master clue
		// These clues use a single item ID, so we cannot detect step changes based on the item ID changing
		final Widget headModelWidget = client.getWidget(ComponentID.DIALOG_NPC_HEAD_MODEL);
		final Widget chatDialogClueItemWidget = client.getWidget(ComponentID.DIALOG_SPRITE_SPRITE);
		final Widget npcChatWidget = client.getWidget(ComponentID.DIALOG_NPC_TEXT);

		if (isNewBeginnerClue(chatDialogClueItemWidget)
			|| (isUriBeginnerClue(headModelWidget) && isUriStandardDialogue(npcChatWidget)))
		{
			resetClue(ItemID.CLUE_SCROLL_BEGINNER, List.of());
		}
		else if (isNewMasterClue(chatDialogClueItemWidget)
			|| (isUriMasterClue(headModelWidget) && isUriStandardDialogue(npcChatWidget)))
		{
			resetClue(ItemID.CLUE_SCROLL_MASTER, List.of());
		}
		else if (isCharlieBeginnerClue(headModelWidget) && isCharlieTaskDialogue(npcChatWidget))
		{
			Integer clueID = getClueIdFromCharlie(npcChatWidget);
			if (clueID == null) return;
			resetClue(ItemID.CLUE_SCROLL_BEGINNER, List.of(clueID));
		}
		else if (isFaloMasterClue(headModelWidget) && isFaloLyricDialogue(npcChatWidget))
		{
			Integer clueID = getClueIdFromFalo(npcChatWidget);
			if (clueID == null) return;
			resetClue(ItemID.CLUE_SCROLL_MASTER, List.of(clueID));
		}
	}

	private void resetClue(int itemID, List<Integer> clueIds)
	{
		ClueInstance clue = cluesInInventory.get(itemID);
		if (clue == null) return;
		clue.setClueIds(clueIds);
		clueDetailsPlugin.getItemsOverlay().invalidateCache();
		clueDetailsPlugin.getClueInventoryManager().updateLastInventoryRefreshTime();
	}

	private boolean isUriMasterClue(Widget headModel)
	{
		if (headModel == null) return false;
		return headModel.getModelId() == NpcID.URI_7311;
	}

	private boolean isUriBeginnerClue(Widget headModel)
	{
		if (headModel == null) return false;
		return headModel.getModelId() == NpcID.URI_8638;
	}

	private boolean isUriStandardDialogue(Widget npcChat)
	{
		if (npcChat == null) return false;
		// Check if speaking with another player's Uri or with incorrect attire
		return !npcChat.getText().contains("I do not believe we have any business, Comrade.");
	}

	private boolean isNewBeginnerClue(Widget chatDialogClueItem)
	{
		if (chatDialogClueItem == null) return false;
		return chatDialogClueItem.getItemId() == ItemID.CLUE_SCROLL_BEGINNER;
	}

	private boolean isNewMasterClue(Widget chatDialogClueItem)
	{
		if (chatDialogClueItem == null) return false;
		return chatDialogClueItem.getItemId() == ItemID.CLUE_SCROLL_MASTER;
	}

	private boolean isCharlieBeginnerClue(Widget headModel)
	{
		if (headModel == null) return false;
		return headModel.getModelId() == NpcID.CHARLIE_THE_TRAMP;
	}

	private boolean isCharlieTaskDialogue(Widget npcChat)
	{
		if (npcChat == null) return false;
		return npcChat.getText().toLowerCase().contains("i really need");
	}

	private static Integer getClueIdFromCharlie(Widget npcChatWidget)
	{
		if (npcChatWidget.getText() == null) return null;
		String clueText = BeginnerMasterClueText.forTaskGetClueText(npcChatWidget.getText());
		if (clueText == null) return null;
		return Clues.forTextGetId(clueText);
	}

	private boolean isFaloMasterClue(Widget headModel)
	{
		if (headModel == null) return false;
		return headModel.getModelId() == NpcID.FALO_THE_BARD;
	}

	private boolean isFaloLyricDialogue(Widget npcChat)
	{
		if (npcChat == null) return false;
		return npcChat.getText().toLowerCase().contains("here goes...");
	}

	private static Integer getClueIdFromFalo(Widget npcChatWidget)
	{
		if (npcChatWidget.getText() == null) return null;
		String clueText = BeginnerMasterClueText.forLyricsGetClueText(npcChatWidget.getText());
		if (clueText == null) return null;
		return Clues.forTextGetId(clueText);
	}

	private boolean isEnabled(Integer itemId)
	{
		if (itemId == ItemID.CLUE_SCROLL_BEGINNER)
		{
			return config.beginnerDetails();
		}
		else if (itemId == ItemID.CLUE_SCROLL_MASTER )
		{
			return config.masterDetails();
		}
		return true;
	}
}
