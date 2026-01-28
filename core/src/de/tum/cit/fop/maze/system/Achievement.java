package de.tum.cit.fop.maze.system;

public class Achievement {
    private String id;
    private String name;
    private String description;
    private int progress;
    private int target;
    private boolean unlocked;

    public Achievement() {
    }

    public Achievement(String id, String name, String description, int target) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.target = target;
        this.progress = 0;
        this.unlocked = false;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = Math.max(0, progress);
        updateUnlocked();
    }

    public int getTarget() {
        return target;
    }

    public void setTarget(int target) {
        this.target = Math.max(1, target);
        updateUnlocked();
    }

    public boolean isUnlocked() {
        return unlocked;
    }

    public void setUnlocked(boolean unlocked) {
        this.unlocked = unlocked;
    }

    public void incrementProgress(int amount) {
        if (amount <= 0) {
            return;
        }
        progress = Math.min(progress + amount, target);
        updateUnlocked();
    }

    private void updateUnlocked() {
        unlocked = progress >= target;
    }
}
