package de.tum.cit.fop.maze.system.progression;

public class HealthBoostIIIUpgrade extends BaseUpgrade {
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
