/*
 * GraphicsState.java
 *
 * This file is part of Eps2pgf.
 *
 * Copyright 2007-2008 Paul Wagenaars <paul@wagenaars.org>
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

import java.io.IOException;

import net.sf.eps2pgf.ProgramError;
import net.sf.eps2pgf.ps.errors.PSError;
import net.sf.eps2pgf.ps.errors.PSErrorNoCurrentPoint;
import net.sf.eps2pgf.ps.errors.PSErrorRangeCheck;
import net.sf.eps2pgf.ps.errors.PSErrorTypeCheck;
import net.sf.eps2pgf.ps.objects.PSObject;
import net.sf.eps2pgf.ps.objects.PSObjectArray;
import net.sf.eps2pgf.ps.objects.PSObjectDict;
import net.sf.eps2pgf.ps.objects.PSObjectFont;
import net.sf.eps2pgf.ps.objects.PSObjectMatrix;
import net.sf.eps2pgf.ps.resources.colors.ColorManager;
import net.sf.eps2pgf.ps.resources.colors.PSColor;
import net.sf.eps2pgf.ps.resources.outputdevices.OutputDevice;

/**
 * Structure that holds the graphics state (graphic control parameter).
 * See PostScript manual table 4.1, p. 179 for more info.
 *
 * @author Paul Wagenaars
 */
public class GraphicsState implements Cloneable {
    
    /**
     * Current Transformation Matrix (CTM). All coordinates will be transformed
     * by this matrix. The default CTM converts PostScript pt used in PostScript
     * files to micrometers.
     * [a b c d tx ty] -> x' = a*x + b*y + tx ; y' = c*x + d*y * ty
     */
    private PSObjectMatrix ctm = new PSObjectMatrix();
    
    /** Current position in pt (before CTM is applied). */
    private double[] position = new double[2];
    
    /** Current path. */
    private Path path;
    
    /** Current clipping path. */
    private Path clippingPath;
    
    /** Current color. */
    private PSColor color;
    
    /** Current font. */
    private PSObjectFont font;
    
    /** Current line width (in user space coordinates). */
    private double lineWidth = 1.0;
    
    /** Current line cap. */
    private int lineCap = 0;
    
    /** Current line join. */
    private int lineJoin = 0;
    
    /** Current liter limit. */
    private double miterLimit = 10.0;
    
    /** Current dash pattern. */
    private PSObjectArray dashPattern = new PSObjectArray();
    
    /** Current dash offset. */
    private double dashOffset = 0.0;
    
    /** Current stroke adjustment. */
    private boolean strokeAdjust = true;
    
    //
    // Device dependent parameters
    //
    /** Current color rendering. */
    private PSObjectDict colorRendering = new PSObjectDict();
    
    /** Current overprint. */
    private boolean overprint = false;
    
    /** Current black generation function. */
    private PSObjectArray blackGeneration;
    
    /** Current undercolor removal function. */
    private PSObjectArray undercolorRemoval;
    
    /**
     * Current transfer functions to convert colors to device settings.
     * The array contains four procedures:
     * {redproc greenproc blueproc grayproc}
     */
    private PSObjectArray transfer;
    
    /** Current halftone. */
    private PSObject halftone = new PSObjectDict();
    
    /** Current flatness of curves. */
    private double flatness = 1.0;
    
    /** Current smoothness. */
    private double smoothness = 1e-3;
    
    /** Reference to current output device. */
    private OutputDevice device;
    
