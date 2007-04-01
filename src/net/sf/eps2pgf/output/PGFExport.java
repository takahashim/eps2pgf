/*
 * PGFExport.java
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

package net.sf.eps2pgf.output;

import java.io.*;
import java.text.*;
import java.util.*;

import net.sf.eps2pgf.postscript.*;
import net.sf.eps2pgf.postscript.errors.*;

/**
 * Writes PGF files.
 * @author Paul Wagenaars
 */
public class PGFExport implements Exporter {
    // Coordinate format (used to format X- and Y-coordinates)
    static final DecimalFormat coorFormat = new DecimalFormat("#.##", 
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
    
    int scopeDepth = 0;
    
    Writer out;
    
    /**
     * Creates a new instance of PGFExport
     * @param wOut Writer to where the PGF code will be written.
     */
    public PGFExport(Writer wOut) {
        out = wOut;
    }
    
    /**
     * Writes header.
     * @throws java.io.IOException Signals that an I/O exception of some sort has occurred.
     */
    public void init() throws IOException {
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
    public void stroke(Path path) throws IOException, PSErrorUnimplemented {
        writePath(path);
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
    public void shfill(PSObjectDict dict, GraphicsState gstate) throws PSErrorTypeCheck, 
            PSErrorUnimplemented, PSErrorRangeCheck, IOException {
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
            PSErrorRangeCheck, PSErrorUnimplemented {
        // Convert coordinates and radii from user space to coordinate space
        // PGF does not support the Extend parameters for shadings. So we
        // try to emulate the effect.
        double scaling = gstate.CTM.getMeanScaling();
        double xScale = gstate.CTM.getXScaling();
        double yScale = gstate.CTM.getYScaling();
        double angle = gstate.CTM.getRotation();
        double[] coor0 = gstate.CTM.apply(shading.getCoord(0.0));
        double[] coor1 = gstate.CTM.apply(shading.getCoord(1.0));

        double max_s = 1.0;
        if (shading.extend1) {
            // Find the s value for which the radius is big (0.3 metres ~ a4 paper)
            max_s = shading.getSForDistance(0.3*1e6/scaling, 1, Double.POSITIVE_INFINITY);
            coor1 = gstate.CTM.apply(shading.getCoord(max_s));
        }
        
        startScope();
        out.write("\\pgfdeclareradialshading{eps2pgfshading}{\\pgfpoint{");
        out.write(coorFormat.format(1e-4*(coor1[0]-coor0[0])) + "cm}{");
        out.write(coorFormat.format(1e-4*(coor1[1]-coor0[1])) + "cm}}{");
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
        out.write(lengthFormat.format(1e-4*coor0[0]) + "cm}{");
        out.write(lengthFormat.format(1e-4*coor0[1]) + "cm}}");
        if (angle != 0) {
            out.write("\\pgftransformrotate{"+coorFormat.format(-angle)+"}");
        }
        if (xScale != scaling) {
            out.write("\\pgftransformxscale{" + xScale/scaling + "}");
        }
        if (yScale != scaling) {
            out.write("\\pgftransformyscale{" + yScale/scaling + "}");
        }
        out.write("}{\\pgfuseshading{eps2pgfshading}}");
        endScope();
    }
    
    /**
     * Implements PostScript operator setdash
     * @param array Array with dash pattern (see PostScript manual for definition)
     * @param offset Dash pattern offset (see PostScript manual for info)
     * @throws java.io.IOException Unable to write output
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorTypeCheck One or more element in the array is not a number
     */
    public void setDash(PSObjectArray array, double offset) throws IOException, PSErrorTypeCheck {
        out.write("\\pgfsetdash{");
        
        try {
            int i = 0;
            while(true) {
                out.write("{" + lengthFormat.format(1e-4*array.get(i++).toReal()) + "cm}");
            }
        } catch (PSErrorRangeCheck e) {
                
        } finally {
            out.write("}{" + lengthFormat.format(1e-4*offset) + "cm}\n");
        }
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
     * Implements PostScript operator setlinewidth
     * @param lineWidth Line width in micrometer
     * @throws java.io.IOException Unable to write output
     */
    public void setlinewidth(double lineWidth) throws IOException {
        lineWidth = Math.abs(lineWidth);
        out.write("\\pgfsetlinewidth{"+ lengthFormat.format(1e-3*lineWidth) +"mm}\n");
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
     * Sets the current color
     * @param r Red value
     * @param g Green value
     * @param b Blue value
     * @throws java.io.IOException Unable to write output
     */
    public void setColor(double r, double g, double b) throws IOException {
        r = Math.max(Math.min(r, 1.0), 0.0);
        g = Math.max(Math.min(g, 1.0), 0.0);
        b = Math.max(Math.min(b, 1.0), 0.0);
        out.write("\\definecolor{eps2pgf_color}{rgb}{" + colorFormat.format(r) +
                "," + colorFormat.format(g) + "," + colorFormat.format(b) + "}");
        out.write("\\pgfsetstrokecolor{eps2pgf_color}");
        out.write("\\pgfsetfillcolor{eps2pgf_color}\n");
    }

    /**
     * Sets the current color in gray
     * @param level Grayscale level
     * @throws java.io.IOException Unable to write output
     */
    public void setColor(double level) throws IOException {
        level = Math.max(Math.min(level, 1.0), 0.0);
        out.write("\\definecolor{eps2pgf_color}{gray}{" + colorFormat.format(level) + "}");
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
                + 1e-4*x + "cm}{" + 1e-4*y + "cm}}{0.25pt}\\pgfusepath{fill}\\end{pgfscope}\n");
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
