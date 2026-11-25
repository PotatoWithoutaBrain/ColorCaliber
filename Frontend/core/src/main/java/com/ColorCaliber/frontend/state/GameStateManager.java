package com.ColorCaliber.frontend.state;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;

public class GameStateManager {

    private GameState currentState;

    public GameStateManager() {
        this.currentState = null;
    }

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

    public void renderWorldShapes(ShapeRenderer shapeRenderer) {
        if (currentState != null) {
            currentState.renderWorldShapes(shapeRenderer);
        }
    }

    public void renderWorldSprites(SpriteBatch spriteBatch) {
        if (currentState != null) {
            currentState.renderWorldSprites(spriteBatch);
        }
    }

    public void renderUiShapes(ShapeRenderer shapeRenderer) {
        if (currentState != null) {
            currentState.renderUiShapes(shapeRenderer);
        }
    }

    public void renderUiSprites(SpriteBatch spriteBatch, BitmapFont font, GlyphLayout glyphLayout) {
        if (currentState != null) {
            currentState.renderUiSprites(spriteBatch, font, glyphLayout);
        }
    }
}
