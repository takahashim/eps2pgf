/*
 * ColorUtils.java
 *
 * This file is part of Eps2pgf.
 *
 * Copyright 2007, 2008 Paul Wagenaars <paul@wagenaars.org>
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
import net.sf.eps2pgf.ps.errors.PSErrorTypeCheck;
import net.sf.eps2pgf.ps.errors.PSErrorUndefined;
import net.sf.eps2pgf.ps.objects.PSObject;
import net.sf.eps2pgf.ps.objects.PSObjectArray;
import net.sf.eps2pgf.ps.objects.PSObjectName;

/**
 * This class offers several static methods to handle different aspect
 * of colors.
 */
public final class ColorManager {
    
    /**
     * "Hidden" constructor.
     */
    private ColorManager() {
        /* empty block */
    }
    
    /**
     * Automatically detects the color space and returns the default
     * color in this color space.
     * 
     * @param obj PostScript object describing the color space. See
     * the PostScript manual on the possible types and
     * values of this object.
     * 
     * @return the default color for the specified color space.
     * 
     * @throws PSError a PostScript error occurred.
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    public static PSColor autoSetColorSpace(final PSObject obj)
            throws PSError, ProgramError {
        
        PSObjectName spaceName;
        if (obj instanceof PSObjectName) {
            spaceName = (PSObjectName) obj;
        } else if (obj instanceof PSObjectArray) {
            spaceName = ((PSObjectArray) obj).get(0).toName();
        } else {
            throw new PSErrorTypeCheck();
        }

        if (spaceName.eq(DeviceGray.FAMILYNAME)) {
            return new DeviceGray();
        } else if (spaceName.eq(DeviceRGB.FAMILYNAME)) {
            return new DeviceRGB();
        } else if (spaceName.eq(DeviceCMYK.FAMILYNAME)) {
            return new DeviceCMYK();
        } else if (spaceName.eq(Indexed.FAMILYNAME)) {
            return new Indexed(obj);
        } else if (spaceName.eq(CIEBasedABC.FAMILYNAME)) {
            return new CIEBasedABC(obj.toArray());
        } else if (spaceName.eq(CIEBasedA.FAMILYNAME)) {
                return new CIEBasedA(obj.toArray());
        } else {
            throw new PSErrorUndefined();
        }

    }
    
    /**
     * Checks whether a specific color space is supported or not.
     * 
     * @param key Key describing color space family.
     * 
     * @return true if supported, false otherwise.
     * 
     * @throws PSError A PostScript error occurred.
     */
    public static boolean colorSpaceFamilyStatus(final PSObject key)
            throws PSError {

        String name = String.format("net.sf.eps2pgf.ps.resources.colors.%s",
                key.toString());
        
        try {
            Class.forName(name);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
