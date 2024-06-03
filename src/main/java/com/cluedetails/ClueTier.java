package com.cluedetails;

import java.util.Arrays;

public enum ClueTier
{
	ALL,
	EASY,
	MEDIUM,
	HARD,
	ELITE;

	public static ClueTier[] displayFilters()
	{
		return Arrays.stream(ClueTier.values()).filter((clueTier -> clueTier.shouldDisplay)).toArray(QuestFilter[]::new);
	}
}
