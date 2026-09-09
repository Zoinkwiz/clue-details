package com.cluedetails.bank.banktab;

import com.cluedetails.ClueBankManager;
import com.cluedetails.ClueDetailsConfig;
import com.cluedetails.ClueDetailsPlugin;
import com.cluedetails.ClueInstance;
import com.cluedetails.ClueInventoryManager;
import com.cluedetails.Clues;
import com.google.gson.Gson;
import net.runelite.api.Client;
import net.runelite.api.Item;
import net.runelite.api.ItemComposition;
import net.runelite.api.ItemContainer;
import net.runelite.api.ItemID;
import net.runelite.api.gameval.InventoryID;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.game.ItemManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.AdditionalAnswers.returnsFirstArg;

/**
 * Covers the clue bank filter's section-building rules: inventory clues never include the clue
 * scroll itself, bank clues do, unresolved tracked-tier clues (Beginner/Master/Elite Challenge)
 * still fall back to a generic tier section, and disabled tiers/the "include bank clues" toggle
 * correctly gate what shows up.
 */
public class ClueBankTagServiceTest
{
	private final Client client = mock(Client.class);
	private final ClueDetailsConfig config = mock(ClueDetailsConfig.class);
	private final ClueInventoryManager clueInventoryManager = mock(ClueInventoryManager.class);
	private final ClueBankManager clueBankManager = mock(ClueBankManager.class);
	private final ConfigManager configManager = mock(ConfigManager.class);
	private final ItemManager itemManager = mock(ItemManager.class);
	private final ClueDetailsPlugin plugin = mock(ClueDetailsPlugin.class);
	private final PotionStorage potionStorage = mock(PotionStorage.class);

	private ClueBankTagService service;

	@BeforeEach
	void setUp() throws Exception
	{
		Clues.rebuildFilteredCluesCache();

		when(config.beginnerDetails()).thenReturn(true);
		when(config.easyDetails()).thenReturn(true);
		when(config.mediumDetails()).thenReturn(true);
		when(config.hardDetails()).thenReturn(true);
		when(config.eliteDetails()).thenReturn(true);
		when(config.masterDetails()).thenReturn(true);

		ItemComposition itemComposition = mock(ItemComposition.class);
		when(itemComposition.getName()).thenReturn("Test item");
		when(itemManager.getItemComposition(anyInt())).thenReturn(itemComposition);

		// By default, nothing is a potion dose that needs substituting - echo the item id back.
		when(potionStorage.resolveActualItemId(anyInt())).thenAnswer(returnsFirstArg());

		// Clues.getItems() reads plugin.gson directly; avoids constructing a full real plugin.
		setField(plugin, ClueDetailsPlugin.class, "gson", new Gson());

		service = new ClueBankTagService();
		setField(service, "plugin", plugin);
		setField(service, "config", config);
		setField(service, "client", client);
		setField(service, "clueInventoryManager", clueInventoryManager);
		setField(service, "clueBankManager", clueBankManager);
		setField(service, "configManager", configManager);
		setField(service, "itemManager", itemManager);
		setField(service, "potionStorage", potionStorage);
	}

	private static void setField(Object target, String fieldName, Object value) throws Exception
	{
		setField(target, target.getClass(), fieldName, value);
	}

	private static void setField(Object target, Class<?> declaringClass, String fieldName, Object value) throws Exception
	{
		Field field = declaringClass.getDeclaredField(fieldName);
		field.setAccessible(true);
		field.set(target, value);
	}

	@Test
	void inventoryClueWithConfiguredItems_showsThoseItemsButNotTheScrollItself()
	{
		when(configManager.getConfiguration(ClueDetailsConfig.CLUE_ITEMS_CONFIG, "0")).thenReturn("[995]");
		when(clueInventoryManager.getCluesInInventory()).thenReturn(Set.of(ItemID.CLUE_SCROLL_BEGINNER));
		when(clueInventoryManager.getClueByClueItemId(ItemID.CLUE_SCROLL_BEGINNER))
			.thenReturn(new ClueInstance(List.of(0), ItemID.CLUE_SCROLL_BEGINNER));

		List<BankTabItems> sections = service.getBankTabSections();

		assertEquals(1, sections.size());
		BankTabItems section = sections.get(0);
		assertEquals("Ranael: Talk", section.getName());
		assertEquals(1, section.getItems().size());
		assertEquals(995, section.getItems().get(0).getItemId());
	}

	@Test
	void unresolvedTrackedClueInInventory_producesNoSection()
	{
		when(clueInventoryManager.getCluesInInventory()).thenReturn(Set.of(ItemID.CLUE_SCROLL_BEGINNER));
		when(clueInventoryManager.getClueByClueItemId(ItemID.CLUE_SCROLL_BEGINNER))
			.thenReturn(new ClueInstance(Collections.emptyList(), ItemID.CLUE_SCROLL_BEGINNER));

		assertTrue(service.getBankTabSections().isEmpty());
	}

	@Test
	void unresolvedTrackedClueInBank_includesScrollItselfUnderGenericTierName()
	{
		when(config.includeBankCluesInBankTab()).thenReturn(true);
		stubBankContents(ItemID.CLUE_SCROLL_BEGINNER);
		// Never seen deposited this session, so ClueBankManager has no record of it.
		when(clueBankManager.getClueByClueItemId(ItemID.CLUE_SCROLL_BEGINNER)).thenReturn(null);

		List<BankTabItems> sections = service.getBankTabSections();

		assertEquals(1, sections.size());
		BankTabItems section = sections.get(0);
		assertEquals("Beginner clue", section.getName());
		assertEquals(1, section.getItems().size());
		assertEquals(ItemID.CLUE_SCROLL_BEGINNER, section.getItems().get(0).getItemId());
	}

	@Test
	void includeBankCluesInBankTabDisabled_ignoresBankContents()
	{
		when(config.includeBankCluesInBankTab()).thenReturn(false);
		stubBankContents(ItemID.CLUE_SCROLL_BEGINNER);

		assertTrue(service.getBankTabSections().isEmpty());
	}

	@Test
	void disabledTier_producesNoSection()
	{
		when(config.beginnerDetails()).thenReturn(false);
		when(clueInventoryManager.getCluesInInventory()).thenReturn(Set.of(ItemID.CLUE_SCROLL_BEGINNER));
		when(clueInventoryManager.getClueByClueItemId(ItemID.CLUE_SCROLL_BEGINNER))
			.thenReturn(new ClueInstance(List.of(0), ItemID.CLUE_SCROLL_BEGINNER));

		assertTrue(service.getBankTabSections().isEmpty());
	}

	private void stubBankContents(int... itemIds)
	{
		ItemContainer bank = mock(ItemContainer.class);
		Item[] items = new Item[itemIds.length];
		for (int i = 0; i < itemIds.length; i++)
		{
			Item item = mock(Item.class);
			when(item.getId()).thenReturn(itemIds[i]);
			items[i] = item;
		}
		when(bank.getItems()).thenReturn(items);
		when(client.getItemContainer(InventoryID.BANK)).thenReturn(bank);
	}
}
