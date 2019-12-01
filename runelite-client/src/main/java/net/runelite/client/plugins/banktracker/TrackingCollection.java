package net.runelite.client.plugins.banktracker;

import lombok.Getter;
import net.runelite.api.ItemComposition;
import net.runelite.client.game.ItemManager;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
                Map<Long, Integer> map = ret.getOverviewMap().getOrDefault(item.getName(), new HashMap<>());
                map.put(container.getTimestamp(), item.getQuantity());
                if (!ret.getOverviewMap().containsKey(item.getName())) {
                    ret.getOverviewMap().put(item.getName(), map);
                }
            }
        }
        return ret;
    }

    /**
     * @param itemName
     * @return time->item count map for a given item
     */
    public Map<Long, Integer> getItemCounts(String itemName){
        return overviewMap.get(itemName);
    }

    /**
     * @param partialName
     * @return matching item names for a partial item name string. Only returns item names of items that
     * have been tracked.
     */
    public List<String> getMatchingItemNames(String partialName){
        List<String> result = new ArrayList<String>();
        for(String name : overviewMap.keySet()){
            if(name.contains(partialName))
                result.add(name);
        }
        return result;
    }
}