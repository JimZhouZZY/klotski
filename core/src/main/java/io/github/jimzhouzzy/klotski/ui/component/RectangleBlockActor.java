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
 * RectangleBlockActor.java
 * 
 * This class represents a rectangular block actor in GameScreen.
 * It handles user interactions such as dragging and dropping the block,
 * and hooks to updates the game state accordingly.
 * 
 * @author JimZhouZZY
 * @version 1.26
 * @since 2025-5-25
 * @see {@link GameScreen}
 * 
 * Change log:
 * 2025-05-27: Generated comment
 * 2025-05-27: implement levels for 'enhanced' game
 * 2025-05-27: fix: arrow key causes crash when selecting blocked pieces
 * 2025-05-27: implement blocked pieces
 * 2025-05-27: Implement Co-op
 * 2025-05-26: Update changelog
 * 2025-05-26: add comment
 * 2025-05-26: Copyright Header
 * 2025-05-25: Refactor all the change logs
 * 2025-05-25: Organize import (doc)
 * 2025-05-25: Organize import
 * 2025-05-25: generate change log
 * 2025-05-25: Update documentary
 * 2025-05-24: Highlight the number of the selected number (#14)
 * 2025-05-24: Highlight the number of the selected number
 * 2025-05-23: Refactor project structure (#12)
 * 2025-05-23: Refactor project structure
 * 2025-05-20: Merge branch v1.0.7 into main (#8)
 * 2025-05-20: Merge branch v1.0.5 into main (#7)
 * 2025-05-08: update soundfx
 * 2025-04-23: better main screen
 * 2025-04-23: better main menu
 * 2025-04-22: better dark mode
 * 2025-04-16: Login & Levels
 * 2025-04-16: Login & Game Mode & Save-Load
 * 2025-04-15: refactor & basic undo redo
 */

package io.github.jimzhouzzy.klotski.ui.component;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;

import io.github.jimzhouzzy.klotski.Klotski;
import io.github.jimzhouzzy.klotski.logic.KlotskiGame;
import io.github.jimzhouzzy.klotski.screen.GameScreen;

public class RectangleBlockActor extends Actor {
    private Rectangle rectangle;
    private Color color;
    private static ShapeRenderer shapeRenderer = new ShapeRenderer();
    public Boolean enableTouch = true;
    public int pieceId; // ID of the corresponding game piece
    private KlotskiGame game; // Reference to the game logic
    private float snappedX;
    private float snappedY;
    private float oldVelocityX;
    private float oldVelocityY;
    private boolean isSelected = false;
    private Texture pieceTexture;
    private GameScreen gameScreen; // Reference to the GameScreen for boundary checks

    // Font for drawing pieceId
    private static final BitmapFont font = new BitmapFont();
    private static final GlyphLayout layout = new GlyphLayout();

    /** * Sets the selected state of this object. Modifies the internal state to reflect
     * whether the object is currently marked as selected or unselected based on the
     * provided parameter.
     *
     * @param selected {@code true} to mark this object as selected, {@code false} to
     * mark it as unselected. */
    public void setSelected(boolean selected) {
            isSelected = selected;
        }

    public RectangleBlockActor(float x, float y, float width, float height, Color color, int pieceId, KlotskiGame game, GameScreen gameScreen) {
        this.rectangle = new Rectangle(x, y, width, height);
        this.color = color;
        this.pieceId = pieceId;
        this.game = game;
        this.gameScreen = gameScreen;

        if (pieceId == 0) {
            pieceTexture = new Texture(Gdx.files.internal("assets/image/CaoCao.png"));
        } else if (pieceId == 1) {
            pieceTexture = new Texture(Gdx.files.internal("assets/image/Guanyu.png"));
        } else if ((pieceId >= 2 && pieceId <= 5)) {
            pieceTexture = new Texture(Gdx.files.internal("assets/image/Normal.png"));
        } else if (pieceId >= 6 && pieceId <= 9) {
            pieceTexture = new Texture(Gdx.files.internal("assets/image/Soldier.png"));
        }
        System.out.println("RectangleBlockActor: pieceId = " + pieceId);
        System.out.println("BlockedId = " + game.blockedId);
        // print hash of game
        System.out.println("Game hash = " + game.hashCode());
        if (pieceId == game.blockedId) {
            color = new Color(Color.valueOf("#808080"));
        }

        setBounds(x, y, width, height);

        addListener(new InputListener() {
            private float offsetX, offsetY;

            /**
             * Handles touch-down events on this actor, enabling interaction with the block in the game.
             * <p>
             * If touch interaction is disabled ({@code enableTouch} is {@code false}) or this block is currently
             * blocked (matches {@code blockedId}), the event is ignored and further processing is prevented.
             * During auto-solving mode, any touch interaction will immediately stop the auto-solving process.
             * <p>
             * The method calculates the initial logical grid position (row and column) of the block based on
             * its current screen coordinates and cell size, then stores this position in the actor's user data.
             * The touch event's local coordinates relative to the actor are saved as offsets for potential
             * drag operations.
             *
             * @param event The input event triggered by the touch action.
             * @param x The x-coordinate of the touch event, relative to the actor's origin.
             * @param y The y-coordinate of the touch event, relative to the actor's origin.
             * @param pointer The pointer index for multitouch scenarios (ignored in single-touch).
             * @param button The button ID associated with the touch event.
             * @return {@code true} if the event was handled, allowing further drag processing; {@code false} if
             * interaction is disabled or the block is currently blocked.
             */
            @Override
                        public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                            if (enableTouch == false
                                    || pieceId == RectangleBlockActor.this.game.blockedId) {
                                return false;
                            }
            
                            GameScreen gameScreen = RectangleBlockActor.this.gameScreen;
                            if (gameScreen.isAutoSolving()) {
                                gameScreen.stopAutoSolving(); // Stop auto-solving if the user interacts with a block
                            }
            
                            // Record the initial logical position
                            float cellSize = Math.min(Gdx.graphics.getWidth() / 4f, Gdx.graphics.getHeight() / 5f);
                            int oldRow = 5 - (int) (getY() / cellSize) - (int) (getHeight() / cellSize);
                            int oldCol = (int) (getX() / cellSize);
            
                            // Store the old position in the block's user data
                            setUserObject(new int[]{oldRow, oldCol});
            
                            offsetX = x;
                            offsetY = y;
                            return true;
                        }

            /**
             * Handles the event when a block is dragged by the user. This method updates the block's position
             * within predefined boundaries, enforces movement constraints (horizontal/vertical only), and
             * prevents collisions with boundaries or other blocks. If touch interactions are disabled or the
             * block is currently blocked, the method returns early without performing any action. Additionally,
             * if the game is in auto-solving mode, dragging the block stops the auto-solver. The block's
             * position is adjusted to stay within valid boundaries, and minor adjustments are made at boundary
             * corners to avoid "sticking." Velocity and acceleration calculations are included for potential
             * collision effects (e.g., sound), though collision detection logic is currently incomplete.
             *
             * @param event The input event associated with the touch drag.
             * @param x The current X-coordinate of the touch pointer relative to the block's origin.
             * @param y The current Y-coordinate of the touch pointer relative to the block's origin.
             * @param pointer The pointer ID for the touch event (unused in this implementation).
             */
            @Override
                        public void touchDragged(InputEvent event, float x, float y, int pointer) {
                            if (enableTouch == false
                                    || pieceId == RectangleBlockActor.this.game.blockedId) {
                                return;
                            }
            
                            GameScreen gameScreen = RectangleBlockActor.this.gameScreen;
                            if (gameScreen.isAutoSolving()) {
                                gameScreen.stopAutoSolving(); // Stop auto-solving if the user interacts with a block
                            }
            
                            // Store old values
                            float oldX = getX();
                            float oldY = getY();
            
                            float newX = getX() + x - offsetX;
                            float newY = getY() + y - offsetY;
            
                            boolean collisionX = false;
                            boolean collisionY = false;
            
                            // Get the boundaries for this block
                            float[] boundaries = gameScreen.getBoundaryForBlock(RectangleBlockActor.this);
            
                            // Adjust position if it meets two boundaries
                            if (newX == boundaries[0] && newY == boundaries[2]) { // At (minX, minY)
                                if (boundaries[0] + getWidth() <= boundaries[1]) {
                                    newX += 1; // Move slightly right if within maxX
                                    collisionX = true;
                                } else if (boundaries[2] + getHeight() <= boundaries[3]) {
                                    newY += 1; // Move slightly up if within maxY
                                    collisionY = true;
                                }
                            } else if (newX == boundaries[0] && newY + getHeight() == boundaries[3]) { // At (minX, maxY)
                                if (boundaries[0] + getWidth() <= boundaries[1]) {
                                    newX += 1; // Move slightly right if within maxX
                                    collisionX = true;
                                } else if (boundaries[2] <= boundaries[3] - getHeight()) {
                                    newY -= 1; // Move slightly down if within minY
                                    collisionY = true;
                                }
                            } else if (newX + getWidth() == boundaries[1] && newY == boundaries[2]) { // At (maxX, minY)
                                if (boundaries[0] <= boundaries[1] - getWidth()) {
                                    newX -= 1; // Move slightly left if within minX
                                    collisionX = true;
                                } else if (boundaries[2] + getHeight() <= boundaries[3]) {
                                    newY += 1; // Move slightly up if within maxY
                                    collisionY = true;
                                }
                            } else if (newX + getWidth() == boundaries[1] && newY + getHeight() == boundaries[3]) { // At (maxX, maxY)
                                if (boundaries[0] <= boundaries[1] - getWidth()) {
                                    newX -= 1; // Move slightly left if within minX
                                    collisionX = true;
                                } else if (boundaries[2] <= boundaries[3] - getHeight()) {
                                    newY -= 1; // Move slightly down if within minY
                                    collisionY = true;
                                }
                            }
                            float minX = boundaries[0];
                            float maxX = boundaries[1];
                            float minY = boundaries[2];
                            float maxY = boundaries[3];
            
                            // Constrain the block within the calculated boundaries
                            newX = Math.max(minX, Math.min(newX, maxX));
                            newY = Math.max(minY, Math.min(newY, maxY));
            
                            // Must be x-y direction moevement
                            if (!(Math.abs(newX - oldX) < 0.001 || Math.abs(newY - oldY) < 0.001)) {
                                newY = oldY; // Keep the old Y position
                                newX = oldX; // Keep the old X position
                            }
            
                            float velocityX = newX - oldX;
                            float velocityY = newY - oldY;
                            float accelerationX = velocityX - oldVelocityX;
                            float accelerationY = velocityY - oldVelocityY;
            
                            oldVelocityX = velocityX;
                            oldVelocityY = velocityY;
            
                            // TODO: find a better way to detect collision
                            /*
                            if (
                                (velocityX / (float) Math.abs(velocityX) * accelerationX < -1.0f && collisionX)
                                    || (velocityY / (float) Math.abs(velocityY) * accelerationY < -1.0f && collisionY)
                                ) {
                                klotski.playBlockCollideSound();
                            }
                            */
            
                            setPosition(newX, newY);
                            rectangle.setPosition(newX, newY);
                        }

            /**
             * Handles the touch-up event to snap the block to the nearest grid cell and update the game state. This method
             * calculates the snapped position based on the grid's cell size, constrains the position within predefined boundaries,
             * and animates the block's movement to the snapped location. If the block's logical position changes, the game state
             * is updated, the move is recorded, and the terminal state is checked. The method also broadcasts the updated game state
             * to relevant listeners. If touch interactions are disabled or the block is currently blocked (as determined by the
             * game's blockedId), the method exits early without performing any actions.
             *
             * @param event The input event associated with the touch-up action.
             * @param x The x-coordinate of the touch-up position in screen space.
             * @param y The y-coordinate of the touch-up position in screen space.
             * @param pointer The pointer index for the touch event.
             * @param button The button index associated with the touch event.
             */
            @Override
                        public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                            if (enableTouch == false
                                    || pieceId == RectangleBlockActor.this.game.blockedId) {
                                return;
                            }
            
                            GameScreen gameScreen = RectangleBlockActor.this.gameScreen;
                            float cellSize = Math.min(Gdx.graphics.getWidth() / 4f, Gdx.graphics.getHeight() / 5f);
            
                            // Calculate the snapped position
                            snappedX = Math.round(getX() / cellSize) * cellSize;
                            snappedY = Math.round(getY() / cellSize) * cellSize;
            
                            // Get the boundaries for this block
                            float[] boundaries = gameScreen.getBoundaryForBlock(RectangleBlockActor.this);
                            float minX = boundaries[0];
                            float maxX = boundaries[1];
                            float minY = boundaries[2];
                            float maxY = boundaries[3];
            
                            // Constrain the snapped position within the boundaries
                            snappedX = Math.max(minX, Math.min(snappedX, maxX));
                            snappedY = Math.max(minY, Math.min(snappedY, maxY));
            
                            // Calculate the logical position after snapping
                            int newRow = 5 - (int) (snappedY / cellSize) - (int) (getHeight() / cellSize);
                            int newCol = (int) (snappedX / cellSize);
            
                            // Retrieve the old position from user data
                            int[] oldPosition = (int[]) getUserObject();
                            int oldRow = oldPosition[0];
                            int oldCol = oldPosition[1];
            
                            // Animate the block's movement to the snapped position
                            addAction(Actions.sequence(
                                Actions.moveTo(snappedX, snappedY, 0.1f), // Smooth snapping animation
                                Actions.run(() -> {
                                    // Update the rectangle's position
                                    // TODO: support 2-step movement
                                    rectangle.setPosition(snappedX, snappedY);
            
                                    // Update the game logic
                                    game.getPiece(pieceId).setPosition(new int[]{newRow, newCol});
            
                                    // Apply the action and record the move only if the block actually moved
                                    if (oldRow != newRow || oldCol != newCol) {
                                        gameScreen.getGame().applyAction(new int[]{oldRow, oldCol}, new int[]{newRow, newCol});
                                        gameScreen.recordMove(new int[]{oldRow, oldCol}, new int[]{newRow, newCol});
                                        gameScreen.isTerminal = game.isTerminal(); // Check if the game is in a terminal state
                                        gameScreen.broadcastGameState();
                                    }
                                })
                            ));
                        }
        });
    }

    /**
     * Renders the game piece with various visual effects. This method overrides the default draw behavior to provide
     * custom rendering logic. It handles both the main shape and additional visual elements:
     * <ul>
     * <li>Draws a selection indicator (black filled circle) if the piece is selected.</li>
     * <li>Applies dynamic color modulation to the piece, including grayscale for blocked pieces and time-based pulsating hues.</li>
     * <li>Renders a gradient-filled rectangle with corner-specific shading variations.</li>
     * <li>Overlays a texture if {@code pieceTexture} is provided.</li>
     * <li>Adds shadow and highlight effects using triangles to simulate depth.</li>
     * <li>Draws a darker border outline around the piece.</li>
     * <li>Displays the {@code pieceId} in the top-right corner with adjustable font color and scaling.</li>
     * </ul>
     * Temporarily interrupts the provided {@link Batch} to use {@link ShapeRenderer} for custom shapes and effects,
     * then restores the batch for subsequent rendering.
     *
     * @param batch The SpriteBatch used for rendering. Temporarily paused for custom shape rendering.
     * @param parentAlpha The parent actor's alpha value for transparency inheritance.
     */
    @Override
        public void draw(Batch batch, float parentAlpha) {
            batch.end(); // End the batch to use ShapeRenderer
    
            // Draw selection indicator (black filled circle in center if selected)
            if (isSelected) {
                shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());
                shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
                shapeRenderer.setColor(Color.BLACK);
                float centerX = getX() + getWidth() / 2f;
                float centerY = getY() + getHeight() / 2f;
                float radius = Math.min(getWidth(), getHeight()) * 0.15f;
                shapeRenderer.circle(centerX, centerY, radius);
                shapeRenderer.end();
            }
    
            shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());
    
            // Optional dynamic color for the rectangle
            float time = (System.currentTimeMillis() % 10000) / 10000f; // 0 to 1 looping value
            if (pieceId == game.blockedId) {
                color = new Color(Color.valueOf("#808080"));
            }
            Color dynamicColor = new Color(
                color.r * (0.9f + 0.1f * (float) Math.sin(2 * Math.PI * time)), // Dynamic red component
                color.g * (0.9f + 0.1f * (float) Math.sin(2 * Math.PI * time + Math.PI / 3)), // Dynamic green component
                color.b * (0.9f + 0.1f * (float) Math.sin(2 * Math.PI * time + 2 * Math.PI / 3)), // Dynamic blue component
                1
            );
    
            // Draw the main filled rectangle
            // Define gradient colors for each corner
            Color bottomLeftColor = dynamicColor.cpy().mul(0.8f); // Slightly darker
            Color bottomRightColor = dynamicColor.cpy().mul(0.9f); // Slightly darker
            Color topLeftColor = dynamicColor.cpy().mul(1.1f); // Slightly lighter
            Color topRightColor = dynamicColor.cpy().mul(1.2f); // Slightly lighter
    
            // Draw the gradient-filled rectangle
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.rect(
                getX(), getY(), getWidth(), getHeight(),
                bottomLeftColor, bottomRightColor, topRightColor, topLeftColor
            );
            shapeRenderer.end();
    
            if (pieceTexture != null) {
                batch.begin();
                batch.draw(pieceTexture, getX(), getY(), getWidth(), getHeight());
                batch.end();
            }
    
            // Add shadow effect (bottom-right gradient)
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(dynamicColor.cpy().mul(0.6f)); // Darker shadow color
            shapeRenderer.triangle(
                getX(), getY(), // Bottom-left corner
                getX() + getWidth(), getY(), // Bottom-right corner
                getX() + getWidth() - 5, getY() + 5 // Slight offset for shadow
            );
            shapeRenderer.triangle(
                getX(), getY(), // Bottom-left corner
                getX(), getY() + getHeight(), // Top-left corner
                getX() + 5, getY() + getHeight() - 5 // Slight offset for shadow
            );
            shapeRenderer.end();
    
            // Add highlight effect (top-left gradient)
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(dynamicColor.cpy().mul(1.2f)); // Lighter highlight color
            shapeRenderer.triangle(
                getX(), getY() + getHeight(), // Top-left corner
                getX() + getWidth(), getY() + getHeight(), // Top-right corner
                getX() + getWidth() - 5, getY() + getHeight() - 5 // Slight offset for highlight
            );
            shapeRenderer.triangle(
                getX(), getY() + getHeight(), // Top-left corner
                getX(), getY(), // Bottom-left corner
                getX() + 5, getY() + 5 // Slight offset for highlight
            );
            shapeRenderer.end();
    
            // Draw the border (outline)
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            shapeRenderer.setColor(dynamicColor.cpy().mul(0.7f)); // Slightly darker border color
            shapeRenderer.rect(getX(), getY(), getWidth(), getHeight());
            shapeRenderer.end();
    
            batch.begin(); // Restart the batch
    
            // Draw the pieceId number in the top-right corner
            if (isSelected) {
                font.setColor(Color.GREEN); // highlight
            } else {
                font.setColor(Color.BLACK); // normal
            }
            layout.setText(font, String.valueOf(pieceId));
            font.getData().setScale(1.5f); // enlarge font
    
    
            float textX = getX() + getWidth() - layout.width - 5f;
            float textY = getY() + getHeight() - 5f;
            font.draw(batch, layout, textX, textY);
        }

    /**
     * Sets the size of this component and updates the associated rectangle to match the new dimensions.
     * This method delegates the size adjustment to the superclass implementation and then synchronizes
     * the dimensions of the internal {@code rectangle} with the specified width and height.
     *
     * @param width the new width for the component and rectangle, in pixels
     * @param height the new height for the component and rectangle, in pixels
     */
    @Override
        public void setSize(float width, float height) {
            super.setSize(width, height);
            rectangle.setSize(width, height);
        }

    /**
     * Returns the {@link Rectangle} instance representing the current bounds of this object.
     * The returned rectangle contains the position (x, y) and dimensions (width, height) that
     * define the object's geometric boundaries. Modifications to the returned {@code Rectangle}
     * will directly affect the internal state of this object, as it is not a defensive copy.
     *
     * @return the {@link Rectangle} instance describing this object's bounds; never {@code null}.
     */
    public Rectangle getRectangle() {
            return rectangle;
        }


    /**
     * Releases system resources associated with this object's graphical assets.
     * Specifically, disposes the {@code pieceTexture} (if it is not {@code null})
     * and the {@code font}. This method should be called when this object is no
     * longer in use to free memory and prevent resource leaks. After calling this
     * method, the assets should not be reused.
     */
    public void dispose() {
            if (pieceTexture != null) {
                pieceTexture.dispose();
            }
            font.dispose();
        }
}
