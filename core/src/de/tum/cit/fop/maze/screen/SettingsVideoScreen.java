package de.tum.cit.fop.maze.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import de.tum.cit.fop.maze.MazeRunnerGame;
import de.tum.cit.fop.maze.system.AudioManager;
import de.tum.cit.fop.maze.system.UiUtils;
import de.tum.cit.fop.maze.system.GraphicsManager;

public class SettingsVideoScreen implements Screen {
    private final MazeRunnerGame game;
    private final AudioManager audioManager;
    private final Stage stage;
    private final Texture vignetteTexture;
    private final GraphicsManager graphicsManager;

    public SettingsVideoScreen(MazeRunnerGame game) {
        this.game = game;
        audioManager = game.getAudioManager();
        graphicsManager = game.getGraphicsManager();

        var camera = new OrthographicCamera();
        camera.zoom = 1.5f; // Set camera zoom for a closer view

        Viewport viewport = new FitViewport(graphicsManager.getWidth(), graphicsManager.getHeight(), camera);
        stage = new Stage(viewport, game.getSpriteBatch());

        vignetteTexture = UiUtils.buildVignetteTexture(512, 512, 0.9f);
        Image vignetteImage = new Image(vignetteTexture);
        vignetteImage.setFillParent(true);
        vignetteImage.setTouchable(Touchable.disabled);
        stage.addActor(vignetteImage);

        Table table = new Table(); // Create a table for layout
        table.setFillParent(true); // Make the table fill the stage
        stage.addActor(table);
        table.add(new Label("Settings: Video", game.getSkin(), "title")).colspan(2).padBottom(80).row();

        Slider fpsSlider = new Slider(30, 360, 2, false, game.getSkin());
        fpsSlider.setValue(graphicsManager.getTargetFrameRate());
        Label fpsLabel = new Label("FPS: " + graphicsManager.getTargetFrameRate(), game.getSkin(), "title");
        fpsSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                audioManager.playSound("Knife.wav", 1, 0.2f, 0.001f);
                graphicsManager.setFrameLimit((int) fpsSlider.getValue());
                fpsLabel.setText("FPS: " + graphicsManager.getTargetFrameRate());
            }
        });


        Label displayLabel = new Label("Display mode:", game.getSkin(), "title");
        SelectBox<String> displayMode = new SelectBox<>(buildSelectBoxStyle());
        displayMode.setItems("Windowed", "Borderless Windowed", "Fullscreen");
        displayMode.setSelected(graphicsManager.getDisplayModeAsString());
        displayMode.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                audioManager.playSound("Click.wav", 1);
                if (displayMode.getSelected().equals("Windowed")) {
                    graphicsManager.setWindowed();
                }
                else if (displayMode.getSelected().equals("Borderless Windowed")) {
                    graphicsManager.setBorderless();
                }
                else  if (displayMode.getSelected().equals("Fullscreen")) {
                    graphicsManager.setFullscreen();
                }
            }
        });


        table.add(fpsLabel);
        table.add(fpsSlider).width(graphicsManager.getWidth() * 0.6f).padBottom(15).row();
        table.add(displayLabel);
        table.add(displayMode);

//        masterVolumeSlider.addListener(new ChangeListener() {
//            @Override
//            public void changed(ChangeEvent event, Actor actor) {
//                audioManager.playSound("Knife.wav", 1, 0.2f, 0.001f);
//                audioManager.setMasterVolume(masterVolumeSlider.getValue());
//                configManager.saveAudioSettings();
//            }
//        });
//
//        soundEffectsVolumeSlider.addListener(new ChangeListener() {
//            @Override
//            public void changed(ChangeEvent event, Actor actor) {
//                audioManager.playSound("Knife.wav", 1, 0.2f, 0.001f);
//                audioManager.setSoundEffectsVolume(soundEffectsVolumeSlider.getValue());
//                configManager.saveAudioSettings();
//            }
//        });
//
//        musicVolumeSlider.addListener(new ChangeListener() {
//            @Override
//            public void changed(ChangeEvent event, Actor actor) {
//                audioManager.playSound("Knife.wav", 1, 0.2f, 0.001f);
//                audioManager.setMusicVolume(musicVolumeSlider.getValue());
//                configManager.saveAudioSettings();
//            }
//        });

    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
        stage.addListener(game.getKeyHandler());
    }

    @Override
    public void render(float v) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true); // Update the stage viewport on resize
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

    @Override
    public void dispose() {
        stage.dispose();
        vignetteTexture.dispose();
    }

    private SelectBox.SelectBoxStyle buildSelectBoxStyle() {
        SelectBox.SelectBoxStyle style = new SelectBox.SelectBoxStyle();
        style.font = game.getSkin().getFont("font");
        style.fontColor = Color.WHITE;
        style.background = game.getSkin().getDrawable("cell");
        style.scrollStyle = game.getSkin().get("default", ScrollPane.ScrollPaneStyle.class);

        List.ListStyle listStyle = new List.ListStyle();
        listStyle.font = style.font;
        listStyle.fontColorSelected = Color.WHITE;
        listStyle.fontColorUnselected = Color.WHITE;
        listStyle.selection = game.getSkin().getDrawable("cell");
        style.listStyle = listStyle;
        return style;
    }
}
