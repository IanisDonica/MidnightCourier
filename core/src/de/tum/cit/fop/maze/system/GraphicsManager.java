package de.tum.cit.fop.maze.system;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;

public class GraphicsManager {

    private static final String SETTINGS_FILE = "graphics-settings.json";
    private static final String TAG = "GraphicsSettings";

    private int targetFrameRate = 60;
    private boolean vsyncEnabled = false;

    private AAMode antiAliasingMode = AAMode.MSAA_4;

    // General graphics
    private int displayMode = 1;
    private int width = 1920;
    private int height = 1080;
    private boolean resizable = true;
    private float targetAspectRatio = 16.0f / 9.0f;

    public GraphicsManager() {
        if (Gdx.app != null) {
            Gdx.app.log(TAG, "Graphics settings initialized with defaults");
        }
    }

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

    public void setFrameLimit(int fps) {
        this.targetFrameRate = MathUtils.clamp(fps, 15, 360);
        applyFrameRateSettings();
    }

    public void setResolution(int width, int height) {
        this.width = width;
        this.height = height;
        this.targetAspectRatio = (float) width / height;
    }

    public int getTargetFrameRate() {return targetFrameRate;}

    public boolean isVsyncEnabled() {return vsyncEnabled;}

    public void setVsyncEnabled(boolean enabled) {
        this.vsyncEnabled = enabled;
        applyFrameRateSettings();
    }

    public AAMode getAntiAliasingMode() {return antiAliasingMode;}

    public void setAntiAliasingMode(AAMode mode) {
        this.antiAliasingMode = mode;
        Gdx.app.log(TAG, String.format("Anti-aliasing mode set to: %s (Samples: %d)", mode.displayName, mode.samples));
    }

    public int getDisplayMode() {return displayMode;}

    public String getDisplayModeAsString() {
        return switch (displayMode) {
            case 0 -> "Windowed";
            case 1 -> "Borderless Windowed";
            case 2 -> "Fullscreen";
            default -> "Error";
        };
    }

    public void setFullscreen() {
        this.displayMode = 1;
        Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
    }

    public void setWindowed() {
        this.displayMode = 0;
        setResizable(true);
        Gdx.graphics.setUndecorated(false);
        Gdx.graphics.setWindowedMode(width / 2, height / 2); // Temp just so I can move it
    }

    public void setBorderless(){
        this.displayMode = 2;
        setResizable(false);
        Gdx.graphics.setUndecorated(true);
        Gdx.graphics.setWindowedMode(width, height); // Temp just so I can move it
    }

    public int getWidth() {return width;}

    public int getHeight() {return height;}

    public boolean isResizable() {return resizable;}

    public void setResizable(boolean resizable) {
        this.resizable = resizable;
    }

    public float getTargetAspectRatio() {return targetAspectRatio;}

    public float getCurrentFrameRate() {
        return 1.0f / Gdx.graphics.getDeltaTime();
    }

    public String getGraphicsDeviceInfo() {
        return String.format("GPU: %s | Driver: %s | GL Version: %s", Gdx.graphics.getGLVersion().getVendorString(), Gdx.graphics.getGLVersion().getRendererString(), Gdx.graphics.getGLVersion().getMajorVersion() + "." + Gdx.graphics.getGLVersion().getMinorVersion());
    }

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

    public String getSummary() {
        return String.format("FPS: %d | VSync: %s | AA: %s | Quality: %.1f%%", targetFrameRate, vsyncEnabled ? "ON" : "OFF", antiAliasingMode.displayName, (getCurrentFrameRate() / targetFrameRate) * 100);
    }

    public enum AAMode {
        DISABLED(0, "None"), MSAA_2(2, "MSAA 2x"), MSAA_4(4, "MSAA 4x"), MSAA_8(8, "MSAA 8x"), MSAA_16(16, "MSAA 16x");

        public final int samples;
        public final String displayName;

        AAMode(int samples, String displayName) {
            this.samples = samples;
            this.displayName = displayName;
        }
    }
}
