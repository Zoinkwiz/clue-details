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
import java.util.stream.Collectors;
import net.runelite.api.*;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.ItemDespawned;
import net.runelite.api.events.ItemSpawned;
import net.runelite.client.config.ConfigManager;

import java.lang.reflect.Type;
import java.util.*;

public class ClueGroundManager
{
    private final Client client;
    private final ConfigManager configManager;
    private final Gson gson = new Gson();

    private static final String CONFIG_GROUP = "clue-details";
    private static final String GROUND_CLUES_KEY = "ground-clues";
	private final List<ClueInstanceData> clueInstanceData = new ArrayList<>();;
    private final Map<WorldPoint, List<ClueInstance>> groundClues = new HashMap<>();
    private final List<PendingGroundClue> pendingGroundClues = new ArrayList<>();
    private final PriorityQueue<ClueInstance> despawnQueue = new PriorityQueue<>(Comparator.comparing(ClueInstance::getDespawnTick));
    private final int MAX_CLUE_DESPAWN_TICKS = 6000;

    private int savedTickCount = -1; // Initialize to -1 to indicate no saved ticks

    public ClueGroundManager(Client client, ConfigManager configManager)
    {
        this.client = client;
        this.configManager = configManager;
        loadStateFromConfig();
    }


	// Possibilities for an itemSpawned for a clue:
	// 1. The player just dropped the clue
	// 2. The player has entered the spawn radius for it.
	// For 2, we need to check if the clue is from the known clues in config
	// If not, we remove it after say 5 ticks, to allow for the onItemContainerChanged event to occur
    public void onItemSpawned(ItemSpawned event)
    {
        TileItem item = event.getItem();
        if (isTrackedClue(item.getId()))
        {
            // Add to pending ground clues
            pendingGroundClues.add(new PendingGroundClue(item, event.getTile().getWorldLocation(), client.getTickCount()));
        }
    }

