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

import net.sf.eps2pgf.ProgramError;
import net.sf.eps2pgf.ps.objects.PSObject;
import net.sf.eps2pgf.util.CloneMappings;
import net.sf.eps2pgf.util.MapCloneable;

/**
 * Represent a part of a PostScript path (i.e. lineto, curveto, moveto, ...)
 * @author Paul Wagenaars
 */
public class PathSection extends PSObject implements MapCloneable {
    
    /** Coordinates associated with this path section. */
    private double[] params = new double[6];
    
    /**
     * Create a clone of this object.
     * 
     * @param cloneMap The clone mappings.
     * 
     * @return Returns clone of this object.
     * 
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    @Override
    public PathSection clone(CloneMappings cloneMap) throws ProgramError {
        if (cloneMap == null) {
            cloneMap = new CloneMappings();
        } else if (cloneMap.containsKey(this)) {
            return (PathSection) cloneMap.get(this);
        }
        
        PathSection copy = (PathSection) super.clone(cloneMap);
        cloneMap.add(this, copy);
        
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
     * PostScript operator 'dup'. Create a (shallow) copy of this object. The
     * values of composite object is not copied, but shared.
     * 
     * @return Shallow copy of this object.
     */
    @Override
    public PathSection dup() {
        PathSection ps;
        if (this instanceof Moveto) {
            ps = new Moveto();
        } else if (this instanceof Lineto) {
            ps = new Lineto();
        } else if (this instanceof Curveto) {
            ps = new Curveto();
        } else {
            double[] dummy = {Double.NaN, Double.NaN};
            ps = new Closepath(dummy);
        }
        ps.params = params;
        
        return ps;
    }
    
    /**
     * Indicates whether some other object is equal to this one.
     * Required when used as index in PSObjectDict
     * 
     * @param obj The object to compare to.
     * 
     * @return True, if equal.
     */
    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof PathSection)) {
            return false;
        }
        PathSection ps = (PathSection) obj;
        if (this.getClass() != ps.getClass()) {
            return false;
        }
        for (int i = 0; i < nrParams(); i++) {
            if (this.getParam(i) != ps.getParam(i)) {
                return false;
            }
        }
        
        return true;
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
     * Returns a hashCode value for this object. This method is supported
     * for the benefit hashtables, such as used in PSObjectDict.
     * 
     * @return Hash code for this object.
     */
    @Override
    public int hashCode() {
        int code = this.getClass().hashCode();
        for (int i = 0; i < nrParams(); i++) {
            double val = getParam(i);
            if (Double.isInfinite(val) || Double.isNaN(val)) {
                break;
            }
            code += (i + 1) * ((int) (val * 1e6));
        }
        return code;
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
     * @return This path section.
     */
    @Override
    public PathSection toPathSection() {
        return this;
    }
    
}
