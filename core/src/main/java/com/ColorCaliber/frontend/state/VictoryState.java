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
        shapeRenderer.setColor(0.02f, 0.06f, 0.08f, 0.38f);
        shapeRenderer.rect(0f, 0f, GameManager.WORLD_WIDTH, GameManager.WORLD_HEIGHT);

        float cardW = 620f;
        float cardH = 235f;
        float cardX = (GameManager.WORLD_WIDTH - cardW) * 0.5f;
        float cardY = GameManager.WORLD_HEIGHT * 0.35f;
        shapeRenderer.setColor(0.03f, 0.08f, 0.11f, 0.80f);
        shapeRenderer.rect(cardX, cardY, cardW, cardH);
        shapeRenderer.setColor(0.12f, 0.95f, 0.72f, 0.88f);
        shapeRenderer.rectLine(cardX, cardY, cardX + cardW, cardY, 2.2f);
        shapeRenderer.rectLine(cardX, cardY + cardH, cardX + cardW, cardY + cardH, 2.2f);
        shapeRenderer.rectLine(cardX, cardY, cardX, cardY + cardH, 2.2f);
        shapeRenderer.rectLine(cardX + cardW, cardY, cardX + cardW, cardY + cardH, 2.2f);
    }

    @Override
    public void renderUiSprites(SpriteBatch spriteBatch, BitmapFont font, GlyphLayout glyphLayout) {
        font.getData().setScale(2.8f);
        font.setColor(0.25f, 0.98f, 0.65f, 1f);
        String title = "VICTORY!";
        glyphLayout.setText(font, title);
        font.draw(spriteBatch, title,
            (GameManager.WORLD_WIDTH - glyphLayout.width) * 0.5f,
            GameManager.WORLD_HEIGHT * 0.60f);

        font.getData().setScale(1.25f);
        font.setColor(Color.WHITE);
        String stats = String.format("Completion Time: %.2fs | Score: %d | High Score: %d",
            gameManager.getElapsedTime(), gameManager.getFinalScore(), gameManager.getHighScore());
        glyphLayout.setText(font, stats);
        font.draw(spriteBatch, stats,
            (GameManager.WORLD_WIDTH - glyphLayout.width) * 0.5f,
            GameManager.WORLD_HEIGHT * 0.50f);

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
