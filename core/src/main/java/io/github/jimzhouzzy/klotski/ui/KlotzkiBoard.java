/**
 * KlotzkiBoard.java
 * 
 * This class represents the game board for the **Klotzki** game.
 * It extends the {@link DynamicBoard} class.
 * 
 * It handles rendering the Klotzki game board, managing tile colors, and implementing 
 * game mechanics.
 * 
 * @author JimZhouZZY
 * @version 1.26
 * @since 2025-5-25
 * @see {@link DynamicBoard}
 * 
 * Change log:
 * 2025-05-26: refactor screens & add Kltozki game
 * 2025-05-26: create KlotzkiGame and KlotzkiBoard
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
import java.util.List;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.ScreenUtils;

import io.github.jimzhouzzy.klotski.Klotski;
import io.github.jimzhouzzy.klotski.ui.component.KlotskiTheme;
import io.github.jimzhouzzy.klotski.util.ColorHelper;

public class KlotzkiBoard extends DynamicBoard {

    private int[] ccPosition = new int[] { 10, 2 }; // Cao cao position in the grid

    public KlotzkiBoard(final Klotski klotski, Stage stage) {
        super(klotski, stage);
    }

    @Override
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
                            || (row == - yRotationAnimationTemp + yRotationAnimationStartingRow)
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
            for (float x = -offsetX - 10f * baseTileSize + screenWidth / 2f; x < -0.01+ -offsetX +
                    + 10f * baseTileSize + screenWidth / 2f; x += baseTileSize) {
                if (x < 0f - 50f * baseTileSize 
                        || x > screenWidth + 50f * baseTileSize 
                        || y < 0f - 2f * baseTileSize
                        || y > screenHeight + 20f * baseTileSize) {
                    continue; // Skip tiles outside the screen
                }

                // Generate a unique key for the current tile
                String key = (int) ((int) Math.floor((x + offsetX) / baseTileSize)) 
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
                    k -> {
                        if (levelColorCacheIndex < levelColorCache.size()) {
                            return levelColorCache.get(levelColorCacheIndex++).cpy();
                        } else {
                            return colorList[0].cpy();
                        }
                    }
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
                            focalLength, centerX, centerZ);

                    topRectangleVectors.add(new Vector2[] { tl, tr, bl, br});
                    topRectangleColors.add(tileColor);
                    topRectangleYs.add(y); // original y position
                    topRectangleYRotationAngles.add((float) yRotationAngle);
                }

                drawTopRectangle(tl, tr, bl, br, y, tileColor, (float) yRotationAngle);
                
                // Add Cao cao block
                tl = projectPerspective(ccPosition[0] * baseTileSize, ccPosition[1] * baseTileSize, focalLength,
                        centerX, centerZ);
                tr = projectPerspective(ccPosition[0] * baseTileSize + 2 * baseTileSize, ccPosition[1] * baseTileSize, focalLength,
                        centerX, centerZ);
                bl = projectPerspective(ccPosition[0] * baseTileSize, ccPosition[1] * baseTileSize + 2 * baseTileSize,
                        focalLength, centerX, centerZ);
                br = projectPerspective(ccPosition[0] * baseTileSize + 2 * baseTileSize, ccPosition[1] * baseTileSize + 2 * baseTileSize, focalLength,
                        centerX, centerZ);

                topRectangleVectors.add(new Vector2[] { tl, tr, bl, br});
                topRectangleColors.add(tileColor);
                topRectangleYs.add(y); // original y position
                topRectangleYRotationAngles.add(0.0f);
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

    @Override
    public void loadColors() {
        // Predefined list of colors
        colorCache.clear();

        // Curently just get Light color, and dark is adopted when rendering
        colorList = klotski.getMainScreenLightColorList();

        levelColorCache.clear();
        levelColorCacheIndex = 0;
        int totalColors = 40000;
        for (int i = 0; i < totalColors; i++) {
            Color chosenColor = ColorHelper.generateSimilarColor(klotski, generateSmoothChangingColor(0.1f), 
                    0.1f, 
                    0.0f, 
                    1.0f);
            levelColorCache.add(chosenColor.cpy());
        }

        int[] map = new int[40000];
        int pos = 9;
        for (int row = 0; row < 40000 / 20; row += 2) {
            if (row * 20 + 20 + 1 + 20 > 40000) {
                break;
            }
            for (int col = 0; col < 20; col ++) {
                map[col + row * 20] = 0;
                map[col + (row + 1) * 20] = 0;
            }
            int changeBound = klotski.randomHelper.nextInt(3);
            if (changeBound == 0) {
                // Do not change
            } else if (changeBound == 1) {
                if (pos == 18) {
                    pos --;
                }
                // Change to right
                pos ++;
            } else if (changeBound == 2) {
                if (pos == 0) {
                    pos ++;
                }
                // Change to left
                pos --;
            }
            map[pos + row * 20] = 1;
            map[pos + (row + 1) * 20] = 1;
            map[pos + 1 + row * 20] = 1;
            map[pos + 1 + (row + 1) * 20] = 1;
            map[pos + 2 + row * 20] = 1;
            map[pos + 2 + (row + 1) * 20] = 1;
        }

        int numCols = 20;
        int numRows = levelColorCache.size() / numCols;
        for (int col = 0; col < numCols; col++) {
            for (int row = 0; row < numRows; row ++) {
                if (row > 0 && map[col + (row-1) * 20] == 1) {
                    int idx = row * numCols + col;
                    if (idx < levelColorCache.size()) {
                        Color c = levelColorCache.get(idx);
                        float gray = (c.r + c.g + c.b) / 3f;
                        //c.r = c.r * 0.25f + gray * 0.75f;
                        //c.g = c.g * 0.25f + gray * 0.75f;
                        //c.b = c.b * 0.25f + gray * 0.75f;
                        c.r = c.g = c.b = 0.9f;
                    }
                }
            }
        }

        int idx = 0;
        for (int i = -500; i <= 500; i++) {
            for (int j = -50; j <= -5; j++) {
                String key = i + "," + j;
                if (!colorCache.containsKey(key)) {
                    if (idx < levelColorCache.size()) {
                        colorCache.put(key, levelColorCache.get(idx++));
                    } else {
                        colorCache.put(key, colorList[0].cpy()); // Default color if out of range
                    }
                }
            }
        }
    }
}
