package de.tum.cit.fop.maze.entity.obstacle;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;

/**
 * Effect shown when a BMW hits a guard.
 */
public class JandarmeriaDeath extends Actor {
    /** Lifetime of the effect in seconds. */
    private static final float LIFETIME = 0.4f;
    /** Texture region for the effect. */
    private final TextureRegion texture;
    /** Timer for lifetime tracking. */
    private float timer = 0f;

    /**
     * Creates the effect centered at the given position.
     *
     * @param centerX center x position
     * @param centerY center y position
     */
    public JandarmeriaDeath(float centerX, float centerY) {
        Texture textureSheet = new Texture(Gdx.files.internal("objects.png"));
        this.texture = new TextureRegion(textureSheet, 96, 48, 16, 16);
        setSize(1.5f, 1.5f);
        setPosition(centerX - getWidth() / 2f, centerY - getHeight() / 2f);
    }

    /**
     * Updates the effect timer and removes when expired.
     *
     * @param delta frame delta time
     */
    @Override
    public void act(float delta) {
        super.act(delta);
        timer += delta;
        if (timer >= LIFETIME) {
            remove();
        }
    }

    /**
     * Draws the effect sprite.
     *
     * @param batch sprite batch
     * @param parentAlpha parent alpha
     */
    @Override
    public void draw(Batch batch, float parentAlpha) {
        batch.draw(texture, getX(), getY(), getWidth(), getHeight());
    }
}
