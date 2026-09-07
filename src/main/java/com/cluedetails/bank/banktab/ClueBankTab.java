/*
 * Copyright (c) 2021, geheur <https://github.com/geheur>
 * Copyright (c) 2021, Zoinkwiz <https://github.com/Zoinkwiz>
 * Copyright (c) 2018, Adam <Adam@sigterm.info>
 * Copyright (c) 2018, Ron Young <https://github.com/raiyni>
 * Copyright (c) 2018, Tomas Slusny <slusnucky@gmail.com>
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

import com.cluedetails.ClueDetailsConfig;
import net.runelite.api.*;
import net.runelite.api.events.*;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.InventoryID;
import net.runelite.api.gameval.ItemID;
import net.runelite.api.gameval.VarbitID;
import net.runelite.api.widgets.ItemQuantityMode;
import net.runelite.api.widgets.JavaScriptCallback;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetType;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.chat.ChatColorType;
import net.runelite.client.chat.ChatMessageBuilder;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.input.KeyManager;
import net.runelite.client.util.HotkeyListener;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static com.cluedetails.bank.banktab.PotionStorage.COMPONENTS_PER_POTION;
import static com.cluedetails.bank.banktab.PotionStorage.VIAL_IDX;
import static net.runelite.client.plugins.banktags.BankTagsPlugin.*;

/**
 * Adapted from quest-helper's QuestBankTab.java (com.questhelper.bank.banktab), reworked to filter
 * for clues instead of quests.
 */
@Singleton
public class ClueBankTab
{
	private static final int ITEMS_PER_ROW = 8;
	private static final int ITEM_VERTICAL_SPACING = 36;
	private static final int ITEM_HORIZONTAL_SPACING = 48;
	private static final int ITEM_ROW_START = 51;
	private static final int TEXT_HEIGHT = 15;

	private final ArrayList<Widget> addedWidgets = new ArrayList<>();

	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private ChatMessageManager chatMessageManager;

	@Inject
	private ClueBankTabInterface clueBankTabInterface;

	@Inject
	private ClueBankTagService clueBankTagService;

	@Inject
	private ClueDetailsConfig config;

	@Inject
	private KeyManager keyManager;

	@Inject
	private PotionStorage potionStorage;

	private final HotkeyListener hotkeyListener = new HotkeyListener(() -> config.clueBankTabHotkey())
	{
		@Override
		public void hotkeyPressed()
		{
			// Fired from the input thread, not the client thread - marshal before touching widgets.
			clientThread.invokeLater(clueBankTabInterface::toggleTab);
		}
	};

	private final HashMap<Widget, BankTabItem> widgetItems = new HashMap<>();

	private int originalContainerChildren = -1;

	public void startUp()
	{
		if (!config.showClueBankTab()) return;
		clientThread.invokeLater(this::initAndUpdateVisibility);
		keyManager.registerKeyListener(hotkeyListener);
	}

	// init() alone leaves the button default-visible until the next bank rebuild fires.
	private void initAndUpdateVisibility()
	{
		clueBankTabInterface.init();
		clueBankTabInterface.updateVisibility(!clueBankTagService.getBankTabSections().isEmpty());
	}

	public void shutDown()
	{
		keyManager.unregisterKeyListener(hotkeyListener);
		clientThread.invokeLater(clueBankTabInterface::destroy);
		clientThread.invokeLater(this::removeAddedWidgets);
	}

	public void register(EventBus eventBus)
	{
		potionStorage.setClueBankTabInterface(clueBankTabInterface);
		eventBus.register(potionStorage);
		eventBus.register(this);
	}

	public void unregister(EventBus eventBus)
	{
		potionStorage.setClueBankTabInterface(null);
		eventBus.unregister(potionStorage);
		eventBus.unregister(this);
	}

	public void refreshBankTab()
	{
		// Guard here rather than at every call site (inventory/bank changes, various config changes).
		if (!config.showClueBankTab()) return;

		// May be called off the client thread (e.g. a config change), so marshal first.
		clientThread.invokeLater(() -> {
			// refreshTab() alone no-ops unless already active, so update visibility separately.
			clueBankTabInterface.updateVisibility(!clueBankTagService.getBankTabSections().isEmpty());
			clueBankTabInterface.refreshTab();
		});
	}

