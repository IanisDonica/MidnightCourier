package de.tum.cit.fop.maze.screen;

import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import de.tum.cit.fop.maze.MazeRunnerGame;

/**
 * Game over screen shown after a pothole death.
 */
public class PotholeDeathScreen extends BaseEndScreen {
    /**
     * Creates a pothole death screen.
     *
     * @param game game instance
     */
    public PotholeDeathScreen(MazeRunnerGame game) {
        super(game, "Sewers.jpg");
    }

    @Override
    protected String getTitleText() {
        return "You died!";
    }

    @Override
    protected String getSubtitleText() {
        return "GlovoDriver 2004-2026";
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
        TextButton retryButton = createButton("Reincarnate", () -> {
            if (game.getCurrentLevelNumber() != 0) {
                game.goToGame(game.getCurrentLevelNumber());
            } else {
                game.goToEndless();
            }
        });
        TextButton mainMenuButton = createButton("Main Menu", game::goToMenu);
        TextButton rageQuitButton = createButton("Rage quit", () -> com.badlogic.gdx.Gdx.app.exit());

        buttons.add(retryButton).padRight(20);
        buttons.add(mainMenuButton).padRight(20);
        buttons.add(rageQuitButton);
    }

    @Override
    protected boolean isBackgroundTouchable() {
        return false;
    }

    @Override
    protected FadeStyle getFadeStyle() {
        return FadeStyle.LINEAR;
    }

    @Override
    protected boolean allowMenuBackground() {
        return false;
    }
}
