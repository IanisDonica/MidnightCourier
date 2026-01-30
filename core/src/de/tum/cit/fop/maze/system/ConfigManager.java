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
    /** Path to the key bindings configuration file. */
    private static final String KEYBINDINGS_CONFIG_FILE = "config/keybindings.json";
    /** Path to the audio settings configuration file. */
    private static final String AUDIO_CONFIG_FILE = "config/audio.json";
    /** Map of action names to key codes. */
    private Map<String, Integer> keyBindings;
    /** Map of audio setting names to volume values. */
    private Map<String, Float> audioSettings;

    /**
     * Creates a configuration manager with empty settings maps.
     */
    public ConfigManager() {
        keyBindings = new HashMap<>();
        audioSettings = new HashMap<>();
    }

    /**
     * Initializes default key bindings.
     */
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

    /**
     * Initializes default audio settings.
     */
    private void initializeAudioSettings(){
        audioSettings.put("masterVolume", 1f);
        audioSettings.put("soundEffectsVolume", 1f);
        audioSettings.put("musicVolume", 1f);
    }

    /**
     * Loads key bindings from disk, falling back to defaults on failure.
     */
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

    /**
     * Saves current key bindings to disk.
     */
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

    /**
     * Returns the key code for a given action.
     *
     * @param action action name
     * @return key code or {@link Input.Keys#UNKNOWN}
     */
    public int getKeyBinding(String action) {
        return keyBindings.getOrDefault(action, Input.Keys.UNKNOWN);
    }

    /**
     * Returns the action mapped to a specific key code.
     *
     * @param keyCode key code to look up
     * @return action name or {@code null} if not found
     */
    public String getActionForKey(int keyCode) {
        for (var entry : keyBindings.entrySet()) {
            if (entry.getValue() == keyCode) {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * Returns the human-readable key name for an action.
     *
     * @param action action name
     * @return key name string
     */
    public String getKeyBindingName(String action) {
        return Input.Keys.toString(getKeyBinding(action));
    }

    /**
     * Sets a key binding and saves it immediately.
     *
     * @param action action name
     * @param keyCode key code to bind
     */
    public void setKeyBinding(String action, int keyCode) {
        keyBindings.put(action, keyCode);
        saveKeyBindings();
    }

    /**
     * Loads audio settings from disk, falling back to defaults on failure.
     */
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

    /**
     * Saves current audio settings to disk.
     */
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

    /**
     * Returns a volume setting by name.
     *
     * @param type setting name
     * @return volume value, defaulting to 1.0
     */
    public float getVolume(String type) {
        return audioSettings.getOrDefault(type, 1.0f);
    }

    /**
     * Sets a volume setting by name.
     *
     * @param type setting name
     * @param volume volume value
     */
    public void setVolume(String type, float volume) {
        audioSettings.put(type, volume);
    }
}
