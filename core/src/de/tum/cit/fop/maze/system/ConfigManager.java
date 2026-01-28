package de.tum.cit.fop.maze.system;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;

import java.util.HashMap;
import java.util.Map;

/**
 * Manages game configuration: key bindings, settings.
 * Saves/loads from JSON file using libGDX's built-in JSON.
 */
public class ConfigManager {
    private static final String KEYBINDINGS_CONFIG_FILE = "config/keybindings.json";
    private static final String AUDIO_CONFIG_FILE = "config/audio.json";
    private Map<String, Integer> keyBindings;
    private Map<String, Float> audioSettings;

    public ConfigManager() {
        keyBindings = new HashMap<>();
        audioSettings = new HashMap<>();
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
        keyBindings.put("moreFog", Input.Keys.NUMPAD_9);
        keyBindings.put("lessFog", Input.Keys.NUMPAD_8);
        keyBindings.put("noire", Input.Keys.NUMPAD_7);
    }

    private void initializeAudioSettings(){
        audioSettings.put("masterVolume", 1f);
        audioSettings.put("soundEffectsVolume", 1f);
        audioSettings.put("musicVolume", 1f);
    }
    public void loadKeyBindings() {
        try {
            FileHandle file = Gdx.files.local(KEYBINDINGS_CONFIG_FILE);
            if (file.exists()) {
                Json json = new Json();
                @SuppressWarnings("unchecked")
                Map<String, Integer> loaded = json.fromJson(HashMap.class, file);
                if (loaded != null) {
                    keyBindings = loaded;
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

            FileHandle file = Gdx.files.local(KEYBINDINGS_CONFIG_FILE);
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

    public void loadAudioSettings() {
        try {
            FileHandle file = Gdx.files.local(AUDIO_CONFIG_FILE);
            if (file.exists()) {
                Json json = new Json();
                @SuppressWarnings("unchecked")
                Map<String, Object> loaded = json.fromJson(HashMap.class, file);
                if (loaded != null) {
                    for (Map.Entry<String, Object> entry : loaded.entrySet()) {
                        Object value = entry.getValue();
                        if (value instanceof Float) {
                            audioSettings.put(entry.getKey(), (Float) value);
                        } else if (value instanceof Map) {
                            // Handle the case where it's serialized as a map with class/value
                            Map<String, Object> valueMap = (Map<String, Object>) value;
                            if (valueMap.containsKey("value")) {
                                Object val = valueMap.get("value");
                                if (val instanceof Number) {
                                    audioSettings.put(entry.getKey(), ((Number) val).floatValue());
                                }
                            }
                        } else if (value instanceof Number) {
                            audioSettings.put(entry.getKey(), ((Number) value).floatValue());
                        }
                    }
                }
            }
            // Ensure defaults if missing from file
            if (!audioSettings.containsKey("masterVolume")) audioSettings.put("masterVolume", 1f);
            if (!audioSettings.containsKey("soundEffectsVolume")) audioSettings.put("soundEffectsVolume", 1f);
            if (!audioSettings.containsKey("musicVolume")) audioSettings.put("musicVolume", 1f);

        } catch (Exception e) {
            System.err.println("Could not load audio Settings, using defaults: " + e.getMessage());
            initializeAudioSettings();
        }
    }

    public void saveAudioSettings() {
        try {
            Json json = new Json();
            json.setOutputType(JsonWriter.OutputType.json);
            FileHandle file = Gdx.files.local(AUDIO_CONFIG_FILE);
            file.writeString(json.prettyPrint(audioSettings), false);
        }
        catch (Exception e) {
            System.err.println("Could not save audio settings: " + e.getMessage());
        }
    }

    public float getVolume(String type) {
        return audioSettings.getOrDefault(type, 1.0f);
    }

    public void setVolume(String type, float volume) {
        audioSettings.put(type, volume);
    }
}