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
import com.badlogic.gdx.utils.viewport.Viewport;
import de.tum.cit.fop.maze.MazeRunnerGame;
import de.tum.cit.fop.maze.system.AudioManager;
import de.tum.cit.fop.maze.system.SaveManager;

/**
 * Screen for selecting a save slot to continue the game.
 */
public class ContinueGameScreen implements Screen {
    /**
     * Game instance for navigation and resources.
     */
    private final MazeRunnerGame game;
    /**
     * Stage hosting UI elements.
     */
    private final Stage stage;
    /**
     * Audio manager for UI sounds.
     */
    private final AudioManager audioManager;
    private boolean file1, file2, file3;


    /**
     * Creates the continue game screen.
     *
     * @param game game instance
     */
    public ContinueGameScreen(MazeRunnerGame game) {
        this.game = game;
        var graphicsManager = game.getGraphicsManager();
        var camera = new OrthographicCamera();
        camera.zoom = 1f; // Set camera zoom for a closer view

        Viewport viewport = new FitViewport(graphicsManager.getWidth(), graphicsManager.getHeight(), camera);
        stage = new Stage(viewport, game.getSpriteBatch()); // Create a stage for UI elements

        audioManager = game.getAudioManager();

        Table table = new Table(); // Create a table for layout
        table.setFillParent(true); // Make the table fill the stage
        stage.addActor(table); // Add the table to the stage

        // Add a label as a title
        table.add(new Label("Select the save file:", game.getSkin(), "title")).padBottom(80).row();
        SaveSlotDialog saveSlotDialog = new SaveSlotDialog(game.getSkin(), stage, game);

        TextButton autosaveButton = new TextButton("Autosave", game.getSkin());
        table.add(autosaveButton).width(600).height(100).row();
        autosaveButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                audioManager.playSound("Click.wav", 1);
                if (SaveManager.getSaveInfo(0)) {
                    game.goToEndless(SaveManager.loadGame("autosave"));
                } else {
                    game.goToGame(SaveManager.loadGame("autosave"));
                }
            }
        });

        TextButton file1Button = new TextButton("Slot 1", game.getSkin());
        table.add(file1Button).width(600).height(100).row();
        file1Button.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                audioManager.playSound("Click.wav", 1);
                if (SaveManager.checkSaveExists("file1")) {
                    saveSlotDialog.showExists(1);
                } else saveSlotDialog.showNew(1);
            }
        });

        TextButton file2Button = new TextButton("Slot 2", game.getSkin());
        table.add(file2Button).width(600).height(100).row();
        file2Button.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                audioManager.playSound("Click.wav", 1);
                if (SaveManager.checkSaveExists("file2")) {
                    saveSlotDialog.showExists(2);
                } else saveSlotDialog.showNew(2);
            }
        });

        TextButton file3Button = new TextButton("Slot 3", game.getSkin());
        table.add(file3Button).width(600).height(100).row();
        table.row();
        file3Button.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                audioManager.playSound("Click.wav", 1);
                if (SaveManager.checkSaveExists("file3")) {
                    saveSlotDialog.showExists(3);
                } else saveSlotDialog.showNew(3);
            }
        });

        TextButton goToMenu = new TextButton("Go back to menu", game.getSkin());
        table.add(goToMenu).width(600).height(100);
        goToMenu.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                audioManager.playSound("Click.wav", 1);
                game.goToMenu();
            }
        });


    }

    /**
     * Renders the screen.
     *
     * @param delta frame delta time
     */
    @Override
    public void render(float delta) {
        if (!game.shouldRenderMenuBackground()) {
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT); // Clear the screen
        }
        stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f)); // Update the stage
        stage.draw(); // Draw the stage
    }

    /**
     * Updates viewport on resize.
     *
     * @param width  new width
     * @param height new height
     */
    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true); // Update the stage viewport on resize
    }

    /**
     * Disposes stage resources.
     */
    @Override
    public void dispose() {
        // Dispose of the stage when the screen is disposed
        stage.dispose();
    }

    /**
     * Sets input processing for the screen.
     */
    @Override
    public void show() {
        // Set the input processor so the stage can receive input events
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
