package de.tum.cit.fop.maze.entity.collectible;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import de.tum.cit.fop.maze.system.PointManager;

public class HealthPickup extends Collectible {
    protected Animation<TextureRegion> healthSpinAnimation;
    private boolean pickedUp = false;
    private int pickUpFrameCounter = 32;

    public HealthPickup(float x, float y, PointManager pointManager) {
        super(x, y, 1, 1, pointManager);
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

        if (pickedUp) {
            if (pickUpFrameCounter == 0) {
                remove();
            }

            this.setSize(getWidth() / 1.1f, getHeight() / 1.1f);
            this.setPosition(getX() + (getWidth() * 1.1f - getWidth()) / 2, getY() + (getHeight() * 1.1f - getHeight()) / 2);
            pickUpFrameCounter--;
        }
    }

    @Override
    protected void collision() {
        if (!pickedUp) {
            this.player.setHp(this.player.getHp() + 1);
            pointManager.add(400);
            this.pickedUp = true;
        }
    }
}
