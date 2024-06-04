package com.cluedetails.panels;

import com.cluedetails.ClueDetailsConfig;
import com.cluedetails.CluePreferenceManager;
import com.cluedetails.Clues;
import com.cluedetails.filters.ClueTier;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.ui.components.IconTextField;
import net.runelite.client.util.Text;

public class ClueDetailsParentPanel extends PluginPanel
{
	private final IconTextField searchBar = new IconTextField();
	JPanel searchCluesPanel = new JPanel();
	JPanel clueListPanel = new JPanel();
	private final JComboBox<Enum> tierFilterDropdown;

	public static final int DROPDOWN_HEIGHT = 26;
	private final ArrayList<ClueSelectLabel> clueSelectLabels = new ArrayList<>();

	private ConfigManager configManager;

	private CluePreferenceManager cluePreferenceManager;
	private final ClueDetailsConfig config;

	private final JScrollPane scrollableContainer;
	private final FixedWidthPanel clueListWrapper = new FixedWidthPanel();

	private final JPanel allDropdownSections = new JPanel();


	public ClueDetailsParentPanel(ConfigManager configManager, CluePreferenceManager cluePreferenceManager, ClueDetailsConfig config)
	{
		super(false);

		this.configManager = configManager;
		this.cluePreferenceManager = cluePreferenceManager;
		this.config = config;

		setBackground(ColorScheme.DARK_GRAY_COLOR);
		setLayout(new BorderLayout());

		/* Setup overview panel */
		JPanel titlePanel = new JPanel();
		titlePanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		titlePanel.setLayout(new BorderLayout());

		JLabel title = new JLabel();
		title.setText("Clue Details");
		title.setForeground(Color.WHITE);
		titlePanel.add(title, BorderLayout.WEST);

		// Options
		final JPanel viewControls = new JPanel(new GridLayout(1, 3, 10, 0));
		viewControls.setBackground(ColorScheme.DARK_GRAY_COLOR);

		/* Search bar */
		searchBar.setIcon(IconTextField.Icon.SEARCH);
		searchBar.setPreferredSize(new Dimension(PluginPanel.PANEL_WIDTH - 20, 30));
		searchBar.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		searchBar.setHoverBackgroundColor(ColorScheme.DARK_GRAY_HOVER_COLOR);
		searchBar.getDocument().addDocumentListener(new DocumentListener()
		{
			@Override
			public void insertUpdate(DocumentEvent e)
			{
				onSearchBarChanged();
			}

			@Override
			public void removeUpdate(DocumentEvent e)
			{
				onSearchBarChanged();
			}

			@Override
			public void changedUpdate(DocumentEvent e)
			{
				onSearchBarChanged();
			}
		});

		searchCluesPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		searchCluesPanel.setLayout(new BorderLayout(0, BORDER_OFFSET));
		searchCluesPanel.add(searchBar, BorderLayout.CENTER);

		clueListPanel.setBorder(new EmptyBorder(8, 10, 0, 10));
		clueListPanel.setLayout(new DynamicPaddedGridLayout(0, 1, 0, 5));
		clueListPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		showMatchingClues("");

		// Filters
		tierFilterDropdown = makeNewDropdown(ClueDetailsConfig.ClueTierFilter.displayFilters(), "filterListByTier");
		JPanel filtersPanel = makeDropdownPanel(tierFilterDropdown, "Tier");
		filtersPanel.setPreferredSize(new Dimension(PANEL_WIDTH, DROPDOWN_HEIGHT));

		allDropdownSections.setLayout(new BoxLayout(allDropdownSections, BoxLayout.Y_AXIS));
		allDropdownSections.setBorder(new EmptyBorder(0, 0, 10, 0));
		allDropdownSections.add(filtersPanel);
//		allDropdownSections.add(difficultyPanel);
//		allDropdownSections.add(orderPanel);
//		allDropdownSections.add(skillsFilterPanel);

		searchCluesPanel.add(allDropdownSections, BorderLayout.NORTH);

		clueListWrapper.setLayout(new BorderLayout());
		clueListWrapper.add(clueListPanel, BorderLayout.NORTH);

		scrollableContainer = new JScrollPane(clueListWrapper);
		scrollableContainer.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

		JPanel introDetailsPanel = new JPanel();
		introDetailsPanel.setLayout(new BorderLayout());
		introDetailsPanel.add(titlePanel, BorderLayout.NORTH);
		introDetailsPanel.add(searchCluesPanel, BorderLayout.CENTER);

		add(introDetailsPanel, BorderLayout.NORTH);
		add(scrollableContainer, BorderLayout.CENTER);

		refresh();
	}

	private JComboBox<Enum> makeNewDropdown(Enum[] values, String key)
	{
		JComboBox<Enum> dropdown = new JComboBox<>(values);
		dropdown.setFocusable(false);
		dropdown.setForeground(Color.WHITE);
		dropdown.setRenderer(new DropdownRenderer());
		dropdown.addItemListener(e ->
		{
			if (e.getStateChange() == ItemEvent.SELECTED)
			{
				Enum source = (Enum) e.getItem();
				configManager.setConfiguration(ClueDetailsConfig.class.getAnnotation(ConfigGroup.class).value(), key,
					source);
			}
		});

		return dropdown;
	}

	private JPanel makeDropdownPanel(JComboBox dropdown, String name)
	{
		// Filters
		JLabel filterName = new JLabel(name);
		filterName.setForeground(Color.WHITE);

		JPanel filtersPanel = new JPanel();
		filtersPanel.setLayout(new BorderLayout());
		filtersPanel.setBorder(new EmptyBorder(0, 0, BORDER_OFFSET, 0));
		filtersPanel.setMinimumSize(new Dimension(PANEL_WIDTH, BORDER_OFFSET));
		filtersPanel.add(filterName, BorderLayout.CENTER);
		filtersPanel.add(dropdown, BorderLayout.EAST);

		return filtersPanel;
	}

	private void showMatchingClues(String text)
	{
		if (text.isEmpty())
		{
			clueSelectLabels.forEach(clueListPanel::add);
			return;
		}

		final String[] searchTerms = text.toLowerCase().split(" ");

		clueSelectLabels.forEach(listItem ->
		{
			if (Text.matchesSearchTerms(Arrays.asList(searchTerms), listItem.getKeywords()))
			{
				clueListPanel.add(listItem);
			}
		});
	}

	private void onSearchBarChanged()
	{
		final String text = searchBar.getText();

		clueSelectLabels.forEach(clueListPanel::remove);
		showMatchingClues(text);

		revalidate();
	}

	public void refresh()
	{
		clueSelectLabels.forEach(clueListPanel::remove);
		clueSelectLabels.clear();

		tierFilterDropdown.setSelectedItem(config.filterListByTier());

		List<Clues> filteredClues = Arrays.stream(Clues.values())
			.filter(config.filterListByTier())
			.collect(Collectors.toList());

		for (Clues clue : filteredClues)
		{
			clueSelectLabels.add(new ClueSelectLabel(cluePreferenceManager, clue));
		}

		showMatchingClues(searchBar.getText() != null ? searchBar.getText() : "");

		repaint();
		revalidate();
	}
}
