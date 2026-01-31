package de.tum.cit.fop.maze.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import de.tum.cit.fop.maze.MazeRunnerGame;

/**
 * Dialog prompting the user to restart after graphics changes.
 */
public class RestartDialog extends Dialog {

    /** Log tag for restart events. */
    private static final String TAG = "MessageDialog";

    /** Stage to show the dialog on. */
    private final Stage stage;

    private final MazeRunnerGame game;

    /**
     * Creates a restart dialog.
     *
     * @param skin UI skin
     * @param stage stage to show the dialog on
     */
    public RestartDialog(Skin skin, Stage stage, MazeRunnerGame game) {
        super("", skin);
        this.game = game;
        this.stage = stage;

        // Set initial size - will be adjusted with pack()
        this.setSize(800, 600);
        setMovable(true);
        setResizable(false);
    }

    /**
     * Shows the dialog with a title and message.
     *
     * @param message dialog message
     */
    public void show(String message) {
        // Clear previous content
        this.getContentTable().clearChildren();
        this.getButtonTable().clearChildren();

        // Create wrapped label for message
        Label.LabelStyle style = getSkin().get(Label.LabelStyle.class);
        Label messageLabel = new Label(message, style);
        messageLabel.setWrap(true);

        // Add the message with width constraint (content table width minus padding)
        this.getContentTable().add(messageLabel).width(750f).pad(20f);

        // Add buttons
        this.button("Save and Restart", true);
        this.button("Cancel", false);
        this.defaults().pad(20);
        this.getButtonTable().pad(20);

        // Center on screen
        this.setPosition(
                (Gdx.graphics.getWidth() - this.getWidth()) / 2f,
                (Gdx.graphics.getHeight() - this.getHeight()) / 2f
        );

        // Show the dialog
        this.show(stage);
    }

    /**
     * Handles dialog result selection.
     *
     * @param object result object
     */
    @Override
    protected void result(Object object) {
        if (Boolean.TRUE.equals(object)) {
            game.getGraphicsManager().save();
            game.getAudioManager().playSound("Click.wav",1);
            Gdx.app.log(TAG, "Restarting application...");
            restartApplication();
        } else {
            game.getAudioManager().playSound("Click.wav",1);
            Gdx.app.log(TAG, "Restart cancelled");
        }
    }

    /**
     * Exits the application to allow a restart.
     */
    private void restartApplication() {
        Gdx.app.log(TAG, "Exiting application for restart");
        Gdx.app.exit();
    }
}
