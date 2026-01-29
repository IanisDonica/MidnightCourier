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

public class VictoryScreen implements Screen {
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
    private static final int DAILY_REWARD = 500;

    public VictoryScreen(MazeRunnerGame game){
        this.game = game;
        if (game.getGameScreen() != null && game.getGameScreen().pointManager != null) {
            survivedSeconds = game.getGameScreen().pointManager.getElapsedTime();
            finalPoints = game.getGameScreen().pointManager.getPoints();
        } else {
            survivedSeconds = 0f;
            finalPoints = 0;
        }
        var camera = new OrthographicCamera();
        camera.zoom = 1.5f; // Set camera zoom for a closer view
        audioManager = game.getAudioManager();

        Viewport viewport = new FitViewport(1920, 1080);
        stage = new Stage(viewport, game.getSpriteBatch()); // Create a stage for UI elements
        backgroundTexture = new Texture(Gdx.files.internal("Assets_Map/delivery.png"));
        backgroundImage = new Image(backgroundTexture);
        backgroundImage.setFillParent(true);
        backgroundImage.setColor(1f, 1f, 1f, 0f);
        stage.addActor(backgroundImage);
        Table table = new Table();
        table.setFillParent(true);
        table.top();
        Table content = new Table();
        content.defaults().center();

        Label titleLabel = new Label("Delivery Sucessfull", game.getSkin(), "title");
        titleLabel.setAlignment(com.badlogic.gdx.utils.Align.center);
        content.add(titleLabel).center().padBottom(10).row();

        Label subtitleLabel = new Label("(you earned " + DAILY_REWARD + " lei, go to a shop to get upgrades)", game.getSkin());
        subtitleLabel.setFontScale(1.1f);
        subtitleLabel.setAlignment(com.badlogic.gdx.utils.Align.center);
        content.add(subtitleLabel).center().padBottom(20).row();

        Table statsTable = new Table();
        statsTable.setBackground(game.getSkin().getDrawable("cell"));
        int totalSeconds = Math.max(0, (int) survivedSeconds);
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        Label survivedLabel = new Label("YOu have completed the deliver in " + String.format("%d:%02d", minutes, seconds), game.getSkin());
        Label pointsLabel = new Label("You have the completed the level with " + finalPoints + " score", game.getSkin());
        statsTable.add(survivedLabel).pad(10).row();
        statsTable.add(pointsLabel).pad(10);
        content.add(statsTable).width(900).center().padBottom(12).row();

        Label savedLabel = new Label("This score has been saved", game.getSkin());
        savedLabel.setFontScale(0.9f);
        savedLabel.setAlignment(com.badlogic.gdx.utils.Align.center);
        content.add(savedLabel).center().padBottom(40).row();

        Table contentBox = new Table();
        contentBox.setBackground(game.getSkin().getDrawable("cell"));
        contentBox.pad(40);
        contentBox.add(content).width(980);
        table.add(contentBox).width(1060).center().padTop(50).row();
        TextButton mainMenuButton = new TextButton("Main Menu", game.getSkin());
        mainMenuButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent changeEvent, Actor actor) {
                audioManager.playSound("Click.wav", 1);
                game.goToMenu();
            }
        });

        TextButton nextLevelButton = new TextButton("Next Level", game.getSkin());
        nextLevelButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent changeEvent, Actor actor) {
                // This is dumb, if I have more time ill make it, so there is a hirearchy of levels
                // mapped to certain proeprty files or something, and you don't have to do it this way
                // for now it's important that the proertyfile namesfollow the structure level-#.properties
                audioManager.playSound("Click.wav", 1);
                int currentLevel = game.getCurrentLevelNumber();
                if (currentLevel + 1 <= 5) {
                    if (currentLevel == 1) {
                        game.goToSecondCutsceneScreen(currentLevel + 1);
                        return;
                    }
                    game.goToGame(currentLevel + 1);
                    return;
                }
                game.goToGame(currentLevel);
            }
        });

        Table buttons = new Table();
        buttons.defaults().center();
        buttons.add(nextLevelButton).padRight(20);
        buttons.add(mainMenuButton);
        content.add(buttons).center().padTop(10).row();

        buttonGroup = new WidgetGroup();
        buttonGroup.setFillParent(true);
        buttonGroup.addActor(table);
        buttonGroup.getColor().a = 0f;
        stage.addActor(buttonGroup);

        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.BLACK);
        pixmap.fill();
        Texture fadeTexture = new Texture(pixmap);
        pixmap.dispose();
        fadeOverlay = new Image(fadeTexture);
        fadeOverlay.setFillParent(true);
        fadeOverlay.setColor(0f, 0f, 0f, 1f);
        stage.addActor(fadeOverlay);
    }

    @Override
    public void render(float delta) {
        if (!game.shouldRenderMenuBackground()) {
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT); // Clear the screen
        }
        stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f)); // Update the stage
        fadeTimer += delta;
        float t = fadeTimer / FADE_DURATION;
        if (t > 1f) {
            t = 1f;
        } else if (t < 0f) {
            t = 0f;
        }
        float k = 9f;
        float overlayAlpha = (float) (Math.log1p(k * (1f - t)) / Math.log1p(k));
        fadeOverlay.setColor(0f, 0f, 0f, overlayAlpha);
        backgroundImage.setColor(1f, 1f, 1f, 1f - overlayAlpha);
        if (overlayAlpha <= 0f) {
            fadeOverlay.setVisible(false);
        }
        float buttonAlpha = (fadeTimer - FADE_DURATION) / BUTTON_FADE_DURATION;
        if (buttonAlpha < 0f) {
            buttonAlpha = 0f;
        } else if (buttonAlpha > 1f) {
            buttonAlpha = 1f;
        }
        buttonGroup.getColor().a = buttonAlpha;
        stage.draw(); // Draw the stage
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true); // Update the stage viewport on resize
    }

    @Override
    public void dispose() {
        // Dispose of the stage when the screen is disposed
        stage.dispose();
        backgroundTexture.dispose();
    }

    @Override
    public void show() {
        // Set the input processor so the stage can receive input events
        Gdx.input.setInputProcessor(stage);
        stage.addListener(game.getKeyHandler());
    }

    //The following methods are part of the Screen interface but are not used in this screen.
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
