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
package com.cluedetails;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;

public class ClueDetailsPanel extends PluginPanel
{
	private final ConfigManager configManager;
	private final ClueDetailsConfig config;

	private final Map<Integer, Boolean> clueStates = new HashMap<>();

	public ClueDetailsPanel(ConfigManager configManager, ClueDetailsConfig config)
	{
		this.configManager = configManager;
		this.config = config;

		// Load the saved state
		loadState();

		setLayout(new BorderLayout(0, 1));
		setBorder(new EmptyBorder(5, 0, 0, 0));

		JPanel body = new JPanel();
		body.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		body.setLayout(new BorderLayout());
		body.setBorder(new EmptyBorder(10, 5, 10, 5));

		JPanel questStepsPanel = new JPanel();
		questStepsPanel.setLayout(new BoxLayout(questStepsPanel, BoxLayout.Y_AXIS));
		questStepsPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);

		for (Clues clue : Clues.CLUES)
		{
			questStepsPanel.add(createClueToggle(clue));
			questStepsPanel.add(new JSeparator(JSeparator.HORIZONTAL));
		}
		body.add(questStepsPanel);

		add(questStepsPanel);
	}

	private void loadState()
	{
		for (Clues clue : Clues.CLUES)
		{
			boolean state = Boolean.TRUE.equals(configManager.getConfiguration("clue-details-highlights", String.valueOf(clue.getClueID()), Boolean.class));
			clueStates.put(clue.getClueID(), state);
		}
	}

	public String generateText(String clueText)
	{
		return "<html><body style='text-align:left'>" + clueText + "</body></html>";
	}

	private JLabel createClueToggle(Clues clue)
	{
		JLabel panel = new JLabel();
		panel.setLayout(new BorderLayout());
		panel.setHorizontalAlignment(SwingConstants.LEFT);
		panel.setVerticalAlignment(SwingConstants.TOP);
		panel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		panel.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createMatteBorder(1, 0, 1, 0, ColorScheme.DARK_GRAY_COLOR.brighter()),
			BorderFactory.createEmptyBorder(5, 5, 10, 0)
		));
		panel.setText(generateText(clue.getDetail(configManager)));
		panel.setOpaque(true);
		boolean isActive = clueStates.getOrDefault(clue.getClueID(), false);
		panel.setBackground(isActive ? Color.GREEN.darker() : Color.RED.darker());

		panel.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent e)
			{
				boolean currentState = clueStates.getOrDefault(clue.getClueID(), false);
				boolean newState = !currentState;

				clueStates.put(clue.getClueID(), newState);
				panel.setBackground(newState ? Color.GREEN.darker() : Color.RED.darker());
				configManager.setConfiguration("clue-details-highlights", String.valueOf(clue.getClueID()), newState);
			}
		});

		return panel;
	}
}
