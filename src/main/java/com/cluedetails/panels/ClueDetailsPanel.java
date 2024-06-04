package com.cluedetails.panels;

import com.cluedetails.CluePreferenceManager;
import com.cluedetails.Clues;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;

public class ClueDetailsPanel extends PluginPanel
{
	private final CluePreferenceManager cluePreferenceManager;

	public ClueDetailsPanel(CluePreferenceManager cluePreferenceManager)
	{
		this.cluePreferenceManager = cluePreferenceManager;

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
		panel.setText(generateText(clue.getClueText()));
		panel.setOpaque(true);
		boolean isActive = cluePreferenceManager.getPreference(clue.getClueID());
		panel.setBackground(isActive ? Color.GREEN.darker() : Color.RED.darker());

		panel.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent e)
			{
				boolean currentState = cluePreferenceManager.getPreference(clue.getClueID());
				boolean newState = !currentState;

				panel.setBackground(newState ? Color.GREEN.darker() : Color.RED.darker());
				cluePreferenceManager.savePreference(clue.getClueID(), newState);
			}
		});

		return panel;
	}
}
