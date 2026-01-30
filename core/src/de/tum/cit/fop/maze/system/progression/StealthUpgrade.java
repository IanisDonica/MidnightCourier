package de.tum.cit.fop.maze.system.progression;

/**
 * Upgrade that reduces enemy detection range.
 */
public class StealthUpgrade extends BaseUpgrade {
    /**
     * Creates the stealth upgrade.
     */
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
