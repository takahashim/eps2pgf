/*
 * Indexed.java
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

package net.sf.eps2pgf.postscript.colors;

import net.sf.eps2pgf.postscript.PSObject;
import net.sf.eps2pgf.postscript.PSObjectArray;
import net.sf.eps2pgf.postscript.PSObjectString;
import net.sf.eps2pgf.postscript.errors.PSError;
import net.sf.eps2pgf.postscript.errors.PSErrorRangeCheck;
import net.sf.eps2pgf.postscript.errors.PSErrorTypeCheck;
import net.sf.eps2pgf.postscript.errors.PSErrorUnimplemented;

/**
 * Implements Indexed color space
 */
public class Indexed extends PSColor {
	PSObjectArray colorSpaceArray;
	PSColor currentColor;
	
	public Indexed(PSObject obj) throws PSError {
		// Indexed color spaces must be defined using an array
		if (!(obj instanceof PSObjectArray)) {
			throw new PSErrorTypeCheck();
		}
		
		// Save the array specifying this color space
		colorSpaceArray = (PSObjectArray)obj;
		
		// Extract base color space
		currentColor = ColorUtils.autoSetColorSpace(colorSpaceArray.get(1));
		
		// Extract lookup table
		PSObject lookup = colorSpaceArray.get(3);
		if (lookup instanceof PSObjectString) {
			// Convert the lookup string to a more convenient format
			
		} else {
			throw new PSErrorUnimplemented("Indexed color space with non-string lookup table.");
		}
		
	}

	@Override
	public PSColor clone() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double[] getCMYK() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PSObjectArray getColorSpace() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double getGray() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double[] getHSB() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getNrComponents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double[] getRGB() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public void setColor(final double[] components)
			throws PSErrorRangeCheck {
		// TODO Implement set color method
	}

}
