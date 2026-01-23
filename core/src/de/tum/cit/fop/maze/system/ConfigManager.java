package de.tum.cit.fop.maze.system;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter;
import de.tum.cit.fop.maze.entity.collectible.Key;

import javax.swing.text.JTextComponent;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages game configuration: key bindings, settings.
 * Saves/loads from JSON file using libGDX's built-in JSON.
 */
public class ConfigManager {
    private Map<String, Integer> keyBindings;
    private static final String CONFIG_FILE = "config/keybindings.json";

    public ConfigManager() {
        keyBindings = new HashMap<>();
    }

    private void initializeDefaults() {
        keyBindings.put("up", Input.Keys.UP);
        keyBindings.put("down", Input.Keys.DOWN);
        keyBindings.put("left", Input.Keys.LEFT);
        keyBindings.put("right", Input.Keys.RIGHT);
        keyBindings.put("sprint", Input.Keys.SHIFT_LEFT);
        keyBindings.put("openShop", Input.Keys.E);
        keyBindings.put("pause", Input.Keys.ESCAPE);
        keyBindings.put("zoomIn", Input.Keys.NUMPAD_ADD);
        keyBindings.put("zoomOut", Input.Keys.NUMPAD_SUBTRACT);
        keyBindings.put("moreFog",  Input.Keys.NUMPAD_9);
        keyBindings.put("lessFog",  Input.Keys.NUMPAD_8);
        keyBindings.put("noire", Input.Keys.NUMPAD_7);
    }

    public void loadKeyBindings() {
        try {
            FileHandle file = Gdx.files.local(CONFIG_FILE);
            if (file.exists()) {
                Json json = new Json();
                @SuppressWarnings("unchecked")
                Map<String, Integer> loaded = json.fromJson(HashMap.class, file);
                if (loaded != null) {
                    keyBindings.putAll(loaded);
                }
            }
        } catch (Exception e) {
            System.err.println("Could not load keybindings, using defaults: " + e.getMessage());
            initializeDefaults();
        }
    }

    public void saveKeyBindings() {
        try {
            Json json = new Json();
            json.setOutputType(JsonWriter.OutputType.json);

            FileHandle file = Gdx.files.local(CONFIG_FILE);
            file.writeString(json.prettyPrint(keyBindings), false);
        } catch (Exception e) {
            System.err.println("Could not save keybindings: " + e.getMessage());
        }
    }

    public int getKeyBinding(String action) {
        return keyBindings.getOrDefault(action, Input.Keys.UNKNOWN);
    }

    public String getActionForKey(int keyCode) {
        for (var entry : keyBindings.entrySet()) {
            if (entry.getValue() == keyCode) {
                return entry.getKey();
            }
        }
        return null;
    }

    public String getKeyBindingName(String action) {
        return Input.Keys.toString(getKeyBinding(action));
    }

    public void setKeyBinding(String action, int keyCode) {
        keyBindings.put(action, keyCode);
        saveKeyBindings();
    }
}
