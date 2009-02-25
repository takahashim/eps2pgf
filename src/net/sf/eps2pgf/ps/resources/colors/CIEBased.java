/*
 * This file is part of Eps2pgf.
 *
 * Copyright 2007-2009 Paul Wagenaars <paul@wagenaars.org>
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

import net.sf.eps2pgf.ProgramError;
import net.sf.eps2pgf.ps.Interpreter;
import net.sf.eps2pgf.ps.VM;
import net.sf.eps2pgf.ps.errors.PSError;
import net.sf.eps2pgf.ps.errors.PSErrorRangeCheck;
import net.sf.eps2pgf.ps.errors.PSErrorTypeCheck;
import net.sf.eps2pgf.ps.errors.PSErrorVMError;
import net.sf.eps2pgf.ps.objects.PSObjectArray;
import net.sf.eps2pgf.ps.objects.PSObjectDict;
import net.sf.eps2pgf.ps.objects.PSObjectName;
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
    
    /** RangeLMN field name. */
    protected static final PSObjectName RANGELMN
        = new PSObjectName("/RangeLMN");
    
    /** DecodeLMN field name. */
    protected static final PSObjectName DECODELMN
        = new PSObjectName("/DecodeLMN");
    
    /** MatrixLMN field name. */
    protected static final PSObjectName MATRIXLMN
        = new PSObjectName("/MatrixLMN");
    
    /** WhitePoint field name. */
    protected static final PSObjectName WHITEPOINT
        = new PSObjectName("/WhitePoint");
    
    /** BlackPoint field name. */
    protected static final PSObjectName BLACKPOINT
        = new PSObjectName("/BlackPoint");
    
    /** Local interpreter in which decode procedures are executed. */
    private static Interpreter localInterp = null;
    

    /** Color space dictionary. */
    private PSObjectDict dict;
    
    /** Component levels in XYZ color space. */
    private double[] xyzLevels = new double[3];
    
    
    /**
     * Creates a new CIE based color.
     * 
     * @param arr The color definition array.
     * @param interp The interpreter to which this color belongs.
     * 
     * @throws PSError A PostScript error occurred.
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    protected CIEBased(final PSObjectArray arr, final Interpreter interp)
            throws PSError, ProgramError {
        
        if (!arr.get(0).eq(getFamilyName())) {
            throw new PSErrorTypeCheck();
        }
        
        setDict(arr.get(1).toDict());
        checkCommonEntries(getDict(), interp);
    }
    
    /**
     * Make sure that all entries in the dictionary are defined. If they are not
     * defined default values are added.
     * 
     * @param dict The dictionary to check
     * @param interp The interpreter
     * 
     * @return The checked dictionary, this is the exact same dictionary as the
     * one passed this method.
     * 
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     * @throws PSError A PostScript error occurred.
     */
    private static PSObjectDict checkCommonEntries(PSObjectDict dict, 
            final Interpreter interp) throws ProgramError, PSError {
        
        VM vm = interp.getVm();
        if (!dict.known(RANGELMN)) {
            double[] defaultRange = {0.0, 1.0, 0.0, 1.0, 0.0, 1.0};
            dict.setKey(RANGELMN, new PSObjectArray(defaultRange, vm));
        }
        if (!dict.known(DECODELMN)) {
            PSObjectArray defaultDecode = new PSObjectArray(vm);
            defaultDecode.addToEnd(new PSObjectArray("{}", interp));
            defaultDecode.addToEnd(new PSObjectArray("{}", interp));
            defaultDecode.addToEnd(new PSObjectArray("{}", interp));
            dict.setKey(DECODELMN, defaultDecode);
        }
        if (!dict.known(MATRIXLMN)) {
            double[] defaultMatrix
                    =  {1.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 1.0};
            dict.setKey(MATRIXLMN, new PSObjectArray(defaultMatrix, vm));
        }
        dict.get(WHITEPOINT).toArray();
        if (!dict.known(BLACKPOINT)) {
            double[] defaultBlack = {0.0, 0.0, 0.0};
            dict.setKey(BLACKPOINT, new PSObjectArray(defaultBlack, vm));
        }
        
        return dict;
    }
    
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
        
        if (localInterp == null) {
            localInterp = new Interpreter();
        }
        
        localInterp.getOpStack().push(new PSObjectReal(input));
        localInterp.getExecStack().push(proc);
        localInterp.run();
        return localInterp.getOpStack().pop().toReal();
    }
    
    /**
     * Gets a PostScript array describing the color space of this color.
     * 
     * @param vm The VM manager.
     * 
     * @return array describing color space.
     * 
     * @throws PSErrorVMError Virtual memory error.
     * @throws PSErrorRangeCheck A PostScript rangecheck error occurred.
     */
    @Override
    public PSObjectArray getColorSpace(final VM vm) throws PSErrorVMError,
            PSErrorRangeCheck {
        
        PSObjectArray colSpace = new PSObjectArray(vm);
        colSpace.addToEnd(getFamilyName());
        colSpace.addToEnd(getDict());
        return colSpace;
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
     * Converts a color in LMN components to XYZ. For the conversion this method
     * uses the dictionary associated with this color.
     * 
     * @param lmnLevels The LMN levels.
     * 
     * @return The XYZ levels.
     * 
     * @throws PSError A PostScript error occurred.
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    protected double[]  lmnToXyz(final double[] lmnLevels)
            throws PSError, ProgramError {
        
        // Apply RangeLMN
        PSObjectArray rangeLmn = dict.get(RANGELMN).toArray();
        for (int i = 0; i < 3; i++) {
            double lowLim = rangeLmn.getReal(2 * i);
            double upLim = rangeLmn.getReal(2 * i + 1);
            lmnLevels[i] = Math.max(lowLim, Math.min(upLim, lmnLevels[i]));
        }
        
        // Apply DecodeLMN
        double[] decodedLmn = new double[3];
        PSObjectArray decodeLMN = dict.get(DECODELMN).toArray();
        for (int i = 0; i < 3; i++) {
            PSObjectArray proc = decodeLMN.get(i).toProc();
            decodedLmn[i] = decode(lmnLevels[i], proc);
        }
        
        // Apply Matrix LMN
        double[] xyz = new double[3];
        PSObjectArray matrixLmn = dict.get(MATRIXLMN).toArray();
        for (int i = 0; i < 3; i++) {
            xyz[i] = decodedLmn[0] * matrixLmn.getReal(i)
                           + decodedLmn[1] * matrixLmn.getReal(3 + i)
                           + decodedLmn[2] * matrixLmn.getReal(6 + i);
        }
        
        return xyz;
    }

    /**
     * Sets the color space dictionary.
     * 
     * @param newDict The new color space dictionary.
     */
    protected void setDict(final PSObjectDict newDict) {
        dict = newDict;
    }

    /**
     * Get the color space dictionary.
     * 
     * @return The color space dictionary.
     */
    protected PSObjectDict getDict() {
        return dict;
    }
    
    /**
     * Set the xyzLevels[].
     * 
     * @param newXyz The new XYZ levels.
     */
    protected void setXyzLevels(final double[] newXyz) {
        for (int i = 0; i < 3; i++) {
            xyzLevels[i] = newXyz[i];
        }
    }
}
