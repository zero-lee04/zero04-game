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

public class AquariusGame implements Screen {

    private final Game game;
    private boolean paused = false;
    private Texture pauseOverlayTexture;
    private final GlyphLayout pauseTextLayout = new GlyphLayout();
    private Portal portal;

    private final Rectangle portalBounds = new Rectangle(4860f, 120f, 120f, 180f);
    private boolean restartRequested;

    private Texture redButtonTexture;
    private Texture greenButtonTexture;
    private final Rectangle floorButton = new Rectangle(520f, 60f, 64f, 32f);
    private Rectangle risingFloor;
    private boolean floorRaised;
    private static final float FLOOR_MIN_HEIGHT = 180f;
    private static final float FLOOR_MAX_HEIGHT = 350f;
    private static final float FLOOR_RISE_SPEED = 70f;
    private static final float CROUCH_DRAW_OFFSET_Y = -50f;

    private static final float WORLD_WIDTH = 1280f;
    private static final float WORLD_HEIGHT = 720f;
    private static final float MAP_WIDTH = 5000f;

    private static final float MOVE_SPEED = 500f;
    private static final float JUMP_SPEED = 500f;
    private static final float GRAVITY = 1000f;
    private static final float MAX_FALL_SPEED = 900f;

    private static final float FADE_DURATION = 0.5f;

    private static final float PLAYER_DRAW_OFFSET_Y = -20f;
    private static final float PLAYER_DRAW_WIDTH = 200f;
    private static final float PLAYER_DRAW_HEIGHT = 200f;
    private static final float PLAYER_HITBOX_WIDTH = 50f;
    private static final float PLAYER_HITBOX_HEIGHT = 120f;
    private static final float PLAYER_START_X =
        80f + (PLAYER_DRAW_WIDTH - PLAYER_HITBOX_WIDTH) / 2f;

    private static final float PLAYER_START_Y = 60f;

    private static final float FOOT_INSET = 15f;

    private static final float RESPAWN_Y = -100f;

    private SpriteBatch batch;

    private StadingPlayer standingPlayer;
    private RightMovingPlayer rightMovingPlayer;
    private JumpingPlayer jumpingPlayer;
    private CrawlPlayer crawlPlayer;
    private Texture downPlayerTexture;

    private Texture blockTexture;
    private Texture backgroundTexture;

    private BitmapFont font;

    private OrthographicCamera camera;
    private FitViewport viewport;

    private final Rectangle player = new Rectangle(
        PLAYER_START_X,
        PLAYER_START_Y,
        PLAYER_HITBOX_WIDTH,
        PLAYER_HITBOX_HEIGHT
    );
    private final Array<Rectangle> blocks = new Array<>();
    private final Array<Rectangle> stars = new Array<>();
    private final Array<StarTexture> starTextures = new Array<>();

    private float velocityX;
    private float velocityY;

    private boolean grounded;
    private boolean gameOver;

    private boolean moving;
    private boolean downMoving;
    private boolean facingRight = true;

    private static final int TOTAL_STARS = 17;
    private int collectedStars;

    private float backgroundAlpha;

    boolean showHelp = false;

