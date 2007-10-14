/*
 * PGFExport.java
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

import java.io.*;
import java.text.*;
import java.util.*;

import net.sf.eps2pgf.postscript.*;
import net.sf.eps2pgf.postscript.errors.*;

/**
 * Writes PGF files.
 * @author Paul Wagenaars
 */
public class PGFDevice implements OutputDevice {
    // Coordinate format (used to format X- and Y-coordinates)
    static final DecimalFormat coorFormat = new DecimalFormat("#.###", 
            new DecimalFormatSymbols(Locale.US));
    
    // Length format (used to format linewidth, dash, etc...)
    static final DecimalFormat lengthFormat = new DecimalFormat("#.###", 
            new DecimalFormatSymbols(Locale.US));
    
    // Font size format (used to set fontsize in pt)
    static final DecimalFormat fontSizeFormat = new DecimalFormat("#.##",
            new DecimalFormatSymbols(Locale.US));
    
    // Colors (in range from 0.0 to 1.0) has at least 16-bit per channel accuracy
    static final DecimalFormat colorFormat = new DecimalFormat("#.######",
            new DecimalFormatSymbols(Locale.US));
    
    static int scopeDepth = 0;
    
    static Writer out;
    
    /**
     * Creates a new instance of PGFExport
     * @param wOut Writer to where the PGF code will be written.
     */
    public PGFDevice(Writer wOut) {
        out = wOut;
    }
    
    /**
     * Returns a copy of the default transformation matrix (default CTM).
     * In this device the coordinates are expressed in micrometers.
     */
    public PSObjectMatrix defaultCTM() {
        return new PSObjectMatrix(25.4*1000/72.0, 0 ,0, 25.4*1000/72.0, 0, 0);
    }
    
    /**
     * Writes header.
     * @throws java.io.IOException Signals that an I/O exception of some sort has occurred.
     */
    public void init(GraphicsState gstate) throws PSError, IOException {
        gstate.deviceData.setKey("pgf_last_linewidth", new PSObjectReal(Double.NaN));
        gstate.deviceData.setKey("pgf_last_dashpattern", new PSObjectArray());
        gstate.deviceData.setKey("pgf_last_dashoffset", new PSObjectReal(0.0));
        
        out.write("% Created by " + net.sf.eps2pgf.Main.getNameVersion() + " ");
        Date now = new Date();
        out.write("on " + now  + "\n");
        out.write("\\begin{pgfpicture}\n");
    }
    
    /**
     * Writes the footer.
     * @throws java.io.IOException Signals that an I/O exception of some sort has occurred.
     */
    public void finish()  throws IOException {
        for (int i = 0 ; i < scopeDepth ; i++) {
            out.write("\\end{pgfscope}\n");
        }
        out.write("\\end{pgfpicture}\n");
    }
    
    /**
     * Convert a Path to pgf code and write in to the output
     */
    void writePath(Path path) throws IOException, PSErrorUnimplemented {
        for (int i = 0 ; i < path.sections.size() ; i++) {
            PathSection section = path.sections.get(i);
            if (section instanceof Moveto) {
                // If the path ends with a moveto, the moveto is ignored.
                if (i < (path.sections.size()-1)) {
                    String x = coorFormat.format(1e-4*section.params[0]);
                    String y = coorFormat.format(1e-4*section.params[1]);
                    out.write("\\pgfpathmoveto{\\pgfpoint{" + x + "cm}{" + y + "cm}}");
                }
            } else if (section instanceof Lineto) {
                String x = coorFormat.format(1e-4*section.params[0]);
                String y = coorFormat.format(1e-4*section.params[1]);
                out.write("\\pgfpathlineto{\\pgfpoint{" + x + "cm}{" + y + "cm}}");
            } else if (section instanceof Curveto) {
                String x1 = coorFormat.format(1e-4*section.params[0]);
                String y1 = coorFormat.format(1e-4*section.params[1]);
                String x2 = coorFormat.format(1e-4*section.params[2]);
                String y2 = coorFormat.format(1e-4*section.params[3]);
                String x3 = coorFormat.format(1e-4*section.params[4]);
                String y3 = coorFormat.format(1e-4*section.params[5]);
                out.write("\\pgfpathcurveto");
                out.write("{\\pgfpoint{" + x1 + "cm}{" + y1 + "cm}}");
                out.write("{\\pgfpoint{" + x2 + "cm}{" + y2 + "cm}}");
                out.write("{\\pgfpoint{" + x3 + "cm}{" + y3 + "cm}}");
            } else if (section instanceof Closepath) {
                out.write("\\pgfpathclose");
            } else {
                throw new PSErrorUnimplemented("Can't handle " + section.getClass().getName());
            }
        }
        out.write("\n");
    }
    
