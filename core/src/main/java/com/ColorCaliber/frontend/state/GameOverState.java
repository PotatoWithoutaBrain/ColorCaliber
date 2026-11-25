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
        gameManager.updateVisualEffects(delta);
    }

    @Override
    public void renderWorldShapes(ShapeRenderer shapeRenderer) {
        gameManager.getGridArena().render(shapeRenderer);
    }

    @Override
    public void renderWorldSprites(SpriteBatch spriteBatch) {
        gameManager.getPlayer().render(spriteBatch);
    }

    @Override
    public void renderUiShapes(ShapeRenderer shapeRenderer) {
        shapeRenderer.setColor(0.08f, 0.01f, 0.03f, 0.42f);
        shapeRenderer.rect(0f, 0f, GameManager.WORLD_WIDTH, GameManager.WORLD_HEIGHT);

        float cardW = 540f;
        float cardH = 220f;
        float cardX = (GameManager.WORLD_WIDTH - cardW) * 0.5f;
        float cardY = GameManager.WORLD_HEIGHT * 0.34f;
        shapeRenderer.setColor(0.06f, 0.02f, 0.08f, 0.80f);
        shapeRenderer.rect(cardX, cardY, cardW, cardH);
        shapeRenderer.setColor(1f, 0.16f, 0.28f, 0.85f);
        shapeRenderer.rectLine(cardX, cardY, cardX + cardW, cardY, 2.2f);
        shapeRenderer.rectLine(cardX, cardY + cardH, cardX + cardW, cardY + cardH, 2.2f);
        shapeRenderer.rectLine(cardX, cardY, cardX, cardY + cardH, 2.2f);
        shapeRenderer.rectLine(cardX + cardW, cardY, cardX + cardW, cardY + cardH, 2.2f);
    }

    @Override
    public void renderUiSprites(SpriteBatch spriteBatch, BitmapFont font, GlyphLayout glyphLayout) {
        font.getData().setScale(2.6f);
        font.setColor(1f, 0.25f, 0.28f, 1f);
        String title = "GAME OVER";
        glyphLayout.setText(font, title);
        font.draw(spriteBatch, title,
            (GameManager.WORLD_WIDTH - glyphLayout.width) * 0.5f,
            GameManager.WORLD_HEIGHT * 0.57f);

        font.getData().setScale(1.2f);
        font.setColor(Color.LIGHT_GRAY);
        String scoreText = "Final Score: " + gameManager.getFinalScore() + " | High Score: " + gameManager.getHighScore();
        glyphLayout.setText(font, scoreText);
        font.draw(spriteBatch, scoreText,
            (GameManager.WORLD_WIDTH - glyphLayout.width) * 0.5f,
            GameManager.WORLD_HEIGHT * 0.48f);

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
