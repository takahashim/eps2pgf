/*
 * This file is part of Eps2pgf.
 *
 * Copyright 2007-2009 Paul Wagenaars
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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import net.sf.eps2pgf.Options;
import net.sf.eps2pgf.ProgramError;
import net.sf.eps2pgf.io.images.EpsImageCreator;
import net.sf.eps2pgf.io.images.PdfImageCreator;
import net.sf.eps2pgf.ps.Closepath;
import net.sf.eps2pgf.ps.Curveto;
import net.sf.eps2pgf.ps.GraphicsState;
import net.sf.eps2pgf.ps.Image;
import net.sf.eps2pgf.ps.Interpreter;
import net.sf.eps2pgf.ps.Lineto;
import net.sf.eps2pgf.ps.Moveto;
import net.sf.eps2pgf.ps.Matrix;
import net.sf.eps2pgf.ps.Path;
import net.sf.eps2pgf.ps.PathSection;
import net.sf.eps2pgf.ps.errors.PSError;
import net.sf.eps2pgf.ps.errors.PSErrorIOError;
import net.sf.eps2pgf.ps.errors.PSErrorRangeCheck;
import net.sf.eps2pgf.ps.errors.PSErrorUnregistered;
import net.sf.eps2pgf.ps.objects.PSObjectDict;
import net.sf.eps2pgf.ps.resources.colors.PSColor;
import net.sf.eps2pgf.ps.resources.shadings.RadialShading;
import net.sf.eps2pgf.ps.resources.shadings.Shading;

/**
 * Writes PGF files.
 * 
 * @author Paul Wagenaars
 */
public class PGFDevice implements OutputDevice, Cloneable {
    
    /** Coordinate format (used to format X- and Y-coordinates). */
    static final DecimalFormat COOR_FORMAT = new DecimalFormat("#.###", 
            new DecimalFormatSymbols(Locale.US));
    
    /** Length format (used to format line width, dash, etc...). */
    static final DecimalFormat LENGTH_FORMAT = new DecimalFormat("#.###", 
            new DecimalFormatSymbols(Locale.US));
    
    /** Font size format (used to set font size in pt). */
    static final DecimalFormat FONTSIZE_FORMAT = new DecimalFormat("#.##",
            new DecimalFormatSymbols(Locale.US));
    
    /**
     * Colors (in range from 0.0 to 1.0) have at least 16-bit per channel
     * accuracy.
     */
    static final DecimalFormat COLOR_FORMAT = new DecimalFormat("#.######",
            new DecimalFormatSymbols(Locale.US));
    
    
    //
    // The variables below keep track of the status of parameters that are
    // influenced by the current scope of the output document.
    //
    
    /** Current color values in output document. Is influenced by scope. */
    private ArrayList<Double> currentColor = new ArrayList<Double>();
    
    /** Current line width in output document. Is influenced by scope. */
    private double currentLineWidth = -1.0;
    
    /** Current dash pattern in output document. Is influenced by scope. */
    private ArrayList<Double> currentDashPattern = new ArrayList<Double>();
    
    /** Current dash offset in output document. Is influenced by scope. */
    private double currentDashOffset = 0.0;
    
    /** Current line cap in output document. Is influenced by scope. */
    private int currentLineCap = 0;
    
    /** Current line join in output document. Is influenced by scope. */
    private int currentLineJoin = 0;
    
    /** Current miter limit in output document. Is influenced by scope. */
    private double currentMiterLimit = 10.0;
    
    /** Current color space in output document. Is influenced by scope. */
    private String currentColorSpace = "";
    

    //
    // The variables below keep track of some parameters that are not limited
    // by scope. If this graphics state is cloned these parameters are shared.
    //
    
    /** Unique number of next bitmap image. */
    private int[] nextImage = {1};
    
    /** Current scope depth in output document. */
    private int[] scopeDepth = {0};
    
    
    //
    // Links to useful objects
    //
    
    /** Output file. */
    private Writer out;
    
    /** Interpreter to which this device belongs. */
    private Interpreter interp;
    
