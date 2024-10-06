package com.cluedetails;

import lombok.AllArgsConstructor;
import lombok.Data;
import net.runelite.api.coords.WorldPoint;


@Data
@AllArgsConstructor
public class FloorClue
{
    Integer clueID;
    /* TODO: Instead make use of relative difference to other clues on tile.
        We can use this to determine order placements and such. Initial clue on tile is 0,
        Next is then diff between despawn times. Eventual negative values as despawns occur, and
        Should shift once a clue is picked up. Should be more robust to go by exact tick diff than
        by approx order
    */
    Integer despawnTick;
    WorldPoint worldPoint;

    public Integer getTicksUntilDespawnForSave(int currentTick)
    {
        return despawnTick - currentTick;
    }
}
