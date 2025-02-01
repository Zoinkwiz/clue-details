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
import java.awt.Color;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import lombok.Getter;
import net.runelite.client.config.*;

@ConfigGroup("clue-details")
public interface ClueDetailsConfig extends Config
{
	String CLUE_ITEMS_CONFIG = "clue-details-items";

	enum ClueOrdering implements Comparator<Clues>
	{
		/**
		 * Sort clues in alphabetical order
		 */
		TIER(ClueOrders.sortByTier(), ClueTierFilter.BEGINNER, ClueTierFilter.EASY, ClueTierFilter.MEDIUM, ClueTierFilter.MEDIUM_KEY, ClueTierFilter.HARD, ClueTierFilter.ELITE, ClueTierFilter.MASTER),
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
		BEGINNER(c -> c.getClueTier() == ClueTier.BEGINNER, "Beginner"),
		EASY(c -> c.getClueTier() == ClueTier.EASY, "Easy"),
		MEDIUM(c -> c.getClueTier() == ClueTier.MEDIUM, "Medium"),
		MEDIUM_KEY(c -> c.getClueTier() == ClueTier.MEDIUM_KEY, "Medium Key"),
		HARD(c -> c.getClueTier() == ClueTier.HARD, "Hard"),
		ELITE(c -> c.getClueTier() == ClueTier.ELITE, "Elite"),
		MASTER(c -> c.getClueTier() == ClueTier.MASTER, "Master");

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

	enum ClueTagLocation
	{
		SPLIT(true, "Split"),
		TOP(false, "Top"),
		BOTTOM(false, "Bottom");

		ClueTagLocation(Object selected, String displayName)
		{
		}
	}

	@ConfigItem(
		keyName = "showSidebar",
		name = "Show sidebar",
		description = "Customise clue details in a sidebar",
		position = 1
	)
	default boolean showSidebar()
	{
		return true;
	}

	@ConfigSection(name = "Sidebar", description = "Options that effect the sidebar", position = 2, closedByDefault = true)
	String sidebarSection = "Sidebar";

	@ConfigItem(
		keyName = "filterListByTier",
		name = "Filter by tier",
		description = "Configures what tier of clue to show",
		section = sidebarSection,
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
		section = sidebarSection,
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
		section = sidebarSection,
		position = 3
	)
	default ClueOrdering orderListBy()
	{
		return ClueOrdering.TIER;
	}

	@ConfigSection(name = "Marked Clues", description = "Options that effect marked clues", position = 3)
	String markedCluesSection = "Marked Clues";

	@ConfigItem(
		keyName = "markedClueDroppedNotification",
		name = "Notify when a marked clue drops",
		description = "Send a notification when a marked clue drops",
		section = markedCluesSection,
		position = 1
	)
	default Notification markedClueDroppedNotification()
	{
		return Notification.ON;
	}

	@ConfigItem(
		keyName = "onlyShowMarkedClues",
		name = "Only show marked clues in the sidebar",
		description = "Toggle whether to only show marked clues in the sidebar",
		section = markedCluesSection,
		position = 5
	)
	default boolean onlyShowMarkedClues()
	{
		return false;
	}

	@ConfigItem(
		keyName = "highlightMarkedClues",
		name = "Highlight marked clues",
		description = "Toggle whether to highlight marked clues",
		section = markedCluesSection,
		position = 2
	)
	default boolean highlightMarkedClues()
	{
		return true;
	}

	@ConfigItem(
		keyName = "highlightFeather",
		name = "Highlighted feathering",
		description = "Configure the feathering of highlighted clues",
	section = markedCluesSection,
		position = 3
	)
	default int highlightFeather()
	{
		return 10;
	}

	@ConfigItem(
		keyName = "outlineWidth",
		name = "Highlighted outline width",
		description = "Configure the outline width of highlighted clues",
		section = markedCluesSection,
		position = 4
	)
	default int outlineWidth()
	{
		return 4;
	}

	@ConfigSection(name = "Overlays", description = "Options that effect overlays", position = 4)
	String overlaysSection = "Overlays";

	@ConfigItem(
		keyName = "showHoverText",
		name = "Show hover text",
		description = "Toggle whether to show tooltips on clue hover",
		section = overlaysSection,
		position = 0
	)
	default boolean showHoverText()
	{
		return true;
	}

	@ConfigItem(
		keyName = "showInventoryClueTags",
		name = "Show clue tags",
		description = "Toggle whether to show clue details as item tags",
		section = overlaysSection,
		position = 1
	)
	default boolean showInventoryClueTags()
	{
		return false;
	}

