/*
 * Copyright (c) 2025, Zoinkwiz <https://github.com/Zoinkwiz>
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import net.runelite.api.Client;
import net.runelite.api.Tile;
import net.runelite.api.WorldView;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;

public class WorldPointToClueInstances
{
	private final Client client;
	private final ClueDetailsPlugin clueDetailsPlugin;
	private final Comparator<ClueInstance> clueComparator;

	private final Map<WorldPoint, SortedSet<ClueInstance>> cluesByWorldPoint = new HashMap<>();
	private final Map<WorldPoint, List<ClueInstance>> groundCluesBeginnerAndMaster = new HashMap<>();
	private final Map<WorldPoint, List<ClueInstance>> groundCluesEasyToElite = new HashMap<>();

	public WorldPointToClueInstances(Client client, ClueDetailsPlugin clueDetailsPlugin)
	{
		this.client = client;
		this.clueDetailsPlugin = clueDetailsPlugin;

		clueComparator = Comparator
			.comparingLong(ClueInstance::getSequenceNumber)
			.thenComparingInt(ClueInstance::getDespawnTick);
	}

	private void createGroupedSet(WorldPoint wp)
	{
		if (cluesByWorldPoint.get(wp) != null)
		{
			cluesByWorldPoint.get(wp).clear();
		}

		if (groundCluesBeginnerAndMaster.get(wp) != null)
		{
			for (ClueInstance clueInstance : groundCluesBeginnerAndMaster.get(wp))
			{
				cluesByWorldPoint.computeIfAbsent(wp, k -> new TreeSet<>(clueComparator)).add(clueInstance);
			}
		}

		if (groundCluesEasyToElite.get(wp) != null)
		{
			for (ClueInstance clueInstance : groundCluesEasyToElite.get(wp))
			{
				cluesByWorldPoint.computeIfAbsent(wp, k -> new TreeSet<>(clueComparator)).add(clueInstance);
			}
		}

		if (cluesByWorldPoint.get(wp) != null && cluesByWorldPoint.get(wp).isEmpty())
		{
			cluesByWorldPoint.remove(wp);
		}
	}

	public List<ClueInstance> getAllClues()
	{
		SortedSet<ClueInstance> allClues = new TreeSet<>(clueComparator);

		cluesByWorldPoint.values().forEach(allClues::addAll);
		return new ArrayList<>(allClues);
	}

	public SortedSet<ClueInstance> getAllCluesAtWorldPoint(WorldPoint wp)
	{
		return cluesByWorldPoint.getOrDefault(wp, Collections.emptySortedSet());
	}

	public List<ClueInstance> getBeginnerAndMasterCluesAtWorldPoint(WorldPoint wp)
	{
		return new ArrayList<>(groundCluesBeginnerAndMaster.getOrDefault(wp, new ArrayList<>()));
	}

	public void addClue(ClueInstance clueInstance)
	{
		if (Clues.isBeginnerOrMasterClue(clueInstance.getItemId(), clueDetailsPlugin.isDeveloperMode()))
		{
			addBeginnerMasterClue(clueInstance);
		}
		else if (Clues.isClue(clueInstance.getItemId(), clueDetailsPlugin.isDeveloperMode()))
		{
			addEasyToEliteClue(clueInstance);
		}

		createGroupedSet(clueInstance.getLocation());
	}

	public void removeClue(ClueInstance clueInstance)
	{
		if (Clues.isBeginnerOrMasterClue(clueInstance.getItemId(), clueDetailsPlugin.isDeveloperMode()))
		{
			removeBeginnerMasterClue(clueInstance);
		}
		else if (Clues.isClue(clueInstance.getItemId(), clueDetailsPlugin.isDeveloperMode()))
		{
			removeEasyToEliteClue(clueInstance);
		}

		createGroupedSet(clueInstance.getLocation());
	}

	private void addBeginnerMasterClue(ClueInstance clue)
	{
		groundCluesBeginnerAndMaster.computeIfAbsent(clue.getLocation(), k -> new ArrayList<>()).add(clue);
	}

	private void addEasyToEliteClue(ClueInstance clueInstance)
	{
		groundCluesEasyToElite.computeIfAbsent(clueInstance.getLocation(), k -> new ArrayList<>()).add(clueInstance);
	}

	private void removeBeginnerMasterClue(ClueInstance clueInstance)
	{
		List<ClueInstance> list = groundCluesBeginnerAndMaster.get(clueInstance.getLocation());
		if (list != null)
		{
			list.remove(clueInstance);
			if (list.isEmpty())
			{
				groundCluesBeginnerAndMaster.remove(clueInstance.getLocation());
			}
		}
	}

	private void removeEasyToEliteClue(ClueInstance clueInstance)
	{
		List<ClueInstance> list = groundCluesEasyToElite.get(clueInstance.getLocation());
		if (list != null)
		{
			list.remove(clueInstance);

			if (groundCluesEasyToElite.get(clueInstance.getLocation()) != null && groundCluesEasyToElite.get(clueInstance.getLocation()).isEmpty())
			{
				groundCluesEasyToElite.remove(clueInstance.getLocation());
			}
		}
	}

	public void clearEasyToEliteCluesAtWorldPoint(WorldPoint wp)
	{
		List<ClueInstance> clues = groundCluesEasyToElite.get(wp);
		if (clues == null) return;
		new ArrayList<>(clues).forEach(this::removeClue);
	}

	public void clearBeginnerAndMasterCluesAtWorldPoint(WorldPoint wp)
	{
		List<ClueInstance> clues = groundCluesBeginnerAndMaster.get(wp);
		if (clues == null) return;
		new ArrayList<>(clues).forEach(this::removeClue);
	}

	public void clearEmptyTiles(Zone currentZone)
	{
		for (ClueInstance clueInstance : getAllClues())
		{
			Tile tile = getTileAtWorldPoint(clueInstance.getLocation());
			if (tile == null) continue;

			Zone clueZone = new Zone(tile.getWorldLocation());
			// Item won't have potentially spawned if too far, so don't remove
			int zonesDistance = clueZone.maxDistanceTo(currentZone);
			if (zonesDistance >= 4) continue;
			if (tile.getGroundItems() == null || tile.getGroundItems().isEmpty())
			{
				removeClue(clueInstance);
			}
		}
	}

	public void clearAllClues()
	{
		groundCluesEasyToElite.clear();
		groundCluesBeginnerAndMaster.clear();
		cluesByWorldPoint.clear();
	}

	public void removeDespawnedClues()
	{
		for (ClueInstance clueInstance : getAllClues())
		{
			if (clueInstance.getDespawnTick() <= client.getTickCount())
			{
				removeClue(clueInstance);
			}
		}
	}

	public Set<WorldPoint> getAllTrackedWorldPoints()
	{
		return cluesByWorldPoint.keySet();
	}

	public Tile getTileAtWorldPoint(WorldPoint tileWp)
	{
		WorldView worldView = client.getTopLevelWorldView();
		LocalPoint tileLp = LocalPoint.fromWorld(worldView, tileWp);
		if (tileLp == null)
		{
			return null;
		}
		return worldView.getScene().getTiles()[tileWp.getPlane()][tileLp.getSceneX()][tileLp.getSceneY()];
	}
}
