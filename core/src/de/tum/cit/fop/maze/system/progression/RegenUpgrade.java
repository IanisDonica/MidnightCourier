package de.tum.cit.fop.maze.system.progression;

public class RegenUpgrade extends BaseUpgrade {
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
