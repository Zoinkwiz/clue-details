/*
 * Copyright (c) 2024, Zoinkwiz <https://github.com/Zoinkwiz>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.cluedetails;

import com.cluedetails.filters.ClueOrders;
import com.cluedetails.filters.ClueRegion;
import com.cluedetails.filters.ClueTier;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import lombok.Getter;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Notification;
import net.runelite.client.util.Text;

@ConfigGroup("clue-details")
public interface ClueDetailsConfig extends Config
{
	enum ClueOrdering implements Comparator<Clues>
	{
		/**
		 * Sort clues in alphabetical order
		 */
		TIER(ClueOrders.sortByTier(), ClueTierFilter.EASY, ClueTierFilter.MEDIUM, ClueTierFilter.HARD, ClueTierFilter.ELITE),
		REGION(ClueOrders.sortByRegion(), ClueRegionFilter.MISTHALIN, ClueRegionFilter.ASGARNIA, ClueRegionFilter.KARAMJA, ClueRegionFilter.KANDARIN, ClueRegionFilter.FREMENNIK_PROVINCE, ClueRegionFilter.KHARIDIAN_DESERT,
			ClueRegionFilter.MORYTANIA, ClueRegionFilter.TIRANNWN, ClueRegionFilter.WILDERNESS, ClueRegionFilter.KOUREND, ClueRegionFilter.VARLAMORE);

		private final Comparator<Clues> comparator;
		@Getter
		private final ClueFilter[] sections;

		ClueOrdering(Comparator<Clues> comparator, ClueFilter... sections)
		{
			this.comparator = comparator;
			this.sections = sections;
		}

		public List<Clues> sort(Collection<Clues> list)
		{
			return list.stream().sorted(this).collect(Collectors.toList());
		}

		@Override
		public int compare(Clues o1, Clues o2)
		{
			return comparator.compare(o1, o2);
		}
	}

	interface ClueFilter extends Predicate<Clues>
	{
		String getDisplayName();
	}

	class BaseClueFilter
	{
		private final String displayName;

		public BaseClueFilter(String displayName)
		{
			this.displayName = displayName;
		}

		public String getDisplayName()
		{
			return displayName;
		}
	}

	enum ClueTierFilter implements ClueFilter
	{
		SHOW_ALL(c -> true, "Show All"),
		EASY(c -> c.getClueTier() == ClueTier.EASY, "Easy"),
		MEDIUM(c -> c.getClueTier() == ClueTier.MEDIUM, "Medium"),
		HARD(c -> c.getClueTier() == ClueTier.HARD, "Hard"),
		ELITE(c -> c.getClueTier() == ClueTier.ELITE, "Elite");

		private final Predicate<Clues> predicate;
		private final BaseClueFilter baseClueFilter;

		ClueTierFilter(Predicate<Clues> predicate, String displayName)
		{
			this.predicate = predicate;
			this.baseClueFilter = new BaseClueFilter(displayName);
		}

		@Override
		public boolean test(Clues clue)
		{
			return predicate.test(clue);
		}

		public List<Clues> test(Collection<Clues> helpers)
		{
			return helpers.stream().filter(this).collect(Collectors.toList());
		}

		public static ClueTierFilter[] displayFilters()
		{
			return ClueTierFilter.values();
		}

		@Override
		public String getDisplayName()
		{
			return baseClueFilter.getDisplayName();
		}
	}

	enum ClueRegionFilter implements ClueFilter
	{
		SHOW_ALL(c -> true, "Show All"),
		MISTHALIN(c -> c.getRegions().isRegionValid(ClueRegion.MISTHALIN), "Misthalin"),
		KARAMJA(c -> c.getRegions().isRegionValid(ClueRegion.KARAMJA), "Karamja"),
		ASGARNIA(c -> c.getRegions().isRegionValid(ClueRegion.ASGARNIA), "Asgarnia"),
		FREMENNIK_PROVINCE(c -> c.getRegions().isRegionValid(ClueRegion.FREMENNIK_PROVINCE), "Fremennik province"),
		KANDARIN(c -> c.getRegions().isRegionValid(ClueRegion.KANDARIN), "Kandarin"),
		KHARIDIAN_DESERT(c -> c.getRegions().isRegionValid(ClueRegion.KHARIDIAN_DESERT), "Kharidian desert"),
		MORYTANIA(c -> c.getRegions().isRegionValid(ClueRegion.MORYTANIA), "Morytania"),
		TIRANNWN(c -> c.getRegions().isRegionValid(ClueRegion.TIRANNWN), "Tirannwn"),
		WILDERNESS(c -> c.getRegions().isRegionValid(ClueRegion.WILDERNESS), "Wilderness"),
		KOUREND(c -> c.getRegions().isRegionValid(ClueRegion.KOUREND), "Kourend"),
		VARLAMORE(c -> c.getRegions().isRegionValid(ClueRegion.VARLAMORE), "Varlamore"),
		;

		private final Predicate<Clues> predicate;
		private final BaseClueFilter baseClueFilter;

		ClueRegionFilter(Predicate<Clues> predicate, String displayName)
		{
			this.predicate = predicate;
			this.baseClueFilter = new BaseClueFilter(displayName);
		}

		@Override
		public boolean test(Clues clue)
		{
			return predicate.test(clue);
		}

		public List<Clues> test(Collection<Clues> helpers)
		{
			return helpers.stream().filter(this).collect(Collectors.toList());
		}

		public static ClueRegionFilter[] displayFilters()
		{
			return ClueRegionFilter.values();
		}

		@Override
		public String getDisplayName()
		{
			return baseClueFilter.getDisplayName();
		}
	}

	@ConfigItem(
		keyName = "showSidebar",
		name = "Show highlighting sidebar",
		description = "Customise clues to be highlighted in a sidebar"
	)
	default boolean showSidebar()
	{
		return true;
	}

	@ConfigItem(
		keyName = "filterListByTier",
		name = "Filter by tier",
		description = "Configures what tier of clue to show",
		position = 1
	)
	default ClueTierFilter filterListByTier()
	{
		return ClueTierFilter.SHOW_ALL;
	}

	@ConfigItem(
		keyName = "filterListByRegion",
		name = "Filter by region",
		description = "Configures what clues to show based on region they fall in",
		position = 2
	)
	default ClueRegionFilter filterListByRegion()
	{
		return ClueRegionFilter.SHOW_ALL;
	}

	@ConfigItem(
		keyName = "orderListBy",
		name = "Clue sidebar order",
		description = "Configures which way to order the clue list",
		position = 3
	)
	default ClueOrdering orderListBy()
	{
		return ClueOrdering.TIER;
	}

	@ConfigItem(
		keyName = "markedClueDroppedNotification",
		name = "Notify when a marked clue drops",
		description = "Send a notification when a marked clue drops",
		position = 4
	)
	default Notification markedClueDroppedNotification()
	{
		return Notification.ON;
	}

	@ConfigItem(
		keyName = "onlyShowMarkedClues",
		name = "Only show marked clues in the sidebar",
		description = "Toggle whether to only show marked clues in the sidebar",
		position = 6
	)
	default boolean onlyShowMarkedClues()
	{
		return false;
	}

	@ConfigItem(
		keyName = "highlightMarkedClues",
		name = "Highlight marked clues",
		description = "Toggle whether to highlight marked clues",
		position = 5
	)
	default boolean highlightMarkedClues()
	{
		return true;
	}

	@ConfigItem(
		keyName = "changeClueText",
		name = "Change clue item text",
		description = "Toggle whether to make the clue item text be the hint or the normal text",
		position = 5
	)
	default boolean changeClueText()
	{
		return false;
	}

	@ConfigItem(
		keyName = "showInventoryCluesOverlay",
		name = "Show clues overlay",
		description = "Toggle whether to show an overlay with details on all clues in your inventory",
		position = 5
	)
	default boolean showInventoryCluesOverlay()
	{
		return false;
	}

	@ConfigItem(
			keyName = "highlightFeather",
			name = "Highlighted feathering",
			description = "Configure the feathering of highlighted clues",
			position = 5
	)
	default int highlightFeather()
	{
		return 10;
	}

	@ConfigItem(
			keyName = "outlineWidth",
			name = "Highlighted outline width",
			description = "Configure the outline width of highlighted clues",
			position = 6
	)
	default int outlineWidth()
	{
		return 4;
	}
}
