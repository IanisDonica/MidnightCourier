package de.tum.cit.fop.maze.system.progression;

public class MasterUpgrade extends BaseUpgrade {
    public MasterUpgrade() {
        super(
                "master",
                "Mastery",
                "Unlocks the final tier of upgrades.",
                "assets/ui/upgrades/master.png",
                400,
                new String[] {"speed_3", "health_3"}
        );
    }
}
