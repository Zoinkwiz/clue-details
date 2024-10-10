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

import java.util.stream.Collectors;
import lombok.Getter;
import net.runelite.api.*;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ItemDespawned;
import net.runelite.api.events.ItemSpawned;

import java.util.*;
import net.runelite.client.config.ConfigManager;

public class ClueGroundManager
{
    private final Client client;
	@Getter
	private final ClueGroundSaveDataManager clueGroundSaveDataManager;
	@Getter
	private final Map<WorldPoint, List<ClueInstance>> groundClues = new HashMap<>();
	private final List<PendingGroundClue> pendingGroundClues = new ArrayList<>();
	private final PriorityQueue<ClueInstance> despawnQueue;
	private final Set<WorldPoint> fullyKnownTiles = new HashSet<>();

	@Getter
	private final List<ClueInstance> despawnedClueQueue = new ArrayList<>();
	private final int MAX_RENDER_DISTANCE = 24;

    public ClueGroundManager(Client client, ConfigManager configManager)
    {
        this.client = client;
		this.clueGroundSaveDataManager = new ClueGroundSaveDataManager(configManager);
		clueGroundSaveDataManager.loadStateFromConfig(client);

	    Comparator<ClueInstance> comparator = Comparator.comparingInt(clue -> clue.getDespawnTick(client.getTickCount()));
		despawnQueue = new PriorityQueue<>(11, comparator);
    }

    public void onItemSpawned(ItemSpawned event)
    {
        TileItem item = event.getItem();
        if (isTrackedClue(item.getId()))
        {
			checkIfItemMatchesKnownItem(item, event.getTile().getWorldLocation());
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

		// If despawned under player, find if it was a clue we know of. If it is, mark it in inventory
		// We don't check the tile being under the player as technically the player could just telegrab it
		Optional<ClueInstance> optionalClue = cluesAtLocation.stream()
			.filter((clue) -> clue.getTileItem() == item)
			.findFirst();
		optionalClue.ifPresent(despawnedClueQueue::add);


		// Remove the clue with matching despawnTick
		cluesAtLocation.removeIf(clue -> clue.getTileItem() == item);

		if (cluesAtLocation.isEmpty())
		{
			groundClues.remove(location);
			despawnQueue.removeIf((clue) -> clue.getLocation().distanceTo(location) == 0);
		}
	}

    private void addClue(ClueInstance clue)
    {
        groundClues.computeIfAbsent(clue.getLocation(), k -> new ArrayList<>()).add(clue);
	    despawnQueue.add(clue);
    }

	private void removeClue(ClueInstance clue)
	{
		groundClues.get(clue.getLocation()).remove(clue);
	}

    private boolean isTrackedClue(int itemId)
    {
        return ItemID.CLUE_SCROLL_MASTER == itemId || ItemID.CLUE_SCROLL_BEGINNER == itemId ||
                ItemID.TORN_CLUE_SCROLL_PART_1 == itemId || ItemID.TORN_CLUE_SCROLL_PART_2 == itemId ||
                ItemID.TORN_CLUE_SCROLL_PART_3 == itemId;
    }

	public void checkIfItemMatchesKnownItem(TileItem tileItem, WorldPoint tileWp)
	{
		List<ClueInstance> knownItemsOnTile = groundClues.get(tileWp);
		if (knownItemsOnTile == null) return;

		for (ClueInstance clueInstance : knownItemsOnTile)
		{
			int currentTick = client.getTickCount();
			// For some reason this is always off by 1? IDK, but need to allow for it
			if (Math.abs(clueInstance.getDespawnTick(currentTick) - tileItem.getDespawnTime()) <= 1)
			{
				clueInstance.setTileItem(tileItem);
				return;
			}
		}
	}

    public void onGameTick()
    {
		processPendingGroundCluesOnGameTick();
		scanTilesForClueAssociation();
		processDespawnQueue();
    }

	private void processDespawnQueue()
	{
		int currentTick = client.getTickCount();

		while (!despawnQueue.isEmpty() && despawnQueue.peek().getDespawnTick(currentTick) <= currentTick)
		{
			ClueInstance clue = despawnQueue.poll();
			if (clue != null) removeClue(clue);
		}
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
					removedClue.getClueIds(),
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

	public void processPendingGroundCluesOnGameTick()
	{
		// Remove any with TileItem matching a properly tracked tile item
		pendingGroundClues.removeIf(pendingGroundClue ->
		{
			List<ClueInstance> groundCluesOnTile = groundClues.get(pendingGroundClue.getLocation());
			if (groundCluesOnTile == null) return false;
			return groundCluesOnTile
				.stream()
				.anyMatch(clueInstance -> clueInstance.getTileItem() == pendingGroundClue.getItem());
		});

		// Remove from pending clues. We will work them out when we're checking tiles for missing clues as well.
		pendingGroundClues.removeIf(pendingGroundClue ->
		{
			// Include unknown clues, so we can still use them for relative despawn considerations
			if (pendingGroundClue.getSpawnTick() + 2 < client.getTickCount())
			{
				ClueInstance groundClueInstance = new ClueInstance(
					List.of(),
					pendingGroundClue.getItem().getId(),
					pendingGroundClue.getLocation(),
					pendingGroundClue.getItem()
				);

				addClue(groundClueInstance);
				return true;
			}
			return false;
		});
	}

	private void scanTilesForClueAssociation()
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
			if (storedClues.isEmpty())
			{
				fullyKnownTiles.remove(location);
				iterator.remove();
				continue;
			}

			List<TileItem> groundClues = getTrackedItemsAtTile(tile);
			if (groundClues.isEmpty())
			{
				fullyKnownTiles.remove(location);
				iterator.remove();
				continue;
			}

			// At this point onwards, we'll resolve the known clues for the tile, and remove ones we don't know
			if (fullyKnownTiles.contains(location)) continue;
			fullyKnownTiles.add(location);

			// TODO: Do we need this to not be here, so we will update tileIDs on load?
			if (storedClues.stream().allMatch((clue) -> clue.getTileItem() != null)) continue;

			List<ClueInstance> updatedStoredClues = generateNewCluesOnTile(storedClues, groundClues);

			if (updatedStoredClues.isEmpty())
			{
				fullyKnownTiles.remove(location);
				iterator.remove();
			}
			else
			{
				// If we didn't find an item for it on the tile, remove it
				updatedStoredClues.removeIf((clue) -> clue.getTileItem() == null);

				// Update the stored clues
				entry.setValue(updatedStoredClues);
				despawnQueue.removeIf((clue) -> clue.getLocation().distanceTo(location) == 0);
				despawnQueue.addAll(entry.getValue());
			}
		}
	}

