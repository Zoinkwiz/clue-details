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

import com.cluedetails.filters.ClueTier;
import com.cluedetails.filters.OrRequirement;
import com.google.common.collect.ImmutableList;
import java.util.List;
import lombok.Getter;
import net.runelite.api.ItemID;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.widgets.InterfaceID;
import net.runelite.client.plugins.cluescrolls.clues.MapClue;

@Getter
public class BeginnerMasterClues
{
	static final List<BeginnerMasterClues> CLUES = ImmutableList.of(
		new BeginnerMasterClues(ItemID.CLUE_SCROLL_BEGINNER, ClueTier.BEGINNER, BeginnerMasterClueText.BEGINNER_ANAGRAM_0, "Ranael.", List.of(new WorldPoint(3316, 3163, 0))),
		new BeginnerMasterClues(ItemID.CLUE_SCROLL_BEGINNER, ClueTier.BEGINNER, BeginnerMasterClueText.BEGINNER_ANAGRAM_1, "Archmage Sedridor.", List.of(new WorldPoint(3109, 3160, 0))),
		new BeginnerMasterClues(ItemID.CLUE_SCROLL_BEGINNER, ClueTier.BEGINNER, BeginnerMasterClueText.BEGINNER_ANAGRAM_2, "Apothecary.", List.of(new WorldPoint(3195, 3404, 0))),
		new BeginnerMasterClues(ItemID.CLUE_SCROLL_BEGINNER, ClueTier.BEGINNER, BeginnerMasterClueText.BEGINNER_ANAGRAM_3, "Doric.", List.of(new WorldPoint(2952, 3452, 0))),
		new BeginnerMasterClues(ItemID.CLUE_SCROLL_BEGINNER, ClueTier.BEGINNER, BeginnerMasterClueText.BEGINNER_ANAGRAM_4, "Brian.", List.of(new WorldPoint(3027, 3249, 0))),
		new BeginnerMasterClues(ItemID.CLUE_SCROLL_BEGINNER, ClueTier.BEGINNER, BeginnerMasterClueText.BEGINNER_ANAGRAM_5, "Veronica.", List.of(new WorldPoint(3110, 3330, 0))),
		new BeginnerMasterClues(ItemID.CLUE_SCROLL_BEGINNER, ClueTier.BEGINNER, BeginnerMasterClueText.BEGINNER_ANAGRAM_6, "Gertrude.", List.of(new WorldPoint(3150, 3409, 0))),
		new BeginnerMasterClues(ItemID.CLUE_SCROLL_BEGINNER, ClueTier.BEGINNER, BeginnerMasterClueText.BEGINNER_ANAGRAM_7, "Hairdresser.", List.of(new WorldPoint(2945, 3380, 0))),
		new BeginnerMasterClues(ItemID.CLUE_SCROLL_BEGINNER, ClueTier.BEGINNER, BeginnerMasterClueText.BEGINNER_ANAGRAM_8, "Fortunato.", List.of(new WorldPoint(3085, 3252, 0))),
		new BeginnerMasterClues(ItemID.CLUE_SCROLL_BEGINNER, ClueTier.BEGINNER, BeginnerMasterClueText.BEGINNER_CRYPTIC_0, "Talk to Hans.", List.of(new WorldPoint(3212, 3219, 0))),
		new BeginnerMasterClues(ItemID.CLUE_SCROLL_BEGINNER, ClueTier.BEGINNER, BeginnerMasterClueText.BEGINNER_CRYPTIC_1, "Speak to Reldo.", List.of(new WorldPoint(3210, 3495, 0))),
		new BeginnerMasterClues(ItemID.CLUE_SCROLL_BEGINNER, ClueTier.BEGINNER, BeginnerMasterClueText.BEGINNER_CRYPTIC_2, "Talk to the Cook in Lumbridge.", List.of(new WorldPoint(3209, 3214, 0))),
		new BeginnerMasterClues(ItemID.CLUE_SCROLL_BEGINNER, ClueTier.BEGINNER, BeginnerMasterClueText.BEGINNER_CRYPTIC_3, "Talk to Hunding.", List.of(new WorldPoint(3097, 3429, 2))),
		new BeginnerMasterClues(ItemID.CLUE_SCROLL_BEGINNER, ClueTier.BEGINNER, BeginnerMasterClueText.BEGINNER_CRYPTIC_4, "Talk to Charlie the Tramp.", List.of(new WorldPoint(3206, 3390, 0))),
		new BeginnerMasterClues(ItemID.CLUE_SCROLL_BEGINNER, ClueTier.BEGINNER, BeginnerMasterClueText.BEGINNER_CRYPTIC_5, "Talk to Shantay.", List.of(new WorldPoint(3304, 3124, 0))),
		new BeginnerMasterClues(ItemID.CLUE_SCROLL_BEGINNER, ClueTier.BEGINNER, BeginnerMasterClueText.BEGINNER_EMOTE_0, "Emote at Aris.", List.of(new WorldPoint(3206, 3422, 0))),
		new BeginnerMasterClues(ItemID.CLUE_SCROLL_BEGINNER, ClueTier.BEGINNER, BeginnerMasterClueText.BEGINNER_EMOTE_1, "Emote to Brugsen Bursen.", List.of(new WorldPoint(3165, 3478, 0))),
		new BeginnerMasterClues(ItemID.CLUE_SCROLL_BEGINNER, ClueTier.BEGINNER, BeginnerMasterClueText.BEGINNER_EMOTE_2, "Emote at Iffie Nitter.", List.of(new WorldPoint(3209, 3416, 0))),
		new BeginnerMasterClues(ItemID.CLUE_SCROLL_BEGINNER, ClueTier.BEGINNER, BeginnerMasterClueText.BEGINNER_EMOTE_3, "Emote at Bob's Brilliant Axes.", List.of(new WorldPoint(3233, 3200, 0))),
		new BeginnerMasterClues(ItemID.CLUE_SCROLL_BEGINNER, ClueTier.BEGINNER, BeginnerMasterClueText.BEGINNER_EMOTE_4, "Emote at Al Kharid mine.", List.of(new WorldPoint(3298, 3293, 0))),
		new BeginnerMasterClues(ItemID.CLUE_SCROLL_BEGINNER, ClueTier.BEGINNER, BeginnerMasterClueText.BEGINNER_EMOTE_5, "Emote at Flynn's Mace Shop.", List.of(new WorldPoint(2948,  3389, 0))),
		new BeginnerMasterClues(InterfaceID.CLUE_BEGINNER_MAP_CHAMPIONS_GUILD, ClueTier.BEGINNER, MapClue.CHAMPIONS_GUILD, "West of the Champions' Guild.", List.of(new WorldPoint(3166, 3361, 0))),
		new BeginnerMasterClues(InterfaceID.CLUE_BEGINNER_MAP_VARROCK_EAST_MINE, ClueTier.BEGINNER, MapClue.VARROCK_EAST_MINE, "Outside Varrock East Mine.", List.of(new WorldPoint(3290, 3374, 0))),
		new BeginnerMasterClues(InterfaceID.CLUE_BEGINNER_MAP_DYANOR, ClueTier.BEGINNER, MapClue.SOUTH_OF_DRAYNOR_BANK, "South of Draynor Village Bank.", List.of(new WorldPoint(3093, 3226, 0))),
		new BeginnerMasterClues(InterfaceID.CLUE_BEGINNER_MAP_NORTH_OF_FALADOR, ClueTier.BEGINNER, MapClue.STANDING_STONES, "Falador standing stones.", List.of(new WorldPoint(3043, 3398, 0))),
		new BeginnerMasterClues(InterfaceID.CLUE_BEGINNER_MAP_WIZARDS_TOWER, ClueTier.BEGINNER, MapClue.WIZARDS_TOWER_DIS, "Fairy ring DIS.", List.of(new WorldPoint(3110, 3152, 0))),
		new BeginnerMasterClues(ItemID.CLUE_SCROLL_MASTER, ClueTier.MASTER, BeginnerMasterClueText.MASTER_ANAGRAM_0, "Snowflake Weiss.", List.of(new WorldPoint(2872, 3935, 0))),
		new BeginnerMasterClues(ItemID.CLUE_SCROLL_MASTER, ClueTier.MASTER, BeginnerMasterClueText.MASTER_ANAGRAM_1, "Captain Bruce Great Kourend.", List.of(new WorldPoint(1530, 3567, 0))),
		new BeginnerMasterClues(ItemID.CLUE_SCROLL_MASTER, ClueTier.MASTER, BeginnerMasterClueText.MASTER_ANAGRAM_2, "Sacrifice Zul-Andra.", List.of(new WorldPoint(2210, 3056, 0))),
		new BeginnerMasterClues(ItemID.CLUE_SCROLL_MASTER, ClueTier.MASTER, BeginnerMasterClueText.MASTER_ANAGRAM_3, "Edward Inside Rogues' Castle.", List.of(new WorldPoint(3283, 3934, 0))),
		new BeginnerMasterClues(ItemID.CLUE_SCROLL_MASTER, ClueTier.MASTER, BeginnerMasterClueText.MASTER_ANAGRAM_4, "Mandrith Outside the Wilderness Resource Area.", List.of(new WorldPoint(3184, 3945, 0))),
		new BeginnerMasterClues(ItemID.CLUE_SCROLL_MASTER, ClueTier.MASTER, BeginnerMasterClueText.MASTER_ANAGRAM_5, "Dugopul Graveyard on Ape Atoll.", List.of(new WorldPoint(2801, 2745, 0))),
		new BeginnerMasterClues(ItemID.CLUE_SCROLL_MASTER, ClueTier.MASTER, BeginnerMasterClueText.MASTER_ANAGRAM_6, "Runolf Miscellania and Etceteria Dungeon.", List.of(new WorldPoint(2508, 10258, 0))),
		new BeginnerMasterClues(ItemID.CLUE_SCROLL_MASTER, ClueTier.MASTER, BeginnerMasterClueText.MASTER_ANAGRAM_7, "Immenizz inside Puro-Puro.", List.of(new WorldPoint(2592, 4319, 0))),
		new BeginnerMasterClues(ItemID.CLUE_SCROLL_MASTER, ClueTier.MASTER, BeginnerMasterClueText.MASTER_ANAGRAM_8, "Luminata East of Burgh de Rott.", List.of(new WorldPoint(3505, 3236, 0))),
		new BeginnerMasterClues(ItemID.CLUE_SCROLL_MASTER, ClueTier.MASTER, BeginnerMasterClueText.MASTER_ANAGRAM_9, "Old Man Ral Meiyerditch.", List.of(new WorldPoint(3607, 3208, 0))),
		new BeginnerMasterClues(ItemID.CLUE_SCROLL_MASTER, ClueTier.MASTER, BeginnerMasterClueText.MASTER_ANAGRAM_10, "Radimus Erkle Legends' Guild.", List.of(new WorldPoint(2726, 3368, 0))),
		new BeginnerMasterClues(ItemID.CLUE_SCROLL_MASTER, ClueTier.MASTER, BeginnerMasterClueText.MASTER_ANAGRAM_11, "Primula Myths' Guild.", List.of(new WorldPoint(2454, 2853, 1))),
		new BeginnerMasterClues(ItemID.CLUE_SCROLL_MASTER, ClueTier.MASTER, BeginnerMasterClueText.MASTER_ANAGRAM_12, "Goreu Lletya.", List.of(new WorldPoint(2336, 3161, 0))),
		new BeginnerMasterClues(ItemID.CLUE_SCROLL_MASTER, ClueTier.MASTER, BeginnerMasterClueText.MASTER_ANAGRAM_13, "Guildmaster Lars Woodcutting Guild.", List.of(new WorldPoint(1652, 3499, 0))),
		new BeginnerMasterClues(ItemID.CLUE_SCROLL_MASTER, ClueTier.MASTER, BeginnerMasterClueText.MASTER_ANAGRAM_14, "Wingstone near Agility Pyramid.", List.of(new WorldPoint(3381, 2891, 0))),
		new BeginnerMasterClues(ItemID.CLUE_SCROLL_MASTER, ClueTier.MASTER, BeginnerMasterClueText.MASTER_ANAGRAM_15, "New Recruit Tony Graveyard of Heroes.", List.of(new WorldPoint(1502, 3554, 0))),
		new BeginnerMasterClues(ItemID.CLUE_SCROLL_MASTER, ClueTier.MASTER, BeginnerMasterClueText.MASTER_COORDINATE_0, "South of the Iorwerth Camp.", List.of(new WorldPoint(2178, 3209, 0))),
		new BeginnerMasterClues(ItemID.CLUE_SCROLL_MASTER, ClueTier.MASTER, BeginnerMasterClueText.MASTER_COORDINATE_1, "South of Port Tyras.", List.of(new WorldPoint(2155, 3100, 0))),
		new BeginnerMasterClues(ItemID.CLUE_SCROLL_MASTER, ClueTier.MASTER, BeginnerMasterClueText.MASTER_COORDINATE_2, "Fairy ring (DLR).", List.of(new WorldPoint(2217, 3092, 0))),
		new BeginnerMasterClues(ItemID.CLUE_SCROLL_MASTER, ClueTier.MASTER, BeginnerMasterClueText.MASTER_COORDINATE_3, "Island north-east of Mos Le'Harmless.", List.of(new WorldPoint(3830, 3060, 0))),
		new BeginnerMasterClues(ItemID.CLUE_SCROLL_MASTER, ClueTier.MASTER, BeginnerMasterClueText.MASTER_COORDINATE_4, "Crandor, centre of the island.", List.of(new WorldPoint(2834, 3271, 0))),
		new BeginnerMasterClues(ItemID.CLUE_SCROLL_MASTER, ClueTier.MASTER, BeginnerMasterClueText.MASTER_COORDINATE_5, "Witchaven, east from the chapel.", List.of(new WorldPoint(2732, 3284, 0))),
		new BeginnerMasterClues(ItemID.CLUE_SCROLL_MASTER, ClueTier.MASTER, BeginnerMasterClueText.MASTER_COORDINATE_6, "Outside the Meiyerditch Mine.", List.of(new WorldPoint(3622, 3320, 0))),
		new BeginnerMasterClues(ItemID.CLUE_SCROLL_MASTER, ClueTier.MASTER, BeginnerMasterClueText.MASTER_COORDINATE_7, "Eastern entrance to Prifddinas.", List.of(new WorldPoint(2303, 3328, 0))),
		new BeginnerMasterClues(ItemID.CLUE_SCROLL_MASTER, ClueTier.MASTER, BeginnerMasterClueText.MASTER_COORDINATE_8, "Graveyard where players fight Dessous.", List.of(new WorldPoint(3570, 3405, 0))),
		new BeginnerMasterClues(ItemID.CLUE_SCROLL_MASTER, ClueTier.MASTER, BeginnerMasterClueText.MASTER_COORDINATE_9, "Crabclaw Isle.", List.of(new WorldPoint(1769, 3418, 0))),
		new BeginnerMasterClues(ItemID.CLUE_SCROLL_MASTER, ClueTier.MASTER, BeginnerMasterClueText.MASTER_COORDINATE_10, "Water Obelisk Island.", List.of(new WorldPoint(2840, 3423, 0))),
		new BeginnerMasterClues(ItemID.CLUE_SCROLL_MASTER, ClueTier.MASTER, BeginnerMasterClueText.MASTER_COORDINATE_11, "Wrecked ship, outside of Port Phasmatys.", List.of(new WorldPoint(3604, 3564, 0))),
		new BeginnerMasterClues(ItemID.CLUE_SCROLL_MASTER, ClueTier.MASTER, BeginnerMasterClueText.MASTER_COORDINATE_12, "Obelisk of Air in the Wilderness.", List.of(new WorldPoint(3085, 3569, 0))),
		new BeginnerMasterClues(ItemID.CLUE_SCROLL_MASTER, ClueTier.MASTER, BeginnerMasterClueText.MASTER_COORDINATE_13, "Eastern shore of Crash Island.", List.of(new WorldPoint(2934, 2727, 0))),
		new BeginnerMasterClues(ItemID.CLUE_SCROLL_MASTER, ClueTier.MASTER, BeginnerMasterClueText.MASTER_COORDINATE_14, "Shaman area west of Lizardman Canyon.", List.of(new WorldPoint(1451, 3695, 0))),
		new BeginnerMasterClues(ItemID.CLUE_SCROLL_MASTER, ClueTier.MASTER, BeginnerMasterClueText.MASTER_COORDINATE_15, "On top of Waterbirth Island.", List.of(new WorldPoint(2538, 3739, 0))),
		new BeginnerMasterClues(ItemID.CLUE_SCROLL_MASTER, ClueTier.MASTER, BeginnerMasterClueText.MASTER_COORDINATE_16, "North wing of the Farming Guild.", List.of(new WorldPoint(1248, 3751, 0))),
		new BeginnerMasterClues(ItemID.CLUE_SCROLL_MASTER, ClueTier.MASTER, BeginnerMasterClueText.MASTER_COORDINATE_17, "Crypt of the Arceuus church.", List.of(new WorldPoint(1698, 3792, 0))),
		new BeginnerMasterClues(ItemID.CLUE_SCROLL_MASTER, ClueTier.MASTER, BeginnerMasterClueText.MASTER_COORDINATE_18, "North-west of The Forgotten Cemetery.", List.of(new WorldPoint(2951, 3820, 0))),
		new BeginnerMasterClues(ItemID.CLUE_SCROLL_MASTER, ClueTier.MASTER, BeginnerMasterClueText.MASTER_COORDINATE_19, "Pirates' Cove.", List.of(new WorldPoint(2202, 3825, 0))),
		new BeginnerMasterClues(ItemID.CLUE_SCROLL_MASTER, ClueTier.MASTER, BeginnerMasterClueText.MASTER_COORDINATE_20, "In the dense essence mine.", List.of(new WorldPoint(1761, 3853, 0))),
		new BeginnerMasterClues(ItemID.CLUE_SCROLL_MASTER, ClueTier.MASTER, BeginnerMasterClueText.MASTER_COORDINATE_21, "Island west of the Astral Altar.", List.of(new WorldPoint(2090, 3863, 0))),
		new BeginnerMasterClues(ItemID.CLUE_SCROLL_MASTER, ClueTier.MASTER, BeginnerMasterClueText.MASTER_COORDINATE_22, "Sulphur mine in Lovakengj.", List.of(new WorldPoint(1442, 3878, 0))),
		new BeginnerMasterClues(ItemID.CLUE_SCROLL_MASTER, ClueTier.MASTER, BeginnerMasterClueText.MASTER_COORDINATE_23, "Wilderness Volcano.", List.of(new WorldPoint(3380, 3929, 0))),
		new BeginnerMasterClues(ItemID.CLUE_SCROLL_MASTER, ClueTier.MASTER, BeginnerMasterClueText.MASTER_COORDINATE_24, "Inside the Resource Area.", List.of(new WorldPoint(3188, 3939, 0))),
		new BeginnerMasterClues(ItemID.CLUE_SCROLL_MASTER, ClueTier.MASTER, BeginnerMasterClueText.MASTER_COORDINATE_25, "Outside Rogues' Castle.", List.of(new WorldPoint(3304, 3941, 0))),
		new BeginnerMasterClues(ItemID.CLUE_SCROLL_MASTER, ClueTier.MASTER, BeginnerMasterClueText.MASTER_COORDINATE_26, "South-east of the Wilderness Agility Course.", List.of(new WorldPoint(3028, 3928, 0))),
		new BeginnerMasterClues(ItemID.CLUE_SCROLL_MASTER, ClueTier.MASTER, BeginnerMasterClueText.MASTER_CRYPTIC_0, "Ping and Pong's room.", List.of(new WorldPoint(2670, 10395, 0))),
		new BeginnerMasterClues(ItemID.CLUE_SCROLL_MASTER, ClueTier.MASTER, BeginnerMasterClueText.MASTER_CRYPTIC_1, "Wizard Cromperty, +100 in magical attack bonus.", List.of(new WorldPoint(2684, 3325, 0))),
		new BeginnerMasterClues(ItemID.CLUE_SCROLL_MASTER, ClueTier.MASTER, BeginnerMasterClueText.MASTER_CRYPTIC_2, "Thorgel at the entrance to the Death Altar.", List.of(new WorldPoint(1861, 4641, 0))),
		new BeginnerMasterClues(ItemID.CLUE_SCROLL_MASTER, ClueTier.MASTER, BeginnerMasterClueText.MASTER_CRYPTIC_3, "North-east of the God Wars Dungeon entrance.", List.of(new WorldPoint(2918, 3745, 0))),
		new BeginnerMasterClues(ItemID.CLUE_SCROLL_MASTER, ClueTier.MASTER, BeginnerMasterClueText.MASTER_CRYPTIC_4, "Abbot Langley with a negative prayer bonus.", List.of(new WorldPoint(3052, 3490, 0))),
		new BeginnerMasterClues(ItemID.CLUE_SCROLL_MASTER, ClueTier.MASTER, BeginnerMasterClueText.MASTER_CRYPTIC_5, "Charter a ship to Entrana with armour or weapons.", List.of(new WorldPoint(3052, 3237, 0))),
		new BeginnerMasterClues(ItemID.CLUE_SCROLL_MASTER, ClueTier.MASTER, BeginnerMasterClueText.MASTER_CRYPTIC_6, "Speak to Jorral to receive a strange device.", List.of(new WorldPoint(2436, 3346, 0))),
		new BeginnerMasterClues(ItemID.CLUE_SCROLL_MASTER, ClueTier.MASTER, BeginnerMasterClueText.MASTER_CRYPTIC_7, "Speak to Viggora.", List.of(new WorldPoint(3119, 9996, 0), new WorldPoint(3294, 3934, 0), new WorldPoint(3448, 3550, 0))),
		new BeginnerMasterClues(ItemID.CLUE_SCROLL_MASTER, ClueTier.MASTER, BeginnerMasterClueText.MASTER_CRYPTIC_8, "Speak to Biblia.", List.of(new WorldPoint(1633, 3823, 0))),
		new BeginnerMasterClues(ItemID.CLUE_SCROLL_MASTER, ClueTier.MASTER, BeginnerMasterClueText.MASTER_CRYPTIC_9, "Where you fought Kamil from Desert Treasure I.", List.of(new WorldPoint(2873, 3757, 0))),
		new BeginnerMasterClues(ItemID.CLUE_SCROLL_MASTER, ClueTier.MASTER, BeginnerMasterClueText.MASTER_CRYPTIC_10, "Pillar at the end of the Deep Wilderness Dungeon.", List.of(new WorldPoint(3045, 3925, 0))),
		new BeginnerMasterClues(ItemID.CLUE_SCROLL_MASTER, ClueTier.MASTER, BeginnerMasterClueText.MASTER_CRYPTIC_11, "Speak to Falo the Bard.", List.of(new WorldPoint(2689, 3549, 0))),
		new BeginnerMasterClues(ItemID.CLUE_SCROLL_MASTER, ClueTier.MASTER, BeginnerMasterClueText.MASTER_CRYPTIC_12, "Onion patch in the east part of Prifddinas.", List.of(new WorldPoint(2299, 3328, 0))),
		new BeginnerMasterClues(ItemID.CLUE_SCROLL_MASTER, ClueTier.MASTER, BeginnerMasterClueText.MASTER_CRYPTIC_13, "Dig by the fire in the Rogues' Den.", List.of(new WorldPoint(2906, 3537, 0))),
		new BeginnerMasterClues(ItemID.CLUE_SCROLL_MASTER, ClueTier.MASTER, BeginnerMasterClueText.MASTER_CRYPTIC_14, "Speak to Ghommal,+100 in strength bonus.", List.of(new WorldPoint(2879, 3547, 0))),
		new BeginnerMasterClues(ItemID.CLUE_SCROLL_MASTER, ClueTier.MASTER, BeginnerMasterClueText.MASTER_CRYPTIC_15, " Runite rock in the Lava Maze Dungeon.", List.of(new WorldPoint(3069, 3860, 0))),
		new BeginnerMasterClues(ItemID.CLUE_SCROLL_MASTER, ClueTier.MASTER, BeginnerMasterClueText.MASTER_CRYPTIC_16, "Lava lake that is shaped like a Guthixian symbol.", List.of(new WorldPoint(3069, 3932, 0))),
		new BeginnerMasterClues(ItemID.CLUE_SCROLL_MASTER, ClueTier.MASTER, BeginnerMasterClueText.MASTER_CRYPTIC_17, "Talk to Juna wearing three Guthix items.", List.of(new WorldPoint(3252, 9517, 0))),
		new BeginnerMasterClues(ItemID.CLUE_SCROLL_MASTER, ClueTier.MASTER, BeginnerMasterClueText.MASTER_CRYPTIC_18, "Speak to Sir Vyvin while wearing white armour.", List.of(new WorldPoint(2984, 3339, 2))),
		new BeginnerMasterClues(ItemID.CLUE_SCROLL_MASTER, ClueTier.MASTER, BeginnerMasterClueText.MASTER_CRYPTIC_19, "Dig below the mossy rock under the Viyeldi caves.", List.of(new WorldPoint(2782, 2935, 0))),
		new BeginnerMasterClues(ItemID.CLUE_SCROLL_MASTER, ClueTier.MASTER, BeginnerMasterClueText.MASTER_CRYPTIC_20, "First set of rocks towards Saradomin's Encampment.", List.of(new WorldPoint(2918, 3745, 0))),
		new BeginnerMasterClues(ItemID.CLUE_SCROLL_MASTER, ClueTier.MASTER, BeginnerMasterClueText.MASTER_CRYPTIC_21, "Speak to the Key Master in Cerberus' Lair.", List.of(new WorldPoint(1310, 1251, 0))),
		new BeginnerMasterClues(ItemID.CLUE_SCROLL_MASTER, ClueTier.MASTER, BeginnerMasterClueText.MASTER_CRYPTIC_22, "Speak to Piles in the Resource Area.", List.of(new WorldPoint(3185, 3934, 0))),
		new BeginnerMasterClues(ItemID.CLUE_SCROLL_MASTER, ClueTier.MASTER, BeginnerMasterClueText.MASTER_CRYPTIC_23, "Blood rune spawn next to the Demonic Ruins.", List.of(new WorldPoint(3294, 3889, 0))),
		new BeginnerMasterClues(ItemID.CLUE_SCROLL_MASTER, ClueTier.MASTER, BeginnerMasterClueText.MASTER_CRYPTIC_24, "Speak to Robin,+100 in ranged attack bonus.", List.of(new WorldPoint(3676, 3494, 0))),
		new BeginnerMasterClues(ItemID.CLUE_SCROLL_MASTER, ClueTier.MASTER, BeginnerMasterClueText.MASTER_CRYPTIC_25, "Speak to Lovada.", List.of(new WorldPoint(1487, 3833, 0))),
		new BeginnerMasterClues(ItemID.CLUE_SCROLL_MASTER, ClueTier.MASTER, BeginnerMasterClueText.MASTER_CRYPTIC_26, "Speak to Logosia.", List.of(new WorldPoint(1633, 3808, 0))),
		new BeginnerMasterClues(ItemID.CLUE_SCROLL_MASTER, ClueTier.MASTER, BeginnerMasterClueText.MASTER_CRYPTIC_27, "Show this to Sherlock.", List.of(new WorldPoint(2733, 3413, 0))),
		new BeginnerMasterClues(ItemID.CLUE_SCROLL_MASTER, ClueTier.MASTER, BeginnerMasterClueText.MASTER_CRYPTIC_28, "Dig in front of the Shilo Village furnace.", List.of(new WorldPoint(2859, 2962, 0))),
		new BeginnerMasterClues(ItemID.CLUE_SCROLL_MASTER, ClueTier.MASTER, BeginnerMasterClueText.MASTER_CRYPTIC_29, "North-eastern-most corner of the Shadow Dungeon.", List.of(new WorldPoint(2547, 3421, 0))),
		new BeginnerMasterClues(ItemID.CLUE_SCROLL_MASTER, ClueTier.MASTER, BeginnerMasterClueText.MASTER_CRYPTIC_30, "Speak to Ewesey.", List.of(new WorldPoint(1647, 3627, 0))),
		new BeginnerMasterClues(ItemID.CLUE_SCROLL_MASTER, ClueTier.MASTER, BeginnerMasterClueText.MASTER_CRYPTIC_31, "Dig next to the terrorbird display.", List.of(new WorldPoint(3260, 3449, 0))),
		new BeginnerMasterClues(ItemID.CLUE_SCROLL_MASTER, ClueTier.MASTER, BeginnerMasterClueText.MASTER_CRYPTIC_32, "Speak to the Mage of Zamorak.", List.of(new WorldPoint(3259, 3386, 0))),
		new BeginnerMasterClues(ItemID.CLUE_SCROLL_MASTER, ClueTier.MASTER, BeginnerMasterClueText.MASTER_EMOTE_0, "Emote in the town of Gwenith.", List.of(new WorldPoint(2213, 3427, 0))),
		new BeginnerMasterClues(ItemID.CLUE_SCROLL_MASTER, ClueTier.MASTER, BeginnerMasterClueText.MASTER_EMOTE_1, "Emote outside K'ril Tsutsaroth.", List.of(new WorldPoint(2931, 5337, 0))),
		new BeginnerMasterClues(ItemID.CLUE_SCROLL_MASTER, ClueTier.MASTER, BeginnerMasterClueText.MASTER_EMOTE_2, "Emote at the Warrior's guild bank.", List.of(new WorldPoint(2843, 3540, 0))),
		new BeginnerMasterClues(ItemID.CLUE_SCROLL_MASTER, ClueTier.MASTER, BeginnerMasterClueText.MASTER_EMOTE_3, "Emote in the Iorwerth Camp.", List.of(new WorldPoint(2199, 3254, 0))),
		new BeginnerMasterClues(ItemID.CLUE_SCROLL_MASTER, ClueTier.MASTER, BeginnerMasterClueText.MASTER_EMOTE_4, "Emote in the Entrana church.", List.of(new WorldPoint(2851, 3354, 0))),
		new BeginnerMasterClues(ItemID.CLUE_SCROLL_MASTER, ClueTier.MASTER, BeginnerMasterClueText.MASTER_EMOTE_5, "Emote in the magic axe hut.", List.of(new WorldPoint(3188, 3957, 0))),
		new BeginnerMasterClues(ItemID.CLUE_SCROLL_MASTER, ClueTier.MASTER, BeginnerMasterClueText.MASTER_EMOTE_6, "Emote in the Tzhaar gem store.", List.of(new WorldPoint(2466, 5150, 0))),
		new BeginnerMasterClues(ItemID.CLUE_SCROLL_MASTER, ClueTier.MASTER, BeginnerMasterClueText.MASTER_EMOTE_7, "Emote in Iban's temple.", List.of(new WorldPoint(2006, 4709, 1))),
		new BeginnerMasterClues(ItemID.CLUE_SCROLL_MASTER, ClueTier.MASTER, BeginnerMasterClueText.MASTER_EMOTE_8, "Emote in the King Black Dragon Lair.", List.of(new WorldPoint(2286, 4680, 0))),
		new BeginnerMasterClues(ItemID.CLUE_SCROLL_MASTER, ClueTier.MASTER, BeginnerMasterClueText.MASTER_EMOTE_9, "Emote at the Barrows chest.", List.of(new WorldPoint(3548, 9691, 0))),
		new BeginnerMasterClues(ItemID.CLUE_SCROLL_MASTER, ClueTier.MASTER, BeginnerMasterClueText.MASTER_EMOTE_10, "Emote at the Death Altar.", List.of(new WorldPoint(2210, 4842, 0))),
		new BeginnerMasterClues(ItemID.CLUE_SCROLL_MASTER, ClueTier.MASTER, BeginnerMasterClueText.MASTER_EMOTE_11, "Emote outside the gates of Cam Torum.", List.of(new WorldPoint(1428, 3119, 0))),
		new BeginnerMasterClues(ItemID.CLUE_SCROLL_MASTER, ClueTier.MASTER, BeginnerMasterClueText.MASTER_EMOTE_12, "Emote at the Goblin Village.", List.of(new WorldPoint(2959, 3502, 0))),
		new BeginnerMasterClues(ItemID.CLUE_SCROLL_MASTER, ClueTier.MASTER, BeginnerMasterClueText.MASTER_EMOTE_13, "Emote in the centre of Zul-Andra.", List.of(new WorldPoint(2204, 3059, 0))),
		new BeginnerMasterClues(ItemID.CLUE_SCROLL_MASTER, ClueTier.MASTER, BeginnerMasterClueText.MASTER_EMOTE_14, "Emote on the east side of Lava Dragon Isle.", List.of(new WorldPoint(3229, 3832, 0))),
		new BeginnerMasterClues(ItemID.CLUE_SCROLL_MASTER, ClueTier.MASTER, BeginnerMasterClueText.MASTER_EMOTE_15, "Emote at the Wise old man.", List.of(new WorldPoint(3095, 3255, 0))),
		new BeginnerMasterClues(ItemID.CLUE_SCROLL_MASTER, ClueTier.MASTER, BeginnerMasterClueText.MASTER_EMOTE_16, "Emote in Ellamaria's garden.", List.of(new WorldPoint(3232, 3493, 0))),
		new BeginnerMasterClues(ItemID.CLUE_SCROLL_MASTER, ClueTier.MASTER, BeginnerMasterClueText.MASTER_EMOTE_17, "Emote in the centre of the Kourend catacombs.", List.of(new WorldPoint(1662, 10044, 0))),
		new BeginnerMasterClues(ItemID.CLUE_SCROLL_MASTER, ClueTier.MASTER, BeginnerMasterClueText.MASTER_EMOTE_18, "Emote in front of the Soul Altar.", List.of(new WorldPoint(1811, 3853, 0))),
		new BeginnerMasterClues(ItemID.CLUE_SCROLL_MASTER, ClueTier.MASTER, BeginnerMasterClueText.MASTER_EMOTE_19, "Emote in the Enchanted valley.", List.of(new WorldPoint(3022, 4517, 0))),
		new BeginnerMasterClues(ItemID.CLUE_SCROLL_MASTER, ClueTier.MASTER, BeginnerMasterClueText.MASTER_EMOTE_20, "Emote at the top of the Watchtower.", List.of(new WorldPoint(2548, 3112, 0))),
		new BeginnerMasterClues(ItemID.CLUE_SCROLL_MASTER, ClueTier.MASTER, BeginnerMasterClueText.MASTER_EMOTE_21, "Emote on the wall of Castle Drakan.", List.of(new WorldPoint(3563, 3379, 0))),
		new BeginnerMasterClues(ItemID.CLUE_SCROLL_MASTER, ClueTier.MASTER, BeginnerMasterClueText.MASTER_EMOTE_22, "Emote in the 7th room of Pyramid Plunder.", List.of(new WorldPoint(1951, 4431, 0))),
		new BeginnerMasterClues(ItemID.CLUE_SCROLL_MASTER, ClueTier.MASTER, BeginnerMasterClueText.MASTER_EMOTE_23, "Emote in the Salvager Overlook.", List.of(new WorldPoint(1614, 3296, 0))),
		new BeginnerMasterClues(ItemID.CLUE_SCROLL_MASTER, ClueTier.MASTER, BeginnerMasterClueText.MASTER_FALO_0, "Falo: Dragon scimitar.", List.of(new WorldPoint(2689, 3549, 0))),
		new BeginnerMasterClues(ItemID.CLUE_SCROLL_MASTER, ClueTier.MASTER, BeginnerMasterClueText.MASTER_FALO_1, "Falo: God book.", List.of(new WorldPoint(2689, 3549, 0))),
		new BeginnerMasterClues(ItemID.CLUE_SCROLL_MASTER, ClueTier.MASTER, BeginnerMasterClueText.MASTER_FALO_2, "Falo: Crystal bow.", List.of(new WorldPoint(2689, 3549, 0))),
		new BeginnerMasterClues(ItemID.CLUE_SCROLL_MASTER, ClueTier.MASTER, BeginnerMasterClueText.MASTER_FALO_3, "Falo: Infernal axe.", List.of(new WorldPoint(2689, 3549, 0))),
		new BeginnerMasterClues(ItemID.CLUE_SCROLL_MASTER, ClueTier.MASTER, BeginnerMasterClueText.MASTER_FALO_4, "Falo: Mark of grace.", List.of(new WorldPoint(2689, 3549, 0))),
		new BeginnerMasterClues(ItemID.CLUE_SCROLL_MASTER, ClueTier.MASTER, BeginnerMasterClueText.MASTER_FALO_5, "Falo: Lava dragon bones.", List.of(new WorldPoint(2689, 3549, 0))),
		new BeginnerMasterClues(ItemID.CLUE_SCROLL_MASTER, ClueTier.MASTER, BeginnerMasterClueText.MASTER_FALO_6, "Falo: Armadyl helmet.", List.of(new WorldPoint(2689, 3549, 0))),
		new BeginnerMasterClues(ItemID.CLUE_SCROLL_MASTER, ClueTier.MASTER, BeginnerMasterClueText.MASTER_FALO_7, "Falo: Dragon defender.", List.of(new WorldPoint(2689, 3549, 0))),
		new BeginnerMasterClues(ItemID.CLUE_SCROLL_MASTER, ClueTier.MASTER, BeginnerMasterClueText.MASTER_FALO_8, "Falo: Warrior guild token.", List.of(new WorldPoint(2689, 3549, 0))),
		new BeginnerMasterClues(ItemID.CLUE_SCROLL_MASTER, ClueTier.MASTER, BeginnerMasterClueText.MASTER_FALO_9, "Falo: Greenman's ale(m).", List.of(new WorldPoint(2689, 3549, 0))),
		new BeginnerMasterClues(ItemID.CLUE_SCROLL_MASTER, ClueTier.MASTER, BeginnerMasterClueText.MASTER_FALO_10, "Falo: Barrelchest anchor.", List.of(new WorldPoint(2689, 3549, 0))),
		new BeginnerMasterClues(ItemID.CLUE_SCROLL_MASTER, ClueTier.MASTER, BeginnerMasterClueText.MASTER_FALO_11, "Falo: Basalt.", List.of(new WorldPoint(2689, 3549, 0))),
		new BeginnerMasterClues(ItemID.CLUE_SCROLL_MASTER, ClueTier.MASTER, BeginnerMasterClueText.MASTER_FALO_12, "Falo: Tzhaar-ket-om.", List.of(new WorldPoint(2689, 3549, 0))),
		new BeginnerMasterClues(ItemID.CLUE_SCROLL_MASTER, ClueTier.MASTER, BeginnerMasterClueText.MASTER_FALO_13, "Falo: Fighter torso.", List.of(new WorldPoint(2689, 3549, 0))),
		new BeginnerMasterClues(ItemID.CLUE_SCROLL_MASTER, ClueTier.MASTER, BeginnerMasterClueText.MASTER_FALO_14, "Falo: Barrows gloves.", List.of(new WorldPoint(2689, 3549, 0))),
		new BeginnerMasterClues(ItemID.CLUE_SCROLL_MASTER, ClueTier.MASTER, BeginnerMasterClueText.MASTER_FALO_15, "Falo: Cooking gauntlets.", List.of(new WorldPoint(2689, 3549, 0))),
		new BeginnerMasterClues(ItemID.CLUE_SCROLL_MASTER, ClueTier.MASTER, BeginnerMasterClueText.MASTER_FALO_16, "Falo: Numulite.", List.of(new WorldPoint(2689, 3549, 0))),
		new BeginnerMasterClues(ItemID.CLUE_SCROLL_MASTER, ClueTier.MASTER, BeginnerMasterClueText.MASTER_FALO_17, "Falo: Rune platebody.", List.of(new WorldPoint(2689, 3549, 0))),
		new BeginnerMasterClues(ItemID.CLUE_SCROLL_MASTER, ClueTier.MASTER, BeginnerMasterClueText.MASTER_FALO_18, "Falo: Ivandis flail.", List.of(new WorldPoint(2689, 3549, 0)))
	);

	private final Integer clueID;
	final ClueTier clueTier;
	private final String text;
	private final String tag;

	@Getter
	final OrRequirement regions;

	public BeginnerMasterClues(Integer clueID, ClueTier clueTier, String text, String tag, List<WorldPoint> wps)
	{
		this.clueID = clueID;
		this.clueTier = clueTier;
		this.text = text;
		this.tag = tag;
		this.regions = new OrRequirement(wps);
	}

	public Integer getFakeId()
	{
		// Use InterfaceID for Beginner Map Clues
		if (getClueID() != ItemID.CLUE_SCROLL_BEGINNER && getClueID() != ItemID.CLUE_SCROLL_MASTER)
		{
			return getClueID();
		}
		return getText().hashCode();
	}

	public static Integer forTextGetId(String text)
	{
		for (BeginnerMasterClues clue : CLUES)
		{
			if (text.equalsIgnoreCase(clue.text))
			{
				return clue.getFakeId();
			}
		}

		return null;
	}

	public static BeginnerMasterClues getById(int id)
	{
		for (BeginnerMasterClues clue : BeginnerMasterClues.CLUES)
		{
			if (clue.getFakeId() == id)
			{
				return clue;
			}
		}
		return null;
	}
}
