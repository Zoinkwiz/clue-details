package com.cluedetails;

import lombok.Data;
import net.runelite.api.Client;
import net.runelite.api.TileItem;
import net.runelite.api.coords.WorldPoint;

@Data
public class ClueInstance
{
    private int clueId; // Fake ID from ClueText
    private String clueText;
    private final int itemId; // Clue item ID
    private final WorldPoint location; // Null if in inventory
    private int despawnTick = -1; // Use -1 if not applicable
	private TileItem tileItem;

    // Constructor for clues from config
    public ClueInstance(ClueInstanceData data, int currentTick)
    {
        this.clueId = data.getClueId();
        this.itemId = data.getItemId();
        this.location = data.getLocation();
		// if had on then turned off in same session, we don't know what happened in meantime.
		// Ticks go forward even when logged into other game modes. For simplicity we assume when
		// Loaded we just are starting from the exact same despawn time remaining.
		this.despawnTick = currentTick + data.getDespawnTick();
		setInitialClueText();
    }

	// Constructor for inventory clues from inventory changed event
	public ClueInstance(int clueId, int itemId)
	{
		this.clueId = clueId;
		this.itemId = itemId;
		this.location = null;
		setInitialClueText();
	}

    // Constructor for ground clues
    public ClueInstance(int clueId, int itemId, WorldPoint location, TileItem tileItem)
    {
        this.clueId = clueId;
        this.itemId = itemId;
        this.location = location;
        this.tileItem = tileItem;
		setInitialClueText();
    }

	private void setInitialClueText()
	{
		if (clueId == -1)
		{
			this.clueText = "?";
		}
		else
		{
			ClueText clueDetails = ClueText.getById(clueId);
			this.clueText = clueDetails == null ? "error" : clueDetails.getText();
		}
	}

    public void adjustDespawnTick(int ticksPassed)
    {
        despawnTick += ticksPassed;
    }

    public int getRelativeDespawnTime(Client client)
    {
        return despawnTick - client.getTickCount();
    }

    public void setClueId(int clueId)
    {
        this.clueId = clueId;
        ClueText clueDetails = ClueText.getById(clueId);
        this.clueText = clueDetails == null ? null : clueDetails.getText();
    }
}
