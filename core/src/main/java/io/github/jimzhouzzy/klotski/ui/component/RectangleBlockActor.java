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
 * @version 1.23
 * @since 2025-5-25
 * @see {@link GameScreen}
 * 
 * Change log:
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

    @Override
    public void setSize(float width, float height) {
        super.setSize(width, height);
        rectangle.setSize(width, height);
    }

    public Rectangle getRectangle() {
        return rectangle;
    }


    public void dispose() {
        if (pieceTexture != null) {
            pieceTexture.dispose();
        }
        font.dispose();
    }
}
