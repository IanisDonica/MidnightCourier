package de.tum.cit.fop.maze.entity.collectible;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import de.tum.cit.fop.maze.system.AchievementManager;
import de.tum.cit.fop.maze.system.PointManager;

/**
 * Drop-off point that completes a delivery when the player has the key.
 */
public class DropOff extends Collectible {
    /**
     * Listener invoked when the drop-off is completed.
     */
    public interface DropOffListener {
        void onDropOff();
    }

    /** Listener to notify on drop-off completion. */
    private final DropOffListener dropOffListener;
    /** Whether the drop-off grants can-leave instead of triggering victory. */
    private final boolean grantsCanLeave;
    /** Whether the drop-off has been triggered. */
    private boolean triggered = false;

    /**
     * Creates a drop-off collectible.
     *
     * @param x spawn x position
     * @param y spawn y position
     * @param pointManager point manager for scoring
     * @param dropOffListener listener to call on completion
     * @param grantsCanLeave whether to grant can-leave instead of triggering victory (to distinguish between the endless and gamescreen loop)
     */
    public DropOff(float x, float y, PointManager pointManager, DropOffListener dropOffListener, boolean grantsCanLeave) {
        super(x, y, 1, 1, pointManager);
        this.dropOffListener = dropOffListener;
        this.grantsCanLeave = grantsCanLeave;
        Texture texture = new Texture(Gdx.files.internal("DropOff.png"));
        Array<TextureRegion> frames = new Array<>(TextureRegion.class);
        frames.add(new TextureRegion(texture));
        spinAnimation = new Animation<>(1f, frames, Animation.PlayMode.NORMAL);
        animationTime = 0f;
    }

    /**
     * Updates visibility based on whether the player can leave.
     *
     * @param delta frame delta time
     */
    @Override
    public void act(float delta) {
        super.act(delta);
        if (player != null) {
            if (grantsCanLeave) {
                setVisible(player.hasKey() && !player.canLeave());
            } else {
                setVisible(player.hasKey());
            }
        }
    }

    /**
     * Handles player collision and completes the drop-off if the key is owned.
     */
    @Override
    protected void collision() {
        if (triggered) {
            return;
        }
        if (!this.player.hasKey()) {
            return;
        }
        triggered = true;
        if (grantsCanLeave) {
            this.player.clearKey();
            this.player.grantCanLeave();
            markPickedUp();
            return;
        }
        markPickedUp();
        this.pointManager.saveScore(this.player.getHp());
        AchievementManager.incrementProgress("first_delivery", 1);
        AchievementManager.incrementProgress("complete_100_deliveries", 1);
        if (this.pointManager.getLevel() == 5) {
            AchievementManager.incrementProgress("finish_level_5", 1);
        }
        if (dropOffListener != null) {
            dropOffListener.onDropOff();
        }
    }
}