	@Subscribe
	public void onScriptPreFired(ScriptPreFired event)
	{
		final int POTION_STORE_UPDATED = 6555;

		int scriptId = event.getScriptId();

		if (scriptId == POTION_STORE_UPDATED)
		{
			// Since the script vm isn't reentrant, we can't call into POTIONSTORE_DOSES/POTIONSTORE_WITHDRAW_DOSES
			// from bankmain_finishbuilding for the layout. Instead, we record all of the potions on client tick,
			// which is after this is run, but before the var/inv transmit listeners run, so that we will have
			// them by the time the inv transmit listener runs.
			potionStorage.updateCachedPotions = true;
		}
		else if (scriptId == ScriptID.BANKMAIN_FINISHBUILDING)
		{
			resetWidgets();
			if (clueBankTabInterface.isClueTabActive())
			{
				Widget bankTitle = client.getWidget(InterfaceID.Bankmain.TITLE);
				if (bankTitle != null)
				{
					bankTitle.setText("Tab <col=ff0000>Clue Details</col>");
				}
			}
		}
		else if (scriptId == ScriptID.BANKMAIN_SEARCH_TOGGLE)
		{
			clueBankTabInterface.handleSearch();
		}
	}

	@Subscribe
	public void onScriptCallbackEvent(ScriptCallbackEvent event)
	{
		String eventName = event.getEventName();

		int[] intStack = client.getIntStack();
		int intStackSize = client.getIntStackSize();

		if ("getSearchingTagTab".equals(eventName))
		{
			intStack[intStackSize - 1] = clueBankTabInterface.isClueTabActive() ? 1 : 0;
		}
	}

	@Subscribe
	public void onWidgetLoaded(WidgetLoaded event)
	{
		if (event.getGroupId() == InterfaceID.BANKMAIN && config.showClueBankTab())
		{
			initAndUpdateVisibility();
		}
	}

	@Subscribe(priority = -1)
	public void onMenuOptionClicked(MenuOptionClicked event)
	{
		clueBankTabInterface.handleClick(event);

		// Update widget index of the menu so withdraws work in laid out tabs.
		if (event.getParam1() == InterfaceID.Bankmain.ITEMS && clueBankTabInterface.isClueTabActive())
		{
			MenuEntry menu = event.getMenuEntry();
			if ("Details".equals(menu.getOption()))
			{
				event.consume();

				Widget widget = event.getWidget();
				if (widget == null) return;
				BankTabItem bankTabItem = widgetItems.get(widget);
				if (bankTabItem == null) return;
				handleFakeItemClick(bankTabItem);
				return;
			}

			Widget w = menu.getWidget();
			if (w != null && w.getItemId() > -1)
			{
				ItemContainer bank = client.getItemContainer(InventoryID.BANK);
				int idx = bank.find(w.getItemId());
				if (idx > -1 && menu.getParam0() != idx)
				{
					menu.setParam0(idx);
					return;
				}

				idx = potionStorage.find(w.getItemId());
				if (idx == VIAL_IDX)
				{
					potionStorage.prepareWidgets();
					menu.setParam1(InterfaceID.Bankmain.POTIONSTORE_ITEMS);
					menu.setParam0(VIAL_IDX);
				}
				else if (idx > -1)
				{
					potionStorage.prepareWidgets();
					menu.setParam1(InterfaceID.Bankmain.POTIONSTORE_ITEMS);
					menu.setParam0(idx * COMPONENTS_PER_POTION);
				}
			}
		}
	}

	private void resetWidgets()
	{
		// We adjust the bank item container children's sizes in layouts,
		// however they are only initially set when the bank is opened,
		// so we have to reset them each time the bank is built.
		Widget w = client.getWidget(InterfaceID.Bankmain.ITEMS);
		if (w == null || w.getChildren() == null) return;

		for (Widget c : w.getChildren())
		{
			if (c.getOriginalHeight() < BANK_ITEM_HEIGHT)
			{
				break;
			}

			if (c.getOriginalWidth() != BANK_ITEM_WIDTH || c.getOriginalHeight() != BANK_ITEM_HEIGHT)
			{
				c.setOriginalWidth(BANK_ITEM_WIDTH);
				c.setOriginalHeight(BANK_ITEM_HEIGHT);
				c.revalidate();
			}
		}
	}

