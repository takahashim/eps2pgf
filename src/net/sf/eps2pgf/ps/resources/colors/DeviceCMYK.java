/*
 * This file is part of Eps2pgf.
 *
 * Copyright 2007-2009 Paul Wagenaars
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

import net.sf.eps2pgf.ps.VM;
import net.sf.eps2pgf.ps.errors.PSErrorRangeCheck;
import net.sf.eps2pgf.ps.errors.PSErrorVMError;
import net.sf.eps2pgf.ps.objects.PSObjectArray;
import net.sf.eps2pgf.ps.objects.PSObjectName;

/**
 * CMYK color.
 * 
 * @author Wagenaars
 *
 */
public class DeviceCMYK extends PSColor implements Cloneable {
    
    /** Name of this color space family. */
    public static final PSObjectName FAMILYNAME
        = new PSObjectName("/DeviceCMYK");
    
    /** Default color is black. */
    private static final double[] DEFAULT_LEVELS = {0.0, 0.0, 0.0, 1.0};
    
    /**
     * Instantiates a new CMYK color.
     */
    public DeviceCMYK() {
        super(DEFAULT_LEVELS);
    }

    /**
     * Gets the equivalent CMYK levels of this color.
     * 
     * @return the CMYK
     */
    @Override
    public double[] getCMYK() {
        double[] cmyk = {getLevel(0), getLevel(1), getLevel(2), getLevel(3)};
        return cmyk;
    }

    /**
     * Gets a PostScript array describing the color space of this color.
     * 
     * @param vm The VM manager.
     * 
     * @return array describing color space.
     * 
     * @throws PSErrorVMError Virtual memory error.
     * @throws PSErrorRangeCheck A PostScript rangecheck error occurred.
     */
    @Override
    public PSObjectArray getColorSpace(final VM vm) throws PSErrorVMError,
            PSErrorRangeCheck {
        
        PSObjectArray array = new PSObjectArray(vm);
        array.addToEnd(new PSObjectName("DeviceCMYK", true));
        return array;
    }

    /**
     * Gets the gray level equivalent of this color.
     * 
     * @return the gray level
     */
    @Override
    public double getGray() {
        return (1.0 - Math.min(1.0, 0.3 * getLevel(0) + 0.59 * getLevel(1)
               + 0.11 * getLevel(2) + getLevel(3)));
    }

    /**
     * Gets the equivalent HSB levels of this color.
     * 
     * @return the HSB
     */
    @Override
    public double[] getHSB() {
        double[] rgb = getRGB();
        return DeviceRGB.convertRGBtoHSB(rgb[0], rgb[1], rgb[2]);
    }

    /**
     * Gets the number of color components required to specify this color.
     * E.g. RGB has three and CMYK has four components.
     * 
     * @return the number of components for this color
     */
    @Override
    public int getNrComponents() {
        return 4;
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
        return getNrComponents();
    }
    
    /**
     * Gets the preferred color space to be used by output devices. Since output
     * devices generally do not support all the color spaces that PostScript
     * uses, the PSColor must specify which color space is preferred. It must be
     * either: "Gray", "RGB" or "CMYK".
     * 
     * @return Always returns "CMYK".
     */
    @Override
    public String getPreferredColorSpace() {
        return "CMYK";
    }

    /**
     * Gets the equivalent RGB levels of this color.
     * 
     * @return the RGB
     */
    @Override
    public double[] getRGB() {
        double r = 1.0 - Math.min(1.0, getLevel(0) + getLevel(3));
        double g = 1.0 - Math.min(1.0, getLevel(1) + getLevel(3));
        double b = 1.0 - Math.min(1.0, getLevel(2) + getLevel(3));
        double[] rgb = {r, g, b};
        return rgb;
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
    

}
