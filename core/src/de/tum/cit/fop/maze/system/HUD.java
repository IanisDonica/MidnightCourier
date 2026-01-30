package de.tum.cit.fop.maze.system;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import de.tum.cit.fop.maze.MazeRunnerGame;
import com.badlogic.gdx.math.MathUtils;

/**
 * Heads-up display for gameplay information and quick actions.
 */
public class HUD {
    /** Stage that hosts HUD actors. */
    private Stage stage;
    /** Viewport used for HUD layout. */
    private Viewport viewport;
    /** Texture region for heart icons. */
    private TextureRegion heart_texture;

    /** Label for the score. */
    private Label scoreLabel;
    /** Label for health/money. */
    private Label healthLabel;
    /** Table containing health elements. */
    private Table healthTable;

    /** Label for key status. */
    private Label keyLabel;
    /** Label for current level. */
    private Label levelLabel;
    /** Label for delivery timer title. */
    private Label deliveryTimerLabel;
    /** Label for timer value. */
    private Label timerValueLabel;
    /** Button to open the shop. */
    private TextButton shopButton;
    /** Configuration manager for key binding names. */
    private final ConfigManager configManager;
    /** Pause menu table. */
    private Table pauseTable;
    /** Top HUD table. */
    private final Table topTable;
    /** Table containing level/timer elements. */
    private final Table timerTable;
    /** Container for HUD background. */
    private final Table hudBox;
    /** Background texture for the HUD box. */
    private final Texture hudBoxTexture;
    /** Image showing regeneration status. */
    private Image regenImage;
    /** Animation for regeneration. */
    private Animation<TextureRegion> regenAnimation;
    /** Texture containing regen frames and heart sprites. */
    private final Texture regenTexture;
    /** Texture for the direction arrow. */
    private final Texture arrowTexture;
    /** Arrow image pointing to objective. */
    private final Image arrowImage;
    /** Image showing key preview. */
    private final Image keyPreviewImage;
    /** Width of the key preview image. */
    private final float keyPreviewWidth = 400f;
    /** Height of the key preview image. */
    private final float keyPreviewHeight = 200f;
    /** Margin for key preview positioning. */
    private final float keyPreviewMargin = 20f;
    /** Gap between key preview and HUD. */
    private final float keyPreviewHudGap = 10f;
    /** Spacing for arrow padding. */
    private final float arrowSpacing = 16f;
    /** Whether to show the level label. */
    private boolean showLevel = true;

