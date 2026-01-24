package de.tum.cit.fop.maze.system.progression;

public interface Upgrade {
    String getName();

    String getTitle();

    String getDescription();

    String getImagePath();

    int getCost();

    String[] getPrerequisites();

    // TODO make a method here that will fire up a call in the Achievment manager for a achivment based on buying certain amount of ugrade
}
