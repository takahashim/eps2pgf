/*
 * PathSection.java
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

package net.sf.eps2pgf.postscript;

/**
 * Represent a part of a PostScript path (i.e. lineto, curveto, moveto, ...)
 * @author Paul Wagenaars
 */
public class PathSection implements Cloneable {
    public double[] params = new double[6];
    
    /**
     * Create a clone of this object.
     * @return Returns clone of this object.
     */
    public PathSection clone() {
        PathSection newSection = new PathSection();
        newSection.params = params.clone();
        return newSection;
    }
    
    /**
     * Get position in device space coordinates.
     * @return X- and Y-coordinate in device space. Returns {NaN, NaN} when
     *         this section has no coordinate.
     */
    public double[] deviceCoor() {
        double[] coor = new double[2];
        coor[0] = params[0];
        coor[1] = params[1];
        return coor;
    }
}
