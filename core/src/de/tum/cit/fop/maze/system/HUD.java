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

public class HUD {
    //private MazeRunnerGame game;
    private Stage stage;
    private Viewport viewport;
    private TextureRegion heart_texture;

    private Label scoreLabel;
    private Label healthLabel;
    private Table healthTable;

    private Label keyLabel;
    private Label levelLabel;
    private Label deliveryTimerLabel;
    private Label timerValueLabel;
    private TextButton shopButton;
    private final ConfigManager configManager;
    private Table pauseTable;
    private final Table topTable;
    private final Table timerTable;
    private final Table hudBox;
    private final Texture hudBoxTexture;
    private Image regenImage;
    private Animation<TextureRegion> regenAnimation;
    private final Texture regenTexture;
    private final Texture arrowTexture;
    private final Image arrowImage;
    private final Image keyPreviewImage;
    private final float keyPreviewWidth = 400f;
    private final float keyPreviewHeight = 200f;
    private final float keyPreviewMargin = 20f;
    private final float keyPreviewHudGap = 10f;
    private final float arrowSpacing = 16f;
    private boolean showLevel = true;

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

    public void setShowLevel(boolean show) {
        this.showLevel = show;
        levelLabel.setVisible(show);
    }

    public Stage getStage(){
        return stage;
    }

    public Viewport getViewport(){
        return viewport;
    }

    public void resize(int width, int height) {
        viewport.update(width, height, true);
        updateHudBoxLayout();
        updateKeyPreviewPosition();
    }

    public void dispose() {
        stage.dispose();
        regenTexture.dispose();
        arrowTexture.dispose();
        hudBoxTexture.dispose();
    }

    public void setShopButtonVisible(boolean visible) {
        shopButton.setVisible(visible);
    }

    public void setPauseMenuVisible(boolean visible) {
        pauseTable.setVisible(visible);
    }

    public boolean isPauseMenuVisibe() {
        return pauseTable.isVisible();
    }

    public boolean isShopButtonVisible() {
        return shopButton.isVisible();
    }

    private void updateKeyPreviewPosition() {
        float x = keyPreviewMargin;
        float y = keyPreviewMargin;
        keyPreviewImage.setPosition(x, y);
    }

    private void updateHudBoxLayout() {
        float barHeight = viewport.getWorldHeight() * 0.1f;
        hudBox.setSize(viewport.getWorldWidth(), barHeight);
        hudBox.setPosition(0f, viewport.getWorldHeight() - barHeight);
    }

    private void updateArrowPosition() {
        float x = (viewport.getWorldWidth() / 2f) - (arrowImage.getWidth() / 2f);
        float y = viewport.getWorldHeight() * 0.1f;
        arrowImage.setPosition(x, y);
    }

    private Texture buildBoxTexture(float r, float g, float b, float a) {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(r, g, b, a);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }
}
