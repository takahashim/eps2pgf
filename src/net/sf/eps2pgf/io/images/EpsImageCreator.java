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

package net.sf.eps2pgf.io.images;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Formatter;

import net.sf.eps2pgf.Main;
import net.sf.eps2pgf.io.RandomAccessOutputStream;
import net.sf.eps2pgf.ps.Image;
import net.sf.eps2pgf.ps.errors.PSError;
import net.sf.eps2pgf.ps.resources.colors.PSColor;
import net.sf.eps2pgf.ps.resources.filters.ASCII85Encode;
import net.sf.eps2pgf.ps.resources.filters.FlateEncode;

/**
 * This class takes bitmap image and writes it to an OutputStream.
 * 
 * @author Paul Wagenaars
 *
 */
public final class EpsImageCreator {
    
    /**
     * "Hidden" constructor.
     */
    private EpsImageCreator() {
        // empty block
    }
    
    /**
     * Takes a bitmap image and writes it to an OutputStream.
     * 
     * @param out OutputStream to which EPS image is written.
     * @param img Bitmap image to must be converted to EPS and written to the
     * OutputStream.
     * @param title Title of figure (used in EPS header)
     * 
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws PSError A PostScript error occurred.
     */
    public static void writeImage(final OutputStream out, final Image img,
            final String title) throws IOException, PSError {
        
        RandomAccessOutputStream outBuf = new RandomAccessOutputStream(out);
        
        writeHeader(outBuf, img, title);
        writeScaling(outBuf, img);
        writeColorSpace(outBuf, img);
        writeImageDict(outBuf, img);

        OutputStream ascii85Out = new ASCII85Encode(outBuf, null);
        OutputStream flateOut = new FlateEncode(ascii85Out, null);
        OutputStream bufOut = new BufferedOutputStream(flateOut);
        writeImageData(bufOut, img);
        bufOut.close();
        flateOut.close();
        ascii85Out.close();
        
        outBuf.write("\n%%EOF");
        
        outBuf.close();
    }
    
