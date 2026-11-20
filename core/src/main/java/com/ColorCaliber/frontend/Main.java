package com.ColorCaliber.frontend;

import com.ColorCaliber.frontend.entity.Bullet;
import com.ColorCaliber.frontend.entity.Enemy;
import com.ColorCaliber.frontend.entity.Player;
import com.ColorCaliber.frontend.manager.GameManager;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.List;

/**
 * Main game entry point for ColorCaliber:
 * - Controllable 4-directional animated player (WASD / Arrow Keys)
 * - Cursor-tracking aiming & shooting (Left Click / Space)
 * - Custom sprite crosshair (crosshair.png)
 * - Colour Grid Arena
 * - Object pooled enemies & bullets
 */
public class Main extends ApplicationAdapter {
    private static final float MAX_DELTA = 0.05f;
    private static final float CROSSHAIR_SIZE = 32f;

    private OrthographicCamera camera;
    private Viewport viewport;
    private ShapeRenderer shapeRenderer;
    private SpriteBatch spriteBatch;
    private BitmapFont font;
    private GlyphLayout glyphLayout;
    private GameManager gameManager;
    private Texture crosshairTexture;

    private final Vector3 mouseWorldPos = new Vector3();

    @Override
    public void create() {
        camera = new OrthographicCamera();
        viewport = new FitViewport(GameManager.WORLD_WIDTH, GameManager.WORLD_HEIGHT, camera);
        viewport.apply();
        camera.position.set(GameManager.WORLD_WIDTH * 0.5f, GameManager.WORLD_HEIGHT * 0.5f, 0f);
        camera.update();

        shapeRenderer = new ShapeRenderer();
        spriteBatch = new SpriteBatch();
        font = new BitmapFont();
        font.getData().setScale(1.4f);
        glyphLayout = new GlyphLayout();

        // Load custom crosshair PNG
        if (Gdx.files != null && Gdx.files.internal("crosshair.png").exists()) {
            crosshairTexture = new Texture(Gdx.files.internal("crosshair.png"));
            crosshairTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        }

        // Initialize Singleton GameManager
        gameManager = GameManager.getInstance();
    }

    @Override
    public void render() {
        float delta = Math.min(Gdx.graphics.getDeltaTime(), MAX_DELTA);

        processInput();
        update(delta);
        renderGame();
    }

    private void processInput() {
        // Convert screen mouse coordinates to world coordinates via Viewport
        mouseWorldPos.set(Gdx.input.getX(), Gdx.input.getY(), 0f);
        viewport.unproject(mouseWorldPos);

        if (gameManager.getGameState() != GameManager.GameState.PLAYING) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
                gameManager.startNewGame();
            }
            return;
        }

        // Controllable 4-directional movement (WASD or Arrow Keys)
        float moveX = 0f;
        float moveY = 0f;

        if (Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT)) moveX -= 1f;
        if (Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT)) moveX += 1f;
        if (Gdx.input.isKeyPressed(Input.Keys.S) || Gdx.input.isKeyPressed(Input.Keys.DOWN)) moveY -= 1f;
        if (Gdx.input.isKeyPressed(Input.Keys.W) || Gdx.input.isKeyPressed(Input.Keys.UP)) moveY += 1f;

        // Diagonal normalization
        if (moveX != 0f && moveY != 0f) {
            float invLen = 1f / 1.41421356f;
            moveX *= invLen;
            moveY *= invLen;
        }

        // Left Click to shoot towards the cursor
        boolean shootJustPressed = Gdx.input.isButtonJustPressed(Input.Buttons.LEFT);
        boolean shootHeld = Gdx.input.isButtonPressed(Input.Buttons.LEFT);

        // Space bar for dodge roll (1.5 Tiles with invincibility frames)
        boolean dodgeRollPressed = Gdx.input.isKeyJustPressed(Input.Keys.SPACE);

        gameManager.getPlayer().setInput(moveX, moveY, mouseWorldPos.x, mouseWorldPos.y,
            shootJustPressed, shootHeld, dodgeRollPressed);
    }

    private void update(float delta) {
        gameManager.update(delta);
    }

    private void renderGame() {
        ScreenUtils.clear(0.07f, 0.08f, 0.11f, 1f);

        viewport.apply();
        camera.update();

        // 1. Render Grid Arena, Enemies, and Bullets via ShapeRenderer
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        gameManager.getGridArena().render(shapeRenderer);

        List<Enemy> enemies = gameManager.getEnemyPool().getActiveObjects();
        for (int i = 0; i < enemies.size(); i++) {
            enemies.get(i).render(shapeRenderer);
        }

        List<Bullet> bullets = gameManager.getBulletPool().getActiveObjects();
        for (int i = 0; i < bullets.size(); i++) {
            bullets.get(i).render(shapeRenderer);
        }

        // Ground shadow for Player (stretches subtly along ground during dodge roll)
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

        // Fallback shape if sprite texture is not loaded
        gameManager.getPlayer().render(shapeRenderer);

        // Player HUD Health Bar (top-left)
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

        // Game Over overlay
        if (gameManager.getGameState() == GameManager.GameState.GAME_OVER) {
            shapeRenderer.setColor(0f, 0f, 0f, 0.65f);
            shapeRenderer.rect(0f, 0f, GameManager.WORLD_WIDTH, GameManager.WORLD_HEIGHT);
        }

        shapeRenderer.end();

        // 2. Render Player Sprite Animation, HUD Text & Crosshair via SpriteBatch
        spriteBatch.setProjectionMatrix(camera.combined);
        spriteBatch.begin();

        gameManager.getPlayer().render(spriteBatch);

        // HUD Health Text
        font.setColor(Color.WHITE);
        font.getData().setScale(1.1f);
        String hpText = "HP: " + (int) Math.ceil(player.getHp()) + " / " + (int) player.getMaxHp();
        font.draw(spriteBatch, hpText, hudBarX + 8f, hudBarY + 17f);

        // Game Over Text
        if (gameManager.getGameState() == GameManager.GameState.GAME_OVER) {
            font.getData().setScale(2.6f);
            font.setColor(1f, 0.25f, 0.25f, 1f);
            String title = "GAME OVER";
            glyphLayout.setText(font, title);
            font.draw(spriteBatch, title,
                (GameManager.WORLD_WIDTH - glyphLayout.width) * 0.5f,
                GameManager.WORLD_HEIGHT * 0.56f);

            font.getData().setScale(1.4f);
            font.setColor(Color.WHITE);
            String prompt = "Press R to Restart";
            glyphLayout.setText(font, prompt);
            font.draw(spriteBatch, prompt,
                (GameManager.WORLD_WIDTH - glyphLayout.width) * 0.5f,
                GameManager.WORLD_HEIGHT * 0.46f);
        }

        // Render Crosshair PNG at Cursor Position
        if (crosshairTexture != null) {
            spriteBatch.draw(
                crosshairTexture,
                mouseWorldPos.x - CROSSHAIR_SIZE * 0.5f,
                mouseWorldPos.y - CROSSHAIR_SIZE * 0.5f,
                CROSSHAIR_SIZE,
                CROSSHAIR_SIZE
            );
        }

        spriteBatch.end();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void dispose() {
        shapeRenderer.dispose();
        spriteBatch.dispose();
        if (font != null) {
            font.dispose();
        }
        if (crosshairTexture != null) {
            crosshairTexture.dispose();
        }
        if (gameManager != null && gameManager.getPlayer() != null) {
            gameManager.getPlayer().dispose();
        }
    }
}
