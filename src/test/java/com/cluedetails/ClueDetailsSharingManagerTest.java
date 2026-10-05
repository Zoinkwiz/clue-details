package com.cluedetails;

import com.cluedetails.panels.ClueDetailsParentPanel;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import java.lang.reflect.Constructor;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javax.swing.SwingUtilities;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.config.ConfigManager;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class ClueDetailsSharingManagerTest
{
	private final ClueDetailsPlugin plugin = mock(ClueDetailsPlugin.class);
	private final ClueDetailsConfig config = mock(ClueDetailsConfig.class, CALLS_REAL_METHODS);
	private final ConfigManager configManager = mock(ConfigManager.class);
	private final ClueDetailsParentPanel panel = mock(ClueDetailsParentPanel.class);
	private final CountDownLatch finished = new CountDownLatch(1);
	private ClueDetailsSharingManager sharing;

	@Before
	public void setUp() throws Exception
	{
		when(plugin.getPanel()).thenReturn(panel);
		when(plugin.getChatMessageManager()).thenReturn(mock(ChatMessageManager.class));
		doAnswer(invocation -> { finished.countDown(); return null; })
			.when(panel).updateStatusTemporarily(anyString(), anyInt());
		Constructor<ClueDetailsSharingManager> constructor = ClueDetailsSharingManager.class.getDeclaredConstructor(
			ClueDetailsPlugin.class, ClueDetailsConfig.class, Gson.class, ConfigManager.class);
		constructor.setAccessible(true);
		sharing = constructor.newInstance(plugin, config, new Gson(), configManager);
	}

	@Test
	public void resetCapturesFilteredRowsOnSwingThread() throws Exception
	{
		Clues clue = Clues.CLUES.get(0);
		when(panel.isVisible()).thenReturn(true);
		when(panel.getVisibleClues()).thenAnswer(invocation ->
		{
			assertTrue("Filtered rows must be captured on the Swing event thread", SwingUtilities.isEventDispatchThread());
			return List.of(clue);
		});
		SwingUtilities.invokeAndWait(() -> sharing.resetClueDetails(true, false, false, false));
		assertTrue(finished.await(5, TimeUnit.SECONDS));
		verify(configManager).unsetConfiguration("clue-details-text", clue.getClueID().toString());
		verifyNoMoreInteractions(configManager);
	}

	@Test
	public void hidingPanelDoesNotExpandResetScope() throws Exception
	{
		Clues clue = Clues.CLUES.get(0);
		when(panel.isVisible()).thenReturn(false);
		when(panel.getVisibleClues()).thenReturn(List.of(clue));
		SwingUtilities.invokeAndWait(() -> sharing.resetClueDetails(true, false, false, false));
		assertTrue(finished.await(5, TimeUnit.SECONDS));
		verify(configManager).unsetConfiguration("clue-details-text", clue.getClueID().toString());
		verifyNoMoreInteractions(configManager);
	}

	@Test
	public void exportCapturesFilteredRowsOnSwingThread() throws Exception
	{
		Clues clue = Clues.CLUES.get(0);
		when(panel.isVisible()).thenReturn(true);
		when(panel.getVisibleClues()).thenAnswer(invocation ->
		{
			assertTrue("Filtered rows must be captured on the Swing event thread", SwingUtilities.isEventDispatchThread());
			return List.of(clue);
		});
		SwingUtilities.invokeAndWait(() -> sharing.exportClueDetails(true, false, false, false));
		assertTrue(finished.await(5, TimeUnit.SECONDS));
		verify(configManager).getConfiguration("clue-details-text", clue.getClueID().toString());
		verifyNoMoreInteractions(configManager);
	}

	@Test
	public void emptyFilterDoesNotResetAnything() throws Exception
	{
		when(panel.getVisibleClues()).thenReturn(List.of());
		SwingUtilities.invokeAndWait(() -> sharing.resetClueDetails(true, true, true, true));
		assertTrue(finished.await(5, TimeUnit.SECONDS));
		verifyNoInteractions(configManager);
	}

	@Test
	public void acceptsSingleObjectAndArrayImports()
	{
		String object = "{\"id\":2677,\"text\":\"Custom detail\",\"itemIds\":[952]}";
		List<ClueIdToDetails> single = ClueDetailsSharingManager.parseClueDetails(new Gson(), object);
		List<ClueIdToDetails> array = ClueDetailsSharingManager.parseClueDetails(new Gson(), "[" + object + "]");
		assertEquals(1, single.size());
		assertEquals(2677, single.get(0).getId());
		assertEquals("Custom detail", single.get(0).getText());
		assertEquals(List.of(952), single.get(0).getItemIds());
		assertEquals(single, array);
		assertTrue(ClueDetailsSharingManager.parseClueDetails(new Gson(), "[]").isEmpty());
	}

	@Test
	public void rejectsNullAndMalformedImportsBeforeAnyWrites()
	{
		for (String json : List.of("null", "[null]", "[{\"id\":2677,\"text\":\"Valid\"},null]", "42", "[42]", "{broken"))
		{
			try
			{
				ClueDetailsSharingManager.parseClueDetails(new Gson(), json);
				fail("Accepted invalid import: " + json);
			}
			catch (JsonSyntaxException expected)
			{
				// No partial result can reach the import worker.
			}
		}
	}
}
