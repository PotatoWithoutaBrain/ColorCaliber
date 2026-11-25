package com.ColorCaliber.frontend.grid;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;

/**
 * Represents a single isometric diamond tile within the Colour Grid arena.
 */
public class GridCell {
    private final int col;
    private final int row;
    private final float isoX;
    private final float isoY;
    private final float tileWidth;
    private final float tileHeight;
    private final Rectangle bounds;

    private GridType baseType;
    private GridType currentType;
    private float pulseTimer;

    public GridCell(int col, int row, float isoX, float isoY, float tileWidth, float tileHeight) {
        this.col = col;
        this.row = row;
        this.isoX = isoX;
        this.isoY = isoY;
        this.tileWidth = tileWidth;
        this.tileHeight = tileHeight;
        this.bounds = new Rectangle(isoX - tileWidth * 0.5f, isoY - tileHeight * 0.5f, tileWidth, tileHeight);
        this.baseType = GridType.NORMAL;
        this.currentType = GridType.NORMAL;
        this.pulseTimer = 0f;
    }

    public GridCell(int col, int row, float x, float y, float size) {
        this(col, row, x, y, size, size * 0.5f);
    }

    public void setPatternType(GridType type) {
        this.baseType = type;
        this.currentType = type;
    }

    public void setCurrentType(GridType type) {
        this.currentType = type;
    }

    public void revertToBaseType() {
        this.currentType = this.baseType;
    }

    public void update(float delta) {
        pulseTimer += delta * 4f;
    }

