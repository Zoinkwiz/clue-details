package com.cluedetails.panels;

import lombok.Getter;
import lombok.Setter;
import net.runelite.client.ui.ColorScheme;

import javax.swing.*;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

class ItemPickerJTable extends JTable
{
    @Getter
    private int hoveredRow = -1;
    @Getter
    private int hoveredColumn = -1;

    @Setter
    private Consumer<ItemDetails> onItemSelected;

    @Setter
    private Consumer<ItemDetails> onItemDeselected;

    private final HashSet<ItemDetails> selected = new HashSet<>();

    private final boolean allowMultiSelect;

    public ItemPickerJTable(TableModel tableModel, boolean multiSelect)
    {
        super(tableModel);
        setShowGrid(false);
        setTableHeader(null);

        allowMultiSelect = multiSelect;

        setRowHeight(50);

        addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseExited(MouseEvent e)
            {
                if (hoveredRow != -1 || hoveredColumn != -1)
                {
                    hoveredRow = -1;
                    hoveredColumn = -1;
                    repaint();
                }
            }

            @Override
            public void mousePressed(MouseEvent e)
            {
                Point point = e.getPoint();
                int row = rowAtPoint(point);
                int col = columnAtPoint(point);
                ItemDetails clickedItem = (ItemDetails) tableModel.getValueAt(row, col);
                if (clickedItem != null)
                {
                    if (selected.contains(clickedItem))
                    {
                        selected.remove(clickedItem);
                        if (onItemDeselected != null)
                        {
                            onItemDeselected.accept(clickedItem);
                        }
                    }
                    else if (allowMultiSelect || selected.isEmpty())
                    {
                        selected.add(clickedItem);
                        if (onItemSelected != null)
                        {
                            onItemSelected.accept(clickedItem);
                        }
                    }
                    else
                    {
                        if (onItemDeselected != null)
                        {
                            selected.forEach(onItemDeselected);
                        }
                        selected.clear();
                        selected.add(clickedItem);
                        if (onItemSelected != null)
                        {
                            onItemSelected.accept(clickedItem);
                        }
                    }
                    repaint();
                }
            }
        });

        addMouseMotionListener(new MouseMotionAdapter()
        {
            @Override
            public void mouseMoved(MouseEvent e)
            {
                int row = rowAtPoint(e.getPoint());
                int col = columnAtPoint(e.getPoint());
                if (row != hoveredRow || col != hoveredColumn)
                {
                    if (tableModel.getValueAt(row, col) != null)
                    {
                        hoveredRow = row;
                        hoveredColumn = col;
                    }
                    else
                    {
                        hoveredRow = -1;
                        hoveredColumn = -1;
                    }
                    repaint();
                }
            }
        });
    }

    public Component prepareRenderer(TableCellRenderer renderer, int row, int column)
    {
        Component c = super.prepareRenderer(renderer, row, column);
        c.setBackground(
            row == hoveredRow && column == hoveredColumn ? ColorScheme.DARK_GRAY_HOVER_COLOR : ColorScheme.DARKER_GRAY_COLOR
        );
        return c;
    }

    @Override
    public boolean isCellSelected(int row, int column)
    {
        ItemDetails cellContents = ((ItemTableModel) getModel()).getValueAt(row, column);
        return cellContents != null && selected.contains(cellContents);
    }

    public List<ItemDetails> getSelectedItems()
    {
        return selected.stream().sorted(Comparator.comparing(ItemDetails::getName)).collect(Collectors.toList());
    }

    public void clearSelectedItems()
    {
        if (onItemDeselected != null)
        {
            selected.forEach(onItemDeselected);
        }
        selected.clear();
        repaint();
    }
}