    /**
     * Write the header of the EPS file.
     * 
     * @param out OutputStream to which EPS image is written.
     * @param img Bitmap image to must be converted to EPS and written to the
     * OutputStream.
     * @param title Title of figure (used in EPS header)
     * 
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private static void writeHeader(final RandomAccessOutputStream out,
            final Image img, final String title) throws IOException {
        
        out.write("%!PS-Adobe-3.0 EPSF-3.0\n");
        out.write("%%Creator: " + Main.getNameVersion() + "\n");
        out.write("%%Title: " + title + "\n");
        out.write("%%CreationDate: ");
        Calendar now = Calendar.getInstance();
        Formatter fmt = new Formatter();
        fmt.format("%1$tY-%1$tm-%1$td %1$tk:%1$tM:%1$tS\n", now);
        out.write(fmt.toString());
        out.write("%%LanguageLevel: 3\n");
        double width = img.getOutputWidthPt();
        double height = img.getOutputHeightPt();
        out.write("%%BoundingBox: 0 0 " + (int) Math.ceil(width));
        out.write(" " + (int) Math.ceil(height) + "\n");
        DecimalFormat format = new DecimalFormat("0.000");
        out.write("%%HiResBoundingBox: 0.0 0.0 " + format.format(width)
                + " " + format.format(height));
        out.write("\n%%EndComments\n\n");
    }
    
    /**
     * Set the scaling of the image so that it is the correct size.
     * 
     * @param out The output stream.
     * @param img The image.
     * 
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private static void writeScaling(final RandomAccessOutputStream out,
            final Image img) throws IOException {
        
        double width = img.getOutputWidthPt();
        double height = img.getOutputHeightPt();        
        DecimalFormat format = new DecimalFormat("0.###");
        out.write(format.format(width));
        out.write(" ");
        out.write(format.format(height));
        out.write(" scale\n");
    }
    
    /**
     * Write color space of the image to EPS.
     * 
     * @param out OutputStream to which EPS image is written.
     * @param img Bitmap image to must be converted to EPS and written to the
     * OutputStream.
     * 
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private static void writeColorSpace(final RandomAccessOutputStream out,
            final Image img) throws IOException {
        
        PSColor colorSpace = img.getColorSpace();
        out.write(colorSpace.getColorSpace().isis());
        out.write(" setcolorspace\n");
    }
    
    /**
     * Write the image dictionary.
     * 
     * @param out OutputStream to which EPS image is written.
     * @param img Bitmap image to must be converted to EPS and written to the
     * OutputStream.
     * 
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private static void writeImageDict(final RandomAccessOutputStream out,
            final Image img) throws IOException {
        
        out.write("<<\n");
        
        out.write("/ImageType 1\n");
        
        int width = img.getOutputWidthPx();
        out.write(String.format("/Width %d\n", width));
        int height = img.getOutputHeightPx();
        out.write(String.format("/Height %d\n", height));
        out.write(String.format("/ImageMatrix [%d 0 0 %d 0 0]\n", width,
                height));
        
        out.write(String.format("/BitsPerComponent %d\n",
                img.getBitsPerComponent()));
        
        double[] decode = img.getDecode();
        out.write("/Decode [");
        for (int i = 0; i < decode.length; i++) {
            if (decode[i] == Math.round(decode[i])) {
                out.write(" " + (int) decode[i]);
            } else {
                out.write(" " + decode[i]);
            }
        }
        out.write(" ]\n");
        
        out.write("/Interpolate " + img.getInterpolate() + "\n");
        
        out.write("/DataSource currentfile"
                + " /ASCII85Decode filter"
                + " /FlateDecode filter\n");
        
        out.write(">>\nimage\n");
    }
    
    /**
     * Write the binary image data to the EPS file.
     * 
     * @param out OutputStream to which EPS image is written.
     * @param img Bitmap image to must be converted to EPS and written to the
     * OutputStream.
     * 
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws PSError A PostScript error occurred.
     */
    public static void writeImageData(final OutputStream out,
            final Image img) throws IOException, PSError {
        
        int width = img.getOutputWidthPx();
        int height = img.getOutputHeightPx();
        int n = img.getBitsPerComponent();
        int nrComponents = img.getColorSpace().getNrInputValues();
        
        int bitBuffer = 0;
        int bitsInBuffer = 0;
        
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int[] components = img.getPixelInputIntValues(x, y);
                for (int v = 0; v < nrComponents; v++) {
                    bitBuffer <<= n;
                    bitBuffer |= components[v];
                    bitsInBuffer += n;
                    
                    bitsInBuffer = writeBytesFromBuffer(out, bitBuffer,
                            bitsInBuffer);
                }
            }
            // We are at the end of the row. If there is still an incomplete
            // byte in the buffer we add some zeros to make a full byte.
            if (bitsInBuffer > 0) {
                int nrZeros = 8 - bitsInBuffer;
                bitBuffer <<= nrZeros;
                bitsInBuffer += nrZeros;
                bitsInBuffer = writeBytesFromBuffer(out, bitBuffer,
                        bitsInBuffer);
            }
        }
    }
    
    /**
     * Write all full bytes from a bit buffer to an output stream.
     * 
     * @param out OutputStream where the bytes must be written to.
     * @param bitBuffer Buffer with bits.
     * @param bitsInBuffer Indicates the number of bits in the bit buffer.
     * 
     * @return Number of bits left in buffer.
     * 
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private static int writeBytesFromBuffer(final OutputStream out,
            final int bitBuffer, final int bitsInBuffer) throws IOException {

        int currentBits = bitsInBuffer;
        while (currentBits >= 8) {
            int byteVal = 0xff & (bitBuffer >> (currentBits - 8));
            out.write(byteVal);
            currentBits -= 8;
        }
        
        return currentBits;
    }
    
}
