/*
 * GraphicsState.java
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

import java.util.logging.*;

import net.sf.eps2pgf.postscript.errors.*;

/**
 * Structure that holds the graphics state (graphic control parameter).
 * See PostScript manual table 4.1, p. 179 for more info.
 *
 * @author Paul Wagenaars
 */
public class GraphicsState implements Cloneable {
    Logger log = Logger.getLogger("global");
    
    /**
     * Current Transformation Matrix (CTM). All coordinates will be transformed by
     * this matrix.
     * TeX points are 1/72.27 inch, while PostScript points are 1/72 inch.
     * That is very annoying. The default CTM converts PostScript pt used
     * in eps files to micrometers.
     * [a b c d tx ty] -> x' = a*x + b*y + tx ; y' = c*x + d*y * ty
     */
    public PSObjectMatrix CTM = new PSObjectMatrix();
    public static PSObjectMatrix defaultCTM = new PSObjectMatrix(25.4*1000/72.0, 0 ,0, 25.4*1000/72.0, 0, 0);
    
    /**
     * Current position in pt (before CTM is applied).
     */
    public double[] position = new double[2];
    
    /**
     * Current path
     */
    public Path path;
    
    /**
     * Current clipping path
     */
    public Path clippingPath;
    
    /**
     * Current font
     */
    public PSObjectFont font;
    
    /**
     * Link to the parent graphics state stack
     */
    GstateStack parentStack;
    
    /**
     * Creates a new default graphics state.
     */
    public GraphicsState(GstateStack parentGraphicsStack) {
        initmatrix();
        path = new Path(parentGraphicsStack);
        clippingPath = new Path(parentGraphicsStack);
        font = new PSObjectFont();
        parentStack = parentGraphicsStack;
    }
    
    /**
     * Intersects the area inside the current clipping path with the area
     * inside the current path.
     */
    public void clip() {
        clippingPath = path.clone();
    }
    
    /**
     * Creates a deep copy of this object.
     * @throws java.lang.CloneNotSupportedException Indicates that clone is not (fully) supported. This should not happen.
     * @return Returns the deep copy.
     */
    public GraphicsState clone() throws CloneNotSupportedException {
        GraphicsState newState = new GraphicsState(parentStack);
        newState.CTM = CTM.clone();
        newState.position = position.clone();
        newState.path = path.clone();
        newState.clippingPath = clippingPath.clone();
        newState.font = font.clone();
        return newState;
    }

    /**
     * Add a curveto section to the current path
     * @param x1 X-coordinate of first control point
     * @param y1 Y-coordinate of first control point
     * @param x2 X-coordinate of second control point
     * @param y2 Y-coordinate of second control point
     * @param x3 X-coordinate of end point
     * @param y3 Y-coordinate of end point
     */
    public void curveto(double x1, double y1, double x2, double y2, 
            double x3, double y3) throws PSErrorInvalidAccess, PSErrorRangeCheck,
            PSErrorTypeCheck {
        position[0] = x3;
        position[1] = y3;
        double[] coor1 = CTM.apply(x1, y1);
        double[] coor2 = CTM.apply(x2, y2);
        double[] coor3 = CTM.apply(x3, y3);
        path.curveto(coor1, coor2, coor3);
    }
    
    /**
     * Retrieves the current position in device space. 
     * @return X- and Y-coordinate in device space (micrometers)
     */
    public double[] getCurrentPosInDeviceSpace() throws PSErrorNoCurrentPoint {
        if (path.sections.size() == 0) {
            throw new PSErrorNoCurrentPoint();
        }
        return path.sections.get(path.sections.size() - 1).deviceCoor();
    }
    
    /**
     * Sets the current transformation matrix (CTM) to its default value
     */
    public void initmatrix() {
        try {
            CTM.copy(defaultCTM);
        } catch (PSErrorRangeCheck e) {
            // this can never happen since both are matrices
        } catch (PSErrorTypeCheck e) {
            // this can never happen since both are matrices
        } catch (PSErrorInvalidAccess e) {
            // this can never happen because user can not change access
            // properties of these matrices.
        }
    }
    
    /**
     * Draw a line to a point.
     * @param x X-coordinate (before CTM is applied)
     * @param y Y-coordinate (before CTM is applied)
     */
    public void lineto(double x, double y) throws PSErrorInvalidAccess,
            PSErrorRangeCheck, PSErrorTypeCheck {
        position[0] = x;
        position[1] = y;
        double[] transformed = CTM.apply(x, y);
        path.lineto(transformed[0], transformed[1]);
    }
    
    /**
     * Move the current position to a new location (PostScript moveto operator)
     * @param x X-coordinate (before CTM is applied)
     * @param y Y-coordinate (before CTM is applied)
     */
    public void moveto(double x, double y) throws PSErrorInvalidAccess,
            PSErrorRangeCheck, PSErrorTypeCheck {
        position[0] = x;
        position[1] = y;
        double[] transformed = CTM.apply(x, y);
        path.moveto(transformed[0], transformed[1]);
    }
    
    /**
     * Draw a line to a relative point.
     * @param dx delta X-coordinate (before CTM is applied)
     * @param dy delta Y-coordinate (before CTM is applied)
     */
    public void rlineto(double dx, double dy) throws PSErrorNoCurrentPoint,
            PSErrorInvalidAccess, PSErrorRangeCheck, PSErrorTypeCheck {
        if (position[0] == Double.NaN) {
            throw new PSErrorNoCurrentPoint();
        }
        position[0] = position[0] + dx;
        position[1] = position[1] + dy;
        double[] transformed = CTM.apply(position);
        path.lineto(transformed[0], transformed[1]);
    }
    
    /**
     * Move to a relative point.
     * @param dx delta X-coordinate (before CTM is applied)
     * @param dy delta Y-coordinate (before CTM is applied)
     */
    public void rmoveto(double dx, double dy) throws PSErrorNoCurrentPoint,
            PSErrorInvalidAccess, PSErrorRangeCheck, PSErrorTypeCheck {
        if (position[0] == Double.NaN) {
            throw new PSErrorNoCurrentPoint();
        }
        position[0] = position[0] + dx;
        position[1] = position[1] + dy;
        double[] transformed = CTM.apply(position);
        path.moveto(transformed[0], transformed[1]);
    }
    
    /**
     * Updates the field current position by retrieving the last coordinate
     * of the current path and transforming it back to user space
     * coordinates. This is usually done after the CTM has been altered.
     */
    public void updatePosition() throws PSErrorInvalidAccess, PSErrorRangeCheck,
            PSErrorTypeCheck {
        try {
            double[] posd = getCurrentPosInDeviceSpace();
            position = CTM.inverseApply(posd);
        } catch (PSErrorNoCurrentPoint e) {
            // Apparently there is no current point
            position[0] = Double.NaN;
            position[1] = Double.NaN;
        }
    }
    
}
