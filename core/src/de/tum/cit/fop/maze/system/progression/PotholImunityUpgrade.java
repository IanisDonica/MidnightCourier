package de.tum.cit.fop.maze.system.progression;

public class PotholImunityUpgrade extends BaseUpgrade {
    public PotholImunityUpgrade() {
        super(
                "pothol_imunity",
                "Pothol Imunity",
                "Makes you immune to pothole traps.",
                "assets/ui/upgrades/pothol_imunity.png",
                260,
                new String[] {"dash", "stealth"}
        );
    }
}
