package de.tum.cit.fop.maze.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import de.tum.cit.fop.maze.MazeRunnerGame;
import de.tum.cit.fop.maze.system.AudioManager;
import de.tum.cit.fop.maze.system.ConfigManager;
import de.tum.cit.fop.maze.system.UiUtils;

/**
 * Settings screen for audio volume controls.
 */
public class SettingsAudioScreen implements Screen {
    /** Game instance for navigation and resources. */
    private final MazeRunnerGame game;
    /** Stage hosting UI elements. */
    private final Stage stage;
    /** Audio manager for volume control. */
    private final AudioManager audioManager;
    /** Configuration manager for saving settings. */
    private final ConfigManager configManager;
    /** Vignette texture overlay. */
    private final Texture vignetteTexture;
    /** Vignette image overlay. */
    private final Image vignetteImage;

    /**
     * Creates the audio settings screen.
     *
     * @param game game instance
     */
    public SettingsAudioScreen(MazeRunnerGame game) {
        this.game = game;
        configManager = game.getConfigManager();
        audioManager = game.getAudioManager();
        var graphicsManager = game.getGraphicsManager();

        var camera = new OrthographicCamera();
        camera.zoom = 1f; // Set camera zoom for a closer view

        Viewport viewport = new FitViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), camera);
        stage = new Stage(viewport, game.getSpriteBatch());

        vignetteTexture = UiUtils.buildVignetteTexture(512, 512, 0.9f);
        vignetteImage = new Image(vignetteTexture);
        vignetteImage.setFillParent(true);
        vignetteImage.setTouchable(Touchable.disabled);
        stage.addActor(vignetteImage);

        Table table = new Table(); // Create a table for layout
        table.setFillParent(true); // Make the table fill the stage
        stage.addActor(table);
        table.add(new Label("Settings: Audio", game.getSkin(), "title")).colspan(2).padBottom(80).row();

        Slider masterVolumeSlider = new Slider(0f, 1f, 0.01f, false, game.getSkin());
        Slider soundEffectsVolumeSlider = new Slider(0f, 1f, 0.01f, false, game.getSkin());
        Slider musicVolumeSlider = new Slider(0f, 1f, 0.01f, false, game.getSkin());
        Label masterVolumeLabel = new Label("Master Volume", game.getSkin(), "title");
        Label soundEffectsVolumeLabel = new Label("Sound Effects Volume", game.getSkin(), "title");
        Label musicVolumeLabel = new Label("Music", game.getSkin(), "title");


        masterVolumeSlider.setValue(audioManager.getMasterVolume());
        soundEffectsVolumeSlider.setValue(audioManager.getSoundEffectsVolume());
        musicVolumeSlider.setValue(audioManager.getMusicVolume());

        table.add(masterVolumeLabel).colspan(2).row();
        table.add(masterVolumeSlider).colspan(2).width(800).padBottom(15).row();
        table.add(soundEffectsVolumeLabel).colspan(2).row();
        table.add(soundEffectsVolumeSlider).width(800).colspan(2).padBottom(15).row();
        table.add(musicVolumeLabel).colspan(2).row();
        table.add(musicVolumeSlider).colspan(2).width(800).row();

        masterVolumeSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                audioManager.playSound("Knife.wav", 1, 0.2f, 0.001f);
                audioManager.setMasterVolume(masterVolumeSlider.getValue());
                configManager.saveAudioSettings();
            }
        });

        soundEffectsVolumeSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                audioManager.playSound("Knife.wav", 1, 0.2f, 0.001f);
                audioManager.setSoundEffectsVolume(soundEffectsVolumeSlider.getValue());
                configManager.saveAudioSettings();
            }
        });

        musicVolumeSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                audioManager.playSound("Knife.wav", 1, 0.2f, 0.001f);
                audioManager.setMusicVolume(musicVolumeSlider.getValue());
                configManager.saveAudioSettings();
            }
        });

        TextButton menuButton = new TextButton("Menu", game.getSkin());
        menuButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                audioManager.playSound("Click.wav", 1);
                game.goToSettingsScreen();
            }
        });
        table.add(menuButton).colspan(2).align(Align.center).pad(15).size(120,45).row();
    }

    /**
     * Sets input processing for the screen.
     */
    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
        stage.addListener(game.getKeyHandler());
    }

    /**
     * Renders the screen.
     *
     * @param v frame delta time
     */
    @Override
    public void render(float v) {
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

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    /**
     * Disposes stage and textures.
     */
    @Override
    public void dispose() {
        stage.dispose();
        vignetteTexture.dispose();
    }
}
