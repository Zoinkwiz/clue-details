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
import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ItemDespawned;
import net.runelite.api.events.ItemSpawned;
import net.runelite.client.config.ConfigManager;

import java.lang.reflect.Type;
import java.util.*;

public class ClueGroundManager
{
    private final Client client;
    private final ConfigManager configManager;
    private final ClueInventoryManager clueInventoryManager;
    private final Gson gson = new Gson();

    private static final String CONFIG_GROUP = "clue-details";
    private static final String GROUND_CLUES_KEY = "ground-clues";

    private final Map<WorldPoint, List<ClueInstance>> groundClues = new HashMap<>();
    private final List<PendingGroundClue> pendingGroundClues = new ArrayList<>();
    private final PriorityQueue<ClueInstance> despawnQueue = new PriorityQueue<>(Comparator.comparing(ClueInstance::getDespawnTick));
    private final int MAX_CLUE_DESPAWN_TICKS = 6000;

    private int savedTickCount = -1; // Initialize to -1 to indicate no saved ticks

    public ClueGroundManager(Client client, ConfigManager configManager, ClueInventoryManager clueInventoryManager)
    {
        this.client = client;
        this.configManager = configManager;
        this.clueInventoryManager = clueInventoryManager;
        loadStateFromConfig();
    }

    public void onItemSpawned(ItemSpawned event)
    {
        TileItem item = event.getItem();
        if (isTrackedClue(item.getId()))
        {
            // Add to pending ground clues
            pendingGroundClues.add(new PendingGroundClue(item, event.getTile().getWorldLocation(), item.getDespawnTime()));
        }
    }

    public void onItemDespawned(ItemDespawned event)
    {
        TileItem item = event.getItem();
        if (isTrackedClue(item.getId()))
        {
            WorldPoint location = event.getTile().getWorldLocation();
            int despawnTick = item.getDespawnTime();

            List<ClueInstance> cluesAtLocation = groundClues.get(location);
            if (cluesAtLocation != null)
            {
                // Remove the clue with matching despawnTick
                cluesAtLocation.removeIf(clue -> clue.getDespawnTick() == despawnTick);

                if (cluesAtLocation.isEmpty())
                {
                    groundClues.remove(location);
                }
            }
        }
    }

    private void addClue(ClueInstance clue)
    {
        groundClues.computeIfAbsent(clue.getLocation(), k -> new ArrayList<>()).add(clue);
    }

    private boolean isTrackedClue(int itemId)
    {
        return ItemID.CLUE_SCROLL_MASTER == itemId || ItemID.CLUE_SCROLL_BEGINNER == itemId ||
                ItemID.TORN_CLUE_SCROLL_PART_1 == itemId || ItemID.TORN_CLUE_SCROLL_PART_2 == itemId ||
                ItemID.TORN_CLUE_SCROLL_PART_3 == itemId;
    }

    public void onGameTick()
    {
        processPendingGroundClues();

        int currentTick = client.getTickCount();

        // If this is the first tick after startup or login, adjust despawn ticks
        if (savedTickCount != -1 && savedTickCount != currentTick)
        {
            int ticksPassed = currentTick - savedTickCount;

            adjustDespawnTicks(ticksPassed);
        }

        // Update savedTickCount for next tick
        savedTickCount = currentTick;

        // Remove clues that have despawned
        removeDespawnedClues(currentTick);
    }

    private void processPendingGroundClues()
    {
        List<ClueInstance> pendingRemovedClues = clueInventoryManager.getPendingRemovedClues();

        Iterator<PendingGroundClue> groundClueIterator = pendingGroundClues.iterator();
        while (groundClueIterator.hasNext())
        {
            PendingGroundClue pendingGroundClue = groundClueIterator.next();

            // Attempt to find a matching removed clue based on itemId
            Iterator<ClueInstance> removedClueIterator = pendingRemovedClues.iterator();
            while (removedClueIterator.hasNext())
            {
                ClueInstance removedClue = removedClueIterator.next();

                if (removedClue.getItemId() == pendingGroundClue.getItem().getId())
                {
                    // Found a match
                    ClueInstance groundClueInstance = new ClueInstance(
                            removedClue.getClueId(),
                            pendingGroundClue.getItem().getId(),
                            pendingGroundClue.getLocation(),
                            pendingGroundClue.getDespawnTick()
                    );

                    addClue(groundClueInstance);

                    // Remove matched clues from pending lists
                    groundClueIterator.remove();
                    removedClueIterator.remove();
                    break;
                }
            }
        }

        // Optionally, handle any unmatched pending ground clues
        // For unmatched clues, you can decide whether to create a ClueInstance with default values or discard them
    }

