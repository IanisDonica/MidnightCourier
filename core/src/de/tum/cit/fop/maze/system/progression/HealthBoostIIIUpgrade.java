package de.tum.cit.fop.maze.system.progression;

/**
 * Upgrade that maximizes max health.
 */
public class HealthBoostIIIUpgrade extends BaseUpgrade {
    /**
     * Creates the health boost III upgrade.
     */
    public HealthBoostIIIUpgrade() {
        super(
                "health_3",
                "Health Boost III",
                "Maximizes max HP.",
                "assets/ui/upgrades/health_3.png",
                320,
                new String[] {"health_2"}
        );
    }
}
