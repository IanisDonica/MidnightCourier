package de.tum.cit.fop.maze.entity.collectible;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import de.tum.cit.fop.maze.system.PointManager;

/**
 * Collectible key required to open the exit.
 */
public class Key extends Collectible {
    /**
     * Creates a key collectible.
     *
     * @param x spawn x position
     * @param y spawn y position
     * @param pointManager point manager for scoring
     */
    public Key(float x, float y, PointManager pointManager) {
        super(x, y, 1, 1, pointManager);
        int variantCount = 0;
        while (true) {
            int next = variantCount + 1;
            if (Gdx.files.internal("key" + next + ".png").exists()) {
                variantCount = next;
                continue;
            }
            break;
        }
        if (variantCount > 0) {
            int pick = MathUtils.random(1, variantCount);
            Texture texture = new Texture(Gdx.files.internal("key" + pick + ".png"));
            Array<TextureRegion> frames = new Array<>(TextureRegion.class);
            frames.add(new TextureRegion(texture));
            spinAnimation = new Animation<>(1f, frames, Animation.PlayMode.NORMAL);
            animationTime = 0f;
        } else {
            initSpinAnimation(64, 48, 4);
        }
    }

    /**
     * Handles player collision by awarding points and granting the key.
     */
    @Override
    protected void collision() {
        if (!this.getPickedUp()) {
            pointManager.add(2000);
            player.playSoundEffect("pickup.wav", 1f);
            player.pickupKey();
            markPickedUp();
        }
    }
}
