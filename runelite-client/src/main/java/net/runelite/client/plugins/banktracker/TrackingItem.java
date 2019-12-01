package net.runelite.client.plugins.banktracker;

import lombok.Getter;

public class TrackingItem {
    @Getter
    private final int id;
    @Getter
    private final int quantity;

    public TrackingItem(final int id, final int quantity) {
        this.id = id;
        this.quantity = quantity;
    }
}
