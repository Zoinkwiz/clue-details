package com.cluedetails;

import lombok.Getter;
import net.runelite.api.TileItem;
import net.runelite.api.coords.WorldPoint;

@Getter
public class PendingGroundClue
{
    private final TileItem item;
    private final WorldPoint location;
	private final int spawnTick;

    public PendingGroundClue(TileItem item, WorldPoint location, int spawnTick)
	{
		this.item = item;
		this.location = location;
		this.spawnTick = spawnTick;
	}
}