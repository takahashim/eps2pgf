/*
 * Moveto.java
 *
 * This file is part of Eps2pgf.
 *
 * Copyright 2007 Paul Wagenaars <pwagenaars@fastmail.fm>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.sf.eps2pgf.postscript;

/**
 * Path section representing a PostScript moveto operator.
 * @author Paul Wagenaars
 */
public class Moveto extends PathSection implements Cloneable {
    
    /**
     * Create a new Movoto instance
     */
    public Moveto() {
        for (int i = 0 ; i < params.length ; i++) {
            params[i] = Double.NaN;
        }        
    }
    
    /**
     * Create a new Moveto instance
     * @param x X-coordinate (in device coordinates, micrometer)
     * @param y Y-coordinate (in device coordinates, micrometer)
     */
    public Moveto(double x, double y) {
        params[0] = x;
        params[1] = y;
        for (int i = 2 ; i < params.length ; i++) {
            params[i] = Double.NaN;
        }
    }

    /**
     * Create a string representation of this object.
     */
    public String toString() {
        return String.format("moveto (%.4g, %.4g)", params[0], params[1]);
    }
    
    /**
     * Create a clone of this object.
     * @return Returns clone of this object.
     */
    public Moveto clone() {
        Moveto newSection = new Moveto();
        newSection.params = params.clone();
        return newSection;
    }

}
