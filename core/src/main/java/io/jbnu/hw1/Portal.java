package io.jbnu.hw1;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;

public class Portal implements Disposable {

    private final Texture texture;
    private final Animation<TextureRegion> animation;

    private float stateTime = 0f;

    public Portal() {
        texture = new Texture(Gdx.files.internal("portal.png"));

        texture.setFilter(
            Texture.TextureFilter.Nearest,
            Texture.TextureFilter.Nearest
        );

        int frameWidth = texture.getWidth() / 3;
        int frameHeight = texture.getHeight() / 4;

        TextureRegion[][] regions = TextureRegion.split(
            texture,
            frameWidth,
            frameHeight
        );

        Array<TextureRegion> frames = new Array<>();

        for (int i = 0; i < 12; i++) {
            frames.add(regions[i / 3][i % 3]);
        }

        animation = new Animation<>(
            0.08f,
            frames,
            Animation.PlayMode.LOOP
        );
    }

    public void reset() {
        stateTime = 0f;
    }

    public void update(float dt) {
        stateTime += dt;
    }

    public void draw(
        SpriteBatch batch,
        float x,
        float y,
        float width,
        float height,
        boolean facingRight
    ) {

        TextureRegion frame = animation.getKeyFrame(stateTime);

        if (facingRight) {
            batch.draw(frame, x, y, width, height);
        } else {
            batch.draw(
                frame,
                x + width,
                y,
                -width,
                height
            );
        }
    }

    @Override
    public void dispose() {
        texture.dispose();
    }
}
