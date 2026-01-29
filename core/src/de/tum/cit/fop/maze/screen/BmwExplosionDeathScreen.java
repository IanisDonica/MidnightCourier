package de.tum.cit.fop.maze.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import de.tum.cit.fop.maze.MazeRunnerGame;
import de.tum.cit.fop.maze.system.AudioManager;

public class BmwExplosionDeathScreen implements Screen {
    private final MazeRunnerGame game;
    private final Stage stage;
    private final AudioManager audioManager;
    private final Image fadeOverlay;
    private final Texture backgroundTexture;
    private final Image backgroundImage;
    private final WidgetGroup buttonGroup;
    private final float survivedSeconds;
    private final int finalPoints;
    private float fadeTimer = 0f;
    private static final float FADE_DURATION = 2f;
    private static final float BUTTON_FADE_DURATION = 1.0f;
    private final Texture fadeTexture;

    public BmwExplosionDeathScreen(MazeRunnerGame game) {
        this.game = game;
        if (game.getGameScreen() != null && game.getGameScreen().pointManager != null) {
            survivedSeconds = game.getGameScreen().pointManager.getElapsedTime();
            finalPoints = game.getGameScreen().pointManager.getPoints();
        } else {
            survivedSeconds = 0f;
            finalPoints = 0;
        }
        var camera = new OrthographicCamera();
        camera.zoom = 1.5f;
        audioManager = game.getAudioManager();

        Viewport viewport = new FitViewport(1920, 1080);
        stage = new Stage(viewport, game.getSpriteBatch());
        backgroundTexture = new Texture(Gdx.files.internal("Assets_Map/Orthodox_crosses_in_Fort_Ross.jpg"));
        backgroundImage = new Image(backgroundTexture);
        backgroundImage.setFillParent(true);
        backgroundImage.setColor(1f, 1f, 1f, 0f);
        backgroundImage.setTouchable(Touchable.disabled);
        stage.addActor(backgroundImage);
        Table table = new Table();
        table.setFillParent(true);
        table.top();
        Table content = new Table();
        content.defaults().center();

        Label titleLabel = new Label("You died!", game.getSkin(), "title");
        titleLabel.setAlignment(com.badlogic.gdx.utils.Align.center);
        content.add(titleLabel).center().padBottom(10).row();

        Label subtitleLabel = new Label("GlovoDriver 2004-2026", game.getSkin());
        subtitleLabel.setFontScale(1.2f);
        subtitleLabel.setAlignment(com.badlogic.gdx.utils.Align.center);
        content.add(subtitleLabel).center().padBottom(20).row();

        Table statsTable = new Table();
        statsTable.setBackground(game.getSkin().getDrawable("cell"));
        int totalSeconds = Math.max(0, (int) survivedSeconds);
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        Label survivedLabel = new Label("You survived for: " + String.format("%d:%02d", minutes, seconds), game.getSkin());
        Label pointsLabel = new Label("You had a score of " + finalPoints + " points", game.getSkin());
        statsTable.add(survivedLabel).pad(10).row();
        statsTable.add(pointsLabel).pad(10);
        content.add(statsTable).width(700).center().padBottom(12).row();

        Label unsavedLabel = new Label("(This score has not been saved)", game.getSkin());
        unsavedLabel.setFontScale(0.9f);
        unsavedLabel.setAlignment(com.badlogic.gdx.utils.Align.center);
        content.add(unsavedLabel).center().padBottom(40).row();

        Table contentBox = new Table();
        contentBox.setBackground(game.getSkin().getDrawable("cell"));
        contentBox.pad(30);
        contentBox.add(content).width(820);
        table.add(contentBox).width(900).center().padTop(50).row();
        TextButton mainMenuButton = new TextButton("Main Menu", game.getSkin());
        mainMenuButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent changeEvent, Actor actor) {
                audioManager.playSound("Click.wav", 1);
                game.goToMenu();
            }
        });

        TextButton retryButton = new TextButton("Reincarnate", game.getSkin());
        retryButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent changeEvent, Actor actor) {
                audioManager.playSound("Click.wav", 1);
                if (game.getCurrentLevelNumber() != 0) {
                    game.goToGame(game.getCurrentLevelNumber());
                } else {
                    System.out.print("S");
                    game.goToEndless();
                }
            }
        });

        TextButton rageQuitButton = new TextButton("Rage quit", game.getSkin());
        rageQuitButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent changeEvent, Actor actor) {
                Gdx.app.exit();
            }
        });

        Table buttons = new Table();
        buttons.defaults().center();
        buttons.add(retryButton).padRight(20);
        buttons.add(mainMenuButton).padRight(20);
        buttons.add(rageQuitButton);
        content.add(buttons).center().padTop(10).row();

        buttonGroup = new WidgetGroup();
        buttonGroup.setFillParent(true);
        buttonGroup.addActor(table);
        buttonGroup.getColor().a = 0f;
        stage.addActor(buttonGroup);

        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.BLACK);
        pixmap.fill();
        fadeTexture = new Texture(pixmap);
        pixmap.dispose();
        fadeOverlay = new Image(fadeTexture);
        fadeOverlay.setFillParent(true);
        fadeOverlay.setColor(0f, 0f, 0f, 1f);
        fadeOverlay.setTouchable(Touchable.disabled);
        stage.addActor(fadeOverlay);
    }

    @Override
    public void render(float delta) {
        if (!game.shouldRenderMenuBackground()) {
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        }
        fadeTimer += delta;
        float alpha = Math.min(1f, fadeTimer / FADE_DURATION);
        backgroundImage.setColor(1f, 1f, 1f, alpha);
        float buttonAlpha = Math.min(1f, fadeTimer / BUTTON_FADE_DURATION);
        buttonGroup.getColor().a = buttonAlpha;
        fadeOverlay.setColor(0f, 0f, 0f, 1f - alpha);
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
        backgroundTexture.dispose();
        fadeTexture.dispose();
    }

    @Override
    public void show() {
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
