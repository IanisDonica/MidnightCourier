package de.tum.cit.fop.maze.entity.collectible;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import de.tum.cit.fop.maze.system.PointManager;

/**
 * Collectible that grants a temporary speed boost.
 */
public class EnergyDrink extends Collectible {
    /**
     * Creates an energy drink collectible.
     *
     * @param x spawn x position
     * @param y spawn y position
     * @param pointManager point manager for scoring
     */
    public EnergyDrink(float x, float y, PointManager pointManager) {
        super(x, y, 1, 1, pointManager);
        initEnergyDrinkAnimation();
    }

    /**
     * Initializes the animated sprite for the energy drink.
     */
    private void initEnergyDrinkAnimation() {
        Texture textureSheet = new Texture(Gdx.files.internal("energy-cans/energy-blau-animation.png"));
        Array<TextureRegion> spinFrames = new Array<>(TextureRegion.class);
        int frameWidth = 32, frameHeight = 32;

        for (int index = 0; index < 16; index++) {
            int col = index % 4; // 4 columns
            int row = index / 4; // 4 frames per col
            spinFrames.add(new TextureRegion(textureSheet,col * frameWidth,row * frameHeight, frameWidth, frameHeight));
        }
        spinAnimation = new Animation<>(0.25f, spinFrames);
    }

    /**
     * Handles collision by applying the drink effect and awarding points.
     */
    @Override
    protected void collision() {
        if (!this.getPickedUp()) {
            this.player.drinkEnergyDrink();
            pointManager.add(50);
            markPickedUp();
        }
    }
}
