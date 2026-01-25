package de.tum.cit.fop.maze.map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.scenes.scene2d.Stage;
import de.tum.cit.fop.maze.entity.collectible.Collectible;
import de.tum.cit.fop.maze.entity.collectible.EnergyDrink;
import de.tum.cit.fop.maze.entity.collectible.ExitDoor;
import de.tum.cit.fop.maze.entity.collectible.HealthPickup;
import de.tum.cit.fop.maze.entity.collectible.Key;
import de.tum.cit.fop.maze.system.HUD;
import de.tum.cit.fop.maze.entity.obstacle.BmwEnemy;
import de.tum.cit.fop.maze.entity.obstacle.Enemy;
import de.tum.cit.fop.maze.entity.obstacle.Shop;
import de.tum.cit.fop.maze.entity.obstacle.Trap;
import de.tum.cit.fop.maze.system.PointManager;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class MapLoader {
    public TiledMapTileLayer buildCollisionLayerFromProperties(TiledMap map, String propertiesPath) {
        TiledMapTileLayer layer = createLayer(map);
        int width = layer.getWidth();
        int height = layer.getHeight();
        Properties props = loadProperties(propertiesPath);
        for (int[] entry : parseProperties(props)) {
            int x = entry[0];
            int y = entry[1];
            int value = entry[2];
            if (value == 7 || value == 2) {
                layer.setCell(x, y, new TiledMapTileLayer.Cell());
            } else if (value == 10) {
                for (int dx = 0; dx < 4; dx++) {
                    for (int dy = 0; dy < 2; dy++) {
                        int cellX = x + dx;
                        int cellY = y + dy;
                        if (cellX >= 0 && cellY >= 0 && cellX < width && cellY < height) {
                            layer.setCell(cellX, cellY, new TiledMapTileLayer.Cell());
                        }
                    }
                }
            }
        }
        return layer;
    }

    public TiledMapTileLayer buildRoadLayerFromProperties(TiledMap map, String propertiesPath) {
        TiledMapTileLayer layer = createLayer(map);
        Properties props = loadProperties(propertiesPath);
        for (int[] entry : parseProperties(props)) {
            int value = entry[2];
            if (value == 11 || value == 12) {
                layer.setCell(entry[0], entry[1], new TiledMapTileLayer.Cell());
            }
        }
        return layer;
    }

    public void spawnEntitiesFromProperties(Stage stage, PointManager pointManager, TiledMapTileLayer collisionLayer, TiledMapTileLayer roadLayer, String propertiesPath, HUD hud, List<Enemy> enemies, List<Collectible> collectibles, ExitDoor.VictoryListener victoryListener) {
        Properties props = loadProperties(propertiesPath);
        for (int[] entry : parseProperties(props)) {
            int x = entry[0];
            int y = entry[1];
            int value = entry[2];
            if (value == 3) {
                HealthPickup pickup = new HealthPickup(x, y, pointManager);
                stage.addActor(pickup);
                collectibles.add(pickup);
            } else if (value == 6) {
                EnergyDrink drink = new EnergyDrink(x, y, pointManager);
                stage.addActor(drink);
                collectibles.add(drink);
            } else if (value == 5) {
                Key keyGame = new Key(x, y, pointManager);
                stage.addActor(keyGame);
                collectibles.add(keyGame);
            } else if (value == 2) {
                ExitDoor exitDoor = new ExitDoor(x, y, pointManager, victoryListener);
                stage.addActor(exitDoor);
                collectibles.add(exitDoor);
            } else if (value == 8) {
                Trap trap = new Trap(x, y);
                stage.addActor(trap);
                // Traps are obstacles but not enemies, for now we don't save their state
                // if they are static.
            } else if (value == 4) {
                Enemy enemy = new Enemy(collisionLayer, x, y);
                stage.addActor(enemy);
                enemies.add(enemy);
            } else if (value == 10) {
                Shop shop = new Shop(x, y, hud);
                stage.addActor(shop);
            } else if (value == 13) {
                BmwEnemy bmw = new BmwEnemy(roadLayer, x, y);
                stage.addActor(bmw);
            }
        }
        refreshBmwRoadTiles();
    }

    public void refreshBmwRoadTiles() {
        BmwEnemy.recomputeRoadTiles();
    }

    private TiledMapTileLayer createLayer(TiledMap map) {
        int width = map.getProperties().get("width", Integer.class);
        int height = map.getProperties().get("height", Integer.class);
        int tileWidth = map.getProperties().get("tilewidth", Integer.class);
        int tileHeight = map.getProperties().get("tileheight", Integer.class);
        return new TiledMapTileLayer(width, height, tileWidth, tileHeight);
    }

    private Properties loadProperties(String propertiesPath) {
        Properties props = new Properties();
        try (InputStream input = openProperties(propertiesPath)) {
            props.load(input);
        } catch (Exception ex) {
            throw new RuntimeException("Property file doesn't exist");
        }
        return props;
    }

    private List<int[]> parseProperties(Properties props) {
        List<int[]> entries = new java.util.ArrayList<>();
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
                entries.add(new int[]{x, y, value});
            } catch (NumberFormatException ignored) {
                // Skip bad coordinates or values.
            }
        }
        return entries;
    }

    private InputStream openProperties(String propertiesPath) {
        if (Gdx.files.local(propertiesPath).exists()) {
            return Gdx.files.local(propertiesPath).read();
        }
        return Gdx.files.internal(propertiesPath).read();
    }
}
