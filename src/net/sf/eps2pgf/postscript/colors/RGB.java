/*
 * RGB.java
 *
 * This file is part of Eps2pgf.
 *
 * Copyright 2007, 2008 Paul Wagenaars <paul@wagenaars.org>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.sf.eps2pgf.postscript.colors;

import net.sf.eps2pgf.postscript.PSObjectArray;
import net.sf.eps2pgf.postscript.PSObjectName;
import net.sf.eps2pgf.postscript.errors.PSErrorRangeCheck;

/**
 * RGB color.
 * 
 * @author Wagenaars
 *
 */
public class RGB extends PSColor {

    /** Default color is black. */
    private static final double[] DEFAULT_LEVELS = {0.0, 0.0, 0.0};
    
    /**
     * Instantiates a new RGB color.
     */
    public RGB() {
        try {
            setColor(DEFAULT_LEVELS);
        } catch (PSErrorRangeCheck e) {
            // this can never happen
        }
    }

    /**
     * Creates an exact deep copy of this object.
     * 
     * @return an exact deep copy of this object.
     * 
     * @throws CloneNotSupportedException Clone not supported by this object.
     */
    @Override
    public RGB clone() throws CloneNotSupportedException {
        RGB copy = (RGB) super.clone();
        return copy;
    }

    /**
     * Gets the equivalent CMYK levels of this color.
     * 
     * @return the CMYK
     */
    @Override
    public double[] getCMYK() {
        // First step: convert RGB to CMY
        double c = 1.0 - getLevel(0);
        double m = 1.0 - getLevel(1);
        double y = 1.0 - getLevel(2);
        
        // Second step: generate black component and alter the other components
        // to produce a better approximation of the original color.
        // See http://en.wikipedia.org/wiki/CMYK_color_model#Mapping_RGB_to_CMYK
        // And http://www.easyrgb.com/math.html
        double k = Math.min(c, Math.min(m, y));
        if (Math.abs(k - 1.0) < 1e-10) {
            c = 0;  m = 0;  y = 0;
        } else {
            c = (c - k) / (1 - k);
            m = (m - k) / (1 - k);
            y = (y - k) / (1 - k);
        }
        
        double[] cmyk = {c, m, y, k};
        return cmyk;
    }
    
    /**
     * Gets a PostScript array describing the color space of this color.
     * 
     * @return array describing color space.
     */
    @Override
    public PSObjectArray getColorSpace() {
        PSObjectArray array = new PSObjectArray();
        array.addToEnd(new PSObjectName("DeviceRGB", true));
        return array;
    }

    /**
     * Gets the gray level equivalent of this color.
     * 
     * @return the gray level
     */
    @Override
    public double getGray() {
        return (0.3 * getLevel(0) + 0.59 * getLevel(1) + 0.11 * getLevel(2));
    }

    /**
     * Gets the equivalent HSB levels of this color.
     * 
     * @return the HSB
     */
    @Override
    public double[] getHSB() {
        return convertRGBtoHSB(getLevel(0), getLevel(1), getLevel(2));
    }

    /**
     * Gets the number of color components required to specify this color.
     * E.g. RGB has three and CMYK has four components.
     * 
     * @return the number of components for this color
     */
    @Override
    public int getNrComponents() {
        return 3;
    }

    /**
     * Gets the equivalent RGB levels of this color.
     * 
     * @return the RGB
     */
    @Override
    public double[] getRGB() {
        double[] rgb = {getLevel(0), getLevel(1), getLevel(2)};
        return rgb;
    }
    
    /**
     * Convert a color specified in HSB to RGB.
     * 
     * @param pH Hue (ranging from 0 to 1)
     * @param s Saturation (ranging from 0 to 1)
     * @param b Brightness (ranging from 0 to 1)
     * 
     * @return Same color in RGB color space. Array with R, G, and B value.
     */
    public static double[] convertHSBtoRGB(final double pH, final double s,
            final double b) {
        // See http://en.wikipedia.org/wiki/HSV_color_space
        double h = pH % 1.0;
        double hi = Math.floor(h * 6);
        double f = h * 6 - hi;
        double p = b * (1 - s);
        double q = b * (1 - f * s);
        double t = b * (1 - (1 - f) * s);
        
        double[] rgb = new double[3];
        switch ((int) hi) {
            case 0:
                rgb[0] = b;  rgb[1] = t;  rgb[2] = p;
                break;
            case 1:
                rgb[0] = q;  rgb[1] = b;  rgb[2] = p;
                break;
            case 2:
                rgb[0] = p;  rgb[1] = b;  rgb[2] = t;
                break;
            case 3:
                rgb[0] = p;  rgb[1] = q;  rgb[2] = b;
                break;
            case 4:
                rgb[0] = t;  rgb[1] = p;  rgb[2] = b;
                break;
            case 5:
            default:
                rgb[0] = b;  rgb[1] = p;  rgb[2] = q;
                break;
                
        }
        return rgb;
    }
    
    /**
     * Convert an color in RGB to HSB (also called HSV).
     * 
     * @param r red value
     * @param g green value
     * @param b blue value
     * 
     * @return array with h, s, and v levels.
     */
    public static double[] convertRGBtoHSB(final double r, final double g,
            final double b) {
        // See http://en.wikipedia.org/wiki/HSV_color_space
        double max = Math.max(r, Math.max(g, b));
        double min = Math.min(r, Math.min(g, b));
        double h, s, v;
        
        if (max == min) {
            h = 0.0;
        } else if ((max == r) && (g >= b)) {
            h = 1.0 / 6.0 * (g - b) / (max - min);
        } else if ((max == r) && (g < b)) {
            h = 1.0 / 6.0 * (g - b) / (max - min) + 1.0;
        } else if (max == g) {
            h = 1.0 / 6.0 * (b - r) / (max - min) + 1.0 / 3.0;
        } else {
            h = 1.0 / 6.0 * (r - g) / (max - min) + 2.0 / 3.0;
        }
        
        if (max == 0.0) {
            s = 0.0;
        } else {
            s = 1 - min / max;
        }
        
        v = max;
        
        double[] hsb = {h, s, v};
        return hsb;
    }

}
