package de.tum.cit.fop.maze.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import de.tum.cit.fop.maze.MazeRunnerGame;
import de.tum.cit.fop.maze.system.AudioManager;
import de.tum.cit.fop.maze.system.UiUtils;

/**
 * Main settings screen that links to sub-settings screens.
 */
public class SettingsScreen implements Screen {
    /** Game instance for navigation and resources. */
    private final MazeRunnerGame game;
    /** Stage hosting UI elements. */
    private final Stage stage;
    /** Audio manager for UI sounds. */
    private final AudioManager audioManager;
    /** Vignette texture overlay. */
    private final Texture vignetteTexture;
    /** Vignette image overlay. */
    private final Image vignetteImage;

    /**
     * Creates the settings screen.
     *
     * @param game game instance
     */
    public SettingsScreen(MazeRunnerGame game) {
        this.game = game;
        audioManager = game.getAudioManager();
        var graphicsManager = game.getGraphicsManager();
        var camera = new OrthographicCamera();
        camera.zoom = 1f; // Set camera zoom for a closer view

        Viewport viewport = new FitViewport(graphicsManager.getWidth(), graphicsManager.getHeight(), camera);
        stage = new Stage(viewport, game.getSpriteBatch()); // Create a stage for UI elements

        vignetteTexture = UiUtils.buildVignetteTexture(512, 512, 0.9f);
        vignetteImage = new Image(vignetteTexture);
        vignetteImage.setFillParent(true);
        vignetteImage.setTouchable(Touchable.disabled);
        stage.addActor(vignetteImage);

        Table table = new Table(); // Create a table for layout
        table.setFillParent(true); // Make the table fill the stage
        stage.addActor(table); // Add the table to the stage

        // Add a label as a title
        table.add(new Label("Settings", game.getSkin(), "title")).padBottom(80).row();

        // Create and add a button to go to the game screen

        TextButton controlSettings = new TextButton("Controls", game.getSkin());
        table.add(controlSettings).width(500).padBottom(15).row();

        TextButton videoSettings = new TextButton("Video", game.getSkin());
        table.add(videoSettings).width(500).padBottom(15).row();

        TextButton audioSettings = new TextButton("Audio", game.getSkin());
        table.add(audioSettings).width(500).padBottom(15).row();

        TextButton gameSettings = new TextButton("Game", game.getSkin());
        table.add(gameSettings).width(500).padBottom(15).row();

        TextButton goBack = new TextButton("Back to the Menu", game.getSkin());
        table.add(goBack).width(500).padBottom(15).row();

        goBack.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                audioManager.playSound("Click.wav", 1);
                game.goToMenu(); // Change to the game screen when the button is pressed
            }
        });

        controlSettings.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                audioManager.playSound("Click.wav", 1);
                game.goToSettingsControlsScreen(); // Change to the settings:controls screen when the button is pressed
            }
        });

        audioSettings.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                audioManager.playSound("Click.wav", 1);
                game.goToSettingsAudioScreen();
            }
        });

        videoSettings.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                audioManager.playSound("Click.wav", 1);
                game.goToSettingsVideoScreen();
            }
        });

        gameSettings.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                audioManager.playSound("Click.wav", 1);
                game.goToSettingsGameScreen();
            }
        });
    }


    /**
     * Renders the screen.
     *
     * @param delta frame delta time
     */
    @Override
    public void render(float delta) {
        if (!game.shouldRenderMenuBackground()) {
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT); // Clear the screen
        }
        vignetteImage.setVisible(!game.shouldRenderMenuBackground());
        stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f)); // Update the stage
        stage.draw(); // Draw the stage
    }

    /**
     * Updates viewport on resize.
     *
     * @param width new width
     * @param height new height
     */
    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true); // Update the stage viewport on resize
    }

    /**
     * Disposes stage and textures.
     */
    @Override
    public void dispose() {
        // Dispose of the stage when the screen is disposed
        stage.dispose();
        vignetteTexture.dispose();
    }

    /**
     * Sets input processing for the screen.
     */
    @Override
    public void show() {
        // Set the input processor so the stage can receive input events
        Gdx.input.setInputProcessor(stage);
        stage.addListener(game.getKeyHandler());
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }
}
