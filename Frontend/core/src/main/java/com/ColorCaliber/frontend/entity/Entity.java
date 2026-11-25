package com.ColorCaliber.frontend.entity;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;

/**
 * Abstract base class for all rectangular entities in ColorCaliber.
 * Since no sprite assets are used yet, entities are rendered as colored rectangles.
 */
public abstract class Entity {
    protected float x;
    protected float y;
    protected float width;
    protected float height;
    protected Color color;
    protected final Rectangle bounds;

    public Entity(float x, float y, float width, float height, Color color) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.color = new Color(color);
        this.bounds = new Rectangle(x, y, width, height);
    }

    public abstract void update(float delta);

    public void render(ShapeRenderer shapeRenderer) {
        shapeRenderer.setColor(color);
        shapeRenderer.rect(x, y, width, height);
    }

    protected void updateBounds() {
        bounds.set(x, y, width, height);
    }

    public float getCenterX() {
        return x + width * 0.5f;
    }

    public float getCenterY() {
        return y + height * 0.5f;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public Color getColor() {
        return color;
    }

    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
        updateBounds();
    }
}
