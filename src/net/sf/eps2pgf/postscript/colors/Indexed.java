/*
 * Indexed.java
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

import net.sf.eps2pgf.postscript.PSObject;
import net.sf.eps2pgf.postscript.PSObjectArray;
import net.sf.eps2pgf.postscript.PSObjectString;
import net.sf.eps2pgf.postscript.errors.PSError;
import net.sf.eps2pgf.postscript.errors.PSErrorRangeCheck;
import net.sf.eps2pgf.postscript.errors.PSErrorTypeCheck;
import net.sf.eps2pgf.postscript.errors.PSErrorUnimplemented;

/**
 * Implements Indexed color space.
 */
public class Indexed extends PSColor {
    
    /** Array with colors. */
    private PSObjectArray colorSpaceArray;
    
    /** Currently selected color. */
    private PSColor currentColor;
    
    /**
     * Create a new indexed color.
     * 
     * @param obj Object describing this color
     * 
     * @throws PSError A PostScript error occurred.
     */
    public Indexed(final PSObject obj) throws PSError {
        // Indexed color spaces must be defined using an array
        if (!(obj instanceof PSObjectArray)) {
            throw new PSErrorTypeCheck();
        }
        
        // Save the array specifying this color space
        colorSpaceArray = (PSObjectArray) obj;
        
        // Extract base color space
        currentColor = ColorUtils.autoSetColorSpace(colorSpaceArray.get(1));
        
        // Extract lookup table
        PSObject lookup = colorSpaceArray.get(3);
        if (lookup instanceof PSObjectString) {
            // Convert the lookup string to a more convenient format
            
        } else {
            throw new PSErrorUnimplemented("Indexed color space with non-string"
                    + " lookup table.");
        }
        
    }

    /**
     * Creates an exact deep copy of this object.
     * 
     * @return an exact deep copy of this object.
     */
    @Override
    public PSColor clone() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * Gets the equivalent CMYK levels of this color.
     * 
     * @return the CMYK
     */
    @Override
    public double[] getCMYK() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * Gets a PostScript array describing the color space of this color.
     * 
     * @return array describing color space.
     */
    @Override
    public PSObjectArray getColorSpace() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * Gets the gray level equivalent of this color.
     * 
     * @return the gray level
     */
    @Override
    public double getGray() {
        // TODO Auto-generated method stub
        return 0;
    }

    /**
     * Gets the equivalent HSB levels of this color.
     * 
     * @return the HSB
     */
    @Override
    public double[] getHSB() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * Gets the number of color components required to specify this color.
     * E.g. RGB has three and CMYK has four components.
     * 
     * @return the number of components for this color
     */
    @Override
    public int getNrComponents() {
        // TODO Auto-generated method stub
        return 0;
    }

    /**
     * Gets the equivalent RGB levels of this color.
     * 
     * @return the RGB
     */
    @Override
    public double[] getRGB() {
        // TODO Auto-generated method stub
        return null;
    }
    
    /**
     * Changes the current color to another color in the same color space.
     * 
     * @param components the new color
     * 
     * @throws PSErrorRangeCheck A PostScript rangecheck error occurred.
     */
    @Override
    public void setColor(final double[] components)
            throws PSErrorRangeCheck {
        // TODO Implement set color method
    }

}