    /**
     * Creates a HUD for the given game.
     *
     * @param game game instance providing skin and config
     */
    public HUD(MazeRunnerGame game){
        //this.game = game;
        this.configManager = game.getConfigManager();
        viewport = new ExtendViewport(1920, 1080);
        stage = new Stage(viewport);
        scoreLabel = new Label("Score: ", game.getSkin());
        scoreLabel.setFontScale(2.0f);

        healthLabel = new Label("Money: ", game.getSkin());
        healthLabel.setFontScale(2.0f);

        keyLabel = new Label("Key: no", game.getSkin());
        keyLabel.setFontScale(2.0f);
        levelLabel = new Label("Level: 1", game.getSkin());
        levelLabel.setFontScale(2.0f);
        timerValueLabel = new Label("", game.getSkin());
        timerValueLabel.setFontScale(2.0f);
        timerValueLabel.setColor(1f, 0f, 0f, 1f);
        timerValueLabel.setVisible(false);
        deliveryTimerLabel = new Label("", game.getSkin());
        deliveryTimerLabel.setFontScale(2.0f);
        deliveryTimerLabel.setVisible(false);

        regenTexture = new Texture(Gdx.files.internal("objects.png"));
        heart_texture = new TextureRegion(regenTexture, 0, 64, 16, 16);
        Array<TextureRegion> regenFrames = new Array<>(TextureRegion.class);
        for (int col = 0; col < 5; col++) {
            regenFrames.add(new TextureRegion(regenTexture, 128 - col * 16, 0, 16, 16));
        }
        regenAnimation = new Animation<>(0.25f, regenFrames);
        regenImage = new Image(regenFrames.first());
        regenImage.setVisible(false);

        arrowTexture = new Texture(Gdx.files.internal("Pointer.png"));
        TextureRegion arrowRegion = new TextureRegion(arrowTexture, 0, 0, 24, 24);
        arrowImage = new Image(arrowRegion);
        arrowImage.setSize(64f, 64f);
        arrowImage.setOrigin(Align.center);
        arrowImage.setVisible(true);

        keyPreviewImage = new Image();
        keyPreviewImage.setSize(keyPreviewWidth, keyPreviewHeight);
        keyPreviewImage.setVisible(false);


        hudBoxTexture = buildBoxTexture(0f, 0f, 0f, 1f);
        topTable = new Table();
        topTable.top().left();
        healthTable = new Table();
        healthTable.add(healthLabel).left();
        timerTable = new Table();
        timerTable.add(levelLabel).left();
        timerTable.add(timerValueLabel).left().padLeft(10);

        topTable.add(timerTable).left().expandX().pad(10);
        topTable.add(healthTable).left().expandX().pad(10);
        topTable.add(regenImage).left().size(64).pad(10);
        topTable.add(scoreLabel).center().expandX().pad(10);
        topTable.add(keyLabel).right().expandX().pad(10);
        topTable.add(deliveryTimerLabel).right().expandX().pad(10);
        topTable.pack();

        hudBox = new Table();
        hudBox.setBackground(new Image(hudBoxTexture).getDrawable());
        hudBox.add(topTable).expand().fill().pad(6);
        updateHudBoxLayout();
        updateKeyPreviewPosition();

        pauseTable = new Table();
        pauseTable.setFillParent(true);
        pauseTable.setVisible(false);

        Button backMain = new TextButton("Main Menu", game.getSkin());
        backMain.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
            if (pauseTable.isVisible()) {
                game.goToMenu();
            }
            }
        });
        pauseTable.add(backMain).row();

        Button unpause = new TextButton("Continue to Game", game.getSkin());
        unpause.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (pauseTable.isVisible()) {
                    game.resume();
                }
            }
        });
        pauseTable.add(unpause).row();

        Button settings = new TextButton("Settings", game.getSkin());
        settings.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (pauseTable.isVisible()) {
                    game.goToSettingsScreen();
                }
            }
        });
        pauseTable.add(settings).row();

        Button exit = new TextButton("Exit game", game.getSkin());
        exit.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Gdx.app.exit();
            }
        });
        pauseTable.add(exit).row();

        shopButton = new TextButton("Open Shop", game.getSkin());
        shopButton.setVisible(false);
        shopButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (shopButton.isVisible()) {
                    game.goToProgressionTreeScreenFromGame();
                }
            }
        });

        Table bottomTable = new Table();
        bottomTable.setFillParent(true);
        bottomTable.bottom();
        bottomTable.add(shopButton).padBottom(30);

        stage.addActor(arrowImage);
        stage.addActor(hudBox);
        stage.addActor(pauseTable);
        stage.addActor(bottomTable);
        stage.addActor(keyPreviewImage);
    }

    /**
     * Updates HUD state for the current frame.
     *
     * @param levelNumber current level number
     * @param hp current health
     * @param score current score
     * @param hasKey whether the player has the key
     * @param hasRegen whether regeneration is active
     * @param regenTimerSeconds current regen timer
     * @param regenIntervalSeconds regen interval duration
     * @param deliveryTimerSeconds delivery timer, or negative if inactive
     * @param playerX player x position
     * @param playerY player y position
     * @param keyX key x position
     * @param keyY key y position
     * @param exitX exit x position
     * @param exitY exit y position
     */
    public void update(int levelNumber, int hp, int score, boolean hasKey, boolean hasRegen,
                       float regenTimerSeconds, float regenIntervalSeconds,
                       float deliveryTimerSeconds,
                       float playerX, float playerY,
                       float keyX, float keyY, float exitX, float exitY) {
        healthTable.clear();
        levelLabel.setText("Level: " + levelNumber);
        levelLabel.setVisible(showLevel);
        levelLabel.setColor(1f, 1f, 1f, 1f);
        timerValueLabel.setVisible(false);
        scoreLabel.setText("Score: " + score);
        healthTable.center();
        healthTable.add(healthLabel);
        for (int i = 0; i < hp; i++) {
            Image heart_image = new Image(heart_texture);
            heart_image.setScale(3.0f);
            healthTable.add(heart_image).padLeft(50).padTop(40);
        }
        shopButton.setText("Open Shop (" + configManager.getKeyBindingName("openShop") + ")");
        if(hasKey){
            keyLabel.setText("Has key");
        }else{
            keyLabel.setText("No key");
        }

        if (deliveryTimerSeconds >= 0f) {
            levelLabel.setText("Timer:");
            levelLabel.setVisible(true);
            timerValueLabel.setText(String.format("%.1fs", deliveryTimerSeconds));
            timerValueLabel.setVisible(true);
            deliveryTimerLabel.setVisible(false);
        } else {
            deliveryTimerLabel.setVisible(false);
        }

        regenImage.setVisible(hasRegen);
        if (hasRegen) {
            float frameDuration = regenIntervalSeconds / 5f;
            regenAnimation.setFrameDuration(frameDuration);
            TextureRegion frame = regenAnimation.getKeyFrame(regenTimerSeconds, false);
            regenImage.setDrawable(new TextureRegionDrawable(frame));
        }

        float targetX = hasKey ? exitX : keyX;
        float targetY = hasKey ? exitY : keyY;
        float dx = targetX - playerX;
        float dy = targetY - playerY;
        float angle = MathUtils.atan2(dy, dx) * MathUtils.radiansToDegrees;
        arrowImage.setRotation(angle);
        arrowImage.setSize(64f, 64f);
        arrowImage.setVisible(true);
        updateArrowPosition();
    }

    /**
     * Shows or hides the key preview image.
     *
     * @param region texture region to display
     * @param visible whether the preview should be visible
     */
    public void showKeyPreview(TextureRegion region, boolean visible) {
        if (region != null) {
            keyPreviewImage.setDrawable(new TextureRegionDrawable(region));
        }
        keyPreviewImage.setVisible(visible);
        if (visible) {
            updateKeyPreviewPosition();
            keyPreviewImage.toFront();
        }
    }

    /**
     * Sets whether the level label should be displayed.
     *
     * @param show {@code true} to show the level label
     */
    public void setShowLevel(boolean show) {
        this.showLevel = show;
        levelLabel.setVisible(show);
    }

    /**
     * Returns the HUD stage.
     *
     * @return HUD stage
     */
    public Stage getStage(){
        return stage;
    }

    /**
     * Returns the HUD viewport.
     *
     * @return HUD viewport
     */
    public Viewport getViewport(){
        return viewport;
    }

    /**
     * Resizes the HUD layout.
     *
     * @param width new width in pixels
     * @param height new height in pixels
     */
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        updateHudBoxLayout();
        updateKeyPreviewPosition();
    }

    /**
     * Disposes HUD resources.
     */
    public void dispose() {
        stage.dispose();
        regenTexture.dispose();
        arrowTexture.dispose();
        hudBoxTexture.dispose();
    }

    /**
     * Sets visibility of the shop button.
     *
     * @param visible whether the button should be visible
     */
    public void setShopButtonVisible(boolean visible) {
        shopButton.setVisible(visible);
    }

    /**
     * Sets visibility of the pause menu.
     *
     * @param visible whether the pause menu should be visible
     */
    public void setPauseMenuVisible(boolean visible) {
        pauseTable.setVisible(visible);
    }

    /**
     * Returns whether the pause menu is visible.
     *
     * @return {@code true} if visible
     */
    public boolean isPauseMenuVisibe() {
        return pauseTable.isVisible();
    }

    /**
     * Returns whether the shop button is visible.
     *
     * @return {@code true} if visible
     */
    public boolean isShopButtonVisible() {
        return shopButton.isVisible();
    }

    /**
     * Updates the key preview position based on margins.
     */
    private void updateKeyPreviewPosition() {
        float x = keyPreviewMargin;
        float y = keyPreviewMargin;
        keyPreviewImage.setPosition(x, y);
    }

    /**
     * Updates the HUD background size and position.
     */
    private void updateHudBoxLayout() {
        float barHeight = viewport.getWorldHeight() * 0.1f;
        hudBox.setSize(viewport.getWorldWidth(), barHeight);
        hudBox.setPosition(0f, viewport.getWorldHeight() - barHeight);
    }

    /**
     * Positions the objective arrow near the bottom center.
     */
    private void updateArrowPosition() {
        float x = (viewport.getWorldWidth() / 2f) - (arrowImage.getWidth() / 2f);
        float y = viewport.getWorldHeight() * 0.1f;
        arrowImage.setPosition(x, y);
    }

    /**
     * Builds a 1x1 solid color texture used for HUD backgrounds.
     *
     * @param r red component
     * @param g green component
     * @param b blue component
     * @param a alpha component
     * @return created texture
     */
    private Texture buildBoxTexture(float r, float g, float b, float a) {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(r, g, b, a);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }
}