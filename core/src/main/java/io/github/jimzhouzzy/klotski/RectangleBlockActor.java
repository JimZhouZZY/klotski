package io.github.jimzhouzzy.klotski;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;

import io.github.jimzhouzzy.klotski.GameScreen;
import io.github.jimzhouzzy.klotski.Klotski;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Rectangle;

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

    public RectangleBlockActor(float x, float y, float width, float height, Color color, int pieceId, KlotskiGame game) {
        this.rectangle = new Rectangle(x, y, width, height);
        this.color = color;
        this.pieceId = pieceId;
        this.game = game;

        setBounds(x, y, width, height);

        addListener(new InputListener() {
            private float offsetX, offsetY;

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                if (enableTouch == false) {
                    return false;
                }

                Klotski klotski = (Klotski) Gdx.app.getApplicationListener();
                GameScreen gameScreen = klotski.gameScreen;
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
                if (enableTouch == false) {
                    return;
                }
                
                Klotski klotski = (Klotski) Gdx.app.getApplicationListener();
                GameScreen gameScreen = klotski.gameScreen;
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
                if (enableTouch == false) {
                    return;
                }
                
                Klotski klotski = (Klotski) Gdx.app.getApplicationListener();
                GameScreen gameScreen = klotski.gameScreen;
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

                        // Apply the action and record the move
                        gameScreen.getGame().applyAction(new int[]{oldRow, oldCol}, new int[]{newRow, newCol});
                        gameScreen.recordMove(new int[]{oldRow, oldCol}, new int[]{newRow, newCol});

                        gameScreen.isTerminal = game.isTerminal(); // Check if the game is in a terminal state
                        gameScreen.broadcastGameState();
                    })
                ));
            }
        });
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        batch.end(); // End the batch to use ShapeRenderer
    
        shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());
    
        // Optional dynamic color for the rectangle
        float time = (System.currentTimeMillis() % 10000) / 10000f; // 0 to 1 looping value
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
    }

    @Override
    public void setSize(float width, float height) {
        super.setSize(width, height);
        rectangle.setSize(width, height);
    }

    public Rectangle getRectangle() {
        return rectangle;
    }
}