package de.tum.cit.fop.maze.system.progression;

public interface Upgrade {
    String getName();

    String getTitle();

    String getDescription();

    String getImagePath();

    int getCost();

    String[] getPrerequisites();
}
