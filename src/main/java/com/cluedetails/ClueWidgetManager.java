package com.cluedetails;

import lombok.Setter;
import net.runelite.api.Client;
import net.runelite.api.KeyCode;
import net.runelite.api.Menu;
import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;
import net.runelite.api.widgets.Widget;
import net.runelite.client.config.ConfigManager;

import javax.inject.Singleton;
import java.util.*;

@Singleton
public class ClueWidgetManager
{
    private final Client client;
    private final ConfigManager configManager;
    private final ClueInventoryManager clueInventoryManager;
    private final CluePreferenceManager cluePreferenceManager;

    public ClueWidgetManager(Client client, ConfigManager configManager, ClueInventoryManager clueInventoryManager, CluePreferenceManager cluePreferenceManager)
    {
        this.client = client;
        this.configManager = configManager;
        this.clueInventoryManager = clueInventoryManager;
        this.cluePreferenceManager = cluePreferenceManager;
    }

    // To be initialized to avoid passing around
    @Setter
    public static ClueDetailsConfig config;

    public void addHighlightWidgetSubmenus(MenuEntry[] entries)
    {
        if (!client.isKeyPressed(KeyCode.KC_SHIFT))
        {
            return;
        }

        if (clueInventoryManager.getCluesInInventory().isEmpty())
        {
            return;
        }

        for (int idx = entries.length - 1; idx >= 0; --idx)
        {
            MenuEntry entry = entries[idx];

            Widget widget = entry.getWidget();
            if (widget == null || widget.getActions() == null)
            {
                return;
            }

            // Disable item marking, separate feature
            if (widget.getItemId() != -1)
            {
                continue;
            }

            int componentId = widget.getId();
            int childIndex = widget.getIndex();
            WidgetId widgetId = new WidgetId(componentId, childIndex == -1 ? null : childIndex);

            MenuEntry clueDetailsEntry = client.getMenu().createMenuEntry(1) // place above Cancel
                    .setOption("Clue details")
                    .setType(MenuAction.RUNELITE);
            Menu submenu = clueDetailsEntry.createSubMenu();

            clueInventoryManager.getCluesInInventory()
                    .forEach(itemId -> {
                        ClueInstance instance = clueInventoryManager.getClueByClueItemId(itemId);
                        if (instance != null) {
                            instance
                                .getClueIds()
                                .forEach((clueId) -> addHighlightWidgetMenu(cluePreferenceManager, submenu, Clues.forClueIdFiltered(clueId), widgetId));
                        }
                    });
            break;
        }
    }

    private void addHighlightWidgetMenu(CluePreferenceManager cluePreferenceManager, Menu menu, Clues clue, WidgetId widgetId)
    {
        if (clue == null) return;

        boolean widgetInCluePreference = cluePreferenceManager.widgetsPreferenceContainsWidget(clue.getClueID(), widgetId);

        String action = widgetInCluePreference ? "Remove from " : "Add to ";
        String clueDetail = clue.getDetail(configManager);
        final String text = action + "'" + clueDetail + "'";

        // Add menu to widget for clue
        menu.createMenuEntry(-1)
                .setOption(text)
                .setType(MenuAction.RUNELITE)
                .onClick(e -> updateClueWidgets(clue, widgetId, cluePreferenceManager));
    }

    private void updateClueWidgets(Clues clue, WidgetId widgetId, CluePreferenceManager cluePreferenceManager)
    {
        // Get existing Clue widgetIds
        int clueId = clue.getClueID();
        List<WidgetId> clueWidgetIds = cluePreferenceManager.getWidgetsPreference(clueId);

        if (clueWidgetIds == null)
        {
            clueWidgetIds = new ArrayList<>();
        }

        // Remove if already present
        if (clueWidgetIds.contains(widgetId))
        {
            clueWidgetIds.remove(widgetId);
        }
        // Add if not present
        else
        {
            clueWidgetIds.add(widgetId);
        }

        // Save Clue widgetIds
        cluePreferenceManager.saveWidgetsPreference(clueId, clueWidgetIds);
    }

}
