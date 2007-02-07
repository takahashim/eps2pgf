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
import java.text.SimpleDateFormat;
import java.util.Date;
import net.sf.eps2pgf.postscript.GraphicsState;
import net.sf.eps2pgf.postscript.Lineto;
import net.sf.eps2pgf.postscript.Moveto;
import net.sf.eps2pgf.postscript.PathSection;
import net.sf.eps2pgf.postscript.errors.PSError;
import net.sf.eps2pgf.postscript.errors.PSErrorRangeCheck;

/**
 * Writes PGF files.
 * @author Paul Wagenaars
 */
public class PGFExport implements Exporter {
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
        out.write("\\end{pgfpicture}\n");
    }
    
    /**
     * Implements PostScript stroke operator.
     * @param gstate Current graphics state.
     */
    public void stroke(GraphicsState gstate) throws IOException {
        
    }
    
    /**
     * Set the current clipping path in the graphics state as clipping path
     * in the output document.
     */
    public void clip(GraphicsState gstate) throws IOException {
        for (int i = 0 ; i < gstate.clippingPath.sections.size() ; i++) {
            PathSection section = gstate.clippingPath.sections.get(i);
            if (section instanceof Moveto) {
                out.write("\\pgfpathmoveto{"+section.params[0]+"}{"+section.params[1]+"}");
            } else if (section instanceof Lineto) {
                out.write("\\pgfpathlineto{"+section.params[0]+"}{"+section.params[1]+"}");
            }
        }
    }
    
    /**
     * Implements PostScript operator setlinecap
     */
    public void setlinecap(int cap) throws PSError, IOException {
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
    public void setlinejoin(int join) throws PSError, IOException {
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
     * Starts a new scope
     */
    public void startScope() throws IOException {
        out.write("\\begin{pgfscope}\n");
    }
    
    /**
     * Sets the current color
     */
    public void setColor(double r, double g, double b) throws IOException {
        r = Math.max(Math.min(r, 1.0), 0.0);
        g = Math.max(Math.min(g, 1.0), 0.0);
        b = Math.max(Math.min(b, 1.0), 0.0);
        out.write("\\definecolor{eps2pgf_color}{rgb}{" + r + "," + g + "," + b + "}");
        out.write("\\pgfsetfillcolor{eps2pgf_color}\n");
    }

    /**
     * Sets the current color in gray
     */
    public void setColor(double level) throws IOException {
        level = Math.max(Math.min(level, 1.0), 0.0);
        out.write("\\definecolor{eps2pgf_color}{gray}{" + level + "}");
        out.write("\\pgfsetfillcolor{eps2pgf_color}\n");        
    }

}
