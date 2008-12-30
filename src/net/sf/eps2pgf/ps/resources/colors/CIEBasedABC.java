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
    
    /** Color space family name. */
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
    
    /**
     * Define a new CIE-based ABC color space.
     * 
     * @param arr The array describing the new color space.
     * 
     * @throws PSError A PostScript error occurred.
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    public CIEBasedABC(final PSObjectArray arr) throws PSError, ProgramError {
        if (!arr.get(0).eq(FAMILYNAME)) {
            throw new PSErrorTypeCheck();
        }
        
        setDict(checkEntries(arr.get(1).toDict()));
    }
    
    /**
     * Make sure that all entries in the dictionary are defined. If they are not
     * defined default values are added.
     * 
     * @param dict The dictionary.
     * 
     * @return The checked dictionary.
     * 
     * @throws PSError A PostScript error occurred.
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    static PSObjectDict checkEntries(final PSObjectDict dict)
        throws PSError, ProgramError {
        
        if (!dict.known(RANGEABC)) {
            double[] defaultRange = {0.0, 1.0, 0.0, 1.0, 0.0, 1.0};
            dict.setKey(RANGEABC, new PSObjectArray(defaultRange));
        }
        if (!dict.known(DECODEABC)) {
            PSObjectArray defaultDecode = new PSObjectArray();
            defaultDecode.addToEnd(new PSObjectArray("{}", null));
            defaultDecode.addToEnd(new PSObjectArray("{}", null));
            defaultDecode.addToEnd(new PSObjectArray("{}", null));
            dict.setKey(DECODEABC, defaultDecode);
        }
        if (!dict.known(MATRIXABC)) {
            double[] defaultMatrix
                    =  {1.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 1.0};
            dict.setKey(MATRIXABC, new PSObjectArray(defaultMatrix));
        }
        
        checkCommonEntries(dict);
        
        return dict;
    }
    
    /**
     * Gets the color space family name.
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
        
        // Store the component values in the levels[] array
        for (int i = 0; i < n; i++) {
            setLevel(i, components[i]);
        }
        
        setXyzLevels(abcToXyz(components));
    }
    
    /**
     * Converts a color in ABC components to XYZ. For the conversion this method
     * uses the dictionary associated with this color.
     * 
     * @param abc The ABC levels.
     * 
     * @return The XYZ levels.
     * 
     * @throws PSError A PostScript error occurred.
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    double[]  abcToXyz(final double[] abc)
            throws PSError, ProgramError {

        PSObjectDict dict = getDict();

        // Apply RangeABC and assign values to levels[]
        double[] rangedAbc = new double[3];
        PSObjectArray rangeAbc = dict.get(RANGEABC).toArray();
        for (int i = 0; i < 3; i++) {
            double lowLim = rangeAbc.getReal(2 * i);
            double upLim = rangeAbc.getReal(2 * i + 1);
            rangedAbc[i] = Math.max(lowLim, Math.min(upLim, abc[i]));
        }
        
        // Apply the DecodeABC procedures
        double[] decodedAbc = new double[3];
        PSObjectArray decodeABC = dict.get(DECODEABC).toArray();
        for (int i = 0; i < 3; i++) {
            PSObjectArray proc = decodeABC.get(i).toProc();
            decodedAbc[i] = decode(getLevel(i), proc);
        }
        
        // Apply MatrixABC
        double[] lmn = new double[3];
        PSObjectArray matrixAbc = dict.get(MATRIXABC).toArray();
        for (int i = 0; i < 3; i++) {
            lmn[i] = matrixAbc.getReal(i) * decodedAbc[0]
                           + matrixAbc.getReal(3 + i) * decodedAbc[1]
                           + matrixAbc.getReal(6 + i) * decodedAbc[2];
        }
        
        return lmnToXyz(lmn);
    }

}
