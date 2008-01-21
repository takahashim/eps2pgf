/*
 * PSColor.java
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

package net.sf.eps2pgf.ps.resources.colors;

import net.sf.eps2pgf.ps.PSObjectArray;
import net.sf.eps2pgf.ps.errors.PSErrorRangeCheck;

/**
 * The Interface PSColor.
 */
public abstract class PSColor implements Cloneable {
    
    /** Color levels of this color. Exact meaning depends of color space. */
    private double[] levels;
    
    /**
     * Creates an exact deep copy of this object.
     * 
     * @return an exact deep copy of this object.
     * 
     * @throws CloneNotSupportedException Clone is not supported by this object.
     */
    @Override
    public PSColor clone() throws CloneNotSupportedException {
        PSColor copy = (PSColor) super.clone();
        if (levels != null) {
            copy.levels = levels.clone();
        }
        return copy;
    }
    
    /**
     * Gets the equivalent CMYK levels of this color.
     * 
     * @return the CMYK
     */
    public abstract double[] getCMYK();
    
    /**
     * Gets a PostScript array describing the color space of this color.
     * 
     * @return array describing color space.
     */
    public abstract PSObjectArray getColorSpace();

    /**
     * Gets the gray level equivalent of this color.
     * 
     * @return the gray level
     */
    public abstract double getGray();
    
    /**
     * Gets the equivalent HSB levels of this color.
     * 
     * @return the HSB
     */
    public abstract double[] getHSB();
    
    /**
     * Gets the number of color components required to specify this color.
     * E.g. RGB has three and CMYK has four components.
     * 
     * @return the number of components for this color
     */
    public abstract int getNrComponents();
    
    /**
     * Gets the number of values required to specify this color. For an RGB,
     * CMYK, ... this is the same as getNrComponents(), but for an indexed
     * color space the number of input values is only 1, while the number of
     * components is 3 (in case the indexed colors were specified as RGB
     * values).
     * 
     * @return The number of input values required to specify this color. 
     */
    public abstract int getNrInputValues();
    
    /**
     * Gets the equivalent RGB levels of this color.
     * 
     * @return the RGB
     */
    public abstract double[] getRGB();
    
    /**
     * Changes the current color to another color in the same color space.
     * 
     * @param components the new color
     * 
     * @throws PSErrorRangeCheck A PostScript rangecheck error occurred.
     */
    public void setColor(final double[] components) throws PSErrorRangeCheck {
        int nrComponents = getNrInputValues();
        if (components.length != nrComponents) {
            throw new PSErrorRangeCheck();
        }
        
        for (int i = 0; i < nrComponents; i++) {
            double level = components[i];
            level = Math.min(level, 1.0);
            level = Math.max(level, 0.0);
            setLevel(i, level);
        }
    }

    /**
     * Set a single color component.
     * 
     * @param i Index of color component to set.
     * @param componentLevel the levels to set
     */
    private void setLevel(final int i, final double componentLevel) {
        if (levels == null) {
            levels = new double[getNrComponents()];
        }
        
        levels[i] = componentLevel;
    }
    
    /**
     * Gets a single color level.
     * 
     * @param i Index of color component to get.
     * 
     * @return The color level.
     */
    public double getLevel(final int i) {
        return levels[i];
    }
    
}
