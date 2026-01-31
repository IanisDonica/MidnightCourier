package de.tum.cit.fop.maze.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import de.tum.cit.fop.maze.MazeRunnerGame;
import de.tum.cit.fop.maze.system.AudioManager;
import de.tum.cit.fop.maze.system.ConfigManager;
import de.tum.cit.fop.maze.system.UiUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Screen that allows rebinding control keys.
 */
public class SettingsControlsScreen implements Screen {
    /** Game instance for navigation and resources. */
    private final MazeRunnerGame game;
    /** Stage hosting UI elements. */
    private final Stage stage;
    /** Configuration manager for key bindings. */
    private final ConfigManager configManager;
    /** Mapping of action names to their button widgets. */
    private final Map<String, TextButton> actionButtons = new HashMap<>();
    /** Audio manager for UI sounds. */
    private final AudioManager audioManager;
    /** Vignette texture overlay. */
    private final Texture vignetteTexture;
    /** Current action being rebound, if any. */
    private String rebindingAction = null;
    /** Button currently awaiting rebinding input. */
    private TextButton rebindingButton = null;

    /**
     * Creates the controls settings screen.
     *
     * @param game game instance
     */
    public SettingsControlsScreen(MazeRunnerGame game) {
        this.game = game;
        this.configManager = game.getConfigManager();
        audioManager = game.getAudioManager();
        var graphicsManager = game.getGraphicsManager();

        Viewport viewport = new FitViewport(graphicsManager.getWidth(), graphicsManager.getHeight());
        stage = new Stage(viewport, game.getSpriteBatch()); // Create a stage for UI elements

        vignetteTexture = UiUtils.buildVignetteTexture(512, 512, 0.9f);
        Image vignetteImage = new Image(vignetteTexture);
        vignetteImage.setFillParent(true);
        vignetteImage.setTouchable(Touchable.disabled);
        stage.addActor(vignetteImage);

        Table table = new Table(); // Create a table for layout
        table.pad(80);
        table.setFillParent(true); // Make the table fill the stage
        stage.addActor(table); // Add the table to the stage

        // Add a label as a title
        table.add(new Label("Settings: Controls", game.getSkin(), "title")).colspan(2).padBottom(15).row();

        Table keybindsTable = new Table();
        keybindsTable.pad(15);

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

            keybindsTable.add(actionLabel).left().padRight(15);
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
        horizontalGroup.space(15);
        table.add(horizontalGroup).colspan(2);
    }

    /**
     * Unbinds a conflicting action and updates its button label.
     *
     * @param action action name to unbind
     */
    private void unbindButton(String action) {
        configManager.setKeyBinding(action, com.badlogic.gdx.Input.Keys.UNKNOWN);
        TextButton conflictingButton = actionButtons.get(action);
        if (conflictingButton != null) {
            conflictingButton.setText(configManager.getKeyBindingName(action));
        }
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
     * @param width new width
     * @param height new height
     */
    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true); // Update the stage viewport on resize
    }

    /**
     * Disposes stage and textures.
     */
    @Override
    public void dispose() {
        // Dispose of the stage when the screen is disposed
        stage.dispose();
        vignetteTexture.dispose();
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
