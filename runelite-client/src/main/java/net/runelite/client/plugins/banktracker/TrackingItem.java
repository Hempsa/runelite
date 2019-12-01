package net.runelite.client.plugins.banktracker;

import lombok.Getter;

import java.io.Serializable;

public class TrackingItem implements Serializable {
    @Getter
    private final int id;
    @Getter
    private final String name;
    @Getter
    private final int quantity;

    public TrackingItem(final int id, final String name, final int quantity) {
        this.id = id;
        this.name = name;
        this.quantity = quantity;
    }
}
