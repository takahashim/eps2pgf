/*
 * This file is part of Eps2pgf.
 *
 * Copyright 2007-2009 Paul Wagenaars
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

import net.sf.eps2pgf.ps.errors.PSErrorRangeCheck;
import net.sf.eps2pgf.ps.errors.PSErrorTypeCheck;
import net.sf.eps2pgf.ps.errors.PSErrorUndefinedResult;
import net.sf.eps2pgf.ps.errors.PSErrorVMError;
import net.sf.eps2pgf.ps.objects.PSObjectArray;

/**
 * Represent a PostScript transformation matrix. This is a six element array
 * with only numeric items.
 * 
 * @author Paul Wagenaars
 */
public class Matrix implements Cloneable {
    
    /** The matrix' values. */
    private double[] m = new double[6];
    
    /**
     * Create a new (unity) transformation matrix.
     */
    public Matrix() {
        m[0] = 1.0;
        m[1] = 0.0;
        m[2] = 0.0;
        m[3] = 1.0;
        m[4] = 0.0;
        m[5] = 0.0;
    }
    
    /**
     * Creates a new instance of Matrix. See PostScript manual
     * under "4.3 Coordinate Systems and Transformation" for more info.
     * 
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
    public Matrix(final double a, final double b, final double c,
            final double d, final double tx, final double ty) {
        
        m[0] = a;
        m[1] = b;
        m[2] = c;
        m[3] = d;
        m[4] = tx;
        m[5] = ty;
    }
    
    /**
     * Creates a new instance of Matrix. The new matrix is an exact copy
     * of 'refArray'. Note that the values are not shared.
     * 
     * @param refArray Array to copy.
     * 
     * @throws PSErrorRangeCheck A PostScript rangecheck error occurred.
     * @throws PSErrorTypeCheck A PostScript typecheck error occurred.
     */
    public Matrix(final PSObjectArray refArray) throws PSErrorRangeCheck,
            PSErrorTypeCheck {

        // Check the size
        if (refArray.size() != 6) {
            throw new PSErrorRangeCheck();
        }
        
        // Copy the values
        for (int i = 0; i < 6; i++) {
            m[i] = refArray.getReal(i);
        }
    }
    
    /**
     * Checks whether an array is a valid matrix. A PostScript error is thrown
     * when the array is not a valid matrix.
     * 
     * @param array The array.
     * 
     * @throws PSErrorRangeCheck A PostScript rangecheck error occurred.
     * @throws PSErrorTypeCheck A PostScript typecheck error occurred.
     */
    public static void checkArray(final PSObjectArray array)
            throws PSErrorRangeCheck, PSErrorTypeCheck {
        
        // Check the size
        if (array.size() != 6) {
            throw new PSErrorRangeCheck();
        }
        
        // Make sure that all values a real numbers.
        for (int i = 0; i < 6; i++) {
            array.getReal(i);
        }
    }
    
    /**
     * Creates a clone of this object.
     * 
     * @return The clone of this object.
     */
    @Override
    public Matrix clone() {
        Matrix copy;
        try {
            copy = (Matrix) super.clone();
            copy.m = (double[]) m.clone();
        } catch (CloneNotSupportedException e) {
            copy = new Matrix(m[0], m[1], m[2], m[3], m[4], m[5]);
        }
        
        return copy;
    }
    
    /**
     * Applies the transformation represented by conc to this matrix.
     * newMatrix = conc * matrix
     * 
     * @param conc Matrix describing the transformation.
     */
    public void concat(final Matrix conc) {
        // See section 4.3.3 of PostScript manual
        // newMatrix = concatMatrix x oldMatrix
        // [a  b ]   = [a  b  0]      [a  b ]
        // [c  d ]   = [c  d  0]   x  [c  d ]
        // [tx ty]   = [tx ty 1]      [tx ty]
        double a = conc.m[0] * m[0] + conc.m[1] * m[2];
        double b = conc.m[0] * m[1] + conc.m[1] * m[3];
        double c = conc.m[2] * m[0] + conc.m[3] * m[2];
        double d = conc.m[2] * m[1] + conc.m[3] * m[3];
        double tx = conc.m[4] * m[0] + conc.m[5] * m[2] + m[4];
        double ty = conc.m[4] * m[1] + conc.m[5] * m[3] + m[5];
        m[0] = a;
        m[1] = b;
        m[2] = c;
        m[3] = d;
        m[4] = tx;
        m[5] = ty;
    }
    
