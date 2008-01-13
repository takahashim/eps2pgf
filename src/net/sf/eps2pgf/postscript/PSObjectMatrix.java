/*
 * PSObjectMatrix.java
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

package net.sf.eps2pgf.postscript;

import net.sf.eps2pgf.postscript.errors.PSErrorRangeCheck;
import net.sf.eps2pgf.postscript.errors.PSErrorTypeCheck;
import net.sf.eps2pgf.postscript.errors.PSErrorUndefinedResult;

/**
 * Represent a PostScript matrix. This is a six element array with only numeric
 * items.
 * @author Paul Wagenaars
 */
public class PSObjectMatrix extends PSObjectArray {
    /**
     * Creates a new instance of PSObjectMatrix. The new object is filled with
     * an identity matrix.
     */
    public PSObjectMatrix() {
        this(1, 0, 0, 1, 0, 0);
    }
    
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
    public PSObjectMatrix(final double a, final double b, final double c,
            final double d, final double tx, final double ty) {
        addAt(0, new PSObjectReal(a));
        addAt(1, new PSObjectReal(b));
        addAt(2, new PSObjectReal(c));
        addAt(3, new PSObjectReal(d));
        addAt(4, new PSObjectReal(tx));
        addAt(5, new PSObjectReal(ty));
    }
    
    /**
     * Creates a new instance of PSObjectMatrix. The new matrix is an exact copy
     * of 'refArray'.
     * 
     * @param refArray Array to copy.
     * 
     * @throws PSErrorRangeCheck A PostScript rangecheck error occurred.
     * @throws PSErrorTypeCheck A PostScript typecheck error occurred.
     */
    public PSObjectMatrix(final PSObjectArray refArray)
            throws PSErrorRangeCheck, PSErrorTypeCheck {
        
        checkMatrix(refArray);

        setArray(refArray.getArray());
        setOffset(refArray.getOffset());
        setCount(refArray.getCount());
        copyCommonAttributes(refArray);
    }
    
    /**
     * Check whether this could be a valid matrix.
     * 
     * @param arrayToCheck Array that is checked
     * 
     * @throws PSErrorRangeCheck A PostScript rangecheck error occurred.
     */
    public static void checkMatrix(final PSObjectArray arrayToCheck)
            throws PSErrorRangeCheck {
        
        if (arrayToCheck.size() != 6) {
            throw new PSErrorRangeCheck();
        }
    }
    
    /**
     * Creates a deep copy of this object.
     * 
     * @return Deep copy of this object.
     */
    @Override
    public PSObjectMatrix clone() {
        PSObjectMatrix copy = (PSObjectMatrix) super.clone();
        return copy;
    }

    /**
     * Applies the transformation represented by conc to this matrix.
     * newMatrix = conc * matrix
     * 
     * @param conc Matrix describing the transformation.
     * 
     * @throws PSErrorRangeCheck A PostScript rangecheck error occurred.
     * @throws PSErrorTypeCheck A PostScript typecheck error occurred.
     */
    public void concat(final PSObjectMatrix conc)
            throws PSErrorRangeCheck, PSErrorTypeCheck {
        // [a b c d tx ty] [a b 0 ; c d 0 ; tx ty 1]
        double a = conc.getReal(0) * getReal(0) + conc.getReal(1) * getReal(2);
        double b = conc.getReal(0) * getReal(1) + conc.getReal(1) * getReal(3);
        double c = conc.getReal(2) * getReal(0) + conc.getReal(3) * getReal(2);
        double d = conc.getReal(2) * getReal(1) + conc.getReal(3) * getReal(3);
        double tx = conc.getReal(4) * getReal(0) + conc.getReal(5) * getReal(2)
                + getReal(4);
        double ty = conc.getReal(4) * getReal(1) + conc.getReal(5) * getReal(3)
                + getReal(5);
        setReal(0, a);
        setReal(1, b);
        setReal(2, c);
        setReal(3, d);
        setReal(4, tx);
        setReal(5, ty);
    }
    
    /**
     * Applies this matrix to a *distance* vector.
     * 
     * @param x The x.
     * @param y The y.
     * 
     * @return the transformed vector
     * 
     * @throws PSErrorRangeCheck A PostScript rangecheck error occurred.
     * @throws PSErrorTypeCheck A PostScript typecheck error occurred.
     */
    public double[] dtransform(final double x, final double y)
            throws PSErrorRangeCheck, PSErrorTypeCheck {
        // [a b c d tx ty]
        double[] converted = new double[2];
        converted[0] = getReal(0) * x + getReal(2) * y;
        converted[1] = getReal(1) * x + getReal(3) * y;
        return converted;
    }
    
