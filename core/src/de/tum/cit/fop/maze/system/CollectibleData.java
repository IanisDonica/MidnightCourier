package de.tum.cit.fop.maze.system;

import java.io.Serializable;

/**
 * Serializable data describing a collectible's state.
 */
public class CollectibleData implements Serializable {
    /** Original x coordinate (used as part of the id). */
    public float x, y; // Original coordinates as ID
    /** Whether the collectible was picked up. */
    public boolean pickedUp;

    /**
     * Creates a collectible data record.
     *
     * @param x original x coordinate
     * @param y original y coordinate
     * @param pickedUp whether the collectible has been picked up
     */
    public CollectibleData(float x, float y, boolean pickedUp) {
        this.x = x;
        this.y = y;
        this.pickedUp = pickedUp;
    }
}
