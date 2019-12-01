package net.runelite.client.plugins.banktracker;

import net.runelite.api.Client;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;

import javax.inject.Inject;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.util.concurrent.ScheduledExecutorService;

public class BankTrackerPanel extends PluginPanel {

    JButton showGraphButton = new JButton("Show data");
    JButton clearUserDataButton = new JButton("Clear user data");

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
    }



    @Override
    public void onDeactivate() {
    }
}
