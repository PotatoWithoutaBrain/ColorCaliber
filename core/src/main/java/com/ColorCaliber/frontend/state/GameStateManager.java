package com.ColorCaliber.frontend.state;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;

/**
 * Context manager for the State Pattern.
 * Coordinates transitions between {@link GameState} implementations and delegates
 * lifecycle, input, update, and render calls to the active state.
 */
public class GameStateManager {

    private GameState currentState;

    public GameStateManager() {
        this.currentState = null;
    }

    /**
     * Transitions to a new state, executing exit() on the previous state
     * and enter() on the new state.
     */
    public void setState(GameState newState) {
        if (currentState != null) {
            currentState.exit();
        }
        this.currentState = newState;
        if (currentState != null) {
            currentState.enter();
        }
    }

    public GameState getCurrentState() {
        return currentState;
    }

    public void handleInput(Vector3 mouseWorldPos) {
        if (currentState != null) {
            currentState.handleInput(mouseWorldPos);
        }
    }

    public void update(float delta) {
        if (currentState != null) {
            currentState.update(delta);
        }
    }

    public void renderShapes(ShapeRenderer shapeRenderer) {
        if (currentState != null) {
            currentState.renderShapes(shapeRenderer);
        }
    }

    public void renderSprites(SpriteBatch spriteBatch, BitmapFont font, GlyphLayout glyphLayout) {
        if (currentState != null) {
            currentState.renderSprites(spriteBatch, font, glyphLayout);
        }
    }
}
