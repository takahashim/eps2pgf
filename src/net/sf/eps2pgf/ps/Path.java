/*
 * Path.java
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

import java.util.ArrayList;
import java.util.logging.Logger;

import net.sf.eps2pgf.ProgramError;
import net.sf.eps2pgf.ps.errors.PSError;
import net.sf.eps2pgf.ps.errors.PSErrorNoCurrentPoint;
import net.sf.eps2pgf.ps.errors.PSErrorRangeCheck;
import net.sf.eps2pgf.ps.errors.PSErrorTypeCheck;

/**
 * Represents a PostScript path.
 *
 * @author Paul Wagenaars
 */
public class Path implements Cloneable {
    /** List with sections of this path. */
    private ArrayList<PathSection> sections = new ArrayList<PathSection>();
    
    /** Reference to the graphics state stack this path is part of. */
    private GstateStack gStateStack;
    
    /** The logger. */
    private static final Logger LOG =
                                    Logger.getLogger("net.sourceforge.eps2pgf");
    
    /**
     * Creates a new instance of Path.
     * 
     * @param graphicsStateStack Pointer to the graphics state to which this
     * path is linked.
     */
    public Path(final GstateStack graphicsStateStack) {
        gStateStack = graphicsStateStack;
    }
    
    /**
     * Return the bounding box (in device coordinates) of the current path.
     * 
     * @throws PSErrorNoCurrentPoint The path is empty.
     * 
     * @return Array with X- and Y-coordinates of lower-left and upper-right
     * corners of the smallest rectangle that encloses this path.
     */
    public double[] boundingBox() throws PSErrorNoCurrentPoint {
        int nr = getSections().size();
        if (nr < 1) {
            throw new PSErrorNoCurrentPoint();
        }
        
        double[] bbox = {Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY,
                Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY};
        for (int i = 0; i < nr; i++) {
            PathSection sec = getSections().get(i);
            if ((nr > 1) && (i == (nr - 1)) && (sec instanceof Moveto)) {
                break;
            }
            for (int j = 0; j < sec.nrParams(); j += 2) {
                if (Double.isNaN(sec.getParam(j))
                        || Double.isNaN(sec.getParam(j + 1))) {
                    break;
                }
                bbox[0] = Math.min(bbox[0], sec.getParam(j));
                bbox[1] = Math.min(bbox[1], sec.getParam(j + 1));
                bbox[2] = Math.max(bbox[2], sec.getParam(j));
                bbox[3] = Math.max(bbox[3], sec.getParam(j + 1));
            }
        }
        
        return bbox;
    }
    
    /**
     * Create a clone of this object.
     * @return Returns a clone of this object. 
     */
    @Override
    public Path clone() {
        LOG.finest("Path clone() called.");
        Path copy;
        try {
            copy = (Path) super.clone();
            copy.sections = new ArrayList<PathSection>();
            copy.gStateStack = gStateStack;
        } catch (CloneNotSupportedException e) {
            copy =  new Path(gStateStack);
        }
        for (int i = 0; i < getSections().size(); i++) {
            copy.getSections().add(getSections().get(i).clone());
        }
        return copy;
    }
    
    /**
     * Add a straight line to the beginning of this subpath and start a
     * new subpath.
     * 
     * @return Returns the starting coordinate of this path. (in document
     * coordinates, before CTM, in pt)
     * 
     * @throws PSErrorRangeCheck A PostScript rangecheck error occurred.
     * @throws PSErrorTypeCheck A PostScript typecheck error occurred.
     */
    public double[] closepath() throws PSErrorRangeCheck, PSErrorTypeCheck {
        int len = getSections().size();
        // If the path is empty closepath does nothing
        if (len == 0) {
            return null;
        }
        // If the subpath is already closed closepath does nothing
        if (getSections().get(len - 1) instanceof Moveto) {
            return null;
        }

        // Search the start of the subpath
        double[] position = {Double.NaN, Double.NaN};
        for (int i = len - 1; i >= 0; i--) {
            PathSection section = getSections().get(i);
            if (section instanceof Moveto) {
                position = gStateStack.current().getCtm().itransform(
                        section.getParam(0), section.getParam(1));
                break;
            }
        }
        
        Closepath closepath = new Closepath(position);
        getSections().add(closepath);

        return position;
    }
    
    /**
     * Returns a flattened version of the path. This path itself it not changed,
     * a new path is created with the flattened version of this path.
     * 
     * @param maxError Maximum distance between flattened path and real curve.
     * Expressed in terms of actual device coordinates.
     * 
     * @return Flattened version of this path
     * 
     * @throws PSError A PostScript error occurred.
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    public Path flattenpath(final double maxError)
            throws PSError, ProgramError {
        Path flatPath = new Path(gStateStack);
        PathSection lastSec = new PathSection();
        for (PathSection sec : getSections()) {
            if (sec instanceof Moveto) {
                flatPath.moveto(sec.getParam(0), sec.getParam(1));
            } else if (sec instanceof Lineto) {
                flatPath.lineto(sec.getParam(0), sec.getParam(1));
            } else if (sec instanceof Closepath) {
                flatPath.closepath();
            } else if (sec instanceof Curveto) {
                ((Curveto) sec).flatten(flatPath, lastSec.deviceCoor(),
                                                                      maxError);
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
     * 
     * @param x X-coordinate in device coordinates
     * @param y Y-coordinate in device coordinates
     */
    public void moveto(final double x, final double y) {
        int len = getSections().size();
        if (len > 0) {
            PathSection lastElem = getSections().get(len - 1);
            if (lastElem instanceof Moveto) {
                getSections().remove(len - 1);
            }
        }
        getSections().add(new Moveto(x, y));
    }
    
    /**
     * Adds a lineto to this path.
     * 
     * @param x X-coordinate in device coordinates
     * @param y Y-coordinate in device coordinates
     */
    public void lineto(final double x, final double y) {
        getSections().add(new Lineto(x, y));
    }
    
    /**
     * Adds a curveto to this path.
     * 
     * @param control1 First Bezier control point
     * @param control2 Second Bezier control point
     * @param end Endpoint
     */
    public void curveto(final double[] control1, final double[] control2,
            final double[] end) {
        getSections().add(new Curveto(control1, control2, end));
    }
    
    /**
     * Creates a human-readable string representation of this object.
     * @return Human-readable string representation of this path
     */
    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append("Path (" + getSections().size() + " items)\n");
        for (int i = 0; i < getSections().size(); i++) {
            str.append(getSections().get(i).toString() + "\n");
        }
        return str.toString();
    }

    /**
     * @return the sections
     */
    public ArrayList<PathSection> getSections() {
        return sections;
    }
    
}
