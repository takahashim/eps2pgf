/*
 * Path.java
 *
 * This file is part of Eps2pgf.
 *
 * Copyright (C) 2007 Paul Wagenaars <pwagenaars@fastmail.fm>
 *
 * Eps2pgf is free software; you can redistribute it and/or modify it under
 * the terms of version 2 of the GNU General Public License as published by the
 * Free Software Foundation.
 *
 * Eps2pgf is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */

package net.sf.eps2pgf.postscript;

import com.sun.org.apache.bcel.internal.verifier.statics.DOUBLE_Upper;
import java.util.*;

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
    public double[] closepath() {
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
