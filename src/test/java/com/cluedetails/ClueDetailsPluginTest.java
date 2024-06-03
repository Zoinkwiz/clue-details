package com.cluedetails;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class ClueDetailsPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(ClueDetailsPlugin.class);
		RuneLite.main(args);
	}
}