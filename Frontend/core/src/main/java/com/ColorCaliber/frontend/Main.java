package com.ColorCaliber.frontend;

import com.ColorCaliber.frontend.entity.Player;
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
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class Main extends ApplicationAdapter {
    private static final float MAX_DELTA = 0.05f;
    private static final float CROSSHAIR_SIZE = 32f;

    private static final float STAGE_CAMERA_ZOOM = 1.44f;
    private static final float STAGE_CAMERA_BASE_X = GameManager.WORLD_WIDTH * 0.5f;
    private static final float STAGE_CAMERA_BASE_Y = 535f;
    private static final float MAX_CAM_SWAY_X = 20f;
    private static final float MAX_CAM_SWAY_Y = 10f;

    private OrthographicCamera worldCamera;
    private Viewport worldViewport;
    private OrthographicCamera uiCamera;
    private Viewport uiViewport;

    private ShapeRenderer shapeRenderer;
    private SpriteBatch spriteBatch;
    private BitmapFont font;
    private GlyphLayout glyphLayout;
    private GameManager gameManager;
    private Texture crosshairTexture;

    private final Vector3 mouseWorldPos = new Vector3();
    private final Vector3 mouseUiPos = new Vector3();

    private float smoothParallaxX;
    private float smoothParallaxY;

    @Override
    public void create() {
        uiCamera = new OrthographicCamera();
        uiViewport = new FitViewport(GameManager.WORLD_WIDTH, GameManager.WORLD_HEIGHT, uiCamera);
        uiViewport.apply(true);
        uiCamera.position.set(GameManager.WORLD_WIDTH * 0.5f, GameManager.WORLD_HEIGHT * 0.5f, 0f);
        uiCamera.update();

        worldCamera = new OrthographicCamera();
        worldViewport = new FitViewport(GameManager.WORLD_WIDTH, GameManager.WORLD_HEIGHT, worldCamera);
        worldViewport.apply();
        worldCamera.zoom = STAGE_CAMERA_ZOOM;
        worldCamera.position.set(STAGE_CAMERA_BASE_X, STAGE_CAMERA_BASE_Y, 0f);
        worldCamera.update();

        shapeRenderer = new ShapeRenderer(16384);
        spriteBatch = new SpriteBatch();
        font = new BitmapFont();
        font.getData().setScale(1.4f);
        glyphLayout = new GlyphLayout();

        if (Gdx.files != null && Gdx.files.internal("crosshair.png").exists()) {
            crosshairTexture = new Texture(Gdx.files.internal("crosshair.png"));
            crosshairTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        }

        gameManager = GameManager.getInstance();
        smoothParallaxX = 0f;
        smoothParallaxY = 0f;
    }

    @Override
    public void render() {
        float delta = Math.min(Gdx.graphics.getDeltaTime(), MAX_DELTA);

        updateCameraParallax(delta);
        processInput();
        update(delta);
        renderGame();
    }

    private void updateCameraParallax(float delta) {
        Player player = gameManager.getPlayer();
        float arenaCenterX = GameManager.ARENA_WIDTH * 0.5f;
        float arenaCenterY = GameManager.ARENA_HEIGHT * 0.5f;

        float targetOffsetX = player.getCenterX() - arenaCenterX;
        float targetOffsetY = player.getCenterY() - arenaCenterY;

        float lerpAlpha = Math.min(1f, delta * 5.0f);
        smoothParallaxX = MathUtils.lerp(smoothParallaxX, targetOffsetX, lerpAlpha);
        smoothParallaxY = MathUtils.lerp(smoothParallaxY, targetOffsetY, lerpAlpha);

        float swayX = MathUtils.clamp(smoothParallaxX * 0.035f, -MAX_CAM_SWAY_X, MAX_CAM_SWAY_X);
        float swayY = MathUtils.clamp(smoothParallaxY * 0.025f, -MAX_CAM_SWAY_Y, MAX_CAM_SWAY_Y);

        worldCamera.zoom = STAGE_CAMERA_ZOOM;
        worldCamera.position.set(STAGE_CAMERA_BASE_X + swayX, STAGE_CAMERA_BASE_Y + swayY, 0f);
        worldCamera.update();
    }

    private void processInput() {
        int screenX = Gdx.input.getX();
        int screenY = Gdx.input.getY();

        mouseWorldPos.set(screenX, screenY, 0f);
        worldViewport.unproject(mouseWorldPos);

        mouseUiPos.set(screenX, screenY, 0f);
        uiViewport.unproject(mouseUiPos);

        gameManager.getStateManager().handleInput(mouseWorldPos);
    }

    private void update(float delta) {
        gameManager.update(delta);
    }

    private void renderGame() {
        ScreenUtils.clear(0.051f, 0.024f, 0.122f, 1f);

        uiViewport.apply();
        uiCamera.update();
        enableAlphaBlending();
        shapeRenderer.setProjectionMatrix(uiCamera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        gameManager.getSynthwaveBackground().renderBackdrop(shapeRenderer, smoothParallaxX, smoothParallaxY);
        shapeRenderer.end();

        worldViewport.apply();
        worldCamera.update();
        enableAlphaBlending();
        shapeRenderer.setProjectionMatrix(worldCamera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        gameManager.getSynthwaveBackground().renderArenaUnderlay(shapeRenderer, gameManager.getGridArena());
        gameManager.getStateManager().renderWorldShapes(shapeRenderer);
        shapeRenderer.end();

        spriteBatch.setProjectionMatrix(worldCamera.combined);
        spriteBatch.begin();
        gameManager.getStateManager().renderWorldSprites(spriteBatch);
        spriteBatch.end();

        uiViewport.apply();
        uiCamera.update();
        enableAlphaBlending();
        shapeRenderer.setProjectionMatrix(uiCamera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        gameManager.getStateManager().renderUiShapes(shapeRenderer);
        shapeRenderer.end();

        spriteBatch.setProjectionMatrix(uiCamera.combined);
        spriteBatch.begin();
        gameManager.getStateManager().renderUiSprites(spriteBatch, font, glyphLayout);

        if (crosshairTexture != null) {
            spriteBatch.draw(
                crosshairTexture,
                mouseUiPos.x - CROSSHAIR_SIZE * 0.5f,
                mouseUiPos.y - CROSSHAIR_SIZE * 0.5f,
                CROSSHAIR_SIZE,
                CROSSHAIR_SIZE
            );
        }
        spriteBatch.end();
    }

    private void enableAlphaBlending() {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFuncSeparate(
            GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA,
            GL20.GL_ONE, GL20.GL_ONE_MINUS_SRC_ALPHA
        );
    }

    @Override
    public void resize(int width, int height) {
        uiViewport.update(width, height, true);
        worldViewport.update(width, height, false);
        worldCamera.zoom = STAGE_CAMERA_ZOOM;
        worldCamera.position.set(STAGE_CAMERA_BASE_X, STAGE_CAMERA_BASE_Y, 0f);
        worldCamera.update();
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
