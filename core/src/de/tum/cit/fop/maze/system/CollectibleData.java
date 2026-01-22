package de.tum.cit.fop.maze.system;

import java.io.Serializable;

public class CollectibleData implements Serializable {
    public float x, y; // Original coordinates as ID
    public boolean pickedUp;

    public CollectibleData(float x, float y, boolean pickedUp) {
        this.x = x;
        this.y = y;
        this.pickedUp = pickedUp;
    }
}
