package net.runelite.client.plugins.banktracker;

import lombok.Getter;
import net.runelite.api.ItemComposition;
import net.runelite.client.game.ItemManager;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class TrackingCollection {

    @Getter
    private Map<String, Map<Long, Integer>> overviewMap;

    private TrackingCollection() {
        overviewMap = new HashMap<>();
    }

    public static TrackingCollection load(final ItemManager itemManager, final File file) {
        TrackingCollection ret = new TrackingCollection();
        final File[] entries = file.listFiles();
        for (final File entry : entries) {
            TrackingContainer container = TrackingContainer.load(entry);
            for (TrackingItem item : container.getItems()) {
                final ItemComposition composition = itemManager.getItemComposition(item.getId());
                if (composition != null) {
                    final String itemName = composition.getName();
                    Map<Long, Integer> map = ret.getOverviewMap().getOrDefault(itemName, new HashMap<>());
                    map.put(container.getTimestamp(), item.getQuantity());
                    if (!ret.getOverviewMap().containsKey(itemName)) {
                        ret.getOverviewMap().put(itemName, map);
                    }
                }
            }
        }
        return ret;
    }
}