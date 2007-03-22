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
