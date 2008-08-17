/*
 * CIEBased.java
 *
 * This file is part of Eps2pgf.
 *
 * Copyright 2007-2008 Paul Wagenaars <paul@wagenaars.org>
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

import java.io.IOException;

import net.sf.eps2pgf.ProgramError;
import net.sf.eps2pgf.ps.Interpreter;
import net.sf.eps2pgf.ps.errors.PSError;
import net.sf.eps2pgf.ps.objects.PSObjectArray;
import net.sf.eps2pgf.ps.objects.PSObjectReal;

/**
 * Base class for the CIEBased* color space families.
 * 
 * The levels field of the PSColor base class are the X,Y,Z levels. Each
 * subclass should save the ABC, A, DEF, etc... levels themselves. 
 * 
 * @author Paul Wagenaars
 *
 */
public abstract class CIEBased extends PSColor {
    
    /** Component levels in XYZ colorspace. */
    private double[] xyzLevels = new double[3];
    
    /** Interpreter in which decode procedures are executed. */
    private static Interpreter interp = null;
    
    /**
     * Run a decode procedure on an input value.
     * 
     * @param input The input value.
     * @param proc The decoding procedure.
     * 
     * @return The output value.
     * 
     * @throws PSError A PostScript error occurred.
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    protected static double decode(final double input, final PSObjectArray proc)
            throws PSError, ProgramError {
        
        if (interp == null) {
            try {
                interp = new Interpreter();
            } catch (IOException e) {
                throw new ProgramError("Creating new interpreter generated an"
                        + " IOException.");
            }
        }
        
        interp.getOpStack().push(new PSObjectReal(input));
        interp.getExecStack().push(proc);
        interp.run();
        return interp.getOpStack().pop().toReal();
    }

    /**
     * Gets the equivalent CMYK levels of this color.
     * 
     * @return the CMYK
     */
    @Override
    public double[] getCMYK() {
        double[] rgb = getRGB();
        return DeviceRGB.convertRGBtoCMYK(rgb[0], rgb[1], rgb[2]);
    }

    /**
     * Gets the gray level equivalent of this color.
     * 
     * @return the gray level
     */
    @Override
    public double getGray() {
        double[] rgb = getRGB();
        return (0.3 * rgb[0] + 0.59 * rgb[1] + 0.11 * rgb[2]);
    }

    /**
     * Gets the equivalent HSB levels of this color.
     * 
     * @return the HSB
     */
    @Override
    public double[] getHSB() {
        double[] rgb = getRGB();
        return DeviceRGB.convertRGBtoHSB(rgb[0], rgb[1], rgb[2]);
    }

    /**
     * Gets the preferred color space to be used by output devices. Since output
     * devices generally do not support all the color spaces that PostScript
     * uses, the PSColor must specify which color space is preferred. It must be
     * either: "Gray", "RGB" or "CMYK".
     * 
     * @return String with either "Gray", "RGB" or "CMYK".
     */
    @Override
    public String getPreferredColorSpace() {
        return "RGB";
    }

    /**
     * Gets the equivalent RGB levels of this color.
     * For the math on the conversion from XYZ to RGB see:
     * http://www.w3.org/Graphics/Color/sRGB.html
     * 
     * @return the RGB
     */
    @Override
    public double[] getRGB() {
        double x = xyzLevels[0];
        double y = xyzLevels[1];
        double z = xyzLevels[2];
        
        double[] rgb = new double[3];
        rgb[0] =  3.2410 * x + -1.5374 * y + -0.4986 * z;
        rgb[1] = -0.9692 * x +  1.8760 * y +  0.0416 * z;
        rgb[2] =  0.0556 * x + -0.2040 * y +  1.0570 * z;
        
        // Clip the values between 0.0 and 1.0
        for (int i = 0; i < 3; i++) {
            rgb[i] = Math.max(0.0, Math.min(1.0, rgb[i]));
        }

        // Transformation to nonlinear sR'G'B'
        for (int i = 0; i < 3; i++) {
            if (rgb[i] <= 0.00304) {
                rgb[i] = 12.92 * rgb[i];
            } else {
                rgb[i] = 1.055 * Math.pow(rgb[i], 1.0 / 2.4) - 0.055;
            }
        }
        
        return rgb;
    }

    /**
     * Changes the current color to another color in the same color space.
     * 
     * @param components the new color
     * 
     * @throws PSError A PostScript error occurred.
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    @Override
    public abstract void setColor(final double[] components)
        throws PSError, ProgramError;
    
    /**
     * Set a single color component for the XYZ color space.
     * 
     * @param i Index of color component to set.
     * @param componentLevel the levels to set
     */
    protected void setXyzLevel(final int i, final double componentLevel) {
        xyzLevels[i] = componentLevel;
    }
}