    /**
     * Creates a new default graphics state.
     * 
     * @param parentGraphicsStack Pointer the graphics stack of which this
     * object is part of.
     * @param wDevice Output device.
     * 
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    public GraphicsState(final GstateStack parentGraphicsStack,
            final OutputDevice wDevice) throws ProgramError {

        device = wDevice;
                
        initmatrix();
        path = new Path(parentGraphicsStack);
        clippingPath = new Path(parentGraphicsStack);
        font = new PSObjectFont();
        
        try {
            blackGeneration = new PSObjectArray("{}");
            undercolorRemoval = new PSObjectArray("{}");
            transfer = new PSObjectArray("[{} {} {} {}]");
        } catch (PSError e) {
            // this can never happen
        }
    }
    
    /**
     * Adds an arc to the end of the current path.
     * 
     * @param x X-coordinate of center of circle in user space coordinates
     * @param y Y-coordinate of center of circle in user space coordinates
     * @param r Radius of circle
     * @param pAngle1 Angle in degrees of starting point
     * @param pAngle2 Angle in degrees of end point
     * @param isPositive Indicates whether arc in positive.
     * 
     * @throws PSErrorRangeCheck A value is out of range
     * @throws PSErrorTypeCheck An object has an incorrect type
     */
    public void arc(final double x, final double y, final double r,
            final double pAngle1, final double pAngle2,
            final boolean isPositive)
            throws PSErrorRangeCheck, PSErrorTypeCheck {
        double angle1 = pAngle1;
        double angle2 = pAngle2;
        double mult = 1;
        if (!isPositive) {
            mult = -1;
        }
        
        while (mult * angle2 < mult * angle1) {
            angle2 += mult * 360;
        }
        
        // convert angles from degrees to radians
        angle1 = Math.toRadians(angle1);
        angle2 = Math.toRadians(angle2);
        
        // move to start, if necessary
        double x0 = x + r * Math.cos(angle1);
        double y0 = y + r * Math.sin(angle1);
        if ((Math.abs(this.position[0] - x0) > 1e-6)
                || (Math.abs(this.position[1] - y0) > 1e-6)) {
            lineto(x0, y0);
        } else if (Double.isNaN(this.position[0])
                || Double.isNaN(this.position[1])) {
            moveto(x0, y0);
        }
        
        // take step from angle1 to multiples of 90 degrees
        double ang1;
        double ang2 = angle1;
        double angleStep = mult * Math.PI / 2;
        for (double ang = angle1; mult * ang2 < mult * angle2;
                ang += angleStep) {
            ang1 = ang2;
            ang2 = angleStep * Math.floor((ang + angleStep) / angleStep);
            if (mult * angle2 < mult * ang2) {
                ang2 = angle2;
            }

            double mang = (ang1 + ang2) / 2;
            
            double unitX0 = Math.cos(mult * (ang2 - ang1) / 2);
            double unitY0 = Math.sin(mult * (ang2 - ang1) / 2);
            
            double xControl = (4 - unitX0) / 3;
            double yControl = (1 - unitX0) * (3 - unitX0) / (3 * unitY0);
            double angControl = Math.atan2(yControl, xControl);
            double rControl = Math.sqrt(xControl * xControl
                    + yControl * yControl);
            
            double x1 = x + r * rControl * Math.cos(mang - mult * angControl);
            double y1 = y + r * rControl * Math.sin(mang - mult * angControl);
            
            double x2 = x + r * rControl * Math.cos(mang + mult * angControl);
            double y2 = y + r * rControl * Math.sin(mang + mult * angControl);
            
            double x3 = x + r * Math.cos(ang2);
            double y3 = y + r * Math.sin(ang2);
            
            curveto(x1, y1, x2, y2, x3, y3);
        }
    }
    
