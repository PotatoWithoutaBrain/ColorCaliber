package com.ColorCaliber.frontend.grid;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;

/**
 * Represents a single tile within the Colour Grid arena.
 */
public class GridCell {
    private final int col;
    private final int row;
    private final float x;
    private final float y;
    private final float size;
    private final Rectangle bounds;

    private GridType baseType;
    private GridType currentType;
    private float pulseTimer;

    public GridCell(int col, int row, float x, float y, float size) {
        this.col = col;
        this.row = row;
        this.x = x;
        this.y = y;
        this.size = size;
        this.bounds = new Rectangle(x, y, size, size);
        this.baseType = GridType.NORMAL;
        this.currentType = GridType.NORMAL;
        this.pulseTimer = 0f;
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

    public void render(ShapeRenderer shapeRenderer) {
        float padding = 1.5f;
        Color baseColor = currentType.getColor();

        if (currentType == GridType.BLUE) {
            // Subtle pulsing glow for collectible Blue Grids
            float pulse = 0.85f + 0.15f * (float) Math.sin(pulseTimer);
            shapeRenderer.setColor(
                baseColor.r * pulse,
                baseColor.g * pulse,
                Math.min(1f, baseColor.b * (pulse + 0.1f)),
                1f
            );
            shapeRenderer.rect(x + padding, y + padding, size - padding * 2, size - padding * 2);

            // Inner bright core to highlight Blue Grid collectible
            shapeRenderer.setColor(0.65f, 0.92f, 1f, 1f);
            float innerMargin = size * 0.28f;
            shapeRenderer.rect(x + innerMargin, y + innerMargin, size - innerMargin * 2, size - innerMargin * 2);
        } else if (currentType == GridType.RED) {
            float pulse = 0.88f + 0.12f * (float) Math.sin(pulseTimer * 1.2f);
            shapeRenderer.setColor(baseColor.r * pulse, baseColor.g, baseColor.b, 1f);
            shapeRenderer.rect(x + padding, y + padding, size - padding * 2, size - padding * 2);
        } else {
            shapeRenderer.setColor(baseColor);
            shapeRenderer.rect(x + padding, y + padding, size - padding * 2, size - padding * 2);
        }
    }

    public int getCol() {
        return col;
    }

    public int getRow() {
        return row;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getSize() {
        return size;
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
