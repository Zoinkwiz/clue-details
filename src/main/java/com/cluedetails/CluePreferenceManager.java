package com.cluedetails;

import net.runelite.client.config.ConfigManager;

public class CluePreferenceManager
{
	ConfigManager configManager;

	public CluePreferenceManager(ConfigManager configManager)
	{
		this.configManager = configManager;
	}

	public boolean getPreference(int clueID)
	{
		return Boolean.TRUE.equals(configManager.getConfiguration("clue-details-highlights",
			String.valueOf(clueID), Boolean.class));
	}

	public void savePreference(int clueID, boolean newValue)
	{
		configManager.setConfiguration("clue-details-highlights", String.valueOf(clueID), newValue);
	}
}
