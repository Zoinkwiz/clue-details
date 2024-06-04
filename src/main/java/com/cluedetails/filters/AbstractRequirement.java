package com.cluedetails.filters;

import java.util.List;
import net.runelite.api.Client;
import net.runelite.api.coords.WorldPoint;

public abstract class AbstractRequirement
{
	List<WorldPoint> wps;

	List<ClueRegion> regions;

	abstract public boolean isRegionValid(ClueRegion clueRegionToCheck);
}
