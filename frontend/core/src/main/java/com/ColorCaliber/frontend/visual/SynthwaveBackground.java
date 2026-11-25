package com.ColorCaliber.frontend.visual;

import com.ColorCaliber.frontend.grid.ColourGridArena;
import com.ColorCaliber.frontend.manager.GameManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;

public class SynthwaveBackground {

    private static final float W = GameManager.WORLD_WIDTH;
    private static final float H = GameManager.WORLD_HEIGHT;
    private static final float HORIZON_Y = 498f;
    private static final float GRID_PARALLAX = 0.15f;

    private static final Color SKY_TOP = new Color(0.051f, 0.024f, 0.122f, 1f);
    private static final Color SKY_MID = new Color(0.141f, 0.043f, 0.212f, 1f);
    private static final Color SKY_HORIZON = new Color(0.290f, 0.082f, 0.294f, 1f);
    private static final Color HORIZON_HAZE = new Color(0.58f, 0.12f, 0.52f, 0.42f);
    private static final Color GROUND_TOP = new Color(0.145f, 0.035f, 0.205f, 1f);
    private static final Color GROUND_MID = new Color(0.068f, 0.020f, 0.125f, 1f);
    private static final Color GROUND_BOT = new Color(0.032f, 0.012f, 0.072f, 1f);

    private static final Color SUN_TOP_GOLD = new Color(1.0f, 0.88f, 0.30f, 1f);
    private static final Color SUN_MID_ORANGE = new Color(1.0f, 0.42f, 0.28f, 1f);
    private static final Color SUN_BOT_MAGENTA = new Color(0.95f, 0.06f, 0.52f, 1f);

    private final Color cTmp1 = new Color();
    private final Color cTmp2 = new Color();
    private final Color cTmp3 = new Color();
    private final Color cTmp4 = new Color();

    private static final int STAR_COUNT = 92;
    private final float[] starX = new float[STAR_COUNT];
    private final float[] starY = new float[STAR_COUNT];
    private final float[] starSize = new float[STAR_COUNT];
    private final float[] starPhase = new float[STAR_COUNT];
    private final float[] starSpeed = new float[STAR_COUNT];
    private final int[] starStyle = new int[STAR_COUNT];

    private static final int CUBE_COUNT = 6;
    private final float[] cubeBaseX = new float[CUBE_COUNT];
    private final float[] cubeBaseY = new float[CUBE_COUNT];
    private final float[] cubeRadius = new float[CUBE_COUNT];
    private final float[] cubeRotSpeedX = new float[CUBE_COUNT];
    private final float[] cubeRotSpeedY = new float[CUBE_COUNT];
    private final float[] cubePhase = new float[CUBE_COUNT];
    private final boolean[] cubeCyan = new boolean[CUBE_COUNT];

    private static final float[][] CUBE_VERTS = {
        {-1f, -1f, -1f}, { 1f, -1f, -1f}, { 1f,  1f, -1f}, {-1f,  1f, -1f},
        {-1f, -1f,  1f}, { 1f, -1f,  1f}, { 1f,  1f,  1f}, {-1f,  1f,  1f}
    };
    private static final int[][] CUBE_EDGES = {
        {0, 1}, {1, 2}, {2, 3}, {3, 0},
        {4, 5}, {5, 6}, {6, 7}, {7, 4},
        {0, 4}, {1, 5}, {2, 6}, {3, 7}
    };
    private final float[] projX = new float[8];
    private final float[] projY = new float[8];

    private static final float[][] FAR_MOUNTAINS = {
        {-120f, 74f, 165f}, {75f, 112f, 170f}, {265f, 88f, 155f}, {445f, 64f, 130f},
        {755f, 66f, 130f}, {935f, 95f, 160f}, {1125f, 118f, 175f}, {1320f, 82f, 165f}
    };
    private static final float[][] NEAR_MOUNTAINS = {
        {-150f, 64f, 140f}, {25f, 92f, 135f}, {185f, 128f, 145f}, {345f, 76f, 125f},
        {475f, 44f, 95f}, {725f, 46f, 95f}, {860f, 82f, 130f}, {1015f, 132f, 150f},
        {1180f, 94f, 135f}, {1345f, 68f, 145f}
    };

    private float animTime;

    public SynthwaveBackground() {
        this.animTime = 0f;
        initStars();
        initWireframeCubes();
    }

