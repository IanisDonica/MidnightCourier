package de.tum.cit.fop.maze.entity;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;

/**
 * Base class for map objects that can collide with the player.
 */
public class MapObject extends Actor {
    /** Collision bounds for this object. */
    protected final Rectangle bounds = new Rectangle();
    /** Player reference once discovered on stage. */
    protected Player player;
    /** Animation time accumulator. */
    protected float animationTime;
    /** Default frame size. */
    protected final static int frameWidth = 16, frameHeight = 16;
    /** Whether onAddedToStage has been invoked. */
    protected boolean addedToStageFired = false;

    /**
     * Updates animation, detects player, and checks collisions.
     *
     * @param delta frame delta time
     */
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

    /**
     * Called once after the actor is added to a stage.
     */
    protected void onAddedToStage() {
        Stage stage = getStage();
        if (stage == null) {
            return;
        }
        for (Actor actor : stage.getActors()) {
            if (actor instanceof Player pl) {
                player = pl;
                return;
            }
        }
        throw new RuntimeException("Player must be added before Obstacles");
    }

    /**
     * Updates the collision bounds for this object.
     */
    protected void updateBounds() {
        bounds.set(getX() + 0.25f, getY() + 0.5f, getWidth() * 0.5f, getHeight() * 0.25f);
    }

    /**
     * Checks for overlap with the player and triggers collision if needed.
     */
    protected void checkCollisionsWithPlayer() {
        if (player == null) {
            return;
        }
        if (bounds.overlaps(new Rectangle(player.getX(), player.getY(), player.getWidth(), player.getHeight()))) {
            collision();
        }
    }

    // This class is meant to be overiden by all classes that extend Collectible
    protected void collision() {
        return;
    }
}
