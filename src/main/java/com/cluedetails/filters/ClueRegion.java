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
package com.cluedetails.filters;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.runelite.api.coords.WorldPoint;

@RequiredArgsConstructor
@Getter
public enum ClueRegion
{
	MISTHALIN(
		// Overlaps a tad with Al Kharid
		new Zone(48, 49, 50, 54),
		new Zone(51, 52, 52, 54),
		// Overlaps with Morytania slightly
		new Zone(new WorldPoint(3392, 3456, 0), new WorldPoint(3423, 3508, 3)),
		// Soul Wars
		new Zone(32, 43, 36, 46),
		// Fossil island
		new Zone(55, 57, 61, 62)
	),
	KARAMJA(
		new Zone(42, 45, 45, 48),
		new Zone(46, 45, 46, 47),
		new Zone(42, 48, 44, 50),
		new Zone(44, 51),
		new Zone(new WorldPoint(2880, 3136, 0), new WorldPoint(2932, 3199, 3)),
		new Zone(new WorldPoint(2933, 3136, 0), new WorldPoint(2964, 3184, 3))
	),
	ASGARNIA(
		new Zone(45, 50, 47, 54),
		new Zone(new WorldPoint(2944, 3162, 0), new WorldPoint(3053, 3199, 3)),
		new Zone(new WorldPoint(2963, 3099, 0), new WorldPoint(3043, 3161, 3)),
		// Troll Territory
		new Zone(44, 55, 45, 59),
		new Zone(43, 58, 43, 60),
		// White wolf mountain
		new Zone(new WorldPoint(2791, 3493, 0), new WorldPoint(2880, 3520, 3)),
		new Zone(new WorldPoint(2832, 3463, 0), new WorldPoint(2880, 3492, 3)),
		new Zone(new WorldPoint(2841, 3446, 0), new WorldPoint(2880, 3462, 3)),
		new Zone(new WorldPoint(2851, 3442, 0), new WorldPoint(2880, 3445, 3)),
		new Zone(new WorldPoint(2855, 3441, 0), new WorldPoint(2880, 3441, 3)),
		new Zone(new WorldPoint(2858, 3438, 0), new WorldPoint(2880, 3440, 3)),
		new Zone(new WorldPoint(2864, 3433, 0), new WorldPoint(2880, 3437, 3)),
		new Zone(new WorldPoint(2867, 3392, 0), new WorldPoint(2880, 3432, 3)),
		// Entrana, slight overlap with Kandarin
		new Zone(43, 52, 44, 52),
		// Pest Control
		new Zone(41, 40, 41, 41)
	),
	FREMENNIK_PROVINCE(
		new Zone(39, 56, 43, 57),
		new Zone(31, 58, 42, 64)
	),
	KANDARIN(
		new Zone(37, 44, 41, 55),
		new Zone(42, 51, 43, 54),
		// Catherby
		new Zone(new WorldPoint(2816, 3392, 0), new WorldPoint(2864, 3436, 3)),
		new Zone(new WorldPoint(2816, 3437, 0), new WorldPoint(2842, 3462, 3)),
		new Zone(new WorldPoint(2816, 3463, 0), new WorldPoint(2829, 3492, 3)),
		// South CW
		new Zone(36, 47, 36, 48),
		// Pisc
		new Zone(36, 53, 36, 57),
		new Zone(35, 54, 35, 57),
		// Ape Atoll
		new Zone(42, 42, 45, 43)
	),
	KHARIDIAN_DESERT(
		new Zone(49, 41, 55, 48),
		new Zone(51, 49, 52, 51),
		// West of Al Kharid bank
		new Zone(new WorldPoint(3253, 3148, 0), new WorldPoint(3263, 3190, 3)),
		new Zone(53, 49),
		// East of duel arena
		new Zone(new WorldPoint(3392, 3200, 0), new WorldPoint(3423, 3263, 3)),
		// Tempoross
		new Zone(47, 46)
	),
	MORYTANIA(
		new Zone(54, 49, 59, 55),
		new Zone(57, 43, 60, 47),
		new Zone(new WorldPoint(3400, 3264, 0), new WorldPoint(3455, 3462, 3)),
		new Zone(new WorldPoint(3424, 3463, 0), new WorldPoint(3455, 3508, 3)),
		new Zone(new WorldPoint(3396, 3509, 0), new WorldPoint(3455, 3579, 3))
	),
	TIRANNWN(
		new Zone(33, 47, 35, 53),
		new Zone(36, 49, 37, 51)
	),
	WILDERNESS(
		new Zone(46, 55, 52, 61)
	),
	KOUREND(
		new Zone(17, 51, 18, 59),
		new Zone(19, 53, 19, 60),
		new Zone(20, 54, 29, 63),
		new Zone(23, 53, 28, 53)
	),
	VARLAMORE(
		new Zone(19, 45, 29, 51),
		new Zone(20, 52, 22, 53)
	);

	Zone[] zones;

	ClueRegion(Zone... zones)
	{
		this.zones = zones;
	}

}
