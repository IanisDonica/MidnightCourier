package de.tum.cit.fop.maze.entity.obstacle;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;

public class JandarmeriaDeath extends Actor {
    private static final float LIFETIME = 0.4f;
    private final TextureRegion texture;
    private float timer = 0f;

    public JandarmeriaDeath(float centerX, float centerY) {
        Texture textureSheet = new Texture(Gdx.files.internal("objects.png"));
        this.texture = new TextureRegion(textureSheet, 96, 48, 16, 16);
        setSize(1.5f, 1.5f);
        setPosition(centerX - getWidth() / 2f, centerY - getHeight() / 2f);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        timer += delta;
        if (timer >= LIFETIME) {
            remove();
        }
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        batch.draw(texture, getX(), getY(), getWidth(), getHeight());
    }
}
