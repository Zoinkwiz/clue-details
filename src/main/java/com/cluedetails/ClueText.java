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

import com.google.common.collect.ImmutableList;
import java.util.List;
import lombok.Getter;

@Getter
public class ClueText
{
	static final List<ClueText> CLUES = ImmutableList.of(
		// Anagrams
		new ClueText("A Elf Knows", "NEX A: snowflake"),
		new ClueText("Brucie Catnap", "XER 1: bruce"),
		new ClueText("Car If Ices", "ZUL: sacrifice"),
		new ClueText("Ded War", "OBEL 6: -edward"),
		new ClueText("Dim Tharn", "RoR: -mandrith"),
		new ClueText("Duo Plug", "NEX M: dugopul"),
		new ClueText("Forlun", "BOX J: runolf"),
		new ClueText("Im N Zezim", "ZANA: immenizz"),
		new ClueText("Mal in Tau", "LEGS: luminata"),
		new ClueText("Mold La Ran", "DRAK 1: old man"),
		new ClueText("Mus Kil Reader", "QPC: +radimus"),
		new ClueText("Rip Maul", "MYTH: +primula"),
		new ClueText("Rue Go", "ETC 1: goreu"),
		new ClueText("Slam Duster Grail", "BOX H: lars"),
		new ClueText("Ten Wigs On", "NARDAH: wingstone"),
		new ClueText("Twenty Cure Iron", "BOTD 3: tony"),
		// Coordinate
		new ClueText("01 degree 30 minutes north 08 degrees 11 minutes west", "IORW: s dig"),
		new ClueText("01 degrees 54 minutes south 08 degrees 54 minutes west", "ZUL: nw dig"),
		new ClueText("02 degrees 09 minutes south 06 degrees 58 minutes west", "DLR: se dig"),
		new ClueText("03 degrees 09 minutes south 43 degrees 26 minutes east", "MOS LE: island dig"),
		new ClueText("03 degrees 26 minutes north 12 degrees 18 minutes east", "DIA 9: crandor dig"),
		new ClueText("03 degrees 50 minutes north 09 degrees 07 minutes east", "ARDY: e dig"),
		new ClueText("04 degrees 58 minutes north 36 degrees 56 minutes east", "DRAK 1: +mines dig"),
		new ClueText("05 degrees 13 minutes north 04 degrees 16 minutes west", "ETC 2: e dig"),
		new ClueText("07 degrees 37 minutes north 35 degrees 18 minutes east", "MORT: +boat dig"),
		new ClueText("08 degrees 01 minutes north 20 degrees 58 minutes west", "POH 5: cc isle dig"),
		new ClueText("08 degrees 11 minutes north 12 degrees 30 minutes east", "POH 3: water dig"),
		new ClueText("12 degrees 35 minutes north 36 degrees 22 minutes east", "FENK: ship dig"),
		new ClueText("12 degrees 45 minutes north 20 degrees 09 minutes east", "PADD: air dig"),
		new ClueText("13 degrees 33 minutes south 15 degrees 26 minutes east", "APE ATOLL: lumdo dig"),
		new ClueText("16 degrees 41 minutes north 30 degrees 54 minutes west", "DJR: nw dig"),
		new ClueText("18 degrees 03 minutes north 03 degrees 03 minutes east", "W BIRTH: top dig"),
		new ClueText("18 degrees 26 minutes north 37 degrees 15 minutes west", "TREE B: dig"),
		new ClueText("19 degrees 43 minutes north 23 degrees 11 minutes west", "BOTD 5: church dig"),
		new ClueText("20 degrees 35 minutes north 15 degrees 58 minutes east", "GHOR: -temple dig"),
		new ClueText("20 degrees 45 minutes north 7 degrees 26 degrees west", "W BIRTH: cove dig"),
		new ClueText("21 degrees 37 minutes north 21 degrees 13 minutes west", "CIS: mine dig"),
		new ClueText("21 degrees 56 minutes north 10 degrees 56 minutes west", "LUNAR: s dig"),
		new ClueText("22 degrees 24 minutes north 31 degrees 11 minutes west", "XER 3: nw dig"),
		new ClueText("24 degrees 00 minutes north 29 degrees 22 minutes east", "OBEL 6: -volcano dig"),
		new ClueText("24 degrees 18 minutes north 23 degrees 22 minutes east", "RoR: -resource dig"),
		new ClueText("24 degrees 22 minutes north 27 degrees 00 minutes east", "OBEL 6: -n dig"),
		new ClueText("23 degrees 58 minutes north 18 degrees 22 minutes east", "ICE: -e dig"),
		// Cryptic
		new ClueText("2 musical birds. Dig in front of the spinning light.", "DKS: +penguin"),
		new ClueText("A chisel and hammer reside in his home, strange for one of magic. Impress him with your magical equipment.", "ARDY: +Wizard"),
		new ClueText("A dwarf, approaching death, but very much in the light.", "DEATH: +Thorgel"),
		new ClueText("A massive battle rages beneath so be careful when you dig by the large broken crossbow.", "HILT: ne climb"),
		new ClueText("Anger Abbot Langley.", "BOX B: +Abbot"),
		new ClueText("Anger those who adhere to Saradomin's edicts to prevent travel.", "TREE 7: monk"),
		new ClueText("Buried beneath the ground, who knows where it's found.", "HOTNCOLD"),
		new ClueText("Lucky for you, A man called Jorral may have a clue.", "HOTNCOLD"),
		new ClueText("Come brave adventurer, your sense is on fire. If you talk to me, it's an old god you desire.", "VIGGORA: +ring"),
		new ClueText("Darkness wanders around me, but fills my mind with knowledge.", "A LIB: Biblia"),
		new ClueText("Dig in front of the icy arena where 1 of 4 was fought.", "HILT: kamil"),
		new ClueText("Faint sounds of 'Arr', fire giants found deep, the eastern tip of a lake, are the rewards you could reap.", "ICE: -dwd"),
		new ClueText("Falo the bard wants to see you.", "MUSIC"),
		new ClueText("Elvish onions.", "ETC 2: onion"),
		new ClueText("Fiendish cooks probably won't dig the dirty dishes.", "BOX 4: rogue's den"),
		new ClueText("The doorman of the Warriors' Guild wishes to be impressed by how strong your equipment is.", "W MAX: Ghommal"),
		new ClueText("Great demons, dragons, and spiders protect this blue rock, beneath which, you may find what you seek.", "REV: -maze"),
		new ClueText("Guthix left his mark in a fiery lake, dig at the tip of it.", "ICE: -lake"),
		new ClueText("Here, there are tears, but nobody is crying. Speak to the guardian and show off your alignment to balance.", "BOX 7: +Juna"),
		new ClueText("Hopefully this set of armour will help you to keep surviving.", "DIA 3: +Vyvin"),
		new ClueText("If you're feeling brave, dig beneath the dragon's eye.", "DIA 8: +viyeldi"),
		new ClueText("I lie beneath the first descent to the holy encampment.", "HILT: sara encamp"),
		new ClueText("My life was spared but these voices remain, now guarding these iron gates is my bane.", "KEY: Keymaster"),
		new ClueText("One of several rhyming brothers, in business attire with an obsession for paper work.", "RoR: -Piles"),
		new ClueText("Pentagrams and demons, burnt bones and remains, I wonder what the blood contains.", "ANNA: -bloodrune"),
		new ClueText("Robin wishes to see your finest ranged equipment.", "ECTO: +Robin"),
		new ClueText("She's small but can build both literally and figuratively.", "XER 3: Lovada"),
		new ClueText("Shhhh!", "A LIB: Logosia"),
		new ClueText("Show this to Sherlock.", "HEAD: Sherlock"),
		new ClueText("South of a river in a town surrounded by the undead, what lies beneath the furnace?", "GLOVE: furnace"),
		new ClueText("The far north eastern corner where 1 of 4 was defeated, the shadows still linger.", "FISH: +shadow"),
		new ClueText("This place sure is a mess.", "DIA A: Ewesey"),
		new ClueText("Under a giant robotic bird that cannot fly.", "VARR: museum"),
		new ClueText("Where safe to speak, the man who offers the pouch of smallest size wishes to see your alignment.", "DIA D: Zamorak"),
		// Emote
		new ClueText("Beckon by a collection of crystalline maple trees. Beware of double agents! Equip Bryophyta's staff and a nature tiara.", "TREE 6: n stash"),
		new ClueText("Blow a kiss outside K'ril Tsutsaroth's chamber. Beware of double agents! Equip a Zamorak full helm and the shadow sword.", "HILT: gwd stash"),
		new ClueText("Blow a raspberry at the bank of the Warrior's guild. Beware of double agents! Equip a dragon battleaxe, a slayer helm of any kind and a dragon defender or avernic defender.", "W MAX: stash"),
		new ClueText("Bow in the Iorwerth Camp. Beware of double agents! Equip a charged crystal bow.", "IORW: stash"),
		new ClueText("Cheer in the Entrana church. Beware of double agents! Equip a set of full black dragonhide armour.", "ENTRANA: -stash"),
		new ClueText("Clap in the magic axe hut. Beware of double agents. Equip only flared trousers", "RoR: -hut stash"),
		new ClueText("Cry in the Tzhaar gem store. Beware of the double agents! Equip a fire cape and a TokTz-Xil-Ul'", "DIA 9: stash"),
		new ClueText("Dance in Iban's temple. Beware of double agents! Equip Iban's staff, a black mystic top, and a black mystic bottom.", "ETC 1: stash"),
		new ClueText("Dance in the King Black Dragon Lair|King Black Dragon's lair. Beware of double agents! Equip a black d'hide body, black d'hide vambraces and a black dragon mask.", "GHOR: -stash"),
		new ClueText("Do a jig at the Barrows chest. Beware of double agents! Equip any full barrows set.", "BARROWS: stash"),
		new ClueText("Flap at the Death Altar. Beware of double agents! Equip a death tiara, a legend's cape and any ring of wealth.", "DEATH: +stash"),
		new ClueText("Salute outside the gates of Cam Torum. Beware of double agents! Equip a full set of blue moon equipment.", "C TORUM: +stash"),
		new ClueText("Goblin Salute at the Goblin Village. Beware of double agents! Equip a Bandos platebody, Bandos cloak and Bandos godsword.", "GOBLIN: +stash"),
		new ClueText("Jump for joy in the centre of Zul-Andra. Beware of double agents! Equip a dragon 2h sword, bandos boots and an obsidian cape.", "ZUL: stash"),
		new ClueText("Panic by the big egg where no one dare goes and the ground is burnt. Beware of double agents! Equip a dragon med helm, a TokTz-Ket-Xil, a brine sabre, rune platebody and an uncharged amulet of glory.", "B MAX 1: -stash"),
		new ClueText("Show your anger at the Wise old man. Beware of double agents! Equip an abyssal whip, a cape of legends|legend's cape and some spined chaps.", "GLORY 3: stash"),
		new ClueText("Show your anger towards the Statue of Saradomin in Ellamaria's garden.", "VARR: stash"),
		new ClueText("Slap your head in the centre of the Kourend catacombs. Beware of double agents! Equip arclight or emberlight along with the amulet of the damned.", "XER 4: stash"),
		new ClueText("Spin in front of the Soul Altar. Beware of double agents! Equip a dragon pickaxe, helm of neitiznot and a pair of rune boots.", "SOUL: +stash"),
		new ClueText("Stamp in the Enchanted valley west of the waterfall. Beware of double agents! Equip a dragon axe.", "BKQ: stash"),
		new ClueText("Swing a bullroarer at the top of the watchtower. Beware of double agents! Equip a dragon plateskirt, climbing boots and a dragon chainbody.", "WATCH: stash"),
		new ClueText("Wave on the northern wall of the Castle Drakan. Beware of double agents! Wear a dragon sq shield, splitbark body and any boater.", "C DRAKAN: stash"),
		new ClueText("Yawn in the 7th room of Pyramid Plunder. Beware of double agents! Equip a pharaoh's sceptre and a full set of menaphite robes.", "PHAR 1: +stash"),
		new ClueText("Think on the western coast of Salvager Overlook. Beware of double agents! Equip a Hueycoatl hide coif and some Hueycoatl hide vambraces.", "QUET: stash"),
		// Falo
		new ClueText("A blood red weapon, a strong curved sword, found on the island of primate lords.", "MUSIC: +d scim"),
		new ClueText("A book that preaches of some great figure, lending strength, might and vigour.", "MUSIC: +god book"),
		new ClueText("A bow of elven craft was made, it shimmers bright, but will soon fade.", "MUSIC: +cr bow"),
		new ClueText("A fiery axe of great inferno, when you use it, you'll wonder where the logs go.", "MUSIC: +infernal axe"),
		new ClueText("A mark used to increase one's grace, found atop a seer's place.", "MUSIC: +grace"),
		new ClueText("A molten beast with fiery breath, you acquire these with its death.", "MUSIC: +lava bone"),
		new ClueText("A shiny helmet of flight, to obtain this with melee, struggle you might.", "MUSIC: +arma helm"),
		new ClueText("A sword held in the other hand, red its colour, Cyclops strength you must withstand.", "MUSIC: +defender"),
		new ClueText("A token used to kill mythical beasts, in hopes of a blade or just for an xp feast.", "MUSIC: +token"),
		new ClueText("Green is my favourite, mature ale I do love, this takes your herblore above.", "MUSIC: +ale"),
		new ClueText("It can hold down a boat or crush a goat, this object, you see, is quite heavy.", "MUSIC: +anchor"),
		new ClueText("It comes from the ground, underneath the snowy plain. Trolls aplenty, with what looks like a mane.", "MUSIC: +basalt"),
		new ClueText("No attack to wield, only strength is required, made of obsidian, but with no room for a shield.", "MUSIC: +maul"),
		new ClueText("Penance healers runners and more, obtaining this body often gives much deplore.", "MUSIC: +torso"),
		new ClueText("Strangely found in a chest, many believe these gloves are the best.", "MUSIC: +bgloves"),
		new ClueText("These gloves of white won't help you fight, but aid in cooking, they just might.", "MUSIC: +gauntlets"),
		new ClueText("They come from some time ago, from a land unto the east. Fossilised they have become, this small and gentle beast.", "MUSIC: +numulite"),
		new ClueText("To slay a dragon you must first do, before this chest piece can be put on you.", "MUSIC: +r platebody"),
		new ClueText("Vampyres are agile opponents, damaged best with a weapon of many components.", "MUSIC: +flail")
	);

	private final String text;
	private final String tag;

	public ClueText(String text, String tag)
	{
		this.text = text;
		this.tag = tag;
	}

	public static String forText(String text)
	{
		for (ClueText clue : CLUES)
		{
			if (clue.text.equals(text))
			{
				return clue.tag;
			}
		}

		return null;
	}
}