    private void initStars() {
        long seed = 0x5EED8051L;
        for (int i = 0; i < STAR_COUNT; i++) {
            seed = nextRand(seed);
            float r1 = toUnitFloat(seed);
            seed = nextRand(seed);
            float r2 = toUnitFloat(seed);
            seed = nextRand(seed);
            float r3 = toUnitFloat(seed);
            seed = nextRand(seed);
            float r4 = toUnitFloat(seed);

            starX[i] = -60f + r1 * (W + 120f);
            starY[i] = HORIZON_Y + 28f + (float) Math.pow(r2, 0.72) * (H - HORIZON_Y - 34f);
            starSize[i] = 1.0f + r3 * 1.9f;
            starPhase[i] = r4 * MathUtils.PI2;
            starSpeed[i] = 1.4f + r3 * 2.8f;
            starStyle[i] = (i % 9 == 0) ? 1 : 0;
        }
    }

    private void initWireframeCubes() {
        float[] xs = {130f, 290f, 430f, 775f, 935f, 1085f};
        float[] ys = {680f, 738f, 650f, 662f, 742f, 686f};
        float[] rs = {24f, 16f, 14f, 15f, 19f, 25f};
        float[] rx = {0.35f, -0.42f, 0.28f, -0.32f, 0.44f, -0.36f};
        float[] ry = {0.48f, 0.36f, -0.52f, 0.45f, -0.38f, 0.50f};

        for (int i = 0; i < CUBE_COUNT; i++) {
            cubeBaseX[i] = xs[i];
            cubeBaseY[i] = ys[i];
            cubeRadius[i] = rs[i];
            cubeRotSpeedX[i] = rx[i];
            cubeRotSpeedY[i] = ry[i];
            cubePhase[i] = i * 1.15f;
            cubeCyan[i] = (i % 2 == 0);
        }
    }

    private static long nextRand(long s) {
        return (s * 6364136223846793005L + 1442695040888963407L);
    }

    private static float toUnitFloat(long s) {
        return ((s >>> 40) & 0xFFFFFFL) / (float) 0x1000000L;
    }

    public void update(float delta) {
        animTime += delta;
    }

    public void renderBackdrop(ShapeRenderer sr, float offsetX, float offsetY) {
        float horizonY = HORIZON_Y - offsetY * 0.03f;

        renderSkyAndGroundGradient(sr, horizonY);
        renderStars(sr, offsetX, horizonY);
        renderFloatingWireframeCubes(sr, offsetX, offsetY);
        renderRetroStripedSun(sr, offsetX, horizonY);
        renderWireframeMountains(sr, offsetX, horizonY);
        renderPerspectiveLaserGrid(sr, offsetX, horizonY);
    }

    private void renderSkyAndGroundGradient(ShapeRenderer sr, float horizonY) {
        float midY = horizonY + (H - horizonY) * 0.52f;

        sr.rect(0f, midY, W, H - midY, SKY_MID, SKY_MID, SKY_TOP, SKY_TOP);
        sr.rect(0f, horizonY, W, midY - horizonY, SKY_HORIZON, SKY_HORIZON, SKY_MID, SKY_MID);

        cTmp1.set(HORIZON_HAZE);
        cTmp2.set(HORIZON_HAZE.r, HORIZON_HAZE.g, HORIZON_HAZE.b, 0f);
        sr.rect(0f, horizonY, W, 62f, cTmp1, cTmp1, cTmp2, cTmp2);

        float groundSplitY = horizonY * 0.45f;
        sr.rect(0f, groundSplitY, W, horizonY - groundSplitY, GROUND_MID, GROUND_MID, GROUND_TOP, GROUND_TOP);
        sr.rect(0f, 0f, W, groundSplitY, GROUND_BOT, GROUND_BOT, GROUND_MID, GROUND_MID);

        cTmp1.set(0.75f, 0.10f, 0.58f, 0.24f);
        cTmp2.set(0.35f, 0.04f, 0.35f, 0f);
        sr.rect(0f, horizonY - 58f, W, 58f, cTmp2, cTmp2, cTmp1, cTmp1);
    }

