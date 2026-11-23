package com.ColorCaliber.frontend;

import com.ColorCaliber.frontend.manager.AudioManager;
import com.ColorCaliber.frontend.manager.GameManager;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

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

        gameManager.getStateManager().handleInput(mouseWorldPos);
    }

    private void update(float delta) {
        gameManager.update(delta);
    }

    private void renderGame() {
        ScreenUtils.clear(0.07f, 0.08f, 0.11f, 1f);

        viewport.apply();
        camera.update();

        // 1. Render Vector Geometries & Overlays via active GameState
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        gameManager.getStateManager().renderShapes(shapeRenderer);

        shapeRenderer.end();

        // 2. Render Sprites, HUD Text & Crosshair via active GameState
        spriteBatch.setProjectionMatrix(camera.combined);
        spriteBatch.begin();

        gameManager.getStateManager().renderSprites(spriteBatch, font, glyphLayout);

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
        AudioManager.getInstance().dispose();
    }
}