    /**
     * Copy values from another matrix to this matrix.
     * 
     * @param matrix The matrix from which the values are copied.
     */
    public void copy(final Matrix matrix) {
        for (int i = 0; i < 6; i++) {
            m[i] = matrix.m[i];
        }
    }
    
    /**
     * Applies this matrix to a *distance* vector.
     * 
     * @param x The x.
     * @param y The y.
     * 
     * @return the transformed vector
     */
    public double[] dtransform(final double x, final double y) {
        // [a b c d tx ty]
        double[] converted = new double[2];
        converted[0] = m[0] * x + m[2] * y;
        converted[1] = m[1] * x + m[3] * y;
        return converted;
    }
    
    /**
     * Return the value at a certain index.
     * 
     * @param index The index.
     * 
     * @return the double
     * 
     * @throws PSErrorRangeCheck A PostScript rangecheck error occurred.
     */
    public double get(final int index) throws PSErrorRangeCheck {
        if ((index < 0) || (index > 5)) {
            throw new PSErrorRangeCheck();
        }
        
        return m[index];
    }
    
    /**
     * Returns the mean scaling factor described by this matrix.
     * 
     * @return Mean scaling factor (= mean(sqrt(a^2+c^2) + sqrt(b^2+d^2)) )
     */
    public double getMeanScaling() {
        return 0.5 * (getXScaling() + getYScaling());
    }
    
    /**
     * Determines the rotation for this transformation matrix.
     * 
     * @return Rotation in degrees
     */
    public double getRotation() {
        return Math.atan2(m[1], m[0]) / Math.PI * 180;
    }
    
    /**
     * Returns the x-scaling factor described by this matrix.
     * 
     * @return the x scaling
     */
    public double getXScaling() {
        return Math.sqrt(Math.pow(m[0], 2) + Math.pow(m[2], 2));
    }
    
    /**
     * Returns the y-scaling factor described by this matrix.
     * 
     * @return the y scaling
     */
    public double getYScaling() {
        return Math.sqrt(Math.pow(m[1], 2) + Math.pow(m[3], 2));
    }
    
    /**
     * Applies inverse transformation to a translation (i.e. tx and ty are
     * ignored).
     * 
     * @param x dx translation
     * @param y dy translation
     * 
     * @return Inverse transformed translation
     */
    public double[] idtransform(final double x, final double y) {
        double a = m[0];
        double b = m[1];
        double c = m[2];
        double d = m[3];
        
        double[] coor = new double[2];
        coor[0] = (d * x - c * y) / (-c * b + a * d);
        coor[1] = -(-a * y + b * x) / (-c * b + a * d);
        
        return coor;
    }
    
    /**
     * Applies inverse transformation to a translation (i.e. tx and ty are
     * ignored).
     * 
     * @param coor Translation vector {dx, dy}
     * 
     * @return Inverse transformed translation
     */
    public double[] idtransform(final double[] coor) {
        return idtransform(coor[0], coor[1]);
    }
    
    /**
     * Calculate the inverse of this matrix. The result replaces the current
     * values in this matrix.
     * 
     * @throws PSErrorUndefinedResult the PS error undefined result
     */
    public void invert() throws PSErrorUndefinedResult {
        double a = m[0];
        double b = m[1];
        double c = m[2];
        double d = m[3];
        double tx = m[4];
        double ty = m[5];
        
        double cmn = 1 / (a * d - c * b);
        if (Double.isInfinite(cmn) || Double.isNaN(cmn)) {
            throw new PSErrorUndefinedResult();
        }
        
        m[0] = d * cmn;
        m[1] = -b * cmn;
        m[2] = -c * cmn;
        m[3] = a * cmn;
        m[4] = (c * ty - d * tx) * cmn;
        m[5] = -(a * ty - b * tx) * cmn;
    }
    
