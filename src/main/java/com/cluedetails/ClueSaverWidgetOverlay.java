package com.cluedetails;

import javax.inject.Singleton;
import net.runelite.api.ItemID;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.ui.overlay.WidgetItemOverlay;
import javax.inject.Inject;
import java.awt.Graphics2D;

import static com.cluedetails.ClueDetailsConfig.SavedClueEnum.BOTH;
import static com.cluedetails.ClueDetailsConfig.SavedClueEnum.INVENTORY;

@Singleton
public class ClueSaverWidgetOverlay extends WidgetItemOverlay
{
	private final ClueDetailsPlugin clueDetailsPlugin;
	private final ClueDetailsConfig config;

	private final ClueSaver clueSaver;

	@Inject
	private ClueSaverWidgetOverlay(ClueDetailsPlugin clueDetailsPlugin, ClueSaver clueSaver, ClueDetailsConfig config)
	{
		this.clueDetailsPlugin = clueDetailsPlugin;
		this.clueSaver = clueSaver;
		this.config = config;
		showOnInventory();
	}

	@Override
	public void renderItemOverlay(Graphics2D graphics, int itemId, WidgetItem widgetItem)
	{
		if (itemId == ItemID.CHALLENGE_SCROLL_ELITE && clueSaver.elitesMatch())
		{
			if (config.eliteSherlockSaver() && (config.highlightSavedClues() == BOTH || config.highlightSavedClues() == INVENTORY))
			{
				clueDetailsPlugin.getItemsOverlay().inventoryTagsOverlay(graphics, itemId, widgetItem, config.invSavedClueHighlightColor());
			}
		}

		if (itemId == ItemID.CLUE_SCROLL_MASTER && clueSaver.mastersMatch())
		{
			if (config.threeStepperSaver() && (config.highlightSavedClues() == BOTH || config.highlightSavedClues() == INVENTORY))
			{
				clueDetailsPlugin.getItemsOverlay().inventoryTagsOverlay(graphics, itemId, widgetItem, config.invSavedClueHighlightColor());
			}
		}
	}
}
