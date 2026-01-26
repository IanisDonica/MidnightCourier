package de.tum.cit.fop.maze.system.progression;

public class NewGlassesUpgrade extends BaseUpgrade {
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
