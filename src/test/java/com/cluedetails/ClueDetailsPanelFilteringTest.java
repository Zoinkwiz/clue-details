package com.cluedetails;

import com.cluedetails.panels.ClueDetailsParentPanel;
import com.cluedetails.panels.ClueTableModel;
import com.google.gson.Gson;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javax.swing.SwingUtilities;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.ui.components.IconTextField;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class ClueDetailsPanelFilteringTest
{
	@Test
	public void visibleCluesRespectTierRegionMarkedAndSearchFilters() throws Exception
	{
		ClueDetailsConfig config = mock(ClueDetailsConfig.class, CALLS_REAL_METHODS);
		when(config.filterListByTier()).thenReturn(ClueDetailsConfig.ClueTierFilter.BEGINNER);
		when(config.filterListByRegion()).thenReturn(ClueDetailsConfig.ClueRegionFilter.MISTHALIN);
		when(config.onlyShowMarkedClues()).thenReturn(true);
		CluePreferenceManager preferences = mock(CluePreferenceManager.class);
		Clues selected = Clues.CLUES.stream()
			.filter(config.filterListByTier()).filter(config.filterListByRegion()).findFirst().get();
		when(preferences.getHighlightPreference(selected.getClueID())).thenReturn(true);
		ClueDetailsParentPanel panel = createPanel(config, preferences);
		SwingUtilities.invokeAndWait(() ->
		{
			assertEquals(List.of(selected), panel.getVisibleClues());
			try
			{
				IconTextField search = (IconTextField) field(panel, "searchBar");
				search.setText("does-not-match-any-clue");
				assertTrue(panel.getVisibleClues().isEmpty());
				search.setText(selected.getClueID().toString());
				assertEquals(List.of(selected), panel.getVisibleClues());
			}
			catch (Exception e)
			{
				throw new AssertionError(e);
			}
		});
	}

	@Test
	public void cluesListedUnderMultipleRegionsOnlyAppearOnceInSnapshot() throws Exception
	{
		ClueDetailsConfig config = mock(ClueDetailsConfig.class, CALLS_REAL_METHODS);
		when(config.orderListBy()).thenReturn(ClueDetailsConfig.ClueOrdering.REGION);
		Clues shared = Clues.CLUES.stream()
			.filter(clue -> Arrays.stream(config.orderListBy().getSections()).filter(section -> section.test(clue)).count() > 1)
			.findFirst().get();
		ClueDetailsParentPanel panel = createPanel(config, mock(CluePreferenceManager.class));
		SwingUtilities.invokeAndWait(() ->
			assertEquals(1, panel.getVisibleClues().stream().filter(clue -> clue == shared).count()));
	}

	private ClueDetailsParentPanel createPanel(ClueDetailsConfig config, CluePreferenceManager preferences) throws Exception
	{
		ConfigManager configManager = mock(ConfigManager.class);
		String tier = config.filterListByTier().name();
		String region = config.filterListByRegion().name();
		String order = config.orderListBy().name();
		when(configManager.getConfiguration(ClueDetailsConfig.GROUP, "filterListByTier")).thenReturn(tier);
		when(configManager.getConfiguration(ClueDetailsConfig.GROUP, "filterListByRegion")).thenReturn(region);
		when(configManager.getConfiguration(ClueDetailsConfig.GROUP, "orderListBy")).thenReturn(order);
		CountDownLatch populated = new CountDownLatch(1);
		ClueDetailsParentPanel[] result = new ClueDetailsParentPanel[1];
		SwingUtilities.invokeAndWait(() ->
		{
			result[0] = new ClueDetailsParentPanel(configManager, new Gson(), preferences, config,
                    mock(ClueDetailsSharingManager.class), mock(ClueDetailsPlugin.class));
			try
			{
				ClueTableModel model = (ClueTableModel) field(result[0], "clueTableModel");
				model.addTableModelListener(event ->
				{
					if (model.getRowCount() > 0)
					{
						populated.countDown();
					}
				});
			}
			catch (Exception e)
			{
				throw new AssertionError(e);
			}
		});
		assertTrue("Panel did not finish loading", populated.await(5, TimeUnit.SECONDS));
		return result[0];
	}

	private Object field(Object target, String name) throws Exception
	{
		Field field = target.getClass().getDeclaredField(name);
		field.setAccessible(true);
		return field.get(target);
	}
}
