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