    /**
     * Implements the 'arcto' PostScript operator.
     * 
     * @param x1 First x-coordinate
     * @param y1 First y-coordinate
     * @param x2 Second x-coordinate
     * @param y2 Second y-coordinate
     * @param r Radius
     * 
     * @return Coordinates of the two tangent points
     * 
     * @throws PSError A PostScript error occurred.
     */
    public double[] arcto(final double x1, final double y1, final double x2,
            final double y2, final double r) throws PSError {
        double x0 = this.position[0];
        double y0 = this.position[1];
        
        if (Double.isNaN(x0) || Double.isNaN(y0)) {
            throw new PSErrorNoCurrentPoint();
        }
        
        // Calculate some angles
        double phi1 = Math.atan2(y0 - y1, x0 - x1);
        double phi2 = Math.atan2(y2 - y1, x2 - x1);
        double alpha = phi2 - phi1;
        if (alpha > Math.PI) {
            alpha = alpha - 2 * Math.PI;
        } else if (alpha < -Math.PI) {
            alpha = alpha + 2 * Math.PI;
        }
        
        // Check if angle is -180, 0 or 180 degrees
        if ((Math.abs(Math.abs(alpha) - Math.PI) < 1e-3)
                || (Math.abs(alpha) < 1e-3)) {
            lineto(x1, y1);
            double[] ret = {x1, y1, x1, y1};
            return ret;
        }
        
        // Calculate lines parallel to the lines (x0,y0)-(x1,y1) and
        // (x1,y1)-(x2,y2).
        double posorneg;
        if (alpha >= 0) {
            posorneg = 1.0;
        } else {
            posorneg = -1.0;
        }
        double p1x0 = x0 + r * Math.cos(phi1 + posorneg * Math.PI / 2);
        double p1y0 = y0 + r * Math.sin(phi1 + posorneg * Math.PI / 2);
        double p1x1 = x1 + r * Math.cos(phi1 + posorneg * Math.PI / 2);
        double p1y1 = y1 + r * Math.sin(phi1 + posorneg * Math.PI / 2);
        
        double p2x1 = x1 + r * Math.cos(phi2 - posorneg * Math.PI / 2);
        double p2y1 = y1 + r * Math.sin(phi2 - posorneg * Math.PI / 2);
        double p2x2 = x2 + r * Math.cos(phi2 - posorneg * Math.PI / 2);
        double p2y2 = y2 + r * Math.sin(phi2 - posorneg * Math.PI / 2);
        
        // Calculate intersection, this is the center of the circle
        // Weisstein, Eric W. "Line-Line Intersection." From MathWorld--A
        // Wolfram Web Resource.
        // http://mathworld.wolfram.com/Line-LineIntersection.html 
        double num = det(det(p1x0, p1y0, p1x1, p1y1), p1x0 - p1x1,
                det(p2x1, p2y1, p2x2, p2y2), p2x1 - p2x2);
        double den = det(p1x0 - p1x1, p1y0 - p1y1, p2x1 - p2x2, p2y1 - p2y2);
        double x = num / den;
        num = det(det(p1x0, p1y0, p1x1, p1y1), p1y0 - p1y1,
                det(p2x1, p2y1, p2x2, p2y2), p2y1 - p2y2);
        double y = num / den;

        double ang1 = Math.PI + phi1 + posorneg * Math.PI / 2;
        double ang2 = Math.PI + phi2 - posorneg * Math.PI / 2;

        double xt1 = x + r * Math.cos(ang1);
        double yt1 = y + r * Math.sin(ang1);
        double xt2 = x + r * Math.cos(ang2);
        double yt2 = y + r * Math.sin(ang2);

        arc(x, y, r, ang1, ang2, (ang2 > ang1));
        
        double[] ret = {xt1, yt1, xt2, yt2};
        return ret;
    }
    
    /**
     * Intersects the area inside the current clipping path with the area
     * inside the current path.
     */
    public void clip() {
        this.clippingPath = this.path.clone();
    }
    
    /**
     * Creates a deep copy of this object.
     * @throws CloneNotSupportedException Indicates that clone is not (fully)
     *         supported. This should not happen.
     * @return Returns the deep copy.
     */
    @Override
    public GraphicsState clone() throws CloneNotSupportedException {
        
        GraphicsState copy = (GraphicsState) super.clone();
        copy.clippingPath = clippingPath.clone();
        copy.color = color.clone();
        copy.ctm = ctm.clone();
        // dashoffset is primitive, it doesn't need to be cloned explicitly.
        copy.dashPattern = dashPattern.clone();
        // flat is primitive, it doesn't need to be cloned explicitly.
        copy.font = font.clone();
        // linewidth is primitive, it doesn't need to be cloned explicitly.
        // linecap is primitive, it doesn't need to be cloned explicitly.
        copy.path = path.clone();
        copy.position = position.clone();
        // strokeAdjust is primitive, it doesn't need to be cloned explicitly.
        
        copy.colorRendering = colorRendering.clone();
        // overprint is primitive, it doesn't need to be cloned explicitely.
        copy.blackGeneration = blackGeneration.clone();
        copy.undercolorRemoval = undercolorRemoval.clone();
        copy.transfer = transfer.clone();
        copy.halftone = halftone.clone();
        // flatness is primitive, it doesn't need to be cloned explicitely.
        // smoothness is primitive, it doesn't need to be cloned explicitely.
        copy.device = device.clone();
        
        return copy;
    }
    
