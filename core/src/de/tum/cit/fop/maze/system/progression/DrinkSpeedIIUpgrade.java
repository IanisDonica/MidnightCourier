package de.tum.cit.fop.maze.system.progression;

public class DrinkSpeedIIUpgrade extends BaseUpgrade {
    public DrinkSpeedIIUpgrade() {
        super(
                "drink_speed_2",
                "Drink Speed II",
                "Energy drinks give a strong speed boost.",
                "assets/ui/upgrades/drink_speed_2.png",
                280,
                new String[] {"drink_speed_1"}
        );
    }
}
