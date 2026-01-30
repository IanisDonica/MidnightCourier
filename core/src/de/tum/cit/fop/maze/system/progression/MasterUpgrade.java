package de.tum.cit.fop.maze.system.progression;

/**
 * Upgrade that unlocks the final tier.
 */
public class MasterUpgrade extends BaseUpgrade {
    /**
     * Creates the mastery upgrade.
     */
    public MasterUpgrade() {
        super(
                "master",
                "Mastery",
                "Unlocks the final tier of upgrades.",
                "assets/ui/upgrades/master.png",
                400,
                new String[] {"speed_3", "health_3"}
        );
    }
}