    /**
     * Gets the current color rendering.
     * 
     * @return The current color rendering.
     */
    public PSObjectDict currentColorRendering() {
        return colorRendering;
    }
    
    /**
     * Gets the current overprint.
     * 
     * @return The current overprint.
     */
    public boolean currentOverprint() {
        return overprint;
    }
    
    /**
     * Gets the current black generation.
     * 
     * @return The current black generation.
     */
    public PSObjectArray currentBlackGeneration() {
        return blackGeneration;
    }
    
    /**
     * Gets the current color transfer.
     * 
     * @return Array with color transfer functions:
     * <code>{redproc greenproc blueproc grayproc}</code>.
     */
    public PSObjectArray currentColorTransfer() {
        return transfer;
    }
    
    /**
     * Gets the current undercolor removal.
     * 
     * @return The current undercolor removal.
     */
    public PSObjectArray currentUndercolorRemoval() {
        return undercolorRemoval;
    }
    
    /**
     * Gets the current gray transfer.
     * 
     * @return The current gray transfer.
     * 
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    public PSObjectArray currentTransfer() throws ProgramError {
        try {
            return transfer.get(3).toProc();
        } catch (PSError e) {
            throw new ProgramError("PSError in currentTransfer()");
        }
    }
    
    /**
     * Gets the current halftone.
     * 
     * @return The current halftone.
     */
    public PSObject currentHalftone() {
        return halftone;
    }
    
    /**
     * Gets the current flatness.
     * 
     * @return The current flatness.
     */
    public double currentFlatness() {
        return flatness;
    }
    
    /**
     * Gets the current smoothness.
     * 
     * @return The current smoothness.
     */
    public double currentSmoothness() {
        return smoothness;
    }
    
    /**
     * Returns the current color in the CMYK color space.
     * 
     * @return Color in CMYK.
     * 
     * @throws PSError A PostScript error occurred.
     * @throws ProgramError A program error occurred.
     */
    public double[] currentcmykcolor() throws PSError, ProgramError {
        return this.color.getCMYK();
    }

    /**
     * Returns the current color in gray scale.
     * 
     * @return Color in gray
     * 
     * @throws PSError A PostScript error occurred.
     * @throws ProgramError Program can not continue to run.
     */
    public double currentgray() throws PSError, ProgramError {
        return this.color.getGray();
    }
    
    /**
     * Returns the current color in the HSB (also HSV) color space.
     * 
     * @return Color in HSB.
     * 
     * @throws PSError A PostScript error occurred.
     * @throws ProgramError Program can not continue to run.
     */
    public double[] currenthsbcolor() throws PSError, ProgramError {
        return this.color.getHSB();
    }

    /**
     * Returns the current color in the RGB color space.
     * 
     * @return Color in RGB.
     * 
     * @throws PSError A PostScript error occurred.
     * @throws ProgramError A fatal program error occurred.
     */
    public double[] currentrgbcolor() throws PSError, ProgramError {
        return this.color.getRGB();
    }

    /**
     * Add a curveto section to the current path.
     * 
     * @param x1 X-coordinate of first control point
     * @param y1 Y-coordinate of first control point
     * @param x2 X-coordinate of second control point
     * @param y2 Y-coordinate of second control point
     * @param x3 X-coordinate of end point
     * @param y3 Y-coordinate of end point
     * 
     * @throws PSErrorRangeCheck A PostScript rangecheck error occurred.
     * @throws PSErrorTypeCheck A PostScript typecheck error occurred.
     */
    public void curveto(final double x1, final double y1, final double x2,
            final double y2, final double x3, final double y3)
            throws PSErrorRangeCheck, PSErrorTypeCheck {
        this.position[0] = x3;
        this.position[1] = y3;
        double[] coor1 = this.ctm.transform(x1, y1);
        double[] coor2 = this.ctm.transform(x2, y2);
        double[] coor3 = this.ctm.transform(x3, y3);
        this.path.curveto(coor1, coor2, coor3);
    }
    
