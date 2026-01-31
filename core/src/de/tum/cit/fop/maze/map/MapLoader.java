package de.tum.cit.fop.maze.map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import de.tum.cit.fop.maze.entity.collectible.Collectible;
import de.tum.cit.fop.maze.entity.collectible.DropOff;
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
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Loads map data from properties files and builds TMX content, layers, and entities.
 */
public class MapLoader {
    /**
     * Builds a TMX file from a properties file and a TMX template.
     *
     * @param propertiesPath path to the properties file containing tile values
     * @param templateTmxPath path to the TMX template file
     * @param outputTmxPath path to write the generated TMX file to
     * @return the output TMX path that was written
     */
    public String buildTmxFromProperties(String propertiesPath, String templateTmxPath, String outputTmxPath) {
        List<long[]> entries = readPropertyEntries(propertiesPath);
        String templateTmx = readTextFile(templateTmxPath);
        int[] size = readMapSizeFromProperties(entries);
        int width = size[0];
        int height = size[1];

        long[][] tiles = new long[height][width];
        for (long[] entry : entries) {
            int x = (int) entry[0];
            int y = (int) entry[1];
            long value = entry[2];
            // All tiles will be of value at least 100 and will be exactly 100 over the
            if (value < 101) {
                continue;
            }
            long gid = value - 100;
            int row = height - 1 - y; // TMX CSV is top-to-bottom; properties use bottom-left origin
            if (x < 0 || x >= width || row < 0 || row >= height) {
                continue;
            }
            tiles[row][x] = gid;
        }

        String csv = buildCsv(tiles, width, height);
        String updatedTmx = buildTmxDocument(templateTmx, width, height, csv);
        Gdx.files.local(outputTmxPath).writeString(updatedTmx, false, "UTF-8");
        return outputTmxPath;
    }

    /**
     * Builds a collision layer from a properties file.
     *
     * @param map the tiled map supplying dimensions and tile sizes
     * @param propertiesPath path to the properties file containing collision values
     * @return a collision layer with blocking cells populated
     */
    public TiledMapTileLayer buildCollisionLayerFromProperties(TiledMap map, String propertiesPath) {
        TiledMapTileLayer layer = createLayer(map);
        int width = layer.getWidth();
        int height = layer.getHeight();
        for (long[] entry : readPropertyEntries(propertiesPath)) {
            int x = (int) entry[0];
            int y = (int) entry[1];
            long value = entry[2];
            if (value >= 101) {
                continue;
            }
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

    /**
     * Builds a road layer from a properties file.
     *
     * @param map the tiled map supplying dimensions and tile sizes
     * @param propertiesPath path to the properties file containing road values
     * @return a road layer with drivable cells populated
     */
    public TiledMapTileLayer buildRoadLayerFromProperties(TiledMap map, String propertiesPath) {
        TiledMapTileLayer layer = createLayer(map);
        for (long[] entry : readPropertyEntries(propertiesPath)) {
            long value = entry[2];
            if (value >= 101) {
                continue;
            }
            if (value == 11) {
                layer.setCell((int) entry[0], (int) entry[1], new TiledMapTileLayer.Cell());
            }
        }
        return layer;
    }

    /**
     * Spawns entities into the stage based on properties entries.
     *
     * @param stage stage to attach actors to
     * @param pointManager point manager for score-related collectibles
     * @param collisionLayer collision layer used for enemy movement
     * @param roadLayer road layer used for BMW enemies
     * @param propertiesPath path to the properties file containing entity values
     * @param hud HUD instance for shop interactions
     * @param enemies list to collect spawned enemies into
     * @param collectibles list to collect spawned collectibles into
     * @param exitDoorListener callback invoked when the exit door triggers victory
     * @param dropOffListener callback invoked when the drop-off is completed
     * @param dropOffGrantsCanLeave whether drop-off should grant can-leave permission
     */
    public void spawnEntitiesFromProperties(Stage stage, PointManager pointManager, TiledMapTileLayer collisionLayer, TiledMapTileLayer roadLayer, String propertiesPath, HUD hud, List<Enemy> enemies, List<Collectible> collectibles, ExitDoor.VictoryListener exitDoorListener, DropOff.DropOffListener dropOffListener, boolean dropOffGrantsCanLeave) {
        for (long[] entry : readPropertyEntries(propertiesPath)) {
            int x = (int) entry[0];
            int y = (int) entry[1];
            long value = entry[2];
            if (value >= 101) {
                continue;
            }
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
                ExitDoor exitDoor = new ExitDoor(x, y, pointManager, exitDoorListener);
                stage.addActor(exitDoor);
                collectibles.add(exitDoor);
            } else if (value == 9) {
                DropOff dropOff = new DropOff(x, y, pointManager, dropOffListener, dropOffGrantsCanLeave);
                stage.addActor(dropOff);
                collectibles.add(dropOff);
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

    /**
     * Finds the player spawn position defined in the properties file.
     *
     * @param propertiesPath path to the properties file containing the spawn entry
     * @return the player spawn position, or {@code null} if none exists
     */
    public GridPoint2 findPlayerSpawnFromProperties(String propertiesPath) {
        for (long[] entry : readPropertyEntries(propertiesPath)) {
            long value = entry[2];
            if (value == 1) {
                return new GridPoint2((int) entry[0], (int) entry[1]);
            }
        }
        return null;
    }

    /**
     * Recomputes BMW road tiles after road layer updates.
     */
    public void refreshBmwRoadTiles() {
        BmwEnemy.recomputeRoadTiles();
    }

    /**
     * Creates a tiled layer using dimensions from the map.
     *
     * @param map the source map for width/height/tile sizes
     * @return a new tiled layer with the same dimensions as the map
     */
    private TiledMapTileLayer createLayer(TiledMap map) {
        int width = map.getProperties().get("width", Integer.class);
        int height = map.getProperties().get("height", Integer.class);
        int tileWidth = map.getProperties().get("tilewidth", Integer.class);
        int tileHeight = map.getProperties().get("tileheight", Integer.class);
        return new TiledMapTileLayer(width, height, tileWidth, tileHeight);
    }

    /**
     * Reads properties entries into a list of {@code [x, y, value]} tuples.
     *
     * @param propertiesPath path to the properties file
     * @return list of parsed entries
     * @throws RuntimeException if the properties file cannot be opened
     */
    private List<long[]> readPropertyEntries(String propertiesPath) {
        List<long[]> entries = new java.util.ArrayList<>();
        try (InputStream input = openProperties(propertiesPath);
             BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#") || line.startsWith("!")) {
                    continue;
                }
                int eqIndex = line.indexOf('=');
                if (eqIndex <= 0) {
                    continue;
                }
                String key = line.substring(0, eqIndex).trim();
                String valueText = line.substring(eqIndex + 1).trim();
                String[] parts = key.split(",");
                if (parts.length != 2) {
                    continue;
                }
                try {
                    int x = Integer.parseInt(parts[0].trim());
                    int y = Integer.parseInt(parts[1].trim());
                    long value = Long.parseLong(valueText);
                    entries.add(new long[]{x, y, value});
                } catch (NumberFormatException ignored) {
                    // Skip bad coordinates or values.
                }
            }
        } catch (Exception ex) {
            throw new RuntimeException("no such propoerty file");
        }
        return entries;
    }

    /**
     * Opens a properties file as an input stream.
     *
     * @param propertiesPath path to the properties file
     * @return input stream for the properties file
     */
    private InputStream openProperties(String propertiesPath) {
        return Gdx.files.local(propertiesPath).read();
    }

    /**
     * Reads a text file from local storage as UTF-8.
     *
     * @param path path to the file
     * @return file contents as a string
     */
    private String readTextFile(String path) {
        return Gdx.files.local(path).readString("UTF-8");
    }

    /**
     * Computes the map size from property entries.
     *
     * @param entries parsed entries containing x/y coordinates
     * @return {@code [width, height]} inferred from the maximum coordinates
     */
    private int[] readMapSizeFromProperties(List<long[]> entries) {
        int maxX = -1;
        int maxY = -1;
        for (long[] entry : entries) {
            int x = (int) entry[0];
            int y = (int) entry[1];
            if (x > maxX) maxX = x;
            if (y > maxY) maxY = y;
        }
        int width = Math.max(1, maxX + 1);
        int height = Math.max(1, maxY + 1);
        return new int[]{width, height};
    }

    /**
     * Builds a CSV tile data block from the tile array.
     *
     * @param tiles tile GIDs, indexed by row and column
     * @param width map width
     * @param height map height
     * @return CSV string compatible with TMX tile layer data
     */
    private String buildCsv(long[][] tiles, int width, int height) {
        StringBuilder csv = new StringBuilder();
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                csv.append(tiles[row][col]).append(',');
            }
            if (row + 1 < height) {
                csv.append('\n');
            }
        }
        return csv.toString();
    }

