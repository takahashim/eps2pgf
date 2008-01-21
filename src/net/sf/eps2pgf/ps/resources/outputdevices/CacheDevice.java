/*
 * CacheDevice.java
 *
 * This file is part of Eps2pgf.
 *
 * Copyright 2007, 2008 Paul Wagenaars <paul@wagenaars.org>
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

import net.sf.eps2pgf.ps.GraphicsState;
import net.sf.eps2pgf.ps.Path;
import net.sf.eps2pgf.ps.errors.PSErrorNoCurrentPoint;
import net.sf.eps2pgf.ps.objects.PSObjectDict;
import net.sf.eps2pgf.ps.objects.PSObjectMatrix;
import net.sf.eps2pgf.ps.resources.colors.PSColor;

/**
 * Cache device, used to create glyphs.
 *
 * @author Paul Wagenaars
 */
public class CacheDevice implements OutputDevice {
    
    /** The user specified wx. */
    private double specifiedWx = 0.0;
    
    /** The user specified wy. */
    private double specifiedWy = 0.0;
    
    /** The user specified llx. */
    private double specifiedLlx = 0.0;
    
    /** The user specified lly. */
    private double specifiedLly = 0.0;
    
    /** The user specified urx. */
    private double specifiedUrx = 0.0;
    
    /** The user specified ury. */
    private double specifiedUry = 0.0;
    
    /** The bounding box of all paths so far. */
    private double[] pathBbox = null;
    
    /**
     * Creates a new cache device and passes glyph width and bounding box
     * information about the glyph that will be created in this device.
     * @param wx wx parameter
     * @param wy wy parameter
     * @param llx llx parameter
     * @param lly lly parameter
     * @param urx urx parameter
     * @param ury ury parameter
     */
    public CacheDevice(final double wx, final double wy, final double llx,
            final double lly, final double urx, final double ury) {
        specifiedWx = wx;
        specifiedWy = wy;
        specifiedLlx = llx;
        specifiedLly = lly;
        specifiedUrx = urx;
        specifiedUry = ury;
    }
    
    /**
     * Implements PostScript clip operator.
     * Intersects the area inside the current clipping path with the area
     * inside the current path to produce a new, smaller clipping path.
     * 
     * @param clipPath the clip path
     */
    public void clip(final Path clipPath) {
    }
    
    /**
     * Returns a <b>copy</b> default transformation matrix (converts user space
     * coordinates to device space).
     * 
     * @return Default transformation matrix.
     */
    public PSObjectMatrix defaultCTM() {
        return new PSObjectMatrix(1, 0 , 0, 1, 0, 0);
    }
    
    /**
     * Initialize before any other methods are called. Normally, this method
     * writes a header.
     * 
     * @param gstate The current graphics state.
     */
    public void init(final GraphicsState gstate) {
    }
    
    /**
     * Internal Eps2pgf command: eps2pgfgetmetrics
     * It is meant for the cache device. When this command is issued, it will
     * return metrics information about the drawn glyph.
     * 
     * @return Metrics information about glyph.
     */
    public double[] eps2pgfGetMetrics() {
        double llx = 0.0;
        double lly = 0.0;
        double urx = 0.0;
        double ury = 0.0;
        if (this.pathBbox != null) {
            llx = Math.max(this.specifiedLlx, this.pathBbox[0]);
            lly = Math.max(this.specifiedLly, this.pathBbox[1]);
            urx = Math.min(this.specifiedUrx, this.pathBbox[2]);
            ury = Math.min(this.specifiedUry, this.pathBbox[3]);
        }
        double[] dummyData = {specifiedWx, specifiedWy, llx, lly, urx, ury};
        return dummyData;
    }
    
    /**
     * Finalize writing. Normally, this method writes a footer.
     */
    public void finish() {
    }
    
    /**
     * Fills a path using the non-zero rule.
     * See the PostScript manual (fill operator) for more info.
     * 
     * @param path the path
     */
    public void fill(final Path path) {
        try {
            this.mergeBbox(path.boundingBox());
        } catch (PSErrorNoCurrentPoint e) {
            // do nothing
        }
    }
    
