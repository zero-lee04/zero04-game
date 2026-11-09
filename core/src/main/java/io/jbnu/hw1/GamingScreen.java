package io.jbnu.hw1;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class GamingScreen implements Screen {
    private final Game game;
    private static final float WORLD_WIDTH = 1280f;
    private static final float MOVE_SPEED = 300f;
    private static final float JUMP_SPEED = 650f;
    private static final float GRAVITY = 1700f;

    private SpriteBatch batch;
    private StadingPlayer standingPlayer;
    private RightMovingPlayer rightMovingPlayer;
    private JumpingPlayer jumpingPlayer;

    private Texture blockTexture;
    private Texture backgroundTexture;
    private Texture downPlayerTexture;

    private OrthographicCamera camera;
    private FitViewport viewport;
    private boolean goToNextScreen = false;

    private final Rectangle player = new Rectangle(80, 60, 100, 100);

    private final Array<Rectangle> blocks = new Array<>();

    private float velocityX;
    private float velocityY;

    private boolean grounded;
    private boolean gameOver;

    private boolean moving;
    private boolean facingRight = true;

    public GamingScreen(Game game) {
        this.game = game;
    }

    private float startBlinkAngle = 0f;
    private float startAlpha = 1f;
    private BitmapFont startFont;
    private BitmapFont exitFont;

    private ShapeRenderer fadeRenderer;
    private float fadeAlpha = 0f;
    private static final float FADE_DURATION = 0.5f;
    @Override
    public void show() {
        batch = new SpriteBatch();

        standingPlayer = new StadingPlayer();
        rightMovingPlayer = new RightMovingPlayer();
        jumpingPlayer = new JumpingPlayer();
        fadeRenderer = new ShapeRenderer();

        blockTexture = new Texture(Gdx.files.internal("block.png"));
        backgroundTexture = new Texture(Gdx.files.internal("start_background.png"));
        downPlayerTexture = new Texture(Gdx.files.internal("down.png"));

        startFont = new BitmapFont();
        exitFont = new BitmapFont();

        camera = new OrthographicCamera();
        camera.setToOrtho(false, 1280, 720);

        viewport = new FitViewport(1280, 720, camera);
        viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), false);

        blocks.add(new Rectangle(0, 0, 1280, 60));

        player.set(80, 60, 300, 300);

        fadeAlpha = 0f;
        goToNextScreen = false;

        grounded = false;
        gameOver = false;
    }

    @Override
    public void render(float delta) {
        float dt = Math.min(Gdx.graphics.getDeltaTime(), 1f / 30f);

        startBlinkAngle += dt * 4.0f;
        float sine = MathUtils.sin(startBlinkAngle);
        startAlpha = 0.7f + 0.3f * (sine +1f) / 2f;
        if(!goToNextScreen && Gdx.input.isKeyJustPressed(Input.Keys.S)){
            goToNextScreen = true;
        }

        if(Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)){
            Gdx.app.exit();
        }

        if (!gameOver && !goToNextScreen) {
            update(dt);
            updateAnimation(dt);

        }

        ScreenUtils.clear(0f, 0f, 0f, 1f);

        viewport.apply();
        batch.setProjectionMatrix(camera.combined);

        batch.begin();

        batch.draw(backgroundTexture, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        for (Rectangle block: blocks) {
            batch.draw(blockTexture, block.x, block.y, block.width, block.height);

        }

        if (jumpAnimating()) {
            jumpingPlayer.draw(batch, player.x, player.y, player.width, player.height, facingRight);
        } else if (moving) {
            rightMovingPlayer.draw(batch, player.x, player.y, player.width, player.height);
        } else if(Gdx.input.isKeyPressed(Input.Keys.DOWN)){
            batch.draw(downPlayerTexture, player.x, player.y, player.width, player.height);
        } else {
            standingPlayer.draw(batch, player.x, player.y, player.width, heightForStand(player), facingRight);
        }

        batch.end();

        batch.begin();
        if (startFont != null) {
            startFont.setColor(1f, 1f, 1f, startAlpha);
            startFont.getData().setScale(2.0f);
            startFont.draw(batch, "Press 'S' to STARt", Gdx.graphics.getWidth() / 2f - 100f, 50f);
        }
        exitFont.setColor(0.35f,0.35f,0.35f,1f);
        exitFont.getData().setScale(1.5f);
        exitFont.draw(batch,"Press 'ESC' to Exit", Gdx.graphics.getWidth() / 2f - 80f, 90f);
        batch.end();

        if (goToNextScreen){
            fadeAlpha = Math.min(1f, fadeAlpha + dt / FADE_DURATION);

            Gdx.gl.glEnable(GL20.GL_BLEND);
            Gdx.gl.glBlendFunc(
                GL20.GL_SRC_ALPHA,
                GL20.GL_ONE_MINUS_SRC_ALPHA
            );

            fadeRenderer.setProjectionMatrix(camera.combined);
            fadeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            fadeRenderer.setColor(0f, 0f, 0f, fadeAlpha);

            fadeRenderer.rect(
                camera.position.x - camera.viewportWidth / 2f,
                camera.position.y - camera.viewportHeight / 2f,
                camera.viewportWidth,
                camera.viewportHeight
            );

            fadeRenderer.end();

            Gdx.gl.glDisable(com.badlogic.gdx.graphics.GL20.GL_BLEND);

            if (fadeAlpha >= 1f) {
                game.setScreen(new FirstGameScreen(game));
                dispose();
                return;
            }
        }
    }

    private float heightForStand(Rectangle r) {
        return r.height;
    }

    private void updateAnimation(float dt) {
        boolean nextMoving = velocityX != 0f;

        if (moving && !nextMoving) {
            standingPlayer.reset();
        }

        moving = nextMoving;

        if (velocityX > 0) {
            facingRight = true;
        } else if (velocityX < 0) {
            facingRight = false;
        }

        if (jumpAnimating()) {
            jumpingPlayer.update(dt);
        } else {
            rightMovingPlayer.update(dt, velocityX);

            if (!moving) {
                standingPlayer.update(dt);
            }
        }
    }

    private boolean jumpAnimating() {
        return false;
    }

    private void update(float dt) {
        if (gameOver) return;

        velocityX = 0;
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) velocityX -= MOVE_SPEED;
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) velocityX += MOVE_SPEED;

        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) && grounded) {
            velocityY = JUMP_SPEED;
            grounded = false;
            jumpingPlayer.reset();
        }

        velocityY = Math.max(velocityY - GRAVITY * dt, -900);

        moveX(dt);
        moveY(dt);

        if (player.y < -100) {
            player.setPosition(80, 60);
            velocityY = 0;
            grounded = false;
            jumpingPlayer.reset();
        }


        camera.position.x = WORLD_WIDTH / 2f;
        camera.position.y = 720f / 2f;
        camera.update();
    }

    private void moveX(float dt) {
        player.x += velocityX * dt;
        player.x = MathUtils.clamp(player.x, 0, WORLD_WIDTH - player.width);
    }

    private void moveY(float dt) {
        float dy = velocityY * dt;
        player.y += dy;
        if (player.y < 0) {
            player.y = 0;
            velocityY = 0;
            grounded = true;
        } else {
            grounded = false;
        }
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, false);
    }

    @Override
    public void pause() { }

    @Override
    public void resume() { }

    @Override
    public void hide() { }

    @Override
    public void dispose() {
        batch.dispose();

        fadeRenderer.dispose();
        jumpingPlayer.dispose();
        startFont.dispose();
        exitFont.dispose();

        standingPlayer.dispose();
        rightMovingPlayer.dispose();
        blockTexture.dispose();
        backgroundTexture.dispose();
        downPlayerTexture.dispose();
    }
}