    /**
     * Applies inverse transformation to a point.
     * 
     * @param x X-coordinate
     * @param y Y-coordinate
     * 
     * @return Inverse transformed coordinate
     */
    public double[] itransform(final double x, final double y) {
        double a = m[0];
        double b = m[1];
        double c = m[2];
        double d = m[3];
        double tx = m[4];
        double ty = m[5];
        
        double[] coor = new double[2];
        coor[0] = (d * x - c * y + c * ty - d * tx) / (-c * b + a * d);
        coor[1] = -(-a * y - b * tx + a * ty + b * x) / (-c * b + a * d);
        
        return coor;
    }
    
    /**
     * Applies inverse transformation to a point.
     * 
     * @param coor Coordinate
     * 
     * @return Inverse transformed coordinate
     */
    public double[] itransform(final double[] coor) {
        return itransform(coor[0], coor[1]);
    }
    
    /**
     * Rotates the matrix (current transformation matrix).
     * 
     * [cos(a)  sin(a) 0]
     * Transformation matrix = [-sin(a) cos(a) 0]
     * [  0      0     1]
     * 
     * @param angle Angle in degrees for counterclockwise rotation.
     */
    public void rotate(final double angle) {
        // [a b c d xx yy]
        double cosa = Math.cos(angle * Math.PI / 180);
        double sina = Math.sin(angle * Math.PI / 180);
        double a = cosa * m[0] + sina * m[2];
        double b = cosa * m[1] + sina * m[3];
        double c = -sina * m[0] + cosa * m[2];
        double d = -sina * m[1] + cosa * m[3];
        m[0] = a;
        m[1] = b;
        m[2] = c;
        m[3] = d;
    }
    
    /**
     * Creates a scaled copy of this matrix.
     * [sx 0  0]
     * Transformation matrix: [0  sy 0]
     * [0  0  1]
     * 
     * @param sx X-coodinate scaling factor.
     * @param sy Y-coordinate scaling factor.
     */
    public void scale(final double sx, final double sy) {
        // [a b c d xx yy]
        m[0] = sx * m[0];
        m[1] = sx * m[1];
        m[2] = sy * m[2];
        m[3] = sy * m[3];
    }
    
    /**
     * Convert this matrix to PostScript array.
     * 
     * @param vm The virtual memory manager that will manage the new array.
     * 
     * @return the PS object array
     * 
     * @throws PSErrorVMError Virtual memory error.
     */
    public PSObjectArray toArray(final VM vm) throws PSErrorVMError {
        return new PSObjectArray(m, vm);
    }
    
    /**
     * Apples this transformation matrix to a point.
     * 
     * @param coor Coordinate {x, y}
     * 
     * @return Transformed coordinate
     */
    public double[] transform(final double[] coor) {
        return transform(coor[0], coor[1]);
    }
    
    /**
     * Applies this transformation matrix to a point.
     * 
     * @param x X-coordinate
     * @param y Y-coordinate
     * 
     * @return Transformed coordinate
     */
    public double[] transform(final double x, final double y) {
        // [a b c d tx ty]
        double[] converted = new double[2];
        converted[0] = m[0] * x + m[2] * y + m[4];
        converted[1] = m[1] * x + m[3] * y + m[5];
        
        return converted;
    }
    
    /**
     * Translates the matrix.
     * [1  0  0]
     * Transformation matrix = [0  1  0]
     * [sx sy 1]
     * 
     * @param tx X-coordinate translation
     * @param ty Y-coordinate translation
     */
    public void translate(final double tx, final double ty) {
        // [a b c d xx yy]
        m[4] = tx * m[0] + ty * m[2] + m[4];
        m[5] = tx * m[1] + ty * m[3] + m[5];
    }
}
