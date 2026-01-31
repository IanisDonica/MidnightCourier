package de.tum.cit.fop.maze.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import de.tum.cit.fop.maze.MazeRunnerGame;
import de.tum.cit.fop.maze.system.AudioManager;
import de.tum.cit.fop.maze.system.UiUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.time.format.TextStyle;
import java.util.Arrays;
import java.util.Locale;

/**
 * Screen that displays local highscores and summary stats.
 */
public class HighscoreScreen implements Screen {
    /** Local highscore data path. */
    private static final String FILE_PATH = "assets/data/highscore.json";
    /** Stage hosting UI elements. */
    private final Stage stage;
    /** Game instance for navigation and resources. */
    private final MazeRunnerGame game;
    /** Root table for layout. */
    private final Table table;
    /** Back button to return to menu. */
    private final TextButton backButton;
    /** Audio manager for UI sounds. */
    private final AudioManager audioManager;
    /** Vignette texture overlay. */
    private final Texture vignetteTexture;
    /** Background texture for the screen. */
    private final Texture backgroundTexture;

    /**
     * Creates a new highscore screen.
     *
     * @param game game instance
     */
    public HighscoreScreen(MazeRunnerGame game) {
        this.game = game;
        var graphicsManager = game.getGraphicsManager();
        Viewport viewport = new FitViewport(graphicsManager.getWidth(), graphicsManager.getHeight());
        stage = new Stage(viewport, game.getSpriteBatch());
        audioManager = game.getAudioManager();

        backgroundTexture = new Texture(Gdx.files.internal("HighscoreBackground.jpg"));
        Image backgroundImage = new Image(backgroundTexture);
        backgroundImage.setFillParent(true);
        stage.addActor(backgroundImage);

        vignetteTexture = UiUtils.buildVignetteTexture(512, 512, 0.9f);
        Image vignetteImage = new Image(vignetteTexture);
        vignetteImage.setFillParent(true);
        vignetteImage.setTouchable(Touchable.disabled);
        stage.addActor(vignetteImage);

        table = new Table();
        table.setFillParent(true);
        table.top();
        table.padTop(20);
        stage.addActor(table);

        backButton = new TextButton("<", game.getSkin());
        backButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                audioManager.playSound("Click.wav", 1);
                game.goToMenu();
            }
        });
    }

    /**
     * Renders the highscore screen.
     *
     * @param delta frame delta time
     */
    @Override
    public void render(float delta) {
        if (!game.shouldRenderMenuBackground()) {
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        }
        stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
        stage.draw();
    }

    /**
     * Updates the viewport on resize.
     *
     * @param width new width
     * @param height new height
     */
    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    /**
     * Disposes stage and textures.
     */
    @Override
    public void dispose() {
        stage.dispose();
        vignetteTexture.dispose();
    }

    /**
     * Sets input processing and rebuilds the table.
     */
    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
        stage.addListener(game.getKeyHandler());
        rebuildTable();
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }


    // Build only once, as opposed to doing it every frame.
    /**
     * Rebuilds the highscore table layout from saved data.
     */
    private void rebuildTable() {
        table.clear();

        Table header = new Table();
        header.add(backButton).size(60, 60).left().padLeft(50);
        Label titleLabel = new Label("Highscores", game.getSkin(), "title");
        header.add(titleLabel).expandX().center().padTop(30);
        header.add().size(60, 60);
        table.add(header).expandX().fillX().padBottom(40).row();

        int[] bestScoreByLevel = new int[6];
        Arrays.fill(bestScoreByLevel, -1);

        int totalScores = 0;
        String[] bestDateByLevel = new String[6];
        int[] bestHpByLevel = new int[6];
        Arrays.fill(bestDateByLevel, "N/A");
        Arrays.fill(bestHpByLevel, -1);
        String firstScoreDateText = "N/A";
        String mostRecentScoreDateText = "N/A";

        JsonValue rootArray = new JsonReader().parse(Gdx.files.local(FILE_PATH).readString("UTF-8"));
        JsonValue firstEntry = rootArray.child;
        for (JsonValue entry = rootArray.child; entry != null; entry = entry.next) {
            totalScores++;
            int level = entry.getInt("level", -1);
            int score = entry.getInt("score", 0);
            if (level >= 0 && level <= 5 && score > bestScoreByLevel[level]) {
                bestScoreByLevel[level] = score;
                JsonValue dateValue = entry.get("dateTime");
                if (dateValue != null && !dateValue.isNull()) {
                    bestDateByLevel[level] = formatDateTime(dateValue.asString());
                } else {
                    bestDateByLevel[level] = "N/A";
                }
                bestHpByLevel[level] = entry.getInt("playerHp", -1);
            }
        }
        if (firstEntry != null) {
            JsonValue firstDateValue = firstEntry.get("dateTime");
            if (firstDateValue != null && !firstDateValue.isNull()) {
                firstScoreDateText = formatDateTime(firstDateValue.asString());
            }
            JsonValue lastEntry = rootArray.child;
            while (lastEntry != null && lastEntry.next != null) {
                lastEntry = lastEntry.next;
            }
            if (lastEntry != null) {
                JsonValue lastDateValue = lastEntry.get("dateTime");
                if (lastDateValue != null && !lastDateValue.isNull()) {
                    mostRecentScoreDateText = formatDateTime(lastDateValue.asString());
                }
            }
        }

        Table scoresTable = new Table();
        for (int i = 0; i <= 5; i++) {
            String scoreText = bestScoreByLevel[i] < 0 ? "N/A" : String.valueOf(bestScoreByLevel[i]);
            String completedText = bestDateByLevel[i];
            String finishedText = bestHpByLevel[i] >= 0 ? bestHpByLevel[i] + " HP left" : "N/A";

            Table scoreBox = new Table();
            scoreBox.setBackground(game.getSkin().getDrawable("whiteBlack"));
            String levelName = (i == 0) ? "Endless mode" : ("Level " + i);
            Label levelLabel = new Label(levelName, game.getSkin());
            Label scoreLabel = new Label(scoreText, game.getSkin());
            scoreBox.add(levelLabel).padTop(10).row();
            scoreBox.add(scoreLabel).padTop(6).padBottom(10).row();

            Table completedBox = new Table();
            completedBox.setBackground(game.getSkin().getDrawable("whiteBlack"));
            Label completedLabel = new Label("Completed on", game.getSkin());
            Label completedValueLabel = new Label(completedText, game.getSkin());
            completedBox.add(completedLabel).padTop(10).row();
            completedBox.add(completedValueLabel).padTop(6).padBottom(10).row();

            scoresTable.add(scoreBox).width(240).height(95).pad(10);
            if (i == 0) {
                scoresTable.add(completedBox).width(400).height(95).pad(10);
            } else {
                scoresTable.add(completedBox).width(400).height(95).pad(10);
            }
            if (i == 0) {
                Table spacerBox = new Table();
                scoresTable.add(spacerBox).width(0).height(95).pad(0);
            } else {
                Table finishedBox = new Table();
                finishedBox.setBackground(game.getSkin().getDrawable("whiteBlack"));
                Label finishedLabel = new Label("Finished with", game.getSkin());
                Label finishedValueLabel = new Label(finishedText, game.getSkin());
                finishedBox.add(finishedLabel).padTop(10).row();
                finishedBox.add(finishedValueLabel).padTop(6).padBottom(10).row();
                scoresTable.add(finishedBox).width(260).height(95).pad(10);
            }
            scoresTable.row();
        }


        Table globalBox = new Table();
        globalBox.setBackground(game.getSkin().getDrawable("whiteBlack"));
        Label globalLabel = new Label("You can also check out top scores from other players on [#0000EE]transprut.solutions[]", game.getSkin());
        globalLabel.setWrap(true);
        globalLabel.setAlignment(Align.center);
        globalLabel.getStyle().font.getData().markupEnabled = true;
        TextButton globalButton = new TextButton("Open in your browser", game.getSkin());
        globalButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Gdx.net.openURI("https://transprut.solutions");
            }
        });
        globalBox.add(globalLabel).width(360).padTop(10).padLeft(20).padRight(20).center().row();
        globalBox.add(globalButton).padTop(10).padBottom(20).center().row();

        Table datesBox = new Table();
        datesBox.setBackground(game.getSkin().getDrawable("whiteBlack"));
        Label firstDateLabel = new Label("First score on:", game.getSkin());
        Label firstDateValueLabel = new Label(firstScoreDateText, game.getSkin());
        Label lastDateLabel = new Label("Most recent score on:", game.getSkin());
        Label lastDateValueLabel = new Label(mostRecentScoreDateText, game.getSkin());
        datesBox.add(firstDateLabel).padTop(20).padLeft(16).padRight(16).center().row();
        datesBox.add(firstDateValueLabel).padTop(6).center().row();
        datesBox.add(lastDateLabel).padTop(20).padLeft(16).padRight(16).center().row();
        datesBox.add(lastDateValueLabel).padTop(6).padBottom(20).center().row();

        Table contentRow = new Table();
        contentRow.add(scoresTable).left().padLeft(50);
        Table rightColumn = new Table();
        rightColumn.add(globalBox).width(420).height(255).padBottom(20).padTop(0).row();
        rightColumn.add(datesBox).width(420).height(200).row();
        Table totalsBox = new Table();
        totalsBox.setBackground(game.getSkin().getDrawable("whiteBlack"));
        Label totalsLabel = new Label("Scores logged:", game.getSkin());
        Label totalsValueLabel = new Label(String.valueOf(totalScores), game.getSkin());
        totalsBox.add(totalsLabel).padTop(12).padLeft(0).padRight(16).center().row();
        totalsBox.add(totalsValueLabel).padTop(6).padBottom(12).center().row();
        rightColumn.add(totalsBox).width(420).height(140).padTop(20).row();
        contentRow.add(rightColumn).padLeft(20).top().padTop(20);
        table.add(contentRow).expandX().fillX().padBottom(30).row();

    }

    /**
     * Formats a raw ISO date-time string for display.
     *
     * @param rawDateTime raw date-time string
     * @return formatted date string or "N/A"
     */
    private String formatDateTime(String rawDateTime) {
        if (rawDateTime == null) {
            return "N/A";
        }
        try {
            LocalDateTime dateTime = LocalDateTime.parse(rawDateTime);
            //int monthNumber = dateTime.getMonthValue();
            int day = dateTime.getDayOfMonth();
            String monthAbbrev = dateTime.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
            int hour24 = dateTime.getHour();
            int hour12 = hour24 % 12;
            if (hour12 == 0) {
                hour12 = 12;
            }
            String ampm = hour24 < 12 ? "am" : "pm";
            return day + daySuffix(day) + " " + monthAbbrev + " " + hour12 + ampm;
        } catch (DateTimeParseException e) {
            return "N/A";
        }
    }

    /**
     * Returns the day-of-month suffix.
     *
     * @param day day of month
     * @return suffix string
     */
    private String daySuffix(int day) {
        if (day >= 11 && day <= 13) {
            return "th";
        }
        return switch (day % 10) {
            case 1 -> "st";
            case 2 -> "nd";
            case 3 -> "rd";
            default -> "th";
        };
    }

}
