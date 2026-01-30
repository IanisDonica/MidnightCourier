package de.tum.cit.fop.maze.system.progression;

/**
 * Upgrade that regenerates HP over time.
 */
public class RegenUpgrade extends BaseUpgrade {
    /**
     * Creates the regeneration upgrade.
     */
    public RegenUpgrade() {
        super(
                "regen",
                "Regeneration",
                "Slowly regenerates HP over time.",
                "assets/ui/upgrades/regen.png",
                280,
                new String[] {"health_2"}
        );
    }
}
