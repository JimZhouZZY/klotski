/**
 * DynmaicBoard.java
 * 
 * This class represents a dynamic board for the Klotski game.
 * It handles the rendering of the backgorund board, including
 * the tiles, colors, and animations.
 * 
 * It also handles user input for moving and rotating the tiles.
 * 
 * It should only be initialized once in the {@link Klotski} class,
 * 
 * It should be staged and displayed with effective input listener
 * in any non-gaming screen (sub-classes of {@link ProtoScreen}),
 * which is why it is rerendered in the {@link ProtoScreen} class.
 * 
 * It is extended by the {@link KlotzkiBoard} class to provide a 
 * basic board for the **Klotzki** game.
 * 
 * @author JimZhouZZY
 * @version 1.25
 * @since 2025-5-25
 * @see {@link Klotski}
 * @see {@link ProtoScreen}
 * @see {@link KlotzkiBoard}
 * 
 * Change log:
 * 2025-05-26: refactor screens & add Kltozki game
 * 2025-05-25: refactor util code to ColorHelper and RandomHelper
 * 2025-05-25: remove deprecated methods and fields in DynamicBoard
 * 2025-05-25: Refactor all the change logs
 * 2025-05-25: Organize import (doc)
 * 2025-05-25: Organize import
 * 2025-05-25: generate change log
 * 2025-05-25: Update documentary
 * 2025-05-23: Refactor project structure (#12)
 * 2025-05-23: Refactor project structure
 * 2025-04-29: web inspection
 * 2025-04-29: focal length animation
 * 2025-04-27: fix:resize dynamic board
 * 2025-04-24: MSAA & Settings
 * 2025-04-24: misty & click effect
 * 2025-04-23: better main screen
 * 2025-04-23: better main menu
 * 2025-04-22: soundtrace.ogg
 * 2025-04-22: better color blocks in main screen
 * 2025-04-22: better dark mode
 * 2025-04-22: Settings view
 * 2025-04-22: better main screen dynamic background
 * 2025-04-21: background
 * 2025-04-21: resizable
 * 2025-04-16: Login & Game Mode & Save-Load
 */

package io.github.jimzhouzzy.klotski.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.ScreenUtils;

import io.github.jimzhouzzy.klotski.Klotski;
import io.github.jimzhouzzy.klotski.screen.core.ProtoScreen;
import io.github.jimzhouzzy.klotski.ui.component.KlotskiTheme;
import io.github.jimzhouzzy.klotski.util.ColorHelper;

public class DynamicBoard {

    final Klotski klotski;
    protected int frameCount;
    protected int frameCountOffset;
    protected float baseTileSize;
    protected Stage stage;
    protected Skin skin;
    protected ShapeRenderer shapeRenderer;
    protected float offsetX; // Offset for translation animation
    protected float offsetY;
    protected float offsetZ;
    protected boolean moveForward;
    protected boolean moveBackward;
    protected boolean moveLeft;
    protected boolean moveRight;
    protected boolean moveShifted;
    protected boolean moveUpward;
    protected boolean moveDownward;
    protected boolean rotateClockwise;
    protected boolean rotateCounterClockwise;
    protected float rotationAngle = 0f; // Rotation angle in degrees
    protected float rotationSpeed = 10f; // Degrees per second
    protected Color currentColor;
    protected float colorChangeSpeed = 0.001f; // Speed of color change
    public Color[] colorList; // Predefined list of colors
    public Map<String, Color> colorCache; // Cache for storing colors
    public Map<String, Double> zPositionCache;
    public Map<String, Double> zPositionTempCache;
    public Map<String, Double> yRotationCache;
    public Map<String, Boolean> triggerYRotation; // Flag to trigger Y rotation
    protected boolean triggerYRotationAnimation;
    protected int yRotationAnimationTemp;
    protected int yRotationAnimationStartingRow;
    protected float yRotationAnimationStartingOffsetY;
    protected boolean mutateColorFollowing;
    protected List<Color> targetColors; // List of target colors
    protected int currentColorIndex = 0; // Index of the current base color
    protected float interpolationFactor = 0f;
    protected float interpolationSpeedMultiplier = 1f; // Speed of color interpolation
    protected List<Vector2[]> topRectangleVectors;
    protected List<Float> topRectangleYs;
    protected float screenWidth;
    protected float screenHeight;
    protected float focalLength;
    protected float focalLengthPrevious;
    protected float focalLengthTarget;
    protected float focalLengthAnimationSpeed;
    protected List<Color> levelColorCache = new ArrayList<>();
    protected int levelColorCacheIndex = 0;

