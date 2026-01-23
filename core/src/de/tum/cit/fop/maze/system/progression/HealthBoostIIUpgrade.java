package de.tum.cit.fop.maze.system.progression;

public class HealthBoostIIUpgrade extends BaseUpgrade {
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
