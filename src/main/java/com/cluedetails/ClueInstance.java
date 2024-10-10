package com.cluedetails;

import java.util.List;
import lombok.Data;
import net.runelite.api.TileItem;
import net.runelite.api.coords.WorldPoint;

@Data
public class ClueInstance
{
    private List<Integer> clueIds; // Fake ID from ClueText
    private final int itemId; // Clue item ID
    private final WorldPoint location; // Null if in inventory
    private int despawnTick = -1; // Use -1 if not applicable
	private TileItem tileItem;

    // Constructor for clues from config
    public ClueInstance(ClueInstanceData data, int currentTick)
    {
        this.clueIds = data.getClueIds();
        this.itemId = data.getItemId();
        this.location = data.getLocation();
		// if had on then turned off in same session, we don't know what happened in meantime.
		// Ticks go forward even when logged into other game modes. For simplicity we assume when
		// Loaded we just are starting from the exact same despawn time remaining.
		this.despawnTick = currentTick + data.getDespawnTick();
    }

	// Constructor for inventory clues from inventory changed event
	public ClueInstance(List<Integer> clueIds, int itemId)
	{
		this.clueIds = clueIds;
		this.itemId = itemId;
		this.location = null;
	}

    // Constructor for ground clues
    public ClueInstance(List<Integer> clueIds, int itemId, WorldPoint location, TileItem tileItem)
    {
        this.clueIds = clueIds;
        this.itemId = itemId;
        this.location = location;
        this.tileItem = tileItem;
		this.despawnTick = tileItem.getDespawnTime();
    }

	public int getDespawnTick()
	{
		if (tileItem != null)
		{
			return tileItem.getDespawnTime();
		}
		return despawnTick;
	}

    public void setClueIds(List<Integer> clueIds)
    {
        this.clueIds = clueIds;
    }
}
