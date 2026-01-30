package de.tum.cit.fop.maze.system.progression;

/**
 * Upgrade that maximizes movement speed.
 */
public class SpeedIIIUpgrade extends BaseUpgrade {
    /**
     * Creates the speed III upgrade.
     */
    public SpeedIIIUpgrade() {
        super(
                "speed_3",
                "Speed III",
                "Maximizes movement speed.",
                300,
                new String[] {"speed_2"}
        );
    }
}
