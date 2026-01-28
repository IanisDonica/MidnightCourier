package de.tum.cit.fop.maze.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import de.tum.cit.fop.maze.MazeRunnerGame;
import de.tum.cit.fop.maze.system.AudioManager;
import de.tum.cit.fop.maze.system.UiUtils;

public class SettingsVideoScreen implements Screen {
    private final MazeRunnerGame game;
    private final AudioManager audioManager;
    private final Stage stage;
    private final Texture vignetteTexture;

    public SettingsVideoScreen(MazeRunnerGame game) {
        this.game = game;
        audioManager = game.getAudioManager();
        Viewport viewport = new FitViewport(1920, 1080);
        stage = new Stage(viewport, game.getSpriteBatch());
        vignetteTexture = UiUtils.buildVignetteTexture(512, 512, 0.9f);
        Image vignetteImage = new Image(vignetteTexture);
        vignetteImage.setFillParent(true);
        vignetteImage.setTouchable(Touchable.disabled);
        stage.addActor(vignetteImage);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
        stage.addListener(game.getKeyHandler());
    }
    
    @Override
    public void render(float v) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
        stage.draw();
    }

    @Override
    public void resize(int i, int i1) {
        stage.getViewport().update(i, i1, true);
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
        stage.dispose();
        vignetteTexture.dispose();
    }
}
