package de.tum.cit.fop.maze.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

/**
 * Dialog prompting the user to restart after graphics changes.
 */
public class RestartDialog extends Dialog {
    /** Log tag for restart events. */
    private static final String TAG = "RestartDialog";
    /** Stage to show the dialog on. */
    private final Stage stage;

    /**
     * Creates a restart dialog.
     *
     * @param skin UI skin
     * @param stage stage to show the dialog on
     */
    public RestartDialog(Skin skin, Stage stage) {
        super("", skin);
        this.stage = stage;
        setMovable(true);
        setResizable(false);
    }

    /**
     * Shows the dialog with a title and message.
     *
     * @param title dialog title
     * @param message dialog message
     */
    public void show(String title, String message) {
        this.getTitleLabel().setText(title);
        this.text(message);

        // Add buttons
        this.button("Restart Now", true);
        this.button("Cancel", false);

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
            Gdx.app.log(TAG, "Restarting application...");
            restartApplication();
        } else {
            Gdx.app.log(TAG, "Restart cancelled");
        }
    }

    /**
     * Exits the application to allow a restart.
     */
    private void restartApplication() {
        Gdx.app.log(TAG, "Exiting application for restart");
        Gdx.app.exit();

        // For automatic restart on desktop:
        // 1. Launch a separate process with ProcessBuilder
        // 2. Exit the current process
    }
}
