/*
 * CMYK.java
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
import net.sf.eps2pgf.postscript.PSObjectName;

public class CMYK extends PSColor {
	// Default color is black.
	static double[] defaultLevels = {0.0, 0.0, 0.0, 1.0};
	
	/**
	 * Instantiates a new CMYK color.
	 */
	public CMYK() {
		levels = defaultLevels.clone();
	}

	public CMYK clone() {
		CMYK newCMYK = new CMYK();
		newCMYK.levels = levels.clone();
		return newCMYK;
	}

	public double[] getCMYK() {
		return levels;
	}

	public PSObjectArray getColorSpace() {
		PSObjectArray array = new PSObjectArray();
		array.addToEnd(new PSObjectName("DeviceCMYK", true));
		return array;
	}

	public double getGray() {
		return (1.0 - Math.min(1.0, 0.3*levels[0] + 0.59*levels[1]
		       + 0.11*levels[2] + levels[3]));
	}

	public double[] getHSB() {
		double[] rgb = getRGB();
		return RGB.RGBtoHSB(rgb[0], rgb[1], rgb[2]);
	}

	public int getNrComponents() {
		return 4;
	}

	public double[] getRGB() {
        double r = 1.0 - Math.min(1.0, levels[0] + levels[3]);
        double g = 1.0 - Math.min(1.0, levels[1] + levels[3]);
        double b = 1.0 - Math.min(1.0, levels[2] + levels[3]);
        double[] rgb = {r, g, b};
        return rgb;
	}

}