	private List<ClueInstance> generateNewCluesOnTile(List<ClueInstance> storedClues, List<TileItem> groundClues)
	{
		// ISSUE: If we place down an unknown clue in a stack, it won't be saved. This means it will fail. We should still catch it and keep in order
		int currentTick = client.getTickCount();

		if (storedClues.size() == 1 && groundClues.size() == 1)
		{
			if (storedClues.get(0).getDespawnTick(currentTick) > groundClues.get(0).getDespawnTime())
			{
				storedClues.get(0).setTileItem(groundClues.get(0));
				return storedClues;
			}
		}

		List<ClueInstance> sortedStoredClues = new ArrayList<>(storedClues);
		sortedStoredClues.sort(Comparator.comparingInt(ClueInstance::getTimeToDespawnFromDataInTicks));

		// If only 1 of either but not both, less certainty as can't use diffs.
		// Could assume things like last clue expired, probs let's just assume nothing
		if (storedClues.size() == 1 || groundClues.size() == 1)
		{
			return storedClues;
		}

		// Sort ground clues by despawn time ascending
		List<TileItem> sortedGroundClues = new ArrayList<>(groundClues);
		sortedGroundClues.removeIf((tileItem -> tileItem.getDespawnTime() > sortedStoredClues.get(sortedStoredClues.size() - 1).getDespawnTick(currentTick)));
		sortedGroundClues.sort(Comparator.comparingInt(TileItem::getDespawnTime));

		List<ClueInstance> foundClues = new ArrayList<>();

		// Need to loop diffs, and see matches in each.
		// For items with the same ID, no matter what item you click in a stack, you will always pick up the first item dropped in the stack
		// This means we don't need to worry about considering gaps where a clue has been taken from the middle of a stack.
		for (int i=0; i < sortedStoredClues.size() - 1; i++)
		{
			int currentStoredClueDiff = sortedStoredClues.get(i+1).getTimeToDespawnFromDataInTicks() - sortedStoredClues.get(i).getTimeToDespawnFromDataInTicks();
			for (int j=0; j < sortedGroundClues.size() - 1; j++)
			{
				// Should go j=0,1 as well
				int currentGroundClueDiff = sortedGroundClues.get(j+1).getDespawnTime() - sortedGroundClues.get(j).getDespawnTime();
				// Same diff, probs same thing
				if (currentGroundClueDiff != currentStoredClueDiff) continue;
				// If item will despawn later than the stored clue, it can't be it.
				if (sortedGroundClues.get(j).getDespawnTime() > sortedStoredClues.get(i).getDespawnTick(currentTick)) continue;
				if (sortedGroundClues.get(j+1).getDespawnTime() > sortedStoredClues.get(i+1).getDespawnTick(currentTick)) continue;

				// Else assume it's right. Currently overwrites a few times but probs okay?
				sortedStoredClues.get(i).setTileItem(sortedGroundClues.get(j));
				sortedStoredClues.get(i+1).setTileItem(sortedGroundClues.get(j+1));
			}
		}

		for (ClueInstance storedClue : sortedStoredClues)
		{
			if (storedClue.getTileItem() == null) continue;
			foundClues.add(storedClue);
		}

		return foundClues;
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

	public void saveStateToConfig()
	{
		clueGroundSaveDataManager.saveStateToConfig(client, groundClues);
	}

	public void loadStateFromConfig()
	{
		groundClues.clear();
		despawnQueue.clear();
		groundClues.putAll(clueGroundSaveDataManager.loadStateFromConfig(client));
		despawnQueue.addAll(groundClues.values()
			.stream()
			.flatMap(List::stream)
			.collect(Collectors.toList()));
		fullyKnownTiles.clear();
	}
}