    /**
     * Calculate determinant of the matrix.
     * [a b]
     * [c d]
     * 
     * @param a the a
     * @param b the b
     * @param c the c
     * @param d the d
     * 
     * @return Determinant of the matrix.
     */
    double det(final double a, final double b, final double c, final double d) {
        return a * d - b * c;
    }
    
    /**
     * Replace the current path by a flattened version of the path.
     * 
     * @throws PSError A PostScript error occurred
     * @throws ProgramError This should never happen. If it is thrown it
     *         indicates a bug in Eps2pgf.
     */
    public void flattenpath() throws PSError, ProgramError {
        // Maximum difference between normal and flattened path. Defined in
        // device space coordinates. Assume a device resolution of 1200 dpi.
        double deviceScale = this.device.defaultCTM().getMeanScaling();
        double maxError = this.flatness * 72.0 / 1200.0 * deviceScale;

        this.path = this.path.flattenpath(maxError);
    }
    
    /**
     * Retrieves the current position in device space.
     * 
     * @return X- and Y-coordinate in device space (micrometers)
     * 
     * @throws PSErrorNoCurrentPoint There is no current point.
     */
    public double[] getCurrentPosInDeviceSpace() throws PSErrorNoCurrentPoint {
        if (this.path.getSections().size() == 0) {
            throw new PSErrorNoCurrentPoint();
        }
        return this.path.getSections().get(this.path.getSections().size() - 1)
                .deviceCoor();
    }
    
    /**
     * Gets the mean scaling relative to the default CTM. This is a combination
     * of all transformations applied by the PostScript program.
     * 
     * @return The mean scaling applied by the user.
     */
    public double getMeanUserScaling() {
        double scaling = Double.NaN;
        try {
            scaling = this.ctm.getMeanScaling()
                    / this.device.defaultCTM().getMeanScaling();
        } catch (PSErrorRangeCheck e) {
            // this can never happen, since none of the matrices above are
            // controlled by the is user.
        } catch (PSErrorTypeCheck e) {
            // this can never happen, since none of the matrices above are
            // controlled by the is user.
        }
        return scaling;
    }
    
    /**
     * Sets the current transformation matrix (CTM) to its default value.
     */
    public void initmatrix() {
        this.ctm = this.device.defaultCTM();
    }
    
    /**
     * Draw a line to a point.
     * 
     * @param x X-coordinate (before CTM is applied)
     * @param y Y-coordinate (before CTM is applied)
     * 
     * @throws PSErrorRangeCheck The PostScript rangecheck error occurred.
     * @throws PSErrorTypeCheck The PostScript typecheck error occurred.
     */
    public void lineto(final double x, final double y)
            throws PSErrorRangeCheck, PSErrorTypeCheck {
        this.position[0] = x;
        this.position[1] = y;
        double[] transformed = this.ctm.transform(x, y);
        this.path.lineto(transformed[0], transformed[1]);
    }
    
    /**
     * Move the current position to a new location (PostScript moveto operator).
     * 
     * @param x X-coordinate (before CTM is applied)
     * @param y Y-coordinate (before CTM is applied)
     * 
     * @throws PSErrorRangeCheck The PostScript rangecheck error occurred.
     * @throws PSErrorTypeCheck The PostScript typecheck error occurred.
     */
    public void moveto(final double x, final double y)
            throws PSErrorRangeCheck, PSErrorTypeCheck {
        this.position[0] = x;
        this.position[1] = y;
        double[] transformed = this.ctm.transform(x, y);
        this.path.moveto(transformed[0], transformed[1]);
    }
    
