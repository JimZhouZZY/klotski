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
 * RandomHelper.java
 * 
 * This class provides utility methods for generating random numbers.
 * 
 * It should NOT be used for seeded games, as it does not provide a
 * deterministic sequence of random numbers.
 * 
 * It should only be initialized once, in {@link Klotski} and used multiple times.
 * 
 * @author JimZhouZZY
 * @version 1.1
 * @since 2025-5-25
 * 
 * Change log:
 * 2025-05-25: refactor util code to ColorHelper and RandomHelper
 */

package io.github.jimzhouzzy.klotski.util;

import java.util.Random;

public class RandomHelper {
    Random random;

    public RandomHelper() {
        this.random = new Random();
    }

    public int nextInt() {
        return random.nextInt();
    }

    public int nextInt(int bound) {
        return random.nextInt(bound);
    }

    public float nextFloat() {
        return random.nextFloat();
    }

    public float nextFloat(float bound) {
        return random.nextFloat() * bound;
    }
}
