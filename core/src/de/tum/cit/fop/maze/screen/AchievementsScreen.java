package de.tum.cit.fop.maze.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import de.tum.cit.fop.maze.MazeRunnerGame;
import de.tum.cit.fop.maze.system.AchievementManager;
import de.tum.cit.fop.maze.system.AudioManager;
import de.tum.cit.fop.maze.system.UiUtils;

public class AchievementsScreen implements Screen {
    private Stage stage;
    private MazeRunnerGame game;
    private final AudioManager audioManager;
    private final Texture backgroundTexture;
    private final Texture vignetteTexture;
    private final Image vignetteImage;
    private final Texture scrollbarTrackTexture;
    private final Texture scrollbarKnobTexture;
    private ScrollPane scrollPane;

    private static final float BOX_WIDTH = 520f;
    private static final float BOX_HEIGHT = 180f;

    public AchievementsScreen(MazeRunnerGame game){
        this.game = game;
        var camera = new OrthographicCamera();
        camera.zoom = 1.5f; // Set camera zoom for a closer view
        audioManager = game.getAudioManager();

        Viewport viewport = new FitViewport(1920, 1080);
        stage = new Stage(viewport, game.getSpriteBatch()); // Create a stage for UI elements
        backgroundTexture = new Texture(Gdx.files.internal("trohpyRoom.png"));
        Image backgroundImage = new Image(backgroundTexture);
        backgroundImage.setFillParent(true);
        stage.addActor(backgroundImage);
        vignetteTexture = UiUtils.buildVignetteTexture(512, 512, 0.9f);
        vignetteImage = new Image(vignetteTexture);
        vignetteImage.setFillParent(true);
        vignetteImage.setTouchable(Touchable.disabled);
        stage.addActor(vignetteImage);
        Table table = new Table();
        stage.addActor(table);
        table.setFillParent(true);
        Table contentBox = new Table();
        contentBox.setBackground(game.getSkin().getDrawable("cell"));
        contentBox.setColor(1f, 1f, 1f, 1f);
        contentBox.pad(30);
        contentBox.add(new Label("Achievements", game.getSkin(), "title")).padTop(10).row();
        TextButton mainMenuButton = new TextButton("Main Menu", game.getSkin());
        mainMenuButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent changeEvent, Actor actor) {
                audioManager.playSound("Click.wav", 1);
                game.goToMenu();
            }
        });

        TextButton resetButton = new TextButton("Reset Achievements", game.getSkin());
        resetButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent changeEvent, Actor actor) {
                audioManager.playSound("Click.wav", 1);
                AchievementManager.resetAll();
                game.goToAchievementsScreen();
            }
        });

        Table achievementsTable = new Table();
        ScrollPane.ScrollPaneStyle scrollStyle = new ScrollPane.ScrollPaneStyle();
        scrollbarTrackTexture = buildSolidTexture(8, 8, 0f, 0f, 0f, 0.6f);
        scrollbarKnobTexture = buildSolidTexture(8, 8, 1f, 1f, 1f, 0.9f);
        scrollStyle.vScroll = new TextureRegionDrawable(scrollbarTrackTexture);
        scrollStyle.vScrollKnob = new TextureRegionDrawable(scrollbarKnobTexture);
        scrollPane = new ScrollPane(achievementsTable, scrollStyle);
        scrollPane.setFadeScrollBars(false);
        scrollPane.setScrollbarsOnTop(true);
        scrollPane.setScrollingDisabled(true, false);
        scrollPane.setForceScroll(false, true);
        scrollPane.setOverscroll(false, false);

        int index = 0;
        for (var achievement : AchievementManager.getAchievements()) {
            Table achievementTable = new Table(game.getSkin());
            achievementTable.setBackground(game.getSkin().getDrawable("cell"));
            achievementTable.pad(24);
            if (!achievement.isUnlocked()) {
                achievementTable.setColor(0.7f, 0.7f, 0.7f, 1f);
            }
            Label nameLabel = new Label(achievement.getName(), game.getSkin());
            nameLabel.setAlignment(com.badlogic.gdx.utils.Align.center);
            Label descriptionLabel = new Label(achievement.getDescription(), game.getSkin());
            descriptionLabel.setAlignment(com.badlogic.gdx.utils.Align.center);
            descriptionLabel.setWrap(true);
            achievementTable.add(nameLabel).center().row();
            achievementTable.add(descriptionLabel).width(480).padTop(6).center().row();
            if (!achievement.isUnlocked()) {
                Label progressLabel = new Label(
                        achievement.getProgress() + "/" + achievement.getTarget(),
                        game.getSkin()
                );
                progressLabel.setAlignment(com.badlogic.gdx.utils.Align.center);
                achievementTable.add(progressLabel).center().padTop(8);
            }

            achievementsTable.add(achievementTable).width(BOX_WIDTH).height(BOX_HEIGHT).pad(16);
            if (index % 2 == 1) {
                achievementsTable.row();
            }
            index++;
        }
        if (index % 2 == 1) {
            achievementsTable.add().width(BOX_WIDTH).height(BOX_HEIGHT).pad(16);
            achievementsTable.row();
        }

        float visibleHeight = BOX_HEIGHT * 3.5f + 32f;
        contentBox.add(scrollPane).width(1200).height(visibleHeight);
        contentBox.row();
        Table buttons = new Table();
        buttons.add(resetButton).padRight(20);
        buttons.add(mainMenuButton);
        contentBox.add(buttons).padTop(20);
        table.add(contentBox).center();
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT); // Clear the screen
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f)); // Update the stage
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
        vignetteTexture.dispose();
        scrollbarTrackTexture.dispose();
        scrollbarKnobTexture.dispose();
    }

    @Override
    public void show() {
        // Set the input processor so the stage can receive input events
        Gdx.input.setInputProcessor(stage);
        stage.setScrollFocus(scrollPane);
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

    private Texture buildSolidTexture(int width, int height, float r, float g, float b, float a) {
        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        pixmap.setColor(r, g, b, a);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }
}
