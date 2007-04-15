/*
 * PSObjectMatrix.java
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

import net.sf.eps2pgf.postscript.errors.*;

/**
 * Represent a PostScript matrix. This is a six element array with only numeric
 * items.
 * @author Paul Wagenaars
 */
public class PSObjectMatrix extends PSObject {
    double[] matrix = new double[6];
    
    // Keep track of rotation (for easy reference, instead of deriving it
    // from the matrix.
    double rotation = 0;
    
    /**
     * Creates a new instance of PSObjectMatrix. See PostScript manual
     * under "4.3 Coordinate Systems and Transformation" for more info.
     * @param a See PostScript manual  under "4.3 Coordinate Systems and
     * Transformation" for more info.
     * @param b See PostScript manual  under "4.3 Coordinate Systems and
     * Transformation" for more info.
     * @param c See PostScript manual  under "4.3 Coordinate Systems and
     * Transformation" for more info.
     * @param d See PostScript manual  under "4.3 Coordinate Systems and
     * Transformation" for more info.
     * @param tx See PostScript manual  under "4.3 Coordinate Systems and
     * Transformation" for more info.
     * @param ty See PostScript manual  under "4.3 Coordinate Systems and
     * Transformation" for more info.
     */
    public PSObjectMatrix(double a, double b, double c, double d, double tx, double ty) {
        matrix[0] = a;
        matrix[1] = b;
        matrix[2] = c;
        matrix[3] = d;
        matrix[4] = tx;
        matrix[5] = ty;
    }
    
    /**
     * Creates a new instance of PSObjectMatrix. The new matrix is an exact copy of
     * refMatrix.
     * 
     * @param refMatrix PSObjectMatrix to copy.
     */
    public PSObjectMatrix(PSObjectMatrix refMatrix) {
        for (int i = 0 ; i < matrix.length ; i++) {
            matrix[i] = refMatrix.matrix[i];
        }
    }
    
    /**
     * Copies values from another matrix to this matrix.
     * @param obj Object from which the values must be copied.
     * @throws net.sf.eps2pgf.postscript.errors.PSError Unable to copy values.
     */
    public void copyValuesFrom(PSObject obj) throws PSErrorRangeCheck, PSErrorTypeCheck {
        PSObjectMatrix fromMatrix = obj.toMatrix();
        for (int i = 0 ; i < matrix.length ; i++) {
            matrix[i] = fromMatrix.matrix[i];
        }
    }
    
    /**
     * Creates a scaled copy of this matrix.
     *                       [sx 0  0]
     * Transformation matrix: [0  sy 0]
     *                       [0  0  1]
     * @param sx X-coodinate scaling factor.
     * @param sy Y-coordinate scaling factor.
     */
    public void scale(double sx, double sy) {
        // [a b c d xx yy]
        matrix[0] = sx*matrix[0];
        matrix[1] = sx*matrix[1];
        matrix[2] = sy*matrix[2];
        matrix[3] = sy*matrix[3];
    }
    
    /**
     * Translates the matrix
     *                         [1  0  0]
     * Transformation matrix = [0  1  0]
     *                         [sx sy 1]
     * @param tx X-coordinate translation
     * @param ty Y-coordinate translation
     */
    public void translate(double tx, double ty) {
        // [a b c d xx yy]
        matrix[4] = tx*matrix[0] + ty*matrix[2] + matrix[4];
        matrix[5] = tx*matrix[1] + ty*matrix[3] + matrix[5];
    }
    
    /**
     * Rotates the matrix (current transformation matrix)
     *                         [cos(a)  sin(a) 0]
     * Transformation matrix = [-sin(a) cos(a) 0]
     *                         [  0      0     1]
     * @param angle Angle in degrees for counterclockwise rotation.
     */
    public void rotate(double angle) {
        // [a b c d xx yy]
        double cosa = Math.cos(angle*Math.PI/180);
        double sina = Math.sin(angle*Math.PI/180);
        double a = cosa*matrix[0] + sina*matrix[2];
        double b = cosa*matrix[1] + sina*matrix[3];
        double c = -sina*matrix[0] + cosa*matrix[2];
        double d = -sina*matrix[1] + cosa*matrix[3];
        matrix[0] = a;
        matrix[1] = b;
        matrix[2] = c;
        matrix[3] = d;
    }
    
    /**
     * Applies the transformation represented by conc to this matrix.
     * newMatrix = conc * matrix
     * @param conc Matrix describing the transformation.
     */
    public void concat(PSObjectMatrix conc) {
        // [a b c d tx ty] [a b 0 ; c d 0 ; tx ty 1]
        double a = conc.matrix[0]*matrix[0] + conc.matrix[1]*matrix[2];
        double b = conc.matrix[0]*matrix[1] + conc.matrix[1]*matrix[3];
        double c = conc.matrix[2]*matrix[0] + conc.matrix[3]*matrix[2];
        double d = conc.matrix[2]*matrix[1] + conc.matrix[3]*matrix[3];
        double tx = conc.matrix[4]*matrix[0] + conc.matrix[5]*matrix[2] + matrix[4];
        double ty = conc.matrix[4]*matrix[1] + conc.matrix[5]*matrix[3] + matrix[5];
        matrix[0] = a;
        matrix[1] = b;
        matrix[2] = c;
        matrix[3] = d;
        matrix[4] = tx;
        matrix[5] = ty;
    }
    
