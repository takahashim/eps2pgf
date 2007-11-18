/*
 * Gray.java
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

public class Gray extends PSColor {
	// Default color is black
	static double[] defaultLevels = {0.0};
	
	/**
	 * Instantiates a new gray color.
	 */
	public Gray() {
		levels = defaultLevels.clone();
	}
	
	public Gray clone() {
		Gray newGray = new Gray();
		newGray.levels = levels.clone();
		return newGray;
	}

	public double[] getCMYK() {
        double c = 0.0;
        double m = 0.0;
        double y = 0.0;
        double k = 1.0 - levels[0];
        double[] cmyk = {c, m, y, k};
        return cmyk;
	}
	
	public PSObjectArray getColorSpace() {
		PSObjectArray array = new PSObjectArray();
		array.addToEnd(new PSObjectName("DeviceGray", true));
		return array;
	}

	public double getGray() {
		return levels[0];
	}

	public double[] getHSB() {
		return RGB.RGBtoHSB(levels[0], levels[0], levels[0]);
	}

	public int getNrComponents() {
		return 1;
	}

	public double[] getRGB() {
        double[] rgb = {levels[0], levels[0], levels[0]};
        return rgb;
	}

}