    /**
     * Implements PostScript stroke operator.
     * @param path Path to stroke
     * @throws java.io.IOException Unable to write output
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorUnimplemented Encountered an unimplemented path section
     */
    public void stroke(GraphicsState gstate) throws IOException, PSError {
        updateDash(gstate);
        updateLinewidth(gstate);
        writePath(gstate.path);
        out.write("\\pgfusepath{stroke}\n");
    }
    
    /**
     * Set the current clipping path in the graphics state as clipping path
     * in the output document.
     * @param clipPath Path to use for clipping
     * @throws java.io.IOException Unable to write output
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorUnimplemented Encountered an unimplemented path element
     */
    public void clip(Path clipPath) throws IOException, PSErrorUnimplemented {
        writePath(clipPath);
        out.write("\\pgfusepath{clip}\n");
    }
    
    /**
     * Fills a path using the non-zero rule
     * See the PostScript manual (fill operator) for more info.
     * @param path Path to use for filling
     * @throws java.io.IOException Unable to write output
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorUnimplemented Encountered a path element type that is not yet implemented
     */
    public void fill(Path path) throws IOException, PSErrorUnimplemented {
        writePath(path);
        out.write("\\pgfusepath{fill}\n");
    }
        
    /**
     * Set the current clipping path in the graphics state as clipping path
     * in the output document. The even-odd rule is used to determine which point
     * are inside the path.
     * @param clipPath Path to use for clipping
     * @throws java.io.IOException Unable to write output
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorUnimplemented Encountered an unimplemented path element
     */
    public void eoclip(Path clipPath) throws IOException, PSErrorUnimplemented {
        writePath(clipPath);
        out.write("\\pgfseteorule\\pgfusepath{clip}\\pgfsetnonzerorule\n");
    }
    
    /**
     * Fills a path using the even-odd rule
     * See the PostScript manual (eofill operator) for more info.
     * @param path Path to use for eofill
     * @throws java.io.IOException Unable to write output
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorUnimplemented Encountered a path element type that has not yet been implemented
     */
    public void eofill(Path path) throws IOException, PSErrorUnimplemented {
        writePath(path);
        out.write("\\pgfseteorule\\pgfusepath{fill}\\pgfsetnonzerorule\n");
    }
    
    /**
     * Shading fill (shfill PostScript operator)
     */
    public void shfill(PSObjectDict dict, GraphicsState gstate) throws PSError, IOException {
        Shading shading = Shading.newShading(dict);
        if (shading instanceof RadialShading) {
            radialShading((RadialShading)shading, gstate);
        } else {
            throw new PSErrorUnimplemented("Shading of this type " + shading);
        }
    }
    
    /**
     * Create a radial shading
     */
    void radialShading(RadialShading shading, GraphicsState gstate) throws IOException, 
            PSErrorRangeCheck, PSErrorUnimplemented, PSErrorTypeCheck {
        // Convert coordinates and radii from user space to coordinate space
        // PGF does not support the Extend parameters for shadings. So we
        // try to emulate the effect.
        double scaling = gstate.CTM.getMeanScaling();
        double xScale = gstate.CTM.getXScaling() / scaling;
        double yScale = gstate.CTM.getYScaling() / scaling;
        double angle = gstate.CTM.getRotation();
        double[] coor0 = gstate.CTM.transform(shading.getCoord(0.0));
        double[] coor1 = gstate.CTM.transform(shading.getCoord(1.0));

        double max_s = 1.0;
        if (shading.extend1) {
            // Find the s value for which the radius is big (0.3 metres ~ a4 paper)
            max_s = shading.getSForDistance(0.3*1e6/scaling, 1, Double.POSITIVE_INFINITY);
            coor1 = gstate.CTM.transform(shading.getCoord(max_s));
        }
        
        startScope();
        out.write("\\pgfdeclareradialshading{eps2pgfshading}{\\pgfpoint{");
        out.write(coorFormat.format(1e-4*(coor0[0]-coor1[0])/xScale) + "cm}{");
        out.write(coorFormat.format(1e-4*(coor0[1]-coor1[1])/yScale) + "cm}}{");
        double[] sFit = shading.fitLinearSegmentsOnColor(0.01);        
        for (int i = 0 ; i < sFit.length ; i++) {
            if (i > 0) {
                out.write(";");
            }
            double r = scaling*shading.getRadius(sFit[i]);
            double[] color = shading.getColor(sFit[i]);
            out.write("rgb(" + lengthFormat.format(1e-4*r) + "cm)=");
            out.write("(" + colorFormat.format(color[0]));
            out.write("," + colorFormat.format(color[1]));
            out.write("," + colorFormat.format(color[2]) + ")");
        }
        if (max_s > 1.0) {
            double r = scaling*shading.getRadius(max_s);
            double[] color = shading.getColor(1.0);
            out.write(";rgb(" + lengthFormat.format(1e-4*r) + "cm)=");
            out.write("(" + colorFormat.format(color[0]));
            out.write("," + colorFormat.format(color[1]));
            out.write("," + colorFormat.format(color[2]) + ")");
        }
        out.write("}");
        out.write("\\pgflowlevelobj{");
        out.write("\\pgftransformshift{\\pgfpoint{");
        out.write(lengthFormat.format(1e-4*coor1[0]) + "cm}{");
        out.write(lengthFormat.format(1e-4*coor1[1]) + "cm}}");
        if (angle != 0) {
            out.write("\\pgftransformrotate{"+coorFormat.format(angle)+"}");
        }
        if (xScale != 1) {
            out.write("\\pgftransformxscale{" + xScale + "}");
        }
        if (yScale != 1) {
            out.write("\\pgftransformyscale{" + yScale + "}");
        }
        out.write("}{\\pgfuseshading{eps2pgfshading}}");
        endScope();
    }
    
