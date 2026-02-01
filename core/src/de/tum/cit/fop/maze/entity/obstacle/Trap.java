package de.tum.cit.fop.maze.entity.obstacle;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import de.tum.cit.fop.maze.entity.DeathCause;

/**
 * Trap obstacle that damages the player on contact.
 */
public class Trap extends Obstacle {
    /**
     * Creates a trap at the given position.
     *
     * @param x x position
     * @param y y position
     */
    public Trap(float x, float y) {
        super(x, y, 1, 1, 0, 0, 1);
    }

    /**
     * Handles collision by damaging the player if not immune.
     */
    @Override
    public void collision() {
        if (player.isPotholeImmune()) {
            return;
        }
        if (!player.isStunned()) {
            player.damage(999, DeathCause.POTHOLE);
        }
    }

    /**
     * Initializes the trap animation.
     */
    @Override
    protected void initAnimation() {
        Texture textureSheet = new Texture(Gdx.files.internal("Pixel_manhole_open_16x16.png"));
        Array<TextureRegion> frames = new Array<>(TextureRegion.class);

        for (int col = 0; col < animationFrames; col++) {
            frames.add(new TextureRegion(textureSheet, textureOffsetX + col * frameWidth, textureOffsetY, frameWidth, frameHeight));
        }

        animation = new Animation<>(0.25f, frames);
    }
}
