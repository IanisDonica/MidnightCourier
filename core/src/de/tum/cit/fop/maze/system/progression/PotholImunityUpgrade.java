package de.tum.cit.fop.maze.system.progression;

/**
 * Upgrade that grants immunity to pothole traps.
 */
public class PotholImunityUpgrade extends BaseUpgrade {
    /**
     * Creates the pothole immunity upgrade.
     */
    public PotholImunityUpgrade() {
        super(
                "pothol_imunity",
                "Pothol Imunity",
                "Makes you immune to pothole traps.",
                "assets/ui/upgrades/pothol_imunity.png",
                260,
                new String[] {"new_glasses", "stealth"}
        );
    }
}
