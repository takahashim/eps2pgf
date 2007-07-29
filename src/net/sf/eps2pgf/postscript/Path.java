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
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.sf.eps2pgf.postscript;

import java.util.*;
import net.sf.eps2pgf.ProgramError;

import net.sf.eps2pgf.postscript.errors.*;

/** Represents a PostScript path
 *
 * @author Paul Wagenaars
 */
public class Path implements Cloneable {
    /**
     * List with sections of this path
     */
    public ArrayList<PathSection> sections;
    GstateStack gStateStack;
    
    /**
     * Creates a new instance of Path
     * @param graphicsStateStack Pointer to the graphics state to which this path is linked
     */
    public Path(GstateStack graphicsStateStack) {
        sections = new ArrayList<PathSection>();
        gStateStack = graphicsStateStack;
    }
    
    /**
     * Return the bounding box (in device coordinates) of the current path
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorNoCurrentPoint The path is empty
     * @return Array with X- and Y-coordinates of lower-left and upper-right
     * corners of the smallest rectangle that encloses this path.
     */
    public double[] boundingBox() throws PSErrorNoCurrentPoint {
        int N = sections.size();
        if (N < 1) {
            throw new PSErrorNoCurrentPoint();
        }
        
        double[] bbox = {Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY,
                Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY};
        for (int i = 0 ; i < N ; i++) {
            PathSection sec = sections.get(i);
            if ((i == (N-1)) && (sec instanceof Moveto)) {
                break;
            }
            for (int j = 0 ; j < sec.params.length ; j += 2) {
                if (Double.isNaN(sec.params[j]) || Double.isNaN(sec.params[j+1])) {
                    break;
                }
                bbox[0] = Math.min(bbox[0], sec.params[j]);
                bbox[1] = Math.min(bbox[1], sec.params[j+1]);
                bbox[2] = Math.max(bbox[2], sec.params[j]);
                bbox[3] = Math.max(bbox[3], sec.params[j+1]);
            }
        }
        
        return bbox;
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
     * Add a straight line to the beginning of this subpath and start a 
     * new subpath.
     * @return Returns the starting coordinate of this path. (in document
     * coordinates, before CTM, in pt)
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorInvalidAccess Insufficient access rights
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorRangeCheck Value out of range
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorTypeCheck Object of invalid type
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
                position = gStateStack.current.CTM.itransform(section.params[0], 
                        section.params[1]);
                break;
            }
        }
        
        Closepath closepath = new Closepath(position);
        sections.add(closepath);

        return position;
    }
    
    /**
     * Returns a flattened version of the path. This path itself it not changed,
     * a new path is created with the flattened version of this path.
     * @param maxError Maximum distance between flattened path and real curve.
     *                 Expressed in terms of actual device coordinates.
     * @return Flattened versiob of this path
     * @throws net.sf.eps2pgf.postscript.errors.PSError Something went wrong during the "flattening" of the path
     * @throws net.sf.eps2pgf.ProgramError If this happens there's a bug in Eps2pgf
     */
    public Path flattenpath(double maxError) throws PSError, ProgramError {
        Path flatPath = new Path(gStateStack);
        PathSection lastSec = new PathSection();
        double x0, y0;
        for (PathSection sec : sections) {
            if (sec instanceof Moveto) {
                flatPath.moveto(sec.params[0], sec.params[1]);
            } else if (sec instanceof Lineto) {
                flatPath.lineto(sec.params[0], sec.params[1]);
            } else if (sec instanceof Closepath) {
                flatPath.closepath();
            } else if (sec instanceof Curveto) {
                ((Curveto)sec).flatten(flatPath, lastSec.deviceCoor(), maxError);
            } else {
                throw new ProgramError("You've found a bug. Flattening this ("
                        + sec + ") type is not implemented.");
            }
            lastSec = sec;
        }
        return flatPath;
    }
    
    /**
     * Adds a moveto to this path.
     * @param x X-coordinate in device coordinates
     * @param y Y-coordinate in device coordinates
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
     * @param x X-coordinate in device coordinates
     * @param y Y-coordinate in device coordinates
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
     * @return Human-readable string representation of this path
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
