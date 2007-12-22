package net.sf.eps2pgf.io.devices;

import java.io.IOException;
import java.io.Writer;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import net.sf.eps2pgf.postscript.GraphicsState;
import net.sf.eps2pgf.postscript.PSObjectDict;
import net.sf.eps2pgf.postscript.PSObjectMatrix;
import net.sf.eps2pgf.postscript.Path;
import net.sf.eps2pgf.postscript.colors.PSColor;
import net.sf.eps2pgf.postscript.errors.PSError;
import net.sf.eps2pgf.postscript.errors.PSErrorRangeCheck;
import net.sf.eps2pgf.postscript.errors.PSErrorUnimplemented;

public class LOLDevice implements OutputDevice {
    static final DecimalFormat floatFormat = new DecimalFormat("#.###", 
            new DecimalFormatSymbols(Locale.US));	
	
	Writer out;

	public LOLDevice(Writer out) {
		this.out = out;
	}

	public void clip(Path clipPath) throws IOException, PSErrorUnimplemented {
		// TODO Auto-generated method stub

	}

	public PSObjectMatrix defaultCTM() {
		// TODO Auto-generated method stub
		return new PSObjectMatrix(1.0, 0.0, 0.0, 1.0, 0.0, 0.0);
	}

	public void drawDot(double x, double y) throws IOException {
		// TODO Auto-generated method stub

	}

	public void drawRect(double[] lowerLeft, double[] upperRight)
			throws IOException {
		// TODO Auto-generated method stub

	}

	public void endScope() throws IOException {
		// TODO Auto-generated method stub

	}

	public void eoclip(Path clipPath) throws IOException, PSErrorUnimplemented {
		// TODO Auto-generated method stub

	}

	public void eofill(Path path) throws IOException, PSErrorUnimplemented {
		// TODO Auto-generated method stub

	}

	public double[] eps2pgfGetMetrics() {
		// TODO Auto-generated method stub
		return null;
	}

	public void fill(Path path) throws IOException, PSErrorUnimplemented {
		// TODO Auto-generated method stub

	}

	public void finish() throws IOException {
		// TODO Auto-generated method stub

	}

	public void init(GraphicsState gstate) throws PSError, IOException {
		// TODO Auto-generated method stub

	}

	public void setColor(PSColor color) throws IOException {
		// TODO Auto-generated method stub

	}

	public void setlinecap(int cap) throws IOException, PSErrorRangeCheck {
		// TODO Auto-generated method stub

	}

	public void setlinejoin(int join) throws IOException, PSErrorRangeCheck {
		// TODO Auto-generated method stub

	}

	public void setmiterlimit(double num) throws IOException {
		// TODO Auto-generated method stub

	}

	public void shfill(PSObjectDict dict, GraphicsState gstate) throws PSError,
			IOException {
		// TODO Auto-generated method stub

	}

	public void show(String text, double[] position, double angle,
			double fontsize, String anchor) throws IOException {
        out.write(String.format("\\overlaylabel(%s,%s)[%s][%s]{%s}\n",
                floatFormat.format(position[0]), floatFormat.format(position[1]),
                anchor, floatFormat.format(angle), text));
	}

	public void startScope() throws IOException {
		// TODO Auto-generated method stub

	}

	public void stroke(GraphicsState gstate) throws IOException, PSError {
		// TODO Auto-generated method stub

	}

}