    public AquariusGame(Game game) {
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
        portal = new Portal();
        showHelp = true;
        redButtonTexture = new Texture(Gdx.files.internal("red_button.png"));
        greenButtonTexture = new Texture(Gdx.files.internal("green_button.png"));

        standingPlayer = new StadingPlayer();
        rightMovingPlayer = new RightMovingPlayer();
        jumpingPlayer = new JumpingPlayer();
        crawlPlayer = new CrawlPlayer();

        blockTexture = new Texture(
            Gdx.files.internal("whiteBlock.png")
        );

        backgroundTexture = new Texture(
            Gdx.files.internal("basicScreen.png")
        );

        downPlayerTexture = new Texture(
            Gdx.files.internal("down.png")
        );

        font = new BitmapFont();
        font.setColor(Color.WHITE);

        camera = new OrthographicCamera();

        viewport = new FitViewport(
            WORLD_WIDTH,
            WORLD_HEIGHT,
            camera
        );

        viewport.update(
            Gdx.graphics.getWidth(),
            Gdx.graphics.getHeight(),
            true
        );

        blocks.clear();
        for (StarTexture st : starTextures) st.dispose();
        starTextures.clear();
        stars.clear();
        blocks.add(new Rectangle(0f, 0f, 580f, 60f));
        blocks.add(new Rectangle(740f, 0f, 260f, 60f));
        blocks.add(new Rectangle(800f, 0f, 120f, 150f));
        risingFloor = new Rectangle(650f, 0f, 110f, FLOOR_MIN_HEIGHT);
        blocks.add(risingFloor);
        blocks.add(new Rectangle(100f, 326f, 200f, 24f));
        blocks.add(new Rectangle(400f, 326f, 150f, 24f));
        blocks.add(new Rectangle(80f, 326f, 20f, 324f));
        blocks.add(new Rectangle(80f, 650f, 470f, 24f));
        blocks.add(new Rectangle(1000f, 0f, 600f, 60f));
        blocks.add(new Rectangle(1080f, 0f, 130f, 140f));
        blocks.add(new Rectangle(1270f, 0f, 130f, 230f));
        blocks.add(new Rectangle(1460f, 0f, 140f, 320f));
        blocks.add(new Rectangle(1230f, 396f, 160f, 24f));
        blocks.add(new Rectangle(1000f, 476f, 150f, 24f));
        blocks.add(new Rectangle(1720f, 0f, 180f, 60f));
        blocks.add(new Rectangle(2020f, 0f, 180f, 100f));
        blocks.add(new Rectangle(2320f, 0f, 180f, 60f));
        blocks.add(new Rectangle(2600f, 0f, 650f, 60f));
        blocks.add(new Rectangle(2680f, 0f, 120f, 140f));
        blocks.add(new Rectangle(2860f, 0f, 120f, 230f));
        blocks.add(new Rectangle(3060f, 0f, 130f, 320f));
        blocks.add(new Rectangle(2840f, 396f, 150f, 24f));
        blocks.add(new Rectangle(2620f, 476f, 150f, 24f));
        blocks.add(new Rectangle(3370f, 0f, 600f, 60f));
        blocks.add(new Rectangle(4090f, 0f, 910f, 60f));
        blocks.add(new Rectangle(3470f, 0f, 130f, 140f));
        blocks.add(new Rectangle(3660f, 0f, 130f, 230f));
        blocks.add(new Rectangle(3850f, 0f, 120f, 320f));
        blocks.add(new Rectangle(3620f, 396f, 140f, 24f));
        blocks.add(new Rectangle(3390f, 476f, 140f, 24f));
        blocks.add(new Rectangle(4200f, 0f, 130f, 140f));
        blocks.add(new Rectangle(4390f, 0f, 130f, 230f));
        blocks.add(new Rectangle(4580f, 0f, 130f, 320f));
        blocks.add(new Rectangle(4800f, 396f, 180f, 24f));
        addStar(180f, 405f);
        addStar(445f, 405f);
        addStar(530f, 110f);
        addStar(1110f, 195f);
        addStar(1300f, 285f);
        addStar(1040f, 530f);
        addStar(1760f, 115f);
        addStar(2060f, 155f);
        addStar(2360f, 115f);
        addStar(2710f, 195f);
        addStar(2890f, 285f);
        addStar(2660f, 530f);
        addStar(3430f, 530f);
        addStar(3660f, 450f);
        addStar(3880f, 375f);
        addStar(4420f, 285f);
        addStar(4840f, 450f);

        floorRaised = false;
        updateButtonPosition();

        player.set(
            PLAYER_START_X,
            PLAYER_START_Y,
            PLAYER_HITBOX_WIDTH,
            PLAYER_HITBOX_HEIGHT
        );

        velocityX = 0f;
        velocityY = 0f;

        grounded = true;
        gameOver = false;

        moving = false;
        downMoving = false;
        facingRight = true;

        collectedStars = 0;

        backgroundAlpha = 0f;

    }

