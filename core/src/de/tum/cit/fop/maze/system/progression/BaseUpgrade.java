package de.tum.cit.fop.maze.system.progression;

public abstract class BaseUpgrade implements Upgrade {
    private final String name;
    private final String title;
    private final String description;
    private final String imagePath;
    private final int cost;
    private final String[] prerequisites;

    protected BaseUpgrade(String name, String title, String description, String imagePath, int cost, String[] prerequisites) {
        this.name = name;
        this.title = title;
        this.description = description;
        this.imagePath = imagePath;
        this.cost = cost;
        this.prerequisites = prerequisites;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getImagePath() {
        return imagePath;
    }

    @Override
    public int getCost() {
        return cost;
    }

    @Override
    public String[] getPrerequisites() {
        return prerequisites;
    }
}
