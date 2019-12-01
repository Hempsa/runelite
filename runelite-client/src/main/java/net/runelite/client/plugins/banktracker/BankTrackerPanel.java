package net.runelite.client.plugins.banktracker;

import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;

import javax.inject.Inject;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.io.File;

public class BankTrackerPanel extends PluginPanel {

    JButton showGraphButton = new JButton("Show user data");
    JButton clearUserDataButton = new JButton("Clear user data");

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
        showGraphButton.addActionListener((e) -> showGraph());
        add(clearUserDataButton);
        clearUserDataButton.addActionListener((e) -> clearUserData());
    }

    private void showGraph() {
        TrackingCollection trackingCollection = plugin.getTrackingCollection();
        if (trackingCollection == null) {
            return;
        }
        JFrame frame = new JFrame();
        frame.setContentPane(new GraphPanel(trackingCollection).contentPanel);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    private void clearUserData() {
        final File storageFolder = plugin.getStorageFolder();
        if (storageFolder != null && storageFolder.exists()) {
            File[] files = storageFolder.listFiles();
            if (files.length > 0) {
                int result = JOptionPane.showConfirmDialog(null, "Are you sure?", "Clear user data", JOptionPane.YES_NO_CANCEL_OPTION);
                if (result == 0) {
                    for (File file : files) {
                        file.delete();
                    }
                }
            }
        }
    }



    @Override
    public void onDeactivate() {
    }
}
