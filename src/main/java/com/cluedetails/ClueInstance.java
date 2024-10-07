package com.cluedetails;

import lombok.Data;
import net.runelite.api.Client;
import net.runelite.api.coords.WorldPoint;

@Data
public class ClueInstance
{
    private int clueId; // Fake ID from ClueText
    private String clueText;
    private final int itemId; // Clue item ID
    private final WorldPoint location; // Null if in inventory
    private int despawnTick; // Use -1 if not applicable

    // Constructor for inventory clues
    public ClueInstance(ClueInstanceData data)
    {
        ClueText clueDetails = ClueText.getById(clueId);
        System.out.println("FROM DATA");
        System.out.println(data.getItemId());
        this.clueId = data.getClueId();
        this.clueText = clueDetails == null ? "error" : clueDetails.getText();
        this.itemId = data.getItemId();
        this.location = data.getLocation();
        this.despawnTick = -1;
    }

    // Constructor for ground clues
    public ClueInstance(int clueId, int itemId, WorldPoint location, int despawnTick)
    {
        ClueText clueDetails = ClueText.getById(clueId);
        this.clueId = clueId;
        this.clueText = clueDetails == null ? null : clueDetails.getText();
        this.itemId = itemId; // Not applicable for ground clues
        this.location = location;
        this.despawnTick = despawnTick;
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
