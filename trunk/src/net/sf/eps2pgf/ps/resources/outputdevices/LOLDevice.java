package net.sf.eps2pgf.ps.resources.outputdevices;

import java.io.IOException;
import java.io.Writer;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import net.sf.eps2pgf.ps.GraphicsState;
import net.sf.eps2pgf.ps.Image;
import net.sf.eps2pgf.ps.Path;
import net.sf.eps2pgf.ps.objects.PSObjectDict;
import net.sf.eps2pgf.ps.objects.PSObjectMatrix;

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
     * Returns a exact deep copy of this output device.
     * 
     * @return Deep copy of this object.
     */
    @Override
    public LOLDevice clone() {
        LOLDevice copy;
        try {
            copy = (LOLDevice) super.clone();
            copy.out = out;  // writer is not cloned
        } catch (CloneNotSupportedException e) {
            /* this exception shouldn't happen. */
            copy = null;
        }
        return copy;
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
     * @param gstate Current graphics state.
     */
    public void fill(final GraphicsState gstate) {

    }

    /**
     * Finalize writing. Normally, this method writes a footer.
     */
    public void finish() {

    }

    /**
     * Initialize before any other methods are called. Normally, this method
     * writes a header.
     */
    public void init() {

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
     * @param gstate Current graphics state.
     *               
     * @throws IOException Unable to write output
     */
    public void show(final String text, final double[] position,
            final double angle, final double fontsize, final String anchor,
            final GraphicsState gstate) throws IOException {
        
        out.write(String.format("\\overlaylabel(%s,%s)[%s][%s]{%s}%%\n",
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
