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
 * GameState.java
 * 
 * Represents the state of the game, including the current move index and elapsed time.
 * This class is serializable to allow saving and loading game states.
 * 
 * @author JimZhouZZY
 * @version 1.12
 * @since 2025-5-25
 * @see {@link GameScreen#handleSave()}
 * 
 * Change log:
 * 2025-05-27: Generated comment
 * 2025-05-26: Update changelog
 * 2025-05-26: add comment
 * 2025-05-26: Copyright Header
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

    /**
     * Retrieves the current move index, which represents the position in the sequence of moves
     * tracked by this object. The index corresponds to the most recently executed move, starting
     * from0 for the initial state. This value can be used to determine progress through the move
     * sequence or to reset to a specific point in the history.
     *
     * @return the zero-based index of the current move in the move sequence.
     */
    public int getCurrentMoveIndex() {
            return currentMoveIndex;
        }

    /**
     * Sets the current move index to the specified value. This method updates the internal state
     * to reflect the new index, which determines the position in the sequence of moves that is
     * currently active or being tracked. The provided index should be valid within the bounds of
     * the move sequence to ensure correct behavior in dependent operations.
     *
     * @param currentMoveIndex The index to set as the current position in the move sequence.
     */
    public void setCurrentMoveIndex(int currentMoveIndex) {
            this.currentMoveIndex = currentMoveIndex;
        }

    /**
     * Retrieves the elapsed time recorded by this instance. The returned value represents
     * the duration that has passed since the start of the timing operation. The elapsed time
     * is expressed as a floating-point number, allowing for fractional precision in the
     * measurement. This method provides access to the current elapsed time value for monitoring
     * or analytical purposes.
     */
    public float getElapsedTime() {
            return elapsedTime;
        }

    /**
     * Sets the elapsed time value for this instance. This method updates the internal state
     * to reflect the specified elapsed time, measured in seconds. The provided value should
     * represent a non-negative duration, typically used for tracking time-based operations.
     *
     * @param elapsedTime The new elapsed time value to set, in seconds. Must be a non-negative float value.
     */
    public void setElapsedTime(float elapsedTime) {
            this.elapsedTime = elapsedTime;
        }
}