	private void removeAddedWidgets()
	{
		if (originalContainerChildren == -1) return;

		if (addedWidgets.isEmpty()) return;
		Widget parent = addedWidgets.get(0).getParent();
		if (parent == null) return;
		if (parent.getChildren() == null) return;
		parent.setChildren(Arrays.copyOf(parent.getChildren(), originalContainerChildren));
		parent.revalidate();

		addedWidgets.clear();
	}

	@Subscribe
	public void onScriptPostFired(ScriptPostFired event)
	{
		if (event.getScriptId() == ScriptID.BANKMAIN_SEARCHING)
		{
			// The return value of bankmain_searching is on the stack. If we have a tag tab active
			// make it return true to put the bank in a searching state.
			if (clueBankTabInterface.isClueTabActive())
			{
				client.getIntStack()[client.getIntStackSize() - 1] = 1; // true
			}

			return;
		}

		if (event.getScriptId() != ScriptID.BANKMAIN_FINISHBUILDING)
		{
			return;
		}

		if (!config.showClueBankTab())
		{
			return;
		}

		removeAddedWidgets();

		Widget itemContainer = client.getWidget(InterfaceID.Bankmain.ITEMS);
		if (itemContainer == null)
		{
			return;
		}
		Widget[] children = itemContainer.getChildren();
		if (children != null && originalContainerChildren == -1) originalContainerChildren = children.length;

		Widget[] containerChildren = itemContainer.getDynamicChildren();
		clientThread.invokeAtTickEnd(() -> {
			List<BankTabItems> tabLayout = clueBankTagService.getBankTabSections();
			clueBankTabInterface.updateVisibility(!tabLayout.isEmpty());

			if (clueBankTabInterface.isClueTabActive())
			{
				sortBankTabItems(itemContainer, containerChildren, tabLayout);
			}
		});
	}

	private void sortBankTabItems(Widget itemContainer, Widget[] containerChildren, List<BankTabItems> newLayout)
	{
		int totalSectionsHeight = 0;

		widgetItems.clear();

		// Hide all widgets as we'll be making our own using them
		hideBankWidgets(itemContainer, containerChildren);

		for (BankTabItems bankTabItems : newLayout)
		{
			totalSectionsHeight = addSection(itemContainer, bankTabItems, totalSectionsHeight);
		}

		currentWidgetToUse = 0;

		final Widget bankItemContainer = client.getWidget(InterfaceID.Bankmain.ITEMS);
		if (bankItemContainer == null) return;
		int itemContainerHeight = bankItemContainer.getHeight();

		bankItemContainer.setScrollHeight(Math.max(totalSectionsHeight, itemContainerHeight));

		final int itemContainerScroll = bankItemContainer.getScrollY();
		clientThread.invokeLater(() ->
			client.runScript(ScriptID.UPDATE_SCROLLBAR,
				InterfaceID.Bankmain.SCROLLBAR,
				InterfaceID.Bankmain.ITEMS,
				itemContainerScroll));
	}

	private void hideBankWidgets(Widget itemContainer, Widget[] containerChildren)
	{
		for (int i = 0; i < containerChildren.length; ++i)
		{
			Widget widget = itemContainer.getChild(i);
			if (widget == null) continue;

			// ~bankmain_drawitem uses 6512 for empty item slots
			if (!widget.isSelfHidden() &&
					(widget.getItemId() > -1 && widget.getItemId() != ItemID.BLANKOBJECT) ||
					(widget.getSpriteId() == SpriteID.RESIZEABLE_MODE_SIDE_PANEL_BACKGROUND || widget.getText().contains("Tab"))
			)
			{
				widget.setHidden(true);
			}
		}
	}

	private int count(ItemContainer bank, int itemId)
	{
		return bank.count(itemId) + potionStorage.count(itemId);
	}