    /**
     * Implements PostScript operator 'pathbbox'. Calculates the bounding box
     * of the current path.
     * @return Array with four values {llx lly urx ury}, which are the X- and
     *         Y-coordinates (in user space coordinates) of the lower-left and
     *         upper-right corner.
     * @throws PSError A PostScript error occurred.
     */
    public double[] pathbbox() throws PSError {
        double[] deviceCoors = this.path.boundingBox();
        double[] ll = this.ctm.itransform(deviceCoors[0], deviceCoors[1]);
        double[] lr = this.ctm.itransform(deviceCoors[2], deviceCoors[1]);
        double[] ur = this.ctm.itransform(deviceCoors[2], deviceCoors[3]);
        double[] ul = this.ctm.itransform(deviceCoors[0], deviceCoors[3]);
        double[] bbox = new double[4];
        bbox[0] = Math.min(Math.min(ll[0], lr[0]), Math.min(ur[0], ul[0]));
        bbox[1] = Math.min(Math.min(ll[1], lr[1]), Math.min(ur[1], ul[1]));
        bbox[2] = Math.max(Math.max(ll[0], lr[0]), Math.max(ur[0], ul[0]));
        bbox[3] = Math.max(Math.max(ll[1], lr[1]), Math.max(ur[1], ul[1]));
        return bbox;
    }
    
    /**
     * Add a curveto section to the current path.
     * 
     * @param dx1 X-coordinate of first control point
     * @param dy1 Y-coordinate of first control point
     * @param dx2 X-coordinate of second control point
     * @param dy2 Y-coordinate of second control point
     * @param dx3 X-coordinate of end point
     * @param dy3 Y-coordinate of end point
     * 
     * @throws PSErrorRangeCheck A PostScript rangecheck error occurred.
     * @throws PSErrorTypeCheck A PostScript typecheck error occurred.
     */
    public void rcurveto(final double dx1, final double dy1, final double dx2,
            final double dy2, final double dx3, final double dy3)
            throws PSErrorRangeCheck, PSErrorTypeCheck {
        double[] coor1 = this.ctm.transform(this.position[0] + dx1,
                this.position[1] + dy1);
        double[] coor2 = this.ctm.transform(this.position[0] + dx2,
                this.position[1] + dy2);
        double[] coor3 = this.ctm.transform(this.position[0] + dx3,
                this.position[1] + dy3);
        this.position[0] = this.position[0] + dx3;
        this.position[1] = this.position[1] + dy3;
        this.path.curveto(coor1, coor2, coor3);
    }
    
    /**
     * Draw a line to a relative point.
     * 
     * @param dx delta X-coordinate (before CTM is applied)
     * @param dy delta Y-coordinate (before CTM is applied)
     * 
     * @throws PSError A PostScript error occurred.
     */
    public void rlineto(final double dx, final double dy) throws PSError {
        if (Double.isNaN(position[0])) {
            throw new PSErrorNoCurrentPoint();
        }
        this.position[0] = this.position[0] + dx;
        this.position[1] = this.position[1] + dy;
        double[] transformed = this.ctm.transform(this.position);
        this.path.lineto(transformed[0], transformed[1]);
    }
    
    /**
     * Move to a relative point.
     * 
     * @param dx delta X-coordinate (before CTM is applied)
     * @param dy delta Y-coordinate (before CTM is applied)
     * 
     * @throws PSError A PostScript error occurred.
     */
    public void rmoveto(final double dx, final double dy) throws PSError {
        if (Double.isNaN(position[0])) {
            throw new PSErrorNoCurrentPoint();
        }
        this.position[0] = this.position[0] + dx;
        this.position[1] = this.position[1] + dy;
        double[] transformed = this.ctm.transform(this.position);
        this.path.moveto(transformed[0], transformed[1]);
    }
    
    /**
     * Sets the color.
     * 
     * @param newColor Parameters of new color (defined in current color space)
     * 
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws PSError A PostScript error occurred.
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    public void setcolor(final double[] newColor)
            throws IOException, PSError, ProgramError {
        
        color.setColor(newColor);
    }
    
    /**
     * Sets the color rendering.
     * 
     * @param dict The new color rendering dictionary.
     */
    public void setColorRendering(final PSObjectDict dict) {
        colorRendering = dict;
    }
    
