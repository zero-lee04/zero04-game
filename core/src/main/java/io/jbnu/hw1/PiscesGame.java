package io.jbnu.hw1;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class PiscesGame implements Screen {

    private final Game game;
    private boolean paused = false;
    private Texture pauseOverlayTexture;
    private final GlyphLayout pauseTextLayout = new GlyphLayout();
    private SpriteBatch batch;
    private OrthographicCamera camera;
    private FitViewport viewport;
    private Texture backgroundTexture;
    private Texture paddleTexture;
    private Texture ballTexture;
    private Texture brickTexture;
    private Texture starBrickTexture;
    private BitmapFont font;

    private Rectangle paddle;
    private Rectangle ball;
    private Array<BrickBlock> bricks;

    private float ballVelocityX = 300f;
    private float ballVelocityY = 300f;
    private boolean ballLaunched = false;
    private static final int MAX_LIVES = 3;
    private int lives = MAX_LIVES;

    private int score = 0;
    private int collectedStars = 0;
    private boolean gameOver = false;

    private static final int TOTAL_STAR_BRICKS = 11;
    private static final int WORLD_WIDTH = 1280;
    private static final int WORLD_HEIGHT = 720;

    private static final float PADDLE_SPEED = 420f;

    public PiscesGame(Game game) {
        this.game = game;
    }

    @Override
    public void show() {

        Pixmap pausePixel = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pausePixel.setColor(Color.WHITE);
        pausePixel.fill();
        pauseOverlayTexture = new Texture(pausePixel);
        pausePixel.dispose();
        paused = false;
        batch = new SpriteBatch();

        camera = new OrthographicCamera();
        viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);
        viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);

        backgroundTexture = new Texture(Gdx.files.internal("aquariusClear.png"));
        paddleTexture = new Texture(Gdx.files.internal("paddle.png"));
        ballTexture = new Texture(Gdx.files.internal("ball.png"));
        brickTexture = new Texture(Gdx.files.internal("brick.png"));
        starBrickTexture = new Texture(Gdx.files.internal("starBrick.png"));

        font = new BitmapFont();
        font.setColor(Color.WHITE);

        paddle = new Rectangle(WORLD_WIDTH / 2f - 60f, 60f, 120f, 20f);
        ball = new Rectangle(WORLD_WIDTH / 2f - 8f, paddle.y + paddle.height + 4f, 16f, 16f);

        lives = MAX_LIVES;
        gameOver = false;
        ballLaunched = false;
        resetBallOnPaddle();

        bricks = new Array<>();
        placeStarBricksRandomly();
    }

    @Override
    public void render(float delta) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            paused = !paused;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.F5)) {
            restart();
            return;
        }

        float dt = Math.min(delta, 1f / 30f);

        if (!paused && !gameOver) {
            update(dt);
        }

        if (collectedStars >= TOTAL_STAR_BRICKS) {
            gameOver = true;

            game.setScreen(new FirstGameScreen(game, false, true));
            dispose();
            return;
        }

        ScreenUtils.clear(0f, 0f, 0f, 1f);
        viewport.apply();
        camera.update();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        batch.draw(backgroundTexture, 0f, 0f, WORLD_WIDTH, WORLD_HEIGHT);

        for (BrickBlock bb : bricks) {
            if (bb.isStarBrick) {
                batch.draw(starBrickTexture, bb.rect.x, bb.rect.y, bb.rect.width, bb.rect.height);
            } else {
                batch.draw(brickTexture, bb.rect.x, bb.rect.y, bb.rect.width, bb.rect.height);
            }
        }

        batch.draw(paddleTexture, paddle.x, paddle.y, paddle.width, paddle.height);
        batch.draw(ballTexture, ball.x, ball.y, ball.width, ball.height);

        font.draw(batch, "Score: " + score, 10f, WORLD_HEIGHT - 10f);
        font.draw(batch, "Stars: " + collectedStars + " / " + TOTAL_STAR_BRICKS, 140f, WORLD_HEIGHT - 10f);

        font.draw(batch, "Lives: " + lives + " / " + MAX_LIVES, 350f, WORLD_HEIGHT - 10f);
        if (gameOver) {
            pauseTextLayout.setText(font, "GAME OVER - Press F5 to retry");
            font.draw(batch, pauseTextLayout,
                camera.position.x - pauseTextLayout.width / 2f,
                camera.position.y + pauseTextLayout.height / 2f);
        } else if (!ballLaunched && !paused) {
            font.draw(batch, "Press SPACE to launch", 10f, 30f);
        }

        if (paused) {

            batch.enableBlending();
            batch.setColor(0f, 0f, 0f, 0.6f);
            batch.draw(pauseOverlayTexture,
                camera.position.x - camera.viewportWidth * camera.zoom / 2f,
                camera.position.y - camera.viewportHeight * camera.zoom / 2f,
                camera.viewportWidth * camera.zoom,
                camera.viewportHeight * camera.zoom);
            batch.setColor(Color.WHITE);
            pauseTextLayout.setText(font, "PAUSED - Press ESC to resume");
            font.draw(batch, pauseTextLayout,
                camera.position.x - pauseTextLayout.width / 2f,
                camera.position.y + pauseTextLayout.height / 2f);
        }
        batch.end();
    }

    private void update(float dt) {

        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            paddle.x -= PADDLE_SPEED * dt;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            paddle.x += PADDLE_SPEED * dt;
        }
        paddle.x = MathUtils.clamp(paddle.x, 0f, WORLD_WIDTH - paddle.width);

        if (!ballLaunched) {
            placeBallOnPaddle();
            if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
                ballLaunched = true;
            }
        }

        if (ballLaunched) {
            ball.x += ballVelocityX * dt;
            ball.y += ballVelocityY * dt;

            if (ball.x < 0f || ball.x > WORLD_WIDTH - ball.width) {
                ballVelocityX = -ballVelocityX;
            }
            if (ball.y > WORLD_HEIGHT - ball.height) {
                ballVelocityY = -ballVelocityY;
            }

            if (ball.y < 0f) {
                lives--;
                resetBallOnPaddle();
                if (lives <= 0) {
                    gameOver = true;
                }
                return;
            }

            if (ball.overlaps(paddle)) {
                ballVelocityY = Math.abs(ballVelocityY);
                float offset = (ball.x + ball.width / 2f) - (paddle.x + paddle.width / 2f);
                ballVelocityX = offset * 2f;
            }

            for (int i = bricks.size - 1; i >= 0; i--) {
                BrickBlock bb = bricks.get(i);
                if (ball.overlaps(bb.rect)) {
                    bricks.removeIndex(i);
                    ballVelocityY = -ballVelocityY;
                    if (bb.isStarBrick) {

                        collectedStars++;
                        score += 100;
                    } else {
                        score += 100;
                    }
                    break;
                }
            }

        }
    }

    private void placeBallOnPaddle() {
        ball.x = paddle.x + (paddle.width - ball.width) / 2f;
        ball.y = paddle.y + paddle.height + 4f;
    }

    private void resetBallOnPaddle() {
        ballLaunched = false;
        ballVelocityX = 300f;
        ballVelocityY = 300f;
        placeBallOnPaddle();
    }

    private void placeStarBricksRandomly() {
        bricks.clear();

        Array<Rectangle> candidates = new Array<>();
        int cols = 10;
        int rows = 4;
        float w = 80f;
        float h = 24f;
        float startX = 100f;
        float startY = WORLD_HEIGHT - 150f;
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                candidates.add(new Rectangle(startX + c * (w + 20f), startY - r * (h + 20f), w, h));
            }
        }

        int need = TOTAL_STAR_BRICKS;
        Array<Integer> picked = new Array<>();
        while (picked.size < need && picked.size < candidates.size) {
            int idx = (int) (Math.random() * candidates.size);
            if (!picked.contains(idx, false)) picked.add(idx);
        }

        for (int i = 0; i < candidates.size; i++) {
            Rectangle r = candidates.get(i);
            boolean isStar = picked.contains(i, false);
            bricks.add(new BrickBlock(r, isStar));
        }
    }

    private static class BrickBlock {
        Rectangle rect;
        boolean isStarBrick;
        BrickBlock(Rectangle r, boolean isStar) { this.rect = r; this.isStarBrick = isStar; }
    }

    private void restart() {
        game.setScreen(new PiscesGame(game));
        dispose();
    }

    @Override
    public void resize(int width, int height) {
        if (viewport != null) {
            viewport.update(width, height, true);
        }
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {}

    @Override
    public void dispose() {
        if (pauseOverlayTexture != null) {
            pauseOverlayTexture.dispose();
            pauseOverlayTexture = null;
        }
        if (batch != null) batch.dispose();
        if (backgroundTexture != null) backgroundTexture.dispose();
        if (paddleTexture != null) paddleTexture.dispose();
        if (ballTexture != null) ballTexture.dispose();
        if (brickTexture != null) brickTexture.dispose();
        if (starBrickTexture != null) starBrickTexture.dispose();
        if (font != null) font.dispose();
    }
}
