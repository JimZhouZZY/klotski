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
 * @version 1.6
 * @since 2025-5-25
 * 
 * Change log:
 * 2025-05-27: Generated comment
 * 2025-05-26: Update changelog
 * 2025-05-26: add comment
 * 2025-05-26: Copyright Header
 * 2025-05-26: refactor screens & add Kltozki game
 * 2025-05-25: refactor util code to ColorHelper and RandomHelper
 */

package io.github.jimzhouzzy.klotski.util;

import com.badlogic.gdx.graphics.Color;

import io.github.jimzhouzzy.klotski.Klotski;
import io.github.jimzhouzzy.klotski.ui.component.KlotskiTheme;

public class ColorHelper {
    /**
     * Generates a new color similar to the specified base color, adjusted according to the provided parameters and theme constraints.
     * The new color is created by applying random offsets to the RGB components of the base color, scaled by the variability parameter,
     * and adding a fixed offset. The green component is intentionally reduced to minimize its presence. The resulting color's luminance
     * is checked to ensure it adheres to the current theme's requirements (e.g., minimum brightness for light themes or maximum brightness
     * for dark themes). If the luminance is outside the acceptable range, the method recursively adjusts the color with reduced variability
     * until the limit threshold is reached or the luminance criteria are satisfied. The alpha value of the base color is preserved.
     *
     * @param klotski The context providing theme configuration and randomness utilities.
     * @param baseColor The starting color from which variations are generated.
     * @param variability The maximum range for random RGB offsets, controlling how much the new color can differ from the base.
     * @param offset A fixed value added to each RGB component after applying the random offset.
     * @param limit The threshold to halt recursive adjustments when variability is reduced below a certain level.
     * @return A new color derived from the base color, adjusted for theme-compliant luminance and reduced green intensity.
     */
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

    /**
     * Calculates the relative luminance of a given color using the standard sRGB luminance formula.
     * Relative luminance represents the brightness of a color normalized to a reference white,
     * following the ITU-R Recommendation BT.709. This is commonly used for accessibility standards
     * to determine contrast ratios between colors.
     *
     * The formula applied is:
     * {@code luminance =0.2126 * red +0.7152 * green +0.0722 * blue},
     * where red, green, and blue are the color components normalized to the range [0.0,1.0].
     *
     * @param color The {@link Color} object from which to derive luminance. Assumes RGB components
     * are in a linearized (non-gamma-corrected) format and within the [0.0,1.0] range.
     * @return A float value between0.0 (absolute black) and1.0 (absolute white) representing
     * the relative luminance of the color.
     */
    public static float calculateLuminance(Color color) {
            // Use the standard formula for relative luminance
            return 0.2126f * color.r + 0.7152f * color.g + 0.0722f * color.b;
        }

    /**
     * Converts RGB color values to HSL color values.
     * <p>
     * This method takes normalized RGB values (clamped to the range [0,1]) and returns an array of HSL values
     * where hue is represented as a fractional value in [0,1) (corresponding to0-360 degrees), saturation
     * as a percentage in [0,1], and lightness as a percentage in [0,1]. The conversion follows the HSL color
     * space calculation, accounting for achromatic colors (where saturation is0) by setting hue to0 in such cases.
     * </p>
     *
     * @param r The red component of the color (clamped to0-1 if outside the range).
     * @param g The green component of the color (clamped to0-1 if outside the range).
     * @param b The blue component of the color (clamped to0-1 if outside the range).
     * @return A float array of length3 containing HSL values in the order:
     * {@code [hue (0-1), saturation (0-1), lightness (0-1)]}.
     */
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

    /**
     * Converts an HSL color value to RGB. The conversion follows the HSL to RGB color space transformation,
     * where hue (h) is a normalized value on the color wheel (0.0 to1.0), saturation (s) is a percentage (0.0 to1.0),
     * and lightness (l) is a percentage (0.0 to1.0). When saturation is0, the result is an achromatic color with
     * red, green, and blue components all equal to the lightness value. The resulting RGB values are returned as
     * an array of floats in the range [0.0,1.0].
     *
     * @param h The hue component (0.0 to1.0).
     * @param s The saturation component (0.0 to1.0).
     * @param l The lightness component (0.0 to1.0).
     * @return An array containing the red, green, and blue components, in that order, each in the range [0.0,1.0].
     */
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

    /**
     * Converts a hue value to the corresponding RGB component using the specified parameters.
     * This method is typically used in color space conversions (e.g., HSL to RGB) to calculate
     * the red, green, or blue component based on the normalized hue value. The parameters {@code p}
     * and {@code q} are derived from lightness and saturation values, while {@code t} represents
     * the adjusted hue value normalized between0 and1. The method applies piecewise linear
     * interpolation based on the hue's position within the color wheel segments (0-1/6,1/6-1/2,
     *1/2-2/3, and2/3-1) to compute the correct RGB component.
     *
     * @param p A component value derived from lightness and saturation (e.g., q = lightness * (1 + saturation)).
     * @param q A component value derived from lightness and saturation (e.g., p = lightness * (1 - saturation)).
     * @param t The normalized hue value, adjusted to wrap around if outside the [0,1] range.
     * @return The calculated RGB component value (red, green, or blue) corresponding to the hue.
     */
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
