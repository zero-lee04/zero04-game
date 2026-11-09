package io.jbnu.hw1;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;

public class CrawlPlayer implements Disposable {
    private final Texture texture;
    private final Animation<TextureRegion> runAnimation;

    private float animationTime = 0f;
    private boolean facingRight = true;

    public CrawlPlayer() {

        texture = new Texture(Gdx.files.internal("crawl.png"));

        texture.setFilter(
            Texture.TextureFilter.Nearest,
            Texture.TextureFilter.Nearest
        );

        int columns = 2;
        int rows = 3;

        int frameCount = 5;

        int frameWidth = texture.getWidth() / columns;
        int frameHeight = texture.getHeight() / rows;

        TextureRegion[][] regions = TextureRegion.split(
            texture,
            frameWidth,
            frameHeight
        );

        Array<TextureRegion> frames = new Array<>();

        int actualRows = regions.length;
        int actualCols = actualRows > 0 ? regions[0].length : 0;
        int maxFrames = actualRows * actualCols;

        for (int i = 0; i < Math.min(frameCount, maxFrames); i++) {
            int r = i / actualCols;
            int c = i % actualCols;
            frames.add(regions[r][c]);
        }

        while (frames.size < frameCount && maxFrames > 0) {
            frames.add(regions[actualRows - 1][actualCols - 1]);
        }

        runAnimation = new Animation<>(
            0.08f,
            frames,
            Animation.PlayMode.LOOP
        );
    }

    public void update(float dt, float velocityX) {

        if (velocityX > 0) {
            facingRight = true;
        } else if (velocityX < 0) {
            facingRight = false;
        }

        if (velocityX != 0) {
            animationTime += dt;
        } else {
            animationTime = 0f;
        }
    }

    public void draw(
        SpriteBatch batch,
        float x,
        float y,
        float width,
        float height
    ) {
        TextureRegion frame =
            runAnimation.getKeyFrame(animationTime);

        float drawY = y - 10f;

        if (facingRight) {
            batch.draw(frame, x, drawY, width, height);
        } else {

            batch.draw(frame, x + width, drawY, -width, height);
        }
    }

    @Override
    public void dispose() {
        texture.dispose();
    }

}

