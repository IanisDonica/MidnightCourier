package de.tum.cit.fop.maze.system.progression;

/**
 * Upgrade that improves energy drink speed boost.
 */
public class DrinkSpeedIUpgrade extends BaseUpgrade {
    /**
     * Creates the drink speed I upgrade.
     */
    public DrinkSpeedIUpgrade() {
        super(
                "drink_speed_1",
                "Drink Speed I",
                "Energy drinks give a small speed boost.",
                200,
                new String[] {"speed_2"}
        );
    }
}
