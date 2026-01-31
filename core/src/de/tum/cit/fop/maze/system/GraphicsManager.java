package de.tum.cit.fop.maze.system;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;

/**
 * Manages graphics settings, persistence, and runtime application.
 */
public class GraphicsManager {

    /** Local settings file path. */
    private static final String SETTINGS_FILE = "graphics-settings.json";
    /** Log tag for graphics settings. */
    private static final String TAG = "GraphicsSettings";

    /** Target frames per second. */
    private int targetFrameRate = 60;
    /** Whether VSync is enabled. */
    private boolean vsyncEnabled = false;

    /** Anti-aliasing mode. */
    private AAMode antiAliasingMode = AAMode.MSAA_4;

    // General graphics
    /** Display mode id. */
    private int displayMode = 1;
    /** Window width. */
    private int width = 1920;
    /** Window height. */
    private int height = 1080;
    /** Whether the window is resizable. */
    private boolean resizable = true;
    /** Target aspect ratio derived from resolution. */
    private float targetAspectRatio = 16.0f / 9.0f;

    /**
     * Creates a graphics manager with default settings.
     */
    public GraphicsManager() {
        if (Gdx.app != null) {
            Gdx.app.log(TAG, "Graphics settings initialized with defaults");
        }
    }

    /**
     * Loads graphics settings from disk.
     */
    public void load() {
        try {
            boolean exists = false;
            String json = null;

            if (Gdx.files != null) {
                if (Gdx.files.local(SETTINGS_FILE).exists()) {
                    exists = true;
                    json = Gdx.files.local(SETTINGS_FILE).readString();
                }
            } else {
                java.io.File file = new java.io.File(SETTINGS_FILE);
                if (file.exists()) {
                    exists = true;
                    json = new String(java.nio.file.Files.readAllBytes(file.toPath()));
                }
            }

            if (exists && json != null) {
                Json jsonParser = new Json();
                GraphicsManager loaded = jsonParser.fromJson(GraphicsManager.class, json);

                // Copy loaded values
                this.targetFrameRate = loaded.targetFrameRate;
                this.vsyncEnabled = loaded.vsyncEnabled;
                this.antiAliasingMode = loaded.antiAliasingMode;
                this.displayMode = loaded.displayMode;
                this.resizable = loaded.resizable;

                if (Gdx.app != null) {
                    Gdx.app.log(TAG, "Settings loaded from: " + SETTINGS_FILE);
                }
            } else {
                if (Gdx.app != null) {
                    Gdx.app.log(TAG, "No existing settings file found, using defaults");
                }
            }
        } catch (Exception e) {
            if (Gdx.app != null) {
                Gdx.app.error(TAG, "Failed to load settings", e);
            } else {
                e.printStackTrace();
            }
        }
    }

    /**
     * Saves graphics settings to disk.
     */
    public void save() {
        try {
            Json jsonWriter = new Json();
            jsonWriter.setOutputType(JsonWriter.OutputType.json);
            String json = jsonWriter.prettyPrint(this);
            if (Gdx.files != null) {
                Gdx.files.local(SETTINGS_FILE).writeString(json, false);
            } else {
                java.nio.file.Files.write(java.nio.file.Paths.get(SETTINGS_FILE), json.getBytes());
            }
            if (Gdx.app != null) {
                Gdx.app.log(TAG, "Settings saved to: " + SETTINGS_FILE);
            }
        } catch (Exception e) {
            if (Gdx.app != null) {
                Gdx.app.error(TAG, "Failed to save settings", e);
            } else {
                e.printStackTrace();
            }
        }
    }

    /**
     * Applies frame rate and VSync settings to the graphics system.
     */
    public void applyFrameRateSettings() {
        Gdx.graphics.setVSync(vsyncEnabled);

        if (vsyncEnabled) {
            // When vsync is on, set foreground FPS to display refresh rate + 1
            int refreshRate = Gdx.graphics.getDisplayMode().refreshRate;
            Gdx.graphics.setForegroundFPS(refreshRate + 1);
        } else {
            // When vsync is off, use the target frame rate
            Gdx.graphics.setForegroundFPS(targetFrameRate);
        }

        Gdx.app.log(TAG, String.format("Frame rate: %d FPS (VSync: %s)", targetFrameRate, vsyncEnabled));
    }

    /**
     * Sets the frame rate limit and applies it.
     *
     * @param fps target frames per second
     */
    public void setFrameLimit(int fps) {
        this.targetFrameRate = MathUtils.clamp(fps, 15, 360);
        applyFrameRateSettings();
    }

    /**
     * Sets resolution fields and updates aspect ratio.
     *
     * @param width new width
     * @param height new height
     */
    public void setResolution(int width, int height) {
        this.width = width;
        this.height = height;
        this.targetAspectRatio = (float) width / height;
    }

    /**
     * Returns the target frame rate.
     *
     * @return target FPS
     */
    public int getTargetFrameRate() {return targetFrameRate;}

    /**
     * Returns whether VSync is enabled.
     *
     * @return {@code true} if VSync is enabled
     */
    public boolean isVsyncEnabled() {return vsyncEnabled;}

