package net.runelite.client.plugins.banktracker;

import lombok.Getter;

import java.io.*;
import java.text.NumberFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

public class TrackingContainer {

    @Getter
    private final long timestamp;

    private List<TrackingItem> items = new LinkedList<>();

    /**
     * Used when loading {@link TrackingContainer} items from the local file system
     *
     * @param file properties file
     * @return {@link TrackingContainer}
     */
    public static TrackingContainer load(final File file) {
        try (FileInputStream stream = new FileInputStream(file)) {
            final long timestamp = Long.parseLong(file.getName().split(".")[0]);
            TrackingContainer container = new TrackingContainer(timestamp);
            Properties properties = new Properties();
            properties.load(stream);
            properties.forEach((key, value) -> {
                try {
                    final int id = Integer.parseInt((String) key);
                    final int quantity = Integer.parseInt((String) value);
                    container.items.add(new TrackingItem(id, quantity));
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            });
            return container;
        } catch (NumberFormatException | IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Used when saving {@link TrackingContainer} to local file system
     *
     * @param file {@link File}
     */
    public void save(File file) {
        try (FileOutputStream stream = new FileOutputStream(file)) {
            Properties properties = new Properties();
            for (TrackingItem item : this.items) {
                properties.put(String.valueOf(item.getId()), String.valueOf(item.getQuantity()));
            }
            properties.store(stream, null);
            System.out.println(String.format("Saved TrackingContainer %d to local file system", timestamp));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public TrackingContainer(final long timestamp) {
        this.timestamp = timestamp;
    }

    public List<TrackingItem> getItems() {
        return items;
    }

    public void addItem(TrackingItem item) {
        this.items.add(item);
    }

    public void removeItem(TrackingItem item) {
        this.items.remove(item);
    }

}
