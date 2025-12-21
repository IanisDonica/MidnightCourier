package de.tum.cit.fop.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputEventQueue;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

/**
 * The GameScreen class is responsible for rendering the gameplay screen.
 * It handles the game logic and rendering of the game elements.
 */
public class GameScreen implements Screen {

    private final MazeRunnerGame game;
    private final BitmapFont font;
    private TiledMap map;
    private OrthogonalTiledMapRenderer mapRenderer;
    private final Stage stage;

    private float sinusInput = 0f;

    /**
     * Constructor for GameScreen. Sets up the camera and font.
     *
     * @param game The main game class, used to access global resources and methods.
     */
    public GameScreen(MazeRunnerGame game) {
        this.game = game;

        Viewport viewport = new FitViewport(512, 256);
        this.stage = new Stage(viewport, game.getSpriteBatch());
        this.map = new TmxMapLoader().load("untitled.tmx");
        mapRenderer = new OrthogonalTiledMapRenderer(map, 1f, game.getSpriteBatch());

        // Get the font from the game's skin
        font = game.getSkin().getFont("font");

        ((OrthographicCamera)stage.getCamera()).zoom = 1f;

        stage.addListener(new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                if (keycode == Input.Keys.ESCAPE) {
                    game.goToMenu();
                    return true;
                }
                return false;
            }
        });
    }


    // Screen interface methods with necessary functionality
    @Override
    public void render(float delta) {

        sinusInput += Gdx.graphics.getDeltaTime();

        ScreenUtils.clear(0, 0, 0, 1); // Clear the screen

        // 4. Update the renderer's camera
        mapRenderer.setView((OrthographicCamera) stage.getCamera());

        // 5. Draw the map first (background)
        mapRenderer.render();


        Actor label = stage.getRoot().findActor("instruction");
        Player player = stage.getRoot().findActor("player");
        if (label != null) {
            float textX = (float) (256 - ((Label)label).getPrefWidth() / 2 + Math.sin(sinusInput) * 50);
            float textY = (float) (128 - ((Label)label).getPrefHeight() / 2 + Math.cos(sinusInput) * 50);
            label.setPosition(textX, textY);
            ((Label) label).setText(String.format("X - %s; Y - %s", player.getX() / 16, player.getY() / 16));
            //player.setPosition(textX + ((Label)label).getPrefWidth() / 2 - player.getWidth() / 2, textY - ((Label)label).getPrefHeight() - 10);

            /*
            System.out.printf("X - %s; Y - %s; Delta - %s; cos - %s; prefW - %s%n",
                    textX,
                    textY,
                    Gdx.graphics.getDeltaTime(),
                    Math.cos(Gdx.graphics.getDeltaTime()),
                    ((Label)label).getPrefWidth()
            );
             */
        }

        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);

        Label.LabelStyle labelStyle = new Label.LabelStyle(game.getSkin().getFont("font"), Color.WHITE);
        Label instructionLabel = new Label("Press ESC to go to menu", labelStyle);
        instructionLabel.setName("instruction");

        Player player = new Player(map, 256, 128);
        player.setName("player");

        stage.addActor(player);
        stage.setKeyboardFocus(player);
        stage.addActor(instructionLabel);

        // Da Blocks

    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
        stage.dispose();
    }

    // Additional methods and logic can be added as needed for the game screen
}
