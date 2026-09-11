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
		new Zone(55, 57, 61, 62),
		// Edgeville Dungeon
		new Zone(48, 154, 51, 155),
		new Zone(47, 154),
		new Zone(48, 153),
		new Zone(48, 156),
		// Varrock West Bank basement
		new Zone(new WorldPoint(3187, 9818, 0), new WorldPoint(3197, 9835, 0)),
		// Lumbridge Swamp Caves + Wizards Tower Basement
		new Zone(48 , 149, 50, 149),
		// Lumbridge Castle basement
		new Zone(50, 150),
		// Dorgesh-Kaan
		new Zone(42, 82, 42, 83),
		// Tears of Guthix
		new Zone(50, 148),
		// Stronghold of Security
		new Zone(29, 81), // Level 1
		new Zone(31, 81), // Level 2
		new Zone(33, 82), // Level 3
		new Zone(36, 81)  // Level 4
	),
	KARAMJA(
		new Zone(42, 45, 45, 48),
		new Zone(46, 45, 46, 47),
		new Zone(42, 48, 44, 50),
		new Zone(44, 51),
		new Zone(new WorldPoint(2880, 3136, 0), new WorldPoint(2932, 3199, 3)),
		new Zone(new WorldPoint(2933, 3136, 0), new WorldPoint(2964, 3184, 3)),
		// Mor Ul Rek
		new Zone(37, 79, 39, 80),
		// Inside volcano
		new Zone(44, 149, 44, 150)
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
		new Zone(41, 40, 41, 41),
		// Dwarven Mine + Taverley Dungeon
		new Zone(43, 151, 47,153),
		// Burthorpe Games Room basement
		new Zone(34, 77),
		// Rogues Den
		new Zone(47, 77),
		// Fountain of Heroes
		new Zone(45, 154),
		// Cerb
		new Zone(19, 19, 21, 20),
		// GWD
		new Zone(44, 82, 45, 83)
	),
	FREMENNIK_PROVINCE(
		new Zone(39, 56, 43, 57),
		new Zone(31, 58, 42, 64),
		// Keldagrim
		new Zone(44, 158, 45, 159),
		// Weiss
		new Zone(44, 61, 45, 61),
		// Slayer cave
		new Zone(42, 155, 43, 156),
		// Basilisk knight place
		new Zone(37, 162, 38, 163),
		// Under misc (Runolf)
		new Zone(39, 160, 40, 160),
		// DK's ladder area
		new Zone(29, 68, 30, 68),
		// KGP
		new Zone(41, 162),
		// Waterbirth dungeon
		new Zone(38, 158, 39, 159),
		// Brine rat cavern
		new Zone(42, 158)
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
		new Zone(42, 42, 45, 43),
		// Sinclair Mansion + East
		new Zone(41, 55, 42, 55),
		// Brimstail's cave
		new Zone(37, 153),
		// Realm of the Fisher King
		new Zone(41, 73, 43, 73),
		// Yanille Agility Dungeon
		new Zone(40, 148, 40, 149),
		// Elemental Workshop
		new Zone(42, 154),
		// Ancient Cavern
		new Zone(25, 83, 27, 83),
		// Shadow Dungeon
		new Zone(41, 79, 42, 79),
		// Mogre camp
		new Zone(46, 148),
		// Mourner Tunnels + Death altar
		new Zone(29, 72, 31, 72),
		// Iban's Temple
		new Zone(31, 73),
		// Death Altar
		new Zone(34, 75),
		// Stronghold Slayer Cave
		new Zone(37, 152, 38, 153),
		// Witchaven Dungeon
		new Zone(42, 151),
		// Kraken Cove
		new Zone(35, 156),
		// Corsair Cove Dungeon + Myth's Guild Basement
		new Zone(29, 140, 32, 141)
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
		new Zone(47, 46),
		// Agility Pyramid
		new Zone(47, 73),
		// Pyramid Plunder
		new Zone(30, 69),
		// Genie crack
		new Zone(52, 145),
		// Kalphite Cave
		new Zone(51, 148, 52, 148),
		new Zone(51, 149)
	),
	MORYTANIA(
		new Zone(54, 49, 59, 55),
		new Zone(57, 43, 60, 47),
		new Zone(new WorldPoint(3400, 3264, 0), new WorldPoint(3455, 3462, 3)),
		new Zone(new WorldPoint(3424, 3463, 0), new WorldPoint(3455, 3508, 3)),
		new Zone(new WorldPoint(3396, 3509, 0), new WorldPoint(3455, 3579, 3)),
		// Some overlap with Desert/Misthalin/Wilderness
		new Zone(53, 50, 53, 56),
		// Under Paterdomus
		new Zone(53, 154),
		// Barrows
		new Zone(55, 151),
		// Shade Catacombs
		new Zone(54, 151),
		// Tarn's Lair
		new Zone(49, 71, 49, 72),
		// Abandoned/Haunted Mine
		new Zone(42, 69, 43, 71),
		// Meiyerditch Labs
		new Zone(55, 153),
		new Zone(55, 152, 56, 152),
		new Zone(56, 151),
		// Slayer Tower Basement
		new Zone(53, 155)
	),
	TIRANNWN(
		new Zone(33, 47, 35, 53),
		new Zone(36, 49, 37, 51),
		// Inside prif
		new Zone(50, 94, 51, 95),
		// Iorwerth Dungeon
		new Zone(49, 193, 50, 194),
		// Prif mine
		new Zone(51, 194)
	),
	WILDERNESS(
		new Zone(46, 55, 52, 61),
		// KBD lair
		new Zone(35, 73),
		// Wilderness Slayer Cave
		new Zone(52, 157, 53, 158),
		// Wildy GWD
		new Zone(47, 158)
	),
	KOUREND(
		new Zone(17, 51, 18, 59),
		new Zone(19, 53, 19, 60),
		new Zone(20, 54, 29, 63),
		new Zone(23, 53, 28, 53),
		// Forthos dungeon
		new Zone(27, 154, 29, 156),
		// Catas
		new Zone(25, 156, 26, 157),
		// Karuulum Dungeon (overlaps a basement in 21 159)
		new Zone(19, 159, 21, 160),
		new Zone(20, 158)
	),
	VARLAMORE(
		new Zone(18, 47, 22, 53),
		new Zone(23, 45, 28, 49),
		new Zone(23, 50, 26, 52),
		new Zone(19, 46),
		// Inaccessible?? west of colo
		new Zone(29, 48),
		// Aldarin
		new Zone(20, 44, 22, 46),
		// East Stranglewood
		new Zone(17, 51, 17, 53),
		// Colo basement
		new Zone(28, 148, 28, 148),
		// Cam Torum + moons
		new Zone(21, 148, 23, 152),
		// Ruins of Tapoyauik
		new Zone(24, 150, 26, 150)
	);

	Zone[] zones;

	ClueRegion(Zone... zones)
	{
		this.zones = zones;
	}

}
