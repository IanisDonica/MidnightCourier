package de.tum.cit.fop.maze.system.progression;

public class SpeedIIUpgrade extends BaseUpgrade {
    public SpeedIIUpgrade() {
        super(
                "speed_2",
                "Speed II",
                "Further increases movement speed.",
                "assets/ui/upgrades/speed_2.png",
                220,
                new String[] {"speed"}
        );
    }
}
