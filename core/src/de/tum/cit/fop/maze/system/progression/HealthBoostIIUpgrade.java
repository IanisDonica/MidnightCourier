package de.tum.cit.fop.maze.system.progression;

/**
 * Upgrade that further increases max health.
 */
public class HealthBoostIIUpgrade extends BaseUpgrade {
    /**
     * Creates the health boost II upgrade.
     */
    public HealthBoostIIUpgrade() {
        super(
                "health_2",
                "Health Boost II",
                "Further increases max HP.",
                "assets/ui/upgrades/health_2.png",
                240,
                new String[] {"health"}
        );
    }
}
