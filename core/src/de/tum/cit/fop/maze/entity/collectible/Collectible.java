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

/**
 * Base class for collectible items.
 */
public class Collectible extends MapObject {
    /** Point manager for scoring effects. */
    protected final PointManager pointManager;
    /** Spin animation for the collectible. */
    protected Animation<TextureRegion> spinAnimation;
    /** Whether the collectible has been picked up. */
    private boolean pickedUp = false;
    /** Frames remaining for pickup shrink animation. */
    private int pickUpFrameCounter = 32;
    /** Spawn coordinates used as ID. */
    private final float spawnX, spawnY;

    /**
     * Creates a collectible at the given position and size.
     *
     * @param x spawn x position
     * @param y spawn y position
     * @param w width in tiles
     * @param h height in tiles
     * @param pointManager point manager for scoring
     */
    public Collectible(float x, float y, int w, int h, PointManager pointManager) {
        setX(x);
        setY(y);
        this.spawnX = x;
        this.spawnY = y;
        setSize(w, h);
        this.pointManager = pointManager;
        this.animationTime = MathUtils.random(0f, 1f);
    }

    /**
     * Returns the spawn x coordinate.
     *
     * @return spawn x
     */
    public float getSpawnX() {
        return spawnX;
    }

    /**
     * Returns the spawn y coordinate.
     *
     * @return spawn y
     */
    public float getSpawnY() {
        return spawnY;
    }

    /**
     * Initializes a spin animation from the objects sprite sheet.
     *
     * @param startX starting x coordinate in the sheet
     * @param rowY row y coordinate in the sheet
     * @param frameCount number of frames
     */
    @SuppressWarnings("SameParameterValue") // So what
    protected void initSpinAnimation(int startX, int rowY, int frameCount) {
        Texture textureSheet = new Texture(Gdx.files.internal("objects.png"));
        Array<TextureRegion> spinFrames = new Array<>(TextureRegion.class);
        for (int col = 0; col < frameCount; col++) {
            spinFrames.add(new TextureRegion(textureSheet, startX + col * frameWidth, rowY, frameWidth, frameHeight));
        }
        spinAnimation = new Animation<>(0.25f, spinFrames);
    }

    /**
     * Marks the collectible as picked up.
     */
    public void markPickedUp() {
        this.pickedUp = true;
    }

    /**
     * Returns whether the collectible has been picked up.
     *
     * @return {@code true} if picked up
     */
    public boolean getPickedUp() {
        return pickedUp;
    }

    /**
     * Draws the collectible's spin animation.
     *
     * @param batch sprite batch
     * @param parentAlpha parent alpha
     */
    @Override
    public void draw(Batch batch, float parentAlpha) {
        batch.draw(spinAnimation.getKeyFrame(animationTime, true), getX(), getY(), getWidth(), getHeight());
    }

    /**
     * Updates the collectible and handles pickup shrink animation.
     *
     * @param delta frame delta time
     */
    @Override
    public void act(float delta) {
        super.act(delta);

        // For now, it's ok if they all have the same shrink animation, later if we got time, maybe we can give them seperate animatinos
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

    /**
     * Updates collision bounds to cover the full tile.
     */
    @Override
    protected void updateBounds() {
        // Use full tile bounds for collectibles to make pickup/exit collisions reliable.
        bounds.set(getX(), getY(), getWidth(), getHeight());
    }
}
