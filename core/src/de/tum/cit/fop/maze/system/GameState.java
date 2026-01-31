package de.tum.cit.fop.maze.system;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

/**
 * Serializable snapshot of game state for saving/loading.
 */
public class GameState implements Serializable {
    /** Map path used for the current level. */
    private String mapPath;
    /** Current level number. */
    private int level;
    /** Camera zoom level. */
    private float cameraZoom;
    /** Player coordinates. */
    private float playerX, playerY;
    /** Remaining player lives. */
    private int playerLives;
    /** Point manager state. */
    private PointManager pointManager;
    /** Whether the player has the key. */
    private boolean hasKey;
    /** Whether the player can leave. */
    private boolean canLeave;
    /** Enemy state data. */
    private List<EnemyData> enemies;
    /** Collectible state data. */
    private List<CollectibleData> collectibles;
    /** Progression points. */
    private int progressionPoints;
    /** Owned upgrades. */
    private Set<String> ownedUpgrades;


    /**
     * Creates a new game state snapshot.
     *
     * @param mapPath map path
     * @param level level number
     * @param cameraZoom camera zoom
     * @param playerX player x coordinate
     * @param playerY player y coordinate
     * @param playerLives remaining lives
     * @param pointManager point manager state
     * @param hasKey whether the player has the key
     * @param canLeave whether the player can leave
     * @param enemies enemy state list
     * @param collectibles collectible state list
     * @param progressionPoints progression points
     * @param ownedUpgrades owned upgrade names
     */
    public GameState(String mapPath, int level, float cameraZoom, float playerX, float playerY, int playerLives, PointManager pointManager, boolean hasKey, boolean canLeave, List<EnemyData> enemies, List<CollectibleData> collectibles, int progressionPoints, Set<String> ownedUpgrades) {
        this.mapPath = mapPath;
        this.level = level;
        this.cameraZoom = cameraZoom;
        this.playerLives = playerLives;
        this.playerX = playerX;
        this.playerY = playerY;
        this.pointManager = pointManager;
        this.hasKey = hasKey;
        this.canLeave = canLeave;
        this.enemies = enemies;
        this.collectibles = collectibles;
        this.progressionPoints = progressionPoints;
        this.ownedUpgrades = ownedUpgrades;
    }

    /**
     * Updates this game state with new values.
     *
     * @param mapPath map path
     * @param level level number
     * @param cameraZoom camera zoom
     * @param playerX player x coordinate
     * @param playerY player y coordinate
     * @param playerLives remaining lives
     * @param pointManager point manager state
     * @param hasKey whether the player has the key
     * @param canLeave whether the player can leave
     * @param enemies enemy state list
     * @param collectibles collectible state list
     * @param progressionPoints progression points
     * @param ownedUpgrades owned upgrade names
     */
    public void save(String mapPath, int level, float cameraZoom, float playerX, float playerY, int playerLives, PointManager pointManager, boolean hasKey, boolean canLeave, List<EnemyData> enemies, List<CollectibleData> collectibles, int progressionPoints, Set<String> ownedUpgrades) {
        this.mapPath = mapPath;
        this.level = level;
        this.cameraZoom = cameraZoom;
        this.playerX = playerX;
        this.playerY = playerY;
        this.playerLives = playerLives;
        this.pointManager = pointManager;
        this.hasKey = hasKey;
        this.canLeave = canLeave;
        this.enemies = enemies;
        this.collectibles = collectibles;
        this.progressionPoints = progressionPoints;
        this.ownedUpgrades = ownedUpgrades;
    }

    /**
     * Returns the map path.
     *
     * @return map path
     */
    public String getMapPath() {
        return mapPath;
    }

    /**
     * Returns the level number.
     *
     * @return level number
     */
    public int getLevel() {
        return level;
    }

    /**
     * Returns the camera zoom level.
     *
     * @return camera zoom
     */
    public float getCameraZoom() {
        return cameraZoom;
    }

    /**
     * Returns the player x coordinate.
     *
     * @return player x
     */
    public float getPlayerX() {
        return playerX;
    }

    /**
     * Returns the player y coordinate.
     *
     * @return player y
     */
    public float getPlayerY() {
        return playerY;
    }

    /**
     * Returns the remaining player lives.
     *
     * @return player lives
     */
    public int getPlayerLives() {
        return playerLives;
    }

    /**
     * Returns the point manager state.
     *
     * @return point manager
     */
    public PointManager getPointManager() {
        return pointManager;
    }

    /**
     * Returns whether the player has the key.
     *
     * @return {@code true} if the key is owned
     */
    public boolean hasKey() {
        return hasKey;
    }

    /**
     * Returns whether the player can leave.
     *
     * @return {@code true} if the player can leave
     */
    public boolean canLeave() {
        return canLeave;
    }

    /**
     * Returns the enemy state list.
     *
     * @return enemy data list
     */
    public List<EnemyData>  getEnemies() {
        return enemies;
    }

    /**
     * Returns the collectible state list.
     *
     * @return collectible data list
     */
    public  List<CollectibleData> getCollectibles() {
        return collectibles;
    }

    /**
     * Returns progression points.
     *
     * @return progression points
     */
    public int getProgressionPoints() {
        return progressionPoints;
    }

    /**
     * Returns owned upgrades.
     *
     * @return owned upgrade names
     */
    public Set<String> getOwnedUpgrades() {
        return ownedUpgrades;
    }
}
