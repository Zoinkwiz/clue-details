/*
 * Copyright (c) 2024, Zoinkwiz <https://github.com/Zoinkwiz>
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
package com.cluedetails;

import com.cluedetails.panels.ClueDetailsParentPanel;
import com.google.inject.Provides;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Collection;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.KeyCode;
import net.runelite.api.MenuAction;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.chatbox.ChatboxPanelManager;
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
	private ClueDetailsWidgetOverlay widgetOverlay;

	@Inject
	private EventBus eventBus;

	@Inject
	private ClientToolbar clientToolbar;

	@Inject
	private ChatboxPanelManager chatboxPanelManager;

	@Inject
	public ClueDetailsSharingManager clueDetailsSharingManager;

	@Inject
	ConfigManager configManager;

	public ClueDetailsParentPanel panel;

	NavigationButton navButton;

	CluePreferenceManager cluePreferenceManager;

	private final Collection<String> configEvents = Arrays.asList("filterListByTier", "filterListByRegion", "orderListBy", "onlyShowMarkedClues");

	@Override
	protected void startUp() throws Exception
	{
		overlayManager.add(infoOverlay);
		eventBus.register(infoOverlay);

		overlayManager.add(widgetOverlay);
		eventBus.register(widgetOverlay);

		cluePreferenceManager = new CluePreferenceManager(configManager);

		final BufferedImage icon = ImageUtil.loadImageResource(ClueDetailsPlugin.class, "/icon.png");

		panel = new ClueDetailsParentPanel(configManager, cluePreferenceManager, config, chatboxPanelManager, clueDetailsSharingManager);
		navButton = NavigationButton.builder()
			.tooltip("Clue Details")
			.icon(icon)
			.priority(7)
			.panel(panel)
			.build();

		if (config.showSidebar())
		{
			clientToolbar.addNavigation(navButton);
		}
	}

	@Override
	protected void shutDown() throws Exception
	{
		overlayManager.remove(infoOverlay);
		eventBus.unregister(infoOverlay);

		overlayManager.remove(widgetOverlay);
		eventBus.unregister(widgetOverlay);

		clientToolbar.removeNavigation(navButton);
	}

	@Provides
	ClueDetailsConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(ClueDetailsConfig.class);
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (event.getGroup().equals("clue-details-highlights"))
		{
			infoOverlay.refreshHighlights();
		}

		if (!event.getGroup().equals(ClueDetailsConfig.class.getAnnotation(ConfigGroup.class).value()))
		{
			return;
		}

		if ("showSidebar".equals(event.getKey()))
		{
			if ("true".equals(event.getNewValue()))
			{
				clientToolbar.addNavigation(navButton);
			}
			else
			{
				clientToolbar.removeNavigation(navButton);
			}
		}

		if (configEvents.contains(event.getKey()))
		{
			panel.refresh();
		}
	}

	@Subscribe
	public void onMenuEntryAdded(MenuEntryAdded event)
	{
		final boolean hotKeyPressed = client.isKeyPressed(KeyCode.KC_SHIFT);
		if (hotKeyPressed && event.getTarget().contains("Clue scroll"))
		{
			if (!infoOverlay.isTakeClue(event.getMenuEntry()) && !infoOverlay.isReadClue(event.getMenuEntry()))
			{
				return;
			}

			int identifier = event.getIdentifier();
			if (infoOverlay.isReadClue(event.getMenuEntry()))
			{
				identifier = event.getMenuEntry().getItemId();
			}

			boolean isMarked = cluePreferenceManager.getPreference(identifier);

			client.createMenuEntry(-1)
				.setOption(isMarked ? "Unmark" : "Mark")
				.setTarget(event.getTarget())
				.setIdentifier(identifier)
				.setType(MenuAction.RUNELITE)
				.onClick(e ->
				{
					boolean currentValue = cluePreferenceManager.getPreference(e.getIdentifier());
					cluePreferenceManager.savePreference(e.getIdentifier(), !currentValue);
					panel.refresh();
				});
		}
	}
}
