package net.runelite.client.plugins.banktracker;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("banktracker")
public interface BankTrackerConfig extends Config {

    @ConfigItem(
            keyName = "recurrentDelay",
            name = "Recurrent delay",
            description = "Delay between recurrent banking in seconds",
            position = 1
    )
    default int recurrentDelay() {
        return 60;
    }

    @ConfigItem(
            keyName = "minQuantity",
            name = "Minimum quantity",
            description = "Minimum quantity of items to track",
            position = 2
    )
    default int minQuantity() {
        return 1;
    }

    // TODO: option to count and display total potion doses instead of item count

}
