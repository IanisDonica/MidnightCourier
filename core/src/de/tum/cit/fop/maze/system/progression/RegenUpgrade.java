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
                "Passive Income",
                "Slowly regenerates HP over time.",
                280,
                new String[] {"health_2"}
        );
    }
}