    private void removeDespawnedClues(int currentTick)
    {
        Iterator<Map.Entry<WorldPoint, List<ClueInstance>>> iterator = groundClues.entrySet().iterator();
        while (iterator.hasNext())
        {
            Map.Entry<WorldPoint, List<ClueInstance>> entry = iterator.next();
            List<ClueInstance> clues = entry.getValue();

            clues.removeIf(clue -> clue.getDespawnTick() <= currentTick);

            if (clues.isEmpty())
            {
                iterator.remove();
            }
        }
    }

    private void adjustDespawnTicks(int ticksPassed)
    {
        for (List<ClueInstance> cluesAtLocation : groundClues.values())
        {
            for (ClueInstance clue : cluesAtLocation)
            {
                clue.adjustDespawnTick(ticksPassed);
            }
        }
    }

    private boolean hasClueDespawned(ClueInstance clue, int currentTick)
    {
        int ticksSinceSpawn = currentTick - clue.getDespawnTick() + clue.getRelativeDespawnTime(client);
        return ticksSinceSpawn >= MAX_CLUE_DESPAWN_TICKS;
    }


    private void removeClue(ClueInstance clue, WorldPoint location)
    {
        List<ClueInstance> cluesAtLocation = groundClues.get(location);
        if (cluesAtLocation != null)
        {
            cluesAtLocation.remove(clue);
            if (cluesAtLocation.isEmpty())
            {
                groundClues.remove(location);
            }
        }
    }

    private void removeClueAtLocation(int despawnTick, WorldPoint location)
    {
        List<ClueInstance> cluesAtLocation = groundClues.get(location);
        if (cluesAtLocation != null)
        {
            cluesAtLocation.removeIf(clue -> clue.getDespawnTick() == despawnTick);
            if (cluesAtLocation.isEmpty())
            {
                groundClues.remove(location);
            }
        }
    }

    public void saveStateToConfig()
    {
        // Serialize groundClues and savedTickCount and save to config
        // Use Gson or another serialization method
        String groundCluesJson = gson.toJson(groundClues);
        System.out.println("BEEP");
        System.out.println(groundCluesJson);
        configManager.setConfiguration(CONFIG_GROUP, GROUND_CLUES_KEY, groundCluesJson);
        configManager.setConfiguration(CONFIG_GROUP, "savedTickCount", savedTickCount);
    }

    public void loadStateFromConfig()
    {
        // Deserialize groundClues and savedTickCount from config
        String groundCluesJson = configManager.getConfiguration(CONFIG_GROUP, GROUND_CLUES_KEY);
        if (groundCluesJson != null)
        {
            try
            {
                Type groundCluesType = new TypeToken<Map<WorldPoint, List<ClueInstanceData>>>()
                {
                }.getType();
                Map<WorldPoint, List<ClueInstanceData>> loadedGroundCluesData = gson.fromJson(groundCluesJson, groundCluesType);

                // Convert ClueInstanceData back to ClueInstance
                for (Map.Entry<WorldPoint, List<ClueInstanceData>> entry : loadedGroundCluesData.entrySet())
                {
                    WorldPoint location = entry.getKey();
                    List<ClueInstanceData> clueDataList = entry.getValue();
                    List<ClueInstance> clueInstances = new ArrayList<>();
                    for (ClueInstanceData data : clueDataList)
                    {
                        ClueInstance clue = new ClueInstance(data);
                        clueInstances.add(clue);
                    }
                    groundClues.put(location, clueInstances);
                }
            } catch (Exception err)
            {
                groundClues.clear();
                saveStateToConfig();
            }
        }

        Integer savedTickCountConfig = configManager.getConfiguration(CONFIG_GROUP, "savedTickCount", Integer.class);
        savedTickCount = Objects.requireNonNullElseGet(savedTickCountConfig, client::getTickCount);
    }
}
