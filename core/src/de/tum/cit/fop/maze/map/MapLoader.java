package de.tum.cit.fop.maze.map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Stage;
import de.tum.cit.fop.maze.entity.collectible.EnergyDrink;
import de.tum.cit.fop.maze.entity.collectible.HealthPickup;
import de.tum.cit.fop.maze.system.PointManager;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

public class MapLoader {
    public void spawnCollectiblesFromProperties(Stage stage, PointManager pointManager, String propertiesPath) {
        Properties props = new Properties();

        try (InputStream input = Gdx.files.internal(propertiesPath).read()) {
            props.load(input);
        } catch (Exception ex) {
            Gdx.app.error("MapLoader", "Propert file mising: " + propertiesPath, ex);
            return;
        }

        for (Map.Entry<Object, Object> entry : props.entrySet()) {
            String key = String.valueOf(entry.getKey()).trim();
            String[] parts = key.split(",");
            if (parts.length != 2) {
                continue;
            }

            try {
                int x = Integer.parseInt(parts[0].trim());
                int y = Integer.parseInt(parts[1].trim());
                String value = String.valueOf(entry.getValue()).trim();
                if ("3".equals(value)) {
                    HealthPickup pickup = new HealthPickup(x, y, pointManager);
                    stage.addActor(pickup);
                    pickup.setZIndex(0);
                } else if ("2".equals(value)) {
                    EnergyDrink drink = new EnergyDrink(x, y, pointManager);
                    stage.addActor(drink);
                    drink.setZIndex(0);
                }
            } catch (NumberFormatException ignored) {
                // Bad coords.
            }
        }
    }
}
