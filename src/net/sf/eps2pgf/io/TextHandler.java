/*
 * TextHandler.java
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

package net.sf.eps2pgf.io;

import java.io.IOException;

import org.fontbox.util.BoundingBox;

import net.sf.eps2pgf.postscript.*;
import net.sf.eps2pgf.postscript.errors.*;

/**
 *
 * @author Paul Wagenaars
 */
public class TextHandler {
    GstateStack gstate;
    
    /** Creates a new instance of TextHandler */
    public TextHandler(GstateStack graphicsStateStack) {
        gstate = graphicsStateStack;
    }
    
    /**
     * Appends the path of a text to the current path
     */
    public void charpath(PSObjectString string) throws PSError {
        PSObjectFont currentFont = gstate.current.font;
        
        PSObjectArray charNames = string.decode(currentFont.getEncoding());
        String text = currentFont.charNames2texStrings(charNames);
        
        double angle = gstate.current.CTM.getRotation();
        
        // Calculate scaling and fontsize in points (=1/72 inch)
        PSObjectMatrix fontMatrix = currentFont.getFontMatrix();
        double scaling = fontMatrix.getMeanScaling() * gstate.current.CTM.getMeanScaling();
        double fontsize = scaling / 1000 / 25.4 * 72;  // convert micrometer to pt

        BoundingBox bbox = currentFont.getBBox(charNames);

        double[] pos = gstate.current.getCurrentPosInDeviceSpace();
        double[] dpos;

        dpos = getAnchor("bl", bbox, scaling, angle);
        gstate.current.path.moveto(pos[0]+dpos[0], pos[1]+dpos[1]);
        dpos = getAnchor("br", bbox, scaling, angle);
        gstate.current.path.lineto(pos[0]+dpos[0], pos[1]+dpos[1]);
        dpos = getAnchor("tr", bbox, scaling, angle);
        gstate.current.path.lineto(pos[0]+dpos[0], pos[1]+dpos[1]);
        dpos = getAnchor("tl", bbox, scaling, angle);
        gstate.current.path.lineto(pos[0]+dpos[0], pos[1]+dpos[1]);
        gstate.current.path.closepath();

        // Determine current point shift in user space coordinates
        double showShift[] = shiftPos(currentFont.getWidth(charNames), 0, scaling, angle);
        showShift = gstate.current.CTM.idtransform(showShift);
        gstate.current.rmoveto(showShift[0], showShift[1]);
    }
    
    /**
     * Shows text in the output
     * @param exp Exporter to which the output will be sent
     * @return Displacement vector [dx, dy] in user space coordinates
     */
    public double[] showText(OutputDevice exp, PSObjectString string) 
            throws PSErrorTypeCheck, PSErrorRangeCheck, PSErrorUndefined, 
            PSErrorUnimplemented, PSErrorNoCurrentPoint, IOException {
        return showText(exp, string, false);
    }
    