    /**
     * Enables or disables VSync and applies frame settings.
     *
     * @param enabled new VSync state
     */
    public void setVsyncEnabled(boolean enabled) {
        this.vsyncEnabled = enabled;
        applyFrameRateSettings();
    }

    /**
     * Returns the anti-aliasing mode.
     *
     * @return AA mode
     */
    public AAMode getAntiAliasingMode() {return antiAliasingMode;}

    /**
     * Sets the anti-aliasing mode.
     *
     * @param mode new AA mode
     */
    public void setAntiAliasingMode(AAMode mode) {
        this.antiAliasingMode = mode;
        Gdx.app.log(TAG, String.format("Anti-aliasing mode set to: %s (Samples: %d)", mode.displayName, mode.samples));
    }

    /**
     * Returns the display mode id.
     *
     * @return display mode id
     */
    public int getDisplayMode() {return displayMode;}

    /**
     * Returns the display mode as a human-readable string.
     *
     * @return display mode label
     */
    public String getDisplayModeAsString() {
        return switch (displayMode) {
            case 0 -> "Windowed";
            case 1 -> "Borderless Windowed";
            case 2 -> "Fullscreen";
            default -> "Error";
        };
    }

    /**
     * Switches to fullscreen mode.
     */
    public void setFullscreen() {
        this.displayMode = 1;
        Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
    }

    /**
     * Switches to windowed mode.
     */
    public void setWindowed() {
        this.displayMode = 0;
        setResizable(true);
        Gdx.graphics.setUndecorated(false);
        Gdx.graphics.setWindowedMode(width / 2, height / 2); // Temp just so I can move it
    }

    /**
     * Switches to borderless windowed mode.
     */
    public void setBorderless(){
        this.displayMode = 2;
        setResizable(false);
        Gdx.graphics.setUndecorated(true);
        Gdx.graphics.setWindowedMode(width, height); // Temp just so I can move it
    }

    /**
     * Returns the configured width.
     *
     * @return width in pixels
     */
    public int getWidth() {return width;}

    /**
     * Returns the configured height.
     *
     * @return height in pixels
     */
    public int getHeight() {return height;}

    /**
     * Returns whether the window is resizable.
     *
     * @return {@code true} if resizable
     */
    public boolean isResizable() {return resizable;}

    /**
     * Sets whether the window is resizable.
     *
     * @param resizable new resizable state
     */
    public void setResizable(boolean resizable) {
        this.resizable = resizable;
    }

    /**
     * Returns the target aspect ratio.
     *
     * @return target aspect ratio
     */
    public String getTargetAspectRatio() {
        if (targetAspectRatio == 16.0f / 10.0f) return "16/10";
        if (targetAspectRatio == 4.0f / 3.0f) return "4/3";
        return "16/9";
    }

    /**
     * Returns the current measured frame rate.
     *
     * @return current FPS
     */
    public float getCurrentFrameRate() {
        return 1.0f / Gdx.graphics.getDeltaTime();
    }

    /**
     * Returns GPU/driver information for logging.
     *
     * @return formatted graphics device info string
     */
    public String getGraphicsDeviceInfo() {
        return String.format("GPU: %s | Driver: %s | GL Version: %s", Gdx.graphics.getGLVersion().getVendorString(), Gdx.graphics.getGLVersion().getRendererString(), Gdx.graphics.getGLVersion().getMajorVersion() + "." + Gdx.graphics.getGLVersion().getMinorVersion());
    }

    /**
     * Logs current graphics settings to the application log.
     */
    public void logCurrentSettings() {
        Gdx.app.log(TAG, "=== GRAPHICS SETTINGS ===");
        Gdx.app.log(TAG, "Frame Rate: " + targetFrameRate + " FPS");
        Gdx.app.log(TAG, "VSync: " + vsyncEnabled);
        Gdx.app.log(TAG, "Anti-Aliasing: " + antiAliasingMode.displayName);
        Gdx.app.log(TAG, "Resolution: " + width + "x" + height);
        Gdx.app.log(TAG, "Fullscreen: " + displayMode);
        Gdx.app.log(TAG, getGraphicsDeviceInfo());
        Gdx.app.log(TAG, "========================");
    }

    /**
     * Returns a concise summary of graphics performance.
     *
     * @return summary string
     */
    public String getSummary() {
        return String.format("FPS: %d | VSync: %s | AA: %s | Quality: %.1f%%", targetFrameRate, vsyncEnabled ? "ON" : "OFF", antiAliasingMode.displayName, (getCurrentFrameRate() / targetFrameRate) * 100);
    }

    /**
     * Anti-aliasing modes and sample counts.
     */
    public enum AAMode {
        DISABLED(0, "None"), MSAA_2(2, "MSAA 2x"), MSAA_4(4, "MSAA 4x"), MSAA_8(8, "MSAA 8x"), MSAA_16(16, "MSAA 16x");

        /** Sample count for the AA mode. */
        public final int samples;
        /** Display name for UI. */
        public final String displayName;

        /**
         * Creates an AA mode.
         *
         * @param samples sample count
         * @param displayName display label
         */
        AAMode(int samples, String displayName) {
            this.samples = samples;
            this.displayName = displayName;
        }
    }
}
