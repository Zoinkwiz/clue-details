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
		new ClueText(0, "A Elf Knows", "NEX A: snowflake"),
		new ClueText(1, "Brucie Catnap", "XER 1: bruce"),
		new ClueText(2, "Car If Ices", "ZUL: sacrifice"),
		new ClueText(3, "Ded War", "OBEL 6: -edward"),
		new ClueText(4, "Dim Tharn", "RoR: -mandrith"),
		new ClueText(5, "Duo Plug", "NEX M: dugopul"),
		new ClueText(6, "Forlun", "BOX J: runolf"),
		new ClueText(7, "Im N Zezim", "ZANA: immenizz"),
		new ClueText(8, "Mal in Tau", "LEGS: luminata"),
		new ClueText(9, "Mold La Ran", "DRAK 1: old man"),
		new ClueText(10, "Mus Kil Reader", "QPC: +radimus"),
		new ClueText(11, "Rip Maul", "MYTH: +primula"),
		new ClueText(12, "Rue Go", "ETC 1: goreu"),
		new ClueText(13, "Slam Duster Grail", "BOX H: lars"),
		new ClueText(14, "Ten Wigs On", "NARDAH: wingstone"),
		new ClueText(15, "Twenty Cure Iron", "BOTD 3: tony"),
		// Coordinate
		new ClueText(16, "01 degree 30 minutes north 08 degrees 11 minutes west", "IORW: s dig"),
		new ClueText(17, "01 degrees 54 minutes south 08 degrees 54 minutes west", "ZUL: nw dig"),
		new ClueText(18, "02 degrees 09 minutes south 06 degrees 58 minutes west", "DLR: se dig"),
		new ClueText(19, "03 degrees 09 minutes south 43 degrees 26 minutes east", "MOS LE: island dig"),
		new ClueText(20, "03 degrees 26 minutes north 12 degrees 18 minutes east", "DIA 9: crandor dig"),
		new ClueText(21, "03 degrees 50 minutes north 09 degrees 07 minutes east", "ARDY: e dig"),
		new ClueText(22, "04 degrees 58 minutes north 36 degrees 56 minutes east", "DRAK 1: +mines dig"),
		new ClueText(23, "05 degrees 13 minutes north 04 degrees 16 minutes west", "ETC 2: e dig"),
		new ClueText(24, "07 degrees 37 minutes north 35 degrees 18 minutes east", "MORT: +boat dig"),
		new ClueText(25, "08 degrees 01 minutes north 20 degrees 58 minutes west", "POH 5: cc isle dig"),
		new ClueText(26, "08 degrees 11 minutes north 12 degrees 30 minutes east", "POH 3: water dig"),
		new ClueText(27, "12 degrees 35 minutes north 36 degrees 22 minutes east", "FENK: ship dig"),
		new ClueText(28, "12 degrees 45 minutes north 20 degrees 09 minutes east", "PADD: air dig"),
		new ClueText(29, "13 degrees 33 minutes south 15 degrees 26 minutes east", "APE ATOLL: lumdo dig"),
		new ClueText(30, "16 degrees 41 minutes north 30 degrees 54 minutes west", "DJR: nw dig"),
		new ClueText(31, "18 degrees 03 minutes north 03 degrees 03 minutes east", "W BIRTH: top dig"),
		new ClueText(32, "18 degrees 26 minutes north 37 degrees 15 minutes west", "TREE B: dig"),
		new ClueText(33, "19 degrees 43 minutes north 23 degrees 11 minutes west", "BOTD 5: church dig"),
		new ClueText(34, "20 degrees 35 minutes north 15 degrees 58 minutes east", "GHOR: -temple dig"),
		new ClueText(35, "20 degrees 45 minutes north 7 degrees 26 degrees west", "W BIRTH: cove dig"),
		new ClueText(36, "21 degrees 37 minutes north 21 degrees 13 minutes west", "CIS: mine dig"),
		new ClueText(37, "21 degrees 56 minutes north 10 degrees 56 minutes west", "LUNAR: s dig"),
		new ClueText(38, "22 degrees 24 minutes north 31 degrees 11 minutes west", "XER 3: nw dig"),
		new ClueText(39, "24 degrees 00 minutes north 29 degrees 22 minutes east", "OBEL 6: -volcano dig"),
		new ClueText(40, "24 degrees 18 minutes north 23 degrees 22 minutes east", "RoR: -resource dig"),
		new ClueText(41, "24 degrees 22 minutes north 27 degrees 00 minutes east", "OBEL 6: -n dig"),
		new ClueText(42, "23 degrees 58 minutes north 18 degrees 22 minutes east", "ICE: -e dig"),
		// Cryptic
		new ClueText(43, "2 musical birds. Dig in front of the spinning light.", "DKS: +penguin"),
		new ClueText(44, "A chisel and hammer reside in his home, strange for one of magic. Impress him with your magical equipment.", "ARDY: +Wizard"),
		new ClueText(45, "A dwarf, approaching death, but very much in the light.", "DEATH: +Thorgel"),
		new ClueText(46, "A massive battle rages beneath so be careful when you dig by the large broken crossbow.", "HILT: ne climb"),
		new ClueText(47, "Anger Abbot Langley.", "BOX B: +Abbot"),
		new ClueText(48, "Anger those who adhere to Saradomin's edicts to prevent travel.", "TREE 7: monk"),
		new ClueText(49, "Buried beneath the ground, who knows where it's found.", "HOTNCOLD"),
		new ClueText(50, "Lucky for you, A man called Jorral may have a clue.", "HOTNCOLD"),
		new ClueText(51, "Come brave adventurer, your sense is on fire. If you talk to me, it's an old god you desire.", "VIGGORA: +ring"),
		new ClueText(52, "Darkness wanders around me, but fills my mind with knowledge.", "A LIB: Biblia"),
		new ClueText(53, "Dig in front of the icy arena where 1 of 4 was fought.", "HILT: kamil"),
		new ClueText(54, "Faint sounds of 'Arr', fire giants found deep, the eastern tip of a lake, are the rewards you could reap.", "ICE: -dwd"),
		new ClueText(55, "Falo the bard wants to see you.", "MUSIC"),
		new ClueText(56, "Elvish onions.", "ETC 2: onion"),
		new ClueText(57, "Fiendish cooks probably won't dig the dirty dishes.", "BOX 4: rogue's den"),
		new ClueText(58, "The doorman of the Warriors' Guild wishes to be impressed by how strong your equipment is.", "W MAX: Ghommal"),
		new ClueText(59, "Great demons, dragons, and spiders protect this blue rock, beneath which, you may find what you seek.", "REV: -maze"),
		new ClueText(60, "Guthix left his mark in a fiery lake, dig at the tip of it.", "ICE: -lake"),
		new ClueText(61, "Here, there are tears, but nobody is crying. Speak to the guardian and show off your alignment to balance.", "BOX 7: +Juna"),
		new ClueText(62, "Hopefully this set of armour will help you to keep surviving.", "DIA 3: +Vyvin"),
		new ClueText(63, "If you're feeling brave, dig beneath the dragon's eye.", "DIA 8: +viyeldi"),
		new ClueText(64, "I lie beneath the first descent to the holy encampment.", "HILT: sara encamp"),
		new ClueText(65, "My life was spared but these voices remain, now guarding these iron gates is my bane.", "KEY: Keymaster"),
		new ClueText(66, "One of several rhyming brothers, in business attire with an obsession for paper work.", "RoR: -Piles"),
		new ClueText(67, "Pentagrams and demons, burnt bones and remains, I wonder what the blood contains.", "ANNA: -bloodrune"),
		new ClueText(68, "Robin wishes to see your finest ranged equipment.", "ECTO: +Robin"),
		new ClueText(69, "She's small but can build both literally and figuratively.", "XER 3: Lovada"),
		new ClueText(70, "Shhhh!", "A LIB: Logosia"),
		new ClueText(71, "Show this to Sherlock.", "HEAD: Sherlock"),
		new ClueText(72, "South of a river in a town surrounded by the undead, what lies beneath the furnace?", "GLOVE: furnace"),
		new ClueText(73, "The far north eastern corner where 1 of 4 was defeated, the shadows still linger.", "FISH: +shadow"),
		new ClueText(74, "This place sure is a mess.", "DIA A: Ewesey"),
		new ClueText(75, "Under a giant robotic bird that cannot fly.", "VARR: museum"),
		new ClueText(76, "Where safe to speak, the man who offers the pouch of smallest size wishes to see your alignment.", "DIA D: Zamorak"),
		// Emote
		new ClueText(77, "Beckon by a collection of crystalline maple trees. Beware of double agents! Equip Bryophyta's staff and a nature tiara.", "TREE 6: n stash"),
		new ClueText(78, "Blow a kiss outside K'ril Tsutsaroth's chamber. Beware of double agents! Equip a Zamorak full helm and the shadow sword.", "HILT: gwd stash"),
		new ClueText(79, "Blow a raspberry at the bank of the Warrior's guild. Beware of double agents! Equip a dragon battleaxe, a slayer helm of any kind and a dragon defender or avernic defender.", "W MAX: stash"),
		new ClueText(80, "Bow in the Iorwerth Camp. Beware of double agents! Equip a charged crystal bow.", "IORW: stash"),
		new ClueText(81, "Cheer in the Entrana church. Beware of double agents! Equip a set of full black dragonhide armour.", "ENTRANA: -stash"),
		new ClueText(82, "Clap in the magic axe hut. Beware of double agents. Equip only flared trousers", "RoR: -hut stash"),
		new ClueText(83, "Cry in the Tzhaar gem store. Beware of the double agents! Equip a fire cape and a TokTz-Xil-Ul'", "DIA 9: stash"),
		new ClueText(84, "Dance in Iban's temple. Beware of double agents! Equip Iban's staff, a black mystic top, and a black mystic bottom.", "ETC 1: stash"),
		new ClueText(85, "Dance in the King Black Dragon Lair|King Black Dragon's lair. Beware of double agents! Equip a black d'hide body, black d'hide vambraces and a black dragon mask.", "GHOR: -stash"),
		new ClueText(86, "Do a jig at the Barrows chest. Beware of double agents! Equip any full barrows set.", "BARROWS: stash"),
		new ClueText(87, "Flap at the Death Altar. Beware of double agents! Equip a death tiara, a legend's cape and any ring of wealth.", "DEATH: +stash"),
		new ClueText(88, "Salute outside the gates of Cam Torum. Beware of double agents! Equip a full set of blue moon equipment.", "C TORUM: +stash"),
		new ClueText(89, "Goblin Salute at the Goblin Village. Beware of double agents! Equip a Bandos platebody, Bandos cloak and Bandos godsword.", "GOBLIN: +stash"),
		new ClueText(90, "Jump for joy in the centre of Zul-Andra. Beware of double agents! Equip a dragon 2h sword, bandos boots and an obsidian cape.", "ZUL: stash"),
		new ClueText(91, "Panic by the big egg where no one dare goes and the ground is burnt. Beware of double agents! Equip a dragon med helm, a TokTz-Ket-Xil, a brine sabre, rune platebody and an uncharged amulet of glory.", "B MAX 1: -stash"),
		new ClueText(92, "Show your anger at the Wise old man. Beware of double agents! Equip an abyssal whip, a cape of legends|legend's cape and some spined chaps.", "GLORY 3: stash"),
		new ClueText(93, "Show your anger towards the Statue of Saradomin in Ellamaria's garden.", "VARR: stash"),
		new ClueText(94, "Slap your head in the centre of the Kourend catacombs. Beware of double agents! Equip arclight or emberlight along with the amulet of the damned.", "XER 4: stash"),
		new ClueText(95, "Spin in front of the Soul Altar. Beware of double agents! Equip a dragon pickaxe, helm of neitiznot and a pair of rune boots.", "SOUL: +stash"),
		new ClueText(96, "Stamp in the Enchanted valley west of the waterfall. Beware of double agents! Equip a dragon axe.", "BKQ: stash"),
		new ClueText(97, "Swing a bullroarer at the top of the Watchtower. Beware of double agents! Equip a dragon plateskirt, climbing boots and a dragon chainbody.", "WATCH: stash"),
		new ClueText(98, "Wave on the northern wall of the Castle Drakan. Beware of double agents! Wear a dragon sq shield, splitbark body and any boater.", "C DRAKAN: stash"),
		new ClueText(99, "Yawn in the 7th room of Pyramid Plunder. Beware of double agents! Equip a pharaoh's sceptre and a full set of menaphite robes.", "PHAR 1: +stash"),
		new ClueText(100, "Think on the western coast of Salvager Overlook. Beware of double agents! Equip a Hueycoatl hide coif and some Hueycoatl hide vambraces.", "QUET: stash"),
		// Falo
		new ClueText(101, "A blood red weapon, a strong curved sword, found on the island of primate lords.", "MUSIC: +d scim"),
		new ClueText(102, "A book that preaches of some great figure, lending strength, might and vigour.", "MUSIC: +god book"),
		new ClueText(103, "A bow of elven craft was made, it shimmers bright, but will soon fade.", "MUSIC: +cr bow"),
		new ClueText(104, "A fiery axe of great inferno, when you use it, you'll wonder where the logs go.", "MUSIC: +infernal axe"),
		new ClueText(105, "A mark used to increase one's grace, found atop a seer's place.", "MUSIC: +grace"),
		new ClueText(106, "A molten beast with fiery breath, you acquire these with its death.", "MUSIC: +lava bone"),
		new ClueText(107, "A shiny helmet of flight, to obtain this with melee, struggle you might.", "MUSIC: +arma helm"),
		new ClueText(108, "A sword held in the other hand, red its colour, Cyclops strength you must withstand.", "MUSIC: +defender"),
		new ClueText(109, "A token used to kill mythical beasts, in hopes of a blade or just for an xp feast.", "MUSIC: +token"),
		new ClueText(110, "Green is my favourite, mature ale I do love, this takes your herblore above.", "MUSIC: +ale"),
		new ClueText(111, "It can hold down a boat or crush a goat, this object, you see, is quite heavy.", "MUSIC: +anchor"),
		new ClueText(112, "It comes from the ground, underneath the snowy plain. Trolls aplenty, with what looks like a mane.", "MUSIC: +basalt"),
		new ClueText(113, "No attack to wield, only strength is required, made of obsidian, but with no room for a shield.", "MUSIC: +maul"),
		new ClueText(114, "Penance healers runners and more, obtaining this body often gives much deplore.", "MUSIC: +torso"),
		new ClueText(115, "Strangely found in a chest, many believe these gloves are the best.", "MUSIC: +bgloves"),
		new ClueText(116, "These gloves of white won't help you fight, but aid in cooking, they just might.", "MUSIC: +gauntlets"),
		new ClueText(117, "They come from some time ago, from a land unto the east. Fossilised they have become, this small and gentle beast.", "MUSIC: +numulite"),
		new ClueText(118, "To slay a dragon you must first do, before this chest piece can be put on you.", "MUSIC: +r platebody"),
		new ClueText(119, "Vampyres are agile opponents, damaged best with a weapon of many components.", "MUSIC: +flail")
	);

	private final Integer fakeId;
	private final String text;
	private final String tag;

	public ClueText(Integer fakeId, String text, String tag)
	{
		this.fakeId = fakeId;
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

	public static Integer forTextGetId(String text)
	{
		for (ClueText clue : CLUES)
		{
			if (clue.text.equals(text))
			{
				return clue.fakeId;
			}
		}

		return null;
	}

	public static ClueText getById(int id)
	{
		for (ClueText clue : ClueText.CLUES)
		{
			if (clue.getFakeId() == id)
			{
				return clue;
			}
		}
		return null;
	}
}