    /**
     * Shows text in the output
     * @param exp Exporter to which the output will be sent
     * @param noOutput Don't show the text in the output if set true.
     * @return Displacement vector [dx, dy] in user space coordinates
     */
    public double[] showText(OutputDevice exp, PSObjectString string, boolean noOutput) 
            throws PSErrorTypeCheck, PSErrorRangeCheck, PSErrorUndefined, 
            PSErrorUnimplemented, PSErrorNoCurrentPoint, IOException {        
        PSObjectFont currentFont = gstate.current.font;
        
        PSObjectArray charNames = string.decode(currentFont.getEncoding());
        
        String text = currentFont.charNames2texStrings(charNames);
        
        double angle = gstate.current.CTM.getRotation();
        
        // Calculate scaling and fontsize in points (=1/72 inch)
        PSObjectMatrix fontMatrix = currentFont.getFontMatrix();
        double scaling = fontMatrix.getMeanScaling() * gstate.current.CTM.getMeanScaling();
        double fontsize = scaling / 1000 / 25.4 * 72;  // convert micrometer to pt

        // Draw text
        if (!noOutput) {
            BoundingBox bbox = currentFont.getBBox(charNames);

            double[] pos = gstate.current.getCurrentPosInDeviceSpace();
            double[] dpos;

//            dpos = getAnchor("tl", bbox, scaling, angle);
//            exp.drawDot(pos[0]+dpos[0], pos[1]+dpos[1]);
//            dpos = getAnchor("bl", bbox, scaling, angle);
//            exp.drawDot(pos[0]+dpos[0], pos[1]+dpos[1]);
//            dpos = getAnchor("tr", bbox, scaling, angle);
//            exp.drawDot(pos[0]+dpos[0], pos[1]+dpos[1]);
//            dpos = getAnchor("br", bbox, scaling, angle);
//            exp.drawDot(pos[0]+dpos[0], pos[1]+dpos[1]);
//            dpos = getAnchor("cc", bbox, scaling, angle);
//            exp.drawDot(pos[0]+dpos[0], pos[1]+dpos[1]);
//            dpos = getAnchor("tc", bbox, scaling, angle);
//            exp.drawDot(pos[0]+dpos[0], pos[1]+dpos[1]);
//            dpos = getAnchor("bc", bbox, scaling, angle);
//            exp.drawDot(pos[0]+dpos[0], pos[1]+dpos[1]);
//            dpos = getAnchor("cl", bbox, scaling, angle);
//            exp.drawDot(pos[0]+dpos[0], pos[1]+dpos[1]);
//            dpos = getAnchor("cr", bbox, scaling, angle);
//            exp.drawDot(pos[0]+dpos[0], pos[1]+dpos[1]);

            dpos = getAnchor("cc", bbox, scaling, angle);
            double textPos[] = new double[2];
            textPos[0] = pos[0] + dpos[0];
            textPos[1] = pos[1] + dpos[1];
            exp.show(text, textPos, angle, fontsize, "cc");
        }

        // Determine current point shift in user space coordinates
        double showShift[] = shiftPos(currentFont.getWidth(charNames), 0, scaling, angle);
        showShift = gstate.current.CTM.idtransform(showShift);
        
        return showShift;
    }
    
    /**
     * Determine the position of an anchor relative to the current position
     * @param anchor Follows psfrag. A combination of two characters that
     *               describe vertical and horizontal alignment. Vertical
     *               alignment: t - top, c - center, B - baselinem b - bottom
     *               and horizontal alignment: l - left, c - center, r - right
     *               If either letter is omitted then c is assumed. If anchor
     *               is completely empty, then "Bl" is assumed.
     * @param unitBbox Text bounding box normalized to 1pt.
     * @param scaling Scaling factor for bounding box. E.g. for 12pt font size, scaling = 12
     * @param angle Text rotation in degrees
     */
    public double[] getAnchor(String anchor, BoundingBox unitBbox, double scaling, double angle) {
        if (anchor.length() == 0) {
            anchor = "Bl";
        }
        
        double x, y;
        
        // Vertical alignment
        if (anchor.contains("t")) {
            y = unitBbox.getUpperRightY();
        } else if (anchor.contains("B")) {
            y = 0;
        } else if (anchor.contains("b")) {
            y = unitBbox.getLowerLeftY();
        } else {
            y = 0.5 * (unitBbox.getUpperRightY() + unitBbox.getLowerLeftY());
        }
        
        // Horizontal alignment
        if (anchor.contains("l")) {
            x = unitBbox.getLowerLeftX();
        } else if (anchor.contains("r")) {
            x = unitBbox.getUpperRightX();
        } else {
            x = 0.5 * (unitBbox.getLowerLeftX() + unitBbox.getUpperRightX());
        }
        
        return shiftPos(x, y, scaling, angle);
    }
    
    /**
     * Scale and rotate the translation vector {dx, dy}
     * @param dx Delta x shift (before scaling and rotation)
     * @param dy Delta y shift (before scaling and rotation)
     * @param scaling Scaling for shift
     * @param angle Angle (in degrees) for rotation
     * @return New translation vector
     */
    double[] shiftPos(double dx, double dy, double scaling, double angle) {
        angle = Math.toRadians(angle);
        double[] newPos = new double[2];
        newPos[0] = scaling * (dx*Math.cos(angle) - dy*Math.sin(angle));
        newPos[1] = scaling * (dx*Math.sin(angle) + dy*Math.cos(angle));
        return newPos;        
    }
    
}
