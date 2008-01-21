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

import net.sf.eps2pgf.ps.PSObject;
import net.sf.eps2pgf.ps.PSObjectArray;
import net.sf.eps2pgf.ps.PSObjectName;
import net.sf.eps2pgf.ps.errors.PSError;
import net.sf.eps2pgf.ps.errors.PSErrorTypeCheck;
import net.sf.eps2pgf.ps.errors.PSErrorUndefined;

/**
 * This class offers several static methods to handle different aspect
 * of colors.
 */
public final class ColorUtils {
    
    /**
     * "Hidden" constructor.
     */
    private ColorUtils() {
        /* empty block */
    }
    
    /**
     * Automatically detects the color space and returns the default
     * color in this color space.
     * 
     * @param obj PostScript object describing the color space. See
     *            the PostScript manual on the possible types and
     *            values of this object.
     * 
     * @return the default color for the specified color space.
     * 
     * @throws PSError a PostScript error occurred.
     */
    public static PSColor autoSetColorSpace(final PSObject obj) throws PSError {
        String spaceName;
        if (obj instanceof PSObjectName) {
            spaceName = ((PSObjectName) obj).toString();
        } else if (obj instanceof PSObjectArray) {
            spaceName = ((PSObjectArray) obj).get(0).toName().toString();
        } else {
            throw new PSErrorTypeCheck();
        }

        if (spaceName.equals("DeviceGray")) {
            return new Gray();
        } else if (spaceName.equals("DeviceRGB")) {
            return new RGB();
        } else if (spaceName.equals("DeviceCMYK")) {
            return new CMYK();
        } else if (spaceName.equals("Indexed")) {
            return new Indexed(obj);
        } else {
            throw new PSErrorUndefined();
        }

    }
}
