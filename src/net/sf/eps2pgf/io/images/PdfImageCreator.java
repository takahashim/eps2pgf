/*
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

package net.sf.eps2pgf.io.images;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

import net.sf.eps2pgf.Main;
import net.sf.eps2pgf.ProgramError;
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
public final class PdfImageCreator {
    
    /** Xref table. */
    private List<Integer> xrefTable = new ArrayList<Integer>();
    
    /** Offset (in file) of xref table. */
    private int xrefOffset;
    
    /**
     * Creates a new PDF image creator.
     */
    public PdfImageCreator() {
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
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    public void writeImage(final OutputStream out, final Image img,
            final String title) throws IOException, PSError, ProgramError {

        RandomAccessOutputStream outBuf = new RandomAccessOutputStream(out);
        
        outBuf.write("%PDF-1.2\n");
        
        writeCatalog(outBuf);
        writeOutlines(outBuf);
        writePageTree(outBuf, img);
        writeInfoDict(outBuf, title);
        writeXrefTable(outBuf);
        writeTrailer(outBuf);
        
        outBuf.close();
    }
    
    /**
     * Writes the documents catalog.
     * 
     * @param out The output stream.
     * 
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private void writeCatalog(final RandomAccessOutputStream out)
            throws IOException {
        
        xrefTable.add(out.getPointer());
        out.write("1 0 obj\n<<\n");
        out.write("/Type /Catalog\n");
        out.write("/Outlines 2 0 R\n");
        out.write("/Pages 3 0 R\n");
        out.write(">> endobj\n");
    }
    
    /**
     * Write outline dictionary.
     * 
     * @param out The output stream.
     * 
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private void writeOutlines(final RandomAccessOutputStream out)
            throws IOException {
        
        xrefTable.add(out.getPointer());
        out.write("2 0 obj\n<<\n/Type /Outlines\n/Count 0\n>>\nendobj\n");
    }
    
    /**
     * Write the Page Tree.
     * 
     * @param out The output stream.
     * @param img The bitmap image.
     * 
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws PSError A PostScript error occurred.
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    private void writePageTree(final RandomAccessOutputStream out,
            final Image img) throws IOException, PSError, ProgramError {
        
        // Append Page Tree node
        xrefTable.add(out.getPointer());
        out.write("3 0 obj\n<<\n");
        out.write("/Type /Pages\n");
        out.write("/Kids [4 0 R]\n");
        out.write("/Count 1\n");
        out.write(">>\nendobj\n");
        
        // Append Page Object
        xrefTable.add(out.getPointer());
        out.write("4 0 obj\n<<\n");
        out.write("/Type /Page\n");
        out.write("/Parent 3 0 R\n");
        double width = img.getOutputWidthPt();
        double height = img.getOutputHeightPt();
        DecimalFormat format = new DecimalFormat("0.000");
        out.write("/MediaBox [0.0 0.0 " + format.format(width) + " "
                + format.format(height) + "]\n");
        out.write("/Contents 6 0 R\n");
        out.write("/Resources\n<<\n");
        out.write("/ProcSet [/PDF /ImageB /ImageC /ImageI]\n");
        out.write("/XObject << /Img 5 0 R >>\n");
        out.write(">>\n");
        out.write(">>\nendobj\n");
        
        // Create an XObject with the bitmap image.
        writeImageXObject(out, img);
        
        // Paint the bitmap image

        xrefTable.add(out.getPointer());
        out.write("6 0 obj\n<<\n");
        out.write("/Length ");
        int lengthPos = out.getPointer();
        out.write("          \n");
        out.write(">>\nstream\n");
        int streamStart = out.getPointer();
        out.write("q\n");
        out.write(String.format("%.3f 0 0 -%.3f 0 %.3f cm\n",
                img.getOutputWidthPt(), img.getOutputHeightPt(),
                img.getOutputHeightPt()));
        out.write("/Img Do\n");
        out.write("Q\n");
        int streamEnd = out.getPointer();
        out.seek(lengthPos);
        out.write(Integer.toString(streamEnd - streamStart));
        out.seek(streamEnd);
        out.write("endstream\nendobj\n");
        
    }
    
    /**
     * Write an XObject with the bitmap image.
     * 
     * @param out The output stream.
     * @param img The bitmap image.
     * 
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws PSError A PostScript error occurred.
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    private void writeImageXObject(final RandomAccessOutputStream out,
            final Image img) throws IOException, PSError, ProgramError {
        
        PSColor colorSpace = img.getColorSpace();
        
        xrefTable.add(out.getPointer());
        out.write("5 0 obj\n<<\n");
        out.write("/Type /XObject\n");
        out.write("/Subtype /Image\n");
        out.write(String.format("/Width %d\n", img.getOutputWidthPx()));
        out.write(String.format("/Height %d\n", img.getOutputHeightPx()));

        out.write(String.format("/ColorSpace %s\n",
                colorSpace.getColorSpace().isis()));

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
        
        out.write("/Length ");
        int streamLengthPos = out.getPointer();
        out.write("         \n");
        
        out.write("/Filter [/ASCII85Decode /FlateDecode]\n");

        out.write(">>\nstream\n");
        
        int streamStart = out.getPointer();
        OutputStream ascii85Out = new ASCII85Encode(out, null);
        OutputStream flateOut = new FlateEncode(ascii85Out, null);
        OutputStream bufOut = new BufferedOutputStream(flateOut);
        EpsImageCreator.writeImageData(bufOut, img);
        bufOut.close();
        flateOut.close();
        ascii85Out.close();
        
        int streamEnd = out.getPointer();
        out.seek(streamLengthPos);
        out.write(String.format("%d", streamEnd - streamStart));
        out.seek(streamEnd);                
        out.write("endstream\nendobj\n");
    }
    
    /**
     * Writes the information dictionary.
     * 
     * @param out The output stream.
     * @param title The title of the PDF document.
     * 
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private void writeInfoDict(final RandomAccessOutputStream out,
            final String title) throws IOException {
        
        xrefTable.add(out.getPointer());
        out.write("7 0 obj\n<<\n");
        out.write(String.format("/Title (%s)\n", title));
        out.write(String.format("/Producer (%s)\n", Main.getNameVersion()));
        out.write(String.format("/Creator (%s)\n", Main.getNameVersion()));
        Calendar clndr = Calendar.getInstance();
        String dateString = String.format("%04d%02d%02d%02d%02d%02d",
                clndr.get(Calendar.YEAR),
                clndr.get(Calendar.MONTH) + 1,
                clndr.get(Calendar.DAY_OF_MONTH),
                clndr.get(Calendar.HOUR_OF_DAY),
                clndr.get(Calendar.MINUTE),
                clndr.get(Calendar.SECOND));
        int zoneOffset = clndr.get(Calendar.ZONE_OFFSET)
            + clndr.get(Calendar.DST_OFFSET);
        if (zoneOffset < 0) {
            dateString += "-";
            zoneOffset = -zoneOffset;
        } else if (zoneOffset == 0) {
            dateString += "Z";
        } else {
            dateString += "+";
        }
        dateString += String.format("%02d'%02d'", zoneOffset / 1000 / 60 / 60,
                zoneOffset % (1000 * 60 * 60));
        out.write(String.format("/CreationDate (D:%s)\n", dateString));
        out.write(">>\nendobj\n");
    }
    
    /**
     * Write cross-reference table.
     * 
     * @param out The output stream.
     * 
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private void writeXrefTable(final RandomAccessOutputStream out)
            throws IOException {
        
        xrefOffset = out.getPointer();
        out.write("xref\n");
        out.write("0 " + (xrefTable.size() + 1) + "\n");
        out.write("0000000000 65535 f \n");
        for (int i = 0; i < xrefTable.size(); i++) {
            out.write(String.format("%010d %05d n \n", xrefTable.get(i), 0));
        }
    }
    
    /**
     * Write the trailer of the PDF document.
     * 
     * @param out The output stream to which the trailer is written.
     * 
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private void writeTrailer(final RandomAccessOutputStream out)
            throws IOException {
        
        // Add trailer dictionary
        out.write("trailer\n<<\n");
        out.write(String.format("/Size %d\n", xrefTable.size() + 1));
        out.write("/Root 1 0 R\n");
        out.write("/Info 7 0 R\n");
        UUID uuid = UUID.randomUUID();
        String uuidHex = String.format("%8x%8x", uuid.getMostSignificantBits(),
                uuid.getLeastSignificantBits());
        out.write(String.format("/ID [ <%s> <%s> ]\n", uuidHex, uuidHex));
        out.write(">>\n");
        
        // Add address of xref table
        out.write("startxref\n");
        out.write(String.format("%d\n", xrefOffset));
        
        // Add end-of-file marker
        out.write("%%EOF");
    }
}
