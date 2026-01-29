package de.tum.cit.fop.maze.entity.obstacle;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import de.tum.cit.fop.maze.entity.DeathCause;
import de.tum.cit.fop.maze.system.PointManager;

public class Trap extends Obstacle {
    public Trap(float x, float y) {
        super(x, y, 1, 1, 0, 0, 1);
    }

    @Override
    public void collision() {
        if (player.isPotholeImmune()) {
            return;
        }
        if (!player.isStunned()) {
            player.damage(1, DeathCause.POTHOLE);
        }
    }

    @Override
    protected void initAnimation() {
        //This is kinda duplicate, but it will later be useful
        Texture textureSheet = new Texture(Gdx.files.internal("Pixel_manhole_open_16x16.png"));
        Array<TextureRegion> frames = new Array<>(TextureRegion.class);

        for (int col = 0; col < animationFrames; col++) {
            frames.add(new TextureRegion(textureSheet, textureOffsetX + col * frameWidth, textureOffsetY, frameWidth, frameHeight));
        }

        animation = new Animation<>(0.25f, frames);
    }
}
