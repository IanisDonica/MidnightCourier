package de.tum.cit.fop.maze.ai;

import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import de.tum.cit.fop.maze.entity.MapObject;
import de.tum.cit.fop.maze.entity.collectible.Collectible;
import de.tum.cit.fop.maze.entity.obstacle.Obstacle;

/**
 * Base helper for AI behaviors operating on a tiled map.
 */
public class AIBehaviour {
    /** Map width in tiles. */
    protected final int width, height;
    /** Stage for actor queries. */
    protected Stage stage;
    /** Collision layer used for path checks. */
    protected final TiledMapTileLayer collisionLayer;

    /**
     * Creates an AI behavior helper.
     *
     * @param width map width in tiles
     * @param height map height in tiles
     * @param collisionLayer collision layer
     */
    public AIBehaviour(int width, int height, TiledMapTileLayer collisionLayer) {
        this.width = width;
        this.height = height;
        this.collisionLayer = collisionLayer;
    }

    /**
     * Sets the stage used for actor queries.
     *
     * @param stage stage instance
     */
    public void setStage(Stage stage) { this.stage = stage; }

    // TODO If enough time, replace method with this algo:
    // Take the inverse of the collision map, remove all static enemies/powerups cache that
    // Each call just pick one of those values and check if there are any actors there
    // Also would be possible to remove the tiles next to a player (for retreat for example)
    // On powerup pickup, add the tile back to the cached map
    // Should be faster and more flexible but gonna do that later if there is enough time
    /**
     * Finds a random free tile that is not blocked.
     *
     * @return free tile position
     */
    protected GridPoint2 findRandomFreeTile() {
        // Roll the dice, see if the tile is good, if it is that your tile, if not roll again

        // In theory should there be more enemies than free spots this could cause an infinite loop
        // however our map will never look like that.
        while (true) {
            int x = MathUtils.random(0, width - 1), y = MathUtils.random(0, height - 1);
            if (collisionLayer.getCell(x, y) == null && !isBlockedAt(x, y)) {
                return new GridPoint2(x, y);
            }
        }
    }

    // This checks if there are any actors (Player/other enemies/collectibles on that point)
    /**
     * Checks whether a tile is blocked by an actor.
     *
     * @param x tile x
     * @param y tile y
     * @return {@code true} if blocked
     */
    protected boolean isBlockedAt(int x, int y) {
        for (Actor actor : stage.getActors()) {
            if (actor instanceof MapObject) {
                int ax = MathUtils.floor(actor.getX()), ay = MathUtils.floor(actor.getY());
                if (ax == x && ay == y) { return true; }
            }
        }
        return false;
    }
}
