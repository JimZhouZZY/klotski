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
