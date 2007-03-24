/*
 * PSFunction.java
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

import net.sf.eps2pgf.postscript.errors.PSErrorRangeCheck;
import net.sf.eps2pgf.postscript.errors.PSErrorTypeCheck;
import net.sf.eps2pgf.postscript.errors.PSErrorUnimplemented;

/**
 * Represents a PostScript function dictionary
 *
 * @author Paul Wagenaars
 */
public class PSFunction {
    double[] domain;
    double[] range;
    
    // number of input values
    int m;
    
    // number of output values
    int n;
    
    /**
     * Create a new function of the type specified in the FuntionType field.
     */
    public static PSFunction newFunction(PSObjectDict dict) throws 
            PSErrorRangeCheck, PSErrorUnimplemented, PSErrorTypeCheck {
        PSObject typeObj = dict.lookup("FunctionType");
        if (typeObj == null) {
            throw new PSErrorRangeCheck();
        }
        
        PSFunction newFunction;
        switch (typeObj.toInt()) {
            case 0:
                throw new PSErrorUnimplemented("FunctionType 0");
            case 2:
                throw new PSErrorUnimplemented("FunctionType 2");
            case 3:
                newFunction = new StitchingFunction(dict);
                break;
            default:
                throw new PSErrorRangeCheck();
        }
        
        return newFunction;
    }
    
    /**
     * 
     */
    void loadCommonEntries(PSObjectDict dict) throws PSErrorRangeCheck, 
            PSErrorTypeCheck {
        // Load domain field
        domain = dict.lookup("Domain").toArray().toDoubleArray();
        m = (int)Math.floor(domain.length/2);
        
        // Load range field
        range = dict.lookup("Range").toArray().toDoubleArray();
        n = (int)Math.floor(range.length/2);
    }
    
}
