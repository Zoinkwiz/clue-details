/*
 * Copyright (c) 2024, Zoinkwiz <https://github.com/Zoinkwiz>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *	  list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *	  this list of conditions and the following disclaimer in the documentation
 *	  and/or other materials provided with the distribution.
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

import com.cluedetails.filters.ClueTier;
import java.awt.Color;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import net.runelite.api.ItemID;
import net.runelite.api.TileItem;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.util.QuantityFormatter;
import org.apache.commons.text.WordUtils;

@Data
public class ClueInstance
{
	@Setter
	private List<Integer> clueIds; // Fake ID from ClueText
	private final int itemId; // Clue item ID
	private final WorldPoint location; // Null if in inventory

	@Getter
	private final Integer timeToDespawnFromDataInTicks;
	private TileItem tileItem;

	// Constructor for clues from config
	public ClueInstance(ClueInstanceData data)
	{
		this.clueIds = data.getClueIds();
		this.itemId = data.getItemId();
		this.location = data.getLocation();
		// if had on then turned off in same session, we don't know what happened in meantime.
		// Ticks go forward even when logged into other game modes. For simplicity we assume when
		// Loaded we just are starting from the exact same despawn time remaining.
		this.timeToDespawnFromDataInTicks = data.getDespawnTick();
	}

	// Constructor for inventory clues from inventory changed event
	public ClueInstance(List<Integer> clueIds, int itemId)
	{
		this.clueIds = clueIds;
		this.itemId = itemId;
		this.location = null;
		this.timeToDespawnFromDataInTicks = -1;
	}

	// Constructor for ground clues
	public ClueInstance(List<Integer> clueIds, int itemId, WorldPoint location, TileItem tileItem, int currentTick)
	{
		this.clueIds = clueIds;
		this.itemId = itemId;
		this.location = location;
		this.tileItem = tileItem;
		this.timeToDespawnFromDataInTicks = tileItem.getDespawnTime() - currentTick;
	}

	public List<Integer> getClueIds()
	{
		if (clueIds.isEmpty() && !(itemId == ItemID.CLUE_SCROLL_BEGINNER || itemId == ItemID.CLUE_SCROLL_MASTER))
		{
			return Collections.singletonList(itemId);
		}
		return clueIds;
	}

	public ClueTier getTier()
	{
		Clues clue;

		if (clueIds.isEmpty())
		{
			clue = Clues.forItemId(itemId);
		}
		else
		{
			clue = Clues.forClueId(getClueIds().get(0));
		}

		if (clue == null)
		{
			return null;
		}
		return clue.getClueTier();
	}

	public String getGroundText(ClueDetailsPlugin plugin, ClueDetailsConfig config, ConfigManager configManager, int quantity)
	{
		StringBuilder itemStringBuilder = new StringBuilder();
		List<Integer> clueIds = this.getClueIds();

		if (clueIds.isEmpty())
		{
			itemStringBuilder.append(this.getItemName(plugin));
		}
		else
		{
			int clueId = this.getClueIds().get(0);
			Clues clueDetails = Clues.forClueIdFiltered(clueId);

			if (clueDetails == null)
			{
				return null;
			}

			String clueText;
			if (config.changeGroundClueText() && !config.collapseGroundCluesByTier())
			{
				if (clueIds.size() > 1)
				{
					clueText = "Three-step (master)";
				}
				else
				{
					clueText = clueDetails.getDetail(configManager);
				}
			}
			else
			{
				clueText = WordUtils.capitalizeFully(clueDetails.getClueTier().toString().replace("_", " "));
			}

			itemStringBuilder.append(clueText);
		}

		if ((config.collapseGroundClues() || config.collapseGroundCluesByTier()) && quantity > 1)
		{
			itemStringBuilder.append(" (")
				.append(QuantityFormatter.quantityToStackSize(quantity))
				.append(')');
		}
		return itemStringBuilder.toString();
	}

	public Color getGroundColor(ClueDetailsConfig config, ConfigManager configManager)
	{
		Color color = Color.WHITE;
		List<Integer> clueIds = this.getClueIds();

		if (!clueIds.isEmpty())
		{
			int clueId = this.getClueIds().get(0);
			Clues clueDetails = Clues.forClueIdFiltered(clueId);

			if (clueDetails == null)
			{
				return color;
			}

			if (config.colorGroundClues())
			{
				color = clueDetails.getDetailColor(configManager);
			}
		}
		return color;
	}

	public int getDespawnTick(int currentTick)
	{
		if (tileItem != null)
		{
			return tileItem.getDespawnTime();
		}
		return currentTick + timeToDespawnFromDataInTicks;
	}

	// Theory: This should mean that tiles we've seen have TileItem, and the actual despawn is used for ALL items on that tile
	// For tiles we've not seen this session, all items on it should have no TileItem, and thus we'll keep the same consistent tick diff
	public int getTicksToDespawnConsideringTileItem(int currentTick)
	{
		if (tileItem != null)
		{
			return tileItem.getDespawnTime() - currentTick;
		}
		return timeToDespawnFromDataInTicks == null ? -1 : timeToDespawnFromDataInTicks;
	}

	public String getCombinedClueText(ClueDetailsPlugin plugin, ConfigManager configManager, boolean showColor, boolean isFloorText)
	{
		StringBuilder returnText = new StringBuilder();
		boolean isFirst = true;
		for (Integer clueId : getClueIds())
		{
			Clues cluePart = Clues.forClueId(clueId);
			if (cluePart == null) continue;
			if (isFirst)
			{
				isFirst = false;
			}
			else
			{
				returnText.append("<br>");
			}

			if (showColor)
			{
				Color color = cluePart.getDetailColor(configManager);

				// Only change floor text color if it's not the default
				if (!(isFloorText && color == Color.WHITE))
				{
					String hexColor = Integer.toHexString(color.getRGB()).substring(2);
					returnText.append("<col=").append(hexColor).append(">");
				}
			}

			returnText.append(cluePart.getDetail(configManager));
		}
		if (returnText.length() == 0) return getItemName(plugin);
		return returnText.toString();
	}

	public String getItemName(ClueDetailsPlugin plugin)
	{
		return plugin.getItemManager().getItemComposition(itemId).getName();
	}

	public boolean isEnabled(ClueDetailsConfig config)
	{
		if (itemId == ItemID.CLUE_SCROLL_BEGINNER)
		{
			return config.beginnerDetails();
		}
		else if (getTier() == ClueTier.EASY)
		{
			return config.easyDetails();
		}
		else if (getTier() == ClueTier.MEDIUM)
		{
			return config.mediumDetails();
		}
		else if (getTier() == ClueTier.HARD)
		{
			return config.hardDetails();
		}
		else if (getTier() == ClueTier.ELITE)
		{
			return config.eliteDetails();
		}
		else if (itemId == ItemID.CLUE_SCROLL_MASTER)
		{
			return config.masterDetails();
		}
		else return getTier() != null;
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (!(o instanceof ClueInstance)) return false;
		ClueInstance clueInstance = (ClueInstance) o;

		int diff1;
		int diff2;
		if (tileItem != null && clueInstance.tileItem != null)
		{
			diff1 = tileItem.getDespawnTime();
			diff2 = clueInstance.tileItem.getDespawnTime();
		}
		else if (tileItem == null && clueInstance.tileItem == null)
		{
			diff1 = timeToDespawnFromDataInTicks;
			diff2 = clueInstance.timeToDespawnFromDataInTicks;
		}
		else
		{
			return false;
		}
		// Should this really be considering it equal without consideration for the clueIds?
		return itemId == clueInstance.itemId && diff1 == diff2 && location.equals(clueInstance.location);
	}

	@Override
	public int hashCode()
	{
		int despawnTime = tileItem != null ? tileItem.getDespawnTime() : timeToDespawnFromDataInTicks;
		return Objects.hash(itemId, despawnTime, location);
	}

	@Override
	public String toString()
	{
		int despawnTime = tileItem != null ? tileItem.getDespawnTime() : timeToDespawnFromDataInTicks;
		return "ClueInstance{" + "itemId=" + itemId + ", despawnTick=" + despawnTime  + ", worldPoint=" + location + "}";
	}
}
