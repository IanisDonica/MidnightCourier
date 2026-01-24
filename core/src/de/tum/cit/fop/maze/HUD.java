package de.tum.cit.fop.maze;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import de.tum.cit.fop.maze.system.ConfigManager;

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

    public HUD(MazeRunnerGame game){
        //this.game = game;
        this.configManager = game.getConfigManager();
        viewport = new StretchViewport(1920, 1080);
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

        Table topTable = new Table();
        topTable.setFillParent(true);
        topTable.top();
        healthTable = new Table();
        healthTable.add(healthLabel).left();

        topTable.add(levelLabel).left().expandX().pad(10);
        topTable.add(healthTable).left().expandX().pad(10);
        topTable.add(scoreLabel).center().expandX().pad(10);
        topTable.add(keyLabel).right().expandX().pad(10);

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

        stage.addActor(topTable);
        stage.addActor(bottomTable);
    }

    public void update(int levelNumber, int hp, int score, boolean hasKey){
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
    }
    
    public Stage getStage(){
        return stage;
    }

    public Viewport getViewport(){
        return viewport;
    }

    public void setShopButtonVisible(boolean visible) {
        shopButton.setVisible(visible);
    }

    public boolean isShopButtonVisible() {
        return shopButton.isVisible();
    }
}
