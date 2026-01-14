package de.tum.cit.fop.maze.entity.collectible;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

public class HealthPickup extends Collectible {
    protected Animation<TextureRegion> healthSpinAnimation;

    public HealthPickup(float x, float y) {
        super(x, y, 1, 1);
        initialiseAnimations();
    }

    private void initialiseAnimations() {
        Texture healthSheet = new Texture(Gdx.files.internal("objects.png"));

        int frameWidth = 16;
        int frameHeight = 16;
        int animationFrame = 4;

        Array<TextureRegion> healthSpinFrames = new Array<>(TextureRegion.class);

        for (int col = 0; col < animationFrame; col++) {
            healthSpinFrames.add(new TextureRegion(healthSheet, col * frameWidth, 48, frameWidth, frameHeight));
        }

        healthSpinAnimation = new Animation<>(0.25f, healthSpinFrames);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        batch.draw(healthSpinAnimation.getKeyFrame(animationTime, true), getX(), getY(), getWidth(), getHeight());
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        animationTime += delta;
    }

    @Override
    protected void collision() {
        this.player.setHp(this.player.getHp() + 1);
        System.out.println(this.player.getHp());
        remove();
    }
}
