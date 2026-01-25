package de.tum.cit.fop.maze.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import de.tum.cit.fop.maze.MazeRunnerGame;
import de.tum.cit.fop.maze.system.AudioManager;
import de.tum.cit.fop.maze.system.ConfigManager;

import java.util.HashMap;
import java.util.Map;

public class SettingsControlsScreen implements Screen {
    private final MazeRunnerGame game;
    private final Stage stage;
    private final ConfigManager configManager;
    private String rebindingAction = null;
    private TextButton rebindingButton = null;
    private final Map<String, TextButton> actionButtons = new HashMap<>();
    private final AudioManager audioManager;

    public SettingsControlsScreen(MazeRunnerGame game) {
        this.game = game;
        this.configManager = game.getConfigManager();
        audioManager = game.getAudioManager();

        var camera = new OrthographicCamera();
        camera.zoom = 1.5f; // Set camera zoom for a closer view

        Viewport viewport = new FitViewport(1920, 1080);
        stage = new Stage(viewport, game.getSpriteBatch()); // Create a stage for UI elements

        Table table = new Table(); // Create a table for layout
        table.setFillParent(true); // Make the table fill the stage
        stage.addActor(table); // Add the table to the stage

        // Add a label as a title
        table.add(new Label("Settings: Controls", game.getSkin(), "title")).colspan(2).padBottom(80).row();

        Table keybindsTable = new Table();
        keybindsTable.pad(20);

        String[] actions = {"up", "down", "left", "right", "sprint", "openShop", "pause", "zoomIn", "zoomOut", "moreFog", "lessFog", "noire"};

        for (String action : actions) {
            Label actionLabel = new Label(action.toUpperCase(), game.getSkin());
            final String currentAction = action;
            TextButton keyButton = new TextButton(configManager.getKeyBindingName(action), game.getSkin());
            actionButtons.put(action, keyButton);

            keyButton.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    audioManager.playSound("Click.wav", 1);
                    if (rebindingAction != null && rebindingButton != null) {
                        rebindingButton.setText(configManager.getKeyBindingName(rebindingAction));
                    }
                    rebindingAction = currentAction;
                    rebindingButton = keyButton;
                    keyButton.setText("Press any key...");
                }
            });

            keybindsTable.add(actionLabel).left().padRight(20);
            keybindsTable.add(keyButton).width(300).padBottom(10).row();
        }


        stage.addListener(new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                if (rebindingAction != null) {
                    audioManager.playSound("Click.wav", 1);
                    // Check for conflicts
                    String conflictingAction = configManager.getActionForKey(keycode);
                    if (conflictingAction != null && !conflictingAction.equals(rebindingAction)) {
                        unbindButton(conflictingAction);
                    }

                    // Rebind current button
                    configManager.setKeyBinding(rebindingAction, keycode);
                    if (rebindingButton != null) {
                        rebindingButton.setText(configManager.getKeyBindingName(rebindingAction));
                    }

                    // Reset action and buttons to null for the next rebinding
                    rebindingAction = null;
                    rebindingButton = null;
                    return true;
                }
                return false;
            }
        });

        ScrollPane scrollPane = new ScrollPane(keybindsTable, game.getSkin());
        scrollPane.setFadeScrollBars(false);
        scrollPane.setScrollingDisabled(true, false); // Disable horizontal scrolling, enable vertical
        stage.setScrollFocus(scrollPane);
        table.add(scrollPane).size(800, 600).expand().fill().colspan(2).row();

        TextButton backButton = new TextButton("Back", game.getSkin());
        backButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                audioManager.playSound("Click.wav", 1);
                configManager.saveKeyBindings();
                game.goToSettingsScreen();
            }
        });

        TextButton saveButton = new TextButton("Save", game.getSkin());
        saveButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                audioManager.playSound("Click.wav", 1);
                configManager.saveKeyBindings();
            }
        });

        HorizontalGroup horizontalGroup = new HorizontalGroup();
        horizontalGroup.addActor(backButton);
        horizontalGroup.addActor(saveButton);
        table.add(horizontalGroup).colspan(2).padTop(20);
    }

    private void unbindButton(String action) {
        configManager.setKeyBinding(action, com.badlogic.gdx.Input.Keys.UNKNOWN);
        TextButton conflictingButton = actionButtons.get(action);
        if (conflictingButton != null) {
            conflictingButton.setText(configManager.getKeyBindingName(action));
        }
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