	private void drawItem(Widget c, int item, ItemContainer bank, BankTabItem bankTabItem)
	{
		if (item > -1 && item != ItemID.BANK_FILLER)
		{
			int qty = count(bank, item);
			ItemComposition def = client.getItemDefinition(item);

			int bankCount = bank.count(item);
			boolean isPotStorage = bankCount <= 0 && qty > 0;

			c.setItemId(item);
			c.setItemQuantity(qty);
			c.setItemQuantityMode(ItemQuantityMode.ALWAYS);

			// Effectively avoid dragging
			c.setDragDeadTime(1000);

			c.setName("<col=ff9040>" + def.getName() + "</col>");
			c.clearActions();

			// Jagex Placeholder
			if (def.getPlaceholderTemplateId() >= 0 && def.getPlaceholderId() >= 0)
			{
				c.setItemQuantity(qty);
				c.setOpacity(120);
				c.setAction(8 - 1, "Release");
				c.setAction(10 - 1, "Examine");
			}
			// Layout placeholder
			else if (qty == 0)
			{
				c.setOpacity(120);
				c.setItemQuantity(0);
				c.setItemQuantityMode(1);
				c.setText("<col=ff9040>" + bankTabItem.getText() + "</col>");
				c.setAction(1, "Details");
			}
			else
			{
				int quantityType = client.getVarbitValue(VarbitID.BANK_QUANTITY_TYPE);
				int requestQty = client.getVarbitValue(VarbitID.BANK_REQUESTEDQUANTITY);
				// ~script2759
				String suffix;
				switch (quantityType)
				{
					default:
						suffix = "1";
						break;
					case 1:
						suffix = "5";
						break;
					case 2:
						suffix = "10";
						break;
					case 3:
						suffix = Integer.toString(Math.max(1, requestQty));
						break;
					case 4:
						suffix = "All";
						break;
				}
				// ~script669
				int opIdx = 0;
				c.setAction(opIdx++, "Withdraw-" + suffix);
				if (quantityType != 0)
				{
					c.setAction(opIdx++, "Withdraw-1");
				}
				if (quantityType != 1)
				{
					c.setAction(opIdx++, "Withdraw-5");
				}
				if (quantityType != 2)
				{
					c.setAction(opIdx++, "Withdraw-10");
				}
				if (quantityType != 3 && requestQty > 0)
				{
					c.setAction(opIdx++, "Withdraw-" + requestQty);
				}
				c.setAction(opIdx++, "Withdraw-X");
				if (quantityType != 4)
				{
					c.setAction(opIdx++, "Withdraw-All");
				}
				c.setAction(opIdx++, "Withdraw-All-but-1");
				if (!isPotStorage && client.getVarbitValue(VarbitID.BANK_BANKOPS_TOGGLE_ON) == 1 && def.getIntValue(ParamID.BANK_AUTOCHARGE) != -1)
				{
					c.setAction(opIdx++, "Configure-Charges");
				}
				if (!isPotStorage && client.getVarbitValue(VarbitID.BANK_LEAVEPLACEHOLDERS) == 0)
				{
					c.setAction(opIdx++, "Placeholder");
				}
				if (!isPotStorage)
				{
					c.setAction(9, "Examine");
				}
				c.setOpacity(0);
			}

			c.setOnDragListener(ScriptID.BANKMAIN_DRAGSCROLL, ScriptEvent.WIDGET_ID, ScriptEvent.WIDGET_INDEX, ScriptEvent.MOUSE_X, ScriptEvent.MOUSE_Y,
					InterfaceID.Bankmain.SCROLLBAR, 0);
			c.setOnDragCompleteListener((JavaScriptCallback) ev -> {});
		}
		else
		{
			// pad size to not leave a gap between items
			c.setOriginalWidth(BANK_ITEM_WIDTH + BANK_ITEM_X_PADDING);
			c.setOriginalHeight(BANK_ITEM_HEIGHT + BANK_ITEM_Y_PADDING);
			c.clearActions();
			c.setItemId(-1);
			c.setItemQuantity(0);
			c.setOnDragListener((Object[]) null);
			c.setOnDragCompleteListener((Object[]) null);
		}
		widgetItems.put(c, bankTabItem);
		c.setHidden(false);
		c.revalidate();
	}

	private int addSection(Widget itemContainer, BankTabItems items, int totalSectionsHeight)
	{
		if (items == null || items.getItems().isEmpty())
		{
			return totalSectionsHeight;
		}

		int newHeight = addSectionHeader(itemContainer, items.getName(), totalSectionsHeight);

		return createPartialSection(items.getItems(), newHeight);
	}

	int currentWidgetToUse = 0;