    private void renderStars(ShapeRenderer sr, float offsetX, float horizonY) {
        float px = -offsetX * 0.025f;
        for (int i = 0; i < STAR_COUNT; i++) {
            float sx = starX[i] + px;
            float sy = starY[i];
            if (sy < horizonY + 14f || sx < -10f || sx > W + 10f) continue;

            float twinkle = 0.42f + 0.58f * (0.5f + 0.5f * MathUtils.sin(animTime * starSpeed[i] + starPhase[i]));
            float heightFade = MathUtils.clamp((sy - horizonY) / 140f, 0.15f, 1f);
            float alpha = twinkle * heightFade;

            if (i % 5 == 0) {
                sr.setColor(0.68f, 0.94f, 1.0f, alpha);
            } else if (i % 7 == 0) {
                sr.setColor(1.0f, 0.72f, 0.95f, alpha);
            } else {
                sr.setColor(0.95f, 0.92f, 1.0f, alpha * 0.9f);
            }

            float sz = starSize[i];
            if (starStyle[i] == 1) {
                float arm = sz * (1.8f + 0.7f * twinkle);
                sr.rect(sx - arm, sy - 0.6f, arm * 2f, 1.2f);
                sr.rect(sx - 0.6f, sy - arm, 1.2f, arm * 2f);
                sr.setColor(1f, 1f, 1f, alpha);
                sr.rect(sx - 1.0f, sy - 1.0f, 2.0f, 2.0f);
            } else {
                sr.rect(sx - sz * 0.5f, sy - sz * 0.5f, sz, sz);
            }
        }
    }

    private void renderFloatingWireframeCubes(ShapeRenderer sr, float offsetX, float offsetY) {
        float px = -offsetX * 0.045f;
        float py = -offsetY * 0.02f;

        for (int i = 0; i < CUBE_COUNT; i++) {
            float cx = cubeBaseX[i] + px;
            float cy = cubeBaseY[i] + py + MathUtils.sin(animTime * 0.85f + cubePhase[i]) * 7f;
            float r = cubeRadius[i];

            float ax = animTime * cubeRotSpeedX[i] + cubePhase[i];
            float ay = animTime * cubeRotSpeedY[i] + cubePhase[i] * 0.7f;
            float cosX = MathUtils.cos(ax);
            float sinX = MathUtils.sin(ax);
            float cosY = MathUtils.cos(ay);
            float sinY = MathUtils.sin(ay);

            for (int v = 0; v < 8; v++) {
                float vx = CUBE_VERTS[v][0];
                float vy = CUBE_VERTS[v][1];
                float vz = CUBE_VERTS[v][2];

                float x1 = vx * cosY + vz * sinY;
                float z1 = -vx * sinY + vz * cosY;
                float y2 = vy * cosX - z1 * sinX;
                float z2 = vy * sinX + z1 * cosX;

                float persp = 3.4f / (3.4f + z2 * 0.55f);
                projX[v] = cx + x1 * r * persp;
                projY[v] = cy + y2 * r * persp;
            }

            float pulse = 0.78f + 0.22f * MathUtils.sin(animTime * 1.4f + cubePhase[i]);
            if (cubeCyan[i]) {
                cTmp1.set(0.05f, 0.92f, 1.0f, 0.14f * pulse);
                cTmp2.set(0.35f, 0.96f, 1.0f, 0.30f * pulse);
            } else {
                cTmp1.set(1.0f, 0.20f, 0.80f, 0.13f * pulse);
                cTmp2.set(1.0f, 0.45f, 0.88f, 0.28f * pulse);
            }

            for (int e = 0; e < CUBE_EDGES.length; e++) {
                int a = CUBE_EDGES[e][0];
                int b = CUBE_EDGES[e][1];
                sr.setColor(cTmp1);
                sr.rectLine(projX[a], projY[a], projX[b], projY[b], 3.2f);
                sr.setColor(cTmp2);
                sr.rectLine(projX[a], projY[a], projX[b], projY[b], 1.15f);
            }
        }
    }

