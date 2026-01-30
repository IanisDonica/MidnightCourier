package de.tum.cit.fop.maze.system.progression;

/**
 * Root upgrade that unlocks the progression tree.
 */
public class RootUpgrade extends BaseUpgrade {
    /**
     * Creates the root upgrade.
     */
    public RootUpgrade() {
        super(
                "root",
                "Root",
                "Base unlock for the progression tree.",
                0,
                new String[0]
        );
    }
}