    @Override
    public void render(float delta) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            paused = !paused;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.F5)) {
            restartGame();
            return;
        }
        float elapsed = Math.max(delta, 0f);

        float dt = Math.min(elapsed, 1f / 30f);
        if (Gdx.input.isKeyJustPressed(Input.Keys.H)) {
            showHelp = !showHelp;
        }

        if (!paused && !gameOver) {
            update(dt);
            if (restartRequested) {
                restartGame();
                return;
            }
            updateAnimation(dt);

            for (StarTexture starTexture : starTextures) {
                starTexture.update(dt);
            }

            collectStars();
            portal.update(dt);

            if (collectedStars >= TOTAL_STARS && player.overlaps(portalBounds)) {
                gameOver = true;
            }
        }

        if (gameOver) {
            game.setScreen(new FirstGameScreen(game, true));
            dispose();
            return;
        }

        ScreenUtils.clear(0f, 0f, 0f, 1f);

        viewport.apply();
        camera.update();

        float screenLeft =
            camera.position.x - camera.viewportWidth * camera.zoom / 2f;

        float screenTop =
            camera.position.y + camera.viewportHeight * camera.zoom / 2f;

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        batch.setColor(
            1f,
            1f,
            1f,
            backgroundAlpha
        );

        float visibleWidth = camera.viewportWidth * camera.zoom;
        float leftEdge = camera.position.x - visibleWidth /2f;
        float rightEdge = camera.position.x + visibleWidth / 2f;

        float tileWidth = WORLD_WIDTH;

        float startX = (float) Math.floor(leftEdge/tileWidth) * tileWidth;
        for (float x = startX; x<rightEdge; x+=tileWidth){
            batch.draw(
                backgroundTexture,
                x,
                0f,
                tileWidth,
                WORLD_HEIGHT
            );
        }

        batch.setColor(1f, 1f, 1f, 1f);

        for (Rectangle block : blocks) {
            batch.draw(
                blockTexture,
                block.x,
                block.y,
                block.width,
                block.height
            );
        }

        batch.setColor(0.3f, 0.7f, 1f, 1f);
        batch.draw(blockTexture, risingFloor.x, risingFloor.y,
            risingFloor.width, risingFloor.height);
        batch.setColor(1f, 1f, 1f, 1f);
        batch.draw(floorRaised ? greenButtonTexture : redButtonTexture,
            floorButton.x, floorButton.y, floorButton.width, floorButton.height);
        font.draw(batch, "E: Lift", floorButton.x, floorButton.y + 52f);

        for (int i = 0; i < starTextures.size; i++) {
            StarTexture starTexture = starTextures.get(i);
            Rectangle starRect = stars.get(i);

            starTexture.draw(
                batch,
                starRect.x,
                starRect.y,
                starRect.width,
                starRect.height,
                true
            );
        }

        boolean portalUnlocked = collectedStars >= TOTAL_STARS;
        batch.setColor(1f, 1f, 1f, portalUnlocked ? 1f : 0.35f);
        portal.draw(batch, portalBounds.x, portalBounds.y,
            portalBounds.width, portalBounds.height, true);
        batch.setColor(1f, 1f, 1f, 1f);
        font.draw(batch, portalUnlocked ? "Portal open!" : "Need 17 stars",
            portalBounds.x - 10f, portalBounds.y + portalBounds.height + 25f);

        drawPlayer();

        font.draw(
            batch,
            "Stars: " + collectedStars + " / " + TOTAL_STARS,
            screenLeft + 10f,
            screenTop - 10f
        );

        if (gameOver) {
            font.draw(batch, "CLEAR! All 17 stars collected!",
                screenLeft + 420f, screenTop - 100f);
        }

        if (showHelp) {
            font.draw(batch, "How to operate:", screenLeft + 10f, screenTop - 30f);
            font.draw(batch, "MOVE: WASD  ", screenLeft + 10f, screenTop - 50f);
            font.draw(batch, "SPACE: jump", screenLeft + 10f, screenTop - 70f);
            font.draw(batch, "E: Interaction", screenLeft + 10f, screenTop - 90f);
            font.draw(batch, "H: help", screenLeft + 10f, screenTop - 110f);
            font.draw(batch, "F5: restart", screenLeft + 10f, screenTop - 130f);
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
            pauseTextLayout.setText(font, "PAUSED");
            font.draw(batch, pauseTextLayout,
                camera.position.x - pauseTextLayout.width / 2f,
                camera.position.y + pauseTextLayout.height / 2f);
        }
        batch.end();

        if (!paused) {
            backgroundAlpha = Math.min(
                1f,
                backgroundAlpha + elapsed / FADE_DURATION
            );
        }
    }

    private void update(float dt) {
        if (gameOver) {
            return;
        }

        updateRisingFloor(dt);

        velocityX = 0f;

        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            velocityX -= MOVE_SPEED;
        }

        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            velocityX += MOVE_SPEED;
        }

        if (
            Gdx.input.isKeyJustPressed(Input.Keys.SPACE)
                && grounded
        ) {
            velocityY = JUMP_SPEED;
            grounded = false;

            jumpingPlayer.reset();
        }

        velocityY = Math.max(
            velocityY - GRAVITY * dt,
            -MAX_FALL_SPEED
        );

        moveX(dt);
        moveY(dt);

        if (player.y + player.height < RESPAWN_Y) {
            restartRequested = true;
            return;
        }

        downMoving =
            grounded
                && Gdx.input.isKeyPressed(Input.Keys.DOWN)
                && velocityX != 0f;

        float halfViewWidth =
            camera.viewportWidth * camera.zoom / 2f;

        camera.position.set(
            MathUtils.clamp(
                player.x + player.width / 2f,
                halfViewWidth,
                MAP_WIDTH - halfViewWidth
            ),
            WORLD_HEIGHT / 2f,
            0f
        );

        camera.update();

    }

    private void restartGame() {
        game.setScreen(new AquariusGame(game));
        dispose();
    }

    private void updateButtonPosition() {
        floorButton.x = risingFloor.x
            + (risingFloor.width - floorButton.width) / 2f;
        floorButton.y = risingFloor.y + risingFloor.height;
    }

    private void updateRisingFloor(float dt) {
        updateButtonPosition();
        float oldTop = risingFloor.y + risingFloor.height;
        float footLeft = player.x + FOOT_INSET;
        float footRight = player.x + player.width - FOOT_INSET;
        boolean overlapsFeet = footRight > risingFloor.x
            && footLeft < risingFloor.x + risingFloor.width;
        boolean riding = grounded && overlapsFeet
            && Math.abs(player.y - oldTop) <= 1f;

        float centerX = player.x + player.width / 2f;
        float buttonCenterX = floorButton.x + floorButton.width / 2f;
        boolean nearButton = grounded
            && Math.abs(centerX - buttonCenterX) <= 120f
            && Math.abs(player.y - floorButton.y) <= 30f;

        if ((nearButton || riding) && Gdx.input.isKeyJustPressed(Input.Keys.E)) {
            floorRaised = !floorRaised;
        }

        float target = floorRaised ? FLOOR_MAX_HEIGHT : FLOOR_MIN_HEIGHT;
        float oldHeight = risingFloor.height;
        float step = FLOOR_RISE_SPEED * dt;
        float nextHeight = oldHeight < target
            ? Math.min(target, oldHeight + step)
            : Math.max(target, oldHeight - step);
        float movementY = nextHeight - oldHeight;

        if (riding && movementY > 0f) {
            float oldHead = player.y + player.height;
            for (Rectangle block : blocks) {
                if (block == risingFloor) continue;
                boolean overlapsX = player.x + player.width > block.x
                    && player.x < block.x + block.width;
                if (overlapsX && oldHead <= block.y
                    && oldHead + movementY > block.y) {
                    movementY = Math.max(0f, block.y - oldHead);
                }
            }
        }

        risingFloor.height = oldHeight + movementY;
        float newTop = risingFloor.y + risingFloor.height;
        if (riding) {
            player.y += movementY;
        } else if (movementY > 0f && overlapsFeet
            && player.y >= oldTop && player.y < newTop) {
            player.y = newTop;
            if (velocityY < 0f) velocityY = 0f;
            grounded = true;
        }
        updateButtonPosition();
    }

    private void moveX(float dt) {
        float previousLeft = player.x;
        float previousRight = player.x + player.width;

        float dx = velocityX * dt;
        float nextX = player.x + dx;

        for (Rectangle block : blocks) {

            boolean overlapsY =
                player.y < block.y + block.height
                    && player.y + player.height > block.y;

            if (!overlapsY) {
                continue;
            }

            if (dx > 0f) {

                boolean crossedLeftSide =
                    previousRight <= block.x
                        && nextX + player.width > block.x;

                if (crossedLeftSide) {
                    nextX = Math.min(
                        nextX,
                        block.x - player.width
                    );
                }

            } else if (dx < 0f) {
                float blockRight = block.x + block.width;

                boolean crossedRightSide =
                    previousLeft >= blockRight
                        && nextX < blockRight;

                if (crossedRightSide) {
                    nextX = Math.max(
                        nextX,
                        blockRight
                    );
                }
            }
        }

        player.x = MathUtils.clamp(
            nextX,
            0f,
            MAP_WIDTH - player.width
        );
    }

    private void moveY(float dt) {
        float previousBottom = player.y;
        float previousTop = player.y + player.height;

        float dy = velocityY * dt;
        player.y += dy;

        grounded = false;

        if (dy < 0f) {
            float footLeft = player.x + FOOT_INSET;
            float footRight = player.x + player.width - FOOT_INSET;

            float landingY = Float.NEGATIVE_INFINITY;

            for (Rectangle block : blocks) {
                float blockTop = block.y + block.height;

                boolean overlapsFeet =
                    footRight > block.x
                        && footLeft < block.x + block.width;

                boolean crossedTop =
                    previousBottom >= blockTop
                        && player.y <= blockTop;

                if (overlapsFeet && crossedTop) {

                    landingY = Math.max(landingY, blockTop);
                }
            }

            if (landingY != Float.NEGATIVE_INFINITY) {
                player.y = landingY;
                velocityY = 0f;
                grounded = true;
            }

        } else if (dy > 0f) {

            float ceilingY = Float.POSITIVE_INFINITY;

            for (Rectangle block : blocks) {
                boolean overlapsX =
                    player.x + player.width > block.x
                        && player.x < block.x + block.width;

                boolean crossedBottom =
                    previousTop <= block.y
                        && player.y + player.height >= block.y;

                if (overlapsX && crossedBottom) {
                    ceilingY = Math.min(ceilingY, block.y);
                }
            }

            if (ceilingY != Float.POSITIVE_INFINITY) {
                player.y = ceilingY - player.height;
                velocityY = 0f;
            }
        }
    }

    private void updateAnimation(float dt) {
        boolean nextMoving = velocityX != 0f;

        if (moving && !nextMoving) {
            standingPlayer.reset();
        }

        moving = nextMoving;

        if (velocityX > 0f) {
            facingRight = true;
        } else if (velocityX < 0f) {
            facingRight = false;
        }

        if (jumpAnimating()) {
            jumpingPlayer.update(dt);
        } else if (downMoving) {
            crawlPlayer.update(dt, velocityX);
        } else if (moving) {
            rightMovingPlayer.update(dt, velocityX);
        } else {
            standingPlayer.update(dt);
        }
    }

    private boolean jumpAnimating() {
        return !grounded;
    }

    private void drawPlayer() {
        float drawX =
            player.x - (PLAYER_DRAW_WIDTH - player.width) / 2f;

        boolean crouching = grounded && Gdx.input.isKeyPressed(Input.Keys.DOWN);
        float drawY = player.y + (crouching
            ? CROUCH_DRAW_OFFSET_Y : PLAYER_DRAW_OFFSET_Y);

        if (jumpAnimating()) {
            jumpingPlayer.draw(
                batch,
                drawX,
                drawY,
                PLAYER_DRAW_WIDTH,
                PLAYER_DRAW_HEIGHT,
                facingRight
            );
        } else if (downMoving) {
            crawlPlayer.draw(
                batch,
                drawX,
                drawY,
                PLAYER_DRAW_WIDTH,
                PLAYER_DRAW_HEIGHT
            );
        } else if (moving) {
            rightMovingPlayer.draw(
                batch,
                drawX,
                drawY,
                PLAYER_DRAW_WIDTH,
                PLAYER_DRAW_HEIGHT
            );
        } else if (crouching) {
            batch.draw(
                downPlayerTexture,
                drawX,
                drawY,
                PLAYER_DRAW_WIDTH,
                PLAYER_DRAW_HEIGHT
            );
        } else {
            standingPlayer.draw(
                batch,
                drawX,
                drawY,
                PLAYER_DRAW_WIDTH,
                PLAYER_DRAW_HEIGHT,
                facingRight
            );
        }
    }

    private void addStar(float x, float y) {
        starTextures.add(new StarTexture());
        stars.add(new Rectangle(x, y, 60f, 60f));
    }

    private void collectStars() {

        for (int i = stars.size - 1; i >= 0; i--) {
            Rectangle starRect = stars.get(i);

            if (player.overlaps(starRect)) {
                stars.removeIndex(i);

                StarTexture collectedStar =
                    starTextures.removeIndex(i);

                collectedStar.dispose();

                collectedStars++;

            }
        }
    }

    @Override
    public void resize(int width, int height) {
        if (viewport != null) {
            viewport.update(width, height, true);
        }
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
        if (pauseOverlayTexture != null) {
            pauseOverlayTexture.dispose();
            pauseOverlayTexture = null;
        }
        if (portal != null) {
            portal.dispose();
            portal = null;
        }
        if (redButtonTexture != null) {
            redButtonTexture.dispose();
            redButtonTexture = null;
        }
        if (greenButtonTexture != null) {
            greenButtonTexture.dispose();
            greenButtonTexture = null;
        }
        if (batch != null) {
            batch.dispose();
            batch = null;
        }

        if (standingPlayer != null) {
            standingPlayer.dispose();
            standingPlayer = null;
        }

        if (rightMovingPlayer != null) {
            rightMovingPlayer.dispose();
            rightMovingPlayer = null;
        }

        if (jumpingPlayer != null) {
            jumpingPlayer.dispose();
            jumpingPlayer = null;
        }

        if (crawlPlayer != null) {
            crawlPlayer.dispose();
            crawlPlayer = null;
        }

        if (backgroundTexture != null) {
            backgroundTexture.dispose();
            backgroundTexture = null;
        }

        if (blockTexture != null) {
            blockTexture.dispose();
            blockTexture = null;
        }

        if (downPlayerTexture != null) {
            downPlayerTexture.dispose();
            downPlayerTexture = null;
        }

        for (StarTexture starTexture : starTextures) {
            starTexture.dispose();
        }

        starTextures.clear();
        stars.clear();
        blocks.clear();

        if (font != null) {
            font.dispose();
            font = null;
        }
    }
}
