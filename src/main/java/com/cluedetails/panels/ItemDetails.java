package com.cluedetails.panels;

import lombok.Getter;

import javax.swing.*;

@Getter
public class ItemDetails
{
    private final int id;
    private final String name;
    private final ImageIcon icon;

    public ItemDetails(int id, String name, ImageIcon icon)
    {
        this.id = id;
        this.name = name;
        this.icon = icon;
    }
}
