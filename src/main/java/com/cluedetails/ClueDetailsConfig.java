package com.cluedetails;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("clue-details")
public interface ClueDetailsConfig extends Config
{
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
	default ClueTier filterListBy()
	{
		return ClueTier.ALL;
	}
}
