package de.tum.cit.fop.maze.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import de.tum.cit.fop.maze.MazeRunnerGame;

import java.util.ArrayDeque;
import java.util.Queue;

// Ive decided to make it a seperate screen so that achivmets can appear on any screen (and during screen transition)
// Originally I added it in HUD, but the issue is that it wouldn't show up when the player would go into the victoryScreen for example
// instead of writing code to manage all screens and posible combinations, its easier to just another screen just for the pop up
/**
 * Overlay screen that shows queued achievement popups.
 */
public class AchievementPopupScreen {
    /** Popup rise animation duration. */
    private static final float POPUP_RISE_DURATION = 0.6f;
    /** Time a popup stays visible. */
    private static final float POPUP_HOLD_DURATION = 5.0f;
    /** Popup fade-out duration. */
    private static final float POPUP_FADE_OUT_DURATION = 0.4f;
    /** Popup fade-in duration. */
    private static final float POPUP_FADE_IN_DURATION = 0.2f;
    /** Margin to screen edge. */
    private static final float POPUP_MARGIN = 20f;
    /** Target Y position for popup. */
    private static final float POPUP_END_Y = 40f;

    /** Game instance providing skin. */
    private final MazeRunnerGame game;
    /** Stage for popup rendering. */
    private final Stage stage;
    /** Viewport for screen-aligned overlay. */
    private final Viewport viewport;
    /** Batch used by the stage. */
    private final SpriteBatch batch;
    // Queue lets us show multiple unlocks one after another without overlap
    /** Pending achievement names to display. */
    private final Queue<String> pending = new ArrayDeque<>();
    /** Whether a popup is currently being shown. */
    private boolean showing = false;

    /**
     * Creates an achievement popup overlay.
     *
     * @param game game instance
     */
    public AchievementPopupScreen(MazeRunnerGame game) {
        this.game = game;
        // ScreenViewport makes the overlay pixel-aligned to the window, independent of game camera zoom.
        this.viewport = new ScreenViewport();
        this.batch = new SpriteBatch();
        this.stage = new Stage(viewport, batch);
        this.viewport.update(game.getGraphicsManager().getWidth(), game.getGraphicsManager().getHeight(), true);
    }

    /**
     * Advances popup animations.
     *
     * @param delta frame delta time
     */
    public void act(float delta) {
        stage.act(delta);
    }

    /**
     * Draws the popup stage.
     */
    public void draw() {
        stage.draw();
    }

    /**
     * Updates viewport on resize.
     *
     * @param width new width
     * @param height new height
     */
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    /**
     * Disposes stage and batch resources.
     */
    public void dispose() {
        stage.dispose();
        batch.dispose();
    }

    /**
     * Enqueues an achievement popup.
     *
     * @param achievementName achievement name to display
     */
    public void showPopup(String achievementName) {
        if (achievementName == null || achievementName.isEmpty()) {
            return;
        }
        pending.add(achievementName);
        // If nothing is currently animating, start immediately; otherwise it'll wait it's turn
        if (!showing) {
            showNext();
        }
    }

    /**
     * Displays the next pending popup, if any.
     */
    private void showNext() {
        String name = pending.poll();
        if (name == null) {
            showing = false;
            return;
        }
        showing = true;
        Table popup = new Table(game.getSkin());
        popup.setBackground(game.getSkin().getDrawable("cell"));
        popup.pad(16);

        Label title = new Label("Achivment unlocked", game.getSkin());
        title.setFontScale(1.2f);
        title.setAlignment(Align.left);
        Label nameLabel = new Label(name, game.getSkin());
        nameLabel.setAlignment(Align.left);

        popup.add(title).left().row();
        popup.add(nameLabel).left().row();
        popup.pack();

        float worldWidth = viewport.getWorldWidth();
        float startX = Math.max(POPUP_MARGIN, worldWidth - popup.getWidth() - POPUP_MARGIN); // Bottom right

        // Start offscreen and go up (kinda like a Steam Achivment)
        float startY = -popup.getHeight();
        popup.setPosition(startX, startY);
        popup.getColor().a = 0f;

        // Sequence keeps the animation self-contained; once done, it triggers the next queued popup.
        popup.addAction(Actions.sequence(
                Actions.parallel(
                        Actions.fadeIn(POPUP_FADE_IN_DURATION),
                        Actions.moveTo(startX, POPUP_END_Y, POPUP_RISE_DURATION)
                ),
                Actions.delay(POPUP_HOLD_DURATION),
                Actions.fadeOut(POPUP_FADE_OUT_DURATION),
                Actions.run(() -> {
                    popup.remove();
                    showNext();
                })
        ));
        stage.addActor(popup);
    }
}
