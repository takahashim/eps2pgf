/*
 * Gray.java
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
 * Gray color.
 */
public class Gray extends PSColor {
    
    /** Default color is black. */
    private static final double[] DEFAULT_LEVELS = {0.0};
    
    /**
     * Instantiates a new gray color.
     */
    public Gray() {
        try {
            setColor(DEFAULT_LEVELS);
        } catch (PSErrorRangeCheck e) {
            // this can never happen
        }
    }
    
    /**
     * Create an exact copy of this object.
     * 
     * @return Copy of this object.
     * 
     * @throws CloneNotSupportedException Clone not supported by this object.
     */
    @Override
    public Gray clone() throws CloneNotSupportedException {
        Gray copy = (Gray) super.clone();
        return copy;
    }

    /**
     * Convert this color to CMYK.
     * 
     * @return This color converted to CMYK.
     */
    @Override
    public double[] getCMYK() {
        double c = 0.0;
        double m = 0.0;
        double y = 0.0;
        double k = 1.0 - getLevel(0);
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
        array.addToEnd(new PSObjectName("DeviceGray", true));
        return array;
    }

    /**
     * Gets the gray level equivalent of this color.
     * 
     * @return the gray level
     */
    @Override
    public double getGray() {
        return getLevel(0);
    }

    /**
     * Gets the equivalent HSB levels of this color.
     * 
     * @return the HSB
     */
    @Override
    public double[] getHSB() {
        return RGB.convertRGBtoHSB(getLevel(0), getLevel(0), getLevel(0));
    }

    /**
     * Gets the number of color components required to specify this color.
     * E.g. RGB has three and CMYK has four components.
     * 
     * @return the number of components for this color
     */
    @Override
    public int getNrComponents() {
        return 1;
    }

    /**
     * Gets the equivalent RGB levels of this color.
     * 
     * @return the RGB
     */
    @Override
    public double[] getRGB() {
        double[] rgb = {getLevel(0), getLevel(0), getLevel(0)};
        return rgb;
    }

}