    /**
     * Implements PostScript operator setlinecap
     * @param cap Cap type (see PostScript manual for info)
     * @throws java.io.IOException Unable to write output
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorRangeCheck Invalid cap type
     */
    public void setlinecap(int cap) throws IOException, PSErrorRangeCheck {
        switch (cap) {
            case 0:
                out.write("\\pgfsetbuttcap\n");
                break;
            case 1:
                out.write("\\pgfsetroundcap\n");
                break;
            case 2:
                out.write("\\pgfsetrectcap\n");
                break;
            default:
                throw new PSErrorRangeCheck();
        }
    }
    
    /**
     * Implements PostScript operator setlinejoin
     * @param join Join type (see PostScript manual)
     * @throws java.io.IOException Unable to write output
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorRangeCheck Invalid join type
     */
    public void setlinejoin(int join) throws IOException, PSErrorRangeCheck {
        switch (join) {
            case 0:
                out.write("\\pgfsetmiterjoin\n");
                break;
            case 1:
                out.write("\\pgfsetroundjoin\n");
                break;
            case 2:
                out.write("\\pgfsetbeveljoin\n");
                break;
            default:
                throw new PSErrorRangeCheck();
        }        
    }
    
    /**
     * Implements PostScript operator setdash
     */
    void updateDash(GraphicsState gstate) throws IOException, PSError {
        String lastPattern = gstate.deviceData.get("pgf_last_dashpattern").isis();
        double lastOffset = gstate.deviceData.get("pgf_last_dashoffset").toReal();
        
        double scaling = gstate.CTM.getMeanScaling();
        PSObjectArray currentArray = new PSObjectArray();
        for (int i = 0 ; i < gstate.dashpattern.size() ; i++) {
            currentArray.addToEnd(new PSObjectReal(gstate.dashpattern.get(i).toReal() * scaling));
        }
        String currentPattern = currentArray.isis();
        double currentOffset = gstate.dashoffset * scaling;
        
        if ( !currentPattern.equals(lastPattern) || (lastOffset != currentOffset) ) {
            out.write("\\pgfsetdash{");
            try {
                int i = 0;
                while(true) {
                    out.write("{" + lengthFormat.format(1e-4*currentArray.get(i++).toReal()) + "cm}");
                }
            } catch (PSErrorRangeCheck e) {
                
            } finally {
                out.write("}{" + lengthFormat.format(1e-4*currentOffset) + "cm}\n");
            }
            gstate.deviceData.setKey("pgf_last_dashpattern", currentArray);
            gstate.deviceData.setKey("pgf_last_dashoffset", new PSObjectReal(currentOffset));
        }
    }
    
    /**
     * Compares the current linewidth with the last-used linewidth. If they
     * are different the new linewidth is set in the output.
     */
    void updateLinewidth(GraphicsState gstate) throws PSError, IOException {
        double lastWidth = gstate.deviceData.get("pgf_last_linewidth").toReal();
        double currentWidth = gstate.linewidth * gstate.CTM.getMeanScaling();
        if (currentWidth != lastWidth) {
            out.write("\\pgfsetlinewidth{"+ lengthFormat.format(1e-3*currentWidth) +"mm}\n");
            gstate.deviceData.setKey("pgf_last_linewidth", new PSObjectReal(currentWidth));
        }
    }
    
    /**
     * Sets the miter limit
     */
    public void setmiterlimit(double miterLimit) throws IOException {
        out.write("\\pgfsetmiterlimit{" + miterLimit + "}\n");
    }
    
   /**
     * Starts a new scope
     * @throws java.io.IOException Unable to write output
     */
    public void startScope() throws IOException {
        out.write("\\begin{pgfscope}\n");
        scopeDepth++;
    }
    
    /**
     * Ends the current scope scope
     * @throws java.io.IOException There was an error write to the output
     */
    public void endScope() throws IOException {
        if (scopeDepth > 0) {
            out.write("\\end{pgfscope}\n");
            scopeDepth--;
        }
    }

