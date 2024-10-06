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
import com.google.gson.JsonSyntaxException;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.config.RuneScapeProfileType;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Singleton
public class ClueFloorManager
{
    private final ConfigManager configManager;
    private final Client client;
    private final Gson gson;

    private static final String CONFIG_GROUP = ClueDetailsConfig.CLUE_DETAILS_CONFIG;
    private static final String FLOOR_CLUE_KEY = "floor-clue";

    @Getter
    private Map<WorldPoint, List<FloorClue>> floorClues;
    private ClueFloorData floorClueData;

    // We keep this to hold the rsProfileKey even when the player logs out, to then save
    private String rsProfileKey;
    private RuneScapeProfileType worldType;

    private boolean loggedInStateKnown;

    @Inject
    public ClueFloorManager(Client client, ConfigManager configManager, Gson gson)
    {
        this.configManager = configManager;
        this.client = client;
        this.gson = gson;
        this.floorClues = new HashMap<>();
        this.floorClueData = new ClueFloorData();
    }

    public void addFloorClue(FloorClue floorClue)
    {
        WorldPoint wp = floorClue.worldPoint;
        if(!floorClues.containsKey(wp)) floorClues.put(wp, new ArrayList<>());
        floorClues.get(wp).add(floorClue);
    }

    public void emptyState()
    {
        rsProfileKey = null;
        worldType = null;
        floorClueData.setEmpty();
    }

    public void loadInitialStateFromConfig(Client client)
    {
        if (!loggedInStateKnown)
        {
            Player localPlayer = client.getLocalPlayer();
            if (localPlayer != null && localPlayer.getName() != null)
            {
                loggedInStateKnown = true;
                loadState();
            }
        }
    }

    public void setUnknownInitialState()
    {
        loggedInStateKnown = false;
    }

    public void loadState()
    {
        System.out.println(RuneScapeProfileType.getCurrent(client));
        System.out.println(worldType);
        // Only re-load from config if loading from a new profile
        if (!RuneScapeProfileType.getCurrent(client).equals(worldType))
        {
            // If we've hopped between profiles
            if (rsProfileKey != null)
            {
                saveFloorCluesToConfig();
            }
            loadFloorCluesFromConfig();
        }
    }

    private void loadFloorCluesFromConfig()
    {
        rsProfileKey = configManager.getRSProfileKey();
        worldType = RuneScapeProfileType.getCurrent(client);

        String json = configManager.getRSProfileConfiguration(CONFIG_GROUP, FLOOR_CLUE_KEY);
        try
        {
            floorClueData.setFloorClueDetails(gson.fromJson(json, int[].class));
        }
        catch (JsonSyntaxException err)
        {
            // Due to changing data format from list to array, need to handle for old users
            floorClueData.setFloorClueDetails(new int[0]);
            saveFloorCluesToConfig();
        }
        // TODO: Look if value saved correctly by loading it in
        List<FloorClue> allFloorClues = floorClueData.getAsList(client);
        floorClues.clear();
        for (FloorClue floorClue : allFloorClues)
        {
            addFloorClue(floorClue);
        }
    }

    public void saveFloorCluesToConfig()
    {
        if (rsProfileKey == null)
        {
            return;
        }

        List<FloorClue> allFloorClues = floorClues.values()
                .stream()
                .flatMap(List::stream)
                .collect(Collectors.toList());

        floorClueData.set(client, allFloorClues);
        configManager.setConfiguration(CONFIG_GROUP, rsProfileKey, FLOOR_CLUE_KEY, gson.toJson(floorClueData.getFloorClueDetails()));
        System.out.println(Arrays.toString(floorClueData.getFloorClueDetails()));
    }

    public boolean isNewClue(TileItem tileItem)
    {
        // This is called from onContanerChanged, which seems to happen a tick after being on the floor
        // TODO: How does this work for lag?
        return (tileItem.getDespawnTime() - client.getTickCount()) == 6099;
    }

    public FloorClue getExistingFloorClue(TileItem tileItem, Tile tile)
    {
        List<FloorClue> cluesOnTile = floorClues.get(tile.getWorldLocation());
        if (cluesOnTile == null) return null;

        List<TileItem> allCluesOnTile = tile.getGroundItems();
        if (allCluesOnTile == null) return null;

        allCluesOnTile = allCluesOnTile.stream()
                .filter((item) -> item.getId() == ItemID.CLUE_SCROLL_MASTER)
                .sorted(Comparator.comparing(TileItem::getDespawnTime))
                .collect(Collectors.toList());

        // Some mishap occured
        if (allCluesOnTile.size() != cluesOnTile.size()) return null;

        return cluesOnTile.get(allCluesOnTile.indexOf(tileItem));
    }

    public void clearClues()
    {
        // TODO: No instance considerations currently
        for (WorldPoint wp : floorClues.keySet())
        {
            List<FloorClue> cluesToRemove = new ArrayList<>();
            List<FloorClue> floorCluesOnTile = floorClues.get(wp);
            for (FloorClue floorClue : floorCluesOnTile)
            {
                LocalPoint lp = LocalPoint.fromWorld(client.getTopLevelWorldView(), wp);
                if (lp == null) continue;

                Tile tile = client.getTopLevelWorldView().getScene().getTiles()[wp.getPlane()][lp.getSceneX()][lp.getSceneY()];
                if (tile == null) continue;

                List<TileItem> tileItems = tile.getGroundItems();
                if (tileItems == null || tileItems.isEmpty())
                {
                    cluesToRemove.add(floorClue);
                    continue;
                }

                for (TileItem tileItem : tileItems)
                {
                    if (tileItem.getId() == ItemID.CLUE_SCROLL_MASTER)
                    {
//                    if (getExistingFloorClue(tileItem) == null) continue;
//                    floorClues.remove(floorClue);
                    }
                }
            }

            for (FloorClue floorClue : cluesToRemove)
            {
                floorClues.get(wp).remove(floorClue);
            }
        }
    }
}
