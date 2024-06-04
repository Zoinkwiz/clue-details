package com.cluedetails;

import com.cluedetails.filters.ClueTier;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import lombok.Getter;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.util.Text;

@ConfigGroup("clue-details")
public interface ClueDetailsConfig extends Config
{
	enum ClueTierFilter implements Predicate<Clues>
	{
		SHOW_ALL(c -> true),
		EASY(c -> c.getClueTier() == ClueTier.EASY),
		MEDIUM(c -> c.getClueTier() == ClueTier.MEDIUM),
		HARD(c -> c.getClueTier() == ClueTier.HARD),
		ELITE(c -> c.getClueTier() == ClueTier.ELITE);

		private final Predicate<Clues> predicate;

		@Getter
		private final String displayName;

		private final boolean shouldDisplay;

		ClueTierFilter(Predicate<Clues> predicate)
		{
			this.predicate = predicate;
			this.displayName = Text.titleCase(this);
			this.shouldDisplay = true;
		}

		@Override
		public boolean test(Clues quest)
		{
			return predicate.test(quest);
		}

		public List<Clues> test(Collection<Clues> helpers)
		{
			return helpers.stream().filter(this).collect(Collectors.toList());
		}

		public static ClueTierFilter[] displayFilters()
		{
			return Arrays.stream(ClueTierFilter.values()).filter((questFilter -> questFilter.shouldDisplay)).toArray(ClueTierFilter[]::new);
		}
	}

	@ConfigItem(
		keyName = "showSidebar",
		name = "Show highlighting sidebar",
		description = "Customise clues to be highlighted in a sidebar"
	)
	default boolean showSidebar()
	{
		return false;
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
}
