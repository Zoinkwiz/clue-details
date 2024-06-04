package com.cluedetails.panels;

import com.cluedetails.CluePreferenceManager;
import com.cluedetails.Clues;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;

public class ClueSelectLabel extends JLabel
{
	final CluePreferenceManager cluePreferenceManager;
	final Clues clue;

	public ClueSelectLabel(CluePreferenceManager cluePreferenceManager, Clues clue)
	{
		this.cluePreferenceManager = cluePreferenceManager;
		this.clue = clue;

		setLayout(new BorderLayout());
		setHorizontalAlignment(SwingConstants.LEFT);
		setVerticalAlignment(SwingConstants.TOP);
		setBackground(ColorScheme.DARKER_GRAY_COLOR);
		setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createMatteBorder(1, 0, 1, 0, ColorScheme.DARK_GRAY_COLOR.brighter()),
			BorderFactory.createEmptyBorder(5, 5, 10, 0)
		));
		setText(generateText(clue.getClueText()));
		setOpaque(true);
		boolean isActive = cluePreferenceManager.getPreference(clue.getClueID());
		setBackground(isActive ? Color.GREEN.darker() : Color.RED.darker());

		addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent e)
			{
				boolean currentState = cluePreferenceManager.getPreference(clue.getClueID());
				boolean newState = !currentState;

				setBackground(newState ? Color.GREEN.darker() : Color.RED.darker());
				cluePreferenceManager.savePreference(clue.getClueID(), newState);
			}
		});
	}


	public ClueSelectLabel(String text)
	{
		this.cluePreferenceManager = null;
		this.clue = null;

		setLayout(new BorderLayout(3, 3));
		setPreferredSize(new Dimension(PluginPanel.PANEL_WIDTH, 30));
		setBackground(ColorScheme.DARKER_GRAY_COLOR);

		JLabel nameLabel = new JLabel(text);
		Color color = Color.WHITE;
		nameLabel.setForeground(color);
		add(nameLabel, BorderLayout.CENTER);
	}

	public String generateText(String clueText)
	{
		return "<html><body style='text-align:left'>" + clueText + "</body></html>";
	}

	public List<String> getKeywords()
	{
		if (clue == null) return List.of();
		return Arrays.asList(clue.getClueText().toLowerCase().split(" "));
	}
}
