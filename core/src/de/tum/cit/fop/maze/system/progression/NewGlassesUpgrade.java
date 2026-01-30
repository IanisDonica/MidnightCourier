package de.tum.cit.fop.maze.system.progression;

/**
 * Upgrade that improves visibility through fog.
 */
public class NewGlassesUpgrade extends BaseUpgrade {
    /**
     * Creates the new glasses upgrade.
     */
    public NewGlassesUpgrade() {
        super(
                "new_glasses",
                "New glasses",
                "Increases fog intensity.",
                "assets/ui/upgrades/new_glasses.png",
                250,
                new String[0]
        );
    }
}
