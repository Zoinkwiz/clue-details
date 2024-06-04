package com.cluedetails.filters;

import com.cluedetails.Clues;
import java.util.Comparator;
import java.util.List;

public class ClueOrders
{
	static List<ClueTier> tierOrder = List.of(
		ClueTier.EASY,
		ClueTier.MEDIUM,
		ClueTier.HARD,
		ClueTier.ELITE
	);

	static List<ClueRegion> regionOrder = List.of(
		ClueRegion.MISTHALIN, ClueRegion.ASGARNIA, ClueRegion.KARAMJA, ClueRegion.KANDARIN, ClueRegion.FREMENNIK_PROVINCE, ClueRegion.KHARIDIAN_DESERT,
		ClueRegion.MORYTANIA, ClueRegion.TIRANNWN, ClueRegion.WILDERNESS, ClueRegion.KOUREND, ClueRegion.VARLAMORE
	);

	public static Comparator<Clues> sortByTier()
	{
		return Comparator.comparing(q -> tierOrder.indexOf(q));
	}

	public static Comparator<Clues> sortByRegion()
	{
		return Comparator.comparing(q -> tierOrder.indexOf(q));
	}
}
