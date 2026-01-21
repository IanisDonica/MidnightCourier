package de.tum.cit.fop.maze.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import de.tum.cit.fop.maze.MazeRunnerGame;

import java.util.HashMap;
import java.util.Map;

public class SettingsControlsScreen implements Screen {
    private final Stage stage;

    public SettingsControlsScreen(MazeRunnerGame game){
        var camera = new OrthographicCamera();
        camera.zoom = 1.5f; // Set camera zoom for a closer view

        Viewport viewport = new FitViewport(1920, 1080);
        stage = new Stage(viewport, game.getSpriteBatch()); // Create a stage for UI elements

        Table table = new Table(); // Create a table for layout
        table.setFillParent(true); // Make the table fill the stage
        stage.addActor(table); // Add the table to the stage

        // Add a label as a title
        table.add(new Label("Settings: Controls", game.getSkin(), "title")).padBottom(80).row();


        TextButton applyAndReturn = new TextButton("Apply", game.getSkin());
        applyAndReturn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                //TODO: add here settings-saving.
                game.goToSettingsGameScreen(); // Change to the game screen when the button is pressed
            }
        });

        TextButton cancelAndReturn = new TextButton("Cancel", game.getSkin());
        cancelAndReturn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.goToSettingsScreen(); // Change to the game screen when the button is pressed
            }
        });

        Table keybindsTable = new Table();

        // i made it using map so you can access the required button.
        Map<String, Button> stringToButton = new HashMap<>();
        stringToButton.put("Up", new TextButton("W", game.getSkin()));
        stringToButton.put("Down", new TextButton("S", game.getSkin()));
        stringToButton.put("Left", new TextButton("A", game.getSkin()));
        stringToButton.put("Right", new TextButton("D", game.getSkin()));

        // here you can put the AddListener to the buttons
        // TODO make AddListener logic for every button.

        for (var button : stringToButton.keySet()){
            keybindsTable.add(new Label(button, game.getSkin()));
            keybindsTable.add(stringToButton.get(button)).row();
        }

        ScrollPane scrollPane = new ScrollPane(keybindsTable); // in case of many keybindings, we can scroll!

        table.add(scrollPane).row();
        table.add(applyAndReturn);
        table.add(cancelAndReturn);

    }


    @Override
    public void render(float delta) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT); // Clear the screen
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