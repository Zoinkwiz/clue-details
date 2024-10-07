package com.cluedetails;

import lombok.Data;
import net.runelite.api.coords.WorldPoint;

@Data
public class ClueInstanceData
{
    private int clueId;
    private int itemId;
    private int despawnTick;
    private int x;
    private int y;
    private int plane;

    public ClueInstanceData(ClueInstance clue)
    {
        this.clueId = clue.getClueId();
        this.itemId = clue.getItemId();
        this.despawnTick = clue.getDespawnTick();
        this.x = clue.getLocation().getX();
        this.y = clue.getLocation().getY();
        this.plane = clue.getLocation().getPlane();
    }

    public WorldPoint getLocation()
    {
        return new WorldPoint(x, y, plane);
    }
}

