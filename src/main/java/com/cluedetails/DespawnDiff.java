/*
 * Copyright (c) 2024, Zoinkwiz <https://www.github.com/Zoinkwiz>
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

import lombok.Getter;
import net.runelite.api.TileItem;

@Getter
public class DespawnDiff
{
	private final ClueInstance clue1;
	private final ClueInstance clue2;
	private final TileItem tileItem1;
	private final TileItem tileItem2;
	private final int despawnDiff;

	public DespawnDiff(ClueInstance clue1, ClueInstance clue2)
	{
		this.clue1 = clue1;
		this.clue2 = clue2;
		this.tileItem1 = null;
		this.tileItem2 = null;
		this.despawnDiff = clue2.getDespawnTick() - clue1.getDespawnTick();
	}

	public DespawnDiff(TileItem tileItem1, TileItem tileItem2)
	{
		this.clue1 = null;
		this.clue2 = null;
		this.tileItem1 = tileItem1;
		this.tileItem2 = tileItem2;
		this.despawnDiff = tileItem1.getDespawnTime() - tileItem2.getDespawnTime();
	}

	public int getDespawn1()
	{
		if (clue1 != null)
		{
			return clue1.getDespawnTick();
		}
		return tileItem1.getDespawnTime();
	}

	public int getDespawn2()
	{
		if (clue2 != null)
		{
			return clue2.getDespawnTick();
		}
		return tileItem2.getDespawnTime();
	}
}
