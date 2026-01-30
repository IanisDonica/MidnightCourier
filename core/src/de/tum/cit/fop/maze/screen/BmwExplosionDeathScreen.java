package de.tum.cit.fop.maze.screen;

import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import de.tum.cit.fop.maze.MazeRunnerGame;

/**
 * Game over screen shown after BMW explosion death.
 */
public class BmwExplosionDeathScreen extends BaseEndScreen {
    /**
     * Creates a BMW explosion death screen.
     *
     * @param game game instance
     */
    public BmwExplosionDeathScreen(MazeRunnerGame game) {
        super(game, "Assets_Map/Orthodox_crosses_in_Fort_Ross.jpg");
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
}
