/*
 * This file is part of Eps2pgf.
 *
 * Copyright 2007-2009 Paul Wagenaars <paul@wagenaars.org>
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

import net.sf.eps2pgf.ps.objects.PSObject;

/**
 * Represent a part of a PostScript path (i.e. lineto, curveto, moveto, ...)
 * @author Paul Wagenaars
 */
public class PathSection extends PSObject implements Cloneable {
    
    /** Coordinates associated with this path section. */
    private double[] params = new double[6];
    
    /**
     * Create a clone of this object.
     * 
     * @return Returns clone of this object.
     */
    @Override
    public PathSection clone() {
        PathSection copy = (PathSection) super.clone();
        
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
     * Returns a duplicate of this object. The duplicates shares the params with
     * this object.
     * 
     * @return Duplicate of this object.
     */
    @Override
    public PathSection dup() {
        return (PathSection) super.clone();
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

    /**
     * Returns this path section.
     * 
     * @return This object.
     */
    @Override
    public PathSection toPathSection() {
        return this;
    }
    
    /**
     * Checks whether this object is equal.
     * 
     * @param obj The object to compare to.
     * 
     * @return true, if equals
     */
    @Override
    public boolean equals(Object obj) {
        if ((obj != null) && (this.getClass().equals(obj.getClass()))) {
            PathSection secObj = (PathSection) obj;
            for (int i = 0; i < params.length; i++) {
                double diff = Math.abs(params[i] - secObj.params[i]);
                double maxValue = 
                    Math.max(Math.abs(params[i]), Math.abs(secObj.params[i]));
                if ((diff > 0.0) && ((diff / maxValue) > 1e-10)) {
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * Return a hash code of this object.
     * 
     * @return The hash code.
     */
    @Override
    public int hashCode() {
        int code = 0;
        for (int i = 0; i < params.length; i++) {
            if (Double.isInfinite(params[i]) || Double.isNaN(params[i])) {
                code += i * 1000;
            } else {
                code += i * (int) (1e3 * params[i]);
            }
        }
        code += this.getClass().hashCode();
        return code;
    }
    
}
