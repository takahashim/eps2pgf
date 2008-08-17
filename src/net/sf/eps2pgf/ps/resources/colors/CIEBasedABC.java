/*
 * CIEBasedABC.java
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

import net.sf.eps2pgf.ProgramError;
import net.sf.eps2pgf.ps.errors.PSError;
import net.sf.eps2pgf.ps.errors.PSErrorRangeCheck;
import net.sf.eps2pgf.ps.errors.PSErrorTypeCheck;
import net.sf.eps2pgf.ps.objects.PSObjectArray;
import net.sf.eps2pgf.ps.objects.PSObjectDict;
import net.sf.eps2pgf.ps.objects.PSObjectName;

/**
 * CIE-based color space.
 * 
 * @author Paul Wagenaars
 *
 */
public class CIEBasedABC extends CIEBased {
    
    /** Name of this color space family. */
    public static final PSObjectName FAMILYNAME
        = new PSObjectName("/CIEBasedABC");
    
    /** RangeABC field name. */
    private static final PSObjectName RANGEABC
        = new PSObjectName("/RangeABC");
    
    /** DecodeABC field name. */
    private static final PSObjectName DECODEABC
        = new PSObjectName("/DecodeABC");
    
    /** MatrixABC field name. */
    private static final PSObjectName MATRIXABC
        = new PSObjectName("/MatrixABC");
    
    /** RangeLMN field name. */
    private static final PSObjectName RANGELMN
        = new PSObjectName("/RangeLMN");
    
    /** DecodeLMN field name. */
    private static final PSObjectName DECODELMN
        = new PSObjectName("/DecodeLMN");
    
    /** MatrixLMN field name. */
    private static final PSObjectName MATRIXLMN
        = new PSObjectName("/MatrixLMN");
    
    /** WhitePoint field name. */
    private static final PSObjectName WHITEPOINT
        = new PSObjectName("/WhitePoint");
    
    /** BlackPoint field name. */
    private static final PSObjectName BLACKPOINT
        = new PSObjectName("/BlackPoint");
    
    /** Color space dictionary. */
    private PSObjectDict dict;
    
    /**
     * Define a new CIE-based ABC color space.
     * 
     * @param arr The array describing the new color space.
     * 
     * @throws PSError A PostScript error occurred.
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    public CIEBasedABC(final PSObjectArray arr) throws PSError, ProgramError {
        if (!arr.get(0).toString().equals("CIEBasedABC")) {
            throw new PSErrorTypeCheck();
        }
        
        dict = checkEntries(arr.get(1).toDict());
    }
    
    /**
     * Make sure that all entries in the dictionary are defined. If they are not
     * defined default values are added.
     * 
     * @param dict The dict.
     * 
     * @return the PS object dict
     * 
     * @throws PSError A PostScript error occurred.
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    private static PSObjectDict checkEntries(final PSObjectDict dict)
        throws PSError, ProgramError {
        
        double[] defaultRange = {0.0, 1.0, 0.0, 1.0, 0.0, 1.0};
        double[] defaultMatrix =  {1.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 1.0};
        PSObjectArray defaultDecode = new PSObjectArray();
        defaultDecode.addToEnd(new PSObjectArray("{}"));
        defaultDecode.addToEnd(new PSObjectArray("{}"));
        defaultDecode.addToEnd(new PSObjectArray("{}"));

        if (!dict.known(RANGEABC)) {
            dict.setKey(RANGEABC, new PSObjectArray(defaultRange));
        }
        if (!dict.known(DECODEABC)) {
            dict.setKey(DECODEABC, defaultDecode);
        }
        if (!dict.known(MATRIXABC)) {
            dict.setKey(MATRIXABC, new PSObjectArray(defaultMatrix));
        }
        if (!dict.known(RANGELMN)) {
            dict.setKey(RANGELMN, new PSObjectArray(defaultRange));
        }
        if (!dict.known(DECODELMN)) {
            dict.setKey(DECODELMN, defaultDecode);
        }
        if (!dict.known(MATRIXLMN)) {
            dict.setKey(MATRIXLMN, new PSObjectArray(defaultMatrix));
        }
        dict.get(WHITEPOINT).toArray();
        if (!dict.known(BLACKPOINT)) {
            double[] defaultBlack = {0.0, 0.0, 0.0};
            dict.setKey(BLACKPOINT, new PSObjectArray(defaultBlack));
        }
        
        return dict;
    }
    
    /**
     * Gets a PostScript array describing the color space of this color.
     * 
     * @return array describing color space.
     */
    @Override
    public PSObjectArray getColorSpace() {
        PSObjectArray colSpace = new PSObjectArray();
        colSpace.addToEnd(FAMILYNAME);
        colSpace.addToEnd(dict);
        return colSpace;
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
        return 3;
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
        
        int n = getNrInputValues();
        if (components.length != n) {
            throw new PSErrorRangeCheck();
        }
        
        // Apply RangeABC and assign values to levels[]
        PSObjectArray rangeAbc = dict.get(RANGEABC).toArray();
        for (int i = 0; i < n; i++) {
            double lowLim = rangeAbc.getReal(2 * i);
            double upLim = rangeAbc.getReal(2 * i + 1);
            setLevel(i, Math.max(lowLim, Math.min(upLim, components[i])));
        }
        
        // Apply the DecodeABC procedures
        double[] decodedAbc = new double[n];
        PSObjectArray decodeABC = dict.get(DECODEABC).toArray();
        for (int i = 0; i < n; i++) {
            PSObjectArray proc = decodeABC.get(i).toProc();
            decodedAbc[i] = decode(getLevel(i), proc);
        }
        
        // Apply MatrixABC
        double[] lmnLevels = new double[n];
        PSObjectArray matrixAbc = dict.get(MATRIXABC).toArray();
        for (int i = 0; i < n; i++) {
            lmnLevels[i] = matrixAbc.getReal(i) * decodedAbc[0]
                           + matrixAbc.getReal(3 + i) * decodedAbc[1]
                           + matrixAbc.getReal(6 + i) * decodedAbc[2];
        }
        
        // Apply RangeLMN
        PSObjectArray rangeLmn = dict.get(RANGELMN).toArray();
        for (int i = 0; i < n; i++) {
            double lowLim = rangeLmn.getReal(2 * i);
            double upLim = rangeLmn.getReal(2 * i + 1);
            lmnLevels[i] = Math.max(lowLim, Math.min(upLim, lmnLevels[i]));
        }
        
        // Apply DecodeLMN
        double[] decodedLmn = new double[n];
        PSObjectArray decodeLMN = dict.get(DECODELMN).toArray();
        for (int i = 0; i < n; i++) {
            PSObjectArray proc = decodeLMN.get(i).toProc();
            decodedLmn[i] = decode(lmnLevels[i], proc);
        }
        
        // Apply Matrix LMN
        PSObjectArray matrixLmn = dict.get(MATRIXLMN).toArray();
        for (int i = 0; i < n; i++) {
            double level = decodedLmn[0] * matrixLmn.getReal(i)
                           + decodedLmn[1] * matrixLmn.getReal(3 + i)
                           + decodedLmn[2] * matrixLmn.getReal(6 + i);
            setXyzLevel(i, level);
        }
    }

}
