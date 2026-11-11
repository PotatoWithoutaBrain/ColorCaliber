package com.ColorCaliber.frontend.grid;

import com.badlogic.gdx.graphics.Color;

/**
 * Defines the types of Colour Grids and their visual colors.
 * Each grid type has a unique effect on gameplay.
 */
public enum GridType {
    NORMAL(new Color(0.16f, 0.17f, 0.22f, 1f), "Neutral Floor"),
    RED(new Color(0.88f, 0.18f, 0.22f, 1f), "Red Grid (Damage Hazard)"),
    BLUE(new Color(0.15f, 0.65f, 0.98f, 1f), "Blue Grid (+1 Point & Weapon Upgrade)"),
    GREEN(new Color(0.18f, 0.80f, 0.44f, 1f), "Green Grid (Heal Zone)"),
    YELLOW(new Color(0.95f, 0.77f, 0.06f, 1f), "Yellow Grid (Speed Boost)"),
    PURPLE(new Color(0.61f, 0.35f, 0.71f, 1f), "Purple Grid (Slow Field)");

    private final Color color;
    private final String description;

    GridType(Color color, String description) {
        this.color = color;
        this.description = description;
    }

    public Color getColor() {
        return color;
    }

    public String getDescription() {
        return description;
    }
}
