package de.tum.cit.fop.maze.system;

import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;

/**
 * Simple holder for shared game context objects.
 */
public class GameContext {
    /** Collision layer used for movement checks. */
    public final TiledMapTileLayer collisionLayer;
    /** Manager for points and scoring. */
    public final PointManager pointManager;

    /**
     * Creates a new game context.
     *
     * @param collisionLayer collision layer to expose
     * @param pointManager point manager to expose
     */
    public GameContext(TiledMapTileLayer collisionLayer, PointManager pointManager) {
        this.collisionLayer = collisionLayer;
        this.pointManager = pointManager;
    }
}
