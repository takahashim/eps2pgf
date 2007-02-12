/*
 * GraphicsState.java
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

/** Structure that holds the graphics state (graphic control parameter).
 *
 * @author Paul Wagenaars
 */
public class GraphicsState implements Cloneable {
    // See PostScript manual table 4.1, p. 179 for more info
    
    /**
     * Current Transformation Matrix (CTM). All coordinates will be transformed by
     * this matrix.
     * TeX points are 1/72.27 inch, while PostScript points are 1/72 inch.
     * That is very annoying. The default CTM converts PostScript pt used
     * in eps files to centimeters.
     * [a b c d tx ty] -> x' = a*x + b*y + tx ; y' = c*x + d*y * ty
     */
    public PSObjectMatrix CTM = new PSObjectMatrix(2.54/72.0, 0 ,0, 2.54/72.0, 0, 0);
    
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
    public PSObjectDict font;
    
    /**
     * Creates a new default graphics state.
     */
    public GraphicsState() {
        path = new Path();
        clippingPath = new Path();
        font = new PSObjectDict();
    }
    
    
    /**
     * Move the current position to a new location (PostScript moveto operator)
     * @param x X-coordinate (before CTM is applied)
     * @param y Y-coordinate (before CTM is applied)
     */
    public void moveto(double x, double y) {
        double[] transformed = CTM.apply(x, y);
        path.moveto(transformed[0], transformed[1], x, y);
    }
    
    /**
     * Draw a line to a point.
     * @param x X-coordinate (before CTM is applied)
     * @param y Y-coordinate (before CTM is applied)
     */
    public void lineto(double x, double y) {
        double[] transformed = CTM.apply(x, y);
        path.lineto(transformed[0], transformed[1]);
    }
    
    /**
     * Draw a line to a relative point.
     * @param dx delta X-coordinate (before CTM is applied)
     * @param dy delta Y-coordinate (before CTM is applied)
     */
    public void rlineto(double dx, double dy) {
        position[0] = position[0] + dx;
        position[1] = position[1] + dy;
        double[] transformed = CTM.apply(position);
        path.lineto(transformed[0], transformed[1]);
    }
    
    /**
     * Intersects the area inside the current clipping path with the area
     * inside the current path.
     */
    public void clip() {
        if (clippingPath.sections.size() > 0) {
            System.out.println("WARNING: clip operator is not fully implemented. " +
                    "This might have an effect on the result.");
        }
        clippingPath = path.clone();
    }
    
    /**
     * Creates a deep copy of this object.
     * @throws java.lang.CloneNotSupportedException Indicates that clone is not (fully) supported. This should not happen.
     * @return Returns the deep copy.
     */
    public GraphicsState clone() throws CloneNotSupportedException {
        GraphicsState newState = new GraphicsState();
        newState.CTM = CTM.clone();
        newState.position = position.clone();
        newState.path = path.clone();
        newState.clippingPath = clippingPath.clone();
        newState.font = font.clone();
        return newState;
    }
}
