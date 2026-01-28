package de.tum.cit.fop.maze;

import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import de.tum.cit.fop.maze.system.GraphicsManager;
import games.spooky.gdx.nativefilechooser.desktop.DesktopFileChooser;

/**
 * The DesktopLauncher class is the entry point for the desktop version of the Maze Runner game.
 * It sets up the game window and launches the game using the LibGDX framework.
 */
public class DesktopLauncher {
    /**
     * The main method sets up the configuration for the game window and starts the application.
     *
     * @param arg Command line arguments (not used in this application)
     */
    public static void main(String[] arg) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        GraphicsManager graphicsManager = new GraphicsManager();
        graphicsManager.load();

        config.setTitle("Maze Runner"); // Set the window title
        config.setWindowedMode(graphicsManager.getWidth(), graphicsManager.getHeight());
        config.setResizable(graphicsManager.isResizable());

        config.setBackBufferConfig(8, 8, 8, 8, 16, 0, graphicsManager.getAntiAliasingMode().samples);
        config.setForegroundFPS(graphicsManager.getTargetFrameRate());
        config.useVsync(graphicsManager.isVsyncEnabled());
        config.setIdleFPS(10);

        // Get the display mode of the current monitor
        Graphics.DisplayMode displayMode = Lwjgl3ApplicationConfiguration.getDisplayMode();
        // Set the window size to 80% of the screen width and height

        if (graphicsManager.getDisplayMode() == 2) {
            config.setFullscreenMode(displayMode);
        } else if (graphicsManager.getDisplayMode() == 0) {
            if (graphicsManager.getWidth() > 0 && graphicsManager.getHeight() > 0) {
                config.setWindowedMode(graphicsManager.getWidth(), graphicsManager.getHeight());
            } else {
                config.setWindowedMode(Math.round(0.8f * displayMode.width), Math.round(0.8f * displayMode.height));
            }
        } else {
            config.setWindowedMode(displayMode.width, displayMode.height);
            config.setDecorated(false);
        }

        new Lwjgl3Application(new MazeRunnerGame(new DesktopFileChooser()), config);
    }
}
