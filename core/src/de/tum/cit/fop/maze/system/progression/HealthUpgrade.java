package de.tum.cit.fop.maze.system.progression;

public class HealthUpgrade extends BaseUpgrade {
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
