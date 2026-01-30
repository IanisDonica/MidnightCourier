package de.tum.cit.fop.maze.system;

/**
 * Represents a single achievement with progress tracking.
 */
public class Achievement {
    /** Unique identifier for the achievement. */
    private String id;
    /** Display name. */
    private String name;
    /** Description text. */
    private String description;
    /** Current progress value. */
    private int progress;
    /** Target value required to unlock. */
    private int target;
    /** Whether the achievement is unlocked. */
    private boolean unlocked;

    /**
     * Creates an empty achievement instance.
     */
    public Achievement() {
    }

    /**
     * Creates an achievement with default progress and locked state.
     *
     * @param id unique id
     * @param name display name
     * @param description description text
     * @param target required target value
     */
    public Achievement(String id, String name, String description, int target) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.target = target;
        this.progress = 0;
        this.unlocked = false;
    }

    /**
     * Returns the achievement id.
     *
     * @return id
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the achievement id.
     *
     * @param id new id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Returns the display name.
     *
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the display name.
     *
     * @param name new name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the description.
     *
     * @return description text
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description.
     *
     * @param description new description text
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Returns current progress.
     *
     * @return progress value
     */
    public int getProgress() {
        return progress;
    }

    /**
     * Sets progress and updates unlock state.
     *
     * @param progress new progress value
     */
    public void setProgress(int progress) {
        this.progress = Math.max(0, progress);
        updateUnlocked();
    }

    /**
     * Returns the target value required to unlock.
     *
     * @return target value
     */
    public int getTarget() {
        return target;
    }

    /**
     * Sets the target value and updates unlock state.
     *
     * @param target new target value
     */
    public void setTarget(int target) {
        this.target = Math.max(1, target);
        updateUnlocked();
    }

    /**
     * Returns whether this achievement is unlocked.
     *
     * @return {@code true} if unlocked
     */
    public boolean isUnlocked() {
        return unlocked;
    }

    /**
     * Sets unlocked status directly.
     *
     * @param unlocked new unlocked status
     */
    public void setUnlocked(boolean unlocked) {
        this.unlocked = unlocked;
    }

    /**
     * Increments progress by the given amount.
     *
     * @param amount increment value
     */
    public void incrementProgress(int amount) {
        if (amount <= 0) {
            return;
        }
        progress = Math.min(progress + amount, target);
        updateUnlocked();
    }

    /**
     * Updates the unlocked state based on progress and target.
     */
    private void updateUnlocked() {
        unlocked = progress >= target;
    }
}
