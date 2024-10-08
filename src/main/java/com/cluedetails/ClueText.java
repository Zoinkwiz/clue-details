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
import net.runelite.api.ItemID;

@Getter
public class ClueText
{
	static final List<ClueText> CLUES = ImmutableList.of(
		// Anagrams
		new ClueText(ItemID.CLUE_SCROLL_MASTER,  "A Elf Knows", "NEX A: snowflake"),
		new ClueText(ItemID.CLUE_SCROLL_MASTER,  "Brucie Catnap", "XER 1: bruce"),
		new ClueText(ItemID.CLUE_SCROLL_MASTER,  "Car If Ices", "ZUL: sacrifice"),
		new ClueText(ItemID.CLUE_SCROLL_MASTER,  "Ded War", "OBEL 6: -edward"),
		new ClueText(ItemID.CLUE_SCROLL_MASTER,  "Dim Tharn", "RoR: -mandrith"),
		new ClueText(ItemID.CLUE_SCROLL_MASTER,  "Duo Plug", "NEX M: dugopul"),
		new ClueText(ItemID.CLUE_SCROLL_MASTER,  "Forlun", "BOX J: runolf"),
		new ClueText(ItemID.CLUE_SCROLL_MASTER,  "Im N Zezim", "ZANA: immenizz"),
		new ClueText(ItemID.CLUE_SCROLL_MASTER,  "Mal in Tau", "LEGS: luminata"),
		new ClueText(ItemID.CLUE_SCROLL_MASTER,  "Mold La Ran", "DRAK 1: old man"),
		new ClueText(ItemID.CLUE_SCROLL_MASTER,  "Mus Kil Reader", "QPC: +radimus"),
		new ClueText(ItemID.CLUE_SCROLL_MASTER,  "Rip Maul", "MYTH: +primula"),
		new ClueText(ItemID.CLUE_SCROLL_MASTER,  "Rue Go", "ETC 1: goreu"),
		new ClueText(ItemID.CLUE_SCROLL_MASTER,  "Slam Duster Grail", "BOX H: lars"),
		new ClueText(ItemID.CLUE_SCROLL_MASTER,  "Ten Wigs On", "NARDAH: wingstone"),
		new ClueText(ItemID.CLUE_SCROLL_MASTER,  "Twenty Cure Iron", "BOTD 3: tony"),
		// Coordinate
		new ClueText(ItemID.CLUE_SCROLL_MASTER,  "01 degree 30 minutes north 08 degrees 11 minutes west", "IORW: s dig"),
		new ClueText(ItemID.CLUE_SCROLL_MASTER,  "01 degrees 54 minutes south 08 degrees 54 minutes west", "ZUL: nw dig"),
		new ClueText(ItemID.CLUE_SCROLL_MASTER,  "02 degrees 09 minutes south 06 degrees 58 minutes west", "DLR: se dig"),
		new ClueText(ItemID.CLUE_SCROLL_MASTER,  "03 degrees 09 minutes south 43 degrees 26 minutes east", "MOS LE: island dig"),
		new ClueText(ItemID.CLUE_SCROLL_MASTER,  "03 degrees 26 minutes north 12 degrees 18 minutes east", "DIA 9: crandor dig"),
		new ClueText(ItemID.CLUE_SCROLL_MASTER,  "03 degrees 50 minutes north 09 degrees 07 minutes east", "ARDY: e dig"),
		new ClueText(ItemID.CLUE_SCROLL_MASTER,  "04 degrees 58 minutes north 36 degrees 56 minutes east", "DRAK 1: +mines dig"),
		new ClueText(ItemID.CLUE_SCROLL_MASTER,  "05 degrees 13 minutes north 04 degrees 16 minutes west", "ETC 2: e dig"),
		new ClueText(ItemID.CLUE_SCROLL_MASTER,  "07 degrees 37 minutes north 35 degrees 18 minutes east", "MORT: +boat dig"),
		new ClueText(ItemID.CLUE_SCROLL_MASTER,  "08 degrees 01 minutes north 20 degrees 58 minutes west", "POH 5: cc isle dig"),
		new ClueText(ItemID.CLUE_SCROLL_MASTER,  "08 degrees 11 minutes north 12 degrees 30 minutes east", "POH 3: water dig"),
		new ClueText(ItemID.CLUE_SCROLL_MASTER,  "12 degrees 35 minutes north 36 degrees 22 minutes east", "FENK: ship dig"),
		new ClueText(ItemID.CLUE_SCROLL_MASTER,  "12 degrees 45 minutes north 20 degrees 09 minutes east", "PADD: air dig"),
		new ClueText(ItemID.CLUE_SCROLL_MASTER,  "13 degrees 33 minutes south 15 degrees 26 minutes east", "APE ATOLL: lumdo dig"),
		new ClueText(ItemID.CLUE_SCROLL_MASTER,  "16 degrees 41 minutes north 30 degrees 54 minutes west", "DJR: nw dig"),
		new ClueText(ItemID.CLUE_SCROLL_MASTER,  "18 degrees 03 minutes north 03 degrees 03 minutes east", "W BIRTH: top dig"),
		new ClueText(ItemID.CLUE_SCROLL_MASTER,  "18 degrees 26 minutes north 37 degrees 15 minutes west", "TREE B: dig"),
		new ClueText(ItemID.CLUE_SCROLL_MASTER,  "19 degrees 43 minutes north 23 degrees 11 minutes west", "BOTD 5: church dig"),
		new ClueText(ItemID.CLUE_SCROLL_MASTER,  "20 degrees 35 minutes north 15 degrees 58 minutes east", "GHOR: -temple dig"),
		new ClueText(ItemID.CLUE_SCROLL_MASTER,  "20 degrees 45 minutes north 7 degrees 26 degrees west", "W BIRTH: cove dig"),
		new ClueText(ItemID.CLUE_SCROLL_MASTER,  "21 degrees 37 minutes north 21 degrees 13 minutes west", "CIS: mine dig"),
		new ClueText(ItemID.CLUE_SCROLL_MASTER,  "21 degrees 56 minutes north 10 degrees 56 minutes west", "LUNAR: s dig"),
		new ClueText(ItemID.CLUE_SCROLL_MASTER,  "22 degrees 24 minutes north 31 degrees 11 minutes west", "XER 3: nw dig"),
		new ClueText(ItemID.CLUE_SCROLL_MASTER,  "24 degrees 00 minutes north 29 degrees 22 minutes east", "OBEL 6: -volcano dig"),
		new ClueText(ItemID.CLUE_SCROLL_MASTER,  "24 degrees 18 minutes north 23 degrees 22 minutes east", "RoR: -resource dig"),
		new ClueText(ItemID.CLUE_SCROLL_MASTER,  "24 degrees 22 minutes north 27 degrees 00 minutes east", "OBEL 6: -n dig"),
		new ClueText(ItemID.CLUE_SCROLL_MASTER,  "23 degrees 58 minutes north 18 degrees 22 minutes east", "ICE: -e dig"),
		// Cryptic
		new ClueText(ItemID.CLUE_SCROLL_MASTER,  "2 musical birds. Dig in front of the spinning light.", "DKS: +penguin"),
		new ClueText(ItemID.CLUE_SCROLL_MASTER,  "A chisel and hammer reside in his home, strange for one of magic. Impress him with your magical equipment.", "ARDY: +Wizard"),
		new ClueText(ItemID.CLUE_SCROLL_MASTER,  "A dwarf, approaching death, but very much in the light.", "DEATH: +Thorgel"),
		new ClueText(ItemID.CLUE_SCROLL_MASTER,  "A massive battle rages beneath so be careful when you dig by the large broken crossbow.", "HILT: ne climb"),
		new ClueText(ItemID.CLUE_SCROLL_MASTER,  "Anger Abbot Langley.", "BOX B: +Abbot"),
		new ClueText(ItemID.CLUE_SCROLL_MASTER,  "Anger those who adhere to Saradomin's edicts to prevent travel.", "TREE 7: monk"),
		new ClueText(ItemID.CLUE_SCROLL_MASTER,  "Buried beneath the ground, who knows where it's found.", "HOTNCOLD"),
		new ClueText(ItemID.CLUE_SCROLL_MASTER,  "Lucky for you, A man called Jorral may have a clue.", "HOTNCOLD"),
		new ClueText(ItemID.CLUE_SCROLL_MASTER,  "Come brave adventurer, your sense is on fire. If you talk to me, it's an old god you desire.", "VIGGORA: +ring"),
		new ClueText(ItemID.CLUE_SCROLL_MASTER,  "Darkness wanders around me, but fills my mind with knowledge.", "A LIB: Biblia"),
		new ClueText(ItemID.CLUE_SCROLL_MASTER,  "Dig in front of the icy arena where 1 of 4 was fought.", "HILT: kamil"),
		new ClueText(ItemID.CLUE_SCROLL_MASTER,  "Faint sounds of 'Arr', fire giants found deep, the eastern tip of a lake, are the rewards you could reap.", "ICE: -dwd"),
		new ClueText(ItemID.CLUE_SCROLL_MASTER,  "Falo the bard wants to see you.", "MUSIC"),
		new ClueText(ItemID.CLUE_SCROLL_MASTER,  "Elvish onions.", "ETC 2: onion"),
		new ClueText(ItemID.CLUE_SCROLL_MASTER,  "Fiendish cooks probably won't dig the dirty dishes.", "BOX 4: rogue's den"),
		new ClueText(ItemID.CLUE_SCROLL_MASTER,  "The doorman of the Warriors' Guild wishes to be impressed by how strong your equipment is.", "W MAX: Ghommal"),
		new ClueText(ItemID.CLUE_SCROLL_MASTER,  "Great demons, dragons, and spiders protect this blue rock, beneath which, you may find what you seek.", "REV: -maze"),
		new ClueText(ItemID.CLUE_SCROLL_MASTER,  "Guthix left his mark in a fiery lake, dig at the tip of it.", "ICE: -lake"),
		new ClueText(ItemID.CLUE_SCROLL_MASTER,  "Here, there are tears, but nobody is crying. Speak to the guardian and show off your alignment to balance.", "BOX 7: +Juna"),
		new ClueText(ItemID.CLUE_SCROLL_MASTER,  "Hopefully this set of armour will help you to keep surviving.", "DIA 3: +Vyvin"),
		new ClueText(ItemID.CLUE_SCROLL_MASTER,  "If you're feeling brave, dig beneath the dragon's eye.", "DIA 8: +viyeldi"),
		new ClueText(ItemID.CLUE_SCROLL_MASTER,  "I lie beneath the first descent to the holy encampment.", "HILT: sara encamp"),
		new ClueText(ItemID.CLUE_SCROLL_MASTER,  "My life was spared but these voices remain, now guarding these iron gates is my bane.", "KEY: Keymaster"),
		new ClueText(ItemID.CLUE_SCROLL_MASTER,  "One of several rhyming brothers, in business attire with an obsession for paper work.", "RoR: -Piles"),
		new ClueText(ItemID.CLUE_SCROLL_MASTER,  "Pentagrams and demons, burnt bones and remains, I wonder what the blood contains.", "ANNA: -bloodrune"),
		new ClueText(ItemID.CLUE_SCROLL_MASTER,  "Robin wishes to see your finest ranged equipment.", "ECTO: +Robin"),
		new ClueText(ItemID.CLUE_SCROLL_MASTER,  "She's small but can build both literally and figuratively.", "XER 3: Lovada"),
		new ClueText(ItemID.CLUE_SCROLL_MASTER,  "Shhhh!", "A LIB: Logosia"),
		new ClueText(ItemID.CLUE_SCROLL_MASTER,  "Show this to Sherlock.", "HEAD: Sherlock"),
		new ClueText(ItemID.CLUE_SCROLL_MASTER,  "South of a river in a town surrounded by the undead, what lies beneath the furnace?", "GLOVE: furnace"),
		new ClueText(ItemID.CLUE_SCROLL_MASTER,  "The far north eastern corner where 1 of 4 was defeated, the shadows still linger.", "FISH: +shadow"),
		new ClueText(ItemID.CLUE_SCROLL_MASTER,  "This place sure is a mess.", "DIA A: Ewesey"),
		new ClueText(ItemID.CLUE_SCROLL_MASTER,  "Under a giant robotic bird that cannot fly.", "VARR: museum"),
		new ClueText(ItemID.CLUE_SCROLL_MASTER,  "Where safe to speak, the man who offers the pouch of smallest size wishes to see your alignment.", "DIA D: Zamorak"),
		// Emote
		new ClueText(ItemID.CLUE_SCROLL_MASTER,  "Beckon by a collection of crystalline maple trees. Beware of double agents! Equip Bryophyta's staff and a nature tiara.", "TREE 6: n stash"),
		new ClueText(ItemID.CLUE_SCROLL_MASTER,  "Blow a kiss outside K'ril Tsutsaroth's chamber. Beware of double agents! Equip a Zamorak full helm and the shadow sword.", "HILT: gwd stash"),
		new ClueText(ItemID.CLUE_SCROLL_MASTER,  "Blow a raspberry at the bank of the Warrior's guild. Beware of double agents! Equip a dragon battleaxe, a slayer helm of any kind and a dragon defender or avernic defender.", "W MAX: stash"),
		new ClueText(ItemID.CLUE_SCROLL_MASTER,  "Bow in the Iorwerth Camp. Beware of double agents! Equip a charged crystal bow.", "IORW: stash"),
		new ClueText(ItemID.CLUE_SCROLL_MASTER,  "Cheer in the Entrana church. Beware of double agents! Equip a set of full black dragonhide armour.", "ENTRANA: -stash"),
		new ClueText(ItemID.CLUE_SCROLL_MASTER,  "Clap in the magic axe hut. Beware of double agents. Equip only flared trousers", "RoR: -hut stash"),
		new ClueText(ItemID.CLUE_SCROLL_MASTER,  "Cry in the Tzhaar gem store. Beware of the double agents! Equip a fire cape and a TokTz-Xil-Ul'", "DIA 9: stash"),
		new ClueText(ItemID.CLUE_SCROLL_MASTER,  "Dance in Iban's temple. Beware of double agents! Equip Iban's staff, a black mystic top, and a black mystic bottom.", "ETC 1: stash"),
		new ClueText(ItemID.CLUE_SCROLL_MASTER,  "Dance in the King Black Dragon Lair|King Black Dragon's lair. Beware of double agents! Equip a black d'hide body, black d'hide vambraces and a black dragon mask.", "GHOR: -stash"),
		new ClueText(ItemID.CLUE_SCROLL_MASTER,  "Do a jig at the Barrows chest. Beware of double agents! Equip any full barrows set.", "BARROWS: stash"),
		new ClueText(ItemID.CLUE_SCROLL_MASTER,  "Flap at the Death Altar. Beware of double agents! Equip a death tiara, a legend's cape and any ring of wealth.", "DEATH: +stash"),
		new ClueText(ItemID.CLUE_SCROLL_MASTER,  "Salute outside the gates of Cam Torum. Beware of double agents! Equip a full set of blue moon equipment.", "C TORUM: +stash"),
		new ClueText(ItemID.CLUE_SCROLL_MASTER,  "Goblin Salute at the Goblin Village. Beware of double agents! Equip a Bandos platebody, Bandos cloak and Bandos godsword.", "GOBLIN: +stash"),
		new ClueText(ItemID.CLUE_SCROLL_MASTER,  "Jump for joy in the centre of Zul-Andra. Beware of double agents! Equip a dragon 2h sword, bandos boots and an obsidian cape.", "ZUL: stash"),
		new ClueText(ItemID.CLUE_SCROLL_MASTER,  "Panic by the big egg where no one dare goes and the ground is burnt. Beware of double agents! Equip a dragon med helm, a TokTz-Ket-Xil, a brine sabre, rune platebody and an uncharged amulet of glory.", "B MAX 1: -stash"),
		new ClueText(ItemID.CLUE_SCROLL_MASTER,  "Show your anger at the Wise old man. Beware of double agents! Equip an abyssal whip, a cape of legends|legend's cape and some spined chaps.", "GLORY 3: stash"),
		new ClueText(ItemID.CLUE_SCROLL_MASTER,  "Show your anger towards the Statue of Saradomin in Ellamaria's garden.", "VARR: stash"),
		new ClueText(ItemID.CLUE_SCROLL_MASTER,  "Slap your head in the centre of the Kourend catacombs. Beware of double agents! Equip arclight or emberlight along with the amulet of the damned.", "XER 4: stash"),
		new ClueText(ItemID.CLUE_SCROLL_MASTER,  "Spin in front of the Soul Altar. Beware of double agents! Equip a dragon pickaxe, helm of neitiznot and a pair of rune boots.", "SOUL: +stash"),
		new ClueText(ItemID.CLUE_SCROLL_MASTER,  "Stamp in the Enchanted valley west of the waterfall. Beware of double agents! Equip a dragon axe.", "BKQ: stash"),
		new ClueText(ItemID.CLUE_SCROLL_MASTER,  "Swing a bullroarer at the top of the Watchtower. Beware of double agents! Equip a dragon plateskirt, climbing boots and a dragon chainbody.", "WATCH: stash"),
		new ClueText(ItemID.CLUE_SCROLL_MASTER,  "Wave on the northern wall of the Castle Drakan. Beware of double agents! Wear a dragon sq shield, splitbark body and any boater.", "C DRAKAN: stash"),
		new ClueText(ItemID.CLUE_SCROLL_MASTER,  "Yawn in the 7th room of Pyramid Plunder. Beware of double agents! Equip a pharaoh's sceptre and a full set of menaphite robes.", "PHAR 1: +stash"),
		new ClueText(ItemID.CLUE_SCROLL_MASTER,  "Think on the western coast of Salvager Overlook. Beware of double agents! Equip a Hueycoatl hide coif and some Hueycoatl hide vambraces.", "QUET: stash"),
		// Falo
		new ClueText(ItemID.CLUE_SCROLL_MASTER,  "A blood red weapon, a strong curved sword, found on the island of primate lords.", "MUSIC: +d scim"),
		new ClueText(ItemID.CLUE_SCROLL_MASTER,  "A book that preaches of some great figure, lending strength, might and vigour.", "MUSIC: +god book"),
		new ClueText(ItemID.CLUE_SCROLL_MASTER,  "A bow of elven craft was made, it shimmers bright, but will soon fade.", "MUSIC: +cr bow"),
		new ClueText(ItemID.CLUE_SCROLL_MASTER,  "A fiery axe of great inferno, when you use it, you'll wonder where the logs go.", "MUSIC: +infernal axe"),
		new ClueText(ItemID.CLUE_SCROLL_MASTER,  "A mark used to increase one's grace, found atop a seer's place.", "MUSIC: +grace"),
		new ClueText(ItemID.CLUE_SCROLL_MASTER,  "A molten beast with fiery breath, you acquire these with its death.", "MUSIC: +lava bone"),
		new ClueText(ItemID.CLUE_SCROLL_MASTER,  "A shiny helmet of flight, to obtain this with melee, struggle you might.", "MUSIC: +arma helm"),
		new ClueText(ItemID.CLUE_SCROLL_MASTER,  "A sword held in the other hand, red its colour, Cyclops strength you must withstand.", "MUSIC: +defender"),
		new ClueText(ItemID.CLUE_SCROLL_MASTER,  "A token used to kill mythical beasts, in hopes of a blade or just for an xp feast.", "MUSIC: +token"),
		new ClueText(ItemID.CLUE_SCROLL_MASTER,  "Green is my favourite, mature ale I do love, this takes your herblore above.", "MUSIC: +ale"),
		new ClueText(ItemID.CLUE_SCROLL_MASTER,  "It can hold down a boat or crush a goat, this object, you see, is quite heavy.", "MUSIC: +anchor"),
		new ClueText(ItemID.CLUE_SCROLL_MASTER,  "It comes from the ground, underneath the snowy plain. Trolls aplenty, with what looks like a mane.", "MUSIC: +basalt"),
		new ClueText(ItemID.CLUE_SCROLL_MASTER,  "No attack to wield, only strength is required, made of obsidian, but with no room for a shield.", "MUSIC: +maul"),
		new ClueText(ItemID.CLUE_SCROLL_MASTER,  "Penance healers runners and more, obtaining this body often gives much deplore.", "MUSIC: +torso"),
		new ClueText(ItemID.CLUE_SCROLL_MASTER,  "Strangely found in a chest, many believe these gloves are the best.", "MUSIC: +bgloves"),
		new ClueText(ItemID.CLUE_SCROLL_MASTER,  "These gloves of white won't help you fight, but aid in cooking, they just might.", "MUSIC: +gauntlets"),
		new ClueText(ItemID.CLUE_SCROLL_MASTER,  "They come from some time ago, from a land unto the east. Fossilised they have become, this small and gentle beast.", "MUSIC: +numulite"),
		new ClueText(ItemID.CLUE_SCROLL_MASTER,  "To slay a dragon you must first do, before this chest piece can be put on you.", "MUSIC: +r platebody"),
		new ClueText(ItemID.CLUE_SCROLL_MASTER,  "Vampyres are agile opponents, damaged best with a weapon of many components.", "MUSIC: +flail")
	);

	private final Integer clueTier;
	private final String text;
	private final String tag;

	public ClueText(Integer clueTier, String text, String tag)
	{
		this.clueTier = clueTier;
		this.text = text;
		this.tag = tag;
	}

	public Integer getFakeId()
	{
		return getText().hashCode();
	}

	public static Integer forTextGetId(String text)
	{
		for (ClueText clue : CLUES)
		{
			if (clue.text.equals(text))
			{
				return clue.getFakeId();
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
