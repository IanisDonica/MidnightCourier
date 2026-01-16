package de.tum.cit.fop.maze.map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Stage;
import de.tum.cit.fop.maze.entity.collectible.EnergyDrink;
import de.tum.cit.fop.maze.entity.collectible.ExitDoor;
import de.tum.cit.fop.maze.entity.collectible.HealthPickup;
import de.tum.cit.fop.maze.entity.collectible.Key;
import de.tum.cit.fop.maze.system.PointManager;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

public class MapLoader {
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
                }
            } catch (NumberFormatException ignored) {
                // Bad coords.
            }
        }
    }
}