	@ConfigItem(
		keyName = "clueTagLocation",
		name = "Clue tag location",
		description = "Configures where on the clue item to draw tags",
		section = overlaysSection,
		position = 2
	)
	default ClueTagLocation clueTagLocation()
	{
		return ClueTagLocation.SPLIT;
	}

	@ConfigItem(
		keyName = "clueTagSplit",
		name = "Clue tag split sequence",
		description = "Character sequence on which the tag will be split",
		section = overlaysSection,
		position = 3
	)
	default String clueTagSplit()
	{
		return ": ";
	}

	@ConfigItem(
		keyName = "showInventoryCluesOverlay",
		name = "Show inventory overlay",
		description = "Toggle whether to show an overlay with details on all clues in your inventory",
		section = overlaysSection,
		position = 4
	)
	default boolean showInventoryCluesOverlay()
	{
		return false;
	}

	@ConfigItem(
		keyName = "changeClueText",
		name = "Change ground item menu text",
		description = "Toggle whether to change the ground item menu text to the clue detail",
		section = overlaysSection,
		position = 5
	)
	default boolean changeClueText()
	{
		return false;
	}

	@ConfigItem(
		keyName = "highlightInventoryClueItems",
		name = "Highlight inventory clue items",
		description = "Toggle whether to highlight configured items for each clue in your inventory",
		section = overlaysSection,
		position = 6
	)
	default boolean highlightInventoryClueItems()
	{
		return true;
	}

	@Alpha
	@ConfigItem(
		keyName = "itemHighlightColor",
		name = "Item highlight color",
		description = "Configures the default color for highlighted inventory clue items",
		section = overlaysSection,
		position = 7
	)
	default Color itemHighlightColor()
	{
		return Color.YELLOW.darker();
	}

	@ConfigSection(name = "Overlay Colors", description = "Options that effect overlay colors", position = 5)
	String overlayColorsSection = "Overlay Colors";

	@ConfigItem(
		keyName = "colorHoverText",
		name = "Color hover text",
		description = "Toggle whether to apply clue details color to hover text",
		section = overlayColorsSection,
		position = 0
	)
	default boolean colorHoverText()
	{
		return true;
	}

	@ConfigItem(
		keyName = "colorInventoryClueTags",
		name = "Color clue tags",
		description = "Toggle whether to apply clue details color to clue tags",
		section = overlayColorsSection,
		position = 1
	)
	default boolean colorInventoryClueTags()
	{
		return true;
	}

	@ConfigItem(
		keyName = "colorInventoryCluesOverlay",
		name = "Color inventory overlay",
		description = "Toggle whether to apply clue details color to inventory overlay",
		section = overlayColorsSection,
		position = 2
	)
	default boolean colorInventoryCluesOverlay()
	{
		return true;
	}

	@ConfigItem(
		keyName = "colorChangeClueText",
		name = "Color ground item menu text",
		description = "Toggle whether to apply clue details color to ground item menu text",
		section = overlayColorsSection,
		position = 3
	)
	default boolean colorChangeClueText()
	{
		return true;
	}

	@ConfigItem(
		keyName = "colorInventoryClueItems",
		name = "Color inventory clue items",
		description = "Toggle whether apply clue details color to highlighted inventory clue items",
		section = overlayColorsSection,
		position = 4
	)
	default boolean colorInventoryClueItems()
	{
		return true;
	}

	@ConfigItem(
		keyName = "colorGroundItems",
		name = "Overwrite Ground Items colors",
		description = "When updating clue details colors, apply the color to the Ground Items plugin",
		warning = "Does not work for Beginner and Master clues. Colors must be reset via Ground Items plugin.",
		section = overlayColorsSection,
		position = 5
	)
	default boolean colorGroundItems()
	{
		return false;
	}

	@ConfigItem(
		keyName = "colorInventoryTags",
		name = "Overwrite Inventory Tags colors",
		description = "When updating clue details colors, apply the color to the Inventory Tags plugin",
		warning = "Does not work for Beginner and Master clues. Colors must be reset via Inventory Tags plugin.",
		section = overlayColorsSection,
		position = 6
	)
	default boolean colorInventoryTags()
	{
		return false;
	}

	@ConfigSection(name = "Ground Clues", description = "Options that effect ground clues overlay (only supports Beginner and Master clues", position = 6)
	String groundCluesSection = "Ground Clues";

	@ConfigItem(
		keyName = "showGroundClues",
		name = "Show ground clues",
		description = "Toggle whether to show ground clues overlay",
		section = groundCluesSection,
		position = 0
	)
	default boolean showGroundClues()
	{
		return false;
	}

