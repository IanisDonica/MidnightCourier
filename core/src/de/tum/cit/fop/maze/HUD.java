package de.tum.cit.fop.maze;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class HUD {
    //private MazeRunnerGame game;
    private Stage stage;
    private Viewport viewport;

    private Label scoreLabel;
    private Label healthLabel;
    private Label keyLabel;

    public HUD(MazeRunnerGame game){
        //this.game = game;
        viewport = new StretchViewport(1920, 1080);
        stage = new Stage(viewport);

        scoreLabel = new Label("Score: ", game.getSkin());
        scoreLabel.setFontScale(2.0f);
        healthLabel = new Label("Health: ", game.getSkin());
        healthLabel.setFontScale(2.0f);
        keyLabel = new Label("Key: no", game.getSkin());
        keyLabel.setFontScale(2.0f);

        Table table = new Table();
        table.setFillParent(true);
        table.top();
        table.add(healthLabel).left().expandX().pad(10);
        table.add(scoreLabel).center().expandX().pad(10);
        table.add(keyLabel).right().expandX().pad(10);

        stage.addActor(table);
    }

    public void update(int hp, double score, boolean hasKey){
        scoreLabel.setText("Score: " + score);
        healthLabel.setText("Health: " + hp);
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
}
