package de.tum.cit.fop.maze.system;

import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import de.tum.cit.fop.maze.entity.Player;

/**
 * Handles collision checks against a tile collision layer.
 */
public class CollisionHandler {

    /** Tile layer containing collision cells. */
    private final TiledMapTileLayer collisionLayer;

    /**
     * Creates a collision handler for the given tile layer.
     *
     * @param collisionLayer tile layer used to test collisions
     */
    public CollisionHandler(TiledMapTileLayer collisionLayer) {
        this.collisionLayer = collisionLayer;
    }

    /**
     * Determines whether a tile cell is solid.
     *
     * @param x tile x coordinate
     * @param y tile y coordinate
     * @return {@code true} if the cell is solid or out of bounds
     */
    private boolean isSolid(int x, int y) {
        if (x < 0 || y < 0) return true; // Treat out of bounds as solid
        TiledMapTileLayer.Cell cell = collisionLayer.getCell(x, y);
        return cell != null;
    }

    /**
     * Checks whether the player can move in the given direction without colliding.
     *
     * @param player player entity to test
     * @param direction direction character: {@code u, d, l, r}
     * @return {@code true} if the movement is allowed
     */
    public boolean checkCollision(Player player, char direction) {
        float leftBound = player.getX() + player.getWidth() / 3;
        float rightBound = player.getX() + 1f - player.getWidth() / 3;
        float topBound = player.getY() + player.getHeight() / 4;
        float bottomBound = player.getY() + player.getHeight() / 6;

        switch (direction) {
            case 'u': // Moving up
                topBound += player.getSpeedY();
                return !isSolid((int) leftBound, (int) topBound) && !isSolid((int) rightBound, (int) topBound);

            case 'l': // Moving left
                leftBound += player.getSpeedX();
                return !isSolid((int) leftBound, (int) topBound) && !isSolid((int) leftBound, (int) bottomBound);

            case 'r': // Moving right
                rightBound += player.getSpeedX();
                return !isSolid((int) rightBound, (int) topBound) && !isSolid((int) rightBound, (int) bottomBound);

            case 'd': // Moving down
                bottomBound += player.getSpeedY();
                return !isSolid((int) leftBound, (int) bottomBound) && !isSolid((int) rightBound, (int) bottomBound);

            default:
                return true;
        }
    }

    /**
     * Stops the player's momentum along the axis of movement.
     *
     * @param player player entity to affect
     * @param direction direction character: {@code u, d, l, r}
     */
    public void stopMomentum(Player player, char direction) {
        switch (direction) {
            case 'u':
            case 'd':
                player.getMovementController().velocity.y = 0; // Stop Y momentum
                break;
            case 'l':
            case 'r':
                player.getMovementController().velocity.x = 0; // Stop X momentum
                break;
        }
    }
}
