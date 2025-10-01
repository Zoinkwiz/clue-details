/*
 * Copyright (c) 2025, TheLope <https://github.com/TheLope>
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

import com.cluedetails.tools.ClueDetailsWorldMapPoint;
import java.awt.Color;
import java.awt.image.BufferedImage;

import java.util.Map;
import java.util.Set;
import java.util.TimerTask;
import lombok.Getter;
import lombok.Setter;
import net.runelite.api.Client;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.ui.overlay.infobox.InfoBox;

class ClueGroundTimer extends InfoBox
{
	private final Client client;
	private final ClueDetailsPlugin plugin;
	private final ClueDetailsConfig config;
	private final ConfigManager configManager;
	@Setter
	private int despawnTick;
	@Setter
	@Getter
	private Map<ClueInstance, Integer> clueInstancesWithQuantity;
	@Getter
	private final WorldPoint worldPoint;
	@Getter
	private final ClueDetailsWorldMapPoint clueDetailsWorldMapPoint;
	private final StringBuilder stringBuilder = new StringBuilder();
	@Setter
	@Getter
	private boolean notified = false;
	@Setter
	@Getter
	private boolean renotifying = false;

	ClueGroundTimer(
		Client client,
		ClueDetailsPlugin plugin,
		ClueDetailsConfig config,
		ConfigManager configManager,
		int despawnTick,
		WorldPoint worldPoint,
		Map<ClueInstance, Integer> clueInstancesWithQuantityAtWp,
		BufferedImage image
	)
	{
		super(image, plugin);
		this.client = client;
		this.plugin = plugin;
		this.config = config;
		this.configManager = configManager;
		this.despawnTick = despawnTick;
		this.worldPoint = worldPoint;
		this.clueDetailsWorldMapPoint = new ClueDetailsWorldMapPoint(worldPoint, plugin);
		this.clueInstancesWithQuantity = clueInstancesWithQuantityAtWp;
	}

	private int getSecondsLeft()
	{
		int ticksLeft = despawnTick - client.getTickCount();
		int millisLeft = ticksLeft * 600;
		return (int) (millisLeft / 1000L);
	}

	@Override
	public String getText()
	{
		int seconds = getSecondsLeft();
		int minutes = seconds / 60;
		int secs = seconds % 60;
		if (minutes < 10)
		{
			if (minutes < 1)
			{
				return String.format("%ds", secs);
			}
			return String.format("%d:%02d", minutes, secs);
		}
		// Something went wrong when tracking despawn time
		else if (minutes > 61)
		{
			return "?";
		}
		else
		{
			return String.format("%dm", minutes);
		}
	}

	@Override
	public String getName()
	{
		return plugin.getClass().getSimpleName() + "_" + getClass().getSimpleName() + "_";
	}

	@Override
	public String getTooltip()
	{
		stringBuilder.setLength(0);

		for (Map.Entry<ClueInstance, Integer> entry : clueInstancesWithQuantity.entrySet())
		{
			ClueInstance item = entry.getKey();

			if(item.isEnabled(config))
			{
				int quantity = entry.getValue();
				String text = item.getGroundText(plugin, config, configManager, quantity);
				Color color = item.getGroundColor(config, configManager);
				String hexColor = Integer.toHexString(color.getRGB()).substring(2);
				stringBuilder.append("<col=").append(hexColor).append(">").append(text);
				stringBuilder.append("<br>");
			}
		}
		return stringBuilder.toString();
	}

	@Override
	public Color getTextColor()
	{
		return shouldNotify()
			? Color.RED
			: Color.WHITE;
	}

	@Override
	public boolean cull()
	{
		// Remove timers if worldPoint no managed by clueGroundManager
		Set<WorldPoint> worldPoints = plugin.getClueGroundManager().getTrackedWorldPoints();
		if (!worldPoints.contains(worldPoint))
		{
			return true;
		}
		int timeLeft = getSecondsLeft();
		return timeLeft == 0 || timeLeft < 0;
	}

	private boolean activeWorldPoint()
	{
		// Remove timers if worldPoint not managed by clueGroundManager
		Set<WorldPoint> worldPoints = plugin.getClueGroundManager().getTrackedWorldPoints();
		return worldPoints.contains(worldPoint);
	}

	@Override
	public boolean render()
	{
		// Render if any ClueInstance is enabled
		if(!clueInstancesWithQuantity.isEmpty())
		{
			for (ClueInstance clueInstance : clueInstancesWithQuantity.keySet())
			{
				if (clueInstance != null && clueInstance.isEnabled(config))
				{
					return true;
				}
			}
		}
		return false;
	}

	public boolean shouldNotify()
	{
		return activeWorldPoint() && (getSecondsLeft() < config.groundClueTimersNotificationTime());
	}

	public void startRenotification()
	{
		setRenotifying(true);

		// Start renotification timer
		java.util.Timer t = new java.util.Timer();
		TimerTask task = new TimerTask()
		{
			@Override
			public void run()
			{
				setNotified(!shouldNotify());
				setRenotifying(false);
			}
		};
		t.schedule(task, config.groundClueTimersRenotificationTime() * 1000L);
	}
}
