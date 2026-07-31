package com.cluedetails;

import com.google.gson.Gson;
import javax.inject.Singleton;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.ItemID;
import net.runelite.api.KeyCode;
import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.events.MenuOpened;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.client.config.ConfigManager;
import javax.inject.Inject;

@Slf4j
@Singleton
public class ClueSaver
{
	@Inject
	private Client client;

	@Inject
	private ClueDetailsConfig config;

	@Inject
	private ConfigManager configManager;

	@Inject
	private Gson gson;

	@Inject
	private ClueInventoryManager cim;

	private ClueInstance activeElite;
	private ClueInstance activeMaster;

	@Getter
	private ClueInstance savedEliteSherlock;
	@Getter
	private ClueInstance savedThreeStepper;

	private boolean removeMasterEntries = false;

	private static final String SHERLOCK_ELITE_KEY = "sherlock-elite";
	private static final String THREE_STEP_MASTER_KEY = "three-step-master";

	public void scanInventory()
	{
		if (config.eliteSherlockSaver())
		{
			activeElite = cim.getClueByClueItemId(ItemID.CHALLENGE_SCROLL_ELITE);
		}

		if (config.threeStepperSaver())
		{
			activeMaster = cim.getClueByClueItemId(ItemID.CLUE_SCROLL_MASTER);
			if(activeMaster == null || savedThreeStepper == null)
			{
				removeMasterEntries = false;

			}
			else
			{
				//removes entries if we don't know what clue is in their inv, can be made a toggle.
				removeMasterEntries = mastersMatch() || activeMaster.getClueIds().isEmpty();
			}
		}
	}

	public boolean elitesMatch()
	{
		if (activeElite == null || savedEliteSherlock == null) return false;
		else return activeElite.getClueIds().equals(savedEliteSherlock.getClueIds());
	}

	public boolean mastersMatch()
	{
		if (activeMaster == null || savedThreeStepper == null) return false;
		else return activeMaster.getClueIds().equals(savedThreeStepper.getClueIds());
	}

	public void onMenuOpened(MenuOpened event)
	{
		if (!client.isKeyPressed(KeyCode.KC_SHIFT))
		{
			return;
		}

		MenuEntry firstEntry = event.getFirstEntry();
		if (firstEntry == null) return;
		if (firstEntry.getWidget() == null) return;

		//only menus generated from a clue in inventory pass this widget check.
		if (firstEntry.getTarget().contains("Challenge scroll (elite)"))
		{
			if (config.eliteSherlockSaver() && activeElite != null)
			{
				MenuEntry[] menuEntries = client.getMenu().getMenuEntries();
				if (elitesMatch())
				{
					client.getMenu().createMenuEntry(-menuEntries.length)
						.setOption("Unset elite sherlock")
						.setTarget(event.getFirstEntry().getTarget())
						.setType(MenuAction.RUNELITE)
						.onClick(e -> removeEliteSherlock());
				}
				else
				{
					client.getMenu().createMenuEntry(-menuEntries.length)
						.setOption("Set elite sherlock")
						.setTarget(event.getFirstEntry().getTarget())
						.setType(MenuAction.RUNELITE)
						.onClick(e -> saveEliteSherlock());
				}
			}
		}

		//only menus generated from a clue in inventory pass this widget check.
		if (firstEntry.getTarget().contains("Clue scroll (master)"))
		{
			if (config.threeStepperSaver() && activeMaster != null && activeMaster.getClueIds().size() == 3)
			{
				MenuEntry[] menuEntries = client.getMenu().getMenuEntries();
				if (mastersMatch())
				{
					client.getMenu().createMenuEntry(-menuEntries.length)
						.setOption("Unset three-stepper")
						.setTarget(event.getFirstEntry().getTarget())
						.setType(MenuAction.RUNELITE)
						.onClick(e -> removeThreeStepper());
				}
				else
				{
					client.getMenu().createMenuEntry(-menuEntries.length)
						.setOption("Set three-stepper")
						.setTarget(event.getFirstEntry().getTarget())
						.setType(MenuAction.RUNELITE)
						.onClick(e -> saveThreeStepper());
				}
			}
		}
	}

	public void onMenuOptionClicked(MenuOptionClicked event)
	{
		if (event.getMenuOption().equals("Talk-to") && event.getMenuTarget().contains("Sherlock"))
		{
			if (config.eliteSherlockSaver() && elitesMatch())
			{
				event.consume();
			}
		}
	}

	public void onMenuEntryAdded(MenuEntryAdded event)
	{
		if (config.threeStepperSaver())
		{
			MenuEntry menuEntry = event.getMenuEntry();
			if (menuEntry.getTarget().contains("Torn clue scroll") && removeMasterEntries)
			{
				if (menuEntry.getOption().contains("Use") || menuEntry.getOption().contains("Combine"))
				{
					client.getMenu().removeMenuEntry(menuEntry);
				}
			}
		}
	}

	public void saveEliteSherlock()
	{
		String clueInstanceJson = gson.toJson(activeElite);
		configManager.setConfiguration(ClueDetailsConfig.GROUP, SHERLOCK_ELITE_KEY, clueInstanceJson);
		client.addChatMessage(ChatMessageType.GAMEMESSAGE,"","Successfully set clue as your elite sherlock.","");
		updateEliteSherlock();
		scanInventory();
	}

	public void saveThreeStepper()
	{
		String clueInstanceJson = gson.toJson(activeMaster);
		configManager.setConfiguration(ClueDetailsConfig.GROUP, THREE_STEP_MASTER_KEY, clueInstanceJson);
		client.addChatMessage(ChatMessageType.GAMEMESSAGE,"","Successfully set clue as your three-stepper.","");
		updateThreeStepper();
		scanInventory();
	}

	public void removeEliteSherlock()
	{
		configManager.setConfiguration(ClueDetailsConfig.GROUP, SHERLOCK_ELITE_KEY, "");
		client.addChatMessage(ChatMessageType.GAMEMESSAGE,"","Successfully unset clue as your elite sherlock.","");
		updateEliteSherlock();
		scanInventory();
	}

	public void removeThreeStepper()
	{
		configManager.setConfiguration(ClueDetailsConfig.GROUP, THREE_STEP_MASTER_KEY, "");
		client.addChatMessage(ChatMessageType.GAMEMESSAGE,"","Successfully unset clue as your three-stepper.","");
		updateThreeStepper();
		scanInventory();
	}

	public void updateEliteSherlock()
	{
		String sherlockEliteJson = configManager.getConfiguration(ClueDetailsConfig.GROUP, SHERLOCK_ELITE_KEY);
		if (sherlockEliteJson == null) return;
		savedEliteSherlock = gson.fromJson(sherlockEliteJson, ClueInstance.class);
	}

	public void updateThreeStepper()
	{
		String threeStepMasterJson = configManager.getConfiguration(ClueDetailsConfig.GROUP, THREE_STEP_MASTER_KEY);
		if (threeStepMasterJson == null) return;
		savedThreeStepper = gson.fromJson(threeStepMasterJson, ClueInstance.class);
	}

	public void startUp()
	{
		updateEliteSherlock();
		updateThreeStepper();
		scanInventory();
	}
}
