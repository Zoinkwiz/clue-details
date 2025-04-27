package com.cluedetails;

import javax.inject.Singleton;
import net.runelite.api.ItemID;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.ui.overlay.WidgetItemOverlay;
import javax.inject.Inject;
import java.awt.Graphics2D;

import static com.cluedetails.ClueDetailsConfig.SavedThreeStepperEnum.BOTH;
import static com.cluedetails.ClueDetailsConfig.SavedThreeStepperEnum.INVENTORY;

@Singleton
public class ClueThreeStepSaverWidgetOverlay extends WidgetItemOverlay
{
	private final ClueDetailsPlugin clueDetailsPlugin;
	private final ClueDetailsConfig config;

	private final ClueThreeStepSaver clueThreeStepSaver;

	@Inject
	private ClueThreeStepSaverWidgetOverlay(ClueDetailsPlugin clueDetailsPlugin, ClueThreeStepSaver clueThreeStepSaver, ClueDetailsConfig config)
	{
		this.clueDetailsPlugin = clueDetailsPlugin;
		this.clueThreeStepSaver = clueThreeStepSaver;
		this.config = config;
		showOnInventory();
	}

	@Override
	public void renderItemOverlay(Graphics2D graphics, int itemId, WidgetItem widgetItem)
	{
		if (itemId != ItemID.CLUE_SCROLL_MASTER || !clueThreeStepSaver.cluesMatch())
		{
			return;
		}

		if (config.threeStepperSaver() && (config.highlightSavedThreeStepper() == BOTH || config.highlightSavedThreeStepper() == INVENTORY))
		{
			clueDetailsPlugin.getItemsOverlay().inventoryTagsOverlay(graphics, itemId, widgetItem, config.invThreeStepperHighlightColor());
		}
	}
}
