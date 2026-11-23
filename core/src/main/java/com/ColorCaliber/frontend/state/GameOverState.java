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
 * Concrete GameState representing the Game Over state.
 * Displays a dark overlay, the Game Over title, score summary, and listens for the 'R' restart input.
 */
public class GameOverState implements GameState {

    private final GameManager gameManager;

    public GameOverState(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    @Override
    public void enter() {
        AudioManager.getInstance().stopMusic();
        AudioManager.getInstance().playPlayerDeathSound();
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

        // Semi-transparent dark overlay
        shapeRenderer.setColor(0f, 0f, 0f, 0.68f);
        shapeRenderer.rect(0f, 0f, GameManager.WORLD_WIDTH, GameManager.WORLD_HEIGHT);
    }

    @Override
    public void renderSprites(SpriteBatch spriteBatch, BitmapFont font, GlyphLayout glyphLayout) {
        // Render Player in fallen state
        gameManager.getPlayer().render(spriteBatch);

        // Game Over Title
        font.getData().setScale(2.6f);
        font.setColor(1f, 0.25f, 0.25f, 1f);
        String title = "GAME OVER";
        glyphLayout.setText(font, title);
        font.draw(spriteBatch, title,
            (GameManager.WORLD_WIDTH - glyphLayout.width) * 0.5f,
            GameManager.WORLD_HEIGHT * 0.58f);

        // Score summary
        font.getData().setScale(1.2f);
        font.setColor(Color.LIGHT_GRAY);
        String scoreText = "Final Score: " + gameManager.getFinalScore() + " | High Score: " + gameManager.getHighScore();
        glyphLayout.setText(font, scoreText);
        font.draw(spriteBatch, scoreText,
            (GameManager.WORLD_WIDTH - glyphLayout.width) * 0.5f,
            GameManager.WORLD_HEIGHT * 0.49f);

        // Restart Prompt
        font.getData().setScale(1.4f);
        font.setColor(Color.WHITE);
        String prompt = "Press R to Restart";
        glyphLayout.setText(font, prompt);
        font.draw(spriteBatch, prompt,
            (GameManager.WORLD_WIDTH - glyphLayout.width) * 0.5f,
            GameManager.WORLD_HEIGHT * 0.40f);
    }

    @Override
    public void exit() {
    }
}