    /**
     * Creates a new instance of PGFExport.
     * 
     * @param wOut Writer to where the PGF code will be written.
     * @param interpreter The interpreter.
     */
    public PGFDevice(final Writer wOut, final Interpreter interpreter) {
        out = wOut;
        interp = interpreter;
    }
    
    /**
     * Returns a <b>copy</b> default transformation matrix (converts user space
     * coordinates to device space).
     * 
     * @return Default transformation matrix.
     */
    public Matrix defaultCTM() {
        return new Matrix(25.4 * 1000.0 / 72.0, 0.0, 0.0,
                25.4 * 1000.0 / 72.0, 0.0, 0.0);
    }
    
    /**
     * Internal Eps2pgf command: eps2pgfgetmetrics
     * It is meant for the cache device. When this command is issued, it will
     * return metrics information about the drawn glyph.
     * 
     * @return Metrics information about glyph.
     */
    public double[] eps2pgfGetMetrics() {
        double[] dummyData = {0.0, 0.0, 0.0, 0.0, 0.0, 0.0};
        return dummyData;
    }
    
    /**
     * Initialize before any other methods are called. Normally, this method
     * writes a header.
     * 
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public void init() throws IOException {
        out.write("% Created by " + net.sf.eps2pgf.Main.getNameVersion() + " ");
        Date now = new Date();
        out.write("on " + now  + "\n");
        out.write("\\begin{pgfpicture}\n");
    }
    
    /**
     * Finalize writing. Normally, this method writes a footer.
     * 
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public void finish() throws IOException {
        while (scopeDepth[0] > 0) {
            endScope();
        }
        out.write("\\end{pgfpicture}\n");
    }
    
    /**
     * Convert a Path to pgf code and write in to the output.
     * 
     * @param path The path.
     * 
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws PSErrorUnregistered Encountered a PostScript feature that is not
     * (yet) implemented.
     */
    void writePath(final Path path) throws IOException, PSErrorUnregistered {
        for (int i = 0; i < path.getSections().size(); i++) {
            PathSection section = path.getSections().get(i);
            if (section instanceof Moveto) {
                // If the path ends with a moveto, the moveto is ignored.
                if (i < (path.getSections().size() - 1)) {
                    String x = COOR_FORMAT.format(1e-4 * section.getParam(0));
                    String y = COOR_FORMAT.format(1e-4 * section.getParam(1));
                    out.write("\\pgfpathmoveto{\\pgfqpoint{" + x + "cm}{" + y
                            + "cm}}\n");
                }
            } else if (section instanceof Lineto) {
                String x = COOR_FORMAT.format(1e-4 * section.getParam(0));
                String y = COOR_FORMAT.format(1e-4 * section.getParam(1));
                out.write("\\pgfpathlineto{\\pgfqpoint{" + x + "cm}{" + y
                        + "cm}}\n");
            } else if (section instanceof Curveto) {
                String x1 = COOR_FORMAT.format(1e-4 * section.getParam(0));
                String y1 = COOR_FORMAT.format(1e-4 * section.getParam(1));
                String x2 = COOR_FORMAT.format(1e-4 * section.getParam(2));
                String y2 = COOR_FORMAT.format(1e-4 * section.getParam(3));
                String x3 = COOR_FORMAT.format(1e-4 * section.getParam(4));
                String y3 = COOR_FORMAT.format(1e-4 * section.getParam(5));
                out.write("\\pgfpathcurveto");
                out.write("{\\pgfqpoint{" + x1 + "cm}{" + y1 + "cm}}");
                out.write("{\\pgfqpoint{" + x2 + "cm}{" + y2 + "cm}}");
                out.write("{\\pgfqpoint{" + x3 + "cm}{" + y3 + "cm}}\n");
            } else if (section instanceof Closepath) {
                out.write("\\pgfpathclose\n");
            } else {
                throw new PSErrorUnregistered("Can't handle "
                        + section.getClass().getName());
            }
        }
    }
    
