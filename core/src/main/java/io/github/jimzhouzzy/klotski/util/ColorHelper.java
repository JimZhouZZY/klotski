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
 * ColorHelper.java
 * 
 * This class provides static methods for generating colors and converting between RGB and HSL color spaces.
 * It includes methods to generate similar colors based on a base color, calculate luminance,
 * and convert between RGB and HSL color spaces.
 * 
 * It is used in the Klotski game to generate colors for various UI elements and game pieces.
 * 
 * @author JimZhouZZY
 * @version 1.2
 * @since 2025-5-25
 * 
 * Change log:
 * 2025-05-26: refactor screens & add Kltozki game
 * 2025-05-25: refactor util code to ColorHelper and RandomHelper
 */

package io.github.jimzhouzzy.klotski.util;

import com.badlogic.gdx.graphics.Color;

import io.github.jimzhouzzy.klotski.Klotski;
import io.github.jimzhouzzy.klotski.ui.component.KlotskiTheme;

public class ColorHelper {
    public static Color generateSimilarColor(Klotski klotski, Color baseColor, float variability, float offset,
            float limit) {
        // Generate small random offsets for RGB values
        float redOffset = (klotski.randomHelper.nextFloat() - 0.5f) * variability;
        float greenOffset = (klotski.randomHelper.nextFloat() - 0.5f) * variability;
        float blueOffset = (klotski.randomHelper.nextFloat() - 0.5f) * variability;

        // Clamp the values to ensure they remain between 0 and 1
        float newRed = Math.min(Math.max(baseColor.r + redOffset, 0) + offset, 1);
        float newGreen = 0.9f * Math.min(Math.max(baseColor.g + greenOffset, 0) + offset, 1); // Eliminate green
        float newBlue = Math.min(Math.max(baseColor.b + blueOffset, 0) + offset, 1);

        // Create the new color
        Color newColor = new Color(newRed, newGreen, newBlue, baseColor.a); // Preserve the alpha value

        // Adjust luminance if necessary
        float luminance = calculateLuminance(newColor);
        if (klotski.klotskiTheme == KlotskiTheme.LIGHT && luminance < 0.3f) {
            if (variability > 0.01f * limit) {
                return generateSimilarColor(klotski, baseColor, 0.5f * variability, 0.2f, 1.0f);
            } else {
                return baseColor;
            }
        } else if (klotski.klotskiTheme != KlotskiTheme.LIGHT && luminance > 0.8f) {
            if (variability > 0.01f * limit) {
                return generateSimilarColor(klotski, baseColor, 0.5f * variability, -0.2f, 1.0f);
            } else {
                return baseColor;
            }
        }

        return newColor;
    }

    public static float calculateLuminance(Color color) {
        // Use the standard formula for relative luminance
        return 0.2126f * color.r + 0.7152f * color.g + 0.0722f * color.b;
    }

    public static float[] rgbToHsl(float r, float g, float b) {
        // Normalize RGB values to [0, 1]
        r = Math.min(Math.max(r, 0), 1);
        g = Math.min(Math.max(g, 0), 1);
        b = Math.min(Math.max(b, 0), 1);

        float max = Math.max(r, Math.max(g, b));
        float min = Math.min(r, Math.min(g, b));
        float delta = max - min;

        float h = 0, s = 0, l = (max + min) / 2;

        if (delta != 0) {
            // Calculate saturation
            s = l < 0.5f ? delta / (max + min) : delta / (2 - max - min);

            // Calculate hue
            if (max == r) {
                h = (g - b) / delta + (g < b ? 6 : 0);
            } else if (max == g) {
                h = (b - r) / delta + 2;
            } else if (max == b) {
                h = (r - g) / delta + 4;
            }
            h /= 6;
        }

        return new float[] { h, s, l };
    }

    public static float[] hslToRgb(float h, float s, float l) {
        float r, g, b;

        if (s == 0) {
            // Achromatic (gray)
            r = g = b = l;
        } else {
            float q = l < 0.5f ? l * (1 + s) : l + s - l * s;
            float p = 2 * l - q;
            r = hueToRgb(p, q, h + 1f / 3f);
            g = hueToRgb(p, q, h);
            b = hueToRgb(p, q, h - 1f / 3f);
        }

        return new float[] { r, g, b };
    }

    public static float hueToRgb(float p, float q, float t) {
        if (t < 0)
            t += 1;
        if (t > 1)
            t -= 1;
        if (t < 1f / 6f)
            return p + (q - p) * 6 * t;
        if (t < 1f / 2f)
            return q;
        if (t < 2f / 3f)
            return p + (q - p) * (2f / 3f - t) * 6;
        return p;
    }

}
