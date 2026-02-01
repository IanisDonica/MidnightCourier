package de.tum.cit.fop.maze.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import de.tum.cit.fop.maze.MazeRunnerGame;
import de.tum.cit.fop.maze.system.AudioManager;
import de.tum.cit.fop.maze.system.UiUtils;

/**
 * Settings screen for game-specific options.
 */
public class SettingsGameScreen implements Screen {
    /** Game instance for navigation and resources. */
    private final MazeRunnerGame game;
    /** Audio manager for UI sounds. */
    private final AudioManager audioManager;
    /** Stage hosting UI elements. */
    private final Stage stage;
    /** Vignette texture overlay. */
    private final Texture vignetteTexture;
    /** Vignette image overlay. */
    private final Image vignetteImage;
    /**
     * Creates the game settings screen.
     *
     * @param game game instance
     */
    public SettingsGameScreen(MazeRunnerGame game) {
        this.game = game;
        audioManager = game.getAudioManager();
        var camera = new OrthographicCamera();
        camera.zoom = 1f; // Set camera zoom for a closer view

        Viewport viewport = new FitViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), camera);
        stage = new Stage(viewport, game.getSpriteBatch());
        vignetteTexture = UiUtils.buildVignetteTexture(512, 512, 0.9f);
        vignetteImage = new Image(vignetteTexture);
        vignetteImage.setFillParent(true);
        vignetteImage.setTouchable(Touchable.disabled);
        stage.addActor(vignetteImage);
        Table table = new Table();
        table.setFillParent(true);

        table.add(new Label("Settings: Game", game.getSkin(), "title")).colspan(2).padBottom(15).row();

        Label devConsoleLabel = new Label("Development Console", game.getSkin(), "title");
        CheckBox devConsoleCheckbox = new CheckBox("", game.getSkin());
        devConsoleCheckbox.getImageCell().padRight(10);
        devConsoleCheckbox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                audioManager.playSound("Click.wav", 1);
                if (game.getGameScreen() != null){
                    game.getGameScreen().setDevConsole(devConsoleCheckbox.isChecked());
                }
                if (game.getSurvivalScreen() != null){
                    game.getSurvivalScreen().setDevConsole(devConsoleCheckbox.isChecked());
                }
            }
        });

        TextButton backButton = new TextButton("Back", game.getSkin());
        backButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                audioManager.playSound("Click.wav", 1);
                game.goToSettingsScreen();
            }
        });

        table.add(devConsoleLabel).pad(15);
        table.add(devConsoleCheckbox).pad(15).row();
        table.add(backButton).colspan(2).align(Align.center).size(120,45).pad(15);
        stage.addActor(table);
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
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        }
        vignetteImage.setVisible(!game.shouldRenderMenuBackground());
        stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
        stage.draw();
    }

    /**
     * Updates viewport on resize.
     *
     * @param i new width
     * @param i1 new height
     */
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

    /**
     * Disposes stage and textures.
     */
    @Override
    public void dispose() {
        stage.dispose();
        vignetteTexture.dispose();
    }
}
