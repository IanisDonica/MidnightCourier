package de.tum.cit.fop.maze.system;

import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;

public class GameContext {
    public final TiledMapTileLayer collisionLayer;
    public final PointManager pointManager;

    public GameContext(TiledMapTileLayer collisionLayer, PointManager pointManager) {
        this.collisionLayer = collisionLayer;
        this.pointManager = pointManager;
    }
}