    private void renderRetroStripedSun(ShapeRenderer sr, float offsetX, float horizonY) {
        float sunX = W * 0.5f - offsetX * 0.04f;
        float sunR = 110f;
        float sunY = horizonY + 150f;

        float pulse = 0.95f + 0.05f * MathUtils.sin(animTime * 1.6f);
        renderRadialGlow(sr, sunX, sunY, sunR * 1.90f, 1.0f, 0.16f, 0.58f, 0.28f * pulse, 48);
        renderRadialGlow(sr, sunX, sunY, sunR * 1.42f, 1.0f, 0.44f, 0.30f, 0.30f * pulse, 48);

        int slices = 64;
        float sliceH = (sunR * 2f) / slices;

        float[] cutCenter = {0.09f, 0.165f, 0.245f, 0.325f, 0.402f, 0.475f};
        float[] cutHalfH  = {0.023f, 0.020f, 0.017f, 0.014f, 0.011f, 0.008f};

        for (int i = 0; i < slices; i++) {
            float y0 = (sunY - sunR) + i * sliceH;
            float y1 = y0 + sliceH;
            float ym = (y0 + y1) * 0.5f;
            if (y1 <= horizonY) continue;
            if (y0 < horizonY) y0 = horizonY;

            float tMid = (ym - (sunY - sunR)) / (sunR * 2f);
            boolean inCutout = false;
            for (int c = 0; c < cutCenter.length; c++) {
                if (Math.abs(tMid - cutCenter[c]) < cutHalfH[c]) {
                    inCutout = true;
                    break;
                }
            }
            if (inCutout) continue;

            float dy0 = MathUtils.clamp((y0 - sunY) / sunR, -1f, 1f);
            float dy1 = MathUtils.clamp((y1 - sunY) / sunR, -1f, 1f);
            float hw0 = (float) Math.sqrt(Math.max(0f, 1f - dy0 * dy0)) * sunR;
            float hw1 = (float) Math.sqrt(Math.max(0f, 1f - dy1 * dy1)) * sunR;

            float t0 = (y0 - (sunY - sunR)) / (sunR * 2f);
            float t1 = (y1 - (sunY - sunR)) / (sunR * 2f);
            sampleSunColor(t0, cTmp1);
            sampleSunColor(t1, cTmp2);

            sr.triangle(sunX - hw0, y0, sunX + hw0, y0, sunX + hw1, y1, cTmp1, cTmp1, cTmp2);
            sr.triangle(sunX - hw0, y0, sunX + hw1, y1, sunX - hw1, y1, cTmp1, cTmp2, cTmp2);
        }
    }

    private void renderRadialGlow(ShapeRenderer sr, float cx, float cy, float radius,
                                  float r, float g, float b, float centerAlpha, int segments) {
        cTmp1.set(r, g, b, centerAlpha);
        cTmp2.set(r, g, b, 0f);
        float step = MathUtils.PI2 / segments;
        for (int i = 0; i < segments; i++) {
            float a0 = i * step;
            float a1 = (i + 1) * step;
            sr.triangle(
                cx, cy,
                cx + MathUtils.cos(a0) * radius, cy + MathUtils.sin(a0) * radius,
                cx + MathUtils.cos(a1) * radius, cy + MathUtils.sin(a1) * radius,
                cTmp1, cTmp2, cTmp2
            );
        }
    }

    private void sampleSunColor(float t, Color out) {
        t = MathUtils.clamp(t, 0f, 1f);
        if (t < 0.50f) {
            float k = t / 0.50f;
            out.set(SUN_BOT_MAGENTA).lerp(SUN_MID_ORANGE, k);
        } else {
            float k = (t - 0.50f) / 0.50f;
            out.set(SUN_MID_ORANGE).lerp(SUN_TOP_GOLD, k);
        }
        out.a = 1f;
    }

