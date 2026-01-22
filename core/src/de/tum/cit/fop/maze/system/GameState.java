package de.tum.cit.fop.maze.system;

import com.badlogic.gdx.maps.tiled.TiledMap;

import java.io.Serializable;
import java.util.List;

public class GameState implements Serializable {
    public String mapPath;
    public float playerX, playerY;
    public int playerLives;
//    public int score;
    public PointManager pointManager;
    public boolean hasKey;
    public List<EnemyData> enemies;
    public List<CollectibleData> collectibles;


    public GameState(String mapPath, float playerX, float playerY, int playerLives, PointManager pointManager, boolean hasKey, List<EnemyData> enemies, List<CollectibleData> collectibles) {
        this.mapPath = mapPath;
        this.playerLives = playerLives;
        this.playerX = playerX;
        this.playerY = playerY;
        this.pointManager = pointManager;
        this.hasKey = hasKey;
        this.enemies = enemies;
        this.collectibles = collectibles;
    }

    public void save(String mapPath, float playerX, float playerY, int playerLives, PointManager pointManager, boolean hasKey, List<EnemyData> enemies, List<CollectibleData> collectibles){
        this.mapPath = mapPath;
        this.playerX = playerX;
        this.playerY = playerY;
        this.playerLives = playerLives;
        this.pointManager = pointManager;
        this.hasKey = hasKey;
        this.enemies = enemies;
        this.collectibles = collectibles;
    }

}
