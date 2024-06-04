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
	public static Comparator<Clues> sortByTier()
	{
		return Comparator.comparing(q -> tierOrder.indexOf(q));
	}
}
