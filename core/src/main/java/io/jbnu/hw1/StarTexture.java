package io.jbnu.hw1;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;

public class StarTexture implements Disposable {
    private final Texture texture;
    private final Animation<TextureRegion> animation;
    private float stateTime = 0f;

    private float x, y, width, height;

    public StarTexture() {
        texture = new Texture(Gdx.files.internal("star.png"));
        texture.setFilter(TextureFilter.Nearest, TextureFilter.Nearest);

        int frameWidth = texture.getWidth() / 3;
        int frameHeight = texture.getHeight() / 3;

        TextureRegion[][] regions = TextureRegion.split(texture, frameWidth, frameHeight);
        Array<TextureRegion> frames = new Array<>(framesCapacity());

        for (int i = 0; i < 7; i++) {
            frames.add(regions[i / 3][i % 3]);
        }

        animation = new Animation<TextureRegion>(0.08f, frames, Animation.PlayMode.LOOP);

        this.x = 0f;
        this.y = 0f;
        this.width = frameWidth;
        this.height = frameHeight;
    }

    private int framesCapacity() {
        return 6;
    }

    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public float getX() { return x; }
    public float getY() { return y; }
    public float getWidth() { return width; }
    public float getHeight() { return height; }

    public void reset() { stateTime = 0f; }

    public void update(float dt) {
        stateTime += dt;
    }

    public void draw(SpriteBatch batch, float drawX, float drawY, float w, float h, boolean facingRight) {
        TextureRegion frame = animation.getKeyFrame(stateTime, true);
        float dw = w;
        float dh = h;

        if (facingRight) {
            batch.draw(frame, drawX, drawY, dw, dh);
        } else {
            batch.draw(frame, drawX + dw, drawY, -dw, dh);
        }
    }

    @Override
    public void dispose() {
        if (texture != null) texture.dispose();
    }
}
