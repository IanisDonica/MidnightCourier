package de.tum.cit.fop.maze.system.progression;

public class SpeedIIIUpgrade extends BaseUpgrade {
    public SpeedIIIUpgrade() {
        super(
                "speed_3",
                "Speed III",
                "Maximizes movement speed.",
                "assets/ui/upgrades/speed_3.png",
                300,
                new String[] {"speed_2"}
        );
    }
}
