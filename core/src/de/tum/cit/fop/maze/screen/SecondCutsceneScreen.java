package de.tum.cit.fop.maze.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import de.tum.cit.fop.maze.MazeRunnerGame;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class SecondCutsceneScreen implements Screen {
    private static final float FADE_IN_SECONDS = 2.5f;
    private static final float SLIDE_DURATION = 3.5f;
    private static final float FADE_TO_BLACK_SECONDS = 1.5f;
    private static final float BOX_FADE_SECONDS = 2f;
    private static final float TYPE_SECONDS_PER_CHAR = 0.06f;
    private static final String FINAL_TEXT = "I have a delivery for you, it's a bit risky with all the cops around the street, but i'm willing to pay extra.";

    private final MazeRunnerGame game;
    private final Stage stage;
    private final Texture[] textures;
    private final Image[] images;
    private final int targetLevel;
    private final Image blackOverlay;
    private final Texture blackTexture;
    private final Table textBox;
    private final Label textLabel;
    private final Texture boxTexture;
    private final Table buttonTable;
    private final TextButton acceptButton;
    private float elapsed = 0f;
    private boolean finished = false;
    private float blackTimer = 0f;
    private float textTimer = 0f;
    private float buttonTimer = 0f;
    private CutsceneState state = CutsceneState.SHOW_SLIDES;
    private final boolean useRollingText;

    private enum CutsceneState {
        SHOW_SLIDES,
        FADE_TO_BLACK,
        SHOW_TEXT
    }

    public SecondCutsceneScreen(MazeRunnerGame game, int targetLevel) {
        this.game = game;
        this.targetLevel = targetLevel;
        this.useRollingText = !(targetLevel == 3 || targetLevel == 4 || targetLevel == 5);

        Viewport viewport = new FitViewport(1920, 1080);
        stage = new Stage(viewport, game.getSpriteBatch());

        List<FileHandle> slideFiles = loadSlideFiles();
        textures = new Texture[slideFiles.size()];
        images = new Image[slideFiles.size()];

        for (int i = 0; i < slideFiles.size(); i++) {
            textures[i] = new Texture(slideFiles.get(i));
            images[i] = new Image(textures[i]);
            images[i].setFillParent(true);
            images[i].getColor().a = 0f;
            stage.addActor(images[i]);
        }

        Pixmap blackPixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        blackPixmap.setColor(Color.BLACK);
        blackPixmap.fill();
        blackTexture = new Texture(blackPixmap);
        blackPixmap.dispose();
        blackOverlay = new Image(blackTexture);
        blackOverlay.setFillParent(true);
        blackOverlay.getColor().a = 0f;
        stage.addActor(blackOverlay);

        Pixmap boxPixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        boxPixmap.setColor(0f, 0f, 0f, 0.65f);
        boxPixmap.fill();
        boxTexture = new Texture(boxPixmap);
        boxPixmap.dispose();

        textBox = new Table();
        textBox.setFillParent(true);
        textBox.bottom().padBottom(120);
        textBox.setBackground(new Image(boxTexture).getDrawable());
        textBox.getColor().a = 0f;

        textLabel = new Label("", game.getSkin());
        textLabel.setColor(Color.WHITE);
        textBox.add(textLabel).pad(24);
        stage.addActor(textBox);

        acceptButton = new TextButton("Accept delivery", game.getSkin());
        if (!useRollingText) {
            acceptButton.setText(String.format("Start your %d delivery", targetLevel));
        }
        acceptButton.setVisible(false);
        acceptButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                finishCutscene();
            }
        });
        buttonTable = new Table();
        buttonTable.setFillParent(true);
        buttonTable.bottom().padBottom(70);
        buttonTable.add(acceptButton).width(420).height(70);
        buttonTable.getColor().a = 0f;
        stage.addActor(buttonTable);
    }

    private List<FileHandle> loadSlideFiles() {
        if (targetLevel == 2) {
            return List.of(
                Gdx.files.internal("Comic-Level2-0.png"),
                Gdx.files.internal("Comic-Level2-1.png"),
                Gdx.files.internal("Comic-Level2-2.png"),
                Gdx.files.internal("Comic-Level2-3.png"),
                Gdx.files.internal("Comic-Level2-4.png")
            );
        }
        if (targetLevel == 3) {
            return List.of(
                Gdx.files.internal("Comic-Level3-0.png"),
                Gdx.files.internal("Comic-Level3-1.png"),
                Gdx.files.internal("Comic-Level3-2.png"),
                Gdx.files.internal("Comic-Level3-3.png"),
                Gdx.files.internal("Comic-Level3-4.png"),
                Gdx.files.internal("Comic-Level3-5.png"),
                Gdx.files.internal("Comic-Level3-6.png")
            );
        }
        if (targetLevel == 4) {
            return List.of(
                Gdx.files.internal("Comic-Level4-0.png"),
                Gdx.files.internal("Comic-Level4-1.png"),
                Gdx.files.internal("Comic-Level4-2.png"),
                Gdx.files.internal("Comic-Level4-3.png"),
                Gdx.files.internal("Comic-Level4-4.png"),
                Gdx.files.internal("Comic-Level4-5.png")
            );
        }
        if (targetLevel == 5) {
            return List.of(
                Gdx.files.internal("Comic-Level5-0.png"),
                Gdx.files.internal("Comic-Level5-1.png"),
                Gdx.files.internal("Comic-Level5-2.png"),
                Gdx.files.internal("Comic-Level5-3.png"),
                Gdx.files.internal("Comic-Level5-4.png"),
                Gdx.files.internal("Comic-Level5-5.png"),
                Gdx.files.internal("Comic-Level5-6.png"),
                Gdx.files.internal("Comic-Level5-7.png"),
                Gdx.files.internal("Comic-Level5-8.png")
            );
        }
        List<FileHandle> slideFiles = new ArrayList<>();
        for (int i = 1; i <= 99; i++) {
            FileHandle jpg = Gdx.files.internal("finished/" + i + ".jpg");
            if (jpg.exists()) {
                slideFiles.add(jpg);
                continue;
            }
            FileHandle png = Gdx.files.internal("finished/" + i + ".png");
            if (png.exists()) {
                slideFiles.add(png);
                continue;
            }
            break;
        }
        slideFiles.sort(Comparator.comparingInt(f -> Integer.parseInt(f.nameWithoutExtension())));
        return slideFiles;
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
        stage.addListener(game.getKeyHandler());
    }

    @Override
    public void render(float delta) {
        if (!game.shouldRenderMenuBackground()) {
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        }
        switch (state) {
            case SHOW_SLIDES -> {
                elapsed += delta;
                if (images.length == 0) {
                    if (useRollingText) {
                        state = CutsceneState.FADE_TO_BLACK;
                    } else {
                        acceptButton.setVisible(true);
                        buttonTable.getColor().a = 1f;
                    }
                } else {
                    int activeIndex = (int) (elapsed / SLIDE_DURATION);
                    if (activeIndex < images.length) {
                        float t = elapsed - (activeIndex * SLIDE_DURATION);
                        float alpha = Math.max(0f, Math.min(1f, t / FADE_IN_SECONDS));
                        for (int i = 0; i < images.length; i++) {
                            if (i < activeIndex) {
                                images[i].getColor().a = 1f;
                            } else if (i == activeIndex) {
                                images[i].getColor().a = alpha;
                            } else {
                                images[i].getColor().a = 0f;
                            }
                        }
                    } else {
                        for (Image image : images) {
                            image.getColor().a = 1f;
                        }
                        if (useRollingText) {
                            state = CutsceneState.FADE_TO_BLACK;
                        } else {
                            acceptButton.setVisible(true);
                            buttonTable.getColor().a = 1f;
                        }
                    }
                }
            }
            case FADE_TO_BLACK -> {
                if (!useRollingText) {
                    return;
                }
                blackTimer += delta;
                float t = Math.min(1f, blackTimer / FADE_TO_BLACK_SECONDS);
                blackOverlay.getColor().a = t;
                if (t >= 1f) {
                    state = CutsceneState.SHOW_TEXT;
                }
            }
            case SHOW_TEXT -> {
                if (!useRollingText) {
                    return;
                }
                textTimer += delta;
                float boxAlpha = Math.min(1f, textTimer / BOX_FADE_SECONDS);
                textBox.getColor().a = boxAlpha;
                int visibleChars = Math.min(FINAL_TEXT.length(), (int) (textTimer / TYPE_SECONDS_PER_CHAR));
                textLabel.setText(FINAL_TEXT.substring(0, visibleChars));
                if (visibleChars >= FINAL_TEXT.length()) {
                    acceptButton.setVisible(true);
                    buttonTimer += delta;
                    float buttonAlpha = Math.min(1f, buttonTimer / BOX_FADE_SECONDS);
                    buttonTable.getColor().a = buttonAlpha;
                }
            }
        }

        stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
        stage.draw();
    }

    private void finishCutscene() {
        if (finished) {
            return;
        }
        finished = true;
        game.goToGame(targetLevel);
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void dispose() {
        stage.dispose();
        for (Texture texture : textures) {
            if (texture != null) {
                texture.dispose();
            }
        }
        blackTexture.dispose();
        boxTexture.dispose();
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
