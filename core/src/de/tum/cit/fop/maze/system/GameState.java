package de.tum.cit.fop.maze.system;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

public class GameState implements Serializable {
    private String mapPath;
    private int level;
    private float cameraZoom;
    private float playerX, playerY;
    private int playerLives;
    private PointManager pointManager;
    private boolean hasKey;
    private List<EnemyData> enemies;
    private List<CollectibleData> collectibles;
    private int progressionPoints;
    private Set<String> ownedUpgrades;


    public GameState(String mapPath, int level, float cameraZoom, float playerX, float playerY, int playerLives, PointManager pointManager, boolean hasKey, List<EnemyData> enemies, List<CollectibleData> collectibles, int progressionPoints, Set<String> ownedUpgrades) {
        this.mapPath = mapPath;
        this.level = level;
        this.cameraZoom = cameraZoom;
        this.playerLives = playerLives;
        this.playerX = playerX;
        this.playerY = playerY;
        this.pointManager = pointManager;
        this.hasKey = hasKey;
        this.enemies = enemies;
        this.collectibles = collectibles;
        this.progressionPoints = progressionPoints;
        this.ownedUpgrades = ownedUpgrades;
    }

    public void save(String mapPath, int level, float cameraZoom, float playerX, float playerY, int playerLives, PointManager pointManager, boolean hasKey, List<EnemyData> enemies, List<CollectibleData> collectibles, int progressionPoints, Set<String> ownedUpgrades) {
        this.mapPath = mapPath;
        this.level = level;
        this.cameraZoom = cameraZoom;
        this.playerX = playerX;
        this.playerY = playerY;
        this.playerLives = playerLives;
        this.pointManager = pointManager;
        this.hasKey = hasKey;
        this.enemies = enemies;
        this.collectibles = collectibles;
        this.progressionPoints = progressionPoints;
        this.ownedUpgrades = ownedUpgrades;
    }

    public String getMapPath() {
        return mapPath;
    }

    public int getLevel() {
        return level;
    }

    public float getCameraZoom() {
        return cameraZoom;
    }

    public float getPlayerX() {
        return playerX;
    }

    public float getPlayerY() {
        return playerY;
    }

    public int getPlayerLives() {
        return playerLives;
    }

    public PointManager getPointManager() {
        return pointManager;
    }

    public boolean hasKey() {
        return hasKey;
    }

    public List<EnemyData>  getEnemies() {
        return enemies;
    }

    public  List<CollectibleData> getCollectibles() {
        return collectibles;
    }

    public int getProgressionPoints() {
        return progressionPoints;
    }

    public Set<String> getOwnedUpgrades() {
        return ownedUpgrades;
    }
}
