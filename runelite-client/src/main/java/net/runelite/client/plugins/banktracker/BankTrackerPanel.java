package net.runelite.client.plugins.banktracker;

import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;

import javax.inject.Inject;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.io.File;

public class BankTrackerPanel extends PluginPanel {

    JButton showGraphButton = new JButton("Show user data");
    JButton clearUserData = new JButton("Clear user data");

    private final BankTrackerPlugin plugin;

    @Inject
    public BankTrackerPanel(final BankTrackerPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onActivate() {
        setBorder(new EmptyBorder(6, 6, 6, 6));
        setBackground(ColorScheme.DARK_GRAY_COLOR);
        add(showGraphButton);
        clearUserData.addActionListener(event -> {
            final File storageFolder = plugin.getStorageFolder();
            if (storageFolder != null && storageFolder.exists()) {
                File[] files = storageFolder.listFiles();
                if (files.length > 0) {
                    int result = JOptionPane.showConfirmDialog(null, "Are you sure?", "Clear user data", JOptionPane.YES_NO_CANCEL_OPTION);
                    if (result == 0) {
                        storageFolder.delete();
                        System.out.println("Yes clicked");
                    } else if (result == 1) {
                        System.out.println("No clicked");
                    }
                }
            }
        });
        add(clearUserData);
        // TODO
    }

    @Override
    public void onDeactivate() {
    }
}
