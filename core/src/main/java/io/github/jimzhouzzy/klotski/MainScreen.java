package io.github.jimzhouzzy.klotski;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import io.github.jimzhouzzy.klotski.GameModeScreen;
import io.github.jimzhouzzy.klotski.LoginScreen;

import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;

public class MainScreen implements Screen {

    final Klotski klotski;
    private int frameCount;
    private int frameCountOffset;
    private float baseTileSize;
    private Stage stage;
    private Skin skin;
    private Label greetingLabel; // Label to display the greeting message
    private ShapeRenderer shapeRenderer;
    private float offsetX; // Offset for translation animation
    private float offsetY;
    private float offsetZ;
    private Random random;
    private boolean moveForward;
    private boolean moveBackward;
    private boolean moveLeft;
    private boolean moveRight;
    private boolean moveShifted;
    private boolean moveUpward;
    private boolean moveDownward;
    private boolean rotateClockwise;
    private boolean rotateCounterClockwise;
    private float rotationAngle = 0f; // Rotation angle in degrees
    private float rotationSpeed = 10f; // Degrees per second
    private Color currentColor;
    private float colorChangeSpeed = 0.001f; // Speed of color change
    public Color[] colorList; // Predefined list of colors
    public Map<String, Color> colorCache; // Cache for storing colors
    public Map<String, Double> zPositionCache;
    public Map<String, Double> zPositionTempCache;
    public Map<String, Double> yRotationCache;
    public Map<String, Boolean> triggerYRotation; // Flag to trigger Y rotation
    private boolean triggerYRotationAnimation;
    private int yRotationAnimationTemp;
    private int yRotationAnimationStartingRow;
    private float yRotationAnimationStartingOffsetY;
    private boolean mutateColorFollowing;
    private float generateSmoothChangingColorTime = 0.0f;
    private List<Color> targetColors; // List of target colors
    private int currentColorIndex = 0; // Index of the current base color
    private float interpolationFactor = 0f;
    private float interpolationSpeedMultiplier = 1f; // Speed of color interpolation
    private List<Vector2[]> topRectangleVectors;
    private List<Float> topRectangleYs;

    public MainScreen(final Klotski klotski) {
        this.klotski = klotski;
        random = new Random();
        colorCache = new HashMap<>();
        yRotationCache = new HashMap<>();
        zPositionCache = new HashMap<>();
        zPositionTempCache = new HashMap<>();
        triggerYRotation = new HashMap<>();

        triggerYRotationAnimation = false;
        moveForward = false;
        moveBackward = false;
        moveRight = false;
        moveLeft = false;
        moveShifted = false;
        moveUpward = false;
        moveDownward = false;
        mutateColorFollowing = false;

        baseTileSize = 50f;

        offsetX = baseTileSize / 2;
        offsetY = 0f;
        offsetZ = 0f;

        frameCount = 0;
        frameCountOffset = random.nextInt(10000); // Random offset for the frame count
        yRotationAnimationTemp = 0;

        create();
    }

    public void create() {
        this.stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);
        klotski.dynamicBoard.setStage(stage);

        // Load colors
        loadColors();

        // Load the skin for UI components
        skin = new Skin(Gdx.files.internal("skins/comic/skin/comic-ui.json"));

        // Create a table for layout
        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        // Add a title label
        Label.LabelStyle titleStyle = skin.get("title", Label.LabelStyle.class);
        Label titleLabel = new Label("Klotski Game", titleStyle);
        titleLabel.setFontScale(1.5f); // Make the title larger
        titleLabel.setColor(Color.WHITE); // Set the title color to white
        table.add(titleLabel).padBottom(50).row();

        // Add a greeting label
        Label.LabelStyle narrationStyle = skin.get("narration", Label.LabelStyle.class);
        String username = klotski.getLoggedInUser();
        String greetingText = username != null ? "Welcome, " + username + "!" : "Welcome, Guest!";
        this.greetingLabel = new Label(greetingText, narrationStyle);
        greetingLabel.setFontScale(2.0f);
        greetingLabel.setColor(Color.WHITE); // Set the title color to white
        table.add(greetingLabel).padBottom(30).row();

