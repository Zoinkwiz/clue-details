package com.cluedetails.panels;

import lombok.extern.slf4j.Slf4j;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

@Slf4j
class ItemTableCellRenderer extends JPanel implements TableCellRenderer
{
    private final JLabel iconLabel = new JLabel();
    private final JLabel nameLabel = new JLabel();
    private final JLabel idLabel = new JLabel();
    private final static EmptyBorder UNSELECTED_BORDER = new EmptyBorder(5, 5, 5, 5);
    private final static CompoundBorder SELECTED_BORDER = new CompoundBorder(
        UNSELECTED_BORDER,
        new LineBorder(ColorScheme.BRAND_ORANGE, 1)
    );
    private final static int NAME_PADDING = 20;

    public ItemTableCellRenderer()
    {
        setOpaque(true);

        // Definitely a better way to do this
        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        c.gridx = 0;
        c.gridy = 0;
        c.gridheight = 4;
        c.gridwidth = 1;
        add(iconLabel, c);

        idLabel.setFont(FontManager.getRunescapeSmallFont());
        c.gridx = 1;
        c.gridy = 3;
        c.gridheight = 1;
        c.gridwidth = 5;
        add(idLabel, c);

        nameLabel.setHorizontalAlignment(JLabel.CENTER);
        c.gridx = 1;
        c.gridy = 2;
        c.gridheight = 1;
        c.gridwidth = 5;
        add(nameLabel, c);

        setBorder(UNSELECTED_BORDER);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
    {
        if (value != null)
        {
            ItemDetails details = (ItemDetails) value;
            String name = details.getName();
            ImageIcon icon = details.getIcon();
            nameLabel.setText(name);
            iconLabel.setIcon(icon);
            idLabel.setText(String.format("(ID: %d)", details.getId()));
            int columnWidth = table.getColumnModel().getColumn(column).getWidth();
            int nameWidth = columnWidth - (icon.getIconWidth() + NAME_PADDING);
            FontMetrics fontMetrics = getFontMetrics(nameLabel.getFont());
            nameLabel.setPreferredSize(new Dimension(nameWidth, 20));
            if (fontMetrics.stringWidth(name) > nameWidth)
            {
                setToolTipText(name);
            }
            else
            {
                setToolTipText("");
            }
        }
        else
        {
            setToolTipText("");
            nameLabel.setText("");
            iconLabel.setIcon(null);
            idLabel.setText("");
        }

        if (isSelected)
        {
            setBorder(SELECTED_BORDER);
        }
        else
        {
            setBorder(UNSELECTED_BORDER);
        }

        return this;
    }
}
