package de.tum.cit.fop.maze.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import de.tum.cit.fop.maze.MazeRunnerGame;
import de.tum.cit.fop.maze.system.AudioManager;
import de.tum.cit.fop.maze.system.ConfigManager;

public class SettingsAudioScreen implements Screen {
    private final MazeRunnerGame game;
    private final Stage stage;
    private final AudioManager audioManager;
    private final ConfigManager configManager;

    public SettingsAudioScreen(MazeRunnerGame game) {
        this.game = game;
        configManager = game.getConfigManager();
        audioManager = game.getAudioManager();

        var camera = new OrthographicCamera();
        camera.zoom = 1.5f; // Set camera zoom for a closer view

        Viewport viewport = new FitViewport(1920, 1080);
        stage = new Stage(viewport, game.getSpriteBatch());

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

    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
        stage.addListener(game.getKeyHandler());
    }

    @Override
    public void render(float v) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT); // Clear the screen
        stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f)); // Update the stage
        stage.draw(); // Draw the stage
    }

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

    @Override
    public void dispose() {

    }
}