        // Add a "Play" button
        TextButton playButton = new TextButton("Play", skin);
        playButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                klotski.playClickSound();;
                klotski.setScreen(new GameModeScreen(klotski)); // Navigate to the GameModeScreen
                klotski.dynamicBoard.triggerAnimateFocalLength(10000.0f, 1.0f);
            }
        });
        table.add(playButton).width(200).height(50).padBottom(20).row();

        // Add a "Login" button
        TextButton loginButton = new TextButton("Login", skin);
        loginButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                klotski.playClickSound();;
                klotski.setScreen(new LoginScreen(klotski)); // Navigate to the LoginScreen
            }
        });
        table.add(loginButton).width(200).height(50).padBottom(20).row();

        // Add a "Settings" button
        TextButton settingsButton = new TextButton("Settings", skin);
        settingsButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                klotski.playClickSound();;
                klotski.setScreen(klotski.settingsScreen); // Navigate to the SettingsScreen
            }
        });
        table.add(settingsButton).width(200).height(50).padBottom(20).row();
        //Add a "Help" button
        TextButton helpButton = new TextButton("Help", skin);
        helpButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                klotski.playClickSound();
                klotski.setScreen(klotski.helpScreen);
            }
        });
        table.add(helpButton).width(200).height(50).padBottom(20).row();


        // Add an "Exit" button
        TextButton exitButton = new TextButton("Exit", skin);
        exitButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                klotski.playClickSound();;
                Gdx.app.exit(); // Exit the application
            }
        });
        table.add(exitButton).width(200).height(50);
    }

    @Override
    public void render(float delta) {
        klotski.dynamicBoard.render(delta);
        stage.act(delta);
        stage.draw();
    }

    // Projection 'Matrix'
    private Vector2 projectPerspective(float x, float y, float focal, float cx, float cy) {
        float scale = focal / (focal + y);
        float screenX = cx + (x - cx) * scale;
        float screenY = cy + (y - cy) * scale;
        return new Vector2(screenX, screenY);
    }

    private static double simpleComplexFunction(double x) {
        return Math.sin(x);
    }

    private static double simpleComplexFunction(float x) {
        return simpleComplexFunction((double) x);
    }

    // A very complex function in [0, 1] to shake the camera
    private static double veryComplexFunction(double x) {
        double term1 = 0.5 * (Math.sin(5 * Math.PI * x) * Math.cos(3 * Math.PI * x * x) + 1);
        double term2 = 0.1 * Math.sin(20 * Math.PI * x);
        return term1 * Math.exp(-x) + term2;
    }

    private static double veryComplexFunction(float x) {
        return veryComplexFunction((double) x);
    }

    @Override
    public void resize(int width, int height) {
        klotski.dynamicBoard.resize(width, height);
        stage.getViewport().update(width, height, true);
        // klotski.dynamicBoard = new DynamicBoard(klotski, stage);
    }

    @Override
    public void dispose() {
        stage.dispose();
        skin.dispose();
    }

    @Override
    public void hide() {
        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void resume() {
    }

    @Override
    public void pause() {
    }

    @Override
    public void show() {
        // We need to re-create the stage to ensure the dynamic components are updated
        // e.g. the displayed username
        dispose();
        create();
        Gdx.input.setInputProcessor(stage);
    }

    public void loadColors() {
        // Predefined list of colors
        colorCache.clear();

        // Curently just get Light color, and dark is adopted when rendering
        colorList = klotski.getMainScreenLightColorList();

        for (int i = -500; i <= 500; i++) {
            // This control the length of defined color blocks
            // currently we are not showing them, but we can use them to generate colors
            // e.g. for (int j = -50; j <= 50; j++) {
            for (int j = -50; j <= -5; j++) {
                String key = i + "," + j;
                if (!colorCache.containsKey(key)) {
                    Color chosenColor = colorList[random.nextInt(colorList.length)];
                    colorCache.put(key, chosenColor);
                }
            }
        }
    }

    public Color generateSimilarColor(Color baseColor, float variability, float offset, float limit) {
        Random random = new Random();

        // Generate small random offsets for RGB values
        float redOffset = (random.nextFloat() - 0.5f) * variability;
        float greenOffset = (random.nextFloat() - 0.5f) * variability;
        float blueOffset = (random.nextFloat() - 0.5f) * variability;

        // Clamp the values to ensure they remain between 0 and 1
        float newRed = Math.min(Math.max(baseColor.r + redOffset, 0) + offset, 1);
        float newGreen = 0.9f * Math.min(Math.max(baseColor.g + greenOffset, 0) + offset, 1); // Eliminate green
        float newBlue = Math.min(Math.max(baseColor.b + blueOffset, 0) + offset, 1);

        // Create the new color
        Color newColor = new Color(newRed, newGreen, newBlue, baseColor.a); // Preserve the alpha value

        // Adjust luminance if necessary
        float luminance = calculateLuminance(newColor);
        if (klotski.klotskiTheme == klotski.klotskiTheme.LIGHT && luminance < 0.3f) {
            if (variability > 0.01f * limit) {
                return generateSimilarColor(baseColor, 0.5f * variability, 0.2f, 1.0f);
            } else {
                return baseColor;
            }
        } else if (klotski.klotskiTheme != klotski.klotskiTheme.LIGHT && luminance > 0.8f) {
            if (variability > 0.01f * limit) {
                return generateSimilarColor(baseColor, 0.5f * variability, -0.2f, 1.0f);
            } else {
                return baseColor;
            }
        }

        return newColor;
    }

    public Color generateSmoothChangingColor(float delta) {
        if (targetColors == null) {
            targetColors = new ArrayList<>();
            targetColors.add(new Color(204 / 255f, 204 / 255f, 255 / 255f, 1)); // rgb(204, 204, 255)
            targetColors.add(new Color(255 / 255f, 204 / 255f, 153 / 255f, 1)); // rgb(255, 204, 153)
            targetColors.add(new Color(255 / 255f, 153 / 255f, 255 / 255f, 1)); // rgb(255, 153, 255)
            targetColors.add(new Color(153 / 255f, 255 / 255f, 204 / 255f, 1)); // rgb(153, 255, 204)
            targetColors.add(new Color(51 / 255f, 153 / 255f, 255 / 255f, 1)); // rgb(51, 153, 255)
        }

        Color currentBaseColor = targetColors.get(currentColorIndex);
        Color nextBaseColor = targetColors.get((currentColorIndex + 1) % targetColors.size());

        interpolationFactor += 0.3 * colorChangeSpeed * interpolationSpeedMultiplier;
        if (interpolationFactor > 1f) {
            interpolationFactor = 0f;
            currentColorIndex = (currentColorIndex + 1) % targetColors.size();
            currentBaseColor = nextBaseColor;
            nextBaseColor = targetColors.get(random.nextInt(targetColors.size()));
            if (currentBaseColor == nextBaseColor) {
                nextBaseColor = targetColors.get((currentColorIndex + 1) % targetColors.size());
            }
            // Randomize the interpolation speed
            interpolationSpeedMultiplier = random.nextFloat() * 0.5f + 0.5f; // Randomize the speed
            if (interpolationSpeedMultiplier > 1.5f) {
                interpolationSpeedMultiplier = 1.5f; // Limit the maximum speed
            }
            if (interpolationSpeedMultiplier < 0.8f) {
                interpolationSpeedMultiplier = 0.8f; // Limit the minimum speed
            }
        }
        float t = interpolationFactor;
        t = t * t * (3 - 2 * t); // smoothstep(t)

        float red   = currentBaseColor.r + t * (nextBaseColor.r - currentBaseColor.r);
        float green = currentBaseColor.g + t * (nextBaseColor.g - currentBaseColor.g);
        float blue  = currentBaseColor.b + t * (nextBaseColor.b - currentBaseColor.b);

        currentColor = new Color(red, green, blue, 1.0f);
        return currentColor;
    }

    private float calculateLuminance(Color color) {
        // Use the standard formula for relative luminance
        return 0.2126f * color.r + 0.7152f * color.g + 0.0722f * color.b;
    }

    private float calculateLuminance1(Color color) {
        // Use the standard formula for relative luminance
        return 0.5f * color.r + 0.5f * color.g + 0.5f * color.b;
    }

    private Vector2 applyRotation(float x, float y, float centerX, float centerY, float angle) {
        float radians = (float) Math.toRadians(angle);
        float cos = (float) Math.cos(radians);
        float sin = (float) Math.sin(radians);

        // Translate point to origin
        float translatedX = x - centerX;
        float translatedY = y - centerY;

        // Apply rotation
        float rotatedX = translatedX * cos - translatedY * sin;
        float rotatedY = translatedX * sin + translatedY * cos;

        // Translate point back
        return new Vector2(rotatedX + centerX, rotatedY + centerY);
    }

    private void updateAllYRotationCache() {
        for (Map.Entry<String, Double> entry : yRotationCache.entrySet()) {
            String key = entry.getKey();
            double yRotation = entry.getValue();
            if (yRotation > 2 * Math.PI) {
                yRotation = 0; // Reset if it exceeds 2π
            }
            yRotation += 0.01; // Increment by 0.01 radians
            yRotationCache.put(key, yRotation);
        }
    }

    private void updateYRotationCache(String key) {
        double yRotation = yRotationCache.getOrDefault(key, 0.0);
        if (yRotation > 2 * Math.PI) {
            yRotation = 0; // Reset if it exceeds 2π
        }
        yRotation += 0.05; // Increment by 0.01 radians
        yRotationCache.put(key, yRotation);
    }

    private void updateZPositionCache() {
        for (Map.Entry<String, Double> entry : zPositionCache.entrySet()) {
            String key = entry.getKey();
            // Although I added default value, it is a MUST NEED to ENSURE every position
            // has a temp
            double zPositionTemp = zPositionTempCache.getOrDefault(key, 0.0);
            double zPosition = entry.getValue();
            if (zPositionTemp > 2 * Math.PI) {
                zPositionTemp = 0; // Reset if it exceeds 2π
            }
            zPositionTemp += 0.1; // Increment by 0.01 radians
            zPosition = 100 * Math.sin(zPositionTemp);
            zPositionCache.put(key, zPosition);
        }
    }

    public void drawTopRectangle(Vector2 tl, Vector2 tr, Vector2 bl, Vector2 br, float y, Color tileColor, float yRotateAngle) {
        float screenHeight = Gdx.graphics.getHeight();

        // Draw the tile
        Gdx.gl.glLineWidth(1f); // Set line width
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(tileColor);
        shapeRenderer.triangle(tl.x, tl.y, tr.x, tr.y, br.x, br.y);
        shapeRenderer.triangle(br.x, br.y, bl.x, bl.y, tl.x, tl.y);
        shapeRenderer.end();

        // Draw the top-left highlight border

        Gdx.gl.glLineWidth(Math.max(1f, 6f * (float) Math.abs(Math.cos(yRotateAngle) * (1 - y / screenHeight)))); // Set line width
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(tileColor.cpy().mul(1.2f)); // Lighter color for highlight
        shapeRenderer.line(tl.x, tl.y, tr.x, tr.y); // Top edge
        shapeRenderer.line(tl.x, tl.y, bl.x, bl.y); // Left edge
        shapeRenderer.end();

        // Draw the bottom-right shadow border
        Gdx.gl.glLineWidth(Math.max(1f, 12f * (float) Math.abs(Math.cos(yRotateAngle) * (1 - y / screenHeight)))); // Set line width
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(tileColor.cpy().mul(0.6f)); // Darker color for shadow
        shapeRenderer.line(bl.x, bl.y, br.x, br.y); // Bottom edge
        shapeRenderer.line(tr.x, tr.y, br.x, br.y); // Right edge
        shapeRenderer.end();

        Gdx.gl.glLineWidth(1f); // Set back line width
    }

    private boolean isPointInQuadrilateral(Vector2 point, Vector2 tl, Vector2 tr, Vector2 bl, Vector2 br) {
        // Check if the point is in either of the two triangles
        return isPointInTriangle(point, tl, tr, br) || isPointInTriangle(point, tl, br, bl);
    }

    private boolean isPointInTriangle(Vector2 point, Vector2 v1, Vector2 v2, Vector2 v3) {
        // Calculate vectors
        Vector2 v1v2 = v2.cpy().sub(v1);
        Vector2 v2v3 = v3.cpy().sub(v2);
        Vector2 v3v1 = v1.cpy().sub(v3);

        // Calculate vectors from the point to the vertices
        Vector2 v1p = point.cpy().sub(v1);
        Vector2 v2p = point.cpy().sub(v2);
        Vector2 v3p = point.cpy().sub(v3);

        // Calculate cross products
        float cross1 = v1v2.crs(v1p);
        float cross2 = v2v3.crs(v2p);
        float cross3 = v3v1.crs(v3p);

        // Check if all cross products have the same sign
        return (cross1 >= 0 && cross2 >= 0 && cross3 >= 0) || (cross1 <= 0 && cross2 <= 0 && cross3 <= 0);
    }

    public void triggerFlip() {
        if (!triggerYRotationAnimation || true) {
            yRotationAnimationStartingOffsetY = offsetY;
            yRotationAnimationStartingRow = (int) Math.floor(yRotationAnimationStartingOffsetY / baseTileSize);
            yRotationAnimationTemp = 0;
            triggerYRotationAnimation = true;
            System.out.println("Triggering flip animation, with starting row: " + yRotationAnimationStartingRow);
        }
    }

    public void triggerFlip(int row) {
        if (!triggerYRotationAnimation || true) {
            yRotationAnimationStartingOffsetY = row * baseTileSize;
            yRotationAnimationStartingRow = row;
            yRotationAnimationTemp = 0;
            triggerYRotationAnimation = true;
            System.out.println("Triggering flip animation, with starting row: " + yRotationAnimationStartingRow);
        }
    }

    private float[] rgbToHsl(float r, float g, float b) {
        // Normalize RGB values to [0, 1]
        r = Math.min(Math.max(r, 0), 1);
        g = Math.min(Math.max(g, 0), 1);
        b = Math.min(Math.max(b, 0), 1);

        float max = Math.max(r, Math.max(g, b));
        float min = Math.min(r, Math.min(g, b));
        float delta = max - min;

        float h = 0, s = 0, l = (max + min) / 2;

        if (delta != 0) {
            // Calculate saturation
            s = l < 0.5f ? delta / (max + min) : delta / (2 - max - min);

            // Calculate hue
            if (max == r) {
                h = (g - b) / delta + (g < b ? 6 : 0);
            } else if (max == g) {
                h = (b - r) / delta + 2;
            } else if (max == b) {
                h = (r - g) / delta + 4;
            }
            h /= 6;
        }

        return new float[]{h, s, l};
    }

    private float[] hslToRgb(float h, float s, float l) {
        float r, g, b;

        if (s == 0) {
            // Achromatic (gray)
            r = g = b = l;
        } else {
            float q = l < 0.5f ? l * (1 + s) : l + s - l * s;
            float p = 2 * l - q;
            r = hueToRgb(p, q, h + 1f / 3f);
            g = hueToRgb(p, q, h);
            b = hueToRgb(p, q, h - 1f / 3f);
        }

        return new float[]{r, g, b};
    }

    private float hueToRgb(float p, float q, float t) {
        if (t < 0) t += 1;
        if (t > 1) t -= 1;
        if (t < 1f / 6f) return p + (q - p) * 6 * t;
        if (t < 1f / 2f) return q;
        if (t < 2f / 3f) return p + (q - p) * (2f / 3f - t) * 6;
        return p;
    }
}
