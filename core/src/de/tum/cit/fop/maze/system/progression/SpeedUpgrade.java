package de.tum.cit.fop.maze.system.progression;

public class SpeedUpgrade extends BaseUpgrade {
    public SpeedUpgrade() {
        super(
                "speed",
                "Speed Boost",
                "Increases player movement speed.",
                "assets/ui/upgrades/speed.png",
                150,
                new String[] {"root"}
        );
    }
}
