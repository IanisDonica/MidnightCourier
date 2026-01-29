package de.tum.cit.fop.maze.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import de.tum.cit.fop.maze.MazeRunnerGame;
import de.tum.cit.fop.maze.system.AudioManager;

public class CutsceneScreen implements Screen {
    private static final float FADE_IN_SECONDS = 1.5f;
    private static final float SLIDE_DURATION = FADE_IN_SECONDS;

    private final MazeRunnerGame game;
    private final Stage stage;
    private final AudioManager audioManager;
    private final Texture[] textures = new Texture[3];
    private final Image[] images = new Image[3];
    private final TextButton startButton;
    private float elapsed = 0f;

    public CutsceneScreen(MazeRunnerGame game) {
        this.game = game;
        this.audioManager = game.getAudioManager();

        Viewport viewport = new FitViewport(1920, 1080);
        stage = new Stage(viewport, game.getSpriteBatch());

        textures[0] = new Texture(Gdx.files.internal("Assets_Map/Comic Title1.png"));
        textures[1] = new Texture(Gdx.files.internal("Assets_Map/Comic Title2.png"));
        textures[2] = new Texture(Gdx.files.internal("Assets_Map/Comic Title3.png"));

        for (int i = 0; i < images.length; i++) {
            images[i] = new Image(textures[i]);
            images[i].setFillParent(true);
            images[i].getColor().a = 0f;
            stage.addActor(images[i]);
        }

        startButton = new TextButton("Start your first delivery", game.getSkin());
        startButton.setVisible(false);
        startButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                audioManager.playSound("Click.wav", 1);
                game.goToGame(1);
            }
        });

        Table buttonTable = new Table();
        buttonTable.setFillParent(true);
        buttonTable.bottom().padBottom(80);
        buttonTable.add(startButton).width(520).height(80);
        stage.addActor(buttonTable);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
        stage.addListener(game.getKeyHandler());
    }

    @Override
    public void render(float delta) {
        if (!game.shouldRenderMenuBackground()) {
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        }
        elapsed += delta;

        int activeIndex = (int) (elapsed / SLIDE_DURATION);
        if (activeIndex < images.length) {
            float t = elapsed - (activeIndex * SLIDE_DURATION);
            float alpha = Math.max(0f, Math.min(1f, t / FADE_IN_SECONDS));
            for (int i = 0; i < images.length; i++) {
                if (i < activeIndex) {
                    images[i].getColor().a = 1f;
                } else if (i == activeIndex) {
                    images[i].getColor().a = alpha;
                } else {
                    images[i].getColor().a = 0f;
                }
            }
        } else {
            for (int i = 0; i < images.length; i++) {
                images[i].getColor().a = 1f;
            }
            startButton.setVisible(true);
        }

        stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void dispose() {
        stage.dispose();
        for (Texture texture : textures) {
            if (texture != null) {
                texture.dispose();
            }
        }
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
