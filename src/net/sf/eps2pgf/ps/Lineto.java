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

/**
 * Represents a path section formed by the lineto operator.
 *
 * @author Paul Wagenaars
 */
public class Lineto extends PathSection implements Cloneable {
    
    /**
     * Create a new Lineto instance.
     */
    public Lineto() {
        for (int i = 0; i < nrParams(); i++) {
            setParam(i, Double.NaN);
        }
    }
    
    /**
     * Create a new Lineto instance.
     * 
     * @param x X-coordinate
     * @param y Y-coordinate
     */
    public Lineto(final double x, final double y) {
        setParam(0, x);
        setParam(1, y);
        for (int i = 2; i < nrParams(); i++) {
            setParam(i, Double.NaN);
        }
    }
    
    /**
     * Create a string representation of this object.
     * 
     * @return String representation of this object.
     */
    @Override
    public String toString() {
        return String.format("lineto (%.4g, %.4g)", getParam(0), getParam(1));
    }
    
    /**
     * Create a clone of this object.
     * 
     * @return Returns clone of this object.
     */
    @Override
    public Lineto clone() {
        return (Lineto) super.clone();
    }

}
