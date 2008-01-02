/*
 * CMYK.java
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
 * CMYK color.
 * 
 * @author Wagenaars
 *
 */
public class CMYK extends PSColor {
    
    /** Default color is black. */
    private static final double[] DEFAULT_LEVELS = {0.0, 0.0, 0.0, 1.0};
    
    /**
     * Instantiates a new CMYK color.
     */
    public CMYK() {
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
    public CMYK clone() throws CloneNotSupportedException {
        CMYK copy = (CMYK) super.clone();
        return copy;
    }

    /**
     * Gets the equivalent CMYK levels of this color.
     * 
     * @return the CMYK
     */
    public double[] getCMYK() {
        double[] cmyk = {getLevel(0), getLevel(1), getLevel(2), getLevel(3)};
        return cmyk;
    }

    /**
     * Gets a PostScript array describing the color space of this color.
     * 
     * @return array describing color space.
     */
    public PSObjectArray getColorSpace() {
        PSObjectArray array = new PSObjectArray();
        array.addToEnd(new PSObjectName("DeviceCMYK", true));
        return array;
    }

    /**
     * Gets the gray level equivalent of this color.
     * 
     * @return the gray level
     */
    public double getGray() {
        return (1.0 - Math.min(1.0, 0.3 * getLevel(0) + 0.59 * getLevel(1)
               + 0.11 * getLevel(2) + getLevel(3)));
    }

    /**
     * Gets the equivalent HSB levels of this color.
     * 
     * @return the HSB
     */
    public double[] getHSB() {
        double[] rgb = getRGB();
        return RGB.convertRGBtoHSB(rgb[0], rgb[1], rgb[2]);
    }

    /**
     * Gets the number of color components required to specify this color.
     * E.g. RGB has three and CMYK has four components.
     * 
     * @return the number of components for this color
     */
    public int getNrComponents() {
        return 4;
    }

    /**
     * Gets the equivalent RGB levels of this color.
     * 
     * @return the RGB
     */
    public double[] getRGB() {
        double r = 1.0 - Math.min(1.0, getLevel(0) + getLevel(3));
        double g = 1.0 - Math.min(1.0, getLevel(1) + getLevel(3));
        double b = 1.0 - Math.min(1.0, getLevel(2) + getLevel(3));
        double[] rgb = {r, g, b};
        return rgb;
    }

}
