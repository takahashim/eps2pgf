/*
 * PSColor.java
 *
 * This file is part of Eps2pgf.
 *
 * Copyright 2007 Paul Wagenaars <paul@wagenaars.org>
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

package net.sf.eps2pgf.postscript.colors;

import net.sf.eps2pgf.postscript.PSObjectArray;
import net.sf.eps2pgf.postscript.errors.PSErrorRangeCheck;

/**
 * The Interface PSColor.
 */
public abstract class PSColor {
	/** Color levels of this color. Exact meaning depends of color space. */
	public double[] levels;
	
	/**
	 * Creates an exact deep copy of this object.
	 * 
	 * @return an exact deep copy of this object.
	 */
	public abstract PSColor clone();
	
	/**
	 * Gets the equivalent CMYK levels of this color.
	 * 
	 * @return the CMYK
	 */
	public abstract double[] getCMYK();
	
	/**
	 * Gets a PostScript array describing the color space of this color.
	 * 
	 * @return array describing color space.
	 */
	public abstract PSObjectArray getColorSpace();

	/**
	 * Gets the gray level equivalent of this color.
	 * 
	 * @return the gray level
	 */
	public abstract double getGray();
	
	/**
	 * Gets the equivalent HSB levels of this color.
	 * 
	 * @return the HSB
	 */
	public abstract double[] getHSB();
	
	/**
	 * Gets the number of color components required to specify this color.
	 * E.g. RGB has three and CMYK has four components.
	 * 
	 * @return the number of components for this color
	 */
	public abstract int getNrComponents();
	
	/**
	 * Gets the equivalent RGB levels of this color.
	 * 
	 * @return the RGB
	 */
	public abstract double[] getRGB();
	
	/**
	 * Changes the current color to another color in the same color space.
	 * 
	 * @param components the new color
	 */
	public void setColor(double[] components) throws PSErrorRangeCheck {
		if (components.length != getNrComponents()) {
			throw new PSErrorRangeCheck();
		}
		
		levels = components.clone();
		for (int i = 0 ; i < levels.length ; i++) {
			levels[i] = Math.min(levels[i], 1.0);
			levels[i] = Math.max(levels[i], 0.0);
		}
	}
	
}
