package com.cluedetails;

import net.runelite.api.TileItem;
import net.runelite.api.coords.WorldPoint;

public class PendingGroundClue
{
    private final TileItem item;
    private final WorldPoint location;
    private final int despawnTick;

    public PendingGroundClue(TileItem item, WorldPoint location, int despawnTick)
    {
        this.item = item;
        this.location = location;
        this.despawnTick = despawnTick;
    }

    public TileItem getItem()
    {
        return item;
    }

    public WorldPoint getLocation()
    {
        return location;
    }

    public int getDespawnTick()
    {
        return despawnTick;
    }
}