    public DynamicBoard(final Klotski klotski, Stage stage) {
        this.klotski = klotski;
        this.stage = stage;
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

        baseTileSize = Gdx.graphics.getWidth() / 21.6f; // 1080 / 21.6 = 50

        offsetX = baseTileSize / 2;
        offsetY = 0f;
        offsetZ = 0f;
        focalLength = 10000.0f;
        focalLengthTarget = 1500.0f;
        focalLengthPrevious = 1500.0f;
        focalLengthAnimationSpeed = 4.0f;

        frameCount = 0;
        frameCountOffset = klotski.randomHelper.nextInt(10000); // Random offset for the frame count
        yRotationAnimationTemp = 0;

        // Initialize ShapeRenderer
        shapeRenderer = new ShapeRenderer();

        // Load colors
        loadColors();
    }

    public void create() {
        stage.addListener(new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                switch (keycode) {
                    case Input.Keys.W: // Move forward
                    case Input.Keys.UP:
                        moveForward = true;
                        break;
                    case Input.Keys.S: // Move backward
                    case Input.Keys.DOWN:
                        moveBackward = true;
                        break;
                    case Input.Keys.A: // Move left
                    case Input.Keys.LEFT:
                        moveLeft = true;
                        break;
                    case Input.Keys.D: // Move right
                    case Input.Keys.RIGHT:
                        moveRight = true;
                        break;
                    case Input.Keys.SHIFT_LEFT:
                    case Input.Keys.SHIFT_RIGHT:
                        moveShifted = true;
                        break;
                    case Input.Keys.CONTROL_LEFT:
                        moveDownward = true;
                        break;
                    case Input.Keys.SPACE:
                        moveUpward = true;
                        break;
                    case Input.Keys.Q:
                        rotateCounterClockwise = true;
                        break;
                    case Input.Keys.E:
                        rotateClockwise = true;
                        break;
                    case Input.Keys.F:
                        triggerFlip();
                        break;
                }
                return true; // Indicate the event was handled
            }

            @Override
            public boolean keyUp(InputEvent event, int keycode) {
                switch (keycode) {
                    case Input.Keys.W:
                    case Input.Keys.UP:
                        moveForward = false;
                        break;
                    case Input.Keys.S:
                    case Input.Keys.DOWN:
                        moveBackward = false;
                        break;
                    case Input.Keys.A:
                    case Input.Keys.LEFT:
                        moveLeft = false;
                        break;
                    case Input.Keys.D:
                    case Input.Keys.RIGHT:
                        moveRight = false;
                        break;
                    case Input.Keys.SHIFT_LEFT:
                    case Input.Keys.SHIFT_RIGHT:
                        moveShifted = false;
                        break;
                    case Input.Keys.CONTROL_LEFT:
                        moveDownward = false;
                        break;
                    case Input.Keys.SPACE:
                        moveUpward = false;
                        break;
                    case Input.Keys.Q:
                        rotateCounterClockwise = false;
                        break;
                    case Input.Keys.E:
                        rotateClockwise = false;
                        break;
                }
                return true;
            }

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                // Update cursor
                Pixmap clickedPixmap = new Pixmap(Gdx.files.internal("assets/image/clicked.png"));

                Pixmap resizedClickedPixmap = new Pixmap(32, 32, clickedPixmap.getFormat());
                resizedClickedPixmap.drawPixmap(clickedPixmap,
                    0, 0, clickedPixmap.getWidth(), clickedPixmap.getHeight(),
                    0, 0, resizedClickedPixmap.getWidth(), resizedClickedPixmap.getHeight()
                );

                int xHotspot = 7, yHotspot = 1;
                Cursor clickedCursor = Gdx.graphics.newCursor(resizedClickedPixmap, xHotspot, yHotspot);
                resizedClickedPixmap.dispose();
                clickedPixmap.dispose();
                Gdx.graphics.setCursor(clickedCursor);

                // If the cursor is clicked inside a quadrant, trigger the flip
                for (int i = 0; i < topRectangleVectors.size(); i++) {
                    Vector2[] vector  = topRectangleVectors.get(i);
                    float rectY = topRectangleYs.get(i);
                    float screenHeight = Gdx.graphics.getHeight();
                    float cursorX = Gdx.input.getX();
                    float cursorY = screenHeight - Gdx.input.getY();
                    if (isPointInQuadrilateral(new Vector2(cursorX, cursorY), vector[0], vector[1], vector[2], vector[3])) {
                        triggerFlip((int) ((rectY + offsetY) / baseTileSize));
                        break;
                    }
                }

                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                Pixmap clickedPixmap = new Pixmap(Gdx.files.internal("assets/image/cursor.png"));

                Pixmap resizedClickedPixmap = new Pixmap(32, 32, clickedPixmap.getFormat());
                resizedClickedPixmap.drawPixmap(clickedPixmap,
                    0, 0, clickedPixmap.getWidth(), clickedPixmap.getHeight(),
                    0, 0, resizedClickedPixmap.getWidth(), resizedClickedPixmap.getHeight()
                );

                int xHotspot = 7, yHotspot = 1;
                Cursor clickedCursor = Gdx.graphics.newCursor(resizedClickedPixmap, xHotspot, yHotspot);
                resizedClickedPixmap.dispose();
                clickedPixmap.dispose();
                Gdx.graphics.setCursor(clickedCursor);
            }
        });
    }

    public void render(float delta) {
        frameCount++;
        frameCount %= Integer.MAX_VALUE; // Avoid overflow

        // The rectangle points for the top layer
        topRectangleVectors = new ArrayList<Vector2[]>();
        List<Color> topRectangleColors = new ArrayList<Color>();
        topRectangleYs = new ArrayList<Float>();
        List<Float> topRectangleYRotationAngles = new ArrayList<Float>();

        // Clear the screen and set the background to light blue
        ScreenUtils.clear(klotski.getBackgroundColor());

        float moveSpeed = 200.0f;
        if (moveShifted) {
            moveSpeed = 3 * moveSpeed;
        }
        if (moveForward) {
            offsetY += delta * moveSpeed;
        }
        if (moveBackward) {
            offsetY -= delta * moveSpeed;
        }
        if (moveRight) {
            offsetX += delta * moveSpeed;
        }
        if (moveLeft) {
            offsetX -= delta * moveSpeed;
        }
        if (moveUpward) {
            offsetZ += 3 * delta * moveSpeed;
        }
        if (moveDownward) {
            offsetZ -= 3 * delta * moveSpeed;
        }
        if (rotateClockwise) {
            if (rotationAngle < 2f && rotationAngle > -3f)
                rotationAngle += rotationSpeed * delta;
        }
        if (rotateCounterClockwise) {
            if (rotationAngle < 3f && rotationAngle > -2f)
                rotationAngle -= rotationSpeed * delta;
        }

        // Update offsets for diagonal translation animation (45-degree movement)
        // offsetX += delta * 20; // Move 20 pixels per second horizontally
        offsetY += delta * 50; // Move 50 pixels per second vertically

        screenWidth = Gdx.graphics.getWidth();
        screenHeight = Gdx.graphics.getHeight();

        float deltaFocalLength = (focalLengthTarget - focalLength) * delta * focalLengthAnimationSpeed;
        focalLength = (focalLength + deltaFocalLength);
        float centerX = screenWidth / 2f
                * (1 - 0.2f * (float) veryComplexFunction((frameCount + frameCountOffset) / 5000f));
        float centerZ = (screenHeight + offsetZ) / 3.75f
                * (1 - 0.5f * (float) veryComplexFunction((frameCount + frameCountOffset) / 5000f)); // Center Y
                                                                                                     // position for
                                                                                                     // perspective
                                                                                                     // projection
        float appliedRotationAngle = rotationAngle
                + 2.0f * ((0.5f - (float) simpleComplexFunction((frameCount + frameCountOffset) / 50f)));

        if (frameCount % (60 * 20) == 0 && !triggerYRotationAnimation) {
            triggerFlip();
        }
        if (triggerYRotationAnimation) {
            if (frameCount % (10 * 1) == 0) {
                for (Map.Entry<String, Boolean> entry : triggerYRotation.entrySet()) {
                    String key = entry.getKey();
                    String[] parts = key.split(",");
                    int row = Integer.parseInt(parts[1]);
                    if (row == yRotationAnimationTemp + yRotationAnimationStartingRow
                            || (row ==  - yRotationAnimationTemp + yRotationAnimationStartingRow)
                            ) {
                        entry.setValue(true); // Trigger rotation for all tiles
                    }
                }
                yRotationAnimationTemp ++;
                if (yRotationAnimationTemp == 200 || yRotationAnimationTemp >= (offsetY + screenHeight 
                        + 20f * baseTileSize) / baseTileSize) {
                    System.out.println("Stop triggering Y rotation, with step: " + yRotationAnimationTemp);
                    triggerYRotationAnimation = false;
                    mutateColorFollowing = true;
                    yRotationAnimationTemp = 0;
                }
            }
        }

        for (float y = -offsetY - 2f * baseTileSize; y < -0.01 + screenHeight + 20f * baseTileSize; y += baseTileSize) {
            for (float x = -offsetX - 100f * baseTileSize; x < -0.01 + -offsetX + screenWidth
                    + 100f * baseTileSize; x += baseTileSize) {
                if (x < 0f - 50f * baseTileSize 
                        || x > screenWidth + 50f * baseTileSize 
                        || y < 0f - 2f * baseTileSize
                        || y > screenHeight + 20f * baseTileSize) {
                    continue; // Skip tiles outside the screen
                }


                // Generate a unique key for the current tile
                String key = (int) ((int) Math.floor((x) / baseTileSize) + (int) Math.floor(offsetX / baseTileSize)) 
                        + ","
                        + (int) ((int) Math.floor((y) / baseTileSize) + (int) Math.floor(offsetY / baseTileSize));
                double zPositionChange = 0.0;
                float zPositionChangedY = (float) (y);
                float yRotatedX = x;
                float yRotatedTileSize = baseTileSize;
                boolean stopTriggering = false;
                boolean mutateColor = false;
                double yRotationAngle = yRotationCache.computeIfAbsent(key, k -> 0.0);
                boolean triggerYRotationBool = triggerYRotation.computeIfAbsent(key, k -> false);

                // Apply Y-rotation if needed
                if (triggerYRotationBool) {
                    updateYRotationCache(key);
                    String lastKey = (int) ((int) Math.floor(x / baseTileSize)
                            + (int) Math.floor(offsetX / baseTileSize)) + ","
                            + (int) ((int) Math.floor(y / baseTileSize) - 1 
                            + (int) Math.floor(offsetY / baseTileSize));
                    final double lastYRotationCache = yRotationCache.getOrDefault(lastKey, 0.0);
                    yRotationAngle = yRotationCache.computeIfAbsent(key, k -> lastYRotationCache);
                    if ((yRotationAngle < 2 * Math.PI + 0.05 && yRotationAngle > 2 * Math.PI - 0.05)) {
                        yRotationCache.put(key, 0.0);
                        yRotationAngle = 0;
                        stopTriggering = true;
                    }
                }
                if (stopTriggering) {
                    triggerYRotation.put(key, false);
                }
                float cosYRotationAngle = (float) Math.cos(yRotationAngle);
                yRotatedX = (x + 0.5f * baseTileSize) + (x - (x + 0.5f * baseTileSize)) * cosYRotationAngle;
                yRotatedTileSize = baseTileSize * cosYRotationAngle;

                // Get or generate a random color for the tile
                Color tileColorOriginal = colorCache.computeIfAbsent(
                    key, 
                    k -> ColorHelper.generateSimilarColor(klotski, generateSmoothChangingColor(delta), 
                    0.1f, 
                    0.0f, 
                    1.0f)
                );
                
                if (klotski.klotskiTheme == KlotskiTheme.DARK) {
                    tileColorOriginal = new Color(tileColorOriginal.r * 0.65f, 
                                                  tileColorOriginal.g * 0.65f, 
                                                  tileColorOriginal.b * 0.65f, 
                                                  tileColorOriginal.a);
                }
                Color tileColor = tileColorOriginal.cpy();
                float luminanceAdjustment = Math.min(1.0f, Math.max(-1.0f, (y - 8 * baseTileSize) / (2.5f * screenHeight)));
                tileColor.lerp(Color.WHITE, luminanceAdjustment);
                if (yRotationAngle > 0.5 * Math.PI && yRotationAngle < 1.5 * Math.PI) {
                    mutateColor = true;
                }
                if (mutateColor
                        || (mutateColorFollowing && (int) ((int) Math.floor(y / baseTileSize) 
                        + (int) Math.floor(offsetY / baseTileSize)) >= (yRotationAnimationStartingRow 
                        + Math.floor((offsetY + screenHeight + 20f * baseTileSize) / baseTileSize)))) {
                    float[] hsl = ColorHelper.rgbToHsl(tileColor.r, tileColor.g, tileColor.b);
                    float alpha = tileColor.a;
                    hsl[0] = (hsl[0] + 0.5f) % 1.0f; // Rotate hue by 180 degrees
                    float[] rgb = ColorHelper.hslToRgb(hsl[0], hsl[1], hsl[2]);
                    tileColor = new Color(rgb[0], rgb[1], rgb[2], alpha);
                }

                // Apply rotation to the tile positions
                // Note that this rotation is not the real rotation
                Vector2 rotatedPosition = applyRotation(yRotatedX, zPositionChangedY, centerX, centerZ,
                        appliedRotationAngle);
                Vector2 rotatedPositionBR = applyRotation(yRotatedX + yRotatedTileSize,
                        zPositionChangedY + baseTileSize, centerX,
                        centerZ,
                        appliedRotationAngle);

                // Calculate the projected positions for the four corners of the tile
                Vector2 tl = projectPerspective(rotatedPosition.x, rotatedPosition.y + (float) zPositionChange,
                        focalLength, centerX, centerZ);
                Vector2 tr = projectPerspective(rotatedPositionBR.x, rotatedPosition.y + (float) zPositionChange,
                        focalLength, centerX, centerZ);
                Vector2 bl = projectPerspective(rotatedPosition.x, rotatedPositionBR.y + (float) zPositionChange,
                        focalLength, centerX, centerZ);
                Vector2 br = projectPerspective(rotatedPositionBR.x, rotatedPositionBR.y + (float) zPositionChange,
                        focalLength, centerX, centerZ);

                // If cursor is inside the rectangle, change zPositionChange to 100
                float cursorX = Gdx.input.getX();
                float cursorY = screenHeight - Gdx.input.getY();
                Vector2 cursor = new Vector2(cursorX, cursorY);

                if (isPointInQuadrilateral(cursor, tl, tr, bl, br)) {
                    zPositionChange = 10.0;

                    // Add zPositionChange
                    tl = projectPerspective(rotatedPosition.x, rotatedPosition.y + (float) zPositionChange, focalLength,
                            centerX, centerZ);
                    tr = projectPerspective(rotatedPositionBR.x, rotatedPosition.y + (float) zPositionChange,
                            focalLength, centerX, centerZ);
                    bl = projectPerspective(rotatedPosition.x, rotatedPositionBR.y + (float) zPositionChange,
                            focalLength, centerX, centerZ);
                    br = projectPerspective(rotatedPositionBR.x, rotatedPositionBR.y + (float) zPositionChange,
                            focalLength, centerX,
                            centerZ);

                    topRectangleVectors.add(new Vector2[] { tl, tr, bl, br});
                    topRectangleColors.add(tileColor);
                    topRectangleYs.add(y); // original y position
                    topRectangleYRotationAngles.add((float) yRotationAngle);
                }

                drawTopRectangle(tl, tr, bl, br, y, tileColor, (float) yRotationAngle);
            }
        }

        // Draw top layer
        for (int i = 0; i < topRectangleVectors.size(); i ++) {
            drawTopRectangle(
                topRectangleVectors.get(i)[0],
                topRectangleVectors.get(i)[1],
                topRectangleVectors.get(i)[2],
                topRectangleVectors.get(i)[3],
                topRectangleYs.get(i),
                topRectangleColors.get(i),
                topRectangleYRotationAngles.get(i)
            );
        }
    }

    // Projection 'Matrix'
    protected Vector2 projectPerspective(float x, float y, float focal, float cx, float cy) {
        float scale = focal / (focal + y);
        float screenX = cx + (x - cx) * scale;
        float screenY = cy + (y - cy) * scale;
        return new Vector2(screenX, screenY);
    }

    protected static double simpleComplexFunction(double x) {
        return Math.sin(x);
    }

    protected static double simpleComplexFunction(float x) {
        return simpleComplexFunction((double) x);
    }

    // A 'very' complex function in [0, 1] to shake the camera
    protected static double veryComplexFunction(double x) {
        double term1 = 0.5 * (Math.sin(5 * Math.PI * x) * Math.cos(3 * Math.PI * x * x) + 1);
        double term2 = 0.1 * Math.sin(20 * Math.PI * x);
        return term1 * Math.exp(-x) + term2;
    }

    protected static double veryComplexFunction(float x) {
        return veryComplexFunction((double) x);
    }

    public void resize(int width, int height) {
        // Store old dimensions
        float oldBaseTileSize = baseTileSize;

        // Recalculate offsets and scaling factors based on the new dimensions
        screenWidth = Gdx.graphics.getWidth();
        screenHeight = Gdx.graphics.getHeight();

        // Adjust the base tile size to maintain aspect ratio
        baseTileSize = Math.min(screenWidth, screenHeight + 1000000) / 21.6f;
        shapeRenderer = new ShapeRenderer();

        // Update offsets to center the content
        offsetX = offsetX / oldBaseTileSize * baseTileSize;
        offsetY = offsetY / oldBaseTileSize * baseTileSize;
        offsetZ = offsetZ / oldBaseTileSize * baseTileSize;
    }

    public void dispose() {
        skin.dispose();
        shapeRenderer.dispose();
    }

    public void hide() {
        Gdx.input.setInputProcessor(null);
    }

    public void resume() {
    }

    public void pause() {
    }

    public void show() {
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
                    Color chosenColor = colorList[klotski.randomHelper.nextInt(colorList.length)];
                    colorCache.put(key, chosenColor);
                }
            }
        }
    }

    protected Vector2 applyRotation(float x, float y, float centerX, float centerY, float angle) {
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

    protected void updateYRotationCache(String key) {
        double yRotation = yRotationCache.getOrDefault(key, 0.0);
        if (yRotation > 2 * Math.PI) {
            yRotation = 0; // Reset if it exceeds 2π
        }
        yRotation += 0.05; // Increment by 0.01 radians
        yRotationCache.put(key, yRotation);
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

    protected boolean isPointInQuadrilateral(Vector2 point, Vector2 tl, Vector2 tr, Vector2 bl, Vector2 br) {
        // Check if the point is in either of the two triangles
        return isPointInTriangle(point, tl, tr, br) || isPointInTriangle(point, tl, br, bl);
    }

    protected boolean isPointInTriangle(Vector2 point, Vector2 v1, Vector2 v2, Vector2 v3) {
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

    public void setStage(Stage stage) {
        this.stage = stage;
        create();
    }

    public void triggerAnimateFocalLength(float to, float speed){
        float from = focalLength;
        triggerAnimateFocalLength(from, to, speed);
    }

    public void triggerAnimateFocalLength(float from, float to, float speed){
        focalLengthAnimationSpeed = speed;
        focalLength = from;
        triggerAnimateFocalLength(to);
    }

    public void triggerAnimateFocalLength(float to){
        focalLengthTarget = to;
    }

    public void triggerAnimateFocalLengthRevert(){
        triggerAnimateFocalLength(focalLengthPrevious, 10.0f);
    }


    // This method is too associated with the instance, so we dont use it as static method
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
            nextBaseColor = targetColors.get(klotski.randomHelper.nextInt(targetColors.size()));
            if (currentBaseColor == nextBaseColor) {
                nextBaseColor = targetColors.get((currentColorIndex + 1) % targetColors.size());
            }
            // Randomize the interpolation speed
            interpolationSpeedMultiplier = klotski.randomHelper.nextFloat() * 0.5f + 0.5f; // Randomize the speed
            if (interpolationSpeedMultiplier > 1.5f) {
                interpolationSpeedMultiplier = 1.5f; // Limit the maximum speed
            }
            if (interpolationSpeedMultiplier < 0.8f) {
                interpolationSpeedMultiplier = 0.8f; // Limit the minimum speed
            }
        }
        float t = interpolationFactor;
        t = t * t * (3 - 2 * t); // smoothstep(t)

        float red = currentBaseColor.r + t * (nextBaseColor.r - currentBaseColor.r);
        float green = currentBaseColor.g + t * (nextBaseColor.g - currentBaseColor.g);
        float blue = currentBaseColor.b + t * (nextBaseColor.b - currentBaseColor.b);

        currentColor = new Color(red, green, blue, 1.0f);
        return currentColor;
    }
}
