package net.runelite.client.plugins.banktracker;

import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;

import javax.inject.Inject;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class BankTrackerPanel extends PluginPanel {

    JButton showGraphButton = new JButton("Show data");
    JButton clearUserData = new JButton("Clear user data");

    private BankTrackerPlugin plugin;

    @Inject
    public BankTrackerPanel(final BankTrackerPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onActivate() {
        setBorder(new EmptyBorder(6, 6, 6, 6));
        setBackground(ColorScheme.DARK_GRAY_COLOR);
        add(showGraphButton);
        add(clearUserData);
        // TODO
    }

    @Override
    public void onDeactivate() {
    }
}
