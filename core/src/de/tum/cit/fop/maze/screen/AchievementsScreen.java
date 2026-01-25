package de.tum.cit.fop.maze.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import de.tum.cit.fop.maze.MazeRunnerGame;
import de.tum.cit.fop.maze.system.AchievementManager;
import de.tum.cit.fop.maze.system.AudioManager;

public class AchievementsScreen implements Screen {
    private Stage stage;
    private MazeRunnerGame game;
    private final AudioManager audioManager;

    public AchievementsScreen(MazeRunnerGame game, AchievementManager achievementManager){
        this.game = game;
        var camera = new OrthographicCamera();
        camera.zoom = 1.5f; // Set camera zoom for a closer view
        audioManager = game.getAudioManager();

        Viewport viewport = new FitViewport(1920, 1080);
        stage = new Stage(viewport, game.getSpriteBatch()); // Create a stage for UI elements
        Table table = new Table();
        stage.addActor(table);
        table.setFillParent(true);
        table.add(new Label("Achievements", game.getSkin(), "title")).padTop(40).row();
        TextButton mainMenuButton = new TextButton("Main Menu", game.getSkin());
        mainMenuButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent changeEvent, Actor actor) {
                audioManager.playSound("Click.wav", 1);
                game.goToMenu();
            }
        });

        Table achievementsTable = new Table(game.getSkin());
        ScrollPane scrollPane = new ScrollPane(achievementsTable);

        for(var achievement: achievementManager.getAchievements()){
            Table achievementTable = new Table(game.getSkin());
            achievementTable.top().add(new Label(achievement.getName(), game.getSkin())).row();
            achievementTable.bottom().add(new Label(achievement.getDescription(), game.getSkin()));
            achievementsTable.add(achievementTable).row();
        }

        table.add(scrollPane).setActorWidth(400);
        table.row();
        table.add(mainMenuButton);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT); // Clear the screen
        stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f)); // Update the stage
        stage.draw(); // Draw the stage
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true); // Update the stage viewport on resize
    }

    @Override
    public void dispose() {
        // Dispose of the stage when the screen is disposed
        stage.dispose();
    }

    @Override
    public void show() {
        // Set the input processor so the stage can receive input events
        Gdx.input.setInputProcessor(stage);
        stage.addListener(game.getKeyHandler());
    }

    // The following methods are part of the Screen interface but are not used in this screen.
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
