package de.tum.cit.fop.maze.system;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

public class DriftyMovementController {
    public Vector2 velocity = new Vector2();
    private float acceleration = 2f;
    private float maxSpeed = 3f;
    private float friction = 0.999f; // 0 means maximum friction, 1 is no friction
    private float rotationSpeed = 200f;     // Degrees per second for smooth rotation to input direction
    private float orientation = 90f;
    private float targetOrientation = 90f;

    public DriftyMovementController() {
        this.velocity.setZero();
        this.orientation = 90f;
        this.targetOrientation = 90f;
    }

    /**
     * Update movement each frame
     */
    public void update(float deltaTime, boolean moveUp, boolean moveDown, boolean moveLeft, boolean moveRight, boolean sprinting) {
        applyForces(deltaTime, moveUp, moveDown, moveLeft, moveRight, sprinting);
        updateTargetOrientation(moveUp, moveDown, moveLeft, moveRight);
        updateOrientation(deltaTime);
        applyFriction();
    }

    private void updateTargetOrientation(boolean moveUp, boolean moveDown, boolean moveLeft, boolean moveRight) {
        if (moveUp || moveDown || moveLeft || moveRight) {
            float inputX = 0;
            float inputY = 0;

            if (moveUp) inputY += 1;
            if (moveDown) inputY -= 1;
            if (moveLeft) inputX -= 1;
            if (moveRight) inputX += 1;

            if (inputX != 0 || inputY != 0) {
                targetOrientation = new Vector2(inputX, inputY).angleDeg();
            }
        }
    }

    private void updateOrientation(float deltaTime) {
        float rotationAmount = rotationSpeed * deltaTime;

        float angleDiff = targetOrientation - orientation;

        while (angleDiff > 180) angleDiff -= 360;
        while (angleDiff < -180) angleDiff += 360;

        if (Math.abs(angleDiff) > rotationAmount) {
            orientation += Math.signum(angleDiff) * rotationAmount;
        } else {
            orientation = targetOrientation;
        }

        orientation = orientation % 360f;
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

        // Cap speed at maxSpeed
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
            velocity.setZero();  // Stop if speed is negligible
        } else {
            velocity.scl(friction);  // Apply friction decay - very high friction = extreme drift
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

    public float getInputAngle() {
        return targetOrientation;
    }

    public float getSpeed() {
        return velocity.len();
    }

    public void setAcceleration(float acceleration) {
        this.acceleration = acceleration;
    }

    public void setMaxSpeed(float maxSpeed) {
        this.maxSpeed = maxSpeed;
    }

    public void setFriction(float friction) {
        this.friction = friction;
    }

    public void setRotationSpeed(float rotationSpeed) {
        this.rotationSpeed = rotationSpeed;
    }

    public void reset() {
        velocity.setZero();
        orientation = 90f;
        targetOrientation = 90f;
    }
}
