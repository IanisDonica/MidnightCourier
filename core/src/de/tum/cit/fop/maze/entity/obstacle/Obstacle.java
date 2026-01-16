package de.tum.cit.fop.maze.entity.obstacle;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import de.tum.cit.fop.maze.entity.MapObject;
import com.badlogic.gdx.utils.Array;

public class Obstacle extends MapObject {
    private Animation<TextureRegion> animation;

    public Obstacle(float x, float y, int w, int h, int textureOffsetX, int textureOffsetY, float animationTime) {
        setPosition(x, y);
        setSize(w, h);
        this.animationTime = animationTime;
        initAnimation(textureOffsetX, textureOffsetY);
    }

    private void initAnimation(int textureOffsetX, int textureOffsetY) {
        Texture textureSheet = new Texture(Gdx.files.internal("objects.png"));
        int frameWidth = 16;
        int frameHeight = 16;
        Array<TextureRegion> frames = new Array<>(TextureRegion.class);
        frames.add(new TextureRegion(textureSheet, textureOffsetX, textureOffsetY, frameWidth, frameHeight));
        animation = new Animation<>(0.25f, frames);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        batch.draw(animation.getKeyFrame(animationTime, true), getX(), getY(), getWidth(), getHeight());
    }
}