    /**
     * Renders the tile face as a 2:1 isometric diamond (rhombus).
     */
    public void render(ShapeRenderer shapeRenderer) {
        float hw = tileWidth * 0.5f - 1.2f;
        float hh = tileHeight * 0.5f - 0.7f;
        Color baseColor = currentType.getColor();

        // 1. Normal Floor: Subtle isometric checkerboard alternation
        if (currentType == GridType.NORMAL) {
            boolean alt = (col + row) % 2 == 0;
            if (alt) {
                shapeRenderer.setColor(0.18f, 0.21f, 0.28f, 1f);
            } else {
                shapeRenderer.setColor(0.14f, 0.16f, 0.22f, 1f);
            }
            shapeRenderer.triangle(isoX, isoY + hh, isoX - hw, isoY, isoX + hw, isoY);
            shapeRenderer.triangle(isoX, isoY - hh, isoX - hw, isoY, isoX + hw, isoY);
            return;
        }

        // 2. Red Hazard Zone
        if (currentType == GridType.RED) {
            float pulse = 0.85f + 0.15f * (float) Math.sin(pulseTimer * 1.5f);
            shapeRenderer.setColor(baseColor.r * pulse, baseColor.g * 0.6f, baseColor.b * 0.6f, 1f);
            shapeRenderer.triangle(isoX, isoY + hh, isoX - hw, isoY, isoX + hw, isoY);
            shapeRenderer.triangle(isoX, isoY - hh, isoX - hw, isoY, isoX + hw, isoY);

            // Inner hazard diamond
            float inHw = hw * 0.42f;
            float inHh = hh * 0.42f;
            shapeRenderer.setColor(1f, 0.35f, 0.40f, 1f);
            shapeRenderer.triangle(isoX, isoY + inHh, isoX - inHw, isoY, isoX + inHw, isoY);
            shapeRenderer.triangle(isoX, isoY - inHh, isoX - inHw, isoY, isoX + inHw, isoY);
            return;
        }

        // 3. Green Heal Zone
        if (currentType == GridType.GREEN) {
            float pulse = 0.90f + 0.10f * (float) Math.sin(pulseTimer * 1.2f);
            shapeRenderer.setColor(baseColor.r * 0.7f, baseColor.g * pulse, baseColor.b * 0.7f, 1f);
            shapeRenderer.triangle(isoX, isoY + hh, isoX - hw, isoY, isoX + hw, isoY);
            shapeRenderer.triangle(isoX, isoY - hh, isoX - hw, isoY, isoX + hw, isoY);

            // Inner healing diamond
            float inHw = hw * 0.45f;
            float inHh = hh * 0.45f;
            shapeRenderer.setColor(0.42f, 0.98f, 0.65f, 1f);
            shapeRenderer.triangle(isoX, isoY + inHh, isoX - inHw, isoY, isoX + inHw, isoY);
            shapeRenderer.triangle(isoX, isoY - inHh, isoX - inHw, isoY, isoX + inHw, isoY);
            return;
        }

        // 4. Blue Collectible Zone
        if (currentType == GridType.BLUE) {
            float pulse = 0.82f + 0.18f * (float) Math.sin(pulseTimer);
            shapeRenderer.setColor(baseColor.r * pulse, baseColor.g * pulse, Math.min(1f, baseColor.b * (pulse + 0.1f)), 1f);
            shapeRenderer.triangle(isoX, isoY + hh, isoX - hw, isoY, isoX + hw, isoY);
            shapeRenderer.triangle(isoX, isoY - hh, isoX - hw, isoY, isoX + hw, isoY);

            // Inner glowing core
            float inHw = hw * 0.48f;
            float inHh = hh * 0.48f;
            shapeRenderer.setColor(0.65f, 0.92f, 1f, 1f);
            shapeRenderer.triangle(isoX, isoY + inHh, isoX - inHw, isoY, isoX + inHw, isoY);
            shapeRenderer.triangle(isoX, isoY - inHh, isoX - inHw, isoY, isoX + inHw, isoY);
            return;
        }

        // 5. Diamond Collectible Tile
        if (currentType == GridType.DIAMOND) {
            float pulse = 0.85f + 0.15f * (float) Math.sin(pulseTimer * 1.6f);
            // Outer glowing cyan diamond tile
            shapeRenderer.setColor(0.15f * pulse, 0.78f * pulse, 1f, 1f);
            shapeRenderer.triangle(isoX, isoY + hh, isoX - hw, isoY, isoX + hw, isoY);
            shapeRenderer.triangle(isoX, isoY - hh, isoX - hw, isoY, isoX + hw, isoY);

            // Mid faceted diamond
            float midHw = hw * 0.62f;
            float midHh = hh * 0.62f;
            shapeRenderer.setColor(0.55f, 0.96f, 1f, 1f);
            shapeRenderer.triangle(isoX, isoY + midHh, isoX - midHw, isoY, isoX + midHw, isoY);
            shapeRenderer.triangle(isoX, isoY - midHh, isoX - midHw, isoY, isoX + midHw, isoY);

            // Bright white sparkle core
            float coreHw = hw * 0.28f;
            float coreHh = hh * 0.28f;
            shapeRenderer.setColor(1f, 1f, 1f, 1f);
            shapeRenderer.triangle(isoX, isoY + coreHh, isoX - coreHw, isoY, isoX + coreHw, isoY);
            shapeRenderer.triangle(isoX, isoY - coreHh, isoX - coreHw, isoY, isoX + coreHw, isoY);
            return;
        }

        // Default
        shapeRenderer.setColor(baseColor);
        shapeRenderer.triangle(isoX, isoY + hh, isoX - hw, isoY, isoX + hw, isoY);
        shapeRenderer.triangle(isoX, isoY - hh, isoX - hw, isoY, isoX + hw, isoY);
    }

    public int getCol() {
        return col;
    }

    public int getRow() {
        return row;
    }

    public float getX() {
        return isoX;
    }

    public float getY() {
        return isoY;
    }

    public float getIsoX() {
        return isoX;
    }

    public float getIsoY() {
        return isoY;
    }

    public float getTileWidth() {
        return tileWidth;
    }

    public float getTileHeight() {
        return tileHeight;
    }

    public float getSize() {
        return tileWidth;
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public GridType getCurrentType() {
        return currentType;
    }

    public GridType getBaseType() {
        return baseType;
    }
}
