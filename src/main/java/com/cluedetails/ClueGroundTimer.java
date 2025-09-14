package com.cluedetails;

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
