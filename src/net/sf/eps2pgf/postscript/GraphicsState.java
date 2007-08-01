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
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.sf.eps2pgf.postscript;

import java.io.IOException;
import java.util.logging.*;
import net.sf.eps2pgf.ProgramError;

import net.sf.eps2pgf.output.OutputDevice;
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
     * Current color space
     */
    public PSObjectArray colorSpace = new PSObjectArray();
    
    /**
     * Parameter describing the accuracy of flattening a path
     */
    public double flat = 1.0;
    
    /**
     * Current color (meaning of parameters depends on the colorSpace specified above)
     */
    public double[] color;
    static double[] defaultGray = {0.0};
    static double[] defaultRgb = {0.0, 0.0, 0.0};
    static double[] defaultCmyk = {0.0, 0.0, 0.0, 1.0};
    
    /**
     * Current font
     */
    public PSObjectFont font;
    
    /**
     * Current line width (in user space coordinates)
     */
    public double linewidth = 1.0;
    
    /**
     * Current dash pattern
     */
    public PSObjectArray dashpattern = new PSObjectArray();
    
    /**
     * Current dash offset
     */
    public double dashoffset = 0.0;
    
    
    /**
     * Current output device
     */
    public OutputDevice device;
    
    /**
     * Dictionary that can be used by the output device to store information.
     * The contents of this dictionary is output device specific.
     */
    public PSObjectDict deviceData = new PSObjectDict();
    
    /**
     * Link to the parent graphics state stack
     */
    GstateStack parentStack;
    
    /**
     * Creates a new default graphics state.
     * @param parentGraphicsStack Pointer the graphics stack of which this object is part of.
     */
    public GraphicsState(GstateStack parentGraphicsStack, OutputDevice wDevice) {
        parentStack = parentGraphicsStack;
        device = wDevice;
                
        initmatrix();
        path = new Path(parentStack);
        clippingPath = new Path(parentStack);
        font = new PSObjectFont();
    }
    
    /**
     * Adds an arc to the end of the current path
     * @param x X-coordinate of center of circle in user space coordinates
     * @param y Y-coordinate of center of circle in user space coordinates
     * @param r Radius of circle
     * @param angle1 Angle in degrees of starting point
     * @param angle2 Angle in degrees of end point
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorInvalidAccess No access to a required object
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorRangeCheck A value is out of range
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorTypeCheck An object has an incorrect type
     */
    public void arc(double x, double y, double r, double angle1, double angle2, boolean isPositive)
            throws PSErrorInvalidAccess, PSErrorRangeCheck, PSErrorTypeCheck {
        double mult = 1;
        if (!isPositive) {
            mult = -1;
        }
        
        while (mult*angle2 < mult*angle1) {
            angle2 += mult*360;
        }
        
        // convert angles from degrees to radians
        angle1 = Math.toRadians(angle1);
        angle2 = Math.toRadians(angle2);
        
        // move to start, if necessary
        double x0 = x + r*Math.cos(angle1);
        double y0 = y + r*Math.sin(angle1);
        if ( (Math.abs(position[0]-x0) > 1e-6) || (Math.abs(position[1]-y0) > 1e-6) ) {
            lineto(x0, y0);
        } else if (Double.isNaN(position[0]) || Double.isNaN(position[1])) {
            moveto(x0, y0);
        }
        
        // take step from angle1 to multiples of 90 degrees
        double ang1;
        double ang2 = angle1;
        double angleStep = mult*Math.PI/2;
        for (double ang = angle1 ; mult*ang2 < mult*angle2 ; ang += angleStep) {
            ang1 = ang2;
            ang2 = angleStep*Math.floor((ang + angleStep)/angleStep);
            if (mult*angle2 < mult*ang2) {
                ang2 = angle2;
            }

            double mang = (ang1+ang2)/2;
            
            double unitX0 = Math.cos(mult*(ang2-ang1)/2);
            double unitY0 = Math.sin(mult*(ang2-ang1)/2);
            
            double xControl = (4-unitX0)/3;
            double yControl = (1-unitX0)*(3-unitX0)/(3*unitY0);
            double angControl = Math.atan2(yControl, xControl);
            double rControl = Math.sqrt(xControl*xControl + yControl*yControl);
            
            double x1 = x + r*rControl*Math.cos(mang-mult*angControl);
            double y1 = y + r*rControl*Math.sin(mang-mult*angControl);
            
            double x2 = x + r*rControl*Math.cos(mang+mult*angControl);
            double y2 = y + r*rControl*Math.sin(mang+mult*angControl);
            
            double x3 = x + r*Math.cos(ang2);
            double y3 = y + r*Math.sin(ang2);
            
            curveto(x1, y1, x2, y2, x3, y3);
        }
    }
    
    /**
     * Implements the 'arcto' PostScript operator
     */
    public double[] arcto(double x1, double y1, double x2, double y2, double r) 
            throws PSError {
        double x0 = position[0];
        double y0 = position[1];
        
        if (Double.isNaN(x0) || Double.isNaN(y0)) {
            throw new PSErrorNoCurrentPoint();
        }
        
        // Calculate some angles
        double phi1 = Math.atan2(y0-y1, x0-x1);
        double phi2 = Math.atan2(y2-y1, x2-x1);
        double alpha = phi2 - phi1;
        if (alpha > Math.PI) {
            alpha = alpha - 2*Math.PI;
        } else if (alpha < -Math.PI) {
            alpha = alpha + 2*Math.PI;
        }
        
        // Check if angle is -180, 0 or 180 degrees
        if ( (Math.abs(Math.abs(alpha) - Math.PI) < 1e-3) || (Math.abs(alpha) < 1e-3) ) {
            lineto(x1, y1);
            double[] ret = {x1, y1, x1, y1};
            return ret;
        }
        
        // Calculate lines parallel to the lines (x0,y0)-(x1,y1) and (x1,y1)-(x2,y2)
        double posorneg;
        if (alpha >= 0) {
            posorneg = 1.0;
        } else {
            posorneg = -1.0;
        }
        double p1x0 = x0 + r*Math.cos(phi1 + posorneg*Math.PI/2);
        double p1y0 = y0 + r*Math.sin(phi1 + posorneg*Math.PI/2);
        double p1x1 = x1 + r*Math.cos(phi1 + posorneg*Math.PI/2);
        double p1y1 = y1 + r*Math.sin(phi1 + posorneg*Math.PI/2);
        
        double p2x1 = x1 + r*Math.cos(phi2 - posorneg*Math.PI/2);
        double p2y1 = y1 + r*Math.sin(phi2 - posorneg*Math.PI/2);
        double p2x2 = x2 + r*Math.cos(phi2 - posorneg*Math.PI/2);
        double p2y2 = y2 + r*Math.sin(phi2 - posorneg*Math.PI/2);
        
        // Calculate intersection, this is the center of the circle
        // Weisstein, Eric W. "Line-Line Intersection." From MathWorld--A Wolfram Web
        // Resource. http://mathworld.wolfram.com/Line-LineIntersection.html 
        double num = det(det(p1x0,p1y0,p1x1,p1y1), p1x0-p1x1, det(p2x1,p2y1,p2x2,p2y2), p2x1-p2x2);
        double den = det(p1x0-p1x1, p1y0-p1y1, p2x1-p2x2, p2y1-p2y2);
        double x = num/den;
        num = det(det(p1x0, p1y0, p1x1, p1y1), p1y0-p1y1, det(p2x1,p2y1,p2x2,p2y2), p2y1-p2y2);
        double y = num/den;

        double ang1 = Math.PI + phi1 + posorneg*Math.PI/2;
        double ang2 = Math.PI + phi2 - posorneg*Math.PI/2;

        double xt1 = x + r*Math.cos(ang1);
        double yt1 = y + r*Math.sin(ang1);
        double xt2 = x + r*Math.cos(ang2);
        double yt2 = y + r*Math.sin(ang2);

        arc(x, y, r, ang1, ang2, (ang2 > ang1));
        
        double[] ret = {xt1, yt1, xt2, yt2};
        return ret;
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
        GraphicsState newState = new GraphicsState(parentStack, device);
        newState.CTM = CTM.clone();
        newState.position = position.clone();
        newState.path = path.clone();
        newState.clippingPath = clippingPath.clone();
        newState.font = font.clone();
        newState.linewidth = linewidth;
        newState.dashpattern = dashpattern.clone();
        newState.dashoffset = dashoffset;
        newState.color = color.clone();
        newState.colorSpace = colorSpace.clone();
        newState.deviceData = deviceData.clone();
        return newState;
    }
    
    /**
     * Returns the current color in the CMYK color space
     */
    public double[] currentcmykcolor() throws PSError, ProgramError {
        String spaceName = colorSpace.get(0).toName().name;
        if (spaceName.equals("DeviceGray")) {
            return ColorConvert.grayToCMYK(color[0]);
        } else if (spaceName.equals("DeviceRGB")) {
            return ColorConvert.RGBtoCMYK(color);
        } else if (spaceName.equals("DeviceCMYK")) {
            return color.clone();
        } else {
            throw new ProgramError("You've found a bug. Current colorspace is"
                    + " invalid. That should not be possible.");
        }
    }

    /**
     * Returns the current color in grayscale
     */
    public double currentgray() throws PSError, ProgramError {
        String spaceName = colorSpace.get(0).toName().name;
        if (spaceName.equals("DeviceGray")) {
            return color[0];
        } else if (spaceName.equals("DeviceRGB")) {
            return ColorConvert.RGBtoGray(color);
        } else if (spaceName.equals("DeviceCMYK")) {
            return ColorConvert.CMYKtoGray(color);
        } else {
            throw new ProgramError("You've found a bug. Current colorspace is"
                    + " invalid. That should not be possible.");
        }
    }
    
    /**
     * Returns the current color in the HSB (also HSV) color space
     */
    public double[] currenthsbcolor() throws PSError, ProgramError {
        String spaceName = colorSpace.get(0).toName().name;
        if (spaceName.equals("DeviceGray")) {
            return ColorConvert.grayToHSB(color[0]);
        } else if (spaceName.equals("DeviceRGB")) {
            return ColorConvert.RGBtoHSB(color);
        } else if (spaceName.equals("DeviceCMYK")) {
            return ColorConvert.CMYKtoHSB(color);
        } else {
            throw new ProgramError("You've found a bug. Current colorspace is"
                    + " invalid. That should not be possible.");
        }
    }

    /**
     * Returns the current color in the RGB color space
     */
    public double[] currentrgbcolor() throws PSError, ProgramError {
        String spaceName = colorSpace.get(0).toName().name;
        if (spaceName.equals("DeviceGray")) {
            return ColorConvert.grayToRGB(color[0]);
        } else if (spaceName.equals("DeviceRGB")) {
            return color.clone();
        } else if (spaceName.equals("DeviceCMYK")) {
            return ColorConvert.CMYKtoRGB(color);
        } else {
            throw new ProgramError("You've found a bug. Current colorspace is"
                    + " invalid. That should not be possible.");
        }
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
        double[] coor1 = CTM.transform(x1, y1);
        double[] coor2 = CTM.transform(x2, y2);
        double[] coor3 = CTM.transform(x3, y3);
        path.curveto(coor1, coor2, coor3);
    }
    
    /**
     * Calculate determinant of the matrix: [a b]
     *                                      [c d]
     */
    double det(double a, double b, double c, double d) {
        return a*d - b*c;
    }
    
    /**
     * Replace the current path by a flattened version of the path
     * @throws net.sf.eps2pgf.postscript.errors.PSError A PostScript error occurred
     * @throws net.sf.eps2pgf.ProgramError This should never happeb. If it is thrown it indicates a bug in
     * Eps2pgf.
     */
    public void flattenpath() throws PSError, ProgramError {
        // Maximum difference between normal and flattened path. Defined in
        // device space coordinates. Assume a device resolution of 1200 dpi.
        double deviceScale = device.defaultCTM().getMeanScaling();
        double maxError = flat * 72.0 / 1200.0 * deviceScale;

        path = path.flattenpath(maxError);
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
        CTM = device.defaultCTM();
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
        double[] transformed = CTM.transform(x, y);
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
        double[] transformed = CTM.transform(x, y);
        path.moveto(transformed[0], transformed[1]);
    }
    
    /**
     * Implements PostScript operator 'pathbbox'. Calculates the bounding box
     * of the current path.
     * @return Array with four values {llx lly urx ury}, which are the X- and
     *         Y-coordinates (in user space coordinates) of the lower-left and
     *         upper-right corner.
     * @throws net.sf.eps2pgf.postscript.errors.PSError A PostScript error occurred.
     */
    public double[] pathbbox() throws PSError {
        double[] deviceCoors = path.boundingBox();
        double[] ll = CTM.itransform(deviceCoors[0], deviceCoors[1]);
        double[] lr = CTM.itransform(deviceCoors[2], deviceCoors[1]);
        double[] ur = CTM.itransform(deviceCoors[2], deviceCoors[3]);
        double[] ul = CTM.itransform(deviceCoors[0], deviceCoors[3]);
        double[] bbox = new double[4];
        bbox[0] = Math.min(Math.min(ll[0], lr[0]), Math.min(ur[0], ul[0]));
        bbox[1] = Math.min(Math.min(ll[1], lr[1]), Math.min(ur[1], ul[1]));
        bbox[2] = Math.max(Math.max(ll[0], lr[0]), Math.max(ur[0], ul[0]));
        bbox[3] = Math.max(Math.max(ll[1], lr[1]), Math.max(ur[1], ul[1]));
        return bbox;
    }
    
    /**
     * Add a curveto section to the current path
     * @param dx1 X-coordinate of first control point
     * @param dy1 Y-coordinate of first control point
     * @param dx2 X-coordinate of second control point
     * @param dy2 Y-coordinate of second control point
     * @param dx3 X-coordinate of end point
     * @param dy3 Y-coordinate of end point
     */
    public void rcurveto(double dx1, double dy1, double dx2, double dy2, 
            double dx3, double dy3) throws PSErrorInvalidAccess, PSErrorRangeCheck,
            PSErrorTypeCheck {
        double[] coor1 = CTM.transform(position[0] + dx1, position[1] + dy1);
        double[] coor2 = CTM.transform(position[0] + dx2, position[1] + dy2);
        double[] coor3 = CTM.transform(position[0] + dx3, position[1] + dy3);
        position[0] = position[0] + dx3;
        position[1] = position[1] + dy3;
        path.curveto(coor1, coor2, coor3);
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
        double[] transformed = CTM.transform(position);
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
        double[] transformed = CTM.transform(position);
        path.moveto(transformed[0], transformed[1]);
    }
    
    /**
     * Sets the color
     * @param newColor Parameters of new color (defined in current color space)
     */
    public void setcolor(double[] newColor) throws IOException {
        int n = Math.min(color.length, newColor.length);
        for (int i = 0 ; i < n ; i++) {
            newColor[i] = Math.max(Math.min(newColor[i], 1.0), 0.0);
        }

        for (int i = 0 ; i < n ; i++) {
            color[i] = newColor[i];
        }
        device.setColor(color);
    }
    
    /**
     * Sets the current color space
     * @param obj Object describing the color space. Should be a literal name or an
     * array starting with a literal name.
     */
    public void setcolorspace(PSObject obj, boolean writeDevice) throws PSError, IOException {
        String spaceName;
        if (obj instanceof PSObjectName) {
            spaceName = ((PSObjectName)obj).name;
        } else if (obj instanceof PSObjectArray) {
            spaceName = ((PSObjectArray)obj).get(0).toName().name;
        } else {
            throw new PSErrorTypeCheck();
        }
        
        if (spaceName.equals("DeviceGray")) {
            color = defaultGray.clone();
        } else if (spaceName.equals("DeviceRGB")) {
            color = defaultRgb.clone();
        } else if (spaceName.equals("DeviceCMYK")) {
            color = defaultCmyk.clone();
        } else {
            throw new PSErrorUndefined();
        }
        PSObjectArray newColSpace = new PSObjectArray();
        newColSpace.addToEnd(new PSObjectName(spaceName, true));
        colorSpace = newColSpace;
        if (writeDevice) {
            device.setColor(color);
        }
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
            position = CTM.itransform(posd);
        } catch (PSErrorNoCurrentPoint e) {
            // Apparently there is no current point
            position[0] = Double.NaN;
            position[1] = Double.NaN;
        }
    }
    
}