    /**
     * PostScript operator 'dup'. Create a (shallow) copy of this object. The
     * values of composite object is not copied, but shared.
     * 
     * @return Duplicate of this object.
     */
    @Override
    public PSObjectMatrix dup() {
        try {
            return new PSObjectMatrix(this);
        } catch (PSErrorRangeCheck e) {
            // this can never happen
            return null;
        } catch (PSErrorTypeCheck e) {
            // this can never happen
            return null;
        }
    }
    
    /**
     * Returns the mean scaling factor described by this matrix.
     * 
     * @return Mean scaling factor (= mean(sqrt(a^2+c^2) + sqrt(b^2+d^2)) )
     * 
     * @throws PSErrorRangeCheck A PostScript rangecheck error occurred.
     * @throws PSErrorTypeCheck A PostScript typecheck error occurred.
     */
    public double getMeanScaling() throws PSErrorRangeCheck,
            PSErrorTypeCheck {
        
        return 0.5 * (getXScaling() + getYScaling());
    }
    
    /**
     * Determines the rotation for this transformation matrix.
     * 
     * @return Rotation in degrees
     * 
     * @throws PSErrorRangeCheck A PostScript rangecheck error occurred.
     * @throws PSErrorTypeCheck A PostScript typecheck error occurred.
     */
    public double getRotation() throws PSErrorRangeCheck,
            PSErrorTypeCheck {
        return Math.atan2(getReal(1), getReal(0)) / Math.PI * 180;
    }
    
    /**
     * Returns the x-scaling factor described by this matrix.
     * 
     * @return the x scaling
     * 
     * @throws PSErrorRangeCheck A PostScript rangecheck error occurred.
     * @throws PSErrorTypeCheck A PostScript typecheck error occurred.
     */
    public double getXScaling() throws PSErrorRangeCheck, PSErrorTypeCheck {
        return Math.sqrt(Math.pow(getReal(0), 2) + Math.pow(getReal(2), 2));
    }
    
    /**
     * Returns the y-scaling factor described by this matrix.
     * 
     * @return the y scaling
     * 
     * @throws PSErrorRangeCheck A PostScript rangecheck error occurred.
     * @throws PSErrorTypeCheck A PostScript typecheck error occurred.
     */
    public double getYScaling() throws PSErrorRangeCheck, PSErrorTypeCheck {
        return Math.sqrt(Math.pow(getReal(1), 2) + Math.pow(getReal(3), 2));
    }
    
