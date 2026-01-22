package de.tum.cit.fop.maze.system;

import java.io.Serializable;

public class EnemyData implements Serializable {
    public float x, y;

    public EnemyData(float x, float y) {
        this.x = x;
        this.y = y;
    }
}
