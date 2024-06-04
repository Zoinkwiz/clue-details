package com.cluedetails.panels;

import com.cluedetails.CluePreferenceManager;
import com.cluedetails.Clues;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import net.runelite.client.ui.ColorScheme;

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

	public String generateText(String clueText)
	{
		StringBuilder text = new StringBuilder();
		text.append(clueText);

		return "<html><body style='text-align:left'>" + text + "</body></html>";
	}

	public List<String> getKeywords()
	{
		return Arrays.asList(clue.getClueText().toLowerCase().split(" "));
	}
}
