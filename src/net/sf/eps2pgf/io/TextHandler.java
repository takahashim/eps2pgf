/*
 * TextHandler.java
 *
 * This file is part of Eps2pgf.
 *
 * Copyright 2007 Paul Wagenaars <paul@wagenaars.org>
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

import net.sf.eps2pgf.ProgramError;
import net.sf.eps2pgf.io.TextReplacements.Rule;
import net.sf.eps2pgf.io.devices.OutputDevice;
import net.sf.eps2pgf.postscript.GstateStack;
import net.sf.eps2pgf.postscript.PSObjectArray;
import net.sf.eps2pgf.postscript.PSObjectFont;
import net.sf.eps2pgf.postscript.PSObjectString;
import net.sf.eps2pgf.postscript.errors.PSError;

import org.fontbox.util.BoundingBox;

/**
 *
 * @author Paul Wagenaars
 */
public class TextHandler {
    
    /** Link to the graphics state object. */
    private GstateStack gstate;
    
    /** Text replacement rules. */
    private TextReplacements textReplace;

    /**
     * Creates a new instance of TextHandler.
     *
     * @param graphicsStateStack Link to graphics state.
     */
    public TextHandler(final GstateStack graphicsStateStack) {
    	this(graphicsStateStack, null);
    }

    /**
     * Creates a new instance of TextHandler.
     * 
     * @param graphicsStateStack Link to graphics state.
     * @param pTextReplace Text replacements.
     */
    public TextHandler(final GstateStack graphicsStateStack,
    		final TextReplacements pTextReplace) {
        this.gstate = graphicsStateStack;
        if (pTextReplace != null) {
        	this.textReplace = pTextReplace;
        } else {
        	this.textReplace = new TextReplacements();
        }
         
    }
    
    /**
     * Appends the path of a text to the current path.
     * 
     * @param string Determine character path of this text.
     * 
     * @throws PSError A PostScript error occurred.
     */
    public final void charpath(final PSObjectString string) throws PSError {
        PSObjectFont currentFont = gstate.current.font;
        
        PSObjectArray charNames = string.decode(currentFont.getEncoding());
        
        double angle = gstate.current.CTM.getRotation();
        
        // Calculate scaling and fontsize in points (=1/72 inch)
        double scaling = gstate.current.CTM.getMeanScaling();

        BoundingBox bbox = currentFont.getBBox(charNames);

        double[] pos = gstate.current.getCurrentPosInDeviceSpace();
        double[] dpos;

        dpos = getAnchor("bl", bbox, scaling, angle);
        gstate.current.path.moveto(pos[0] + dpos[0], pos[1] + dpos[1]);
        dpos = getAnchor("br", bbox, scaling, angle);
        gstate.current.path.lineto(pos[0] + dpos[0], pos[1] + dpos[1]);
        dpos = getAnchor("tr", bbox, scaling, angle);
        gstate.current.path.lineto(pos[0] + dpos[0], pos[1] + dpos[1]);
        dpos = getAnchor("tl", bbox, scaling, angle);
        gstate.current.path.lineto(pos[0] + dpos[0], pos[1] + dpos[1]);
        gstate.current.path.closepath();

        // Determine current point shift in user space coordinates
        double[] showShift = shiftPos(currentFont.getWidth(charNames), 0,
        		scaling, angle);
        showShift = gstate.current.CTM.idtransform(showShift);
        gstate.current.rmoveto(showShift[0], showShift[1]);
    }
    
    /**
     * Shows text in the output.
     * 
     * @param exp Exporter to which the output will be sent.
     * @param string Text to show.
     * 
     * @return Displacement vector [dx, dy] in user space coordinates
     * 
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws ProgramError Program can not continue execution.
     * @throws PSError A PostScript error occurred.
     */
    public final double[] showText(final OutputDevice exp,
    		final PSObjectString string)
    		throws PSError, IOException, ProgramError {
        return showText(exp, string, false);
    }
    
