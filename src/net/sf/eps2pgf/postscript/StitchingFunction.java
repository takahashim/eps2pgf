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
 *
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
    
    /** Creates a new instance of StitchingFunction */
    public StitchingFunction(PSObjectDict dict) throws PSErrorRangeCheck, 
            PSErrorTypeCheck, PSErrorUnimplemented {
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
    
}
