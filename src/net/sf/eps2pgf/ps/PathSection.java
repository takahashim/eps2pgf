/*
 * PathSection.java
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

package net.sf.eps2pgf.ps;

import java.util.logging.Logger;

/**
 * Represent a part of a PostScript path (i.e. lineto, curveto, moveto, ...)
 * @author Paul Wagenaars
 */
public class PathSection implements Cloneable {
    
    /** Coordinates associated with this path section. */
    private double[] params = new double[6];
    
    /** The logger. */
    private static final Logger LOG
                                  = Logger.getLogger("net.sourceforge.eps2pgf");
    
    /**
     * Create a clone of this object.
     * @return Returns clone of this object.
     */
    @Override
    public PathSection clone() {
        LOG.finest("PathSection clone() called.");
        PathSection copy;
        try {
            copy = (PathSection) super.clone();
        } catch (CloneNotSupportedException e) {
            copy = new PathSection();
        }
        copy.params = params.clone();
        return copy;
    }
    
    /**
     * Get position in device space coordinates.
     * @return X- and Y-coordinate in device space. Returns {NaN, NaN} when
     *         this section has no coordinate.
     */
    public double[] deviceCoor() {
        double[] coor = new double[2];
        coor[0] = getParam(0);
        coor[1] = getParam(1);
        return coor;
    }
    
    /**
     * Gets the parameter with the specified index.
     * 
     * @param index Index of the value to get. 
     * 
     * @return the param
     */
    public double getParam(final int index) {
        return params[index];
    }
    
    /**
     * Returns the maximum number of parameters that can be defined.
     * 
     * @return Maximum number of parameters.
     */
    public int nrParams() {
        return params.length;
    }
    
    /**
     * Sets the parameter with the specified index.
     * 
     * @param index Index of the value to set.
     * @param newValue The new value. 
     */
    public void setParam(final int index, final double newValue) {
        params[index] = newValue;
    }
    
    
}
