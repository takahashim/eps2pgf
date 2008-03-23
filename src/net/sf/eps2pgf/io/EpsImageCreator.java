/*
 * EpsImageCreator.java
 *
 * This file is part of Eps2pgf.
 *
 * Copyright 2007-2008 Paul Wagenaars <paul@wagenaars.org>
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

package net.sf.eps2pgf.io;

import java.io.IOException;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Formatter;

import net.sf.eps2pgf.Main;
import net.sf.eps2pgf.ps.Image;
import net.sf.eps2pgf.ps.errors.PSError;
import net.sf.eps2pgf.ps.errors.PSErrorUnimplemented;
import net.sf.eps2pgf.ps.resources.colors.PSColor;

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
     * 
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws PSError A PostScript error occurred.
     */
    public static void writeEpsImage(final OutputStream out, final Image img)
            throws IOException, PSError {
        
        writeHeader(out, img);
        writeScaling(out, img);
        writeColorSpace(out, img);
        writeImageDict(out, img);
        writeImageData(out, img);
        
        
        String footer = "\n%%EOF";
        out.write(footer.getBytes());
    }
    
    /**
     * Write the header of the EPS file.
     * 
     * @param out OutputStream to which EPS image is written.
     * @param img Bitmap image to must be converted to EPS and written to the
     * OutputStream.
     * 
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private static void writeHeader(final OutputStream out, final Image img)
            throws IOException {
        
        StringBuilder header = new StringBuilder();
        header.append("%!PS-Adobe-3.0 EPSF-3.0\n");
        header.append("%%Creator: " + Main.getNameVersion() + "\n");
        header.append("%%Title: Eps2pgf bitmap image\n");
        header.append("%%CreationDate: ");
        Calendar now = Calendar.getInstance();
        Formatter fmt = new Formatter(header);
        fmt.format("%1$tY-%1$tm-%1$td %1$tk:%1$tM:%1$tS\n", now);
        header.append("%%LanguageLevel: 3\n");
        double width = img.getOutputWidthPt();
        double height = img.getOutputHeightPt();
        header.append("%%BoundingBox: 0 0 " + (int) Math.ceil(width));
        header.append(" " + (int) Math.ceil(height) + "\n");
        DecimalFormat format = new DecimalFormat("0.000");
        header.append("%%HiResBoundingBox: 0.0 0.0 " + format.format(width)
                + " " + format.format(height));
        header.append("\n%%EndComments\n\n");
        out.write(header.toString().getBytes());
    }
    
    /**
     * Set the scaling of the image so that it is the correct size.
     * 
     * @param out The output stream.
     * @param img The image.
     * 
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private static void writeScaling(final OutputStream out, final Image img)
            throws IOException {
        
        double width = img.getOutputWidthPt();
        double height = img.getOutputHeightPt();        
        DecimalFormat format = new DecimalFormat("0.###");
        StringBuilder str = new StringBuilder();
        str.append(format.format(width));
        str.append(" ");
        str.append(format.format(height));
        str.append(" scale\n");
        out.write(str.toString().getBytes());
    }
    
    /**
     * Write color space of the image to EPS.
     * 
     * @param out OutputStream to which EPS image is written.
     * @param img Bitmap image to must be converted to EPS and written to the
     * OutputStream.
     * 
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws PSErrorUnimplemented Encountered a PostScript feature that is not
     * (yet) implemented.
     */
    private static void writeColorSpace(final OutputStream out,
            final Image img) throws PSErrorUnimplemented, IOException {
        
        PSColor colorSpace = img.getColorSpace();
        StringBuilder epsCode = new StringBuilder();
        epsCode.append(colorSpace.getColorSpace().isis());
        epsCode.append(" setcolorspace\n");
        out.write(epsCode.toString().getBytes());
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
    private static void writeImageDict(final OutputStream out,
            final Image img) throws IOException {
        
        StringBuilder dict = new StringBuilder();
        dict.append("<<\n");
        
        dict.append("/ImageType 1\n");
        
        int width = img.getOutputWidthPx();
        dict.append(String.format("/Width %d\n", width));
        int height = img.getOutputHeightPx();
        dict.append(String.format("/Height %d\n", height));
        dict.append(String.format("/ImageMatrix [%d 0 0 %d 0 0]\n", width,
                height));
        
        dict.append(String.format("/BitsPerComponent %d\n",
                img.getBitsPerComponent()));
        
        double[] decode = img.getDecode();
        dict.append("/Decode [");
        for (int i = 0; i < decode.length; i++) {
            if (decode[i] == Math.round(decode[i])) {
                dict.append(" " + (int) decode[i]);
            } else {
                dict.append(" " + decode[i]);
            }
        }
        dict.append(" ]\n");
        
        dict.append("/Interpolate " + img.getInterpolate() + "\n");
        
        dict.append("/DataSource currentfile\n");
        
        dict.append(">>\nimage\n");
        out.write(dict.toString().getBytes());
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
    private static void writeImageData(final OutputStream out,
            final Image img) throws IOException, PSError {

        int width = img.getOutputWidthPx();
        int height = img.getOutputHeightPx();
        int n = img.getBitsPerComponent();
        int nrComponents = img.getColorSpace().getNrInputValues();
        double[] decode = img.getDecode();
        int bitBuffer = 0;
        int bitsInBuffer = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                double[] components = img.getPixelInputValues(x, y);
                for (int v = 0; v < nrComponents; v++) {
                    double tmp = components[v] - decode[2 * v];
                    tmp *= Math.pow(2.0, (double) n) - 1.0;
                    tmp /= decode[2 * v + 1] - decode[2 * v];
                    
                    bitBuffer <<= n;
                    bitBuffer |= (int) Math.round(tmp);
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
            byte[] data = new byte[1];
            data[0] = (byte) (0xff & (bitBuffer >> (currentBits - 8)));
            out.write(data);
            currentBits -= 8;
        }
        
        return currentBits;
    }
    
}
