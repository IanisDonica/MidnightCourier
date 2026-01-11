package de.tum.cit.fop.maze;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;

public class Entity extends Actor {
    protected Animation<TextureRegion> downAnimation;
    protected Animation<TextureRegion> upAnimation;
    protected Animation<TextureRegion> rightAnimation;
    protected Animation<TextureRegion> leftAnimation;
    protected char facingDirection;
    protected float animationTime;
    protected float speed;


    public float getSpeed() {
        return speed;
    }
    public void multiplySpeed(float speedMultiplier) {
        this.speed *= speedMultiplier;
    }

}
