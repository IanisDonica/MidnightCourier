package de.tum.cit.fop.maze.system;

import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import de.tum.cit.fop.maze.entity.Entity;
import de.tum.cit.fop.maze.entity.Player;

public class CollisionHandler {
    private final TiledMapTileLayer collisionLayer;

    public CollisionHandler(TiledMap map) {
        this.collisionLayer = (TiledMapTileLayer) map.getLayers().get("Walls");
    }

    public boolean checkCollision(Player player, char direction) {
        float leftBound = player.getX() + player.getWidth() / 3;
        float rightBound = player.getX() + 1f - player.getWidth() / 3;
        float topBound = player.getY() + player.getHeight() / 4;
        float bottomBound = player.getY() + player.getHeight() / 6;

        TiledMapTileLayer.Cell topCell, bottomCell, leftCell, rightCell;

        switch (direction) {
            case 'u':
                topBound += player.getSpeed();
                leftCell = collisionLayer.getCell((int) leftBound, (int) topBound);
                rightCell = collisionLayer.getCell((int) rightBound, (int) topBound);
                return (leftCell == null && rightCell == null);
            case 'l':
                leftBound -= player.getSpeed();
                topCell = collisionLayer.getCell((int) leftBound, (int) topBound);
                bottomCell = collisionLayer.getCell((int) leftBound, (int) bottomBound);
                return (topCell == null && bottomCell == null);
            case 'r':
                rightBound += player.getSpeed();
                topCell = collisionLayer.getCell((int) rightBound, (int) topBound);
                bottomCell = collisionLayer.getCell((int) rightBound, (int) bottomBound);
                return (topCell == null && bottomCell == null);
            case 'd':
                bottomBound -= player.getSpeed();
                leftCell = collisionLayer.getCell((int) leftBound, (int) bottomBound);
                rightCell = collisionLayer.getCell((int) rightBound, (int) bottomBound);
                return (leftCell == null && rightCell == null);
            default:
                return true;
        }

    }

    //public boolean colidingWithPlayer(Player player, Entity entity) {

    //}

}
