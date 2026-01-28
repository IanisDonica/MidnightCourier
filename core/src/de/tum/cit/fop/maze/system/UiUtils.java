package de.tum.cit.fop.maze.system;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;

public final class UiUtils {
    private UiUtils() {
    }

    // This will get reused in multiple screens so it's better to have ti as a static method that can be called in any other class

    // This is actually more or the same as my fog of war openGL code (it's not a circle when height != width tho)
    // but on the CPU, however it only runs once so it is never going to be a real issue, its also easier to work with
    public static Texture buildVignetteTexture(int width, int height, float maxAlpha) {
        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        float cx = width / 2f, cy = height / 2f;  // center X/Y
        float maxDist = (float) Math.sqrt(cx * cx + cy * cy); // So it can be normalized to a 0-1 range later
        float inner = maxDist * 0.45f; // Inner radius where no effect will be applied (alpha will be 0)

        // Go through each pixel, compute their distance to the center, if they are inside the
        // inner radius, dont draw anything, otherwise compute the alpha value (darkness)
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                float dx = x - cx, dy = y - cy; // delta (to center) of X/Y
                float dist = (float) Math.sqrt(dx * dx + dy * dy); // distance to center
                float t = (dist - inner) / (maxDist - inner);
                if (t < 0f) {
                    // t wll be less than 0 when its in the inner radius
                    t = 0f;
                } else if (t > 1f) {
                    // fully dark, clamp it to 1
                    t = 1f;
                }
                float alpha = maxAlpha * t * t;
                pixmap.setColor(0f, 0f, 0f, alpha);
                pixmap.drawPixel(x, y);
            }
        }
        Texture texture = new Texture(pixmap);
        pixmap.dispose(); // memeory leak if you dont do this
        return texture;
    }
}
