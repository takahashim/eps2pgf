/*
 * StitchingFunction.java
 *
 * This file is part of Eps2pgf.
 *
 * Copyright (C) 2007 Paul Wagenaars <pwagenaars@fastmail.fm>
 *
 * Eps2pgf is free software; you can redistribute it and/or modify it under
 * the terms of version 2 of the GNU General Public License as published by the
 * Free Software Foundation.
 *
 * Eps2pgf is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */

package net.sf.eps2pgf.postscript;

import java.util.*;

import net.sf.eps2pgf.postscript.errors.*;

/**
 * Represents a PostScript function of type 3 (stitching function)
 * @author Paul Wagenaars
 */
public class StitchingFunction extends PSFunction {
    List<PSFunction> functions = new ArrayList<PSFunction>();
    
    // Number of subdomains/subfunctions
    int k;
    
    // Bounds for subdomains
    double[] bounds;
    
    // Encode field, map each subset of Domain and the Bounds array to the
    // domain of the the domain de?ned by corresponding function. 
    double[] encode;
    
    /**
     * Creates a new instance of StitchingFunction
     * @param dict Dictionary describing the function
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorRangeCheck A required key was not found or one of the entries is out of range
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorTypeCheck One of the entries has an invalid type
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorUnimplemented A feature is not (yet) implemented
     */
    public StitchingFunction(PSObjectDict dict) throws PSErrorRangeCheck, 
            PSErrorTypeCheck, PSErrorUnimplemented, PSErrorUndefined,
            PSErrorInvalidAccess {
        // First, load all common entries
        loadCommonEntries(dict);
        
        // Next, load all type specific entries
        // Functions array
        PSObjectArray funcArr = dict.get("Functions").toArray();
        k = funcArr.size();
        for (int i = 0 ; i < k ; i++) {
            PSObject currentObj = funcArr.get(i);
            if (!(currentObj instanceof PSObjectDict)) {
                throw new PSErrorTypeCheck();
            }
            PSFunction funcDict = PSFunction.newFunction((PSObjectDict)currentObj);
            functions.add(funcDict);
        }
        
        // Bounds array
        bounds = dict.get("Bounds").toArray().toDoubleArray(k-1);
        
        // Encode array
        encode = dict.get("Encode").toArray().toDoubleArray(2*k);
    }
    
    /**
     * Evaluate this function for a set of input values
     * @param input values
     * @return output values
     */
    public double[] evaluate(double[] input) throws PSErrorRangeCheck, 
            PSErrorUnimplemented {
        input = evaluatePreProcess(input);
        
        double x = input[0];
        
        // Search in which range x falls
        int subFunc = -1;
        for (int i = 0 ; i < (k-1) ; i++) {
            if (x < bounds[i]) {
                subFunc = i;
                break;
            }
        }
        if (subFunc < 0) {
            subFunc = k-1;
        }
        
        // encode x value as described by encoding vector
        double bound0;
        double bound1;
        if (subFunc == 0) {
            bound0 = domain[0];
            if (k > 1) {
                bound1 = bounds[0];
            } else {
                bound1 = domain[1];
            }
        } else if (subFunc == (k-1)) {
            bound0 = bounds[k-2];
            bound1 = domain[1];
        } else {
            bound0 = bounds[subFunc-1];
            bound1 = bounds[subFunc];
        }
        double encode0 = encode[2*subFunc];
        double encode1 = encode[2*subFunc+1];
        x = (x-bound0)/(bound1-bound0) * (encode1-encode0) + encode0;
        
        double[] encInput = {x};
        double[] y = functions.get(subFunc).evaluate(encInput);
        
        return evaluatePostProcess(y);
    }
}
