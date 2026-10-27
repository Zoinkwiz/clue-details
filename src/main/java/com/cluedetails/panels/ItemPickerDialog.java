package com.cluedetails.panels;

import lombok.Setter;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.components.IconTextField;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

class ItemPickerDialog extends JDialog
{
    private final ItemTableModel itemTableModel = new ItemTableModel();
    private final ItemPickerJTable itemTable;
    private final IconTextField searchBar = createSearchBar();

    private final static int FRAME_WIDTH = 600;
    private final static int FRAME_HEIGHT = 300;

    @Setter
    private Consumer<List<ItemDetails>> onConfirm;

    @Setter
    private Consumer<List<ItemDetails>> onCancel;

    public ItemPickerDialog(Window parent, String title, List<ItemDetails> items, boolean allowMultiSelect)
    {
        super(parent, "Item Picker - " + title, ModalityType.MODELESS);

        itemTable = new ItemPickerJTable(itemTableModel, allowMultiSelect);

        setResizable(false);
        setSize(FRAME_WIDTH, FRAME_HEIGHT);

        JPanel content = new JPanel(new BorderLayout(0, 10));
        content.setBorder(new EmptyBorder(15, 15, 15, 15));

        JPanel topPanel = createTopPanel();
        content.add(topPanel, BorderLayout.PAGE_START);

        itemTable.setDefaultRenderer(Object.class, new ItemTableCellRenderer());
        itemTableModel.setItems(items);

        JScrollPane scrollPane = new JScrollPane(itemTable);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(new EmptyBorder(0, 0, 0, 0));
        content.add(scrollPane);

        JPanel buttonPanel = createBottomPanel();

        content.add(buttonPanel, BorderLayout.PAGE_END);

        setContentPane(content);
    }

    private IconTextField createSearchBar()
    {
        IconTextField bar = new IconTextField();

        bar.setIcon(IconTextField.Icon.SEARCH);
        bar.setPreferredSize(new Dimension(FRAME_WIDTH - 20, 30));
        bar.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        bar.setHoverBackgroundColor(ColorScheme.DARK_GRAY_HOVER_COLOR);
        bar.getDocument().addDocumentListener(new DocumentListener()
        {
            @Override
            public void insertUpdate(DocumentEvent e)
            {
                onSearchTextChanged();
            }

            @Override
            public void removeUpdate(DocumentEvent e)
            {
                onSearchTextChanged();
            }

            @Override
            public void changedUpdate(DocumentEvent e)
            {
                onSearchTextChanged();
            }
        });

        return bar;
    }

    private JPanel createTopPanel()
    {
        JPanel topPanel = new JPanel(new BorderLayout(10, 0));
        topPanel.setBorder(new EmptyBorder(0, 0, 0, 0));

        topPanel.add(searchBar);

        JButton clearButton = new JButton("Clear selected");
        clearButton.addActionListener(e ->
            itemTable.clearSelectedItems()
        );
        topPanel.add(clearButton, BorderLayout.EAST);

        return topPanel;
    }

    private JPanel createBottomPanel()
    {
        JPanel bottomPanel = new JPanel();

        JButton confirmButton = new JButton("Confirm");
        confirmButton.addActionListener(e ->
        {
            if (onConfirm != null)
            {
                onConfirm.accept(new ArrayList<>(itemTable.getSelectedItems()));
            }
            setVisible(false);
        });
        bottomPanel.add(confirmButton);

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e ->
        {
            if (onCancel != null)
            {
                onCancel.accept(new ArrayList<>(itemTable.getSelectedItems()));
            }
            setVisible(false);
        });
        bottomPanel.add(cancelButton);
        return bottomPanel;
    }

    private void onSearchTextChanged()
    {
        final String text = searchBar.getText();
        itemTableModel.filterItems(text);
    }

    public void setOnItemSelected(Consumer<ItemDetails> consumer)
    {
        itemTable.setOnItemSelected(consumer);
    }

    public void setOnItemDeselected(Consumer<ItemDetails> consumer)
    {
        itemTable.setOnItemDeselected(consumer);
    }
}
