package de.tum.cit.fop.maze.system;

import java.io.Serializable;
import java.util.List;

public class GameState implements Serializable {
    private String mapPath;
    private String propertiesPath;
    private float playerX, playerY;
    private int playerLives;
    private PointManager pointManager;
    private boolean hasKey;
    private List<EnemyData> enemies;
    private List<CollectibleData> collectibles;


    public GameState(String mapPath, String propertiesPath, float playerX, float playerY, int playerLives, PointManager pointManager, boolean hasKey, List<EnemyData> enemies, List<CollectibleData> collectibles) {
        this.mapPath = mapPath;
        this.propertiesPath = propertiesPath;
        this.playerLives = playerLives;
        this.playerX = playerX;
        this.playerY = playerY;
        this.pointManager = pointManager;
        this.hasKey = hasKey;
        this.enemies = enemies;
        this.collectibles = collectibles;
    }

    public void save(String mapPath, String propertiesPath, float playerX, float playerY, int playerLives, PointManager pointManager, boolean hasKey, List<EnemyData> enemies, List<CollectibleData> collectibles) {
        this.mapPath = mapPath;
        this.propertiesPath = propertiesPath;
        this.playerX = playerX;
        this.playerY = playerY;
        this.playerLives = playerLives;
        this.pointManager = pointManager;
        this.hasKey = hasKey;
        this.enemies = enemies;
        this.collectibles = collectibles;
    }

    public String getMapPath() {
        return mapPath;
    }

    public String getPropertiesPath() {
        return propertiesPath;
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
}

