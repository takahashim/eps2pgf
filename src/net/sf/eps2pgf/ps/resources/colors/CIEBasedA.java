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
import net.sf.eps2pgf.ps.objects.PSObjectArray;
import net.sf.eps2pgf.ps.objects.PSObjectDict;
import net.sf.eps2pgf.ps.objects.PSObjectName;

/**
 * CIE-based color space.
 * 
 * @author Paul Wagenaars
 *
 */
public class CIEBasedA extends CIEBased {
    
    /** Color space family name. */
    public static final PSObjectName FAMILYNAME
        = new PSObjectName("/CIEBasedA");
    
    /** RangeABC field name. */
    private static final PSObjectName RANGEA
        = new PSObjectName("/RangeA");
    
    /** DecodeABC field name. */
    private static final PSObjectName DECODEA
        = new PSObjectName("/DecodeA");
    
    /** MatrixABC field name. */
    private static final PSObjectName MATRIXA
        = new PSObjectName("/MatrixA");
    
    /**
     * Define a new CIE-based A color space.
     * 
     * @param arr The array describing the new color space.
     * @param interp The interpreter.
     * 
     * @throws PSError A PostScript error occurred.
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    public CIEBasedA(final PSObjectArray arr, final Interpreter interp)
            throws PSError, ProgramError {
        
        super(arr, interp);
        checkEntries(getDict(), interp);
    }
    
    /**
     * Make sure that all entries in the dictionary are defined. If they are not
     * defined default values are added.
     * 
     * @param dict The color dictionary.
     * @param interp The interpreter.
     * 
     * @throws PSError A PostScript error occurred.
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    private static void checkEntries(final PSObjectDict dict,
            final Interpreter interp) throws PSError, ProgramError {
        
        VM vm = interp.getVm();
        if (!dict.known(RANGEA)) {
            double[] defaultRange = {0.0, 1.0};
            dict.setKey(RANGEA, new PSObjectArray(defaultRange, vm));
        }
        if (!dict.known(DECODEA)) {
            dict.setKey(DECODEA, new PSObjectArray("{}", interp));
        }
        if (!dict.known(MATRIXA)) {
            double[] defaultMatrix =  {1.0, 1.0, 1.0};
            dict.setKey(MATRIXA, new PSObjectArray(defaultMatrix, vm));
        }
    }
    
    /**
     * Gets the name of this color space family.
     * 
     * @return Color space family name.
     */
    @Override
    public PSObjectName getFamilyName() {
        return FAMILYNAME;
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
     * Gets the number of values required to specify this color. For an RGB,
     * CMYK, ... this is the same as getNrComponents(), but for an indexed
     * color space the number of input values is only 1, while the number of
     * components is 3 (in case the indexed colors were specified as RGB
     * values).
     * 
     * @return The number of input values required to specify this color. 
     */
    @Override
    public int getNrInputValues() {
        return 1;
    }

    /**
     * Sets a new color in ABC components.
     * 
     * @param components The components.
     * 
     * @throws PSError A PostScript error occurred.
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    @Override
    public void setColor(final double[] components)
            throws PSError, ProgramError {
        
        if (components.length != 1) {
            throw new PSErrorRangeCheck();
        }
        
        PSObjectDict dict = getDict();
        
        // Apply RangeA and assign values to levels[]
        PSObjectArray rangeA = dict.get(RANGEA).toArray();
        double lowLim = rangeA.getReal(0);
        double upLim = rangeA.getReal(1);
        setLevel(0, Math.max(lowLim, Math.min(upLim, components[0])));
        
        // Apply the DecodeA procedures
        PSObjectArray decodeA = dict.get(DECODEA).toArray();
        PSObjectArray proc;
        if (decodeA.isLiteral()) {
            proc = decodeA.get(0).toProc();
        } else {
            proc = decodeA.toProc();
        }
        double decodedA = decode(getLevel(0), proc);
        
        // Apply MatrixABC
        double[] lmn = new double[3];
        PSObjectArray matrixAbc = dict.get(MATRIXA).toArray();
        for (int i = 0; i < 3; i++) {
            lmn[i] = decodedA * matrixAbc.getReal(i);
        }
        
        setXyzLevels(lmnToXyz(lmn));
    }

}