    /**
     * Shows text in the output.
     * 
     * @param exp Exporter to which the output will be sent
     * @param string Text to show.
     * @param noOutput Indicates whether the text must actually be written to
     *                 the output.
     * 
     * @return Displacement vector [dx, dy] in user space coordinates
     * 
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws ProgramError Program can not continue execution.
     * @throws PSError A PostScript error occurred.
     */
    public final double[] showText(final OutputDevice exp,
    		final PSObjectString string, final boolean noOutput) 
            throws PSError, IOException, ProgramError {        
        PSObjectFont currentFont = gstate.current.font;
        
        Rule replaceRule = textReplace.findReplacement(string.toString());
        String texRefPoint = "cc";
        String psRefPoint = "cc";
        if (replaceRule != null) {
        	texRefPoint = replaceRule.getTexRefPoint();
        	psRefPoint = replaceRule.getPsRefPoint();
        }
        
        PSObjectArray charNames = string.decode(currentFont.getEncoding());
        String text = currentFont.charNames2texStrings(charNames);
        
        double angle = gstate.current.CTM.getRotation();
        
        // Calculate scaling and font size in points (= 1/72 inch)
        double scaling = gstate.current.CTM.getMeanScaling();
        double fontsize = currentFont.getFontSize()
        					* gstate.current.getMeanUserScaling();

        // Draw text
        if (!noOutput) {
            BoundingBox bbox = currentFont.getBBox(charNames);

            double[] pos = gstate.current.getCurrentPosInDeviceSpace();
            double[] dpos;

            // Print red dots for the bounding box of the text. This is
            // extremely useful for debugging purposes, and should therefore
            // not be deleted.
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

            dpos = getAnchor(psRefPoint, bbox, scaling, angle);
            double[] textPos = new double[2];
            textPos[0] = pos[0] + dpos[0];
            textPos[1] = pos[1] + dpos[1];
            
            if (replaceRule == null) {
            	exp.show(text, textPos, angle, fontsize, texRefPoint);
            } else {
            	exp.show(replaceRule.getTexText(), textPos,
            			angle + replaceRule.getRotation(), Double.NaN,
            			texRefPoint);
            }
        }

        // Determine current point shift in user space coordinates
        double[] showShift = shiftPos(currentFont.getWidth(charNames), 0,
        		scaling, angle);
        showShift = gstate.current.CTM.idtransform(showShift);
        
        return showShift;
    }
    
    /**
     * Determine the position of an anchor relative to the current position.
     * @param pAnchor Follows psfrag. A combination of two characters that
     *                describe vertical and horizontal alignment. Vertical
     *                alignment: t - top, c - center, B - baselinem b - bottom
     *                and horizontal alignment: l - left, c - center, r - right
     *                If either letter is omitted then c is assumed. If anchor
     *                is completely empty, then "Bl" is assumed.
     * @param unitBbox Text bounding box normalized to 1pt.
     * @param scaling Scaling factor for bounding box. E.g. for 12pt font size,
     *                scaling = 12
     * @param angle Text rotation in degrees
     * 
     * @return Coordinates of anchor.
     */
    public final double[] getAnchor(final String pAnchor,
    		final BoundingBox unitBbox, final double scaling,
    		final double angle) {
    	String anchor;
    	if (pAnchor.length() == 0) {
    		anchor = "Bl";
    	} else {
    		anchor = pAnchor;
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
            y = (unitBbox.getUpperRightY() + unitBbox.getLowerLeftY()) / 2.0;
        }
        
        // Horizontal alignment
        if (anchor.contains("l")) {
            x = unitBbox.getLowerLeftX();
        } else if (anchor.contains("r")) {
            x = unitBbox.getUpperRightX();
        } else {
            x = (unitBbox.getLowerLeftX() + unitBbox.getUpperRightX()) / 2.0;
        }
        
        return shiftPos(x, y, scaling, angle);
    }
    
    /**
     * Scale and rotate the translation vector {dx, dy}.
     * @param dx Delta x shift (before scaling and rotation)
     * @param dy Delta y shift (before scaling and rotation)
     * @param scaling Scaling for shift
     * @param pAngle Angle (in degrees) for rotation
     * @return New translation vector
     */
    final double[] shiftPos(final double dx, final double dy,
    		final double scaling, final double pAngle) {
    	double angle = Math.toRadians(pAngle);
        double[] newPos = new double[2];
        newPos[0] = scaling * (dx * Math.cos(angle) - dy * Math.sin(angle));
        newPos[1] = scaling * (dx * Math.sin(angle) + dy * Math.cos(angle));
        return newPos;        
    }
    
}
