/*
 * DSCHeader.java
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

package net.sf.eps2pgf.ps;

import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Reads DSC header and holds header info.
 * 
 * @author Paul Wagenaars
 */
public class DSCHeader {
    /** Value of BoundingBox, HiResBoundingBox or ExactBoundingBox. */
    private double[] boundingBox = null;
    
    /** Pattern describing a DSC comment. */
    private Pattern dscRegExp = Pattern.compile("%%(\\w+):?\\s*(.*)\\s*");
    
    /**
     * Creates a new instance of DSCHeader. Reads and interprets characters
     * from file until it encounters a line that does not start with a
     * '%'-character.
     * 
     * @param in Read header from this reader
     * 
     * @throws IOException Unable to read from reader
     */
    public DSCHeader(final InputStream in) throws IOException {
        loadAllDSCComments(in);
    }
    
    /**
     * Creates a new instance of DSCHeader. The header is initialized with the
     * specified bounding box.
     * 
     * @param bbox Use this array of four doubles as bounding box
     */
    public DSCHeader(final double[] bbox) {
        boundingBox = bbox.clone();
    }
    
    /**
     * Read and interpret all DSC comments from the header.
     * 
     * @param in Read header from this reader
     * 
     * @throws IOException Signals that an I/O exception has occurred.
     */
    void loadAllDSCComments(final InputStream in) throws IOException {
        String[] comment;
        while ((comment = readDSCComment(in)) != null) {
            String fieldname = comment[0].toLowerCase();
            if (fieldname.equals("hiresboundingbox")) {
                boundingBox = parseBoundingBox(comment[1]);
            } else if (fieldname.equals("exactboundingbox")) {
                boundingBox = parseBoundingBox(comment[1]);
            } else if (fieldname.equals("boundingbox")
                    && (boundingBox == null)) {
                
                boundingBox = parseBoundingBox(comment[1]);
            }
        }
    }
    
    /**
     * Parse a string as bounding box.
     * 
     * @param bboxString String representation of the bounding box.
     * 
     * @return The bounding box.
     */
    double[] parseBoundingBox(final String bboxString) {
        Matcher matcher = Pattern.compile(
                "\\s*(\\S*)\\s+(\\S*)\\s+(\\S*)\\s+(\\S*)\\s*")
                .matcher(bboxString);
        if (!matcher.matches()) {
            return null;
        } else {
            double[] bbox = new double[4];
            bbox[0] = Double.parseDouble(matcher.group(1));
            bbox[1] = Double.parseDouble(matcher.group(2));
            bbox[2] = Double.parseDouble(matcher.group(3));
            bbox[3] = Double.parseDouble(matcher.group(4));
            return bbox;
        }
    }
    
    /**
     * Read the next DSC comment.
     * 
     * @param in Read comments from this reader
     * 
     * @return the string[]
     * 
     * @throws IOException Signals that an I/O exception has occurred.
     */
    String[] readDSCComment(final InputStream in) throws IOException {
        Matcher matcher;
                
        while (true) {
            String line = readCommentLine(in);
            if (line.length() == 0) {
                // we found the end of the header
                return null;
            }
            
            // Check whether this is a DSC comment
            matcher = dscRegExp.matcher(line);
            if (matcher.matches()) {
                // we found a good DSC comment
                break;
            }
        }

        String[] comment = new String[2];
        comment[0] = matcher.group(1);
        comment[1] = matcher.group(2);
        return comment;
    }
    
    /**
     * Reads characters from 'in' until a full comment line is read. Return
     * immediately if the current line is not a comment line.
     * 
     * @param in Read comments from this stream.
     * 
     * @return The comment line.
     * 
     * @throws IOException Signals that an I/O exception has occurred.
     */
    String readCommentLine(final InputStream in) throws IOException {
        StringBuilder line = new StringBuilder();
        int nextChar;
        boolean lineEnded = false;
        
        in.mark(1);
        while ((nextChar = in.read()) != -1) {
            // Check for the end of a line
            if ((nextChar == 10) || (nextChar == 12) || (nextChar == 13)) {
                if (line.length() > 0) {
                    lineEnded = true;
                }
            } else if (lineEnded) {
                in.reset();
                break;
            } else if (line.length() > 0) {
                line.append((char) nextChar);
            } else {
                // this is the first character of a new line. Check whether it
                // is a comment line (i.e. starts with %-character).
                if (nextChar == 37) {
                    line.append((char) nextChar);
                } else {
                    in.reset();
                    break;
                }
            }
            
            in.mark(1);
        }
        
        return line.toString();
    }

    /**
     * @return the boundingBox
     */
    public double[] getBoundingBox() {
        if (boundingBox != null) {
            return boundingBox.clone();
        } else {
            return null;
        }
    }
}
