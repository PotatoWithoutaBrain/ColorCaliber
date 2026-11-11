package com.ColorCaliber.frontend;

import com.ColorCaliber.frontend.entity.Bullet;
import com.ColorCaliber.frontend.entity.Enemy;
import com.ColorCaliber.frontend.entity.Player;
import com.ColorCaliber.frontend.manager.GameManager;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.List;

/**
 * Barebones Main entry point:
 * - No UI / HUD / text
 * - No aiming (player only shoots straight up)
 * - 3 stationary red enemies shooting straight down
 * - Green and Red boxes only
 * - Runs continuously with no game over or victory screens
 */
public class Main extends ApplicationAdapter {
    private static final float MAX_DELTA = 0.05f;

    private OrthographicCamera camera;
    private Viewport viewport;
    private ShapeRenderer shapeRenderer;
    private GameManager gameManager;

    @Override
    public void create() {
        camera = new OrthographicCamera();
        viewport = new FitViewport(GameManager.WORLD_WIDTH, GameManager.WORLD_HEIGHT, camera);
        viewport.apply();
        camera.position.set(GameManager.WORLD_WIDTH * 0.5f, GameManager.WORLD_HEIGHT * 0.5f, 0f);
        camera.update();

        shapeRenderer = new ShapeRenderer();

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
        // Discrete press or continuous hold to shoot straight up (no mouse aiming)
        boolean shootJustPressed = Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)
            || Gdx.input.isKeyJustPressed(Input.Keys.SPACE);
        boolean shootHeld = Gdx.input.isButtonPressed(Input.Buttons.LEFT)
            || Gdx.input.isKeyPressed(Input.Keys.SPACE);

        gameManager.getPlayer().setInput(shootJustPressed, shootHeld);
    }

    private void update(float delta) {
        gameManager.update(delta);
    }

    private void renderGame() {
        ScreenUtils.clear(0.07f, 0.08f, 0.11f, 1f);

        viewport.apply();
        camera.update();

        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // 1. Render Colour Grid Arena (Green and Red boxes)
        gameManager.getGridArena().render(shapeRenderer);

        // 2. Render Player (Green box)
        gameManager.getPlayer().render(shapeRenderer);

        // 3. Render active Enemies (Red boxes)
        List<Enemy> enemies = gameManager.getEnemyPool().getActiveObjects();
        for (int i = 0; i < enemies.size(); i++) {
            enemies.get(i).render(shapeRenderer);
        }

        // 4. Render active Bullets
        List<Bullet> bullets = gameManager.getBulletPool().getActiveObjects();
        for (int i = 0; i < bullets.size(); i++) {
            bullets.get(i).render(shapeRenderer);
        }

        shapeRenderer.end();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void dispose() {
        shapeRenderer.dispose();
    }
}