    /**
     * Sets the current color in gray, RGB or CMYK
     * @param colorLevels Depending on the length of the array. If it has one
     * parameter it is a gray value, if it has three parameters it are RGB values
     * and if it has four parameters it are CMYK values.
     */
    public void setColor(double[] colorLevels) throws IOException {
        for (int i = 0 ; i < colorLevels.length ; i++) {
            colorLevels[i] = Math.max(Math.min(colorLevels[i], 1.0), 0.0);
        }
        if (colorLevels.length >= 4) {
            out.write("\\definecolor{eps2pgf_color}{cmyk}{"
                    + colorFormat.format(colorLevels[0])
                    + "," + colorFormat.format(colorLevels[1])
                    + "," + colorFormat.format(colorLevels[2])
                    + "," + colorFormat.format(colorLevels[3]) + "}");
        } else if (colorLevels.length == 3) {
            out.write("\\definecolor{eps2pgf_color}{rgb}{"
                    + colorFormat.format(colorLevels[0])
                    + "," + colorFormat.format(colorLevels[1])
                    + "," + colorFormat.format(colorLevels[2]) + "}");
        } else {
            out.write("\\definecolor{eps2pgf_color}{gray}{"
                    + colorFormat.format(colorLevels[0]) + "}");
        }
        
        out.write("\\pgfsetstrokecolor{eps2pgf_color}");
        out.write("\\pgfsetfillcolor{eps2pgf_color}\n");
    }

    /**
     * Draws text
     * @param text Exact text to draw
     * @param position Text anchor point in [micrometer, micrometer]
     * @param angle Text angle in degrees
     * @param fontsize in PostScript pt (= 1/72 pt)
     * @param anchor String with two characters:
     *               t - top, c - center, B - baseline b - bottom
     *               l - left, c - center, r - right
     *               e.g. Br = baseline,right
     * @throws java.io.IOException Unable to write output
     */
    public void show(String text, double[] position, double angle,
            double fontsize, String anchor) throws IOException {
        String x = coorFormat.format(1e-4*position[0]);
        String y = coorFormat.format(1e-4*position[1]);
        
        // Process anchor
        String posOpts = "";
        // Vertical alignment
        if (anchor.contains("t")) {
            posOpts = "top,";
        } else if (anchor.contains("B")) {
            posOpts = "base,";
        } else if (anchor.contains("b")) {
            posOpts = "bottom,";
        }
        
        // Horizontal alignment
        if (anchor.contains("l")) {
            posOpts += "left,";
        } else if (anchor.contains("r")) {
            posOpts += "right,";
        }
        
        // Convert fontsize in PostScript pt to TeX pt
        fontsize = fontsize / 72 * 72.27;
        
        // The angle definition in PostScript is just the other way around
        // as is pgf.
        String angStr = lengthFormat.format(angle);
        
        text = "{\\fontsize{" + fontSizeFormat.format(fontsize) + "}{" 
                + fontSizeFormat.format(1.2*fontsize) + "}\\selectfont" + text + "}";
        out.write(String.format("\\pgftext[%sx=%scm,y=%scm,rotate=%s]{%s}\n",
                posOpts, x, y, angStr, text));
    }
    
    /**
     * Draws a red dot (usefull for debugging, don't use otherwise)
     * @param x X-coordinate (micrometer)
     * @param y Y-coordinate (micrometer)
     * @throws java.io.IOException Unable to write output
     */
    public void drawDot(double x, double y) throws IOException {
        out.write("\\begin{pgfscope}\\pgfsetfillcolor{red}\\pgfpathcircle{\\pgfpoint{"
                + 1e-4*x + "cm}{" + 1e-4*y + "cm}}{0.5pt}\\pgfusepath{fill}\\end{pgfscope}\n");
    }
    
    /**
     * Draws a blue rectangle (usefull for debugging, don't use otherwise)
     * @param lowerLeft X- and Y-coordinate (in micrometer) of lower left corner
     * @param upperRight X- and Y-coordinate (in micrometer) of upper right corner
     * @throws java.io.IOException Unable to write output
     */
    public void drawRect(double[] lowerLeft, double[] upperRight) throws IOException {
        out.write("\\begin{pgfscope}\\pgfsetstrokecolor{blue}\\pgfsetlinewidth{0.1pt}\\pgfpathrectangle{\\pgfpoint{"
                + 1e-4*lowerLeft[0] + "cm}{" + 1e-4*lowerLeft[1] + "cm}}{\\pgfpoint{"
                + 1e-4*(upperRight[0]-lowerLeft[0]) +"cm}{" + 1e-4*(upperRight[1]-lowerLeft[1])
                + "cm}}\\pgfusepath{stroke}\\end{pgfscope}\n");
    }

}