    /**
     * Implements PostScript stroke operator.
     * 
     * @param gstate Current graphics state.
     * 
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws PSError A PostScript error occurred.
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    public void stroke(final GraphicsState gstate)
            throws IOException, PSError, ProgramError {
        
        updateDash(gstate);
        updateLineWidth(gstate);
        updateLineCap(gstate);
        updateLineJoin(gstate);
        updateMiterLimit(gstate);
        updateColor(gstate);
        writePath(gstate.getPath());
        out.write("\\pgfusepath{stroke}\n");
    }
    
    /**
     * Implements PostScript clip operator.
     * Intersects the area inside the current clipping path with the area
     * inside the current path to produce a new, smaller clipping path.
     * 
     * @param clipPath the clip path
     * 
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws PSErrorUnregistered Encountered a PostScript feature that is not
     *                              (yet) implemented.
     */
    public void clip(final Path clipPath)
            throws IOException, PSErrorUnregistered {
        writePath(clipPath);
        out.write("\\pgfusepath{clip}\n");
    }
    
    /**
     * Returns a exact deep copy of this output device.
     * 
     * @return Deep copy of this object.
     */
    @Override
    public PGFDevice clone() {
        PGFDevice copy;
        try {
            copy = (PGFDevice) super.clone();
        } catch (CloneNotSupportedException e) {
            copy = null;
        }

        copy.currentColor = new ArrayList<Double>(currentColor);
        copy.currentDashPattern = new ArrayList<Double>(currentDashPattern);
        
        return copy;
    }

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
    public void fill(final GraphicsState gstate)
            throws IOException, PSError, ProgramError {
        
        updateColor(gstate);
        writePath(gstate.getPath());
        out.write("\\pgfusepath{fill}\n");
    }
    
    /**
     * Set the current clipping path in the graphics state as clipping path in
     * the output document. The even-odd rule is used to determine which point
     * are inside the path.
     * 
     * @param gstate The current graphics state.
     * 
     * @throws IOException Signals that an I/O exception has occurred.
     * (yet) implemented.
     * @throws PSError A PostScript error occurred.
     */
    public void eoclip(final GraphicsState gstate)
            throws IOException, PSError {
        
        writePath(gstate.getClippingPath());
        out.write("\\pgfseteorule\\pgfusepath{clip}\\pgfsetnonzerorule\n");
    }
    
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
    public void eofill(final GraphicsState gstate)
            throws IOException, PSError, ProgramError {
        
        updateColor(gstate);
        writePath(gstate.getPath());
        out.write("\\pgfseteorule\\pgfusepath{fill}\\pgfsetnonzerorule\n");
    }
    
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
    public void shfill(final PSObjectDict dict, final GraphicsState gstate)
            throws PSError, IOException, ProgramError {
        
        updateColor(gstate);
        Shading shading = Shading.newShading(dict);
        if (shading instanceof RadialShading) {
            radialShading((RadialShading) shading, gstate);
        } else {
            throw new PSErrorUnregistered("Shading of this type " + shading);
        }
    }
    