    /**
     * Set the current clipping path in the graphics state as clipping path in
     * the output document. The even-odd rule is used to determine which point
     * are inside the path.
     * 
     * @param clipPath Path to use for clipping
     */
    public void eoclip(final Path clipPath) {
    }

    /**
     * Fills a path using the even-odd rule.
     * See the PostScript manual (fill operator) for more info.
     * 
     * @param path the path
     */
    public void eofill(final Path path) {
        try {
            this.mergeBbox(path.boundingBox());
        } catch (PSErrorNoCurrentPoint e) {
            // do nothing
        }
    }
    
    /**
     * Merge a new bounding box with the current bounding box. Afterwards
     * this.pathBbox will contain a bounding box that contains both the
     * previous this.pathBbox and the newBbox.
     * 
     * @param newBbox Bounding box to merge with current bounding box.
     */
    void mergeBbox(final double[] newBbox) {
        if (pathBbox == null) {
            pathBbox = newBbox.clone();
        } else {
            pathBbox[0] = Math.min(pathBbox[0], newBbox[0]);
            pathBbox[1] = Math.min(pathBbox[1], newBbox[1]);
            pathBbox[2] = Math.max(pathBbox[2], newBbox[2]);
            pathBbox[3] = Math.max(pathBbox[3], newBbox[3]);
        }
    }
    
    /**
     * Shading fill (shfill PostScript operator).
     * 
     * @param dict Shading to use.
     * @param gstate Current graphics state.
     */
    public void shfill(final PSObjectDict dict, final GraphicsState gstate) {
        try {
            mergeBbox(gstate.getPath().boundingBox());
        } catch (PSErrorNoCurrentPoint e) {
            // do nothing
        }
    }

    /**
     * Implements PostScript stroke operator.
     * 
     * @param gstate Current graphics state.
     */
    public void stroke(final GraphicsState gstate) {
        try {
            mergeBbox(gstate.getPath().boundingBox());
        } catch (PSErrorNoCurrentPoint e) {
            // do nothing
        }
    }
    
    /**
     * Implements PostScript operator setlinecap.
     * 
     * @param cap Line cap parameter. 0: butt cap, 1: round cap, or
     *            2: projecting square cap.
     */
    public void setlinecap(final int cap) {
    }
    
    /**
     * Implements PostScript operator setlinejoin.
     * 
     * @param join Line join parameter. 0: miter join, 1: round join, or
     *             2: bevel join.
     */
    public void setlinejoin(final int join) {
    }
    
    /**
     * Sets the current color in gray, rgb or cmyk.
     * 
     * @param color The color.
     */
    public void setColor(final PSColor color) {
    }
    
    /**
     * Sets the miter limit.
     * 
     * @param num The miter limit.
     */
    public void setmiterlimit(final double num) {
    }
    
    /**
     * Starts a new scope.
     */
    public void startScope() {
    }
    
    /**
     * Ends the current scope.
     */
    public void endScope() {
    }
    
    /**
     * Draws text.
     * @param text Exact text to draw
     * @param position Text anchor point in [micrometer, micrometer]
     * @param angle Text angle in degrees
     * @param fontsize in PostScript pt (= 1/72 inch). If fontsize is NaN, the
     *        font size is not set and completely determined by LaTeX.
     * @param anchor String with two characters:
     *               t - top, c - center, B - baseline b - bottom
     *               l - left, c - center, r - right
     *               e.g. Br = baseline,right
     */
    public void show(final String text, final double[] position,
            final double angle, final double fontsize, final String anchor) {
    }
    
    /**
     * Draws a red dot (useful for debugging, don't use otherwise).
     * 
     * @param x X-coordinate of dot.
     * @param y Y-coordinate of dot.
     */
    public void drawDot(final double x, final double y) {
    }
    
    /**
     * Draws a blue rectangle (useful for debugging, don't use otherwise).
     * 
     * @param lowerLeft Lower-left coordinate.
     * @param upperRight Upper-right coordinate.
     */
    public void drawRect(final double[] lowerLeft, final double[] upperRight) {
    }
    
}
