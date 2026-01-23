package de.tum.cit.fop.maze.system.progression;

public class DashUpgrade extends BaseUpgrade {
    public DashUpgrade() {
        super(
                "dash",
                "Dash",
                "Unlocks a short burst dash.",
                "assets/ui/upgrades/dash.png",
                250,
                new String[0]
        );
    }
}
