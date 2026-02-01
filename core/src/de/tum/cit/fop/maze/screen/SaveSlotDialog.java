package de.tum.cit.fop.maze.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Align;
import de.tum.cit.fop.maze.MazeRunnerGame;
import de.tum.cit.fop.maze.system.SaveManager;

/**
 * Dialog for save slot management - allows user to load or overwrite save data.
 */
public class SaveSlotDialog extends Dialog {

    /**
     * Log tag for save slot events.
     */
    private static final String TAG = "SaveSlotDialog";

    /**
     * Stage to show the dialog on.
     */
    private final Stage stage;

    private final MazeRunnerGame game;
    private int slotName;

    /**
     * Creates a save slot dialog.
     *
     * @param skin  UI skin
     * @param stage stage to show the dialog on
     * @param game  game instance
     */
    public SaveSlotDialog(Skin skin, Stage stage, MazeRunnerGame game) {
        super("", skin);
        this.game = game;
        this.stage = stage;

        // Set initial size - will be adjusted with pack()
        this.setSize(800, 600);
        setMovable(true);
        setResizable(false);
    }

    /**
     * Shows the dialog asking user to load or overwrite save slot.
     *
     * @param slotName name of the save slot (e.g., "Slot 1")
     */
    public void showExists(int slotName) {
        this.slotName = slotName;
        // Clear previous content
        this.getContentTable().clearChildren();
        this.getButtonTable().clearChildren();

        // Create the message label
        String message = "Save slot " + slotName + " already contains data.\n\n" + "Would you like to load the existing save\n" + "or overwrite it with new data?";

        Label.LabelStyle style = getSkin().get(Label.LabelStyle.class);
        Label messageLabel = new Label(message, style);
        messageLabel.setWrap(true);

        // Add message with padding
        this.getContentTable().add(messageLabel).width(700f).pad(20);

        // Add buttons with callbacks
        this.button(" Load Save ", 1).align(Align.left).pad(10);
        this.button(" Overwrite ", 2).align(Align.center).pad(10);
        this.button(" Cancel ", 3).align(Align.right).pad(10);

        // Pack the dialog to fit content
        this.pack();

        // Ensure dialog doesn't exceed screen bounds
        float maxWidth = Gdx.graphics.getWidth() * 0.9f;
        float maxHeight = Gdx.graphics.getHeight() * 0.9f;

        if (this.getWidth() > maxWidth) {
            this.setWidth(maxWidth);
        }
        if (this.getHeight() > maxHeight) {
            this.setHeight(maxHeight);
        }

        // Center on screen
        this.setPosition((Gdx.graphics.getWidth() - this.getWidth()) / 2f, (Gdx.graphics.getHeight() - this.getHeight()) / 2f);

        // Show the dialog
        this.show(stage);
    }

    public void showNew(int slotName) {
        this.slotName = slotName;
        // Clear previous content
        this.getContentTable().clearChildren();
        this.getButtonTable().clearChildren();

        // Create the message label
        String message = "Would you like to save game to slot " + slotName + "?";

        Label.LabelStyle style = getSkin().get(Label.LabelStyle.class);
        Label messageLabel = new Label(message, style);
        messageLabel.setWrap(true);

        // Add the message with padding
        this.getContentTable().add(messageLabel).width(700f).pad(20);

        // Add buttons with callbacks
        this.button(" Save ", 2).align(Align.left).pad(10);
        this.button(" Cancel ", 3).align(Align.right).pad(10);

        // Pack the dialog to fit content
        this.pack();

        // Ensure dialog doesn't exceed screen bounds
        float maxWidth = Gdx.graphics.getWidth() * 0.9f;
        float maxHeight = Gdx.graphics.getHeight() * 0.9f;

        if (this.getWidth() > maxWidth) {
            this.setWidth(maxWidth);
        }
        if (this.getHeight() > maxHeight) {
            this.setHeight(maxHeight);
        }

        // Center on screen
        this.setPosition((Gdx.graphics.getWidth() - this.getWidth()) / 2f, (Gdx.graphics.getHeight() - this.getHeight()) / 2f);

        // Show the dialog
        this.show(stage);
    }

    /**
     * Handles dialog result selection.
     *
     * @param object result object (1=Load, 2=Overwrite, 3=Cancel)
     */
    @Override
    protected void result(Object object) {
        int choice = (Integer) object;
        switch (choice) {
            case 1:
                Gdx.app.log(TAG, "Loading save from slot");
                game.getAudioManager().playSound("Click.wav", 1);
                if (SaveManager.getSaveInfo(slotName)) {
                    game.goToEndless(SaveManager.loadGame("file" + slotName));
                } else {
                    game.goToGame(SaveManager.loadGame("file" + slotName));
                }
                break;
            case 2:
                Gdx.app.log(TAG, "Overwriting save slot");
                game.getAudioManager().playSound("Click.wav", 1);
                if (game.getSelectedScreen()) {
                    if (game.getSurvivalScreen() == null) {
                        System.err.println("Game state is null");
                        break;
                    }
                    SaveManager.saveGame("file" + slotName, game.getSurvivalScreen().getGameState());
                    SaveManager.saveInfo(true, slotName);
                } else {
                    if (game.getGameScreen() == null) {
                        System.err.println("Game state is null");
                        break;
                    }
                    SaveManager.saveGame("file" + slotName, game.getGameScreen().getGameState());
                    SaveManager.saveInfo(false, slotName);
                }
                break;
            case 3:
                Gdx.app.log(TAG, "Save slot dialog cancelled");
                game.getAudioManager().playSound("Click.wav", 1);
                break;
        }
    }

}
