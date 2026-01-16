package de.tum.cit.fop.maze.entity.collectible;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;
import de.tum.cit.fop.maze.entity.Entity;
import de.tum.cit.fop.maze.entity.Player;
import de.tum.cit.fop.maze.system.PointManager;

public class Collectible extends Actor {
    protected final Rectangle bounds = new Rectangle();
    protected Player player;
    protected final PointManager pointManager;
    protected Animation<TextureRegion> spinAnimation;
    private boolean pickedUp = false;
    private int pickUpFrameCounter = 32;
    protected float animationTime;

    private boolean addedToStageFired = false;

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

    protected void markPickedUp() {
        this.pickedUp = true;
    }

    public boolean getPickedUp() {
        return pickedUp;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (spinAnimation != null) {
            batch.draw(spinAnimation.getKeyFrame(animationTime, true), getX(), getY(), getWidth(), getHeight());
        }
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        animationTime += delta;

        if (!addedToStageFired && getStage() != null) {
            addedToStageFired = true;
            onAddedToStage();
        }

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

        //These values are calculated to account for the fact that the heart texture isnt fully 16x16
        //When Nicolas will give us the new textures this will have to be changed.
        bounds.set(getX() + 0.25f,getY() + 0.5f, getWidth() / 2 ,getHeight() / 4);
        if(checkCollisionsWithPlayer()) { this.collision(); }
    }

    protected void onAddedToStage() {
        Stage stage = getStage();

        for (Actor actor : stage.getActors()) {
            if (actor instanceof Player pl) {
                player = pl;
                return;
            }
        }

        throw new RuntimeException("Player must be added before Collectibles");
    }

    protected boolean checkCollisionsWithPlayer() {
        return bounds.overlaps(new Rectangle(player.getX(), player.getY(), player.getWidth(), player.getHeight()));
    }

    // This class is meant to be overiden by all classes that extend Collectible
    protected void collision() {
        return;
    }
}
