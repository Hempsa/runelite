package net.runelite.client.plugins.banktracker;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;

import javax.inject.Inject;
import java.awt.image.BufferedImage;
import java.io.File;

@PluginDescriptor(
        name = "Bank Tracker",
        description = "Keeps track of quantities of items in bank",
        tags = {"bank", "tracker", "item", "quantities"}
)
@Slf4j
public class BankTrackerPlugin extends Plugin {

    private final File RUNELITE_DIR = new File(System.getProperty("user.home"), ".runelite");
    private final File BANKTRACKER_DIR = new File(RUNELITE_DIR, "banktracker");

    @Inject
    private ClientToolbar clientToolbar;

    @Inject
    private ItemManager itemManager;

    @Inject
    private Client client;

    @Inject
    private BankTrackerConfig config;

    @Inject
    private BankTrackerPanel panel;

    private NavigationButton navButton;

    private Long lastUpdate = null;

    @Provides
    BankTrackerConfig getConfig(ConfigManager configManager) {
        return configManager.getConfig(BankTrackerConfig.class);
    }

    @Override
    protected void startUp() throws Exception {
        final BufferedImage icon = ImageUtil.getResourceStreamFromClass(getClass(), "panel_icon.png");
        navButton = NavigationButton.builder()
                .tooltip("Bank tracker")
                .icon(icon)
                .priority(0)
                .panel(panel)
                .build();
        clientToolbar.addNavigation(navButton);
    }

    @Override
    protected void shutDown() {
        clientToolbar.removeNavigation(navButton);
    }

    @Subscribe
    public void onItemContainerChanged(ItemContainerChanged event) {
        final int containerId = event.getContainerId();
        if (containerId == InventoryID.BANK.getId()) {
            long now = System.currentTimeMillis();
            ItemContainer bankContainer = client.getItemContainer(InventoryID.BANK);
            if (bankContainer == null) {
                return;
            }
            final Item[] items = bankContainer.getItems();
            TrackingContainer container = new TrackingContainer(now);
            for (Item item : items) {
                final int id = item.getId();
                final int quantity = item.getQuantity();
                if (quantity >= config.minQuantity()) {
                    container.addItem(new TrackingItem(id, quantity));
                }
            }
            final File storageFolder = getStorageFolder();
            if (storageFolder != null) {
                container.save(new File(getStorageFolder(), container.getTimestamp() + ".properties"));
                lastUpdate = System.currentTimeMillis();
            } else {
                log.warn("Storage folder not found.");
            }
        }
    }

    public File getStorageFolder() {
        final Player localPlayer = client.getLocalPlayer();
        if (localPlayer != null) {
            final File folder = new File(BANKTRACKER_DIR, localPlayer.getName());
            if (!folder.exists()) {
                log.info("Folder {} created", folder.getAbsolutePath());
                folder.mkdirs();
            }
            return folder;
        }
        return null;
    }

    public TrackingCollection getTrackingCollection() {
        final File storageFolder = getStorageFolder();
        if (storageFolder != null && storageFolder.exists()) {
            return TrackingCollection.load(itemManager, storageFolder);
        }
        return null;
    }
}