    /**
     * Create a radial shading.
     * 
     * @param shading The shading.
     * @param gstate The graphics state.
     * 
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws PSError A PostScript error occurred.
     */
    void radialShading(final RadialShading shading, final GraphicsState gstate)
            throws IOException, PSError {
        // Convert coordinates and radii from user space to coordinate space
        // PGF does not support the Extend parameters for shadings. So we
        // try to emulate the effect.
        double scaling = gstate.getCtm().getMeanScaling();
        double xScale = gstate.getCtm().getXScaling() / scaling;
        double yScale = gstate.getCtm().getYScaling() / scaling;
        double angle = gstate.getCtm().getRotation();
        double[] coor0 = gstate.getCtm().transform(shading.getCoord(0.0));
        double[] coor1 = gstate.getCtm().transform(shading.getCoord(1.0));

        double maxS = 1.0;
        if (shading.getExtend1()) {
            // Find the s value for which the radius is big (0.3 meters ~
            // a4 paper).
            maxS = shading.getSForDistance(0.3 * 1e6 / scaling, 1,
                    Double.POSITIVE_INFINITY);
            coor1 = gstate.getCtm().transform(shading.getCoord(maxS));
        }
        
        startScope();
        out.write("\\pgfdeclareradialshading{eps2pgfshading}{\\pgfqpoint{");
        out.write(COOR_FORMAT.format(1e-4 * (coor0[0] - coor1[0]) / xScale)
                + "cm}{");
        out.write(COOR_FORMAT.format(1e-4 * (coor0[1] - coor1[1]) / yScale)
                + "cm}}{");
        double[] sFit = shading.fitLinearSegmentsOnColor(0.01);        
        for (int i = 0; i < sFit.length; i++) {
            if (i > 0) {
                out.write(";");
            }
            double r = scaling * shading.getRadius(sFit[i]);
            double[] color = shading.getColor(sFit[i]);
            out.write("rgb(" + LENGTH_FORMAT.format(1e-4 * r) + "cm)=");
            out.write("(" + COLOR_FORMAT.format(color[0]));
            out.write("," + COLOR_FORMAT.format(color[1]));
            out.write("," + COLOR_FORMAT.format(color[2]) + ")");
        }
        if (maxS > 1.0) {
            double r = scaling * shading.getRadius(maxS);
            double[] color = shading.getColor(1.0);
            out.write(";rgb(" + LENGTH_FORMAT.format(1e-4 * r) + "cm)=");
            out.write("(" + COLOR_FORMAT.format(color[0]));
            out.write("," + COLOR_FORMAT.format(color[1]));
            out.write("," + COLOR_FORMAT.format(color[2]) + ")");
        }
        out.write("}");
        out.write("\\pgflowlevelobj{");
        out.write("\\pgftransformshift{\\pgfqpoint{");
        out.write(LENGTH_FORMAT.format(1e-4 * coor1[0]) + "cm}{");
        out.write(LENGTH_FORMAT.format(1e-4 * coor1[1]) + "cm}}");
        if (Math.abs(angle) > 1e-10) {
            out.write("\\pgftransformrotate{" + COOR_FORMAT.format(angle)
                    + "}");
        }
        if (Math.abs(xScale - 1.0) > 1e-10) {
            out.write("\\pgftransformxscale{" + xScale + "}");
        }
        if (Math.abs(yScale - 1.0) > 1e-10) {
            out.write("\\pgftransformyscale{" + yScale + "}");
        }
        out.write("}{\\pgfuseshading{eps2pgfshading}}");
        endScope();
    }
    
    /**
     * Implements PostScript operator setlinecap.
     * 
     * @param gstate The current graphics state.
     * 
     * @throws IOException Unable to write output
     * @throws PSError A PostScript error occurred.
     */
    private void updateLineCap(final GraphicsState gstate)
            throws IOException, PSError {
        
        int cap = gstate.getLineCap();
        if (cap != currentLineCap) {
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
            currentLineCap = cap;
        }
    }
    
    /**
     * Updates the line join in the output.
     * 
     * @param gstate The current graphics state.
     * 
     * @throws IOException Unable to write output
     * @throws PSError A PostScript error occurred.
     */
    private void updateLineJoin(final GraphicsState gstate)
            throws IOException, PSError {
        
        int join = gstate.getLineJoin();
        if (currentLineJoin != join) {
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
            currentLineJoin = join;
        }        
    }
    
