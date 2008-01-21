package net.sf.eps2pgf.ps.resources.outputdevices;

import java.io.IOException;
import java.io.Writer;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import net.sf.eps2pgf.ps.GraphicsState;
import net.sf.eps2pgf.ps.Path;
import net.sf.eps2pgf.ps.objects.PSObjectDict;
import net.sf.eps2pgf.ps.objects.PSObjectMatrix;
import net.sf.eps2pgf.ps.resources.colors.PSColor;

/**
 * Device that writes only the labels to the output.
 * 
 * @author Paul Wagenaars
 *
 */
public class LOLDevice implements OutputDevice {
    
    /** Format for floating point number in the output.. */
    static final DecimalFormat FLOAT_FORMAT = new DecimalFormat("#.###", 
            new DecimalFormatSymbols(Locale.US));    
    
    /** Output is written to this writer. */
    private Writer out;

    /**
     * Instantiates a new lOL device.
     * 
     * @param pOut Output will be written to this object.
     */
    public LOLDevice(final Writer pOut) {
        this.out = pOut;
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
        return new PSObjectMatrix(1.0, 0.0, 0.0, 1.0, 0.0, 0.0);
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
    public void drawRect(final double[] lowerLeft,
            final double[] upperRight) {

    }

    /**
     * Ends the current scope.
     */
    public void endScope() {

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

    }

    /**
     * Internal Eps2pgf command: eps2pgfgetmetrics
     * It is meant for the cache device. When this command is issued, it will
     * return metrics information about the drawn glyph.
     * 
     * @return Metrics information about glyph.
     */
    public double[] eps2pgfGetMetrics() {
        return null;
    }

    /**
     * Fills a path using the non-zero rule
     * See the PostScript manual (fill operator) for more info.
     * 
     * @param path the path
     */
    public void fill(final Path path) {

    }

    /**
     * Finalize writing. Normally, this method writes a footer.
     */
    public void finish() {

    }

    /**
     * Initialize before any other methods are called. Normally, this method
     * writes a header.
     * 
     * @param gstate the gstate
     */
    public void init(final GraphicsState gstate) {

    }

    /**
     * Sets the current color in gray, rgb or cmyk.
     * 
     * @param color The color.
     */
    public void setColor(final PSColor color) {

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
     * Sets the miter limit.
     * 
     * @param num The miter limit.
     */
    public void setmiterlimit(final double num) {

    }

    /**
     * Shading fill (shfill PostScript operator).
     * 
     * @param dict Shading to use.
     * @param gstate Current graphics state.
     */
    public void shfill(final PSObjectDict dict, final GraphicsState gstate) {

    }

    /**
     * Draws text.
     * 
     * @param text Exact text to draw
     * @param position Text anchor point in [micrometer, micrometer]
     * @param angle Text angle in degrees
     * @param fontsize in PostScript pt (= 1/72 inch). If fontsize is NaN, the
     *        font size is not set and completely determined by LaTeX.
     * @param anchor String with two characters:
     *               t - top, c - center, B - baseline b - bottom
     *               l - left, c - center, r - right
     *               e.g. Br = baseline,right
     *               
     * @throws IOException Unable to write output
     */
    public void show(final String text, final double[] position,
            final double angle, final double fontsize, final String anchor)
            throws IOException {
        out.write(String.format("\\overlaylabel(%s,%s)[%s][%s]{%s}\n",
                FLOAT_FORMAT.format(position[0]),
                FLOAT_FORMAT.format(position[1]),
                anchor,
                FLOAT_FORMAT.format(angle),
                text));
    }

    /**
     * Starts a new scope.
     */
    public void startScope() {

    }

    /**
     * Implements PostScript stroke operator.
     * 
     * @param gstate Current graphics state.
     */
    public void stroke(final GraphicsState gstate) {

    }

}
