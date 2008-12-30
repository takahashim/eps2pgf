/*
 * This file is part of Eps2pgf.
 *
 * Copyright 2007-2009 Paul Wagenaars <paul@wagenaars.org>
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

package net.sf.eps2pgf.ps.resources.outputdevices;

import java.io.IOException;

import net.sf.eps2pgf.ProgramError;
import net.sf.eps2pgf.ps.GraphicsState;
import net.sf.eps2pgf.ps.Image;
import net.sf.eps2pgf.ps.Path;
import net.sf.eps2pgf.ps.errors.PSError;
import net.sf.eps2pgf.ps.errors.PSErrorUnregistered;
import net.sf.eps2pgf.ps.objects.PSObjectDict;
import net.sf.eps2pgf.ps.objects.PSObjectMatrix;

/**
 * Interface for exporters (e.g. PGF and TikZ)
 * @author Paul Wagenaars
 */
public interface OutputDevice extends Cloneable {
    
    /**
     * Implements PostScript clip operator.
     * Intersects the area inside the current clipping path with the area
     * inside the current path to produce a new, smaller clipping path.
     * 
     * @param clipPath the clip path
     * 
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws PSErrorUnregistered the PS error unregistered
     */
    void clip(Path clipPath)
            throws IOException, PSErrorUnregistered;
    
    /**
     * Returns a exact deep copy of this output device.
     * 
     * @return Deep copy of this object.
     */
    OutputDevice clone();
    
    /**
     * Returns a <b>copy</b> default transformation matrix (converts user space
     * coordinates to device space).
     * 
     * @return Default transformation matrix.
     */
    PSObjectMatrix defaultCTM();
    
    /**
     * Initialize before any other methods are called. Normally, this method
     * writes a header.
     * 
     * @throws PSError A PostScript error occurred.
     * @throws IOException Signals that an I/O exception has occurred.
     */
    void init() throws PSError, IOException;
    
    /**
     * Finalize writing. Normally, this method writes a footer.
     * 
     * @throws IOException Signals that an I/O exception has occurred.
     */
    void finish() throws IOException;
    
    /**
     * Fills a path using the non-zero rule.
     * See the PostScript manual (fill operator) for more info.
     * 
     * @param gstate Current graphics state.
     * 
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws PSError A PostScript error occurred.
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    void fill(GraphicsState gstate) throws IOException, PSError, ProgramError;

    /**
     * Set the current clipping path in the graphics state as clipping path in
     * the output document. The even-odd rule is used to determine which point
     * are inside the path.
     * 
     * @param gstate Current graphics state.
     * 
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws PSError A PostScript error occurred.
     */
    void eoclip(GraphicsState gstate) throws IOException, PSError;
    
    /**
     * Fills a path using the even-odd rule.
     * See the PostScript manual (fill operator) for more info.
     * 
     * @param gstate The current graphics state.
     * 
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws PSError A PostScript error occurred.
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    void eofill(GraphicsState gstate) throws IOException, PSError, ProgramError;
    
    /**
     * Internal Eps2pgf command: eps2pgfgetmetrics
     * It is meant for the cache device. When this command is issued, it will
     * return metrics information about the drawn glyph.
     * 
     * @return Metrics information about glyph.
     */
    double[] eps2pgfGetMetrics();
    
    /**
     * Shading fill (shfill PostScript operator).
     * 
     * @param dict Shading to use.
     * @param gstate Current graphics state.
     * 
     * @throws PSError A PostScript error occurred.
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    void shfill(PSObjectDict dict, GraphicsState gstate)
            throws PSError, IOException, ProgramError;

    /**
     * Implements PostScript stroke operator.
     * 
     * @param gstate Current graphics state.
     * 
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws PSError A PostScript error occurred.
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    void stroke(GraphicsState gstate) throws IOException, PSError, ProgramError;
    
    /**
     * Draws a red dot (useful for debugging, don't use otherwise).
     * 
     * @param x X-coordinate of dot.
     * @param y Y-coordinate of dot.
     * 
     * @throws IOException Signals that an I/O exception has occurred.
     */
    void drawDot(double x, double y) throws IOException;
    
    /**
     * Draws a blue rectangle (useful for debugging, don't use otherwise).
     * 
     * @param lowerLeft Lower-left coordinate.
     * @param upperRight Upper-right coordinate.
     * 
     * @throws IOException Signals that an I/O exception has occurred.
     */
    void drawRect(double[] lowerLeft, double[] upperRight) throws IOException;

    /**
     * Draws text.
     * 
     * @param text Exact text to draw
     * @param position Text anchor point in [micrometer, micrometer]
     * @param angle Text angle in degrees
     * @param fontsize in PostScript pt (= 1/72 inch). If fontsize is NaN, the
     * font size is not set and completely determined by LaTeX.
     * @param anchor String with two characters:
     * t - top, c - center, B - baseline b - bottom
     * l - left, c - center, r - right
     * e.g. Br = baseline,right
     * @param gstate Current graphics state.
     * 
     * @throws IOException Unable to write output
     * @throws PSError A PostScript error occurred.
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    void show(String text, double[] position, double angle,
            double fontsize, String anchor, final GraphicsState gstate)
            throws IOException, PSError, ProgramError;

    /**
     * Starts a new scope.
     * 
     * @throws IOException Signals that an I/O exception has occurred.
     */
    void startScope() throws IOException;
    
    /**
     * Ends the current scope.
     * 
     * @throws IOException Signals that an I/O exception has occurred.
     */
    void endScope() throws IOException;
    
    /**
     * Adds a bitmap image to the output.
     * 
     * @param img The bitmap image to add.
     * 
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws PSError A PostScript error occurred.
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    void image(final Image img) throws PSError, IOException, ProgramError;
    
}