	// Returns the new total section height after laying out this batch of items
	private int createPartialSection(List<BankTabItem> items, int totalSectionsHeight)
	{
		int totalItemsAdded = 0;

		ItemContainer bank = client.getItemContainer(InventoryID.BANK);
		if (bank == null) return totalSectionsHeight;
		Widget bankItemContainer = client.getWidget(InterfaceID.Bankmain.ITEMS);
		if (bankItemContainer == null) return totalSectionsHeight;

		for (BankTabItem item : items)
		{
			int itemId = item.getItemId();
			if (itemId == -1)
			{
				continue;
			}

			Widget c = bankItemContainer.getChild(currentWidgetToUse);
			if (c == null)
			{
				return totalSectionsHeight;
			}
			drawItem(c, itemId, bank, item);
			placeItem(c, totalItemsAdded, totalSectionsHeight);

			currentWidgetToUse++;
			totalItemsAdded++;
		}

		int newHeight = totalSectionsHeight + (totalItemsAdded / ITEMS_PER_ROW) * ITEM_VERTICAL_SPACING;
		newHeight = totalItemsAdded % ITEMS_PER_ROW != 0 ? newHeight + ITEM_VERTICAL_SPACING : newHeight;
		return newHeight;
	}

	private int addSectionHeader(Widget itemContainer, String title, int totalSectionsHeight)
	{
		int lineVerticalSpacing = 5;
		addedWidgets.add(createGraphic(itemContainer, SpriteID.RESIZEABLE_MODE_SIDE_PANEL_BACKGROUND, ITEM_ROW_START, totalSectionsHeight));
		addedWidgets.add(createText(itemContainer, title, new Color(228, 216, 162).getRGB(), (ITEMS_PER_ROW * ITEM_HORIZONTAL_SPACING) + ITEM_ROW_START
			, TEXT_HEIGHT, ITEM_ROW_START, totalSectionsHeight + lineVerticalSpacing));

		return totalSectionsHeight + lineVerticalSpacing + TEXT_HEIGHT;
	}

	private void placeItem(Widget widget, int totalItemsAdded, int totalSectionsHeight)
	{
		int adjYOffset = totalSectionsHeight + (totalItemsAdded / ITEMS_PER_ROW) * ITEM_VERTICAL_SPACING;
		int adjXOffset = (totalItemsAdded % ITEMS_PER_ROW) * ITEM_HORIZONTAL_SPACING + ITEM_ROW_START;

		if (widget.getOriginalY() != adjYOffset)
		{
			widget.setOriginalY(adjYOffset);
			widget.revalidate();
		}

		if (widget.getOriginalX() != adjXOffset)
		{
			widget.setOriginalX(adjXOffset);
			widget.revalidate();
		}
	}

	private void handleFakeItemClick(BankTabItem bankTabItem)
	{
		final ChatMessageBuilder message = new ChatMessageBuilder()
				.append("You may need ")
				.append(ChatColorType.HIGHLIGHT)
				.append(bankTabItem.getText());

		if (bankTabItem.getDetails() != null && !bankTabItem.getDetails().isEmpty())
		{
			message.append(ChatColorType.NORMAL)
					.append(" for '" + bankTabItem.getDetails() + "'.");
		}
		else
		{
			message.append(ChatColorType.NORMAL).append(".");
		}

		chatMessageManager.queue(QueuedMessage.builder()
				.type(ChatMessageType.ITEM_EXAMINE)
				.runeLiteFormattedMessage(message.build())
				.build());
	}

	private Widget createGraphic(Widget container, int spriteId, int x, int y)
	{
		final int WIDTH = ITEMS_PER_ROW * ITEM_HORIZONTAL_SPACING;
		final int LINE_HEIGHT = 2;
		Widget widget = container.createChild(-1, WidgetType.GRAPHIC);
		widget.setOriginalWidth(WIDTH);
		widget.setOriginalHeight(LINE_HEIGHT);
		widget.setOriginalX(x);
		widget.setOriginalY(y);

		widget.setSpriteId(spriteId);

		widget.revalidate();

		return widget;
	}

	private Widget createText(Widget container, String text, int color, int width, int height, int x, int y)
	{
		Widget widget = container.createChild(-1, WidgetType.TEXT);

		widget.setOriginalWidth(width);
		widget.setOriginalHeight(height);
		widget.setOriginalX(x);
		widget.setOriginalY(y);

		widget.setText(text);
		widget.setFontId(FontID.PLAIN_11);
		widget.setTextColor(color);
		widget.setTextShadowed(true);

		widget.revalidate();

		return widget;
	}
}
