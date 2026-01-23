package de.tum.cit.fop.maze.system.progression;

public class DrinkSpeedIUpgrade extends BaseUpgrade {
    public DrinkSpeedIUpgrade() {
        super(
                "drink_speed_1",
                "Drink Speed I",
                "Energy drinks give a small speed boost.",
                "assets/ui/upgrades/drink_speed_1.png",
                200,
                new String[] {"speed_2"}
        );
    }
}
