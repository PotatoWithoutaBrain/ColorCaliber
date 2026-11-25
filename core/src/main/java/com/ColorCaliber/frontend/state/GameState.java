package com.ColorCaliber.frontend.state;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;

public interface GameState {

    void enter();

    void handleInput(Vector3 mouseWorldPos);

    void update(float delta);

    void renderWorldShapes(ShapeRenderer shapeRenderer);

    void renderWorldSprites(SpriteBatch spriteBatch);

    void renderUiShapes(ShapeRenderer shapeRenderer);

    void renderUiSprites(SpriteBatch spriteBatch, BitmapFont font, GlyphLayout glyphLayout);

    void exit();
}
