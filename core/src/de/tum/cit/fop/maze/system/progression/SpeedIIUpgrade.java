package de.tum.cit.fop.maze.system.progression;

/**
 * Upgrade that further increases movement speed.
 */
public class SpeedIIUpgrade extends BaseUpgrade {
    /**
     * Creates the speed II upgrade.
     */
    public SpeedIIUpgrade() {
        super(
                "speed_2",
                "Speed II",
                "Further increases movement speed.",
                220,
                new String[] {"speed"}
        );
    }
}
