/*
 * Copyright (c) 2024, TheLope <https://github.com/TheLope>
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

import java.util.*;

import lombok.Getter;
import static net.runelite.api.ItemID.TORN_CLUE_SCROLL_PART_1;
import static net.runelite.api.ItemID.TORN_CLUE_SCROLL_PART_2;
import static net.runelite.api.ItemID.TORN_CLUE_SCROLL_PART_3;
import net.runelite.client.util.Text;

@Getter
public class ThreeStepCrypticClue
{
	private final List<Map.Entry<ClueText, Boolean>> clueSteps;
	private String tag;

	public ThreeStepCrypticClue(List<Map.Entry<ClueText, Boolean>> clueSteps, String tag)
	{
		this.clueSteps = clueSteps;
		this.tag = tag;
	}

	public static ThreeStepCrypticClue forText(String text)
	{
		if (text == null)
		{
			return null;
		}

		final String[] split = text.split("<br>\\s*<br>");
		final List<Map.Entry<ClueText, Boolean>> steps = new ArrayList<>(split.length);

		StringBuilder tag = new StringBuilder();

		for (String part : split)
		{
			boolean isDone = part.contains("<str>");
			final String rawText = Text.sanitizeMultilineText(part);

			for (ClueText clue : ClueText.CLUES)
			{
				if (!rawText.equalsIgnoreCase(clue.getText()))
				{
					continue;
				}

				steps.add(new AbstractMap.SimpleEntry<>(clue, isDone));
				tag.append(clue.getTag()).append("<br>");
				break;
			}
		}

		if (steps.isEmpty() || steps.size() < 3)
		{
			return null;
		}

		return new ThreeStepCrypticClue(steps, tag.toString());
	}

	public String makeHint()
	{
		StringBuilder tag = new StringBuilder();

		for (final Map.Entry<ClueText, Boolean> e : clueSteps)
		{
			if (!e.getValue())
			{
				ClueText c = e.getKey();
				tag.append(c.getTag()).append("<br>");
			}
		}
		return tag.toString();
	}

	public void update(final Collection<ClueInstance> clues)
	{
		checkForPart(clues, TORN_CLUE_SCROLL_PART_1, 0);
		checkForPart(clues, TORN_CLUE_SCROLL_PART_2, 1);
		checkForPart(clues, TORN_CLUE_SCROLL_PART_3, 2);
		this.tag = makeHint();
	}

	private void checkForPart(final Collection<ClueInstance> clues, int clueScrollPart, int index)
	{
		// If we have the part then that step is done
		if (clues.stream().anyMatch((clue) -> clue.getItemId() == clueScrollPart))
		{
			final Map.Entry<ClueText, Boolean> entry = clueSteps.get(index);

			if (!entry.getValue())
			{
				entry.setValue(true);
			}
		}
	}
}
