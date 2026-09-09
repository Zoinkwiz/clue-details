/*
 * Copyright (c) 2021, Zoinkwiz <https://github.com/Zoinkwiz>
 * Copyright (c) 2018, Tomas Slusny <slusnucky@gmail.com>
 * Copyright (c) 2018, Ron Young <https://github.com/raiyni>
 * Copyright (c) 2026, TheLope <https://github.com/TheLope>
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
package com.cluedetails.bank.banktab;

import lombok.Getter;
import lombok.Setter;
import net.runelite.api.*;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.VarbitID;
import net.runelite.api.widgets.JavaScriptCallback;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetType;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.plugins.bank.BankSearch;
import net.runelite.client.util.ImageUtil;

import javax.inject.Inject;
import java.awt.image.BufferedImage;

/**
 * Adapted from quest-helper's QuestBankTabInterface.java (com.questhelper.bank.banktab), reworked
 * to filter for clues instead of quests.
 */
public class ClueBankTabInterface
{
	private static final String VIEW_TAB = "View tab ";

	private static final int BANKTAB_POTIONSTORE = 15;

	private static final int CLUE_BUTTON_SIZE = 25;
	private static final int CLUE_ICON_SIZE = CLUE_BUTTON_SIZE - 6;

	// Arbitrary sprite id (outside the real game sprite range) used to register the plugin's own
	// icon.png as a sprite override, so the bank tab button matches the icon used elsewhere in
	// the plugin (e.g. the sidebar).
	private static final int CLUE_TAB_ICON_SPRITE_ID = 92040;

	@Setter
	@Getter
	private boolean clueTabActive = false;

	@Getter
	private Widget parent;

	@Getter
	private Widget clueIconWidget;

	@Getter
	private Widget clueBackgroundWidget;

	private final Client client;
	private final ClientThread clientThread;
	private final BankSearch bankSearch;
	private final BufferedImage clueIcon;

	@Inject
	public ClueBankTabInterface(Client client, ClientThread clientThread, BankSearch bankSearch)
	{
		this.client = client;
		this.bankSearch = bankSearch;
		this.clientThread = clientThread;
		this.clueIcon = ImageUtil.loadImageResource(getClass(), "/icon.png");
	}

	public void init()
	{
		if (isHidden())
		{
			return;
		}

		parent = client.getWidget(InterfaceID.Bankmain.UNIVERSE);

		// Placed in the gap between quest-helper's bank button (ends at local x=433) and the next
		// native icon (starts at local x=459), to avoid conflicting with either.
		int CLUE_BUTTON_X = 434;
		int CLUE_BUTTON_Y = 5;
		clueBackgroundWidget = createGraphic("clue-details", SpriteID.UNKNOWN_BUTTON_SQUARE_SMALL, CLUE_BUTTON_SIZE,
			CLUE_BUTTON_SIZE,
			CLUE_BUTTON_X, CLUE_BUTTON_Y);
		clueBackgroundWidget.setAction(1, VIEW_TAB);
		clueBackgroundWidget.setOnOpListener((JavaScriptCallback) this::handleTagTab);

		registerClueIconSpriteOverride();
		clueIconWidget = createGraphic("", CLUE_TAB_ICON_SPRITE_ID, CLUE_ICON_SIZE,
			CLUE_ICON_SIZE,
			CLUE_BUTTON_X + 3, CLUE_BUTTON_Y + 3);

		if (clueTabActive)
		{
			boolean wasInPotionStorage = client.getVarbitValue(VarbitID.BANK_CURRENTTAB) == BANKTAB_POTIONSTORE;
			clueTabActive = false;
			clientThread.invokeLater(() -> activateTab(wasInPotionStorage));
		}
	}

	public void destroy()
	{
		if (clueTabActive)
		{
			closeTab();
			bankSearch.reset(true);
		}

		parent = null;

		if (clueIconWidget != null)
		{
			clueIconWidget.setHidden(true);
		}

		if (clueBackgroundWidget != null)
		{
			clueBackgroundWidget.setHidden(true);
		}
		clueTabActive = false;
	}

	/**
	 * Shows or hides the button depending on whether there's anything to filter for. Closes the
	 * tab first if it's being hidden while active, so the player isn't stuck on a dead view.
	 */
	public void updateVisibility(boolean hasClues)
	{
		if (clueBackgroundWidget == null || clueIconWidget == null)
		{
			return;
		}

		if (!hasClues && clueTabActive)
		{
			closeTab();
			bankSearch.reset(true);
		}

		clueBackgroundWidget.setHidden(!hasClues);
		clueIconWidget.setHidden(!hasClues);
	}

	public void handleClick(MenuOptionClicked event)
	{
		if (isHidden())
		{
			return;
		}
		String menuOption = event.getMenuOption();

		// If click a base tab, close
		boolean clickedTabTag = menuOption.startsWith("View tab") && !event.getMenuTarget().equals("clue-details");
		boolean clickPotionStorage = menuOption.startsWith("Potion store");
		boolean clickedOtherTab = menuOption.equals("View all items") || menuOption.startsWith("View tag tab");
		if (clueTabActive && clickPotionStorage)
		{
			closeTab();
			// Opening potion storage doesn't trigger a bank rebuild on its own, so force one.
			bankSearch.reset(true);
		}
		else if (clueTabActive && (clickedTabTag || clickedOtherTab))
		{
			closeTab();
		}
	}

