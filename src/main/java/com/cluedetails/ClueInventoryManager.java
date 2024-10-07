package com.cluedetails;

import com.cluedetails.panels.ClueDetailsParentPanel;
import net.runelite.api.*;

import java.awt.event.KeyEvent;
import java.util.*;
import java.util.Deque;
import javax.inject.Singleton;
import net.runelite.api.Client;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.ItemID;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.client.input.KeyListener;

@Singleton
public class ClueInventoryManager implements KeyListener
{
    private final Client client;
	private final ClueGroundManager clueGroundManager;
    private final Map<Integer, ClueInstance> trackedCluesInInventory = new HashMap<>();
    private final Map<Integer, ClueInstance> previousTrackedCluesInInventory = new HashMap<>();
    private final Deque<ClueInstance> recentlyDroppedClues = new ArrayDeque<>();

    private boolean shiftKeyDown;

    private static final Collection<Integer> TRACKED_CLUE_IDS = Arrays.asList(
            ItemID.CLUE_SCROLL_MASTER,
            ItemID.CLUE_SCROLL_BEGINNER,
            ItemID.TORN_CLUE_SCROLL_PART_1,
            ItemID.TORN_CLUE_SCROLL_PART_2,
            ItemID.TORN_CLUE_SCROLL_PART_3
    );

    public ClueInventoryManager(Client client, ClueGroundManager clueGroundManager)
    {
        this.client = client;
		this.clueGroundManager = clueGroundManager;
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
            if (item != null && TRACKED_CLUE_IDS.contains(item.getId()))
            {
                int itemId = item.getId();

                // If clue is already in previous, keep the same ClueInstance
                ClueInstance clueInstance = previousTrackedCluesInInventory.get(itemId);
                if (clueInstance == null)
                {
                    System.out.println(itemId);
                    clueInstance = new ClueInstance(-1, itemId);
                }
                trackedCluesInInventory.put(itemId, clueInstance);
            }
        }

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
        Integer clueId = ClueText.forTextGetId(clueText);
        if (clueId == null) return;

        // Find the clue instance with undefined clueId
        for (ClueInstance clueInstance : trackedCluesInInventory.values())
        {
            if (clueInstance.getClueId() == -1)
            {
                clueInstance.setClueId(clueId);
                clueInstance.setClueText(clueText);
                break;
            }
        }
    }

    public ClueInstance getClueFromRecentlyDropped(int itemId)
    {
        // Iterate over the recently dropped clues to find one with matching itemId
        Iterator<ClueInstance> iterator = recentlyDroppedClues.descendingIterator();
        while (iterator.hasNext())
        {
            ClueInstance clueInstance = iterator.next();
            if (clueInstance.getItemId() == itemId)
            {
                iterator.remove(); // Remove it from the deque
                return clueInstance;
            }
        }
        return null;
    }

    public Collection<ClueInstance> getTrackedClues()
    {
        return trackedCluesInInventory.values();
    }

    public boolean hasTrackedClues()
    {
        return !trackedCluesInInventory.isEmpty();
    }

    public void onMenuEntryAdded(MenuEntryAdded event, CluePreferenceManager cluePreferenceManager, ClueDetailsParentPanel panel)
    {
        if (!shiftKeyDown)
        {
            return;
        }

        if (event.getTarget().contains("Clue scroll"))
        {
            int itemId = event.getIdentifier();
            boolean isMarked = cluePreferenceManager.getPreference(itemId);

            client.createMenuEntry(-1)
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
        }
    }

    @Override
    public void keyTyped(KeyEvent e)
    {
        // Not used
    }

    @Override
    public void keyPressed(KeyEvent e)
    {
        if (e.getKeyCode() == KeyEvent.VK_SHIFT)
        {
            shiftKeyDown = true;
        }
    }

    @Override
    public void keyReleased(KeyEvent e)
    {
        if (e.getKeyCode() == KeyEvent.VK_SHIFT)
        {
            shiftKeyDown = false;
        }
    }
}
