package net.runelite.client.plugins.banktracker;

import lombok.Getter;

import java.io.*;
import java.util.*;

public class TrackingContainer implements Serializable {

    @Getter
    private final long timestamp;

    private List<TrackingItem> items = new ArrayList<TrackingItem>();

    /**
     * Used when loading {@link TrackingContainer} from local file system
     *
     * @param file {@link File}
     */
    public static TrackingContainer load(File file) {
        // TODO: test this
        try (FileInputStream  stream = new FileInputStream (file)) {
            ObjectInputStream in = new ObjectInputStream(stream);
            TrackingContainer container = (TrackingContainer) in.readObject();
            in.close();
            System.out.println(String.format("Loaded TrackingContainer %d", container.timestamp));
            return container;
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Used when saving {@link TrackingContainer} to local file system
     *
     * @param file {@link File}
     */
    public void save(File file) {
        // TODO: test this
        try (FileOutputStream stream = new FileOutputStream(file)) {
            ObjectOutputStream out = new ObjectOutputStream(stream);
            out.writeObject(this);
            out.close();
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
        this.items.remove(item.getId());
    }

}
