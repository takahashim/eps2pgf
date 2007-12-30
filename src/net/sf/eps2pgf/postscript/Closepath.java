/*
 * Closepath.java
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
 *
 * @author Paul Wagenaars
 */
public class Closepath extends PathSection implements Cloneable {
    
    /**
     * Create a new Closepath object.
     * 
     * @param position Coordinate to where this closepath returns.
     */
    public Closepath(final double[] position) {
        int nr = nrParams();
        for (int i = 0; i < nr; i++) {
            setParam(i, Double.NaN);
        }
    }
    
    /**
     * Create a clone of this object.
     * @return Returns clone of this object.
     */
    public Closepath clone() {
        Closepath copy = (Closepath) super.clone();
        return copy;
    }

}
