package de.tum.cit.fop.maze.system;

import java.io.Serializable;

/**
 * Serializable data describing an enemy's position.
 */
public class EnemyData implements Serializable {
    /** Enemy coordinates. */
    public float x, y;

    /**
     * Creates an enemy data record.
     *
     * @param x enemy x coordinate
     * @param y enemy y coordinate
     */
    public EnemyData(float x, float y) {
        this.x = x;
        this.y = y;
    }
}