    /**
     * Applies inverse transformation to a translation (i.e. tx and ty are
     * ignored).
     * 
     * @param x dx translation
     * @param y dy translation
     * 
     * @return Inverse transformed translation
     * 
     * @throws PSErrorRangeCheck A PostScript rangecheck error occurred.
     * @throws PSErrorTypeCheck A PostScript typecheck error occurred.
     */
    public double[] idtransform(final double x, final double y)
            throws PSErrorRangeCheck, PSErrorTypeCheck {
        double a = getReal(0);
        double b = getReal(1);
        double c = getReal(2);
        double d = getReal(3);
        
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
     * 
     * @throws PSErrorRangeCheck A PostScript rangecheck error occurred.
     * @throws PSErrorTypeCheck A PostScript typecheck error occurred.
     */
    public double[] idtransform(final double[] coor)
            throws PSErrorRangeCheck, PSErrorTypeCheck {
        
        return idtransform(coor[0], coor[1]);
    }
    
    /**
     * Calculate the inverse of this matrix. The result replaces the current
     * values in this matrix.
     * 
     * @throws PSErrorUndefinedResult the PS error undefined result
     * @throws PSErrorRangeCheck A PostScript rangecheck error occurred.
     * @throws PSErrorTypeCheck A PostScript typecheck error occurred.
     */
    public void invert() throws PSErrorUndefinedResult, PSErrorRangeCheck,
            PSErrorTypeCheck {
        
        double a = getReal(0);
        double b = getReal(1);
        double c = getReal(2);
        double d = getReal(3);
        double tx = getReal(4);
        double ty = getReal(5);
        
        double cmn = 1 / (a * d - c * b);
        if (Double.isInfinite(cmn) || Double.isNaN(cmn)) {
            throw new PSErrorUndefinedResult();
        }
        
        setReal(0, d * cmn);
        setReal(1, -b * cmn);
        setReal(2, -c * cmn);
        setReal(3, a * cmn);
        setReal(4, (c * ty - d * tx) * cmn);
        setReal(5, -(a * ty - b * tx) * cmn);
    }
    
    /**
     * Applies inverse transformation to a point.
     * 
     * @param x X-coordinate
     * @param y Y-coordinate
     * 
     * @return Inverse transformed coordinate
     * 
     * @throws PSErrorRangeCheck A PostScript rangecheck error occurred.
     * @throws PSErrorTypeCheck A PostScript typecheck error occurred.
     */
    public double[] itransform(final double x, final double y)
            throws PSErrorRangeCheck, PSErrorTypeCheck {
        double a = getReal(0);
        double b = getReal(1);
        double c = getReal(2);
        double d = getReal(3);
        double tx = getReal(4);
        double ty = getReal(5);
        
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
     * 
     * @throws PSErrorRangeCheck A PostScript rangecheck error occurred.
     * @throws PSErrorTypeCheck A PostScript typecheck error occurred.
     */
    public double[] itransform(final double[] coor) throws PSErrorRangeCheck,
            PSErrorTypeCheck {
        
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
     * 
     * @throws PSErrorRangeCheck A PostScript rangecheck error occurred.
     * @throws PSErrorTypeCheck A PostScript typecheck error occurred.
     */
    public void rotate(final double angle) throws PSErrorRangeCheck,
            PSErrorTypeCheck {
        
        // [a b c d xx yy]
        double cosa = Math.cos(angle * Math.PI / 180);
        double sina = Math.sin(angle * Math.PI / 180);
        double a = cosa * getReal(0) + sina * getReal(2);
        double b = cosa * getReal(1) + sina * getReal(3);
        double c = -sina * getReal(0) + cosa * getReal(2);
        double d = -sina * getReal(1) + cosa * getReal(3);
        setReal(0, a);
        setReal(1, b);
        setReal(2, c);
        setReal(3, d);
    }
    
    /**
     * Creates a scaled copy of this matrix.
     * [sx 0  0]
     * Transformation matrix: [0  sy 0]
     * [0  0  1]
     * 
     * @param sx X-coodinate scaling factor.
     * @param sy Y-coordinate scaling factor.
     * 
     * @throws PSErrorRangeCheck A PostScript rangecheck error occurred.
     * @throws PSErrorTypeCheck A PostScript typecheck error occurred.
     */
    public void scale(final double sx, final double sy)
            throws PSErrorRangeCheck, PSErrorTypeCheck {
        
        // [a b c d xx yy]
        setReal(0, sx * getReal(0));
        setReal(1, sx * getReal(1));
        setReal(2, sy * getReal(2));
        setReal(3, sy * getReal(3));
    }
    
    /**
     * Converts this object to a matrix. In this case it simply returns this.
     * @return This object itself
     */
    @Override
    public PSObjectMatrix toMatrix() {
        return this;
    }

    /**
     * Apples this transformation matrix to a point.
     * 
     * @param coor Coordinate {x, y}
     * 
     * @return Transformed coordinate
     * 
     * @throws PSErrorRangeCheck A PostScript rangecheck error occurred.
     * @throws PSErrorTypeCheck A PostScript typecheck error occurred.
     */
    public double[] transform(final double[] coor)
            throws PSErrorRangeCheck, PSErrorTypeCheck {
        
        return transform(coor[0], coor[1]);
    }
    
    /**
     * Applies this transformation matrix to a point.
     * 
     * @param x X-coordinate
     * @param y Y-coordinate
     * 
     * @return Transformed coordinate
     * 
     * @throws PSErrorRangeCheck A PostScript rangecheck error occurred.
     * @throws PSErrorTypeCheck A PostScript typecheck error occurred.
     */
    public double[] transform(final double x, final double y)
            throws PSErrorRangeCheck, PSErrorTypeCheck {
        
        // [a b c d tx ty]
        double[] converted = new double[2];
        converted[0] = getReal(0) * x + getReal(2) * y + getReal(4);
        converted[1] = getReal(1) * x + getReal(3) * y + getReal(5);
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
     * 
     * @throws PSErrorRangeCheck A PostScript rangecheck error occurred.
     * @throws PSErrorTypeCheck A PostScript typecheck error occurred.
     */
    public void translate(final double tx, final double ty)
            throws PSErrorRangeCheck, PSErrorTypeCheck {
        
        // [a b c d xx yy]
        setReal(4, tx * getReal(0) + ty * getReal(2) + getReal(4));
        setReal(5, tx * getReal(1) + ty * getReal(3) + getReal(5));
    }

}
