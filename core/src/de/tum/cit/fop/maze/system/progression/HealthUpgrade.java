package de.tum.cit.fop.maze.system.progression;

/**
 * Upgrade that increases max health.
 */
public class HealthUpgrade extends BaseUpgrade {
    /**
     * Creates the health upgrade.
     */
    public HealthUpgrade() {
        super(
                "health",
                "Extra Health",
                "Gives the player additional HP.",
                "assets/ui/upgrades/health.png",
                200,
                new String[] {"root"}
        );
    }
}
