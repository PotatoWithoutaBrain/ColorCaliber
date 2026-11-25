package com.ColorCaliber.frontend.state;

import com.ColorCaliber.frontend.entity.Bullet;
import com.ColorCaliber.frontend.entity.Enemy;
import com.ColorCaliber.frontend.entity.Player;
import com.ColorCaliber.frontend.manager.AudioManager;
import com.ColorCaliber.frontend.manager.GameManager;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;

import java.util.List;

public class PlayingState implements GameState {

    private final GameManager gameManager;

    public PlayingState(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    @Override
    public void enter() {
        AudioManager.getInstance().playLevelMusic();
    }

    @Override
    public void handleInput(Vector3 mouseWorldPos) {
        float moveX = 0f;
        float moveY = 0f;

        if (Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT)) moveX -= 1f;
        if (Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT)) moveX += 1f;
        if (Gdx.input.isKeyPressed(Input.Keys.S) || Gdx.input.isKeyPressed(Input.Keys.DOWN)) moveY -= 1f;
        if (Gdx.input.isKeyPressed(Input.Keys.W) || Gdx.input.isKeyPressed(Input.Keys.UP)) moveY += 1f;

        if (moveX != 0f && moveY != 0f) {
            float invLen = 1f / 1.41421356f;
            moveX *= invLen;
            moveY *= invLen;
        }

        boolean shootJustPressed = Gdx.input.isButtonJustPressed(Input.Buttons.LEFT);
        boolean shootHeld = Gdx.input.isButtonPressed(Input.Buttons.LEFT);
        boolean dodgeRollPressed = Gdx.input.isKeyJustPressed(Input.Keys.SPACE);

        gameManager.getPlayer().setInput(moveX, moveY, mouseWorldPos.x, mouseWorldPos.y,
            shootJustPressed, shootHeld, dodgeRollPressed);

        if (gameManager.isWeaponChoiceAvailable()) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) {
                gameManager.selectWeaponUpgrade(1);
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)) {
                gameManager.selectWeaponUpgrade(2);
            }
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.F1)) {
            gameManager.jumpToLevel(1);
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.F2)) {
            gameManager.jumpToLevel(2);
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.F3)) {
            gameManager.jumpToLevel(3);
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.F4)) {
            gameManager.onPlayerDeath();
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.F5)) {
            gameManager.onVictory();
        }
    }

    @Override
    public void update(float delta) {
        gameManager.updateGameplay(delta);
    }

    @Override
    public void renderWorldShapes(ShapeRenderer shapeRenderer) {
        gameManager.getGridArena().render(shapeRenderer);

        List<Enemy> enemies = gameManager.getEnemyPool().getActiveObjects();
        for (int i = 0; i < enemies.size(); i++) {
            enemies.get(i).render(shapeRenderer);
        }

        List<Bullet> bullets = gameManager.getBulletPool().getActiveObjects();
        for (int i = 0; i < bullets.size(); i++) {
            bullets.get(i).render(shapeRenderer);
        }

        Player player = gameManager.getPlayer();
        float shadowW = player.getWidth() * (player.isDodgeRolling() ? 1.25f : 0.90f);
        float shadowH = player.getHeight() * (player.isDodgeRolling() ? 0.28f : 0.35f);
        shapeRenderer.setColor(0f, 0f, 0f, player.isDodgeRolling() ? 0.35f : 0.40f);
        shapeRenderer.ellipse(
            player.getCenterX() - shadowW * 0.5f,
            player.getY() + 2f - shadowH * 0.5f,
            shadowW,
            shadowH
        );

        player.render(shapeRenderer);
    }

    @Override
    public void renderWorldSprites(SpriteBatch spriteBatch) {
        gameManager.getPlayer().render(spriteBatch);
    }

    @Override
    public void renderUiShapes(ShapeRenderer shapeRenderer) {
        Player player = gameManager.getPlayer();
        float hudBarX = 24f;
        float hudBarY = GameManager.WORLD_HEIGHT - 46f;
        float hudBarW = 240f;
        float hudBarH = 22f;
        float hpRatio = player.getMaxHp() > 0f
            ? MathUtils.clamp(player.getHp() / player.getMaxHp(), 0f, 1f)
            : 0f;

        shapeRenderer.setColor(0.08f, 0.09f, 0.12f, 0.9f);
        shapeRenderer.rect(hudBarX - 2f, hudBarY - 2f, hudBarW + 4f, hudBarH + 4f);
        shapeRenderer.setColor(0.25f, 0.08f, 0.08f, 0.9f);
        shapeRenderer.rect(hudBarX, hudBarY, hudBarW, hudBarH);
        shapeRenderer.setColor(1f - hpRatio * 0.75f, 0.2f + hpRatio * 0.75f, 0.25f, 1f);
        shapeRenderer.rect(hudBarX, hudBarY, hudBarW * hpRatio, hudBarH);
    }

    @Override
    public void renderUiSprites(SpriteBatch spriteBatch, BitmapFont font, GlyphLayout glyphLayout) {
        Player player = gameManager.getPlayer();

        font.setColor(Color.WHITE);
        font.getData().setScale(1.1f);
        float hudBarX = 24f;
        float hudBarY = GameManager.WORLD_HEIGHT - 46f;
        String hpText = "HP: " + (int) Math.ceil(player.getHp()) + " / " + (int) player.getMaxHp();
        font.draw(spriteBatch, hpText, hudBarX + 8f, hudBarY + 17f);

        font.getData().setScale(1.15f);
        font.setColor(0.35f, 0.92f, 1f, 1f);
        String diamondText = "Diamonds: " + gameManager.getDiamondsCollected() + " / " + gameManager.getTotalDiamonds();
        font.draw(spriteBatch, diamondText, hudBarX, hudBarY - 12f);

        font.setColor(Color.GOLD);
        String scoreText = "Score: " + gameManager.getScore();
        font.draw(spriteBatch, scoreText, hudBarX, hudBarY - 36f);

        font.getData().setScale(1.2f);
        font.setColor(1.0f, 0.835f, 0.310f, 1f);
        String levelText = "LVL " + gameManager.getCurrentRound() + " / " + GameManager.MAX_ROUNDS;
        glyphLayout.setText(font, levelText);
        font.draw(spriteBatch, levelText, GameManager.WORLD_WIDTH - glyphLayout.width - 24f, GameManager.WORLD_HEIGHT - 26f);

        String notification = gameManager.getStatusNotification();
        if (notification != null) {
            font.getData().setScale(1.2f);
            glyphLayout.setText(font, notification);
            float notifX = (GameManager.WORLD_WIDTH - glyphLayout.width) * 0.5f;
            float notifY = GameManager.WORLD_HEIGHT - 14f;
            font.setColor(0.05f, 0.02f, 0.12f, 0.85f);
            font.draw(spriteBatch, notification, notifX + 1.5f, notifY - 1.5f);
            font.setColor(Color.YELLOW);
            font.draw(spriteBatch, notification, notifX, notifY);
        }
    }

    @Override
    public void exit() {
    }
}
