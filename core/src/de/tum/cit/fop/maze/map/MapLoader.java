package de.tum.cit.fop.maze.map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.scenes.scene2d.Stage;
import de.tum.cit.fop.maze.entity.collectible.EnergyDrink;
import de.tum.cit.fop.maze.entity.collectible.ExitDoor;
import de.tum.cit.fop.maze.entity.collectible.HealthPickup;
import de.tum.cit.fop.maze.entity.collectible.Key;
import de.tum.cit.fop.maze.entity.obstacle.Obstacle;
import de.tum.cit.fop.maze.entity.obstacle.Trap;
import de.tum.cit.fop.maze.system.PointManager;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

public class MapLoader {
    public TiledMapTileLayer buildCollisionLayerFromProperties(TiledMap map, String propertiesPath) {
        int width = map.getProperties().get("width", Integer.class);
        int height = map.getProperties().get("height", Integer.class);
        int tileWidth = map.getProperties().get("tilewidth", Integer.class);
        int tileHeight = map.getProperties().get("tileheight", Integer.class);

        TiledMapTileLayer layer = new TiledMapTileLayer(width, height, tileWidth, tileHeight);
        Properties props = new Properties();
        try (InputStream input = Gdx.files.internal(propertiesPath).read()) {
            props.load(input);
        } catch (Exception ex) {
            throw new RuntimeException("Property file doesn't exist");
        }

        for (Map.Entry<Object, Object> entry : props.entrySet()) {
            String key = String.valueOf(entry.getKey());
            String[] parts = key.split(",");
            if (parts.length != 2) {
                continue;
            }
            try {
                int x = Integer.parseInt(parts[0]);
                int y = Integer.parseInt(parts[1]);
                int value = Integer.parseInt(String.valueOf(entry.getValue()));
                if (value == 7 || value == 2) {
                    layer.setCell(x, y, new TiledMapTileLayer.Cell());
                }
            } catch (NumberFormatException ignored) {
                // Skip bad coordinates or values.
            }
        }
        return layer;
    }

    public void spawnCollectiblesFromProperties(Stage stage, PointManager pointManager, String propertiesPath) {
        Properties props = new Properties();

        // CHeck if the map actually exists, if it doesn't crash the game, cause
        // otherwise it would be unplayable without a key
        try (InputStream input = Gdx.files.internal(propertiesPath).read()) {
            props.load(input);
        } catch (Exception ex) {
            throw new RuntimeException("Property file doesn't exist");
        }

        for (Map.Entry<Object, Object> entry : props.entrySet()) {
            String key = String.valueOf(entry.getKey());
            String[] parts = key.split(",");
            try {
                int x = Integer.parseInt(parts[0]), y = Integer.parseInt(parts[1]);
                int value = Integer.parseInt(String.valueOf(entry.getValue()));
                if (value == 3) {
                    HealthPickup pickup = new HealthPickup(x, y, pointManager);
                    stage.addActor(pickup);
                } else if (value == 6) {
                    EnergyDrink drink = new EnergyDrink(x, y, pointManager);
                    stage.addActor(drink);
                } else if (value == 5) {
                    Key keyGame = new Key(x, y, pointManager);
                    stage.addActor(keyGame);
                } else if (value == 2) {
                    ExitDoor exitDoor = new ExitDoor(x, y);
                    stage.addActor(exitDoor);
                } else if (value == 8) {
                    Trap trap = new Trap(x, y);
                    stage.addActor(trap);
                }
            } catch (NumberFormatException ignored) {
                // Bad coords.
            }
        }
    }
}
