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
    // All dimension will be rounded to this precision (in centimetres)
    static final double precision = 0.01;
    
    // Coordinate format (used to format X- and Y-coordinates)
    static final DecimalFormat coorFormat = new DecimalFormat("#.##", 
            new DecimalFormatSymbols(Locale.US));
    
    // Length format (used to format linewidth, dash, etc...)
    static final DecimalFormat lengthFormat = new DecimalFormat("#.###", 
            new DecimalFormatSymbols(Locale.US));
    
    // Font size format (used to set fontsize in pt)
    static final DecimalFormat fontSizeFormat = new DecimalFormat("#.##",
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
                    String x = coorFormat.format(section.params[0]);
                    String y = coorFormat.format(section.params[1]);
                    out.write("\\pgfpathmoveto{\\pgfpoint{" + x + "cm}{" + y + "cm}}");
                }
            } else if (section instanceof Lineto) {
                String x = coorFormat.format(section.params[0]);
                String y = coorFormat.format(section.params[1]);
                out.write("\\pgfpathlineto{\\pgfpoint{" + x + "cm}{" + y + "cm}}");
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
                out.write("{" + lengthFormat.format(array.get(i++).toReal()) + "cm}");
            }
        } catch (PSErrorRangeCheck e) {
                
        } finally {
            out.write("}{" + lengthFormat.format(offset) + "cm}\n");
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
     * @param lineWidth Line width in cm
     * @throws java.io.IOException Unable to write output
     */
    public void setlinewidth(double lineWidth) throws IOException {
        lineWidth = Math.abs(lineWidth);
        out.write("\\pgfsetlinewidth{"+ lengthFormat.format(lineWidth) +"cm}\n");
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
        out.write("\\definecolor{eps2pgf_color}{rgb}{" + r + "," + g + "," + b + "}");
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
        out.write("\\definecolor{eps2pgf_color}{gray}{" + level + "}");
        out.write("\\pgfsetstrokecolor{eps2pgf_color}");
        out.write("\\pgfsetfillcolor{eps2pgf_color}\n");        
    }
    
    /**
     * Draws text
     * @param text Exact text to draw
     * @param position Text anchor point in [cm, cm]
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
        String x = coorFormat.format(position[0]);
        String y = coorFormat.format(position[1]);
        
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
     * @param x X-coordinate (cm)
     * @param y Y-coordinate (cm)
     * @throws java.io.IOException Unable to write output
     */
    public void drawDot(double x, double y) throws IOException {
        out.write("\\begin{pgfscope}\\pgfsetfillcolor{red}\\pgfpathcircle{\\pgfpoint{"
                + x + "cm}{" + y + "cm}}{0.25pt}\\pgfusepath{fill}\\end{pgfscope}\n");
    }
    
    /**
     * Draws a blue rectangle (usefull for debugging, don't use otherwise)
     * @param lowerLeft X- and Y-coordinate (in cm) of lower left corner
     * @param upperRight X- and Y-coordinate (in cm) of upper right corner
     * @throws java.io.IOException Unable to write output
     */
    public void drawRect(double[] lowerLeft, double[] upperRight) throws IOException {
        out.write("\\begin{pgfscope}\\pgfsetstrokecolor{blue}\\pgfsetlinewidth{0.1pt}\\pgfpathrectangle{\\pgfpoint{"
                + lowerLeft[0] + "cm}{" + lowerLeft[1] + "cm}}{\\pgfpoint{"
                + (upperRight[0]-lowerLeft[0]) +"cm}{" + (upperRight[1]-lowerLeft[1])
                + "cm}}\\pgfusepath{stroke}\\end{pgfscope}\n");
    }

}
