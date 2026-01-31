package de.tum.cit.fop.maze.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import de.tum.cit.fop.maze.MazeRunnerGame;
import de.tum.cit.fop.maze.system.AudioManager;

/**
 * Base screen for victory/death screens with shared layout and fade effects.
 */
public abstract class BaseEndScreen implements Screen {
    /** Supported fade styles. */
    protected enum FadeStyle {
        LOG_OVERLAY,
        LINEAR
    }

    /** Game instance for navigation and resources. */
    protected final MazeRunnerGame game;
    /** Stage hosting UI elements. */
    protected final Stage stage;
    /** Audio manager for UI sounds. */
    protected final AudioManager audioManager;
    /** Overlay image used for fade in. */
    protected final Image fadeOverlay;
    /** Background texture. */
    protected final Texture backgroundTexture;
    /** Background image. */
    protected final Image backgroundImage;
    /** Group containing buttons for fade-in control. */
    protected final WidgetGroup buttonGroup;
    /** Survival time at end. */
    protected final float survivedSeconds;
    /** Final points at end. */
    protected final int finalPoints;
    /** Timer used for fade effects. */
    protected float fadeTimer = 0f;
    /** Fade duration for overlay. */
    protected static final float FADE_DURATION = 2f;
    /** Fade duration for button group. */
    protected static final float BUTTON_FADE_DURATION = 1.0f;
    /** Fade texture for disposal. */
    private final Texture fadeTexture;

