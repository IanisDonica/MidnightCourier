package de.tum.cit.fop.maze.system;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

public class DriftyMovementController {

    private float acceleration = 2f;
    private float maxSpeed = 3f;
    private float friction = 0.999f;         // 1 means no friction, 0 is maximum friction

    public final Vector2 velocity = new Vector2();
    private float orientation = 90f;        // Direction facing in degrees (90 = up)

    public DriftyMovementController() {
        this.velocity.setZero();
        this.orientation = 90f;     // Start facing up
    }

    public void update(float deltaTime, boolean moveUp, boolean moveDown, boolean moveLeft, boolean moveRight, boolean sprinting) {
        applyForces(deltaTime, moveUp, moveDown, moveLeft, moveRight, sprinting);
        updateOrientation();
        applyFriction();
    }


    private void updateOrientation() {
        if (velocity.len() > 0.1f) {
            // Face the direction of movement
            orientation = velocity.angleDeg();
        }
    }

    private void applyForces(float deltaTime, boolean moveUp, boolean moveDown, boolean moveLeft, boolean moveRight, boolean sprinting) {
        float forceStrength = acceleration;
        if (sprinting) {
            forceStrength *= 1.5f;
        }

        if (moveUp) {
            velocity.y += forceStrength * deltaTime;  // Force upward (+Y)
        }
        if (moveDown) {
            velocity.y -= forceStrength * deltaTime;  // Force downward (-Y)
        }
        if (moveRight) {
            velocity.x += forceStrength * deltaTime;  // Force right (+X)
        }
        if (moveLeft) {
            velocity.x -= forceStrength * deltaTime;  // Force left (-X)
        }

        float maxSpeedActual = maxSpeed;
        if (sprinting) {
            maxSpeedActual *= 1.5f;
        }
        if (velocity.len() > maxSpeedActual) {
            velocity.setLength(maxSpeedActual);
        }
    }

    private void applyFriction() {
        if (velocity.len() < 0.01f) {
            velocity.setZero();
        } else {
            velocity.scl(friction);
        }
    }

    private Vector2 getDirectionVector() {
        float radians = orientation * MathUtils.degreesToRadians;
        return new Vector2(MathUtils.cos(radians), MathUtils.sin(radians));
    }

    public Vector2 getVelocity() {
        return velocity.cpy();
    }

    public float getOrientation() {
        return orientation;
    }

    public float getSpeed() {
        return velocity.len();
    }

    // ============ SETTERS ============

    public void setAcceleration(float acceleration) {
        this.acceleration = acceleration;
    }

    public void setMaxSpeed(float maxSpeed) {
        this.maxSpeed = maxSpeed;
    }

    public void setFriction(float friction) {
        this.friction = friction;
    }

    public void reset() {
        velocity.setZero();
        orientation = 90f;
    }
}
