/*
 * Path.java
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

import java.util.*;

import net.sf.eps2pgf.postscript.errors.*;

/** Represents a PostScript path
 *
 * @author Paul Wagenaars
 */
public class Path implements Cloneable {
    public ArrayList<PathSection> sections;
    GstateStack gStateStack;
    
    /** Creates a new instance of Path */
    public Path(GstateStack graphicsStateStack) {
        sections = new ArrayList<PathSection>();
        gStateStack = graphicsStateStack;
    }
    
    /**
     * Add a straight line to the beginning of this subpath and start a 
     * new subpath.
     * @return Returns the starting coordinate of this path. (in document
     * coordinates, before CTM, in pt)
     */
    public double[] closepath() throws PSErrorInvalidAccess, PSErrorRangeCheck,
            PSErrorTypeCheck {
        int len = sections.size();
        // If the path is empty closepath does nothing
        if (len == 0) {
            return null;
        }
        // If the subpath is already closed closepath does nothing
        if (sections.get(len-1) instanceof Moveto) {
            return null;
        }

        // Search the start of the subpath
        double[] position = {Double.NaN, Double.NaN};
        for (int i = len-1 ; i >= 0 ; i--) {
            PathSection section = sections.get(i);
            if (section instanceof Moveto) {
                position = gStateStack.current.CTM.inverseApply(section.params[0], 
                        section.params[1]);
                break;
            }
        }
        
        Closepath closepath = new Closepath(position);
        sections.add(closepath);

        return position;
    }
    
    /**
     * Create a clone of this object.
     * @return Returns a clone of this object. 
     */
    public Path clone() {
        Path clonedPath = new Path(gStateStack);
        for (int i = 0 ; i < sections.size() ; i++) {
            clonedPath.sections.add(sections.get(i).clone());
        }
        return clonedPath;
    }
    
    /**
     * Adds a moveto to this path.
     */
    public void moveto(double x, double y) {
        int len = sections.size();
        if (len > 0) {
            PathSection lastElem = sections.get(len - 1);
            if (lastElem instanceof Moveto) {
                sections.remove(len - 1);
            }
        }
        sections.add(new Moveto(x, y));
    }
    
    /**
     * Adds a lineto to this path.
     */
    public void lineto(double x, double y) {
        sections.add(new Lineto(x, y));
    }
    
    /**
     * Adds a curveto to this path
     * @param control1 First Bezier control point
     * @param control2 Second Bezier control point
     * @param end Endpoint
     */
    public void curveto(double[] control1, double[] control2, double[] end) {
        sections.add(new Curveto(control1, control2, end));
    }
    
    /**
     * Creates a human-readable string representation of this object.
     */
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append("Path (" + sections.size() + " items)\n");
        for(int i = 0 ; i < sections.size() ; i++) {
            str.append(sections.get(i).toString() + "\n");
        }
        return str.toString();
    }
    
}
