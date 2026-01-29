package de.tum.cit.fop.maze.entity.obstacle;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import de.tum.cit.fop.maze.entity.MapObject;
import com.badlogic.gdx.utils.Array;

public class Obstacle extends MapObject {
    Animation<TextureRegion> animation;
    int textureOffsetX, textureOffsetY, animationFrames;
    private static Texture sharedTextureSheet;
    private static boolean textureInitialized = false;

    public Obstacle(float x, float y, int w, int h, int textureOffsetX, int textureOffsetY, int animationFrames) {
        setPosition(x, y);
        setSize(w, h);
        this.animationFrames = animationFrames;
        this.textureOffsetX = textureOffsetX;
        this.textureOffsetY = textureOffsetY;
        initAnimation();
    }

    protected void initAnimation() {
        //This is kinda duplicate, but it will later be useful
        if (!textureInitialized) {
            textureInitialized = true;
            sharedTextureSheet = new Texture(Gdx.files.internal("objects.png"));
        }
        Array<TextureRegion> frames = new Array<>(TextureRegion.class);

        for (int col = 0; col < animationFrames; col++) {
            frames.add(new TextureRegion(sharedTextureSheet, textureOffsetX + col * frameWidth, textureOffsetY, frameWidth, frameHeight));
        }

        animation = new Animation<>(0.25f, frames);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        batch.draw(animation.getKeyFrame(animationTime, true), getX(), getY(), getWidth(), getHeight());
    }
}
