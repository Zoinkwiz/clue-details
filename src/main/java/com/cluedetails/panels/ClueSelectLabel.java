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
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.ui.components.materialtabs.MaterialTab;

public class ClueSelectLabel extends JLabel
{
	final CluePreferenceManager cluePreferenceManager;
	final Clues clue;

	private static final Border SELECTED_BORDER = new CompoundBorder(
		BorderFactory.createMatteBorder(0, 0, 1, 0, ColorScheme.BRAND_ORANGE),
		BorderFactory.createEmptyBorder(5, 10, 4, 10));

	private static final Border UNSELECTED_BORDER = BorderFactory
		.createEmptyBorder(5, 10, 5, 10);


	public ClueSelectLabel(CluePreferenceManager cluePreferenceManager, Clues clue)
	{
		this.cluePreferenceManager = cluePreferenceManager;
		this.clue = clue;

		setLayout(new BorderLayout());
		setHorizontalAlignment(SwingConstants.LEFT);
		setVerticalAlignment(SwingConstants.TOP);
		setText(generateText(clue.getClueText()));
		setOpaque(true);
		boolean isActive = cluePreferenceManager.getPreference(clue.getClueID());
		setSelected(isActive);
		setHovered(false);

		addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent e)
			{
				if (e.getButton() != MouseEvent.BUTTON1) return;

				boolean currentState = cluePreferenceManager.getPreference(clue.getClueID());
				boolean newState = !currentState;

				setSelected(newState);
				cluePreferenceManager.savePreference(clue.getClueID(), newState);
			}

			@Override
			public void mouseEntered(MouseEvent e)
			{
				setHovered(true);
			}

			@Override
			public void mouseExited(MouseEvent e)
			{
				setHovered(false);
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

	private void setSelected(boolean isSelected)
	{
		if (isSelected)
		{
			setBorder(SELECTED_BORDER);
		}
		else
		{
			setBorder(UNSELECTED_BORDER);
		}
	}

	private void setHovered(boolean isHovered)
	{
		if (isHovered)
		{
			setBackground(ColorScheme.DARK_GRAY_HOVER_COLOR);
		}
		else
		{
			setBackground(ColorScheme.DARKER_GRAY_COLOR);
		}
	}
}
