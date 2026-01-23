package de.tum.cit.fop.maze.system.progression;

public class StealthUpgrade extends BaseUpgrade {
    public StealthUpgrade() {
        super(
                "stealth",
                "Stealth",
                "Reduces enemy detection range.",
                "assets/ui/upgrades/stealth.png",
                260,
                new String[] {"root"}
        );
    }
}
