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
}