    /**
     * Creates a base end screen.
     *
     * @param game game instance
     * @param backgroundPath path to background texture
     */
    protected BaseEndScreen(MazeRunnerGame game, String backgroundPath) {
        this.game = game;
        // Derive end-of-run stats from the active game/survival screen if available
        if (game.getGameScreen() != null && game.getGameScreen().pointManager != null) {
            survivedSeconds = game.getGameScreen().pointManager.getElapsedTime();
            finalPoints = game.getGameScreen().pointManager.getPoints();
        } else if (game.getSurvivalScreen() != null && game.getSurvivalScreen().pointManager != null) {
            survivedSeconds = game.getSurvivalScreen().pointManager.getElapsedTime();
            finalPoints = game.getSurvivalScreen().pointManager.getPoints();
        } else {
            survivedSeconds = 0f;
            finalPoints = 0;
        }
        audioManager = game.getAudioManager();
        var graphicsManager = game.getGraphicsManager();

        Viewport viewport = new FitViewport(graphicsManager.getWidth(), graphicsManager.getHeight());
        stage = new Stage(viewport, game.getSpriteBatch());
        backgroundTexture = new Texture(Gdx.files.internal(backgroundPath));
        backgroundImage = new Image(backgroundTexture);
        backgroundImage.setFillParent(true);
        // Start fully transparent, fade-in will be handled in render()
        backgroundImage.setColor(1f, 1f, 1f, 0f);
        if (!isBackgroundTouchable()) {
            backgroundImage.setTouchable(Touchable.disabled);
        }
        stage.addActor(backgroundImage);

        // Build the UI layout: title, optional subtitle, stats, optional saved text, and buttons
        Table table = new Table();
        table.setFillParent(true);
        table.top();
        Table content = new Table();
        content.defaults().center();

        Label titleLabel = new Label(getTitleText(), game.getSkin(), "title");
        titleLabel.setAlignment(com.badlogic.gdx.utils.Align.center);
        content.add(titleLabel).center().padBottom(getTitlePadBottom()).row();

        String subtitle = getSubtitleText();
        if (subtitle != null && !subtitle.isEmpty()) {
            Label subtitleLabel = new Label(subtitle, game.getSkin());
            subtitleLabel.setFontScale(getSubtitleFontScale());
            subtitleLabel.setAlignment(com.badlogic.gdx.utils.Align.center);
            content.add(subtitleLabel).center().padBottom(getSubtitlePadBottom()).row();
        }

        Table statsTable = new Table();
        statsTable.setBackground(game.getSkin().getDrawable(getStatsBackgroundDrawableName()));
        int totalSeconds = Math.max(0, (int) survivedSeconds);
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        Label survivedLabel = new Label(getStatsLine1(minutes, seconds), game.getSkin());
        Label pointsLabel = new Label(getStatsLine2(), game.getSkin());
        statsTable.add(survivedLabel).pad(10).row();
        statsTable.add(pointsLabel).pad(10);
        content.add(statsTable).width(getStatsWidth()).center().padBottom(getStatsPadBottom()).row();

        String savedText = getSavedText();
        if (savedText != null && !savedText.isEmpty()) {
            Label savedLabel = new Label(savedText, game.getSkin());
            savedLabel.setFontScale(getSavedFontScale());
            savedLabel.setAlignment(com.badlogic.gdx.utils.Align.center);
            content.add(savedLabel).center().padBottom(getSavedPadBottom()).row();
        }

        Table buttons = new Table();
        buttons.defaults().center();
        buildButtons(buttons);
        content.add(buttons).center().padTop(getButtonsPadTop()).row();

        Table contentBox = new Table();
        contentBox.setBackground(game.getSkin().getDrawable("cell"));
        contentBox.pad(getContentPadding());
        contentBox.add(content).width(getContentWidth());
        table.add(contentBox).width(getContentBoxWidth()).center().padTop(getContentBoxPadTop()).row();

        buttonGroup = new WidgetGroup();
        buttonGroup.setFillParent(true);
        buttonGroup.addActor(table);
        // buttons fade in separately after the background is visible
        buttonGroup.getColor().a = 0f;
        stage.addActor(buttonGroup);

        // make a 1x1 black texture used as a full-screen fade overlay
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.BLACK);
        pixmap.fill();
        fadeTexture = new Texture(pixmap);
        pixmap.dispose();
        fadeOverlay = new Image(fadeTexture);
        fadeOverlay.setFillParent(true);
        fadeOverlay.setColor(0f, 0f, 0f, 1f);
        fadeOverlay.setTouchable(Touchable.disabled);
        stage.addActor(fadeOverlay);
    }

    /**
     * Creates a button that plays a click sound and runs an action.
     *
     * @param text button text
     * @param action action to run on click
     * @return configured button
     */
    protected TextButton createButton(String text, Runnable action) {
        TextButton button = new TextButton(text, game.getSkin());
        button.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent changeEvent, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                audioManager.playSound("Click.wav", 1);
                action.run();
            }
        });
        return button;
    }

    /**
     * Returns the title text.
     *
     * @return title
     */
    protected abstract String getTitleText();

    /**
     * Returns the subtitle text.
     *
     * @return subtitle
     */
    protected abstract String getSubtitleText();

    /**
     * Returns the saved/unsaved label text.
     *
     * @return label text or null to omit
     */
    protected abstract String getSavedText();

    /**
     * Returns the first stats line.
     *
     * @param minutes elapsed minutes
     * @param seconds elapsed seconds
     * @return stats line
     */
    protected abstract String getStatsLine1(int minutes, int seconds);

    /**
     * Returns the second stats line.
     *
     * @return stats line
     */
    protected abstract String getStatsLine2();

    /**
     * Builds the button layout for this screen.
     *
     * @param buttons button table
     */
    protected abstract void buildButtons(Table buttons);

    /**
     * Returns whether the background image should accept input.
     *
     * @return true if touchable
     */
    protected boolean isBackgroundTouchable() {
        return true;
    }

    /**
     * Returns the drawable name for the stats box.
     *
     * @return drawable name
     */
    protected String getStatsBackgroundDrawableName() {
        return "cell";
    }

    /**
     * Returns the fade style for the screen.
     *
     * @return fade style
     */
    protected FadeStyle getFadeStyle() {
        return FadeStyle.LOG_OVERLAY;
    }

    /**
     * Returns the title padding.
     *
     * @return padding
     */
    protected float getTitlePadBottom() {
        return 10f;
    }

    /**
     * Returns the subtitle padding.
     *
     * @return padding
     */
    protected float getSubtitlePadBottom() {
        return 20f;
    }

    /**
     * Returns subtitle font scale.
     *
     * @return font scale
     */
    protected float getSubtitleFontScale() {
        return 1.2f;
    }

    /**
     * Returns the stats padding.
     *
     * @return padding
     */
    protected float getStatsPadBottom() {
        return 12f;
    }

    /**
     * Returns the saved label padding.
     *
     * @return padding
     */
    protected float getSavedPadBottom() {
        return 40f;
    }

    /**
     * Returns the saved label font scale.
     *
     * @return font scale
     */
    protected float getSavedFontScale() {
        return 0.9f;
    }

    /**
     * Returns the content box padding.
     *
     * @return padding
     */
    protected float getContentPadding() {
        return 30f;
    }

    /**
     * Returns the content width.
     *
     * @return width
     */
    protected float getContentWidth() {
        return 820f;
    }

    /**
     * Returns the content box width.
     *
     * @return width
     */
    protected float getContentBoxWidth() {
        return 900f;
    }

    /**
     * Returns the content box top padding.
     *
     * @return padding
     */
    protected float getContentBoxPadTop() {
        return 50f;
    }

    /**
     * Returns the stats table width.
     *
     * @return width
     */
    protected float getStatsWidth() {
        return 700f;
    }

    /**
     * Returns the buttons top padding.
     *
     * @return padding
     */
    protected float getButtonsPadTop() {
        return 10f;
    }

    /**
     * Returns the main menu background rendering preference.
     *
     * @return true to allow menu background rendering
     */
    protected boolean allowMenuBackground() {
        return game.shouldRenderMenuBackground();
    }

    @Override
    public void render(float delta) {
        if (!allowMenuBackground()) {
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        }
        stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
        fadeTimer += delta;
        if (getFadeStyle() == FadeStyle.LINEAR) {
            // Linear fade: background alpha goes 0->1, overlay alpha goes 1->0.
            float alpha = Math.min(1f, fadeTimer / FADE_DURATION);
            backgroundImage.setColor(1f, 1f, 1f, alpha);
            float buttonAlpha = Math.min(1f, fadeTimer / BUTTON_FADE_DURATION);
            buttonGroup.getColor().a = buttonAlpha;
            fadeOverlay.setColor(0f, 0f, 0f, 1f - alpha);
        } else {
            // log fade quicker reveal early, smoother at the end
            float t = fadeTimer / FADE_DURATION;
            if (t > 1f) {
                t = 1f;
            } else if (t < 0f) {
                t = 0f;
            }
            float k = 9f;
            float overlayAlpha = (float) (Math.log1p(k * (1f - t)) / Math.log1p(k));
            fadeOverlay.setColor(0f, 0f, 0f, overlayAlpha);
            backgroundImage.setColor(1f, 1f, 1f, 1f - overlayAlpha);
            if (overlayAlpha <= 0f) {
                fadeOverlay.setVisible(false);
            }
            // buttons start fading in after the main fade completes
            float buttonAlpha = (fadeTimer - FADE_DURATION) / BUTTON_FADE_DURATION;
            if (buttonAlpha < 0f) {
                buttonAlpha = 0f;
            } else if (buttonAlpha > 1f) {
                buttonAlpha = 1f;
            }
            buttonGroup.getColor().a = buttonAlpha;
        }
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void dispose() {
        stage.dispose();
        backgroundTexture.dispose();
        fadeTexture.dispose();
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
        stage.addListener(game.getKeyHandler());
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
}
