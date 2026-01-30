package de.tum.cit.fop.maze.screen;

import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import de.tum.cit.fop.maze.MazeRunnerGame;

/**
 * Victory screen shown after completing a level.
 */
public class VictoryScreen extends BaseEndScreen {
    /** Points rewarded for daily delivery. */
    private static final int DAILY_REWARD = 500;

    /**
     * Creates a victory screen.
     *
     * @param game game instance
     */
    public VictoryScreen(MazeRunnerGame game) {
        super(game, "VictoryScreen.jpg");
    }

    @Override
    protected String getTitleText() {
        return "Delivery Sucessfull";
    }

    @Override
    protected String getSubtitleText() {
        return "(you earned " + DAILY_REWARD + " lei, go to a shop to get upgrades)";
    }

    @Override
    protected String getSavedText() {
        return "This score has been saved";
    }

    @Override
    protected String getStatsLine1(int minutes, int seconds) {
        return "You have completed the deliver in " + String.format("%d:%02d", minutes, seconds);
    }

    @Override
    protected String getStatsLine2() {
        return "You have the completed the level with " + finalPoints + " score";
    }

    @Override
    protected void buildButtons(Table buttons) {
        int currentLevel = game.getCurrentLevelNumber();
        boolean isLastLevel = currentLevel >= 5;
        TextButton nextLevelButton = createButton(isLastLevel ? "Go on your vacation" : "Next Level", () -> {
            if (isLastLevel) {
                game.goToSecondCutsceneScreen(6);
                return;
            }
            if (currentLevel + 1 <= 5) {
                game.goToSecondCutsceneScreen(currentLevel + 1);
                return;
            }
            game.goToGame(currentLevel);
        });
        TextButton mainMenuButton = createButton("Main Menu", game::goToMenu);

        buttons.add(nextLevelButton).padRight(20);
        buttons.add(mainMenuButton);
    }

    @Override
    protected float getContentPadding() {
        return 40f;
    }

    @Override
    protected float getContentWidth() {
        return 980f;
    }

    @Override
    protected float getContentBoxWidth() {
        return 1060f;
    }

    @Override
    protected float getStatsWidth() {
        return 900f;
    }

    @Override
    protected float getSubtitleFontScale() {
        return 1.1f;
    }
}
