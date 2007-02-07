/*
 * Exporter.java
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

package eps2pgf.output;

import java.io.*;

import eps2pgf.postscript.*;
import eps2pgf.postscript.errors.*;

/**
 * Interface for exporters (e.g. Pgf and Tikz)
 * @author Paul Wagenaars
 */
public interface Exporter {
    
    /**
     * Initialize before any other methods are called. Normally, this method
     * writes a header.
     */
    public void init() throws IOException;
    
    /**
     * Finilize writing. Normally, this method writes a footer.
     */
    public void finish() throws IOException;
    
    /**
     * Implements PostScript clip operator
     * Intersects the area inside the current clipping path with the area
     * inside the current path to produce a new, smaller clipping path.
     */
    public void clip(GraphicsState gstate) throws IOException;

    /**
     * Implements PostScript stroke operator
     */
    public void stroke(GraphicsState gstate) throws IOException;
    
    /**
     * Implements PostScript operator setlinecap
     */
    public void setlinecap(int cap) throws PSError, IOException;
    
    /**
     * Implements PostScript operator setlinejoin
     */
    public void setlinejoin(int join) throws PSError, IOException;
    
    /**
     * Starts a new scope
     */
    public void startScope() throws IOException;
    
    /**
     * Sets the current color in Red-Green-Blue (RGB)
     */
    public void setColor(double r, double g, double b) throws IOException;

    /**
     * Sets the current color in gray
     */
    public void setColor(double level) throws IOException;
}