    /**
     * Updates dash pattern in PGF output.
     * 
     * @param gstate Current graphics state
     * 
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws PSError A PostScript error occurred.
     */
    private void updateDash(final GraphicsState gstate)
            throws IOException, PSError {

        double scaling = gstate.getCtm().getMeanScaling();
        double gsOffset = gstate.getDashOffset();
        List<Double> gsPattern = gstate.getDashPattern();
        int gsN = gsPattern.size();

        // Check whether anything was changed
        boolean dashChanged = false;
        if (Math.abs(currentDashOffset - gsOffset * scaling) > 1e-10) {
            dashChanged = true;
        } else {
            if (gsN != currentDashPattern.size()) {
                dashChanged = true;
            } else {
                for (int i = 0; i < gsN; i++) {
                    double val1 = gsPattern.get(i) * scaling;
                    double val2 = currentDashPattern.get(i);
                    if (Math.abs(val1 - val2) > 1e-10) {
                        dashChanged = true;
                        break;
                    }
                }
            }
        }
        
        if (dashChanged) {
            // Determine new dash offset
            currentDashOffset = gsOffset * scaling;
            
            // Make sure that currentDashPattern has the same size as the new
            // dash pattern.
            if (currentDashPattern.size() > gsN) {
                for (int i = currentDashPattern.size() - 1; i >= gsN; i--) {
                    currentDashPattern.remove(i);
                }
            } else if (currentDashPattern.size() < gsN) {
                for (int i = gsN - currentDashPattern.size(); i > 0; i--) {
                    currentDashPattern.add(Double.NaN);
                }
            }
            
            // Determine the new dash pattern.
            for (int i = 0; i < gsN; i++) {
                currentDashPattern.set(i, gsPattern.get(i) * scaling);
            }
            
            // Write the new dash pattern and offset to the output document.
            out.write("\\pgfsetdash{");
            for (int i = 0; i < gsN; i++) {
                out.write("{" + LENGTH_FORMAT.format(
                        1e-4 * currentDashPattern.get(i)) + "cm}");
            }
            out.write("}{" + LENGTH_FORMAT.format(1e-4 * currentDashOffset)
                        + "cm}\n");
        }
    }
    
    /**
     * Compares the current line width with the last-used line width. If they
     * are different the new line width is set in the output.
     * 
     * @param gstate The graphics state.
     * 
     * @throws PSError A PostScript error occurred.
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private void updateLineWidth(final GraphicsState gstate)
            throws PSError, IOException {
        
        double gsWidth = gstate.getLineWidth()
                                       * gstate.getCtm().getMeanScaling();
        
        if (Math.abs(gsWidth - currentLineWidth) > 1e-10) {
            out.write("\\pgfsetlinewidth{"
                    + LENGTH_FORMAT.format(1e-3 * gsWidth) + "mm}\n");
            currentLineWidth = gsWidth;
        }
    }
    
    /**
     * Updates the miter limit.
     * 
     * @param gstate The current graphics state.
     * 
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws PSError A PostScript error occurred.
     */
    private void updateMiterLimit(final GraphicsState gstate)
            throws IOException, PSError {
        
        double gsLimit = gstate.getMiterLimit();
        
        if (Math.abs(currentMiterLimit - gsLimit) > 1e-6) {
            out.write("\\pgfsetmiterlimit{" + gsLimit + "}\n");
            currentMiterLimit = gsLimit;
        }
    }
    
   /**
     * Starts a new scope.
     * 
     * @throws IOException Unable to write output
     */
    public void startScope() throws IOException {
        out.write("\\begin{pgfscope}\n");
        scopeDepth[0] = scopeDepth[0] + 1;
    }
    
    /**
     * Ends the current scope scope.
     * 
     * @throws IOException There was an error write to the output
     */
    public void endScope() throws IOException {
        if (scopeDepth[0] > 0) {
            out.write("\\end{pgfscope}\n");
            scopeDepth[0] = scopeDepth[0] - 1;
        }
    }

