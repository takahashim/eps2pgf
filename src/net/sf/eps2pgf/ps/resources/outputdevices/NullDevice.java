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

import net.sf.eps2pgf.ps.GraphicsState;
import net.sf.eps2pgf.ps.Image;
import net.sf.eps2pgf.ps.Path;
import net.sf.eps2pgf.ps.objects.PSObjectDict;
import net.sf.eps2pgf.ps.objects.PSObjectMatrix;

/**
 * Discards all output written to this device.
 *
 * @author Paul Wagenaars
 */
public class NullDevice implements OutputDevice {
    
    /**
     * Implements PostScript clip operator.
     * Intersects the area inside the current clipping path with the area
     * inside the current path to produce a new, smaller clipping path.
     * 
     * @param clipPath The clip path.
     */
    public void clip(final Path clipPath) {
        
    }
    
    /**
     * Returns a exact deep copy of this output device.
     * 
     * @return Deep copy of this object.
     */
    @Override
    public NullDevice clone() {
        NullDevice copy;
        try {
            copy = (NullDevice) super.clone();
        } catch (CloneNotSupportedException e) {
            /* this exception shouldn't happen. */
            copy = null;
        }
        return copy;
    }

    /**
     * Returns a <b>copy</b> default transformation matrix (converts user space
     * coordinates to device space).
     * 
     * @return the PS object matrix
     */
    public PSObjectMatrix defaultCTM() {
        return new PSObjectMatrix(1, 0 , 0, 1, 0, 0);
    }
    
    /**
     * Initialize before any other methods are called. Normally, this method
     * writes a header.
     */
    public void init() {
        
    }
    
    /**
     * Finalize writing. Normally, this method writes a footer.
     */
    public void finish() {
        
    }
    
    /**
     * Fills a path using the non-zero rule
     * See the PostScript manual (fill operator) for more info.
     * 
     * @param gstate Current graphics state.
     */
    public void fill(final GraphicsState gstate) {
        
    }
    
    /**
     * Implements 'eoclip'.
     * 
     * @param gstate Current graphics state.
     */
    public void eoclip(final GraphicsState gstate) {
    }

    /**
     * Fills a path using the even-odd rule
     * See the PostScript manual (fill operator) for more info.
     * 
     * @param gstate Current graphics state.
     */
    public void eofill(final GraphicsState gstate) {
    }
    
    /**
     * Internal Eps2pgf command: eps2pgfgetmetrics
     * It is meant for the cache device. When this command is issued, it will
     * return metrics information about the drawn glyph.
     * 
     * @return the double[]
     */
    public double[] eps2pgfGetMetrics() {
        double[] dummyData = {0.0, 0.0, 0.0, 0.0, 0.0, 0.0};
        return dummyData;
    }
    
    /**
     * Shading fill (shfill PostScript operator).
     * 
     * @param dict The dict.
     * @param gstate The gstate.
     */
    public void shfill(final PSObjectDict dict, final GraphicsState gstate) {
        
    }

    /**
     * Implements PostScript stroke operator.
     * 
     * @param gstate The gstate.
     */
    public void stroke(final GraphicsState gstate) {
        
    }
    
    /**
     * Starts a new scope.
     */
    public void startScope() {
    }
    
    /**
     * Ends the current scope scope.
     */
    public void endScope() {
    }
    
    /**
     * Draws text.
     * 
     * @param text The text.
     * @param position The position.
     * @param angle The angle.
     * @param fontsize The font size.
     * @param anchor The anchor.
     * @param gstate Current graphics state.
     */
    public void show(final String text, final double[] position,
            final double angle, final double fontsize, final String anchor,
            final GraphicsState gstate) {
        
    }
    
    /**
     * Draws a red dot (usefull for debugging, don't use otherwise).
     * 
     * @param x The x.
     * @param y The y.
     */
    public void drawDot(final double x, final double y) {
        
    }
    
    /**
     * Draws a blue rectangle (usefull for debugging, don't use otherwise).
     * 
     * @param lowerLeft The lower left.
     * @param upperRight The upper right.
     */
    public void drawRect(final double[] lowerLeft, final double[] upperRight) {
        
    }
    
    /**
     * Adds a bitmap image to the output.
     * 
     * @param img The bitmap image to add.
     * 
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public void image(final Image img) throws IOException {
        /* empty block */
    }
}
