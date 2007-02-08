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
    
    // TeX points are 1/72.27 inch, while PostScript points are 1/72 inch.
    // That is very annoying. The default CTM converts PostScript pt used
    // in eps files to centimeters.
    // [a b c d tx ty] -> x' = a*x + b*y + tx ; y' = c*x + d*y * ty
    public double[] CTM = {2.54/72.0, 0 ,0, 2.54/72.0, 0, 0};
    
    // Current position in pt before the CTM is applied
    public double[] position = new double[2];
    
    // Current path
    public Path path;
    
    // Current clipping path;
    public Path clippingPath;
    
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
     * Scales the CTM
     */
    public void scale(double sx, double sy) {
        // [a b c d xx yy] a = CTM[0], b = CTM[1], c = CTM[2], d = CTM[3], xx = CTM[4], yy = CTM[5]
        CTM[0] = sx*CTM[0];
        CTM[1] = sx*CTM[1];
        CTM[2] = sy*CTM[2];
        CTM[3] = sy*CTM[3];
    }
    
    /**
     * Translates the CTM
     */
    public void translate(double tx, double ty) {
        // [a b c d xx yy] a = CTM[0], b = CTM[1], c = CTM[2], d = CTM[3], xx = CTM[4], yy = CTM[5]
        CTM[4] = tx*CTM[0] + ty*CTM[2] + CTM[4];
        CTM[5] = tx*CTM[1] + ty*CTM[3] + CTM[5];
    }
    
    /**
     * Rotates the CTM (current transformation matrix)
     */
    public void rotate(double angle) {
        // [a b c d xx yy] a = CTM[0], b = CTM[1], c = CTM[2], d = CTM[3], xx = CTM[4], yy = CTM[5]
        double cosa = Math.cos(angle*Math.PI/180);
        double sina = Math.sin(angle*Math.PI/180);
        double a = cosa*CTM[0] + sina*CTM[2];
        double b = cosa*CTM[1] + sina*CTM[3];
        double c = -sina*CTM[0] + cosa*CTM[2];
        double d = -sina*CTM[1] + cosa*CTM[3];
        CTM[0] = a;
        CTM[1] = b;
        CTM[2] = c;
        CTM[3] = d;
    }
    
    /**
     * Applies the current transformation matrix (CTM) to a point
     */
    public double[] applyCTM(double x, double y) {
        // [a b c d xx yy] a = CTM[0], b = CTM[1], c = CTM[2], d = CTM[3], tx = CTM[4], ty = CTM[5]
        double[] converted = new double[2];
        converted[0] = CTM[0]*x + CTM[2]*y + CTM[4];
        converted[1] = CTM[1]*x + CTM[3]*y + CTM[5];
        return converted;
    }
    
    /**
     * Apples the current transformation matrix (CTM) to a point
     */
    public double[] applyCTM(double[] coor) {
        double x = coor[0];
        double y = coor[1];
        return applyCTM(x, y);
    }
    
    /**
     * Returns the mean scaling factor described by the CTM
     */
    public double getMeanScaling() {
        double xScale = Math.sqrt(Math.pow(CTM[0], 2) + Math.pow(CTM[2], 2));
        double yScale = Math.sqrt(Math.pow(CTM[1], 2) + Math.pow(CTM[3], 2));
        return 0.5 * (xScale + yScale);
    }
    
    /**
     * Move the current position to a new location (PostScript moveto operator)
     * @param x X-coordinate (before CTM is applied)
     * @param y Y-coordinate (before CTM is applied)
     */
    public void moveto(double x, double y) {
        position[0] = x;
        position[1] = y;
        double[] transformed = applyCTM(position);
        path.moveto(transformed[0], transformed[1], x, y);
    }
    
    /**
     * Draw a line to a relative point.
     * @param dx delta X-coordinate (before CTM is applied)
     * @param dy delta Y-coordinate (before CTM is applied)
     */
    public void rlineto(double dx, double dy) {
        position[0] = position[0] + dx;
        position[1] = position[1] + dy;
        double[] transformed = applyCTM(position);
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
