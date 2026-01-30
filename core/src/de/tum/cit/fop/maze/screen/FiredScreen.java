package de.tum.cit.fop.maze.screen;

import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import de.tum.cit.fop.maze.MazeRunnerGame;

/**
 * Game over screen shown when the player runs out of time.
 */
public class FiredScreen extends BaseEndScreen {
    /**
     * Creates a new fired screen.
     *
     * @param game game instance
     */
    public FiredScreen(MazeRunnerGame game) {
        super(game, "fired.png");
    }

    @Override
    protected String getTitleText() {
        return "Fired";
    }

    @Override
    protected String getSubtitleText() {
        return "Be faster next time";
    }

    @Override
    protected String getSavedText() {
        return "(This score has not been saved)";
    }

    @Override
    protected String getStatsLine1(int minutes, int seconds) {
        return "You survived for: " + String.format("%d:%02d", minutes, seconds);
    }

    @Override
    protected String getStatsLine2() {
        return "You had a score of " + finalPoints + " points";
    }

    @Override
    protected void buildButtons(Table buttons) {
        TextButton retryButton = createButton("Try again", () -> {
            if (game.getCurrentLevelNumber() != 0) {
                game.goToGame(game.getCurrentLevelNumber());
            } else {
                game.goToEndless();
            }
        });
        TextButton mainMenuButton = createButton("Main Menu", game::goToMenu);
        TextButton rageQuitButton = createButton("Rage quit", () -> com.badlogic.gdx.Gdx.app.exit());

        buttons.add(retryButton).padRight(20);
        buttons.add(mainMenuButton);
        buttons.row();
        buttons.add(rageQuitButton).colspan(2).padTop(10);
    }
}
