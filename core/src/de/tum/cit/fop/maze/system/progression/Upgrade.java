package de.tum.cit.fop.maze.system.progression;

/**
 * Interface defining a progression upgrade.
 */
public interface Upgrade {
    /**
     * Returns the unique upgrade name.
     *
     * @return upgrade name
     */
    String getName();

    /**
     * Returns the display title.
     *
     * @return title
     */
    String getTitle();

    /**
     * Returns the description text.
     *
     * @return description
     */
    String getDescription();

    /**
     * Returns the image path for UI display.
     *
     * @return image path
     */
    String getImagePath();

    /**
     * Returns the upgrade cost.
     *
     * @return cost
     */
    int getCost();

    /**
     * Returns prerequisite upgrade names.
     *
     * @return prerequisites
     */
    String[] getPrerequisites();

    // TODO make a method here that will fire up a call in the Achievment manager for a achivment based on buying certain amount of ugrade
    // TODO add fog of war related upgrades, implement stealth upgrades, and change background
}
