/*
 * Exporter.java
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

package net.sf.eps2pgf.output;

import java.io.*;

import net.sf.eps2pgf.postscript.*;
import net.sf.eps2pgf.postscript.errors.*;

/**
 * Interface for exporters (e.g. Pgf and Tikz)
 * @author Paul Wagenaars
 */
public interface OutputDevice {
    
    /**
     * Implements PostScript clip operator
     * Intersects the area inside the current clipping path with the area
     * inside the current path to produce a new, smaller clipping path.
     */
    public void clip(Path clipPath) throws IOException, PSErrorUnimplemented;
    
    /**
     * Retuns a <b>copy</b> default transformation matrix (converts user space
     * coordinates to device space).
     */
    public PSObjectMatrix defaultCTM();
    
    /**
     * Initialize before any other methods are called. Normally, this method
     * writes a header.
     */
    public void init(GraphicsState gstate) throws PSError, IOException;
    
    /**
     * Finilize writing. Normally, this method writes a footer.
     */
    public void finish() throws IOException;
    
    /**
     * Fills a path using the non-zero rule
     * See the PostScript manual (fill operator) for more info.
     */
    public void fill(Path path) throws IOException, PSErrorUnimplemented;

    /**
     * Set the current clipping path in the graphics state as clipping path
     * in the output document. The even-odd rule is used to determine which point
     * are inside the path.
     * @param clipPath Path to use for clipping
     * @throws java.io.IOException Unable to write output
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorUnimplemented Encountered an unimplemented path element
     */
    public void eoclip(Path clipPath) throws IOException, PSErrorUnimplemented;
    
    /**
     * Fills a path using the even-odd rule
     * See the PostScript manual (fill operator) for more info.
     */
    public void eofill(Path path) throws IOException, PSErrorUnimplemented;
    
    /**
     * Shading fill (shfill PostScript operator)
     */
    public void shfill(PSObjectDict dict, GraphicsState gstate) throws PSErrorTypeCheck, 
            PSErrorUnimplemented, PSErrorRangeCheck, PSErrorUndefined, IOException,
            PSErrorInvalidAccess;

    /**
     * Implements PostScript stroke operator
     */
    public void stroke(GraphicsState gstate) throws IOException, PSError;
    
    /**
     * Implements PostScript operator setdash
     */
    public void setDash(PSObjectArray array, double offset) throws IOException,
            PSErrorTypeCheck, PSErrorInvalidAccess;
    
    /**
     * Implements PostScript operator setlinecap
     */
    public void setlinecap(int cap) throws IOException, PSErrorRangeCheck;
    
    /**
     * Implements PostScript operator setlinejoin
     */
    public void setlinejoin(int join) throws IOException, PSErrorRangeCheck;
    
    /**
     * Sets the current color in gray, rgb or cmyk
     */
    public void setColor(double[] colorLevels) throws IOException;
    
    /**
     * Sets the miter limit
     */
    public void setmiterlimit(double num) throws IOException;
    
    /**
     * Starts a new scope
     */
    public void startScope() throws IOException;
    
    /**
     * Ends the current scope scope
     */
    public void endScope() throws IOException;
    
    /**
     * Draws text
     */
    public void show(String text, double[] position, double angle,
            double fontsize, String anchor) throws IOException;
    
    /**
     * Draws a red dot (usefull for debugging, don't use otherwise)
     */
    public void drawDot(double x, double y) throws IOException;
    
    /**
     * Draws a blue rectangle (usefull for debugging, don't use otherwise)
     */
    public void drawRect(double[] lowerLeft, double[] upperRight) throws IOException;
}
