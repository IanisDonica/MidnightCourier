package de.tum.cit.fop.maze.entity;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;

/**
 * Base entity class with animations and movement properties.
 */
public class Entity extends Actor {
    /** Animation for facing down. */
    protected Animation<TextureRegion> downAnimation;
    /** Animation for facing up. */
    protected Animation<TextureRegion> upAnimation;
    /** Animation for facing right. */
    protected Animation<TextureRegion> rightAnimation;
    /** Animation for facing left. */
    protected Animation<TextureRegion> leftAnimation;
    /** Facing direction indicator. */
    protected char facingDirection;
    /** Animation time accumulator. */
    protected float animationTime;
    /** Movement speed. */
    protected float speed;

    /**
     * Returns the current speed.
     *
     * @return speed value
     */
    public float getSpeed() {
        return speed;
    }

    /**
     * Multiplies speed by the given factor.
     *
     * @param speedMultiplier multiplier to apply
     */
    public void multiplySpeed(float speedMultiplier) {
        this.speed *= speedMultiplier;
    }

    /**
     * Creates an entity at the given position.
     *
     * @param x x position
     * @param y y position
     */
    public Entity(float x, float y) {
        setX(x);
        setY(y);
    }
}
