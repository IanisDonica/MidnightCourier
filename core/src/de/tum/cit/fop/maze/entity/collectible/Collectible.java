package de.tum.cit.fop.maze.entity.collectible;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import de.tum.cit.fop.maze.entity.MapObject;
import de.tum.cit.fop.maze.system.PointManager;
import com.badlogic.gdx.utils.Array;

public class Collectible extends MapObject {
    protected final PointManager pointManager;
    protected Animation<TextureRegion> spinAnimation;
    private boolean pickedUp = false;
    private int pickUpFrameCounter = 32;

    public Collectible(float x, float y, int w, int h, PointManager pointManager) {
        setX(x);
        setY(y);
        setSize(w, h);
        this.pointManager = pointManager;
        this.animationTime = MathUtils.random(0f, 1f);
    }

    protected void initSpinAnimation(int startX, int rowY, int frameCount) {
        Texture textureSheet = new Texture(Gdx.files.internal("objects.png"));
        int frameWidth = 16;
        int frameHeight = 16;
        Array<TextureRegion> spinFrames = new Array<>(TextureRegion.class);
        for (int col = 0; col < frameCount; col++) {
            spinFrames.add(new TextureRegion(textureSheet, startX + col * frameWidth, rowY, frameWidth, frameHeight));
        }
        spinAnimation = new Animation<>(0.25f, spinFrames);
    }

    public void markPickedUp() {
        this.pickedUp = true;
    }
    public boolean getPickedUp() {
        return pickedUp;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        batch.draw(spinAnimation.getKeyFrame(animationTime, true), getX(), getY(), getWidth(), getHeight());
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        // For now its ok if they all have the same shrink animation, later if we got time maybe we can give them seperate animatinos
        // in which this code would need to be changed.
        if (pickedUp) {
            if (pickUpFrameCounter == 0) {
                remove();
            }

            this.setSize(getWidth() / 1.1f, getHeight() / 1.1f);
            this.setPosition(getX() + (getWidth() * 1.1f - getWidth()) / 2, getY() + (getHeight() * 1.1f - getHeight()) / 2);
            pickUpFrameCounter--;
        }
    }
}
