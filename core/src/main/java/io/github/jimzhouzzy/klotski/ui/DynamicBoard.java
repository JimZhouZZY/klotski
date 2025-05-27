/*
 * Copyright (C) 2025 Zhiyu Zhou (jimzhouzzy@gmail.com)
 * This file is part of github.com/jimzhouzzy/Klotski.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

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
 * @version 1.29
 * @since 2025-5-25
 * @see {@link Klotski}
 * @see {@link ProtoScreen}
 * @see {@link KlotzkiBoard}
 * 
 * Change log:
 * 2025-05-27: Generated comment
 * 2025-05-26: Update changelog
 * 2025-05-26: add comment
 * 2025-05-26: Copyright Header
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

    /**
     * Initializes and attaches input listeners to the stage to handle keyboard and touch events.
     * <p>
     * Keyboard inputs control movement, rotation, and actions: W/â (forward), S/â (backward), A/â (left),
     * D/â (right), Q (counter-clockwise rotation), E (clockwise rotation), F (trigger flip),
     * Shift (modifier key), Left Control (downward movement), and Space (upward movement). Key releases
     * reset corresponding movement flags.
     * <p>
     * Touch events change the cursor icon during a press and revert it on release. A touch within predefined
     * quadrilateral regions (stored in {@code topRectangleVectors}) triggers a flip action via {@link #triggerFlip(int)},
     * calculated based on the touched quadrant's Y-coordinate offset. Cursor images are loaded from
     * {@code assets/image/clicked.png} (pressed state) and {@code assets/image/cursor.png} (default state),
     * resized to32x32 pixels with a hotspot at (7,1).
     */
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
    /**
     * Projects a3D point into2D screen coordinates using a perspective projection model.
     * The method calculates the projected position by scaling the input coordinates relative to
     * a focal length and projection center, simulating depth-based perspective distortion.
     * The projection is computed as:
     * - Scale factor: focal / (focal + y)
     * - Adjusted x-coordinate: cx + (x - cx) * scale
     * - Adjusted y-coordinate: cy + (y - cy) * scale
     *
     * @param x The x-coordinate of the input point in3D space.
     * @param y The y-coordinate of the input point in3D space, also contributing to depth.
     * @param focal The focal length representing the distance from the projection plane.
     * @param cx The x-coordinate of the projection center (e.g., camera or screen center).
     * @param cy The y-coordinate of the projection center (e.g., camera or screen center).
     * @return A {@link Vector2} containing the projected2D screen coordinates (screenX, screenY).
     */
    protected Vector2 projectPerspective(float x, float y, float focal, float cx, float cy) {
            float scale = focal / (focal + y);
            float screenX = cx + (x - cx) * scale;
            float screenY = cy + (y - cy) * scale;
            return new Vector2(screenX, screenY);
        }

    /**
     * Calculates the sine of the specified angle in radians. This method wraps the standard
     * {@link Math#sin(double)} function to compute the trigonometric sine of the input value.
     * The result is a double value between -1.0 and1.0, inclusive.
     *
     * @param x The angle in radians for which to compute the sine.
     * @return The sine of the input angle.
     */
    protected static double simpleComplexFunction(double x) {
            return Math.sin(x);
        }

    /**
     * Converts the specified float value to a double and delegates the computation to the overloaded
     * {@link #simpleComplexFunction(double)} method. This method provides a convenience layer for
     * handling float inputs by internally promoting them to double precision before processing.
     *
     * @param x The input value of type float to be processed.
     * @return A double result obtained by invoking {@link #simpleComplexFunction(double)} with the
     * double representation of the input float value.
     * @see #simpleComplexFunction(double)
     */
    protected static double simpleComplexFunction(float x) {
            return simpleComplexFunction((double) x);
        }

    // A 'very' complex function in [0, 1] to shake the camera
    /**
     * Computes a complex mathematical function combining trigonometric and exponential components. The function
     * is defined as the sum of two terms: the first term is a scaled and shifted product of sine and cosine functions
     * with arguments involving multiples of Ï and x, multiplied by an exponential decay factor. The second term is a
     * scaled high-frequency sine wave. The combination results in a waveform that decays exponentially while oscillating
     * with varying frequencies.
     *
     * @param x The input value to the function, typically representing a point in a continuous domain.
     * @return The computed value of the function at the given input x. The result is a double-precision floating-point
     * number representing the combined effect of the trigonometric and exponential terms.
     */
    protected static double veryComplexFunction(double x) {
            double term1 = 0.5 * (Math.sin(5 * Math.PI * x) * Math.cos(3 * Math.PI * x * x) + 1);
            double term2 = 0.1 * Math.sin(20 * Math.PI * x);
            return term1 * Math.exp(-x) + term2;
        }

    /**
     * Converts the specified float value to a double and delegates computation to the
     * {@link #veryComplexFunction(double)} implementation. This method serves as a convenience
     * overload to enable processing of float values without requiring explicit casting to double.
     *
     * @param x The input value of type float to be processed.
     * @return The computed result after converting the input to a double and applying the
     * complex calculation defined in {@link #veryComplexFunction(double)}.
     * @see #veryComplexFunction(double)
     */
    protected static double veryComplexFunction(float x) {
            return veryComplexFunction((double) x);
        }

    /**
     * Resizes the rendering components to accommodate new screen dimensions. This method updates
     * the screen width and height, recalculates the base tile size to maintain aspect ratio based
     * on the minimum of the screen dimensions, and adjusts content offsets to center the display.
     * The base tile size is scaled proportionally from the previous size to preserve relative
     * positioning. A new {@link ShapeRenderer} is initialized to reflect the updated dimensions.
     *
     * @param width the new screen width
     * @param height the new screen height
     */
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

    /**
     * Releases resources used by this object. This method disposes the {@code skin} and
     * {@code shapeRenderer} associated with this instance, ensuring proper cleanup of
     * allocated assets. Calling this method is necessary to free up system resources
     * and prevent memory leaks when this object is no longer needed.
     */
    public void dispose() {
            skin.dispose();
            shapeRenderer.dispose();
        }

    /**
     * Disables input processing by setting the LibGDX input processor to {@code null}. This ensures
     * that no user input events (e.g., touch, keyboard, mouse, or other device interactions) will be
     * forwarded to any listener or handler until another input processor is explicitly configured.
     * Use this method to prevent the application from responding to input when interaction should
     * be temporarily suspended, such as during transitions, pauses, or UI state changes.
     */
    public void hide() {
            Gdx.input.setInputProcessor(null);
        }

    /**
     * Resumes the operation or activity that was previously paused. This method re-enables
     * processing, execution, or functionality that was halted by a corresponding {@link #pause()}
     * method call. If the component or resource associated with this method is already in a running
     * state, invoking this method has no effect. Implementations should ensure thread safety and
     * handle any necessary state transitions or resource reallocations required to restore normal
     * operation. If the resume operation depends on specific preconditions (e.g., being in a paused
     * state), those should be documented here or enforced via runtime checks.
     *
     * @throws IllegalStateException if the method is called when the component is not in a
     * paused state and cannot be resumed due to invalid state conditions.
     * @see #pause()
     */
    public void resume() {
        }

    /**
     * Pauses the current activity or process controlled by this method. This method temporarily halts
     * the ongoing operation, allowing it to be resumed later from the point it was paused. Depending
     * on the implementation context, this may stop timers, suspend resource utilization, or freeze
     * state changes until {@link #resume()} or a similar method is invoked. If the process is already
     * paused, calling this method will have no effect. Implementations should ensure thread safety
     * and handle interruptions appropriately if used in concurrent environments.
     */
    public void pause() {
        }

    /**
     * Sets the stage as the primary input processor for handling user input events.
     * This configures the LibGDX input system to direct all subsequent input events
     * (such as touch, keyboard, or mouse events) to the {@code stage} instance,
     * enabling UI components attached to the stage to receive and process interactions.
     * Typically called when this screen or interface becomes active or visible.
     */
    public void show() {
            Gdx.input.setInputProcessor(stage);
        }

    /**
     * Initializes the color cache by generating and storing colors for a predefined grid of coordinates.
     * Colors are randomly selected from a predefined list of light colors associated with the main screen.
     * The method iterates over a range of horizontal (i: -500 to500) and vertical (j: -50 to -5) coordinates,
     * creating a unique key for each (i,j) pair. If the key is not already present in the cache, a random color
     * from the light color list is assigned. Dark color variants, used during rendering, are not precomputed here
     * but are derived dynamically when needed. This ensures consistent color distribution across the grid while
     * allowing runtime adjustments for dark mode rendering.
     */
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

    /**
     * Rotates a2D point around a specified center coordinate by a given angle.
     *
     * This method calculates the new position of a point (x, y) after applying a rotation
     * of {@code angle} degrees around the pivot point (centerX, centerY). The rotation follows
     * the standard2D rotation matrix formula, with intermediate translation steps to adjust
     * the rotation center to the origin.
     *
     * @param x The x-coordinate of the point to rotate.
     * @param y The y-coordinate of the point to rotate.
     * @param centerX The x-coordinate of the rotation center point.
     * @param centerY The y-coordinate of the rotation center point.
     * @param angle The rotation angle in degrees (positive values for counterclockwise rotation).
     * @return A {@link Vector2} containing the rotated coordinates.
     *
     * @see <a href="https://en.wikipedia.org/wiki/Rotation_matrix">Rotation Matrix Formula</a>
     */
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

    /**
     * Updates the y-axis rotation value stored in the cache for the specified key. This method retrieves
     * the current rotation value associated with the key, increments it by0.05 radians, and resets it to0
     * if the value exceeds2Ï (a full rotation). The updated value is then stored back in the cache.
     * If the key is not present in the cache, the initial rotation value is assumed to be0.0.
     *
     * @param key The identifier used to retrieve and update the corresponding y-axis rotation value in the cache.
     */
    protected void updateYRotationCache(String key) {
            double yRotation = yRotationCache.getOrDefault(key, 0.0);
            if (yRotation > 2 * Math.PI) {
                yRotation = 0; // Reset if it exceeds 2π
            }
            yRotation += 0.05; // Increment by 0.01 radians
            yRotationCache.put(key, yRotation);
        }

    /**
     * Draws a3D-styled top face of a rectangle with perspective-adjusted borders to simulate depth. The method first renders
     * a filled quadrilateral using two triangles formed by the provided corner points (tl, tr, bl, br). It then adds
     * highlighted and shadowed borders to create a beveled effect. The top-left edges are highlighted with a lighter color,
     * while the bottom-right edges are shaded darker. The thickness of the borders dynamically adjusts based on the vertical
     * position (y) and rotation angle (yRotateAngle) to mimic perspective distortion. This effect is calculated using
     * the cosine of the rotation angle and the normalized y-coordinate relative to the screen height.
     *
     * @param tl The top-left corner of the rectangle in screen coordinates.
     * @param tr The top-right corner of the rectangle in screen coordinates.
     * @param bl The bottom-left corner of the rectangle in screen coordinates.
     * @param br The bottom-right corner of the rectangle in screen coordinates.
     * @param y The vertical position of the rectangle, used to calculate perspective scaling for border thickness.
     * @param tileColor The base color of the rectangle. Highlight and shadow colors are derived from this.
     * @param yRotateAngle The rotation angle (in radians) around the y-axis, influencing the apparent thickness of borders.
     */
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

    /**
     * Determines whether a specified point is located within a quadrilateral defined by four corner points.
     * The quadrilateral is treated as two triangles (top-left, top-right, bottom-right and top-left, bottom-right, bottom-left),
     * and the method checks if the point lies within either of these triangles.
     *
     * @param point The {@link Vector2} point to test for inclusion in the quadrilateral.
     * @param tl The {@link Vector2} representing the top-left corner of the quadrilateral.
     * @param tr The {@link Vector2} representing the top-right corner of the quadrilateral.
     * @param bl The {@link Vector2} representing the bottom-left corner of the quadrilateral.
     * @param br The {@link Vector2} representing the bottom-right corner of the quadrilateral.
     * @return {@code true} if the point is within the quadrilateral (in either of the two triangles), {@code false} otherwise.
     */
    protected boolean isPointInQuadrilateral(Vector2 point, Vector2 tl, Vector2 tr, Vector2 bl, Vector2 br) {
            // Check if the point is in either of the two triangles
            return isPointInTriangle(point, tl, tr, br) || isPointInTriangle(point, tl, br, bl);
        }

    /**
     * Determines whether a specified point is located inside or on the edge of a triangle defined by three vertices.
     * This method uses the barycentric coordinate technique to check the alignment of the point relative to the
     * triangle's edges. By calculating cross products between the triangle's edge vectors and vectors from each vertex
     * to the point, it verifies whether the point lies on the same side (inside or on the edge) for all three edges.
     *
     * @param point The2D point to test for inclusion within the triangle.
     * @param v1 The first vertex of the triangle.
     * @param v2 The second vertex of the triangle.
     * @param v3 The third vertex of the triangle.
     * @return {@code true} if the point is inside the triangle or on its edges; {@code false} if the point is outside.
     */
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

    /**
     * Triggers the y-axis rotation (flip) animation by initializing the necessary parameters and enabling the animation flag.
     * Sets the starting offset and calculates the starting row based on the current offsetY divided by the base tile size.
     * Resets the temporary animation variable and logs the starting row of the animation for debugging purposes.
     * If the animation is not already triggered or under certain conditions, ensures the animation setup is reinitialized.
     */
    public void triggerFlip() {
            if (!triggerYRotationAnimation || true) {
                yRotationAnimationStartingOffsetY = offsetY;
                yRotationAnimationStartingRow = (int) Math.floor(yRotationAnimationStartingOffsetY / baseTileSize);
                yRotationAnimationTemp = 0;
                triggerYRotationAnimation = true;
                System.out.println("Triggering flip animation, with starting row: " + yRotationAnimationStartingRow);
            }
        }

    /**
     * Triggers a Y-axis flip animation starting from the specified row. This method initializes
     * animation parameters, including the starting offset based on the row and base tile size,
     * sets the starting row for the animation, resets temporary animation variables, and activates
     * the animation trigger flag. A debug message is printed to confirm the animation start.
     *
     * <p>The animation will only be triggered if the {@code triggerYRotationAnimation} flag is false
     * or if the condition is explicitly overridden. This ensures the animation setup occurs regardless
     * of the current state of the flag.</p>
     *
     * @param row The row index from which the flip animation will start. This value is used to
     * calculate the initial vertical offset for the animation.
     */
    public void triggerFlip(int row) {
            if (!triggerYRotationAnimation || true) {
                yRotationAnimationStartingOffsetY = row * baseTileSize;
                yRotationAnimationStartingRow = row;
                yRotationAnimationTemp = 0;
                triggerYRotationAnimation = true;
                System.out.println("Triggering flip animation, with starting row: " + yRotationAnimationStartingRow);
            }
        }

    /**
     * Sets the primary stage for this component and triggers the initialization of UI elements.
     * This method assigns the provided {@link Stage} to the instance variable and immediately
     * invokes the {@code create()} method to configure and prepare the component's visual
     * structure and dependencies. It must be called before the component is rendered or
     * interacted with, as it establishes the necessary context for the component's operation.
     *
     * @param stage the primary stage to associate with this component, which serves as the
     * root container for the application's UI elements
     */
    public void setStage(Stage stage) {
            this.stage = stage;
            create();
        }

    /**
     * Initiates an animation to transition the focal length from its current value to a specified target value
     * at a given speed. The animation starts automatically using the current focal length as the starting point
     * and updates it progressively until the target value is reached. The speed parameter controls the rate
     * of change during the animation.
     *
     * @param to The target focal length to animate toward.
     * @param speed The speed at which the animation occurs (units dependent on implementation context).
     */
    public void triggerAnimateFocalLength(float to, float speed){
            float from = focalLength;
            triggerAnimateFocalLength(from, to, speed);
        }

    /**
     * Initiates an animation for transitioning the focal length between specified values at a given speed.
     * Sets the initial focal length to the provided {@code from} value, configures the animation speed,
     * and triggers the animation to transition the focal length to the specified {@code to} value.
     *
     * @param from The initial value of the focal length at the start of the animation.
     * @param to The target value of the focal length to animate toward.
     * @param speed The speed at which the focal length animation should proceed (in units per second).
     */
    public void triggerAnimateFocalLength(float from, float to, float speed){
            focalLengthAnimationSpeed = speed;
            focalLength = from;
            triggerAnimateFocalLength(to);
        }

    /**
     * Sets the target value for the focal length and triggers an animation to smoothly transition
     * from the current focal length to the specified target value. This method updates the internal
     * target state, which is typically used by an animation loop or interpolator to gradually adjust
     * the focal length over time. The animation progress and duration depend on the system's
     * animation configuration or implementation.
     *
     * @param to The target focal length to animate toward. Must be a positive value representing
     * the desired focal length in the applicable units (e.g., millimeters).
     */
    public void triggerAnimateFocalLength(float to){
            focalLengthTarget = to;
        }

    /**
     * Triggers an animation to revert the focal length to its previous value. The animation is
     * performed over a duration of10.0 seconds, using the stored previous focal length value
     * ({@code focalLengthPrevious}) as the target. This method is typically used to restore the
     * focal length to a prior state after temporary adjustments.
     */
    public void triggerAnimateFocalLengthRevert(){
            triggerAnimateFocalLength(focalLengthPrevious, 10.0f);
        }


    // This method is too associated with the instance, so we dont use it as static method
    /**
     * Generates a smoothly transitioning color by interpolating between a predefined sequence of colors.
     * The method progresses through a list of target colors, blending each consecutive pair using a
     * smoothstep interpolation function to create gradual transitions. When nearing the end of a transition,
     * the next target color is selected, either sequentially or randomly (avoiding immediate repetition).
     * The interpolation speed is randomized within a controlled range (0.8x to1.5x base speed) after each
     * transition to add variability. The color sequence cycles indefinitely, and the initial target colors
     * include soft pastel hues. The alpha value of the returned color is always1.0 (fully opaque).
     *
     * @param delta The time delta since the last update, though this parameter is not directly used
     * in the current implementation.
     * @return The interpolated {@link Color} at the current transition state.
     */
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
