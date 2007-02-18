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
     * @param gstate Current graphics state.
     */
    public void stroke(Path path) throws IOException, PSErrorUnimplemented {
        writePath(path);
        out.write("\\pgfusepath{stroke}\n");
    }
    
    /**
     * Set the current clipping path in the graphics state as clipping path
     * in the output document.
     */
    public void clip(Path clipPath) throws IOException, PSErrorUnimplemented {
        writePath(clipPath);
        out.write("\\pgfusepath{clip}\n");
    }
    
    /**
     * Fills a path
     * See the PostScript manual (fill operator) for more info.
     */
    public void fill(Path path) throws IOException, PSErrorUnimplemented {
        writePath(path);
        out.write("\\pgfusepath{fill}\n");
    }
    
    /**
     * Implements PostScript operator setdash
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
     */
    public void setlinewidth(double lineWidth) throws IOException {
        lineWidth = Math.abs(lineWidth);
        out.write("\\pgfsetlinewidth{"+ lengthFormat.format(lineWidth) +"cm}\n");
    }
    
   /**
     * Starts a new scope
     */
    public void startScope() throws IOException {
        out.write("\\begin{pgfscope}\n");
        scopeDepth++;
    }
    
    /**
     * Ends the current scope scope
     */
    public void endScope() throws IOException {
        if (scopeDepth > 0) {
            out.write("\\end{pgfscope}\n");
            scopeDepth--;
        }
    }

    /**
     * Sets the current color
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
     */
    public void setColor(double level) throws IOException {
        level = Math.max(Math.min(level, 1.0), 0.0);
        out.write("\\definecolor{eps2pgf_color}{gray}{" + level + "}");
        out.write("\\pgfsetstrokecolor{eps2pgf_color}");
        out.write("\\pgfsetfillcolor{eps2pgf_color}\n");        
    }
    
    /**
     * Draws text
     */
    public void show(String text, double[] position, double angle) throws IOException {
        String x = coorFormat.format(position[0]);
        String y = coorFormat.format(position[1]);
        out.write("\\pgftext[bottom,left,x="+x+"cm,y="+y+"cm]{" + text + "}\n");
    }

}
