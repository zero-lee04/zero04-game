package io.jbnu.hw1;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class AriesGame implements Screen {
    private static final int WORLD_WIDTH = 1280;
    private static final int WORLD_HEIGHT = 720;
    private static final int MAX_PLAYER_HP = 5;
    private static final int MAX_BOSS_HP = 100;
    private final Game game;
    private boolean paused = false;
    private static final float INTRO_FADE_DURATION = 0.5f;
    private float introFadeAlpha = 1f;
    private Texture pauseOverlayTexture;
    private final GlyphLayout pauseTextLayout = new GlyphLayout();
    private OrthographicCamera camera;
    private FitViewport viewport;
    private ShapeRenderer shapes;
    private SpriteBatch batch;
    private BitmapFont font;
    private Texture ramBossTexture;
    private StarTexture enemyStar;
    private Texture backgroundTexture;
    private final Rectangle player = new Rectangle();
    private final Rectangle boss = new Rectangle();
    private final Array<Shot> playerShots = new Array<>();
    private final Array<Shot> enemyShots = new Array<>();
    private int playerHp;
    private int bossHp;
    private float elapsed;
    private float shootCooldown;
    private float invincible;
    private int attackNumber;
    private int pattern;
    private float patternTime;
    private float patternShotTimer;
    private float spiralAngle;
    private float safeGapX;
    private final Array<Laser> lasers = new Array<>();

    private static final String[] PATTERN_NAMES = {
        "SPIRAL HORNS", "HUNTER VOLLEY", "STAR RAIN", "HORN LASERS"
    };

    private static class Laser {
        final Rectangle area;
        float age;
        Laser(float x) { area = new Rectangle(x - 27f, 20f, 54f, 600f); }
        boolean active() { return age >= 1.05f && age < 1.65f; }
    }
    private boolean won;
    private boolean lost;

    private static class Shot {
        final Rectangle rect;
        final float vx;
        final float vy;
        Shot(float x, float y, float size, float vx, float vy) {
            rect = new Rectangle(x - size / 2f, y - size / 2f, size, size);
            this.vx = vx;
            this.vy = vy;
        }
        void move(float dt) {
            rect.x += vx * dt;
            rect.y += vy * dt;
        }
        boolean outside() {
            return rect.x + rect.width < 0 || rect.x > WORLD_WIDTH
                || rect.y + rect.height < 0 || rect.y > WORLD_HEIGHT;
        }
    }

    public AriesGame(Game game) {
        this.game = game;
    }

    @Override
    public void show() {

        Pixmap pausePixel = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pausePixel.setColor(Color.WHITE);
        pausePixel.fill();
        pauseOverlayTexture = new Texture(pausePixel);
        pausePixel.dispose();
        camera = new OrthographicCamera();
        viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);
        viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
        shapes = new ShapeRenderer();
        batch = new SpriteBatch();
        font = new BitmapFont();
        font.getData().setScale(1.4f);
        backgroundTexture = new Texture(Gdx.files.internal("piscesClear.png"));
        ramBossTexture = new Texture(Gdx.files.internal("ramBoss.png"));
        ramBossTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

        enemyStar = new StarTexture();
        reset();
    }

    private void reset() {
        introFadeAlpha = 1f;
        paused = false;
        player.set(WORLD_WIDTH / 2f - 18f, 85f, 36f, 36f);
        boss.set(WORLD_WIDTH / 2f - 75f, 515f, 300f, 200f);
        playerShots.clear();
        enemyShots.clear();
        playerHp = MAX_PLAYER_HP;
        bossHp = MAX_BOSS_HP;
        elapsed = 0f;
        enemyStar.reset();
        shootCooldown = 0f;
        invincible = 0f;
        attackNumber = 0;
        pattern = 0;
        patternTime = 0f;
        patternShotTimer = 0.8f;
        spiralAngle = -150f;
        safeGapX = WORLD_WIDTH / 2f;
        lasers.clear();
        won = false;
        lost = false;
    }

    @Override
    public void render(float delta) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            paused = !paused;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.F5)) reset();

        if (introFadeAlpha > 0f) {
            if (!paused) {
                introFadeAlpha = Math.max(0f,
                    introFadeAlpha - Math.max(delta, 0f) / INTRO_FADE_DURATION);
            }
            draw();
            return;
        }

        float remaining = MathUtils.clamp(delta, 0f, 0.1f);
        while (remaining > 0f && !paused && !won && !lost) {
            float dt = Math.min(remaining, 1f / 120f);
            update(dt);
            remaining -= dt;
        }
        if (won) {
            game.setScreen(new FirstGameScreen(game, false, false, true));
            dispose();
            return;
        }
        draw();

    }

    private void update(float dt) {
        elapsed += dt;
        enemyStar.update(dt);
        invincible = Math.max(0f, invincible - dt);
        float dx = 0f;
        float dy = 0f;
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT) || Gdx.input.isKeyPressed(Input.Keys.A)) dx--;
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT) || Gdx.input.isKeyPressed(Input.Keys.D)) dx++;
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN) || Gdx.input.isKeyPressed(Input.Keys.S)) dy--;
        if (Gdx.input.isKeyPressed(Input.Keys.UP) || Gdx.input.isKeyPressed(Input.Keys.W)) dy++;
        float length = (float) Math.sqrt(dx * dx + dy * dy);
        if (length > 0f) {
            player.x += dx / length * 350f * dt;
            player.y += dy / length * 350f * dt;
        }
        player.x = MathUtils.clamp(player.x, 0f, WORLD_WIDTH - player.width);

        player.y = MathUtils.clamp(player.y, 20f, 430f - player.height);
        boss.x = WORLD_WIDTH / 2f - boss.width / 2f + MathUtils.sin(elapsed * 0.8f) * 370f;
        shootCooldown -= dt;
        if (Gdx.input.isKeyPressed(Input.Keys.SPACE) && shootCooldown <= 0f) {
            playerShots.add(new Shot(player.x + player.width / 2f,
                player.y + player.height + 9f, 10f, 0f, 700f));
            shootCooldown = 0.15f;
        }
        updateBossPatterns(dt);
        if (lost) return;
        for (int i = playerShots.size - 1; i >= 0; i--) {
            Shot shot = playerShots.get(i);
            shot.move(dt);
            if (shot.rect.overlaps(boss)) {
                playerShots.removeIndex(i);
                bossHp = Math.max(0, bossHp - 2);
                if (bossHp == 0) {
                    won = true;
                    enemyShots.clear();
                    lasers.clear();
                    return;
                }
            } else if (shot.outside()) {
                playerShots.removeIndex(i);
            }
        }
        for (int i = enemyShots.size - 1; i >= 0; i--) {
            Shot shot = enemyShots.get(i);
            shot.move(dt);
            if (shot.rect.overlaps(player)) {
                enemyShots.removeIndex(i);
                if (invincible <= 0f) {
                    playerHp--;
                    invincible = 1.2f;
                    if (playerHp <= 0) {
                        lost = true;
                        return;
                    }
                }
            } else if (shot.outside()) {
                enemyShots.removeIndex(i);
            }
        }
    }

    private boolean enraged() {
        return bossHp <= MAX_BOSS_HP / 2;
    }

    private void emit(float x, float y, float angle, float speed, float size) {
        enemyShots.add(new Shot(x, y, size,
            MathUtils.cosDeg(angle) * speed, MathUtils.sinDeg(angle) * speed));
    }

    private void updateBossPatterns(float dt) {

        for (int i = lasers.size - 1; i >= 0; i--) {
            Laser laser = lasers.get(i);
            laser.age += dt;
            if (laser.age >= 1.85f) {
                lasers.removeIndex(i);
            } else if (laser.active() && invincible <= 0f
                && laser.area.overlaps(player)) {
                playerHp--;
                invincible = 1.2f;
                if (playerHp <= 0) {
                    lost = true;
                    return;
                }
            }
        }
        patternTime += dt;
        if (patternTime >= 6f) {
            patternTime = 0f;
            pattern = (pattern + 1) % PATTERN_NAMES.length;
            patternShotTimer = 0.65f;
            attackNumber = 0;
            safeGapX = MathUtils.clamp(player.x + player.width / 2f, 150f, WORLD_WIDTH - 150f);
        }
        patternShotTimer -= dt;
        if (patternShotTimer > 0f) return;
        float x = boss.x + boss.width / 2f;
        float y = boss.y - 12f;
        boolean rage = enraged();
        attackNumber++;
        switch (pattern) {
            case 0:

                spiralAngle += rage ? 19f : 15f;
                float a = -155f + (spiralAngle + 3600f) % 130f;
                emit(x - 45f, y, a, rage ? 260f : 215f, 14f);
                emit(x + 45f, y, -180f - a, rage ? 260f : 215f, 14f);
                if (rage && attackNumber % 6 == 0) emit(x, y, -90f, 200f, 14f);
                patternShotTimer = rage ? 0.18f : 0.24f;
                break;
            case 1:

                float aim = MathUtils.atan2(player.y + player.height / 2f - y,
                    player.x + player.width / 2f - x) * MathUtils.radiansToDegrees;
                int spread = 1;
                for (int j = -spread; j <= spread; j++) {
                    emit(x, y, aim + j * 13f, rage ? 315f : 270f, 16f);
                }

                patternShotTimer = attackNumber % 3 == 0 ? 1.2f : 0.34f;
                break;
            case 2:

                float gapHalf = rage ? 120f : 150f;
                for (float column = 35f; column < WORLD_WIDTH; column += 80f) {
                    float offset = attackNumber % 2 == 0 ? 14f : -14f;
                    float rainX = column + offset;
                    if (Math.abs(rainX - safeGapX) < gapHalf) continue;
                    emit(rainX, 620f, -90f, rage ? 265f : 220f, 16f);
                }
                patternShotTimer = rage ? 1.0f : 1.25f;
                break;
            case 3:

                float target = MathUtils.clamp(player.x + player.width / 2f, 35f, WORLD_WIDTH - 35f);
                lasers.add(new Laser(target));
                if (rage) {
                    float second = target < WORLD_WIDTH / 2f ? target + 230f : target - 230f;
                    lasers.add(new Laser(MathUtils.clamp(second, 35f, WORLD_WIDTH - 35f)));
                }

                for (int j = 0; j < 3; j++) emit(x, y, -135f + j * 45f, 175f, 14f);
                patternShotTimer = 2.1f;
                break;
            default:
                break;
        }
    }

    private void draw() {
        ScreenUtils.clear(0.025f, 0.035f, 0.10f, 1f);
        viewport.apply();
        camera.update();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        batch.setColor(Color.WHITE);
        batch.draw(backgroundTexture, 0f, 0f, WORLD_WIDTH, WORLD_HEIGHT);
        batch.end();

        shapes.setProjectionMatrix(camera.combined);
        shapes.begin(ShapeRenderer.ShapeType.Filled);

        shapes.setColor(0.35f, 0.4f, 0.6f, 1f);
        for (int i = 0; i < 65; i++) {
            shapes.circle((i * 193 + 31) % 1280, (i * 137 + 71) % 720, 1.5f);
        }

        for (Laser laser : lasers) {
            if (laser.active()) {
                shapes.setColor(1f, 0.18f, 0.25f, 1f);
                shapes.rect(laser.area.x, laser.area.y, laser.area.width, laser.area.height);
                shapes.setColor(1f, 0.85f, 0.65f, 1f);
                shapes.rect(laser.area.x + 21f, laser.area.y, 12f, laser.area.height);
            } else if (laser.age < 1.05f) {
                shapes.setColor(1f, 0.65f, 0.12f, 1f);
                shapes.rect(laser.area.x, laser.area.y, 2f, laser.area.height);
                shapes.rect(laser.area.x + laser.area.width - 2f, laser.area.y, 2f, laser.area.height);
                if (((int) (laser.age * 12f)) % 2 == 0) {
                    shapes.rect(laser.area.x + laser.area.width / 2f - 1f,
                        laser.area.y, 2f, laser.area.height);
                }
            }
        }
        if (pattern == 2 && !won && !lost) {
            shapes.setColor(0.2f, 0.75f, 0.6f, 1f);
            shapes.rect(safeGapX - 35f, 45f, 70f, 4f);
        }
        if (invincible <= 0f || ((int) (invincible * 12f)) % 2 == 0) {
            shapes.setColor(Color.CYAN);
            shapes.triangle(player.x, player.y, player.x + player.width, player.y,
                player.x + player.width / 2f, player.y + player.height);
        }
        shapes.setColor(Color.YELLOW);
        for (Shot shot : playerShots) {
            shapes.rect(shot.rect.x, shot.rect.y, shot.rect.width, shot.rect.height);
        }
        shapes.setColor(Color.DARK_GRAY);
        shapes.rect(390f, 665f, 500f, 18f);
        shapes.setColor(Color.SCARLET);
        shapes.rect(390f, 665f, 500f * bossHp / MAX_BOSS_HP, 18f);
        shapes.setColor(Color.DARK_GRAY);
        shapes.rect(25f, 655f, 200f, 18f);
        shapes.setColor(Color.GREEN);
        shapes.rect(25f, 655f, 200f * playerHp / MAX_PLAYER_HP, 18f);
        shapes.end();
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        batch.setColor(Color.WHITE);
        drawBoss();

        for (Shot shot : enemyShots) {
            float starScale = 7.0f;
            float drawWidth = shot.rect.width * starScale;
            float drawHeight = shot.rect.height * starScale;

            enemyStar.draw(
                batch,
                shot.rect.x - (drawWidth - shot.rect.width) / 2f,
                shot.rect.y - (drawHeight - shot.rect.height) / 2f,
                drawWidth,
                drawHeight,
                true
            );
        }
        font.setColor(Color.WHITE);
        font.draw(batch, "PLAYER HP: " + playerHp + " / " + MAX_PLAYER_HP, 25f, 700f);
        font.draw(batch, "RAM BOSS   HP: " + bossHp + " / " + MAX_BOSS_HP, 390f, 710f);
        font.draw(batch, "Move: WASD    Attack Hold: SPACE    Restart: F5", 25f, 25f);
        if (won) {
            font.setColor(Color.YELLOW);
            font.draw(batch, "VICTORY! Aries has been defeated!", 410f, 370f);
            font.draw(batch, "Press F5 to play again", 490f, 330f);
        } else if (lost) {
            font.setColor(Color.SALMON);
            font.draw(batch, "GAME OVER - Press F5 to retry", 425f, 360f);
        } else if (bossHp <= MAX_BOSS_HP / 2) {
            font.setColor(Color.SALMON);
            font.draw(batch, "ENRAGED! Dodge the faster attacks!", 430f, 640f);
        }
        if (!won && !lost) {
            font.setColor(Color.WHITE);
            font.draw(batch, "Pattern: " + PATTERN_NAMES[pattern], 900f, 700f);
            if (pattern == 3) {
                font.setColor(Color.ORANGE);
                font.draw(batch, "WARNING: Leave the marked lanes!", 430f, 605f);
            } else if (pattern == 2) {
                font.setColor(Color.CYAN);
                font.draw(batch, "Find the gap in the rain!", 470f, 605f);
            }
        }
        if (introFadeAlpha > 0f) {
            batch.enableBlending();
            batch.setColor(0f, 0f, 0f, introFadeAlpha);
            batch.draw(pauseOverlayTexture, 0f, 0f, WORLD_WIDTH, WORLD_HEIGHT);
            batch.setColor(Color.WHITE);
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
            font.setColor(Color.WHITE);
            pauseTextLayout.setText(font, "PAUSED - Press ESC to resume");
            font.draw(batch, pauseTextLayout,
                camera.position.x - pauseTextLayout.width / 2f,
                camera.position.y + pauseTextLayout.height / 2f);
        }
        batch.end();
    }

    private void drawBoss() {

        float scale = Math.min(boss.width / ramBossTexture.getWidth(),
            boss.height / ramBossTexture.getHeight());
        float width = ramBossTexture.getWidth() * scale;
        float height = ramBossTexture.getHeight() * scale;
        batch.draw(ramBossTexture, boss.x + (boss.width - width) / 2f,
            boss.y + (boss.height - height) / 2f, width, height);
    }

    @Override
    public void resize(int width, int height) {
        if (viewport != null) viewport.update(width, height, true);
    }
    @Override public void pause() { }
    @Override public void resume() { }
    @Override public void hide() { }
    @Override
    public void dispose() {
        if (pauseOverlayTexture != null) {
            pauseOverlayTexture.dispose();
            pauseOverlayTexture = null;
        }
        if (shapes != null) { shapes.dispose(); shapes = null; }
        if (batch != null) { batch.dispose(); batch = null; }
        if (font != null) { font.dispose(); font = null; }
        if (ramBossTexture != null) { ramBossTexture.dispose(); ramBossTexture = null; }
        if (enemyStar != null) { enemyStar.dispose(); enemyStar = null; }
        if (backgroundTexture != null) {
            backgroundTexture.dispose();
            backgroundTexture = null;
        }

    }
}
