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
import net.sf.eps2pgf.ps.errors.PSErrorNoCurrentPoint;
import net.sf.eps2pgf.ps.objects.PSObjectDict;
import net.sf.eps2pgf.ps.objects.PSObjectMatrix;

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
     * Returns a exact deep copy of this output device.
     * 
     * @return Deep copy of this object.
     */
    @Override
    public CacheDevice clone() {
        CacheDevice copy;
        try {
            copy = (CacheDevice) super.clone();
            if (pathBbox != null) {
                copy.pathBbox = pathBbox.clone();
            }
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
     * @return Default transformation matrix.
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
     * @param gstate Current graphics state.
     */
    public void fill(final GraphicsState gstate) {
        try {
            this.mergeBbox(gstate.getPath().boundingBox());
        } catch (PSErrorNoCurrentPoint e) {
            // do nothing
        }
    }
    
    /**
     * Set the current clipping path in the graphics state as clipping path in
     * the output document. The even-odd rule is used to determine which point
     * are inside the path.
     * 
     * @param gstate Current graphics state.
     */
    public void eoclip(final GraphicsState gstate) {
    }

    /**
     * Fills a path using the even-odd rule.
     * See the PostScript manual (fill operator) for more info.
     * 
     * @param gstate Current graphics state.
     */
    public void eofill(final GraphicsState gstate) {
        try {
            this.mergeBbox(gstate.getPath().boundingBox());
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
     * @param gstate Current graphics state.
     */
    public void show(final String text, final double[] position,
            final double angle, final double fontsize, final String anchor,
            final GraphicsState gstate) {
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
    
    /**
     * Adds a bitmap image to the output.
     * 
     * @param img The bitmap image to add.
     * 
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public void image(final Image img) throws IOException {
        double[] imgBbox = new double[4];
        double[][] imgCorners = img.getDeviceBbox();
        imgBbox[0] = Math.min(imgCorners[0][0], Math.min(imgCorners[1][0],
                Math.min(imgCorners[2][0], imgCorners[3][0])));
        imgBbox[1] = Math.min(imgCorners[0][1], Math.min(imgCorners[1][1],
                Math.min(imgCorners[2][1], imgCorners[3][1])));
        imgBbox[2] = Math.max(imgCorners[0][0], Math.max(imgCorners[1][0],
                Math.max(imgCorners[2][0], imgCorners[3][0])));
        imgBbox[3] = Math.max(imgCorners[0][1], Math.max(imgCorners[1][1],
                Math.max(imgCorners[2][1], imgCorners[3][1])));
        mergeBbox(imgBbox);
    }
}
