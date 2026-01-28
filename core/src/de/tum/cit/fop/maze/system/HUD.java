package de.tum.cit.fop.maze.system;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
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
    private TextButton shopButton;
    private final ConfigManager configManager;
    private Table pauseTable;
    private Image regenImage;
    private Animation<TextureRegion> regenAnimation;
    private final Texture regenTexture;
    private final Texture arrowTexture;
    private final Image arrowImage;

    public HUD(MazeRunnerGame game){
        //this.game = game;
        this.configManager = game.getConfigManager();
        viewport = new ExtendViewport(1920, 1080);
        stage = new Stage(viewport);
        TextureAtlas textureAtlas = new TextureAtlas("assets/craft/craftacular-ui.atlas");
        heart_texture = textureAtlas.findRegion("heart");

        scoreLabel = new Label("Score: ", game.getSkin());
        scoreLabel.setFontScale(2.0f);

        healthLabel = new Label("Health: ", game.getSkin());
        healthLabel.setFontScale(2.0f);

        keyLabel = new Label("Key: no", game.getSkin());
        keyLabel.setFontScale(2.0f);
        levelLabel = new Label("Level: 1", game.getSkin());
        levelLabel.setFontScale(2.0f);

        regenTexture = new Texture(Gdx.files.internal("objects.png"));
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

        Table topTable = new Table();
        topTable.setFillParent(true);
        topTable.top();
        healthTable = new Table();
        healthTable.add(healthLabel).left();

        topTable.add(levelLabel).left().expandX().pad(10);
        topTable.add(healthTable).left().expandX().pad(10);
        topTable.add(regenImage).left().size(64).pad(10);
        topTable.add(scoreLabel).center().expandX().pad(10);
        topTable.add(keyLabel).right().expandX().pad(10);

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
        stage.addActor(topTable);
        stage.addActor(pauseTable);
        stage.addActor(bottomTable);
    }

    public void update(int levelNumber, int hp, int score, boolean hasKey, boolean hasRegen,
                       float regenTimerSeconds, float regenIntervalSeconds,
                       float playerX, float playerY,
                       float keyX, float keyY, float exitX, float exitY) {
        healthTable.clear();
        levelLabel.setText("Level: " + levelNumber);
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
        arrowImage.setPosition(10f, 10f);
        arrowImage.setVisible(true);
    }
    
    public Stage getStage(){
        return stage;
    }

    public Viewport getViewport(){
        return viewport;
    }

    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    public void dispose() {
        stage.dispose();
        regenTexture.dispose();
        arrowTexture.dispose();
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

}
