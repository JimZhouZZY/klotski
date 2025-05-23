/**
 * GameState.java
 * 
 * Represents the state of the game, including the current move index and elapsed time.
 * This class is serializable to allow saving and loading game states.
 * 
 * @author JimZhouZZY
 * @version 1.8
 * @since 2025-5-25
 * @see {@link GameScreen#handleSave()}
 * 
 * Change log:
 * 2025-05-25: Refactor all the change logs
 * 2025-05-25: Organize import (doc)
 * 2025-05-25: Organize import
 * 2025-05-25: generate change log
 * 2025-05-25: Update documentary
 * 2025-05-23: Refactor project structure (#12)
 * 2025-05-23: Refactor project structure
 * 2025-04-28: Online server auth & save-load
 */

package io.github.jimzhouzzy.klotski.logic;

import java.io.Serializable;

public class GameState implements Serializable {
    private static final long serialVersionUID = 1L;  // Ensuring compatibility during deserialization

    private int currentMoveIndex;
    private float elapsedTime;

    public GameState(int currentMoveIndex, float elapsedTime) {
        this.currentMoveIndex = currentMoveIndex;
        this.elapsedTime = elapsedTime;
    }

    public int getCurrentMoveIndex() {
        return currentMoveIndex;
    }

    public void setCurrentMoveIndex(int currentMoveIndex) {
        this.currentMoveIndex = currentMoveIndex;
    }

    public float getElapsedTime() {
        return elapsedTime;
    }

    public void setElapsedTime(float elapsedTime) {
        this.elapsedTime = elapsedTime;
    }
}
