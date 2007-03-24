/*
 * Shading.java
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

import net.sf.eps2pgf.postscript.errors.*;

/**
 * Base class for shading 
 *
 * @author Paul Wagenaars
 */
public class Shading {
    String ColorSpace;
    
    /**
     * Create a new shading of the type defined in the supplied shading dictionary.
     * @param dict PostScript shading dictionary
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorRangeCheck One or more required fields were not found in the dictionary
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorTypeCheck One of the fields has an invalid type
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorUnimplemented A feature is not (yet) implemented
     * @return New shading object
     */
    public static Shading newShading(PSObjectDict dict) throws PSErrorRangeCheck, 
            PSErrorTypeCheck, PSErrorUnimplemented {
        PSObject shadingTypeObj = dict.lookup("ShadingType");
        if (shadingTypeObj == null) {
            throw new PSErrorRangeCheck();
        }

        Shading newShading;
        switch (shadingTypeObj.toInt()) {
            case 3:
                newShading = new RadialShading(dict);
                break;
            default:
                throw new PSErrorUnimplemented("Shading type " + shadingTypeObj.toInt());
        }
        
        return newShading;
    }
    
    /**
     * Load the entries common to all types of shading dictionaries
     */
    void loadCommonEntries(PSObjectDict dict) throws PSErrorRangeCheck, 
            PSErrorUnimplemented, PSErrorTypeCheck {
        PSObject colSpaceObj = dict.lookup("ColorSpace");
        if (colSpaceObj == null) {
            throw new PSErrorRangeCheck();
        }
        if (colSpaceObj instanceof PSObjectName) {
            ColorSpace = ((PSObjectName)colSpaceObj).name;
        } else if (colSpaceObj instanceof PSObjectArray) {
            throw new PSErrorUnimplemented("Defining ColorSpace with an array");
        } else {
            throw new PSErrorTypeCheck();
        }
    }
    
}