    private void renderWireframeMountains(ShapeRenderer sr, float offsetX, float horizonY) {
        float farPx = -offsetX * 0.055f;
        for (int i = 0; i < FAR_MOUNTAINS.length; i++) {
            float px = FAR_MOUNTAINS[i][0] + farPx;
            float ph = FAR_MOUNTAINS[i][1];
            float hw = FAR_MOUNTAINS[i][2];

            float leftX = px - hw;
            float rightX = px + hw;
            float peakY = horizonY + ph;
            float ridgeX = px + hw * 0.14f;

            cTmp1.set(0.085f, 0.025f, 0.175f, 1f);
            cTmp2.set(0.145f, 0.040f, 0.255f, 1f);
            cTmp3.set(0.055f, 0.018f, 0.125f, 1f);
            sr.triangle(leftX, horizonY, ridgeX, horizonY, px, peakY, cTmp1, cTmp1, cTmp2);
            sr.triangle(ridgeX, horizonY, rightX, horizonY, px, peakY, cTmp3, cTmp3, cTmp1);

            sr.setColor(0.85f, 0.12f, 0.65f, 0.28f);
            sr.rectLine(leftX, horizonY, px, peakY, 1.6f);
            sr.rectLine(px, peakY, rightX, horizonY, 1.6f);
        }

        float nearPx = -offsetX * 0.085f;
        for (int i = 0; i < NEAR_MOUNTAINS.length; i++) {
            float px = NEAR_MOUNTAINS[i][0] + nearPx;
            float ph = NEAR_MOUNTAINS[i][1];
            float hw = NEAR_MOUNTAINS[i][2];

            float leftX = px - hw;
            float rightX = px + hw;
            float peakY = horizonY + ph;
            float midSplitX = px - hw * 0.12f;
            float shoulderX = px + hw * 0.42f;
            float shoulderY = horizonY + ph * 0.46f;

            cTmp1.set(0.060f, 0.018f, 0.135f, 1f);
            cTmp2.set(0.115f, 0.032f, 0.215f, 1f);
            cTmp3.set(0.035f, 0.012f, 0.088f, 1f);
            cTmp4.set(0.050f, 0.015f, 0.115f, 1f);

            sr.triangle(leftX, horizonY, midSplitX, horizonY, px, peakY, cTmp1, cTmp1, cTmp2);
            sr.triangle(midSplitX, horizonY, shoulderX, shoulderY, px, peakY, cTmp3, cTmp4, cTmp2);
            sr.triangle(midSplitX, horizonY, rightX, horizonY, shoulderX, shoulderY, cTmp3, cTmp3, cTmp4);

            sr.setColor(1.0f, 0.10f, 0.72f, 0.26f);
            sr.rectLine(leftX, horizonY, px, peakY, 4.0f);
            sr.rectLine(px, peakY, shoulderX, shoulderY, 4.0f);
            sr.rectLine(shoulderX, shoulderY, rightX, horizonY, 4.0f);

            sr.setColor(1.0f, 0.24f, 0.84f, 0.90f);
            sr.rectLine(leftX, horizonY, px, peakY, 1.4f);
            sr.rectLine(px, peakY, shoulderX, shoulderY, 1.4f);
            sr.rectLine(shoulderX, shoulderY, rightX, horizonY, 1.4f);

            sr.setColor(0.95f, 0.18f, 0.78f, 0.42f);
            sr.rectLine(px, peakY, midSplitX, horizonY, 1.0f);
            sr.rectLine(midSplitX, horizonY, shoulderX, shoulderY, 0.95f);
        }
    }

    private void renderPerspectiveLaserGrid(ShapeRenderer sr, float offsetX, float horizonY) {
        float vpX = W * 0.5f - offsetX * GRID_PARALLAX;
        float gridHeight = horizonY;

        int railHalfCount = 18;
        float bottomSpread = 138f;

        for (int i = -railHalfCount; i <= railHalfCount; i++) {
            float topX = vpX + i * 2.5f;
            float botX = vpX + i * bottomSpread - offsetX * GRID_PARALLAX * 1.4f;

            boolean isCyan = (Math.abs(i) % 2 == 0);
            float distFade = MathUtils.clamp(1.0f - Math.abs(i) / (railHalfCount + 3f), 0.22f, 1.0f);

            if (isCyan) {
                cTmp1.set(0.0f, 0.92f, 1.0f, 0.03f);
                cTmp2.set(0.0f, 0.92f, 1.0f, 0.22f * distFade);
            } else {
                cTmp1.set(1.0f, 0.14f, 0.78f, 0.03f);
                cTmp2.set(1.0f, 0.14f, 0.78f, 0.20f * distFade);
            }
            drawGradientLine(sr, topX, horizonY, botX, 0f, 1.2f, 5.2f, cTmp1, cTmp2);

            if (isCyan) {
                cTmp1.set(0.15f, 0.95f, 1.0f, 0.10f);
                cTmp2.set(0.25f, 0.98f, 1.0f, 0.62f * distFade);
            } else {
                cTmp1.set(1.0f, 0.22f, 0.85f, 0.10f);
                cTmp2.set(1.0f, 0.35f, 0.90f, 0.54f * distFade);
            }
            drawGradientLine(sr, topX, horizonY, botX, 0f, 0.6f, 1.7f, cTmp1, cTmp2);
        }

        int depthLines = 18;
        float scrollSpeed = 0.68f;
        float scrollFrac = (animTime * scrollSpeed) % 1.0f;

        for (int i = 0; i < depthLines; i++) {
            float z = (i + 1f - scrollFrac);
            if (z <= 0.12f) continue;

            float perspectiveT = 1.0f / (0.35f + z * 0.58f);
            float y = horizonY - perspectiveT * gridHeight * 1.18f;
            if (y < -4f || y >= horizonY - 1f) continue;

            float nearness = MathUtils.clamp((horizonY - y) / horizonY, 0f, 1f);
            boolean magentaAccent = (i % 3 == 0);

            if (magentaAccent) {
                sr.setColor(1.0f, 0.14f, 0.80f, 0.08f + 0.18f * nearness);
            } else {
                sr.setColor(0.0f, 0.90f, 1.0f, 0.08f + 0.20f * nearness);
            }
            float glowH = 1.8f + 3.6f * nearness;
            sr.rect(0f, y - glowH * 0.5f, W, glowH);

            if (magentaAccent) {
                sr.setColor(1.0f, 0.38f, 0.90f, 0.18f + 0.58f * nearness);
            } else {
                sr.setColor(0.28f, 0.96f, 1.0f, 0.18f + 0.62f * nearness);
            }
            float coreH = 0.7f + 1.2f * nearness;
            sr.rect(0f, y - coreH * 0.5f, W, coreH);
        }

        sr.setColor(1.0f, 0.14f, 0.78f, 0.30f);
        sr.rect(0f, horizonY - 4f, W, 8f);
        sr.setColor(1.0f, 0.58f, 0.96f, 0.88f);
        sr.rect(0f, horizonY - 1.1f, W, 2.2f);
    }

