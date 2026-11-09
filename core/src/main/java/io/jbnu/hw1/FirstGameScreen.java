package io.jbnu.hw1;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class FirstGameScreen implements Screen {

    private static final float WORLD_WIDTH = 1280f;
    private static final float WORLD_HEIGHT = 720f;

    private static final float FADE_DURATION = 0.5f;
    private ShapeRenderer fadeRenderer;
    private float fadeAlpha = 0f;

    private final Game game;
    private final boolean afterAquarius;
    private final boolean afterPisces;
    private final boolean afterAries;
    private boolean storyFinished;

    private static final String CLEAR_BACKGROUND = "aquariusClear.png";

    private SpriteBatch batch;
    private Texture backgroundTexture;
    private Texture speechBalloon;
    private Texture blockTexture;

    private OrthographicCamera camera;
    private FitViewport viewport;

    private boolean showSpeech = false;
    private BitmapFont font;
    private BitmapFont dialogueFont;
    private String[] dialogues = new String[] {
        "Hey, we've lost our constellation.",
        "Wouldn't you like to go look for" + "\n" + "      it together?",
        "First, let's finding Aquarius."
    };
    private int dialogueIndex = 0;
    private String currentDialogue() { return dialogues[dialogueIndex]; }

    private StringBuilder currentText = new StringBuilder();
    private float charInterval = 0.04f;
    private float timeSinceLastChar = 0f;
    private boolean dialogueFinished = false;

    private float speechX = 60f;
    private float speechY = 120f;
    private float speechWidth = 135f;

    private GlyphLayout layout;

    private float backgroundAlpha;
    private float continueBlinkAngle = 0f;
    private float continueAlpha = 1f;
    private boolean goToNextScreen = false;

    public FirstGameScreen(Game game) {
        this(game, false);
    }

    public FirstGameScreen(Game game, boolean afterAquarius) { this(game, afterAquarius, false);}

    public FirstGameScreen(
        Game game,
        boolean afterAquarius,
        boolean afterPisces
    ) {
        this(game, afterAquarius, afterPisces, false);
    }

    public FirstGameScreen(Game game, boolean afterAquarius,
                           boolean afterPisces, boolean afterAries) {
        this.game = game;
        this.afterAquarius = afterAquarius;
        this.afterPisces = afterPisces;
        this.afterAries = afterAries;

        if (afterAries) {
            dialogues = new String[] {
                "You defeated Aries!",
                "Aries is shining again.",
                "Thank you for bringing it back."
            };
        } else if (afterPisces) {
            dialogues = new String[] {
                "You found all 11 stars!",
                "Pisces is shining again.",
                "Thank you for bringing it back.",
                "Our journey is not over yet.",
                "Let us find the next constellation."
            };
        } else if (afterAquarius) {
            dialogues = new String[] {
                "You found all 17 stars!",
                "Aquarius is shining again.",
                "Thank you for bringing it back.",
                "Let us go find\n"
                    + "the next constellation.",
                "The next constellation is Pisces."
            };
        }
    }

    @Override
    public void show() {
        batch = new SpriteBatch();

        speechBalloon = new Texture(Gdx.files.internal("speech.png"));

        String backgroundPath;

        if (afterAries) {
            backgroundPath = "ariesClear.png";
        } else if (afterPisces) {
            backgroundPath = "piscesClear.png";
        } else if (afterAquarius) {
            backgroundPath = CLEAR_BACKGROUND;
        } else {
            backgroundPath = "mainGaming.png";
        }

        if (!Gdx.files.internal(backgroundPath).exists()) {
            Gdx.app.log("FirstGameScreen", "Missing " + backgroundPath
                + "; using mainGaming.png instead.");
            backgroundPath = "mainGaming.png";
        }
        backgroundTexture = new Texture(Gdx.files.internal(backgroundPath));

        blockTexture = new Texture(Gdx.files.internal("block.png"));
        fadeRenderer = new ShapeRenderer();

        font = new BitmapFont();
        font.getData().setScale(1.0f);
        font.setColor(Color.WHITE);

        dialogueFont = new BitmapFont();
        dialogueFont.getData().setScale(1.5f);
        dialogueFont.setColor(Color.BLACK);

        layout = new GlyphLayout();

        camera = new OrthographicCamera();
        viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);
        viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);

        backgroundAlpha = 0f;
        storyFinished = false;
        dialogueFinished = false;
        showSpeech = false;
        currentText.setLength(0);
        timeSinceLastChar = 0f;
        dialogueIndex = 0;

        speechX = 60f;
        speechY = 120f;

        fadeAlpha = 0f;
        goToNextScreen = false;
    }

    @Override
    public void render(float delta) {

        if (afterAries && storyFinished
            && Gdx.input.isKeyJustPressed(Input.Keys.F5)) {
            game.setScreen(new GamingScreen(game));
            dispose();
            return;
        }
        float elapsed = Math.max(delta, 0f);
        float dt = Math.min(elapsed, 1f / 30f);

        if (!goToNextScreen && !storyFinished
            && Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            if (!showSpeech) {
                showSpeech = true;
                dialogueFinished = false;
                currentText.setLength(0);
                timeSinceLastChar = 0f;
            } else if (!dialogueFinished) {

                currentText.setLength(0);
                currentText.append(currentDialogue());
                dialogueFinished = true;
            } else {

                if (dialogueIndex + 1 < dialogues.length) {
                    dialogueIndex++;
                    currentText.setLength(0);
                    timeSinceLastChar = 0f;
                    dialogueFinished = false;
                } else {

                    if (afterAries) {

                        storyFinished = true;
                        showSpeech = false;
                    } else if (afterPisces) {

                        goToNextScreen = true;

                    } else if (afterAquarius) {

                        game.setScreen(new PiscesGame(game));
                        dispose();
                        return;

                    } else {

                        goToNextScreen = true;
                    }

                }
            }
        }

        if (showSpeech && !dialogueFinished) {
            timeSinceLastChar += dt;
            while (timeSinceLastChar >= charInterval && currentText.length() < currentDialogue().length()) {
                timeSinceLastChar -= charInterval;
                currentText.append(currentDialogue().charAt(currentText.length()));
            }
            if (currentText.length() >= currentDialogue().length()) {
                dialogueFinished = true;
            }
        }

        if (backgroundAlpha < 1f) {
            backgroundAlpha = Math.min(1f, backgroundAlpha + elapsed / FADE_DURATION);
        } else {
            update(dt);
        }

        ScreenUtils.clear(0f, 0f, 0f, 1f);

        viewport.apply();
        camera.update();

        batch.setProjectionMatrix(camera.combined);

        batch.begin();

        batch.setColor(1f, 1f, 1f, backgroundAlpha);
        batch.draw(backgroundTexture, 0f, 0f, WORLD_WIDTH, WORLD_HEIGHT);
        batch.setColor(1f, 1f, 1f, 1f);

        if (showSpeech) {

            batch.draw(speechBalloon, speechX - 60f, speechY - 30f, speechWidth + 280f, 180f);

            dialogueFont.draw(batch, currentText.toString(), speechX, speechY + 75f);

        }

        batch.draw(blockTexture, 0f, 0f, WORLD_WIDTH, 60f);

        continueBlinkAngle += dt * 4.0f;
        float sine = MathUtils.sin(continueBlinkAngle);
        continueAlpha = 0.7f + 0.3f * (sine + 1f) / 2f;
        font.setColor(1f, 1f, 1f, continueAlpha);
        String continueMessage = (afterAries && storyFinished)
            ? "ALL CLEAR! Press F5 to restart from the beginning"
            : "Press 'SPACE' to continue";
        layout.setText(font, continueMessage);
        font.draw(batch, layout, (WORLD_WIDTH - layout.width) / 2f, 40f);

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
                if (afterPisces) {
                    game.setScreen(new AriesGame(game));
                } else {
                    game.setScreen(new AquariusGame(game));
                }
                dispose();
                return;
            }
        }
    }

    private void update(float dt) {

    }

    private void drawGame() {

    }

    @Override
    public void resize(int width, int height) {
        if (viewport != null) {
            viewport.update(width, height, true);
        }
    }

    @Override
    public void pause() { }

    @Override
    public void resume() { }

    @Override
    public void hide() { }

    @Override
    public void dispose() {
        fadeRenderer.dispose();
        if (batch != null) {
            batch.dispose();
            batch = null;
        }
        if (backgroundTexture != null) {
            backgroundTexture.dispose();
            backgroundTexture = null;
        }
        if (speechBalloon != null) {
            speechBalloon.dispose();
            speechBalloon = null;
        }
        if (blockTexture != null) {
            blockTexture.dispose();
            blockTexture = null;
        }
        if (font != null) {
            font.dispose();
            font = null;
        }
        if (dialogueFont != null) {
            dialogueFont.dispose();
            dialogueFont = null;
        }
    }
}
