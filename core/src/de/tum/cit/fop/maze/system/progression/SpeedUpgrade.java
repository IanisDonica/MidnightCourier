package de.tum.cit.fop.maze.system.progression;

/**
 * Upgrade that increases movement speed.
 */
public class SpeedUpgrade extends BaseUpgrade {
    /**
     * Creates the speed upgrade.
     */
    public SpeedUpgrade() {
        super(
                "speed",
                "Speed Boost",
                "Increases player movement speed.",
                "assets/ui/upgrades/speed.png",
                150,
                new String[] {"root"}
        );
    }
}
