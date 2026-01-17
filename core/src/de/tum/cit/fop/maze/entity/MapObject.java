package de.tum.cit.fop.maze.entity;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;

public class MapObject extends Actor {
    protected final Rectangle bounds = new Rectangle();
    protected Player player;
    protected float animationTime;
    protected final static int frameWidth = 16, frameHeight = 16;
    protected boolean addedToStageFired = false;

    @Override
    public void act(float delta) {
        super.act(delta);
        animationTime += delta;

        if (!addedToStageFired) {
            addedToStageFired = true;
            onAddedToStage();
        }

        updateBounds();
        checkCollisionsWithPlayer();
    }

    protected void onAddedToStage() {
        Stage stage = getStage();
        for (Actor actor : stage.getActors()) {
            if (actor instanceof Player pl) {
                player = pl;
                return;
            }
        }
        throw new RuntimeException("Player must be added before Obstacles");
    }

    protected void updateBounds() {
        bounds.set(getX() + 0.25f, getY() + 0.5f, getWidth() * 0.5f, getHeight() * 0.25f);
    }

    protected void checkCollisionsWithPlayer() {
        if (bounds.overlaps(new Rectangle(player.getX(), player.getY(), player.getWidth(), player.getHeight()))) {
            collision();
        }
    }

    // This class is meant to be overiden by all classes that extend Collectible
    protected void collision() {
        return;
    }
}
