package de.tum.cit.fop.maze.system.progression;

/**
 * Base implementation of an upgrade with immutable properties.
 */
public abstract class BaseUpgrade implements Upgrade {
    /** Unique upgrade name. */
    private final String name;
    /** Display title. */
    private final String title;
    /** Description text. */
    private final String description;
    /** Cost in points. */
    private final int cost;
    /** Prerequisite upgrade names. */
    private final String[] prerequisites;

    /**
     * Creates a base upgrade definition.
     *
     * @param name unique upgrade name
     * @param title display title
     * @param description description text
     * @param cost upgrade cost
     * @param prerequisites prerequisite upgrade names
     */
    protected BaseUpgrade(String name, String title, String description, int cost, String[] prerequisites) {
        this.name = name;
        this.title = title;
        this.description = description;
        this.cost = cost;
        this.prerequisites = prerequisites;
    }

    /**
     * Returns the upgrade name.
     *
     * @return name
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * Returns the display title.
     *
     * @return title
     */
    @Override
    public String getTitle() {
        return title;
    }

    /**
     * Returns the description text.
     *
     * @return description
     */
    @Override
    public String getDescription() {
        return description;
    }

    /**
     * Returns the upgrade cost.
     *
     * @return cost
     */
    @Override
    public int getCost() {
        return cost;
    }

    /**
     * Returns prerequisite upgrade names.
     *
     * @return prerequisites
     */
    @Override
    public String[] getPrerequisites() {
        return prerequisites;
    }
}
