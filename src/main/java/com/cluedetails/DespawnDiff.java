package com.cluedetails;

import lombok.Getter;
import net.runelite.api.TileItem;

@Getter
public class DespawnDiff
{
	private final ClueInstance clue1;
	private final ClueInstance clue2;
	private final TileItem tileItem1;
	private final TileItem tileItem2;
	private final int despawnDiff;

	public DespawnDiff(ClueInstance clue1, ClueInstance clue2)
	{
		this.clue1 = clue1;
		this.clue2 = clue2;
		this.tileItem1 = null;
		this.tileItem2 = null;
		this.despawnDiff = clue2.getDespawnTick() - clue1.getDespawnTick();
	}

	public DespawnDiff(TileItem tileItem1, TileItem tileItem2)
	{
		this.clue1 = null;
		this.clue2 = null;
		this.tileItem1 = tileItem1;
		this.tileItem2 = tileItem2;
		this.despawnDiff = tileItem1.getDespawnTime() - tileItem2.getDespawnTime();
	}

	public int getDespawn1()
	{
		if (clue1 != null)
		{
			return clue1.getDespawnTick();
		}
		return tileItem1.getDespawnTime();
	}

	public int getDespawn2()
	{
		if (clue2 != null)
		{
			return clue2.getDespawnTick();
		}
		return tileItem2.getDespawnTime();
	}
}