    /**
     * Sets the current color space.
     * 
     * @param obj Object describing the color space. Should be a literal name or
     * an array starting with a literal name.
     * 
     * @throws PSError A PostScript error has occurred.
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    public void setcolorspace(final PSObject obj)
            throws PSError, IOException, ProgramError {
        
        color = ColorManager.autoSetColorSpace(obj);
    }
    
    /**
     * Updates the field current position by retrieving the last coordinate
     * of the current path and transforming it back to user space
     * coordinates. This is usually done after the CTM has been altered.
     * 
     * @throws PSErrorRangeCheck A PostScript rangecheck error occurred.
     * @throws PSErrorTypeCheck A PostScript typecheck error occurred.
     */
    public void updatePosition() throws PSErrorRangeCheck, PSErrorTypeCheck {
        try {
            double[] posd = getCurrentPosInDeviceSpace();
            this.position = this.ctm.itransform(posd);
        } catch (PSErrorNoCurrentPoint e) {
            // Apparently there is no current point
            this.position[0] = Double.NaN;
            this.position[1] = Double.NaN;
        }
    }
    
    /**
     * Set the black generation.
     * 
     * @param proc The black generation procedure.
     */
    public void setBlackGeneration(final PSObjectArray proc) {
        blackGeneration = proc;
    }

    /**
     * Sets the current transformation matrix (CTM).
     * 
     * @param pCtm the ctm to set
     */
    public void setCtm(final PSObjectMatrix pCtm) {
        ctm = pCtm;
    }

    /**
     * Gets the current transformation matrix (CTM).
     * @return the ctm
     */
    public PSObjectMatrix getCtm() {
        return ctm;
    }
    
    /**
     * Set the overprint.
     * 
     * @param pOverprint The overprint.
     */
    public void setOverprint(final boolean pOverprint) {
        overprint = pOverprint;
    }

    /**
     * Sets the current position.
     * 
     * @param x The X-coordinate.
     * @param y The Y-coordinate.
     */
    public void setPosition(final double x, final double y) {
        position[0] = x;
        position[1] = y;
    }

    /**
     * Gets the current position.
     * 
     * @return the position
     */
    public double[] getPosition() {
        return position.clone();
    }

    /**
     * Sets the current path.
     * 
     * @param pPath the path to set
     */
    public void setPath(final Path pPath) {
        path = pPath;
    }

    /**
     * Gets the current path.
     * 
     * @return the path
     */
    public Path getPath() {
        return path;
    }

    /**
     * Sets the clipping path.
     * 
     * @param pClippingPath the clippingPath to set
     */
    public void setClippingPath(final Path pClippingPath) {
        clippingPath = pClippingPath;
    }

    /**
     * Gets the clipping path.
     * 
     * @return the clippingPath
     */
    public Path getClippingPath() {
        return clippingPath;
    }

    /**
     * Sets the color.
     * 
     * @param pColor The new color.
     */
    public void setColor(final PSColor pColor) {
        color = pColor;
    }

    /**
     * Gets the color.
     * 
     * @return The current color.
     */
    public PSColor getColor() {
        return color;
    }

    /**
     * Sets the flat parameter.
     * 
     * @param pFlat the flat to set
     */
    public void setFlatness(final double pFlat) {
        flatness = pFlat;
    }

    /**
     * Sets the font.
     * 
     * @param pFont the font to set
     */
    public void setFont(final PSObjectFont pFont) {
        font = pFont;
    }
    
    /**
     * Sets the halftone.
     * 
     * @param pHalftone The new halftone.
     */
    public void setHalftone(final PSObject pHalftone) {
        halftone = pHalftone;
    }

    /**
     * Gets the font.
     * 
     * @return the font
     */
    public PSObjectFont getFont() {
        return font;
    }

    /**
     * Sets the line width.
     * 
     * @param pLineWidth the lineWidth to set
     */
    public void setLineWidth(final double pLineWidth) {
        lineWidth = pLineWidth;
    }

    /**
     * Gets the line width.
     * 
     * @return the lineWidth
     */
    public double getLineWidth() {
        return lineWidth;
    }

