package de.tum.cit.fop.maze.system;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;

/**
 * Resolution scaler that renders to internal resolution then upscales to window
 * Similar to CS:GO's resolution scaling - lower quality but fills the screen
 */
public class ResolutionScaler {

    private FrameBuffer frameBuffer;
    private int internalWidth;
    private int internalHeight;
    private int windowWidth;
    private int windowHeight;
    private SpriteBatch scaleBatch;
    private boolean enabled = true;

    /**
     * Creates a resolution scaler
     * @param internalWidth internal render resolution width
     * @param internalHeight internal render resolution height
     * @param spriteBatch batch to use for upscaling
     */
    public ResolutionScaler(int internalWidth, int internalHeight, SpriteBatch spriteBatch) {
        this.internalWidth = internalWidth;
        this.internalHeight = internalHeight;
        this.windowWidth = Gdx.graphics.getWidth();
        this.windowHeight = Gdx.graphics.getHeight();
        this.scaleBatch = spriteBatch;

        // Create a framebuffer at internal resolution
        this.frameBuffer = new FrameBuffer(
                Pixmap.Format.RGB888,
                internalWidth,
                internalHeight,
                false
        );

        Gdx.app.log("ResolutionScaler",
                String.format("Initialized: %dx%d internal -> %dx%d window",
                        internalWidth, internalHeight, windowWidth, windowHeight));
    }

    /**
     * Begin rendering to internal framebuffer
     * Call this before rendering your game content
     */
    public void beginRender() {
        if (!enabled) return;

        frameBuffer.begin();
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
    }

    /**
     * End rendering to internal framebuffer and upscale to window
     * Call this after rendering your game content
     */
    public void endRender() {
        if (!enabled) return;

        frameBuffer.end();

        // Render the framebuffer texture upscaled to window size
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        scaleBatch.begin();
        scaleBatch.draw(
                frameBuffer.getColorBufferTexture(),
                0, 0,                           // position
                windowWidth, windowHeight,      // size (upscaled to window)
                0, 0,                           // texture offset
                frameBuffer.getWidth(), frameBuffer.getHeight(),  // texture size
                false, true                     // flip x, flip y (framebuffer is inverted)
        );
        scaleBatch.end();
    }

    /**
     * Update internal resolution
     * @param newWidth new internal width
     * @param newHeight new internal height
     */
    public void setInternalResolution(int newWidth, int newHeight) {
        if (internalWidth == newWidth && internalHeight == newHeight) return;

        this.internalWidth = newWidth;
        this.internalHeight = newHeight;

        // Recreate framebuffer with new resolution
        if (frameBuffer != null) {
            frameBuffer.dispose();
        }
        frameBuffer = new FrameBuffer(
                Pixmap.Format.RGB888,
                internalWidth,
                internalHeight,
                false
        );

        Gdx.app.log("ResolutionScaler",
                String.format("Resolution changed to: %dx%d", internalWidth, internalHeight));
    }

    /**
     * Called when a window is resized
     */
    public void onWindowResize(int newWindowWidth, int newWindowHeight) {
        this.windowWidth = newWindowWidth;
        this.windowHeight = newWindowHeight;
        Gdx.app.log("ResolutionScaler",
                String.format("Window resized to: %dx%d", windowWidth, windowHeight));
    }

    /**
     * Enable/disable resolution scaling
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Get current scaling factor (how much the internal resolution is upscaled)
     */
    public float getScaleFactor() {
        return (float) windowWidth / internalWidth;  // or windowHeight / internalHeight
    }

    /**
     * Cleanup resources
     */
    public void dispose() {
        if (frameBuffer != null) {
            frameBuffer.dispose();
        }
    }

    // Getters
    public int getInternalWidth() { return internalWidth; }
    public int getInternalHeight() { return internalHeight; }
    public int getWindowWidth() { return windowWidth; }
    public int getWindowHeight() { return windowHeight; }
    public boolean isEnabled() { return enabled; }
}
