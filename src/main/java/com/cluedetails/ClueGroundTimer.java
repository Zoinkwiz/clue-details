package com.cluedetails;

import java.awt.Color;
import java.awt.image.BufferedImage;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.ui.overlay.infobox.Timer;

class ClueGroundTimer extends Timer
{
	private final ClueDetailsPlugin plugin;
	private final ClueDetailsConfig config;
	private final ConfigManager configManager;
	@Setter
	@Getter
	private Map<ClueInstance, Integer> clueInstancesWithQuantity;
	@Getter
	private final WorldPoint worldPoint;
	private final StringBuilder stringBuilder = new StringBuilder();
	@Setter
	@Getter
	private boolean notified = false;

	ClueGroundTimer(
		ClueDetailsPlugin plugin,
		ClueDetailsConfig config,
		ConfigManager configManager,
		Duration duration,
		WorldPoint worldPoint,
		Map<ClueInstance, Integer> clueInstancesWithQuantityAtWp,
		BufferedImage image
	)
	{
		super(duration.toMillis(), ChronoUnit.MILLIS, image, plugin);
		this.plugin = plugin;
		this.config = config;
		this.configManager = configManager;
		this.worldPoint = worldPoint;
		this.clueInstancesWithQuantity = clueInstancesWithQuantityAtWp;
	}

	@Override
	public String getText()
	{
		Duration timeLeft = Duration.between(Instant.now(), this.getEndTime());
		int seconds = (int)(timeLeft.toMillis() / 1000L);
		int minutes = seconds / 60;
		int secs = seconds % 60;
		if (minutes < 1)
		{
			return String.format("%ds", secs);
		}
		return String.format("%dm", minutes);
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
		return getDuration().compareTo(Duration.ofSeconds(config.groundClueTimersNotificationTime())) < 0
			? Color.RED
			: Color.WHITE;
	}

	@Override
	public boolean cull()
	{
		// Remove timers if worldPoint no managed by clueGroundManager
		Set<WorldPoint> worldPoints = plugin.getClueGroundManager().getGroundClues().keySet();
		if (!worldPoints.contains(worldPoint))
		{
			return true;
		}
		Duration timeLeft = Duration.between(Instant.now(), getEndTime());
		return timeLeft.isZero() || timeLeft.isNegative();
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
}