    public void renderArenaUnderlay(ShapeRenderer sr, ColourGridArena arena) {
        float ox = arena.getOriginX();
        float oy = arena.getOriginY();
        int cols = arena.getCols();
        int rows = arena.getRows();
        float hw = ColourGridArena.TILE_WIDTH * 0.5f;
        float hh = ColourGridArena.TILE_HEIGHT * 0.5f;
        float platformDepth = ColourGridArena.PLATFORM_DEPTH;

        float[][] pillars = {
            {0f,  rows - 1f, 1.15f, 360f},
            {cols - 1f, 0f,  1.15f, 360f},
            {0f,  12f, 0.85f, 340f},
            {12f, 0f,  0.85f, 340f},
            {0f,  6f,  0.90f, 360f},
            {6f,  0f,  0.90f, 360f},
            {5.5f, 5.5f, 1.75f, 430f},
            {0f,  0f,  1.25f, 390f}
        };

        for (int i = 0; i < pillars.length; i++) {
            float pc = pillars[i][0];
            float pr = pillars[i][1];
            float span = pillars[i][2];
            float depth = pillars[i][3];

            float cx = ox + (pc - pr) * hw;
            float cy = oy + (pc + pr) * hh - platformDepth;
            float pW = hw * span;
            float pH = hh * span;

            renderDescendingPillar(sr, cx, cy, pW, pH, depth, (i % 2 == 0));
        }

        float botTipX = ox;
        float botTipY = oy - hh - platformDepth;
        float leftTipX = ox - cols * hw;
        float leftTipY = oy + (rows - 1) * hh - platformDepth;
        float rightTipX = ox + cols * hw;
        float rightTipY = oy + (cols - 1) * hh - platformDepth;

        cTmp1.set(0.42f, 0.06f, 0.48f, 0.30f);
        cTmp2.set(0.28f, 0.04f, 0.36f, 0.0f);
        sr.triangle(botTipX, botTipY - 20f, leftTipX - 40f, leftTipY - 60f, botTipX, botTipY - 300f, cTmp1, cTmp2, cTmp2);
        sr.triangle(botTipX, botTipY - 20f, rightTipX + 40f, rightTipY - 60f, botTipX, botTipY - 300f, cTmp1, cTmp2, cTmp2);

        float pulse = 0.88f + 0.12f * MathUtils.sin(animTime * 2.4f);
        float[] glowDrops = {64f, 34f, 16f, 6f};
        float[] glowAlphas = {0.10f, 0.22f, 0.42f, 0.85f};

        for (int g = 0; g < glowDrops.length; g++) {
            float drop = glowDrops[g];
            float a = glowAlphas[g] * pulse;

            cTmp1.set(0.0f, 0.95f, 1.0f, a);
            cTmp2.set(0.55f, 0.35f, 1.0f, a);
            cTmp3.set(0.0f, 0.80f, 1.0f, 0f);
            cTmp4.set(0.65f, 0.15f, 0.90f, 0f);
            sr.triangle(leftTipX, leftTipY, botTipX, botTipY, botTipX, botTipY - drop, cTmp1, cTmp2, cTmp4);
            sr.triangle(leftTipX, leftTipY, botTipX, botTipY - drop, leftTipX, leftTipY - drop, cTmp1, cTmp4, cTmp3);

            cTmp1.set(1.0f, 0.12f, 0.76f, a);
            cTmp2.set(0.55f, 0.35f, 1.0f, a);
            cTmp3.set(0.95f, 0.10f, 0.65f, 0f);
            cTmp4.set(0.65f, 0.15f, 0.90f, 0f);
            sr.triangle(botTipX, botTipY, rightTipX, rightTipY, rightTipX, rightTipY - drop, cTmp2, cTmp1, cTmp3);
            sr.triangle(botTipX, botTipY, rightTipX, rightTipY - drop, botTipX, botTipY - drop, cTmp2, cTmp3, cTmp4);
        }

        cTmp1.set(0.15f, 0.98f, 1.0f, 0.95f * pulse);
        cTmp2.set(0.85f, 0.55f, 1.0f, 0.95f * pulse);
        cTmp3.set(1.0f, 0.22f, 0.84f, 0.95f * pulse);
        drawGradientLine(sr, leftTipX, leftTipY, botTipX, botTipY, 3.2f, 3.6f, cTmp1, cTmp2);
        drawGradientLine(sr, botTipX, botTipY, rightTipX, rightTipY, 3.6f, 3.2f, cTmp2, cTmp3);
    }

