package com.ColorCaliber.frontend.state;

import com.ColorCaliber.frontend.manager.AudioManager;
import com.ColorCaliber.frontend.manager.GameManager;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;

/**
 * Concrete GameState representing the Victory state when all waves are cleared.
 * Displays a victory overlay, completion statistics, and restart prompt.
 */
public class VictoryState implements GameState {

    private final GameManager gameManager;

    public VictoryState(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    @Override
    public void enter() {
        AudioManager.getInstance().stopMusic();
        AudioManager.getInstance().playUpgradeSound();
    }

    @Override
    public void handleInput(Vector3 mouseWorldPos) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            gameManager.startNewGame();
        }
    }

    @Override
    public void update(float delta) {
    }

    @Override
    public void renderShapes(ShapeRenderer shapeRenderer) {
        // Draw the playing field underneath
        gameManager.getGridArena().render(shapeRenderer);

        // Soft victory gold/dark tint overlay
        shapeRenderer.setColor(0.04f, 0.08f, 0.06f, 0.70f);
        shapeRenderer.rect(0f, 0f, GameManager.WORLD_WIDTH, GameManager.WORLD_HEIGHT);
    }

    @Override
    public void renderSprites(SpriteBatch spriteBatch, BitmapFont font, GlyphLayout glyphLayout) {
        gameManager.getPlayer().render(spriteBatch);

        // Victory Title
        font.getData().setScale(2.8f);
        font.setColor(0.35f, 0.95f, 0.45f, 1f);
        String title = "VICTORY!";
        glyphLayout.setText(font, title);
        font.draw(spriteBatch, title,
            (GameManager.WORLD_WIDTH - glyphLayout.width) * 0.5f,
            GameManager.WORLD_HEIGHT * 0.62f);

        // Statistics
        font.getData().setScale(1.25f);
        font.setColor(Color.WHITE);
        String stats = String.format("Completion Time: %.2fs | Score: %d | High Score: %d",
            gameManager.getElapsedTime(), gameManager.getFinalScore(), gameManager.getHighScore());
        glyphLayout.setText(font, stats);
        font.draw(spriteBatch, stats,
            (GameManager.WORLD_WIDTH - glyphLayout.width) * 0.5f,
            GameManager.WORLD_HEIGHT * 0.51f);

        // Restart Prompt
        font.getData().setScale(1.35f);
        font.setColor(Color.GOLD);
        String prompt = "Press R to Play Again";
        glyphLayout.setText(font, prompt);
        font.draw(spriteBatch, prompt,
            (GameManager.WORLD_WIDTH - glyphLayout.width) * 0.5f,
            GameManager.WORLD_HEIGHT * 0.41f);
    }

    @Override
    public void exit() {
    }
}
