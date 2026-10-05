package com.cluedetails;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import net.runelite.api.Client;
import net.runelite.api.ItemID;
import net.runelite.api.MenuAction;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.events.InfoBoxMenuClicked;
import net.runelite.client.ui.overlay.OverlayMenuEntry;
import net.runelite.client.ui.overlay.infobox.InfoBoxManager;
import net.runelite.client.ui.overlay.worldmap.WorldMapPointManager;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class ClueGroundTimersTest
{
	private final ClueDetailsConfig config = mock(ClueDetailsConfig.class, CALLS_REAL_METHODS);
	private final Client client = mock(Client.class);
	private final ClueDetailsPlugin plugin = spy(new ClueDetailsPlugin());
	private final InfoBoxManager infoBoxes = mock(InfoBoxManager.class);
	private final WorldMapPointManager worldMap = mock(WorldMapPointManager.class);
	private ClueGroundManager ground;
	private WorldPointToClueInstances tracked;
	private List<ClueGroundTimer> timers;

	@Before
	@SuppressWarnings("unchecked")
	public void setUp() throws Exception
	{
		Clues.setConfig(null);
		Clues.rebuildFilteredCluesCache();
		when(config.showGroundClueTimers()).thenReturn(true);
		when(config.combineGroundClueTimers()).thenReturn(true);
		when(config.collapseGroundClues()).thenReturn(false);
		when(config.collapseGroundCluesByTier()).thenReturn(false);
		when(config.groundClueTimersNotificationTime()).thenReturn(60);
		when(client.getTickCount()).thenReturn(100);
		doReturn(new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB)).when(plugin).getClueScrollImage();
		ground = new ClueGroundManager(client, null, plugin);
		tracked = (WorldPointToClueInstances) field(ground, "trackedClues").get(ground);
		setField(plugin, "client", client);
		setField(plugin, "config", config);
		setField(plugin, "configManager", mock(ConfigManager.class));
		setField(plugin, "clueGroundManager", ground);
		setField(plugin, "infoBoxManager", infoBoxes);
		setField(plugin, "worldMapPointManager", worldMap);
		timers = (List<ClueGroundTimer>) field(plugin, "clueGroundTimers").get(plugin);
	}

	@Test
	public void selectsSoonestExpiryEvenWhenItWasDiscoveredLater()
	{
		add(clue(0, 600, ItemID.CLUE_SCROLL_BEGINNER));
		add(clue(0, 200, ItemID.CLUE_SCROLL_BEGINNER));
		add(clue(1, 400, ItemID.CLUE_SCROLL_BEGINNER));
		plugin.renderGroundClueTimers();
		assertEquals(200, visibleTimer().getDespawnTick());
		assertEquals(new WorldPoint(3200, 3200, 0), visibleTimer().getWorldPoint());
		assertTrue(hasOption(visibleTimer(), "Clear Oldest"));
	}

	@Test
	public void tierCollapseCountsCluesDiscoveredOutOfExpiryOrder()
	{
		assertCollapseCounts(true);
	}

	@Test
	public void stepCollapseCountsCluesDiscoveredOutOfExpiryOrder()
	{
		assertCollapseCounts(false);
	}

	private void assertCollapseCounts(boolean byTier)
	{
		ClueInstance later = clue(0, 600, ItemID.CLUE_SCROLL_BEGINNER);
		ClueInstance sooner = clue(0, 200, ItemID.CLUE_SCROLL_BEGINNER);
		TreeSet<ClueInstance> instances = new TreeSet<>(Comparator.comparingLong(ClueInstance::getSequenceNumber));
		instances.addAll(Arrays.asList(later, sooner));
		Map<ClueInstance, Integer> collapsed = byTier
			? ClueGroundManager.keepOldestTierClues(instances)
			: ClueGroundManager.keepOldestUniqueClues(instances);
		assertEquals(Integer.valueOf(2), collapsed.get(sooner));
	}

	@Test
	public void sumsCollapsedQuantitiesAcrossTilesAndRetainsSoonestClue()
	{
		when(config.collapseGroundCluesByTier()).thenReturn(true);
		ClueInstance later = clue(0, 600, ItemID.CLUE_SCROLL_BEGINNER);
		ClueInstance sooner = clue(1, 200, ItemID.CLUE_SCROLL_BEGINNER);
		Map<ClueInstance, Integer> entries = new LinkedHashMap<>();
		entries.put(later, 2);
		entries.put(sooner, 3);
		Map<ClueInstance, Integer> merged = ClueGroundManager.mergeQuantitiesAcrossTiles(entries, config);
		assertEquals(1, merged.size());
		assertEquals(Integer.valueOf(5), merged.get(sooner));
	}

	@Test
	public void combinedTooltipCountsAllTilesAndToggleRestoresIndividualTimers()
	{
		when(config.collapseGroundClues()).thenReturn(true);
		add(clue(0, 200, ItemID.CLUE_SCROLL_BEGINNER));
		add(clue(1, 400, ItemID.CLUE_SCROLL_BEGINNER));
		plugin.renderGroundClueTimers();
		assertTrue(visibleTimer().getTooltip().contains("Beginner (2)"));
		when(config.combineGroundClueTimers()).thenReturn(false);
		plugin.renderGroundClueTimers();
		assertEquals(2, timers.stream().filter(ClueGroundTimer::render).count());
		for (ClueGroundTimer timer : timers)
		{
			assertFalse(timer.getTooltip().contains("(2)"));
			assertTrue(hasOption(timer, "Clear"));
			assertTrue(hasOption(timer, "Locate"));
		}
	}

	@Test
	public void ignoresDisabledTiersAndHandsOffAfterExpiry()
	{
		when(config.masterDetails()).thenReturn(false);
		add(clue(0, 150, ItemID.CLUE_SCROLL_MASTER));
		add(clue(1, 200, ItemID.CLUE_SCROLL_BEGINNER));
		add(clue(2, 400, ItemID.CLUE_SCROLL_BEGINNER));
		plugin.renderGroundClueTimers();
		assertEquals(200, visibleTimer().getDespawnTick());
		when(client.getTickCount()).thenReturn(200);
		tracked.removeDespawnedClues();
		plugin.renderGroundClueTimers();
		assertEquals(400, visibleTimer().getDespawnTick());
		assertTrue(hasOption(visibleTimer(), "Clear"));
	}

	@Test
	public void doesNotCullTheOnlyVisibleTimerOneTickBeforeExpiry()
	{
		add(clue(0, 101, ItemID.CLUE_SCROLL_BEGINNER));
		add(clue(1, 400, ItemID.CLUE_SCROLL_BEGINNER));
		plugin.renderGroundClueTimers();
		assertFalse(visibleTimer().cull());
		when(client.getTickCount()).thenReturn(101);
		assertTrue(visibleTimer().cull());
	}

	@Test
	public void notificationColorClearsWhenPileIsRefreshed()
	{
		ClueInstance clue = clue(0, 150, ItemID.CLUE_SCROLL_BEGINNER);
		add(clue);
		plugin.renderGroundClueTimers();
		ClueGroundTimer timer = visibleTimer();
		assertEquals(Color.RED, timer.getTextColor());
		timer.setNotified(true);
		clue.setTimeToDespawnFromDataInTicks(600);
		plugin.renderGroundClueTimers();
		assertEquals(Color.WHITE, timer.getTextColor());
	}

	@Test
	public void menuActionsOnlyAffectTheSelectedTile()
	{
		add(clue(0, 200, ItemID.CLUE_SCROLL_BEGINNER));
		add(clue(1, 400, ItemID.CLUE_SCROLL_BEGINNER));
		plugin.renderGroundClueTimers();
		ClueGroundTimer oldest = visibleTimer();
		click(oldest, "Locate Oldest");
		verify(worldMap).add(oldest.getClueDetailsWorldMapPoint());
		assertTrue(hasOption(oldest, "Unlocate Oldest"));
		click(oldest, "Unlocate Oldest");
		verify(worldMap).remove(oldest.getClueDetailsWorldMapPoint());
		click(oldest, "Clear Oldest");
		assertEquals(1, ground.getTrackedWorldPoints().size());
		assertFalse(ground.getTrackedWorldPoints().contains(oldest.getWorldPoint()));
		assertEquals(400, visibleTimer().getDespawnTick());
		assertTrue(hasOption(visibleTimer(), "Clear"));
	}

	private void click(ClueGroundTimer timer, String option)
	{
		InfoBoxMenuClicked event = new InfoBoxMenuClicked(
			new OverlayMenuEntry(MenuAction.RUNELITE_INFOBOX, option, ClueDetailsPlugin.CLUE_GROUND_TIMER_TARGET), timer);
		plugin.onInfoBoxMenuClicked(event);
	}

	private ClueGroundTimer visibleTimer()
	{
		List<ClueGroundTimer> visible = new ArrayList<>();
		timers.stream().filter(ClueGroundTimer::render).forEach(visible::add);
		assertEquals(1, visible.size());
		return visible.get(0);
	}

	private boolean hasOption(ClueGroundTimer timer, String option)
	{
		return timer.getMenuEntries().stream().anyMatch(entry -> option.equals(entry.getOption()));
	}

	private void add(ClueInstance clue)
	{
		tracked.addClue(clue);
	}

	private ClueInstance clue(int tile, int despawnTick, int itemId)
	{
		ClueInstanceData data = new ClueInstanceData(new ClueInstance(List.of(), itemId));
		data.setX(3200 + tile);
		data.setY(3200);
		data.setDespawnTick(despawnTick);
		return new ClueInstance(data);
	}

	private static Field field(Object target, String name) throws Exception
	{
		Field field = target instanceof ClueDetailsPlugin
			? ClueDetailsPlugin.class.getDeclaredField(name) : target.getClass().getDeclaredField(name);
		field.setAccessible(true);
		return field;
	}

	private static void setField(Object target, String name, Object value) throws Exception
	{
		field(target, name).set(target, value);
	}
}