    /**
     * Updates the current color in gray, RGB or CMYK in the PGF output.
     * 
     * @param gstate Current graphics state.
     * 
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws PSError A PostScript error occurred.
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    private void updateColor(final GraphicsState gstate)
            throws IOException, PSError, ProgramError {
        
        PSColor gsColor = gstate.getColor();
        int n = gsColor.getNrComponents();
        String gsColspace = gsColor.getFamilyName().isis();
        
        // Check whether current color and color space is the same as the last
        // color and color space.
        boolean colorChanged = false;
        if (!currentColorSpace.equals(gsColspace)) {
            colorChanged = true;
        } else {
            for (int i = 0; i < n; i++) {
                if (Math.abs(currentColor.get(i) - gsColor.getLevel(i))
                        > 1e-6) {
                    
                    colorChanged = true;
                    break;
                }
            }            
        }
        
        if (colorChanged) {
            // Write new color to the output document. 
            String prefColSpace = gsColor.getPreferredColorSpace();
            if (prefColSpace.equals("CMYK")) {
                double[] cmyk = gsColor.getCMYK();
                out.write("\\definecolor{eps2pgf_color}{cmyk}{"
                        + COLOR_FORMAT.format(cmyk[0])
                        + "," + COLOR_FORMAT.format(cmyk[1])
                        + "," + COLOR_FORMAT.format(cmyk[2])
                        + "," + COLOR_FORMAT.format(cmyk[3]) + "}");
            } else if (prefColSpace.equals("RGB")) {
                double[] rgb = gsColor.getRGB();
                out.write("\\definecolor{eps2pgf_color}{rgb}{"
                        + COLOR_FORMAT.format(rgb[0])
                        + "," + COLOR_FORMAT.format(rgb[1])
                        + "," + COLOR_FORMAT.format(rgb[2]) + "}");
            } else if (prefColSpace.equals("Gray")) {
                double gray = gsColor.getGray();
                out.write("\\definecolor{eps2pgf_color}{gray}{"
                        + COLOR_FORMAT.format(gray) + "}");
            } else {
                throw new ProgramError("Invalid preferred color space: "
                        + prefColSpace);
            }
            out.write("\\pgfsetstrokecolor{eps2pgf_color}");
            out.write("\\pgfsetfillcolor{eps2pgf_color}\n");
            
            // Make sure that the currentColor array has the same size as
            // gsColor.
            if (n < currentColor.size()) {
                for (int i = currentColor.size() - 1; i >= n; i--) {
                    currentColor.remove(i);
                }
            } else if (n > currentColor.size()) {
                for (int i = n - currentColor.size(); i > 0; i--) {
                    currentColor.add(Double.NaN);
                }
            }
            
            // Copy the graphics state values to currentColor and
            // currentColorSpace.
            for (int i = 0; i < n; i++) {
                currentColor.set(i, gsColor.getLevel(i));
            }
            currentColorSpace = gsColspace;
        }
    }

    /**
     * Draws text.
     * 
     * @param text Exact text to draw
     * @param position Text anchor point in [micrometer, micrometer]
     * @param angle Text angle in degrees
     * @param pFontsize in PostScript pt (= 1/72 inch). If font size is NaN, the
     * font size is not set and completely determined by LaTeX.
     * @param anchor String with two characters:
     * t - top, c - center, B - baseline b - bottom
     * l - left, c - center, r - right
     * e.g. Br = baseline,right
     * @param gstate The current graphics state.
     * 
     * @throws IOException Unable to write output
     * @throws PSError A PostScript error occurred.
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    public void show(final String text, final double[] position,
            final double angle, final double pFontsize, final String anchor,
            final GraphicsState gstate)
            throws IOException, PSError, ProgramError {
        
        updateColor(gstate);
        
        String x = COOR_FORMAT.format(1e-4 * position[0]);
        String y = COOR_FORMAT.format(1e-4 * position[1]);
        
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
        double fontsize = pFontsize / 72.0 * 72.27;
        
        String angStr = LENGTH_FORMAT.format(angle);
        
        String texText = "";
        if (!Double.isNaN(fontsize)) {
            texText += "\\fontsize{" + FONTSIZE_FORMAT.format(fontsize) + "}{" 
            + FONTSIZE_FORMAT.format(1.2 * fontsize) + "}\\selectfont{";
        }
        texText += text;
        if (!Double.isNaN(fontsize)) {
            texText += "}";
        }
        out.write(String.format("\\pgftext[%sx=%scm,y=%scm,rotate=%s]{%s}\n",
                posOpts, x, y, angStr, texText));
    }
    
    /**
     * Draws a red dot (useful for debugging, don't use otherwise).
     * 
     * @param x X-coordinate (micrometer)
     * @param y Y-coordinate (micrometer)
     * 
     * @throws IOException Unable to write output
     */
    public void drawDot(final double x, final double y) throws IOException {
        out.write("\\begin{pgfscope}\\pgfsetfillcolor{red}\\pgfpathcircle{"
                + "\\pgfqpoint{" + 1e-4 * x + "cm}{" + 1e-4 * y
                + "cm}}{0.5pt}\\pgfusepath{fill}\\end{pgfscope}\n");
    }
    