    private void renderDescendingPillar(ShapeRenderer sr, float cx, float topY,
                                        float pW, float pH, float depth, boolean cyanEdge) {
        float lx = cx - pW;
        float ly = topY;
        float bx = cx;
        float by = topY - pH;
        float rx = cx + pW;
        float ry = topY;

        float fogYOffset = depth;

        cTmp1.set(0.065f, 0.035f, 0.140f, 0.96f);
        cTmp2.set(0.048f, 0.024f, 0.108f, 0.96f);
        cTmp3.set(0.36f, 0.07f, 0.42f, 0.0f);

        sr.triangle(lx, ly, bx, by, bx, by - fogYOffset, cTmp2, cTmp1, cTmp3);
        sr.triangle(lx, ly, bx, by - fogYOffset, lx, ly - fogYOffset, cTmp2, cTmp3, cTmp3);

        cTmp1.set(0.105f, 0.052f, 0.205f, 0.96f);
        cTmp2.set(0.080f, 0.040f, 0.165f, 0.96f);

        sr.triangle(bx, by, rx, ry, rx, ry - fogYOffset, cTmp1, cTmp2, cTmp3);
        sr.triangle(bx, by, rx, ry - fogYOffset, bx, by - fogYOffset, cTmp1, cTmp3, cTmp3);

        if (cyanEdge) {
            cTmp1.set(0.0f, 0.90f, 1.0f, 0.48f);
            cTmp2.set(0.35f, 0.10f, 0.55f, 0.0f);
        } else {
            cTmp1.set(1.0f, 0.16f, 0.80f, 0.45f);
            cTmp2.set(0.45f, 0.08f, 0.48f, 0.0f);
        }
        drawGradientLine(sr, bx, by, bx, by - fogYOffset * 0.72f, 2.2f, 1.0f, cTmp1, cTmp2);
    }

    private void drawGradientLine(ShapeRenderer sr, float x1, float y1, float x2, float y2,
                                  float width1, float width2, Color c1, Color c2) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        float len = (float) Math.sqrt(dx * dx + dy * dy);
        if (len < 0.0001f) return;

        float nx = -dy / len;
        float ny = dx / len;
        float hw1 = width1 * 0.5f;
        float hw2 = width2 * 0.5f;

        float ax = x1 + nx * hw1, ay = y1 + ny * hw1;
        float bx = x1 - nx * hw1, by = y1 - ny * hw1;
        float cx = x2 - nx * hw2, cy = y2 - ny * hw2;
        float dxp = x2 + nx * hw2, dyp = y2 + ny * hw2;

        sr.triangle(ax, ay, bx, by, cx, cy, c1, c1, c2);
        sr.triangle(ax, ay, cx, cy, dxp, dyp, c1, c2, c2);
    }
}
