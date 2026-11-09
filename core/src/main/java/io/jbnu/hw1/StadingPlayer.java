package io.jbnu.hw1;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;

public class StadingPlayer implements Disposable {

    private final Texture texture;
    private final Animation<TextureRegion> standing;

    private float stateTime = 0f;

    public StadingPlayer() {
        texture = new Texture(Gdx.files.internal("standing.png"));

        texture.setFilter(
            Texture.TextureFilter.Nearest,
            Texture.TextureFilter.Nearest
        );

        TextureRegion[][] grid =
            TextureRegion.split(texture, 192, 192);

        Array<TextureRegion> frames = new Array<>();

        for (int i = 0; i < 8; i++) {
            frames.add(grid[i / 3][i % 3]);
        }

        standing = new Animation<>(
            0.12f,
            frames,
            Animation.PlayMode.LOOP
        );
    }

    public void update(float dt) {
        stateTime += dt;
    }

    public void reset() {
        stateTime = 0f;
    }

    public void draw(
        SpriteBatch batch,
        float x,
        float y,
        float width,
        float height,
        boolean facingRight
    ) {
        TextureRegion frame = standing.getKeyFrame(stateTime);

        float drawY = y - 10f;

        if (facingRight) {
            batch.draw(frame, x, drawY, width, height);
        } else {
            batch.draw(
                frame,
                x + width,
                drawY,
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