    public void onItemDespawned(ItemDespawned event)
	{
		TileItem item = event.getItem();
		if (!isTrackedClue(item.getId())) return;

		WorldPoint location = event.getTile().getWorldLocation();

		List<ClueInstance> cluesAtLocation = groundClues.get(location);
		if (cluesAtLocation == null) return;

		// Remove the clue with matching despawnTick
		cluesAtLocation.removeIf(clue -> clue.getTileItem() == item);

		if (cluesAtLocation.isEmpty())
		{
			groundClues.remove(location);
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
		System.out.println(client.getTickCount());
		processPendingGroundCluesOnGameTick();
		// TODO: this should be done more smartly. For example, on first seeing of a known tile within the limit radius
		// 	test just that tile, and don't test it again
		scanTilesForClueAssociation();
		/*
		groundClues.forEach((wp, listClues) -> {
			for (ClueInstance clue : listClues)
			{
				TileItem item = clue.getTileItem();
				if (item == null)
				{
					System.out.println("NULL");
					continue;
				}
				System.out.println(item.getDespawnTime());
			}
		});

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

        */
    }

    public void processPendingGroundCluesFromInventoryChanged(ClueInstance removedClue)
	{
		Iterator<PendingGroundClue> groundClueIterator = pendingGroundClues.iterator();
		while (groundClueIterator.hasNext())
		{
			PendingGroundClue pendingGroundClue = groundClueIterator.next();
			// This should be enough, as a player shouldn't be able to drop two of the same item in the same tick
			// As you can only have one of each item on you at once
			if (removedClue.getItemId() == pendingGroundClue.getItem().getId())
			{
				// Found a match
				ClueInstance groundClueInstance = new ClueInstance(
					removedClue.getClueId(),
					pendingGroundClue.getItem().getId(),
					pendingGroundClue.getLocation(),
					pendingGroundClue.getItem()
				);

				addClue(groundClueInstance);

				// Remove matched clues from pending lists
				groundClueIterator.remove();
				break;
			}
		}
    }

	// Scenarios:
	// 1. We log in/tele somewhere, and have some knowledge of expected clues to be somewhere.
	// 	  From this, we enter the area with ClueInstances tracked for the spot, but with no TileItem attached
	//    onItemSpawned called for these items. We need to work out which items match to which ClueInstance.
	// 2. We log in, and find out that some clues have been removed, maybe more added since last logging in.
	//    We need to look through them all, and see which match, and which probs are new.
	// ISSUE: Stuff is mixed between pending and not pending. We should consolidate unknown to known, but without
	// a TileItem attached. Do a scan for missing steps with TileItem

	public void processPendingGroundCluesOnGameTick()
	{
		// Remove from pending clues. We will work them out when we're checking tiles for missing clues as well.
		pendingGroundClues.removeIf(pendingGroundClue -> pendingGroundClue.getSpawnTick() + 5 < client.getTickCount());
	}

	private void scanTilesForClueAssociation()
	{
		// We don't care about clues we didn't know about, because we still don't know anything about them
		// We checks clue we did know, to find them on tiles.
		Iterator<Map.Entry<WorldPoint, List<ClueInstance>>> iterator = groundClues.entrySet().iterator();
		while (iterator.hasNext())
		{
			Map.Entry<WorldPoint, List<ClueInstance>> entry = iterator.next();
			WorldPoint location = entry.getKey();

			if (!isTileWithinRenderDistance(location))
			{
				continue;
			}

			Tile tile = getTileAtWorldPoint(location);
			if (tile == null)
			{
				continue;
			}

			List<ClueInstance> storedClues = entry.getValue();
			if (storedClues.isEmpty())
			{
				iterator.remove();
				continue;
			}

			List<TileItem> groundClues = getTrackedItemsAtTile(tile);
			if (groundClues.isEmpty())
			{
				iterator.remove();
				continue;
			}

			// If all have known items, don't do anything.
			if (storedClues.stream().allMatch((clue) -> clue.getTileItem() != null)) continue;

			// Compare stored clues and ground clues
			List<ClueInstance> updatedStoredClues = generateNewCluesOnTile(storedClues, groundClues);

			if (updatedStoredClues.isEmpty())
			{
				// All clues are missing, remove the entry
				iterator.remove();
			}
			else
			{
				// Update the stored clues
				entry.setValue(updatedStoredClues);
			}
		}
	}

	private List<ClueInstance> generateNewCluesOnTile(List<ClueInstance> storedClues, List<TileItem> groundClues)
	{
		if (storedClues.size() == 1 && groundClues.size() == 1)
		{
			if (storedClues.get(0).getDespawnTick() > groundClues.get(0).getDespawnTime())
			{
				storedClues.get(0).setTileItem(groundClues.get(0));
				return storedClues;
			}
		}

		List<ClueInstance> sortedStoredClues = new ArrayList<>(storedClues);
		sortedStoredClues.sort(Comparator.comparingInt(ClueInstance::getDespawnTick));

		// If only 1 of either but not both, less certainty as can't use diffs.
		// Could assume things like last clue expired, probs let's just assume nothing
		if (storedClues.size() == 1 || groundClues.size() == 1)
		{
			return storedClues;
		}

		// Sort ground clues by despawn time ascending
		List<TileItem> sortedGroundClues = new ArrayList<>(groundClues);
		sortedGroundClues.sort(Comparator.comparingInt(TileItem::getDespawnTime));
		sortedGroundClues.removeIf((tileItem -> tileItem.getDespawnTime() > sortedStoredClues.get(sortedGroundClues.size() - 1).getDespawnTick()));

		// Want to generate expected diffs
		List<DespawnDiff> despawnDiffsStoredClues = new ArrayList<>();
		for (int i=0; i < sortedStoredClues.size() - 1; i++)
		{
			for (int j=i+1; j < sortedStoredClues.size(); j++)
			{
				despawnDiffsStoredClues.add(new DespawnDiff(sortedStoredClues.get(i), sortedStoredClues.get(j)));
			}
		}

		List<DespawnDiff> despawnDiffsGroundClues = new ArrayList<>();
		for (int i=0; i < sortedGroundClues.size() - 1; i++)
		{
			for (int j=i+1; j < sortedGroundClues.size(); j++)
			{
				despawnDiffsGroundClues.add(new DespawnDiff(sortedGroundClues.get(i), sortedGroundClues.get(j)));
			}
		}

		// Need to loop diffs, and see matches in each.
		for (DespawnDiff despawnDiffsStoredClue : despawnDiffsStoredClues)
		{
			// Find all with matching diffs
			for (DespawnDiff despawnDiffsGroundClue : despawnDiffsGroundClues)
			{
				// Same diff, probs same thing
				if (despawnDiffsGroundClue.getDespawnDiff() != despawnDiffsStoredClue.getDespawnDiff()) continue;
				// If item will despawn later than the stored clue, it can't be it.
				if (despawnDiffsGroundClue.getDespawn1() > despawnDiffsStoredClue.getDespawn1()) continue;
				if (despawnDiffsGroundClue.getDespawn2() > despawnDiffsStoredClue.getDespawn2()) continue;

				// Else assume it's right. Currently overwrites a few times but probs okay?
				despawnDiffsGroundClue.getClue1().setTileItem(despawnDiffsStoredClue.getTileItem1());
				despawnDiffsGroundClue.getClue2().setTileItem(despawnDiffsStoredClue.getTileItem2());
			}
		}

		return sortedStoredClues;
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
		updateData();
        String groundCluesJson = gson.toJson(clueInstanceData);
        configManager.setConfiguration(CONFIG_GROUP, GROUND_CLUES_KEY, groundCluesJson);
        configManager.setConfiguration(CONFIG_GROUP, "savedTickCount", savedTickCount);
    }

	private void updateData()
	{
		List<ClueInstanceData> newData = new ArrayList<>();
		for (Map.Entry<WorldPoint, List<ClueInstance>> entry : groundClues.entrySet())
		{
			List<ClueInstance> clueDataList = entry.getValue();
			int currentTick = client.getTickCount();
			for (ClueInstance data : clueDataList)
			{
				newData.add(new ClueInstanceData(data, currentTick));
			}
		}
		clueInstanceData.clear();
		clueInstanceData.addAll(newData);
	}

    public void loadStateFromConfig()
    {
        // Deserialize groundClues and savedTickCount from config
        String groundCluesJson = configManager.getConfiguration(CONFIG_GROUP, GROUND_CLUES_KEY);
		clueInstanceData.clear();
        if (groundCluesJson != null)
        {
            try
            {
                Type groundCluesType = new TypeToken<List<ClueInstanceData>>()
                {
                }.getType();

                List<ClueInstanceData> loadedGroundCluesData = gson.fromJson(groundCluesJson, groundCluesType);

				int currentTick = client.getTickCount();
                // Convert ClueInstanceData back to ClueInstance
				for (ClueInstanceData clueData : loadedGroundCluesData)
				{
					WorldPoint location = clueData.getLocation();
					List<ClueInstance> clueInstances = new ArrayList<>();
					ClueInstance clue = new ClueInstance(clueData, currentTick);
					clueInstances.add(clue);

					clueInstanceData.add(clueData);
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

	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		if (gameStateChanged.getGameState() == GameState.LOGGED_IN)
		{
			checkForMissingClues();
		}
	}

	private final int MAX_RENDER_DISTANCE = 24;

	public void checkForMissingClues()
	{
		Iterator<Map.Entry<WorldPoint, List<ClueInstance>>> iterator = groundClues.entrySet().iterator();
		while (iterator.hasNext())
		{
			Map.Entry<WorldPoint, List<ClueInstance>> entry = iterator.next();
			WorldPoint location = entry.getKey();

			if (!isTileWithinRenderDistance(location))
			{
				continue;
			}

			Tile tile = getTileAtWorldPoint(location);
			if (tile == null)
			{
				continue;
			}

			List<ClueInstance> storedClues = entry.getValue();
			List<TileItem> groundClues = getTrackedItemsAtTile(tile);

			if (groundClues.isEmpty())
			{
				// No clues on the tile, remove stored clues
				iterator.remove();
				continue;
			}

			// Compare stored clues and ground clues
			List<ClueInstance> updatedStoredClues = compareCluesOnTile(storedClues, groundClues);

			if (updatedStoredClues.isEmpty())
			{
				// All clues are missing, remove the entry
				iterator.remove();
			}
			else
			{
				// Update the stored clues
				entry.setValue(updatedStoredClues);
			}
		}
	}

	private boolean isTileWithinRenderDistance(WorldPoint tileWp)
	{
		if (tileWp == null)
		{
			return false;
		}
		int distance = client.getLocalPlayer().getWorldLocation().distanceTo2D(tileWp);
		return distance <= MAX_RENDER_DISTANCE;
	}

	private Tile getTileAtWorldPoint(WorldPoint tileWp)
	{
		WorldView worldView = client.getTopLevelWorldView();
		LocalPoint tileLp = LocalPoint.fromWorld(worldView, tileWp);
		if (tileLp == null)
		{
			return null;
		}
		return worldView.getScene().getTiles()[tileWp.getPlane()][tileLp.getSceneX()][tileLp.getSceneY()];
	}

	private List<TileItem> getTrackedItemsAtTile(Tile tile)
	{
		List<TileItem> items = tile.getGroundItems();
		if (items == null)
		{
			return Collections.emptyList();
		}
		return items.stream()
			.filter(item -> isTrackedClue(item.getId()))
			.collect(Collectors.toList());
	}

	// TODO: Ensure tick returned from ClueInstance is considering current tick system
	// TODO: Ensure ???
	private List<ClueInstance> compareCluesOnTile(List<ClueInstance> storedClues, List<TileItem> groundClues)
	{
		List<ClueInstance> sortedStoredClues = new ArrayList<>(storedClues);
		sortedStoredClues.sort(Comparator.comparingInt(ClueInstance::getDespawnTick));

		// Sort ground clues by despawn time ascending
		List<TileItem> sortedGroundClues = new ArrayList<>(groundClues);
		sortedGroundClues.sort(Comparator.comparingInt(TileItem::getDespawnTime));

		// Map to hold the matched stored clue to ground clue
		Map<ClueInstance, TileItem> matchedClues = new HashMap<>();

		// Tolerance for despawn time differences
		final int TOLERANCE = 1;

		// First, attempt to match based on positions (assuming order hasn't changed)
		for (int i = 0; i < sortedStoredClues.size(); i++)
		{
			ClueInstance storedClue = sortedStoredClues.get(i);

			if (i < sortedGroundClues.size())
			{
				TileItem groundClue = sortedGroundClues.get(i);
				int despawnDifference = storedClue.getDespawnTick() - groundClue.getDespawnTime();

				if (despawnDifference <= TOLERANCE)
				{
					matchedClues.put(storedClue, groundClue);
					storedClue.setTileItem(groundClue);
					continue;
				}
			}

			// If position-based matching fails, try to match individually
			boolean matched = false;
			for (TileItem groundClue : sortedGroundClues)
			{
				if (matchedClues.containsValue(groundClue))
				{
					continue;
				}

				int despawnDifference = storedClue.getDespawnTick() - groundClue.getDespawnTime();
				if (despawnDifference <= TOLERANCE)
				{
					matchedClues.put(storedClue, groundClue);
					matched = true;
					break;
				}
			}


			// Matched means tick shift hasn't happened, means probably didn't do a lot on a non-tracked mobile device or w/e
			if (!matched)
			{
				// Try matching based on relative differences

				// Attempt to match storedClue with pairs of ground clues
				for (int k = 0; k < sortedGroundClues.size(); k++)
				{
					for (int j = k + 1; j < sortedGroundClues.size(); j++)
					{
						int groundDiff = sortedGroundClues.get(j).getDespawnTime() - sortedGroundClues.get(k).getDespawnTime();
						int storedDiff = getClosestDespawnDifference(storedClue, sortedStoredClues);

						if (Math.abs(groundDiff - storedDiff) <= TOLERANCE)
						{
							// Match storedClue to one of these ground clues
							matchedClues.put(storedClue, sortedGroundClues.get(k));
							matched = true;
							break;
						}
					}
					if (matched)
					{
						break;
					}
				}
			}
		}

		// Return only the matched stored clues
		return new ArrayList<>(matchedClues.keySet());
	}

	private int getClosestDespawnDifference(ClueInstance storedClue, List<ClueInstance> sortedStoredClues)
	{
		int index = sortedStoredClues.indexOf(storedClue);
		int storedClueDespawnTick = storedClue.getDespawnTick();

		int closestDifference = Integer.MAX_VALUE;

		// Compare with the next clue
		if (index + 1 < sortedStoredClues.size())
		{
			int nextDespawnTick = sortedStoredClues.get(index + 1).getDespawnTick();
			int diff = nextDespawnTick - storedClueDespawnTick;
			closestDifference = Math.min(closestDifference, diff);
		}

		// Compare with the previous clue
		if (index - 1 >= 0)
		{
			int prevDespawnTick = sortedStoredClues.get(index - 1).getDespawnTick();
			int diff = storedClueDespawnTick - prevDespawnTick;
			closestDifference = Math.min(closestDifference, diff);
		}

		return closestDifference == Integer.MAX_VALUE ? 0 : closestDifference;
	}
}
