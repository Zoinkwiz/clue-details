package com.cluedetails;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextPane;
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

		for (Clues clue : Clues.values())
		{
			questStepsPanel.add(createClueToggle(clue));
			questStepsPanel.add(new JSeparator(JSeparator.HORIZONTAL));
		}
		body.add(questStepsPanel);

		add(questStepsPanel);
	}

	private void loadState()
	{
		for (Clues clue : Clues.values())
		{
			boolean state = Boolean.TRUE.equals(configManager.getConfiguration("clue-details-highlights", String.valueOf(clue.getClueID()), Boolean.class));
			clueStates.put(clue.getClueID(), state);
		}
	}

	public String generateText(String clueText)
	{
		StringBuilder text = new StringBuilder();
		text.append(clueText);

		return "<html><body style='text-align:left'>" + text + "</body></html>";
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
		panel.setText(generateText(clue.getDisplayText(configManager)));
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
