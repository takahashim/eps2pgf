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
import net.sf.eps2pgf.util.CloneMappings;
import net.sf.eps2pgf.util.MapCloneable;

/**
 * Path section representing a PostScript moveto operator.
 * @author Paul Wagenaars
 */
public class Moveto extends PathSection implements MapCloneable {
    
    /**
     * Create a new Movoto instance.
     */
    public Moveto() {
        int nr = nrParams();
        for (int i = 0; i < nr; i++) {
            setParam(i, Double.NaN);
        }        
    }
    
    /**
     * Create a new Moveto instance.
     * 
     * @param x X-coordinate (in device coordinates, micrometer)
     * @param y Y-coordinate (in device coordinates, micrometer)
     */
    public Moveto(final double x, final double y) {
        setParam(0, x);
        setParam(1, y);
        int nr = nrParams();
        for (int i = 2; i < nr; i++) {
            setParam(i, Double.NaN);
        }
    }

    /**
     * Create a string representation of this object.
     * 
     * @return String representation of this object.
     */
    @Override
    public String isis() {
        return String.format("%.4g %.4g moveto", getParam(0), getParam(1));
    }
    
    /**
     * Create a clone of this object.
     * 
     * @param cloneMap The clone map.
     * 
     * @return Returns clone of this object.
     * 
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    @Override
    public Moveto clone(CloneMappings cloneMap) throws ProgramError {
        if (cloneMap == null) {
            cloneMap = new CloneMappings();
        } else if (cloneMap.containsKey(this)) {
            return (Moveto) cloneMap.get(this);
        }

        Moveto copy = (Moveto) super.clone(cloneMap);
        cloneMap.add(this, copy);
        
        return copy;
    }

}