	public void handleSearch()
	{
		if (clueTabActive)
		{
			closeTab();
			// This ensures that when clicking Search when tab is selected, the search input is opened rather
			// than client trying to close it first
			client.setVarcStrValue(VarClientStr.INPUT_TEXT, "");
			client.setVarcIntValue(VarClientInt.INPUT_TYPE, 0);
		}
	}

	public boolean isHidden()
	{
		Widget widget = client.getWidget(InterfaceID.Bankmain.UNIVERSE);
		return widget == null || widget.isHidden();
	}

	private void handleTagTab(ScriptEvent event)
	{
		if (event.getOp() == 2)
		{
			handleToggle();
		}
	}

	/**
	 * Toggles the clue tab on/off, as if the button were clicked. No-ops if the button is hidden,
	 * so this is also safe to wire up to a hotkey.
	 * <p>
	 * Known conflict: using the hotkey while quest-helper's bank filter is active can leave both
	 * tabs active at once.
	 */
	public void toggleTab()
	{
		if (isHidden() || clueBackgroundWidget == null || clueBackgroundWidget.isHidden())
		{
			return;
		}

		handleToggle();
	}

	private void handleToggle()
	{
		boolean wasInPotionStorage = client.getVarbitValue(VarbitID.BANK_CURRENTTAB) == BANKTAB_POTIONSTORE;
		client.setVarbit(VarbitID.BANK_CURRENTTAB, 0);

		if (clueTabActive)
		{
			closeTab();
			bankSearch.reset(true);
		}
		else
		{
			activateTab(wasInPotionStorage);
			// openTag will reset and relayout
		}

		client.playSoundEffect(SoundEffectID.UI_BOOP);
	}

	public void closeTab()
	{
		clueTabActive = false;
		if (clueBackgroundWidget != null)
		{
			clueBackgroundWidget.setSpriteId(SpriteID.UNKNOWN_BUTTON_SQUARE_SMALL);
			clueBackgroundWidget.revalidate();
		}
	}

	public void refreshTab()
	{
		if (!clueTabActive)
		{
			return;
		}

		client.setVarbit(VarbitID.BANK_CURRENTTAB, 0);

		bankSearch.reset(true); // clear search dialog & relayout bank for new tab.

		// When searching the button has a script on timer to detect search end, that will set the background back
		// and remove the timer. However since we are going from a bank search to our fake search this will not remove
		// the timer but instead re-add it and reset the background. So remove the timer and the background. This is the
		// same as bankmain_search_setbutton.
		Widget searchButtonBackground = client.getWidget(InterfaceID.Bankmain.SEARCH);
		if (searchButtonBackground != null)
		{
			searchButtonBackground.setOnTimerListener((Object[]) null);
			searchButtonBackground.setSpriteId(SpriteID.EQUIPMENT_SLOT_TILE);
		}
	}

	private void activateTab(boolean wasInPotionStorage)
	{
		if (clueTabActive)
		{
			return;
		}

		if (wasInPotionStorage)
		{
			// Opening a tag tab with the potion store open would leave the store open in the bankground,
			// making deposits not work. Force close the potion store.
			client.menuAction(-1, InterfaceID.Bankmain.POTIONSTORE_BUTTON, MenuAction.CC_OP, 1, -1, "Potion store", "");
		}

		clueBackgroundWidget.setSpriteId(SpriteID.UNKNOWN_BUTTON_SQUARE_SMALL_SELECTED);
		clueBackgroundWidget.revalidate();
		clueTabActive = true;

		bankSearch.reset(true); // clear search dialog & relayout bank for new tab.

		// When searching the button has a script on timer to detect search end, that will set the background back
		// and remove the timer. However since we are going from a bank search to our fake search this will not remove
		// the timer but instead re-add it and reset the background. So remove the timer and the background. This is the
		// same as bankmain_search_setbutton.
		Widget searchButtonBackground = client.getWidget(InterfaceID.Bankmain.SEARCH);
		if (searchButtonBackground != null)
		{
			searchButtonBackground.setOnTimerListener((Object[]) null);
			searchButtonBackground.setSpriteId(SpriteID.EQUIPMENT_SLOT_TILE);
		}
	}

	private void registerClueIconSpriteOverride()
	{
		if (client.getSpriteOverrides().containsKey(CLUE_TAB_ICON_SPRITE_ID))
		{
			return;
		}
		client.getSpriteOverrides().put(CLUE_TAB_ICON_SPRITE_ID, ImageUtil.getImageSpritePixels(clueIcon, client));
		client.getWidgetSpriteCache().reset();
	}

	private Widget createGraphic(Widget container, String name, int spriteId, int width, int height, int x, int y)
	{
		Widget widget = container.createChild(-1, WidgetType.GRAPHIC);
		widget.setOriginalWidth(width);
		widget.setOriginalHeight(height);
		widget.setOriginalX(x);
		widget.setOriginalY(y);

		widget.setSpriteId(spriteId);
		widget.setOnOpListener(ScriptID.NULL);
		widget.setHasListener(true);
		widget.setName(name);
		widget.revalidate();

		return widget;
	}

	private Widget createGraphic(String name, int spriteId, int width, int height, int x, int y)
	{
		return createGraphic(parent, name, spriteId, width, height, x, y);
	}
}
