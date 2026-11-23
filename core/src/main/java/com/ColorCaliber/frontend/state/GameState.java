package com.ColorCaliber.frontend.state;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;

/**
 * State Pattern interface defining the lifecycle, input handling,
 * updating, and rendering routines for distinct game states in ColorCaliber.
 */
public interface GameState {

    /**
     * Called when transitioning into this state.
     */
    void enter();

    /**
     * Processes state-specific user input.
     *
     * @param mouseWorldPos The current world coordinates of the cursor
     */
    void handleInput(Vector3 mouseWorldPos);

    /**
     * Updates state logic for the current frame.
     *
     * @param delta Elapsed time in seconds since the last frame
     */
    void update(float delta);

    /**
     * Renders vector geometries and overlays for this state using {@link ShapeRenderer}.
     */
    void renderShapes(ShapeRenderer shapeRenderer);

    /**
     * Renders textures, animated sprites, and HUD/status text for this state using {@link SpriteBatch}.
     */
    void renderSprites(SpriteBatch spriteBatch, BitmapFont font, GlyphLayout glyphLayout);

    /**
     * Called when transitioning out of this state.
     */
    void exit();
}
