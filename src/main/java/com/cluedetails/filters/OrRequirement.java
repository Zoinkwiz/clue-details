package com.cluedetails.filters;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import net.runelite.api.Client;
import net.runelite.api.coords.WorldPoint;

public class OrRequirement extends AbstractRequirement
{
	public OrRequirement(WorldPoint... wps)
	{
		this.wps = List.of(wps);
		this.regions = findRegions();
	}

	public OrRequirement(List<WorldPoint> wps)
	{
		this.wps = wps;
		this.regions = findRegions();
	}

	@Override
	public boolean isRegionValid(ClueRegion clueRegionToCheck)
	{
		return regions.contains(clueRegionToCheck);
	}

	private List<ClueRegion> findRegions()
	{
		return wps.stream()
			.flatMap(wp ->
				Arrays.stream(ClueRegion.values())
					.filter(region ->
						Arrays.stream(region.getZones())
							.anyMatch(zone -> zone.contains(wp))
					)
			)
			.distinct()
			.collect(Collectors.toList());
	}
}

