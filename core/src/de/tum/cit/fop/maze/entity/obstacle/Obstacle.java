package de.tum.cit.fop.maze.entity.obstacle;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;
import de.tum.cit.fop.maze.entity.Player;

public class Obstacle extends Actor {
    //THis entire class is more or less a copy of Collectible
    //TODO merge them
    private Animation<TextureRegion> animation;
    private float animationTime;
    protected final Rectangle bounds = new Rectangle();
    private boolean addedToStageFired = false;
    protected Player player;

    public Obstacle(float x, float y, int w, int h, int textureOffsetX, int textureOffsetY, float animationTime) {
        setPosition(x, y);
        setSize(w, h);
        this.animationTime = animationTime;
        initAnimation(textureOffsetX, textureOffsetY);

        //Only works for Static shit
        //TODO generalize it and also remove redunatant code with Collectible
        bounds.set(getX() + 0.25f,getY() + 0.5f, getWidth() / 2 ,getHeight() / 4);
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
        animationTime += delta;

        if (!addedToStageFired && getStage() != null) {
            addedToStageFired = true;
            onAddedToStage();
        }

        checkCollision();
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

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (animation != null) {
            batch.draw(animation.getKeyFrame(animationTime, true), getX(), getY(), getWidth(), getHeight());
        }
    }

    public void checkCollision() {
        if (bounds.overlaps(new Rectangle(player.getX(), player.getY(), player.getWidth(), player.getHeight()))) {
            if (!player.isStunned()) {
                player.damage(1);
            }
        }
    }
}