    /**
     * Applies this transformation matrix to a point
     * @param x X-coordinate
     * @param y Y-coordinate
     * @return Transformed coordinate
     */
    public double[] apply(double x, double y) {
        // [a b c d tx ty]
        double[] converted = new double[2];
        converted[0] = matrix[0]*x + matrix[2]*y + matrix[4];
        converted[1] = matrix[1]*x + matrix[3]*y + matrix[5];
        return converted;
    }
    
    /**
     * Apples this transformation matrix to a point
     * @param coor Coordinate {x, y}
     * @return Transformed coordinate
     */
    public double[] apply(double[] coor) {
        return apply(coor[0], coor[1]);
    }
    
    /**
     * Applies inverse transformation to a point
     * @param x X-coordinate
     * @param y Y-coordinate
     * @return Inverse transformed coordinate
     */
    public double[] inverseApply(double x, double y) {
        double a = matrix[0];
        double b = matrix[1];
        double c = matrix[2];
        double d = matrix[3];
        double tx = matrix[4];
        double ty = matrix[5];
        
        double[] coor = new double[2];
        
        coor[0] = (d*x-c*y+c*ty-d*tx)/(-c*b+a*d);
        coor[1] = -(-a*y-b*tx+a*ty+b*x)/(-c*b+a*d);
        return coor;
    }
    
    /**
     * Applies inverse transformation to a point
     * @param coor Coordinate
     * @return Inverse transformed coordinate
     */
    public double[] inverseApply(double[] coor) {
        return inverseApply(coor[0], coor[1]);
    }
    
    /**
     * Applies inverse transformation to a translation (i.e. tx and ty are ignored)
     * @param x dx translation
     * @param y dy translation
     * @return Inverse transformed translation
     */
    public double[] inverseApplyShift(double x, double y) {
        double a = matrix[0];
        double b = matrix[1];
        double c = matrix[2];
        double d = matrix[3];
        
        double[] coor = new double[2];
        
        coor[0] = (d*x-c*y)/(-c*b+a*d);
        coor[1] = -(-a*y+b*x)/(-c*b+a*d);
        return coor;
    }
    
    /**
     * Applies inverse transformation to a translation (i.e. tx and ty are ignored)
     * @param coor Translation vector {dx, dy}
     * @return Inverse transformed translation
     */
    public double[] inverseApplyShift(double[] coor) {
        return inverseApplyShift(coor[0], coor[1]);
    }
    
    /**
     * Returns the x-scaling factor described by this matrix
     */
    public double getXScaling() {
        return Math.sqrt(Math.pow(matrix[0], 2) + Math.pow(matrix[2], 2));
    }
    
    /**
     * Returns the y-scaling factor described by this matrix
     */
    public double getYScaling() {
        return Math.sqrt(Math.pow(matrix[1], 2) + Math.pow(matrix[3], 2));
    }
    
    
    /**
     * Returns the mean scaling factor described by this matrix
     * @return Mean scaling factor (= mean(sqrt(a^2+c^2) + sqrt(b^2+d^2)) )
     */
    public double getMeanScaling() {
        return 0.5 * (getXScaling() + getYScaling());
    }
    
    /**
     * Determines the rotation for this transformation matrix
     * @return Rotation in degrees
     */
    public double getRotation() {
        return Math.atan2(matrix[1], matrix[0]) / Math.PI * 180;
    }
    
    /**
     * Convert this matrix to an array.
     * @return Array copy of this matrix.
     */
    public PSObjectArray toArray() {
        PSObject[] objs = new PSObject[matrix.length];
        for (int i = 0 ; i < objs.length ; i++) {
            objs[i] = new PSObjectReal(matrix[i]);
        }
        return new PSObjectArray(objs);
    }
    
    /**
     * Converts this object to a matrix. In this case it simply returns this.
     * @return This object itself
     */
    public PSObjectMatrix toMatrix() {
        return this;
    }

    /**
     * Returns an exact copy of this matrix.
     * @return Exact copy of this object.
     */
    public PSObjectMatrix clone() {
       return new PSObjectMatrix(this);
    }
    
    /**
     * Creates a human-readable string representation of this object.
     * @return String representation of this object.
     */
    public String isis() {
        StringBuilder str = new StringBuilder();
        str.append("[");
        for (int i = 0 ; i < matrix.length ; i++) {
            str.append(" " + matrix[i]);
        }
        str.append(" ]");
        return str.toString();
    }

    /**
     * Returns the type of this object
     * @return Type of this object (see PostScript manual for possible values)
     */
    public String type() {
        return "arraytype";
    }
}
