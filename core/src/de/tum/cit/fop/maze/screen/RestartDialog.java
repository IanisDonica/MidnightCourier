package de.tum.cit.fop.maze.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

public class RestartDialog extends Dialog {
    private static final String TAG = "RestartDialog";
    private final Stage stage;

    public RestartDialog(Skin skin, Stage stage) {
        super("", skin);
        this.stage = stage;
        setMovable(true);
        setResizable(false);
    }

    public void show(String title, String message) {
        this.getTitleLabel().setText(title);
        this.text(message);

        // Add buttons
        this.button("Restart Now", true);
        this.button("Cancel", false);

        // Show the dialog
        this.show(stage);
    }

    @Override
    protected void result(Object object) {
        if (Boolean.TRUE.equals(object)) {
            Gdx.app.log(TAG, "Restarting application...");
            restartApplication();
        } else {
            Gdx.app.log(TAG, "Restart cancelled");
        }
    }

    private void restartApplication() {
        Gdx.app.log(TAG, "Exiting application for restart");
        Gdx.app.exit();

        // For automatic restart on desktop:
        // 1. Launch a separate process with ProcessBuilder
        // 2. Exit the current process
    }
}
