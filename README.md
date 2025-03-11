# Clue Details

This plugin provides additional details on clue scrolls. This can be particularly useful for snowflake ironmen who can only do particular subsets of clues, allowing them to ignore ones they can't do.

**NOTE**: This plugin will only work for beginner and master clues after you have read them once. After that, the plugin should keep track of them as long as they are in your inventory, dropped by you, or banked.

![HxS6Axb](https://github.com/user-attachments/assets/b9c8799c-9210-4f46-82b6-7f339104d210)

## Marked Clues

There are configurable filter lists you can set up to enable highlighting of clues you've chosen. Use filters in the sidebar to easily find the clues you're interested in highlighting.

![Screenshot 2024-06-04 180612](https://github.com/Zoinkwiz/clue-regions/assets/29153234/d08ee01a-d8c0-4baf-9054-6fc9173af6cd)

You can also easily mark and unmark clues by shift-clicking one already on the floor or in your inventory.

![nLv28QX](https://github.com/user-attachments/assets/c059b2e2-c45f-4c83-becc-17bd6eda22fc)

As the hints appear if you hover over the menu option for a clue, it can make it easier to identify which clue is which in a big stack of clues.

## Overlays

The "Show hover text" toggle will provide an infobox letting you know the step's details when you hover over a clue scroll.

![8K3izqk](https://github.com/user-attachments/assets/a3b4a3f6-1844-4584-b701-f4d8dca7e334)

The "Show clue tags" toggle in the plugin's settings renders the details on the clue item in your inventory.

![365445045-736b3d09-62ac-4273-91c5-60b47a8e788f](https://github.com/user-attachments/assets/5b895aa2-0182-412f-9854-376abdaa228a)

- This is most useful when using shorthand details rather than with the default details. See link in Import/Export section.
- Text following the "Clue tag split sequence" configuration will be automatically split to the bottom of the clue item. The default is ":".

![365446878-169c15ad-f793-4c7d-af0c-a2b787f072f0](https://github.com/user-attachments/assets/cac24b71-74a8-4a13-94b7-6d61ae5d1813)

The "Show inventory overlay" toggle in the plugin's settings renders the details as an infobox for all clues in your inventory.

![365445046-c5c40aa4-b2d1-46a4-aa31-9ce609760bb6](https://github.com/user-attachments/assets/a1386cd7-7802-471f-92cd-9578688bda83)

The "Change ground item menu text" toggle in the plugin's settings adjusts the actual text of the clue item menu, so you don't even need to hover for details.

![Screenshot 2024-09-01 221305](https://github.com/user-attachments/assets/72685ba5-f441-4cac-b18c-6cc0ddf42d98)

The "Inventory tag clue scrolls" toggle applies inventory tags to clues in your inventory using configured clue colors

![image](https://github.com/user-attachments/assets/ba9ca49f-df55-48c5-a112-5302ecef1683)

The "Inventory tag clue items" toggle applies inventory tags configured items for each clue in your inventory

![image](https://github.com/user-attachments/assets/3dc33f5b-f300-4614-b6bf-db7359f23ae5)

The "Show ground clues" toggle shows text overlay for ground clues, similar to the Ground Items plugin.

![ground_clues](https://github.com/user-attachments/assets/bb067da3-faaf-4d5f-a521-d1c3b030ab9b)

The "Show ground clue timers" toggle shows despawn timers for ground clues, similar to the Clue Juggling Timer plugin.

![ground_clue_timers](https://github.com/user-attachments/assets/73e70e22-a541-48a2-a9ef-92f324a8f38e)

## Editing clue details

### Text
You can edit the clue details shown to you, this can be done 3 different ways.
1. In the sidebar, right-click the clue hint you want to change and select "Edit text for clue"
2. Shift+right-click on a clue in your inventory, then select the "Clue details" option
3. Via Import/Export. See below

Input the next text you want shown for it, and then submit. It will now be what is shown to you for the specific clue.

### Colors
You can edit the clue details colors shown to you, this can be done 2 different ways.
1. In the sidebar, right-click the clue hint you want to change and select "Edit color for clue"
2. Via Import/Export in RGB Hexadecimal format. See below

Colors will now be updated for the specific clue depending on the configuration toggles shown below.

Updating colors for the Ground Items and Inventory Tags plugins is also supported. 

- Enable the "Overwrite ..." configuration option(s) in the "Overlay Colors" section.
    - This is applied only at the time the colors are edited/imported.
    - Does not apply to Beginner and Master clues.
    - Resetting Ground Items and Inventory Tags colors can must be done via setting the clue's color to white (#FFFFFF).

### Items
You can edit the clue details items for each clue, this can be done 3 different ways.
1. If you have the clue in your inventory for which you would like to inventory tag an item:
    - Right-click the desired inventory item and choose "Add" or "Remove" for the clue via the option specifying its tier
2. In the sidebar, right-click the clue you would like to inventory tag an item for, and select "Add/Remove item"
3. Via Import/Export. See below

## Import/Export clue details

You can share your edited clue detail text, colors, and items, or import from someone else via the import and export buttons at the top of the sidebar.

![java_WJJq9uFwNH](https://github.com/user-attachments/assets/504a4bb8-a0dc-429d-be3d-1684e663a264)

You can find custom clue details examples at https://thelope.github.io/clue-tags/details/
