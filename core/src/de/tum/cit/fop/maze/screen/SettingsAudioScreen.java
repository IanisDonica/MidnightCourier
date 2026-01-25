package de.tum.cit.fop.maze.screen;

import com.badlogic.gdx.Screen;
import de.tum.cit.fop.maze.MazeRunnerGame;
import de.tum.cit.fop.maze.system.AudioManager;

public class SettingsAudioScreen implements Screen {
    private final MazeRunnerGame game;
    private final AudioManager audioManager;

    public SettingsAudioScreen(MazeRunnerGame game) {
        this.game = game;
        audioManager = game.getAudioManager();
    }
    @Override
    public void show() {

    }

    @Override
    public void render(float v) {

    }

    @Override
    public void resize(int i, int i1) {

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

    }
}
