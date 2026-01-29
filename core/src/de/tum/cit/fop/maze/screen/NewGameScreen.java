package de.tum.cit.fop.maze.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import de.tum.cit.fop.maze.MazeRunnerGame;
import de.tum.cit.fop.maze.system.AudioManager;

public class NewGameScreen implements Screen {
    private final MazeRunnerGame game;
    private final Stage stage;
    private final AudioManager audioManager;

    public NewGameScreen(MazeRunnerGame game){
        this.game = game;
        var camera = new OrthographicCamera();
        camera.zoom = 1.5f; // Set camera zoom for a closer view
        audioManager = game.getAudioManager();

        Viewport viewport = new FitViewport(1920, 1080); // Create a viewport with the camera
        stage = new Stage(viewport, game.getSpriteBatch()); // Create a stage for UI elements

        Table table = new Table(); // Create a table for layout
        table.setFillParent(true); // Make the table fill the stage
        stage.addActor(table); // Add the table to the stage

        table.add(new Label("New Game", game.getSkin(), "title")).padBottom(80).row();

        TextButton startCampaign = new TextButton("Start new campaign", game.getSkin());
        table.add(startCampaign).width(500).padBottom(15).row();

        TextButton startEndless = new TextButton("Start endless run", game.getSkin());
        table.add(startEndless).width(500).padBottom(15).row();

        startCampaign.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                audioManager.playSound("Click.wav", 1);
                game.startNewGameProgression();
                game.goToCutsceneScreen();
            }
        });
        startEndless.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                audioManager.playSound("Click.wav", 1);
                game.goToEndless();
            }
        });

    }


    @Override
    public void render(float delta) {
        if (!game.shouldRenderMenuBackground()) {
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT); // Clear the screen
        }
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
    }

    @Override
    public void show() {
        // Set the input processor so the stage can receive input events
        Gdx.input.setInputProcessor(stage);
        stage.addListener(game.getKeyHandler());
    }

    // The following methods are part of the Screen interface but are not used in this screen.
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