	@ConfigItem(
			keyName = "showGroundCluesText",
			name = "Show ground clues text",
			description = "Toggle whether to show clue details text to ground clues",
			section = groundCluesSection,
			position = 1
	)
	default boolean showGroundCluesText()
	{
		return true;
	}

	@ConfigItem(
		keyName = "changeGroundClueText",
		name = "Change ground clue text",
		description = "Toggle whether to change the ground clue text to the clue detail",
		section = groundCluesSection,
		position = 2
	)
	default boolean changeGroundClueText()
	{
		return false;
	}

	@ConfigItem(
		keyName = "showGroundCluesDespawn",
		name = "Show ground clues despawn",
		description = "Toggle whether to add despawn timers to the ground clues overlay",
		section = groundCluesSection,
		position = 3
	)
	default boolean showGroundCluesDespawn()
	{
		return false;
	}

	@ConfigItem(
		keyName = "collapseGroundClues",
		name = "Collapse ground clues",
		description = "Toggle whether to combine duplicates in the ground clues overlay",
		section = groundCluesSection,
		position = 4
	)
	default boolean collapseGroundClues()
	{
		return true;
	}

	@ConfigItem(
		keyName = "colorGroundClues",
		name = "Color ground clues",
		description = "Toggle whether to apply clue details color to ground clue text",
		section = groundCluesSection,
		position = 5
	)
	default boolean colorGroundClues()
	{
		return true;
	}

	@ConfigSection(name = "Tier Toggles", description = "Options to enable particular clue tiers", position = 7)
	String tierTogglesSection = "Tier Toggles";

	@ConfigItem(
		keyName = "beginnerDetails",
		name = "Beginner clues",
		description = "Beginner clue details are shown",
		section = tierTogglesSection,
		position = 0
	)
	default boolean beginnerDetails()
	{
		return true;
	}

	@ConfigItem(
		keyName = "easyDetails",
		name = "Easy clues",
		description = "Easy clue details are shown",
		section = tierTogglesSection,
		position = 1
	)
	default boolean easyDetails()
	{
		return true;
	}

	@ConfigItem(
		keyName = "mediumDetails",
		name = "Medium clues",
		description = "Medium clue details are shown",
		section = tierTogglesSection,
		position = 2
	)
	default boolean mediumDetails()
	{
		return true;
	}

	@ConfigItem(
		keyName = "hardDetails",
		name = "Hard clues",
		description = "Hard clue details are shown",
		section = tierTogglesSection,
		position = 3
	)
	default boolean hardDetails()
	{
		return true;
	}

	@ConfigItem(
		keyName = "eliteDetails",
		name = "Elite clues",
		description = "Elite clue details are shown",
		section = tierTogglesSection,
		position = 4
	)
	default boolean eliteDetails()
	{
		return true;
	}

	@ConfigItem(
		keyName = "masterDetails",
		name = "Master clues",
		description = "Master clue details are shown",
		section = tierTogglesSection,
		position = 5
	)
	default boolean masterDetails()
	{
		return true;
	}

	@ConfigSection(name = "Helpers", description = "Options to configure particular helper features", position = 8)
	String helperSection = "helperSection";

	@ConfigItem(
			keyName = "threeStepSaver",
			name = "Three-step saver",
			description = "Allows you to set a three-step master clue you wish to save, removing the ability combine torn scrolls when the set clue is in your inventory",
			section = helperSection,
			position = 1
	)
	default boolean threeStepSaver()
	{
		return true;
	}

	@ConfigItem(
			keyName = "highlightSavedThreeStep",
			name = "Highlight saved three-stepper",
			description = "Configures where to highlight your saved three-stepper",
			section = helperSection,
			position = 2
	)
	default SavedThreeStepEnum highlightSavedThreeStep()
	{
		return SavedThreeStepEnum.OFF;
	}

	enum SavedThreeStepEnum
	{
		OFF,
		GROUND,
		INVENTORY,
		BOTH
	}

	@Alpha
	@ConfigItem(
			keyName = "groundThreeStepHighlightColor",
			name = "Ground saved three-stepper highlight color",
			description = "Configures the color for highlighted saved three-stepper on the ground",
			section = helperSection,
			position = 3
	)
	default Color groundThreeStepHighlightColor()
	{
		return Color.GREEN.darker();
	}

	@Alpha
	@ConfigItem(
			keyName = "threeStepHighlightColor",
			name = "Inventory saved three-stepper highlight color",
			description = "Configures the color for highlighted saved three-stepper in inventory",
			section = helperSection,
			position = 4
	)
	default Color invThreeStepHighlightColor()
	{
		return Color.GREEN.darker();
	}
}