    /**
     * Sets the dash pattern.
     * 
     * @param pDashPattern the dashPattern to set
     */
    public void setDashPattern(final PSObjectArray pDashPattern) {
        dashPattern = pDashPattern;
    }

    /**
     * Gets the dash pattern.
     * 
     * @return the dashPattern
     */
    public PSObjectArray getDashPattern() {
        return dashPattern;
    }

    /**
     * Sets the dash offset.
     * 
     * @param pDashOffset the dashOffset to set
     */
    public void setDashOffset(final double pDashOffset) {
        dashOffset = pDashOffset;
    }

    /**
     * Gets the dash offset.
     * 
     * @return the dashOffset
     */
    public double getDashOffset() {
        return dashOffset;
    }

    /**
     * Sets the output device.
     * 
     * @param pDevice the device to set
     */
    public void setDevice(final OutputDevice pDevice) {
        device = pDevice;
    }

    /**
     * Gets the output device.
     * 
     * @return the device
     */
    public OutputDevice getDevice() {
        return device;
    }

    /**
     * Sets the line cap.
     * 
     * @param pLineCap the lineCap to set
     */
    public void setLineCap(final int pLineCap) {
        lineCap = pLineCap;
    }

    /**
     * Gets the current line cap.
     * 
     * @return the lineCap
     */
    public int getLineCap() {
        return lineCap;
    }

    /**
     * Sets the line join.
     * 
     * @param pLineJoin the lineJoin to set
     */
    public void setLineJoin(final int pLineJoin) {
        lineJoin = pLineJoin;
    }

    /**
     * Gets the current line join.
     * 
     * @return the lineJoin
     */
    public int getLineJoin() {
        return lineJoin;
    }

    /**
     * Sets the current miter limit.
     * 
     * @param pMiterLimit the miterLimit to set
     */
    public void setMiterLimit(final double pMiterLimit) {
        miterLimit = pMiterLimit;
    }

    /**
     * Sets the miter limit.
     * 
     * @return the miterLimit
     */
    public double getMiterLimit() {
        return miterLimit;
    }
    
    /**
     * Sets the smoothness.
     * 
     * @param pSmoothness The smoothness.
     */
    public void setSmoothness(final double pSmoothness) {
        smoothness = pSmoothness;
    }

    /**
     * Sets the stroke adjustment.
     * 
     * @param pStrokeAdjust The new stroke adjustment.
     */
    public void setStrokeAdjust(final boolean pStrokeAdjust) {
        strokeAdjust = pStrokeAdjust;
    }

    /**
     * Gets the current stroke adjustment.
     * 
     * @return The stroke adjustment.
     */
    public boolean getStrokeAdjust() {
        return strokeAdjust;
    }
    
    /**
     * Set the transfer procedure.
     * 
     * @param redproc The red transfer function.
     * @param greenproc The green transfer function.
     * @param blueproc The blue transfer function.
     * @param grayproc The gray transfer function.
     * 
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    public void setColorTransfer(final PSObjectArray redproc,
            final PSObjectArray greenproc, final PSObjectArray blueproc,
            final PSObjectArray grayproc) throws ProgramError {
        
        try {
            transfer.set(0, redproc);
            transfer.set(1, greenproc);
            transfer.set(2, blueproc);
            transfer.set(3, grayproc);
        } catch (PSErrorRangeCheck e) {
            throw new ProgramError("rangecheck in setColorTransfer()");
        }
    }
    

    /**
     * Set the transfer procedure.
     * 
     * @param proc The new transfer procedure.
     * 
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    public void setTransfer(final PSObjectArray proc) throws ProgramError {
        for (int i = 0; i < 4; i++) {
            try {
                transfer.set(i, proc);
            } catch (PSErrorRangeCheck e) {
                throw new ProgramError("rangecheck in setTransfer()");
            }
        }
    }
    
    /**
     * Set the undercolor removal.
     * 
     * @param proc The new undercolor removal procedure.
     */
    public void setUndercolorRemoval(final PSObjectArray proc) {
        undercolorRemoval = proc;
    }

}
