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
package com.cluedetails.panels;

import com.cluedetails.Clues;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableCellEditor;
import lombok.Getter;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.components.FlatTextField;

public class ClueTableCellEditor extends AbstractCellEditor implements TableCellEditor
{
	private JPanel editorComponent = new JPanel(new BorderLayout());
	@Getter
	private FlatTextField nameInput = new FlatTextField();
	private JPanel nameActions = new JPanel(new BorderLayout(3, 0));
	private JTextArea save = JGenerator.makeJTextArea("Save");
	private JTextArea cancel = JGenerator.makeJTextArea("Cancel");

	private Clues clue;
	private ConfigManager configManager;
	private JTable clueTable;

	public ClueTableCellEditor(ConfigManager configManager, JTable clueTable)
	{
		this.configManager = configManager;
		this.clueTable = clueTable;

		editorComponent.setOpaque(true);
		editorComponent.setBackground(ColorScheme.DARK_GRAY_COLOR);
		editorComponent.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
		nameInput.setBorder(null);
		nameInput.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		nameInput.setPreferredSize(new Dimension(0, 24));
		nameInput.getTextField().setForeground(Color.WHITE);
		nameInput.getTextField().setBorder(new EmptyBorder(0, 5, 0, 0));

		save.setPreferredSize(new Dimension(30, 10));
		save.setBorder(new EmptyBorder(8, 0, 0, 0));
		save.setFont(FontManager.getRunescapeSmallFont());
		save.setForeground(ColorScheme.PROGRESS_COMPLETE_COLOR);
		save.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent e)
			{
				stopCellEditing();
			}

			@Override
			public void mouseEntered(MouseEvent e)
			{
				save.setForeground(ColorScheme.PROGRESS_COMPLETE_COLOR.darker());
			}

			@Override
			public void mouseExited(MouseEvent e)
			{
				save.setForeground(ColorScheme.PROGRESS_COMPLETE_COLOR);
			}
		});

		cancel.setPreferredSize(new Dimension(35, 10));
		cancel.setBorder(new EmptyBorder(8, 0, 0, 0));
		cancel.setFont(FontManager.getRunescapeSmallFont());
		cancel.setForeground(ColorScheme.PROGRESS_ERROR_COLOR);
		cancel.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent e)
			{
				cancelCellEditing();
			}

			@Override
			public void mouseEntered(MouseEvent e)
			{
				cancel.setForeground(ColorScheme.PROGRESS_ERROR_COLOR.darker());
			}

			@Override
			public void mouseExited(MouseEvent e)
			{
				cancel.setForeground(ColorScheme.PROGRESS_ERROR_COLOR);
			}
		});

		nameActions.setOpaque(false);
		nameActions.add(save, BorderLayout.EAST);
		nameActions.add(cancel, BorderLayout.WEST);

		editorComponent.add(nameInput, BorderLayout.CENTER);
		editorComponent.add(nameActions, BorderLayout.EAST);
	}

	@Override
	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column)
	{
		ListItem item = (ListItem) value;
		clue = item.getClue();

		String displayText = clue.getDisplayText(configManager);
		nameInput.setText(displayText);
		nameInput.getTextField().selectAll();

		return editorComponent;
	}

	@Override
	public Object getCellEditorValue()
	{
		return clue;
	}

	@Override
	public boolean stopCellEditing()
	{
		String newName = nameInput.getText().trim();
		if (!newName.isEmpty())
		{
			configManager.setConfiguration("clue-details-text", String.valueOf(clue.getClueID()), newName);
		}
		((ClueTableModel) clueTable.getModel()).resetEditableRow();
		return super.stopCellEditing();
	}

	@Override
	public void cancelCellEditing()
	{
		((ClueTableModel) clueTable.getModel()).resetEditableRow();
		super.cancelCellEditing();
	}
}
