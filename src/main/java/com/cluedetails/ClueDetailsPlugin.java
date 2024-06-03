package com.cluedetails;

import com.google.inject.Provides;
import java.awt.image.BufferedImage;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.ImageUtil;

@Slf4j
@PluginDescriptor(
	name = "Clue Details",
	description = "Provides details and highlighting for clues on the floor",
	tags = { "clue", "overlay" }
)
public class ClueDetailsPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private ClueDetailsConfig config;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private ClueDetailsOverlay infoOverlay;

	@Inject
	private EventBus eventBus;

	@Inject
	private ClientToolbar clientToolbar;

	@Inject
	ConfigManager configManager;

	ClueDetailsPanel panel;

	NavigationButton navButton;

	@Override
	protected void startUp() throws Exception
	{
		overlayManager.add(infoOverlay);
		eventBus.register(infoOverlay);

		final BufferedImage icon = ImageUtil.loadImageResource(ClueDetailsPlugin.class, "/icon.png");

		panel = new ClueDetailsPanel(configManager, config);
		navButton = NavigationButton.builder()
			.tooltip("Clue Details")
			.icon(icon)
			.priority(7)
			.panel(panel)
			.build();

		clientToolbar.addNavigation(navButton);
	}

	@Override
	protected void shutDown() throws Exception
	{
		overlayManager.remove(infoOverlay);
		eventBus.unregister(infoOverlay);

		clientToolbar.removeNavigation(navButton);
	}

	@Provides
	ClueDetailsConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(ClueDetailsConfig.class);
	}
}
