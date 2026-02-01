package de.tum.cit.fop.maze.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import de.tum.cit.fop.maze.MazeRunnerGame;

public class MessageDialog extends Dialog {
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
    public MessageDialog(Skin skin, Stage stage, MazeRunnerGame game) {
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

        // Create a wrapped label for the message
        Label.LabelStyle style = getSkin().get(Label.LabelStyle.class);
        Label messageLabel = new Label(message, style);
        messageLabel.setWrap(true);

        // Add the message with a width constraint (content table width minus padding)
        this.getContentTable().add(messageLabel).width(750f).pad(20f);

        // Add buttons
        this.button("OK", false);
        this.defaults().pad(20);
        this.getButtonTable().pad(20);

        // Center on screen
        this.setPosition(
                (Gdx.graphics.getWidth() - this.getWidth()) / 2f,
                (Gdx.graphics.getHeight() - this.getHeight()) / 2f
        );

        // Show the dialog
        this.show(stage, Actions.alpha(1f));
    }

    /**
     * Handles dialog result selection.
     *
     * @param object result object
     */
    @Override
    protected void result(Object object) {
        this.hide(Actions.alpha(1f));
    }

}
