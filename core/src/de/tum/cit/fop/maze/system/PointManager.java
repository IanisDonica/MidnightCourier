package de.tum.cit.fop.maze.system;

public class PointManager {
    private double points = 0;
    private double timePoints = 100000;
    private float offset = 0;
    private float safetyTime = 5f; // For the first 5 seconds points will not be deducted

    public double getPoints() {
        return points + timePoints;
    }

    public void add(int amount) {
        if (amount < 0) throw new IllegalArgumentException("points need to be non-negative");
        points += amount;
    }

    /*
    At the start they decrease quickly, later on they decrease slower, this is to make it a
     challenge for good players to get a lot of points, yet at the same allow worse players
    to at least get some points.
    */
    public void decreasePoints() {
        double deduct = 400 * Math.ceil(Math.log10(timePoints));
        if (timePoints - deduct > 0) {
            timePoints = timePoints - deduct;
        } else {
            timePoints = 0;
        }
    }

    public void act(float delta) {
        // This might cause the safety time to be slightly bigger than the amount specified,
        // but since its going to be a very very small difference, it's easier to leave it as such
        if (safetyTime > 0) {
            safetyTime -= delta;
        } else {
            offset += delta;
        }
        if (offset > 3) {
            decreasePoints();
            offset = offset % 3;
        }
    }
}
