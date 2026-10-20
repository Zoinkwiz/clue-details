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
package com.cluedetails;

import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Collectors;
import net.runelite.api.Client;
import net.runelite.api.ItemID;
import net.runelite.api.TileItem;
import net.runelite.api.coords.WorldPoint;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

// Despawn ticks are taken from real logs. Reads during a game tick match what the ground reports after a reload, except
// for a freshly dropped item, which reads 1 higher (and 1 higher again at spawn) until it is reloaded from the scene
public class ClueGroundMatchingTest
{
	private static final WorldPoint TILE = new WorldPoint(3200, 3200, 0);

	private final Client client = mock(Client.class);
	private ClueGroundManager ground;

	@Before
	public void setUp()
	{
		Clues.setConfig(null);
		Clues.rebuildFilteredCluesCache();
		ground = new ClueGroundManager(client, null, mock(ClueDetailsPlugin.class));
	}

	@Test
	public void hopKeepsFreshDropInStack()
	{
		ClueInstance older = settled(1, 6696);
		ClueInstance fresh = freshDrop(2, 8204);
		older.detachTileItem();
		fresh.detachTileItem();

		List<ClueInstance> result = match(List.of(older, fresh), item(6695), item(8201));

		assertEquals(List.of(List.of(1), List.of(2)), ids(result));
		assertEquals(List.of(6695, 8201), ticks(result));
	}

	@Test
	public void relogKeepsFreshDropInStack()
	{
		ClueInstance older = settled(1, 5843);
		ClueInstance fresh = freshDrop(2, 6080);
		older.detachTileItem();
		fresh.detachTileItem();

		List<ClueInstance> result = match(List.of(older, fresh), item(5839), item(6074));

		assertEquals(List.of(List.of(1), List.of(2)), ids(result));
	}

	@Test
	public void singleClueMatchKeepsStackConsistentWhenAnotherIsDropped()
	{
		ClueInstance single = loaded(1, 6015);
		List<ClueInstance> afterRelog = match(List.of(single), item(6011));
		assertEquals(6011, afterRelog.get(0).getDespawnTick());

		ClueInstance fresh = freshDrop(2, 6164);
		List<ClueInstance> afterTeleport = match(List.of(single, fresh), item(6010), item(6161));

		assertEquals(List.of(List.of(1), List.of(2)), ids(afterTeleport));
	}

	@Test
	public void rejectsGapThatDoesNotMatch()
	{
		ClueInstance first = loaded(1, 1000);
		ClueInstance second = loaded(2, 1003);

		List<ClueInstance> result = match(List.of(first, second), item(999), item(1003));

		assertEquals(List.of(List.of(), List.of()), ids(result));
	}

	@Test
	public void detachedClueKeepsDespawnTickWhenGameTickCounterResets()
	{
		TileItem tileItem = item(8204);
		ClueInstance fresh = new ClueInstance(List.of(2), ItemID.CLUE_SCROLL_MASTER, TILE, tileItem, true);
		when(tileItem.getDespawnTime()).thenReturn(8203);
		fresh.refreshDespawnTick();
		fresh.detachTileItem();

		when(tileItem.getDespawnTime()).thenReturn(8203 + 10000);
		fresh.refreshDespawnTick();

		assertEquals(8202, fresh.getDespawnTick());
	}

	private ClueInstance settled(int clueId, int despawnTick)
	{
		return new ClueInstance(List.of(clueId), ItemID.CLUE_SCROLL_MASTER, TILE, item(despawnTick), 0);
	}

	// spawnDespawnTick is read when the item spawns, one tick ahead of reads during the game tick
	private ClueInstance freshDrop(int clueId, int spawnDespawnTick)
	{
		TileItem tileItem = item(spawnDespawnTick);
		ClueInstance clue = new ClueInstance(List.of(clueId), ItemID.CLUE_SCROLL_MASTER, TILE, tileItem, true);
		when(tileItem.getDespawnTime()).thenReturn(spawnDespawnTick - 1);
		clue.refreshDespawnTick();
		return clue;
	}

	private ClueInstance loaded(int clueId, int despawnTick)
	{
		ClueInstanceData data = new ClueInstanceData(new ClueInstance(List.of(clueId), ItemID.CLUE_SCROLL_MASTER));
		data.setX(TILE.getX());
		data.setY(TILE.getY());
		data.setDespawnTick(despawnTick);
		return new ClueInstance(data);
	}

	private TileItem item(int despawnTick)
	{
		TileItem tileItem = mock(TileItem.class);
		when(tileItem.getId()).thenReturn(ItemID.CLUE_SCROLL_MASTER);
		when(tileItem.getDespawnTime()).thenReturn(despawnTick);
		return tileItem;
	}

	@SuppressWarnings("unchecked")
	private List<ClueInstance> match(List<ClueInstance> stored, TileItem... onGround)
	{
		try
		{
			Method method = ClueGroundManager.class.getDeclaredMethod("generateNewCluesOnTile", WorldPoint.class, List.class, List.class);
			method.setAccessible(true);
			return (List<ClueInstance>) method.invoke(ground, TILE, stored, List.of(onGround));
		}
		catch (ReflectiveOperationException e)
		{
			throw new RuntimeException(e);
		}
	}

	private static List<List<Integer>> ids(List<ClueInstance> clues)
	{
		return clues.stream().map(ClueInstance::getClueIds).collect(Collectors.toList());
	}

	private static List<Integer> ticks(List<ClueInstance> clues)
	{
		return clues.stream().map(ClueInstance::getDespawnTick).collect(Collectors.toList());
	}
}