    /**
     * Builds a TMX document based on a template and CSV tile data.
     *
     * @param templateTmx TMX template contents
     * @param width map width
     * @param height map height
     * @param csv CSV tile data block
     * @return full TMX document content
     */
    private String buildTmxDocument(String templateTmx, int width, int height, String csv) {
        String propertiesBlock = findFirstMatch(templateTmx, "<properties>.*?</properties>");
        if (propertiesBlock == null) {
            propertiesBlock = "";
        }

        java.util.regex.Pattern tilesetPattern = java.util.regex.Pattern.compile("<tileset[^>]*(?:/>|>.*?</tileset>)", java.util.regex.Pattern.DOTALL);
        java.util.regex.Matcher matcher = tilesetPattern.matcher(templateTmx);
        StringBuilder tilesets = new StringBuilder();
        while (matcher.find()) {
            tilesets.append(matcher.group()).append('\n');
        }
        String tilesetBlock = tilesets.toString();

        StringBuilder tmx = new StringBuilder();
        tmx.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        tmx.append(String.format(
                "<map version=\"1.10\" tiledversion=\"1.11.2\" orientation=\"orthogonal\" renderorder=\"right-down\" width=\"%d\" height=\"%d\" tilewidth=\"32\" tileheight=\"32\" infinite=\"0\" nextlayerid=\"2\" nextobjectid=\"1\">\n",
                width,
                height
        ));
        if (!propertiesBlock.isEmpty()) {
            tmx.append(' ').append(propertiesBlock).append('\n');
        }
        if (!tilesetBlock.isEmpty()) {
            tmx.append(' ').append(tilesetBlock);
        }
        tmx.append(" <layer id=\"1\" name=\"Tile Layer 1\" width=\"").append(width).append("\" height=\"").append(height).append("\">\n");
        tmx.append("  <data encoding=\"csv\">\n");
        tmx.append(csv).append('\n');
        tmx.append("  </data>\n");
        tmx.append(" </layer>\n");
        tmx.append("</map>\n");
        return tmx.toString();
    }

    /**
     * Finds the first regex match in text.
     *
     * @param text source text
     * @param pattern regex pattern to search for
     * @return the matched text, or {@code null} if not found
     */
    private String findFirstMatch(String text, String pattern) {
        java.util.regex.Pattern regex = java.util.regex.Pattern.compile(pattern, java.util.regex.Pattern.DOTALL);
        java.util.regex.Matcher matcher = regex.matcher(text);
        return matcher.find() ? matcher.group() : null;
    }

}