    /**
     * Draws a blue rectangle (useful for debugging, don't use otherwise).
     * 
     * @param lowerLeft X- and Y-coordinate (in micrometer) of lower left
     * corner.
     * @param upperRight X- and Y-coordinate (in micrometer) of upper right
     * corner.
     * 
     * @throws IOException Unable to write output
     */
    public void drawRect(final double[] lowerLeft, final double[] upperRight)
            throws IOException {
        out.write("\\begin{pgfscope}\\pgfsetstrokecolor{blue}"
                + "\\pgfsetlinewidth{0.1pt}\\pgfpathrectangle{\\pgfqpoint{"
                + 1e-4 * lowerLeft[0] + "cm}{" + 1e-4 * lowerLeft[1]
                + "cm}}{\\pgfqpoint{" + 1e-4 * (upperRight[0] - lowerLeft[0])
                + "cm}{" + 1e-4 * (upperRight[1] - lowerLeft[1])
                + "cm}}\\pgfusepath{stroke}\\end{pgfscope}\n");
    }

    /**
     * Adds a bitmap image to the output.
     * 
     * @param img The bitmap image to add.
     * 
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws PSError A PostScript error occurred.
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    public void image(final Image img)
            throws PSError, IOException, ProgramError {
        
        Options options = interp.getOptions();
        String filename = options.getOutputFile().getName();
        String basename;
        int dot = filename.lastIndexOf('.');
        if (dot >= 0) {
            basename = filename.substring(0, dot);
        } else {
            basename = filename;
        }
        basename += "-image" + nextImage[0];
        nextImage[0] = nextImage[0] + 1;
        File epsFile = new File(options.getOutputFile().getParent(), 
                basename + ".eps");
        File pdfFile = new File(options.getOutputFile().getParent(), 
                basename + ".pdf");
        try {
            OutputStream epsOut = new BufferedOutputStream(
                    new FileOutputStream(epsFile));
            EpsImageCreator.writeImage(epsOut, img, epsFile.getName(),
                    interp.getVm());
            epsOut.close();
            
            OutputStream pdfOut = new BufferedOutputStream(
                    new FileOutputStream(pdfFile));
            PdfImageCreator pdfImgCreator = new PdfImageCreator(interp.getVm());
            pdfImgCreator.writeImage(pdfOut, img, pdfFile.getName());
            pdfOut.close();
            
            double[][] bbox = img.getDeviceBbox();
            int[] cornerMap = img.getCornerMap();
            double llx = bbox[cornerMap[0]][0];
            double lly = bbox[cornerMap[0]][1];
            double lrx = bbox[cornerMap[1]][0];
            double lry = bbox[cornerMap[1]][1];
            double ulx = bbox[cornerMap[3]][0];
            double uly = bbox[cornerMap[3]][1];
            double angle = img.getAngle();
            double x = Math.min(Math.min(llx, ulx), lrx);
            double y = Math.min(Math.min(lly, uly), lry);
            String xStr = COOR_FORMAT.format(1e-4 * x);
            String yStr = COOR_FORMAT.format(1e-4 * y);
            out.write(String.format("\\pgftext[at=\\pgfqpoint{%scm}{%scm},left,"
                    + "bottom]{\\includegraphics[angle=%f]{%s}}\n",
                    xStr, yStr, angle, basename));
        } catch (FileNotFoundException e) {
            throw new PSErrorIOError();
        } catch (IOException e) {
            throw new PSErrorIOError();
        }
    }
}
