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

import com.cluedetails.filters.ClueTier;
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

	private final ClueDetailsPlugin clueDetailsPlugin;
	@Getter
	private final ClueGroundSaveDataManager clueGroundSaveDataManager;
//	@Getter
	private final Map<WorldPoint, List<ClueInstance>> groundCluesBeginnerAndMaster = new HashMap<>();

	@Getter
	private final Map<WorldPoint, List<ClueInstance>> groundCluesEasyToElite = new HashMap<>();
	private final Set<Tile> itemHasSpawnedOnTileThisTick = new HashSet<>();

	@Getter
	private final List<ClueInstance> despawnedClueQueueForInventoryCheck = new ArrayList<>();
	private final int MAX_DESPAWN_TIMER = 6100;
	private Zone lastZone;
	private Zone currentZone;
	private Set<Zone> zonesSeenThisSession = new HashSet<>();

	public ClueGroundManager(Client client, ConfigManager configManager, ClueDetailsPlugin clueDetailsPlugin)
	{
		this.client = client;
		this.clueDetailsPlugin = clueDetailsPlugin;
		this.clueGroundSaveDataManager = new ClueGroundSaveDataManager(configManager, clueDetailsPlugin.gson);
		clueGroundSaveDataManager.loadStateFromConfig(client);
	}

	// Three spawn scenarios:
	// 1. Item spawns with max despawn time. Means dropped/drop. Can work out by if in inventory or not.
	// 1a. If in inventory, set ClueInstance from there to floor. ALSO REMOVE FROM INVENTORY?
	// 1b. If not, fresh ClueInstance.
	// 2. Item doesn't have max despawn time. Probably something we already know about.
	// 2a. We've got a store of clues for the tile from this session. Unless you did something weird like kill on same tile as stack and tele out
	//     It will be the same as before, so just need to iterate whole pile after
	// 2b. Not seen stack this session. Still need to check it I guess?

	// CURRENTLY:
	// Check if easy-elite, if so just add it as no tracking needed.
	// Next, check if new clue. If it is and it came from the inventory, copy the ClueInstance from the inventory.
	// If it's not dropped and is beginner/master clue, then add pile to the to be considered pile in onGameTick
	public void onItemSpawned(ItemSpawned event)
	{
		TileItem item = event.getItem();
		Tile tile = event.getTile();
		WorldPoint wp = tile.getWorldLocation();

		if (!Clues.isClue(item.getId(), clueDetailsPlugin.isDeveloperMode())) return;

		// If easy-elite task, we just override
		// TODO: Maybe also means torn clues and such?
		if (!Clues.isBeginnerOrMasterClue(item.getId(), clueDetailsPlugin.isDeveloperMode()))
		{
			// TODO: Should final arguement be client.getTickCount, or MAX_DESPAWN_TIME?
			ClueInstance clueInstance = new ClueInstance(List.of(), item.getId(), tile.getWorldLocation(), item, client.getTickCount());
			addEasyToEliteClue(clueInstance);
			return;
		}

		ClueInstance inventoryClue = clueDetailsPlugin.getClueInventoryManager().getTrackedClueByClueItemId(item.getId());
		// If clue in inventory AND new clue appeared with fresh despawn timer, it must be the inventory item being dropped
		if (isNewGroundClue(item.getId(), item.getDespawnTime()) && inventoryClue != null)
		{
			ClueInstance newGroundClue = new ClueInstance(inventoryClue.getClueIds(), inventoryClue.getItemId(), tile.getWorldLocation(), item, client.getTickCount());
			addClue(newGroundClue);
			return;
		}

		// Handle items spawned on tile without aligned times and not dropped
		itemHasSpawnedOnTileThisTick.add(tile);
	}

	private boolean isNewGroundClue(int itemID, int despawnTick)
	{
		int ticksToDespawn = despawnTick - client.getTickCount();

		if (ticksToDespawn == MAX_DESPAWN_TIMER) return true;

		return clueDetailsPlugin.isDeveloperMode() &&
			Clues.DEV_MODE_IDS.contains(itemID) &&
			despawnTick >= 300;
	}

	public void onItemDespawned(ItemDespawned event)
	{
		TileItem item = event.getItem();
		if (!Clues.isClue(item.getId(), clueDetailsPlugin.isDeveloperMode())) return;
		WorldPoint location = event.getTile().getWorldLocation();

		if (!Clues.isBeginnerOrMasterClue(item.getId(), clueDetailsPlugin.isDeveloperMode()))
		{
			ClueInstance clueInstance = new ClueInstance(List.of(), item.getId(), location, item, client.getTickCount());
			removeEasyToEliteClue(clueInstance);
			return;
		}

		List<ClueInstance> cluesAtLocation = groundCluesBeginnerAndMaster.get(location);

		// Catch despawn in vicinity
		if (cluesAtLocation == null) return;

		// If no logging out/reloading and such happens, despawnTime remains off by 1, so need to account for it
		if (item.getDespawnTime() - client.getTickCount() <= 1)
		{
			Optional<ClueInstance> optionalClue = cluesAtLocation.stream()
				.filter((clue) -> clue.getTileItem() == item)
				.findFirst();
			optionalClue.ifPresent(this::removeClue);
			return;
		}

		if (getTileAtWorldPoint(location) == null)
		{
			return;
		}

		// If despawned on a tile still in the scene, AND hasn't timed out, we might have:
		// 1. Picked up the clue
		// 2. Done nothing, clue is still there just with a new ID
		// We know it's 2 if we've gone from 5 zones distance to 4 zones distance
		Zone clueZone = new Zone(location);
		Zone currentZone = new Zone(client.getLocalPlayer().getWorldLocation());
		if (lastZone != null)
		{
			int distFromLastZone = clueZone.maxDistanceTo(lastZone);
			int distFromCurrentZone = clueZone.maxDistanceTo(currentZone);
			if (distFromLastZone == 4 && distFromCurrentZone == 3)
			{
				return;
			}
		}

		// Not gone over a zone to load, probably picked up
		Optional<ClueInstance> optionalClue = cluesAtLocation.stream()
			.filter((clue) -> clue.getTileItem() == item)
			.findFirst();
		optionalClue.ifPresent(despawnedClueQueueForInventoryCheck::add);

		// Remove the clue with matching tileItem
		cluesAtLocation.removeIf(clue -> clue.getTileItem() == item);

		// Clue despawned, don't know if it will spawn again as a new TileItem, or if it is gonezo
		// If it does respawn, we need it still in groundItems to check
		// If it doesn't respawn, we have nothing which is checking the tile

		if (cluesAtLocation.isEmpty())
		{
			groundCluesBeginnerAndMaster.remove(location);
		}
	}

	private void addClue(ClueInstance clue)
	{
		groundCluesBeginnerAndMaster.computeIfAbsent(clue.getLocation(), k -> new ArrayList<>()).add(clue);
	}

	private void addEasyToEliteClue(ClueInstance clueInstance)
	{
		groundCluesEasyToElite.computeIfAbsent(clueInstance.getLocation(), k -> new ArrayList<>()).add(clueInstance);
	}

	private void removeClue(ClueInstance clue)
	{
		groundCluesBeginnerAndMaster.get(clue.getLocation()).remove(clue);
	}

	private void removeEasyToEliteClue(ClueInstance clueInstance)
	{
		groundCluesEasyToElite.get(clueInstance.getLocation()).remove(clueInstance);
	}

	public void clearEasyToEliteClues()
	{
		groundCluesEasyToElite.clear();
	}

	public Map<WorldPoint, List<ClueInstance>> getAllGroundClues()
	{
		Map<WorldPoint, List<ClueInstance>> allGroundClues = new HashMap<>();
		for (Map.Entry<WorldPoint, List<ClueInstance>> entry : groundCluesEasyToElite.entrySet()) {
			allGroundClues.put(entry.getKey(), new ArrayList<>(entry.getValue()));
		}

		for (Map.Entry<WorldPoint, List<ClueInstance>> entry : groundCluesBeginnerAndMaster.entrySet()) {
			allGroundClues.computeIfAbsent(entry.getKey(), k -> new ArrayList<>())
				.addAll(entry.getValue());
		}

		allGroundClues.forEach(((worldPoint, clueInstances) -> {
			clueInstances.sort(Comparator.comparingInt((clue) -> clue.getTicksToDespawnConsideringTileItem(client.getTickCount())));
		}));

		return allGroundClues;
	}

	public Set<WorldPoint> getTrackedWorldPoints()
	{
		Set<WorldPoint> allWorldPointsTracked = new HashSet<>();
		allWorldPointsTracked.addAll(groundCluesEasyToElite.keySet());
		allWorldPointsTracked.addAll(groundCluesBeginnerAndMaster.keySet());

		return allWorldPointsTracked;
	}

	public void onGameTick()
	{
		currentZone = new Zone(client.getLocalPlayer().getWorldLocation());
		processEmptyTiles();

		for (Tile tile : itemHasSpawnedOnTileThisTick)
		{
			checkClueThroughRelativeDespawnTimers(tile);
		}
		itemHasSpawnedOnTileThisTick.clear();
		removeDespawnedClues();

		lastZone = currentZone;
	}

	private void processEmptyTiles()
	{
		groundCluesBeginnerAndMaster.entrySet().removeIf(entry ->
		{
			Tile tile = getTileAtWorldPoint(entry.getKey());
			if (tile == null) return false;

			Zone clueZone = new Zone(tile.getWorldLocation());
			// Item won't have potentially spawned if too far, so don't remove
			int zonesDistance = clueZone.maxDistanceTo(currentZone);
			if (zonesDistance >= 4) return false;
			return tile.getGroundItems() == null || tile.getGroundItems().isEmpty();
		});
	}

	private void removeDespawnedClues()
	{
		groundCluesBeginnerAndMaster.entrySet().removeIf(entry ->
		{
			List<ClueInstance> cluesList = entry.getValue();
			cluesList.removeIf(clueInstance -> clueInstance.getDespawnTick(client.getTickCount()) <= client.getTickCount());
			return cluesList.isEmpty();
		});
	}

	private void checkClueThroughRelativeDespawnTimers(Tile tile)
	{
		WorldPoint tileWp = tile.getWorldLocation();

		List<TileItem> itemsOnTile = getTrackedItemsAtTile(tile);
		if (itemsOnTile.isEmpty())
		{
			return;
		}

		if (!groundCluesBeginnerAndMaster.containsKey(tileWp))
		{
			groundCluesBeginnerAndMaster.put(tileWp, new ArrayList<>());
		}

		List<ClueInstance> storedClues = groundCluesBeginnerAndMaster.get(tileWp);

		List<ClueInstance> updatedStoredClues = generateNewCluesOnTile(tileWp, storedClues, itemsOnTile);

		if (updatedStoredClues.isEmpty())
		{
			groundCluesBeginnerAndMaster.remove(tileWp);
		}
		else
		{
			// If we didn't find an item for it on the tile, remove it
			updatedStoredClues.removeIf((clue) -> clue.getTileItem() == null);

			// Update the stored clues
			groundCluesBeginnerAndMaster.put(tileWp, updatedStoredClues);
		}
	}

	private List<ClueInstance> generateNewCluesOnTile(WorldPoint tileWp, List<ClueInstance> storedClues, List<TileItem> cluesOnTile)
	{
		int currentTick = client.getTickCount();

		if (storedClues.size() == 1 && cluesOnTile.size() == 1)
		{
			// We assume it is the same clue. It is possible for it to be swapped with another clue though in
			// another client/mobile, and this will be wrong
			if (storedClues.get(0).getDespawnTick(currentTick) >= cluesOnTile.get(0).getDespawnTime())
			{
				storedClues.get(0).setTileItem(cluesOnTile.get(0));
				return storedClues;
			}
		}

		List<ClueInstance> sortedStoredClues = new ArrayList<>(storedClues);
		sortedStoredClues.sort(Comparator.comparingInt((clue) -> clue.getTicksToDespawnConsideringTileItem(currentTick)));

		// If only 1 of either but not both, less certainty as can't use diffs.
		// Could assume things like last clue expired, probs let's just assume nothing
		if (storedClues.size() <= 1 || cluesOnTile.size() == 1)
		{
			List<ClueInstance> actualCluesOnTile = new ArrayList<>();
			// Set tile's clues to just be unknown for all clues on tile
			for (TileItem groundClue : cluesOnTile)
			{
				ClueInstance clueInstance = new ClueInstance(List.of(),
					groundClue.getId(),
					tileWp,
					groundClue,
					client.getTickCount()
				);
				clueInstance.setTileItem(groundClue);
				actualCluesOnTile.add(clueInstance);
			}
			return actualCluesOnTile;
		}

		// Sort ground clues by despawn time ascending
		List<TileItem> sortedGroundClues = new ArrayList<>(cluesOnTile);
		sortedGroundClues.removeIf((tileItem -> tileItem.getDespawnTime() > sortedStoredClues.get(sortedStoredClues.size() - 1).getDespawnTick(currentTick)));
		sortedGroundClues.sort(Comparator.comparingInt(TileItem::getDespawnTime));

		Map<Integer, List<TileItem>> groundItemsByItemID = sortedGroundClues.stream()
			.collect(Collectors.groupingBy(TileItem::getId, LinkedHashMap::new, Collectors.toList()));
		Map<Integer, List<ClueInstance>> sortedItemsByItemID = sortedStoredClues.stream()
			.collect(Collectors.groupingBy(ClueInstance::getItemId, LinkedHashMap::new, Collectors.toList()));

		for (Integer itemID : groundItemsByItemID.keySet())
		{
			if (sortedItemsByItemID.get(itemID) == null) continue;
			findMatchingClues(sortedItemsByItemID.get(itemID), groundItemsByItemID.get(itemID));
		}
		
		List<ClueInstance> foundClues = new ArrayList<>();

		cluesOnTile.stream()
			.map(tileItem -> sortedStoredClues.stream()
				.filter(clue -> clue.getTileItem() == tileItem)
				.findFirst()
				.orElseGet(() ->
				{
					ClueInstance clueInstance = new ClueInstance(List.of(), tileItem.getId(), tileWp, tileItem, client.getTickCount());
					clueInstance.setTileItem(tileItem);
					return clueInstance;
				}))
			.forEach(foundClues::add);

		return foundClues;
	}

	private void findMatchingClues(List<ClueInstance> sortedStoredClues, List<TileItem> sortedGroundClues)
	{
		int currentTick = client.getTickCount();

		// Need to loop diffs, and see matches in each.
		// For items with the same ID, no matter what item you click in a stack, you will always pick up the first item dropped in the stack
		// This means we don't need to worry about considering gaps where a clue has been taken from the middle of a stack.
		int minGroundItemFound = 0;

		if (sortedStoredClues.size() == 1 && sortedGroundClues.size() == 1)
		{
			// We assume it is the same clue. It is possible for it to be swapped with another clue though in
			// another client/mobile, and this will be wrong
			if (sortedStoredClues.get(0).getDespawnTick(currentTick) >= sortedGroundClues.get(0).getDespawnTime())
			{
				sortedStoredClues.get(0).setTileItem(sortedGroundClues.get(0));
				return;
			}
		}

		for (int i = 0; i < sortedStoredClues.size() - 1; i++)
		{
			ClueInstance clueInstance1 = sortedStoredClues.get(i);
			ClueInstance clueInstance2 = sortedStoredClues.get(i + 1);

			TileItem groundClue1 = sortedGroundClues.get(minGroundItemFound);
			TileItem groundClue2 = sortedGroundClues.get(minGroundItemFound + 1);

			int currentStoredClueDiff = clueInstance2.getTimeToDespawnFromDataInTicks() - clueInstance1.getTimeToDespawnFromDataInTicks();
			int currentGroundClueDiff = groundClue2.getDespawnTime() - groundClue1.getDespawnTime();

			// Same diff, probs same thing
			if (currentGroundClueDiff != currentStoredClueDiff) continue;
			// If item will despawn later than the stored clue, it can't be it.
			if (groundClue1.getDespawnTime() > clueInstance1.getDespawnTick(currentTick)) continue;
			if (groundClue2.getDespawnTime() > clueInstance2.getDespawnTick(currentTick)) continue;
			clueInstance1.setTileItem(groundClue1);
			clueInstance2.setTileItem(groundClue2);
			minGroundItemFound++;
		}
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
			.filter(item -> Clues.isClue(item.getId(), clueDetailsPlugin.isDeveloperMode()))
			.collect(Collectors.toList());
	}

	class ClueInstanceComparator implements Comparator<ClueInstance>
	{
		@Override
		public int compare(ClueInstance o1, ClueInstance o2)
		{
			// Primary comparison: Compare by despawn time
			int despawnComparison = Integer.compare(o1.getTicksToDespawnConsideringTileItem(client.getTickCount()),
				o2.getTicksToDespawnConsideringTileItem(client.getTickCount()));
			if (despawnComparison != 0)
			{
				return despawnComparison;
			}
			else
			{
				// Secondary comparison: If despawn times are the same, compare by itemId
				return Integer.compare(o1.getItemId(), o2.getItemId());
			}
		}
	}

	public TreeMap<ClueInstance, Integer> getClueInstancesWithQuantityAtWp(ClueDetailsConfig config, WorldPoint wp, int currentTick)
	{
		if (getAllGroundClues().get(wp).isEmpty()) return null;

		List<ClueInstance> groundItemList = getAllGroundClues().get(wp);
		Map<ClueInstance, Integer> groundItemMap = new HashMap<>();

		if (config.collapseGroundCluesByTier())
		{
			groundItemMap = keepOldestTierClues(groundItemList, currentTick);
		}
		else if (config.collapseGroundClues())
		{
			groundItemMap = keepOldestUniqueClues(groundItemList, currentTick);
		}
		else
		{
			for (ClueInstance item : groundItemList)
			{
				groundItemMap.put(item, 1);
			}
		}

		// Sort ClueInstances by despawn time
		ClueInstanceComparator clueInstanceComparator = new ClueInstanceComparator();
		TreeMap<ClueInstance, Integer> clueInstancesWithQuantityAtWp = new TreeMap<>(clueInstanceComparator);
		clueInstancesWithQuantityAtWp.putAll(groundItemMap);
		return clueInstancesWithQuantityAtWp;
	}

	// Remove duplicate step clues, maintaining a count of the original amount of each
	public static Map<ClueInstance, Integer> keepOldestUniqueClues(List<ClueInstance> items, int currentTick)
	{
		Map<List<Integer>, ClueInstance> lowestValueItems = new HashMap<>();
		Map<List<Integer>, Integer> uniqueCount = new HashMap<>();

		for (ClueInstance item : items)
		{
			List<Integer> clueIds = item.getClueIds();

			if (!lowestValueItems.containsKey(clueIds)
				|| item.getDespawnTick(currentTick) < lowestValueItems.get(clueIds).getDespawnTick(currentTick))
			{
				lowestValueItems.put(clueIds, item);
				uniqueCount.put(clueIds, 1);
			}
			else
			{
				uniqueCount.put(clueIds, uniqueCount.get(clueIds) + 1);
			}
		}

		return lowestValueItems.values().stream()
			.collect(Collectors.toMap(item -> item, item -> uniqueCount.get(item.getClueIds())));
	}

	// Remove duplicate tier clues, maintaining a count of the original amount of each
	public static Map<ClueInstance, Integer> keepOldestTierClues(List<ClueInstance> items, int currentTick)
	{
		Map<ClueTier, ClueInstance> lowestValueItems = new HashMap<>();
		Map<ClueTier, Integer> uniqueCount = new HashMap<>();

		for (ClueInstance item : items)
		{
			ClueTier tier = item.getTier();

			if (!lowestValueItems.containsKey(tier)
				|| item.getDespawnTick(currentTick) < lowestValueItems.get(tier).getDespawnTick(currentTick))
			{
				lowestValueItems.put(tier, item);
				uniqueCount.put(tier, 1);
			}
			else
			{
				uniqueCount.put(tier, uniqueCount.get(tier) + 1);
			}
		}

		return lowestValueItems.values().stream()
			.collect(Collectors.toMap(item -> item, item ->	uniqueCount.get(item.getTier())));
	}

	public void saveStateToConfig()
	{
		clueGroundSaveDataManager.saveStateToConfig(client, groundCluesBeginnerAndMaster);
	}

	public void loadStateFromConfig()
	{
		groundCluesBeginnerAndMaster.clear();
		groundCluesBeginnerAndMaster.putAll(clueGroundSaveDataManager.loadStateFromConfig(client));
	}
}
