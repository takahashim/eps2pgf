/*
 * Preview.java
 *
 * This file is part of Eps2pgf.
 *
 * Copyright 2007 Paul Wagenaars <paul@wagenaars.org>
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

package net.sf.eps2pgf.postscript;

import java.io.*;

/**
 *
 * @author Paul Wagenaars
 */
public class Header {
    
    /**
     * Reads a binary header (e.g. with preview information), if
     * present. Most eps files don't have a binary header.
     * For definition of this header see 'Encapsulated PostScript
     * File Format Specification' or 'Supporting Downloadable
     * PostScript Language Fonts'.
     * 
     */
    public static int[] getPostScriptSection(File file) throws IOException {
        RandomAccessFile rFile = new RandomAccessFile(file, "r");
        
        int dim[];
        dim = getEpsPreviewInfo(rFile);
        if (dim == null) {
        	dim = getPfbInfo(rFile);
        }
        
        return dim;
    }
    
    /**
     * Gets the eps preview info.
     * 
     * @param file the file
     * 
     * @return two integers. The first is the start position of
     * PostScript section and the second is the length of PostScript
     * section.
     * 
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static int[] getEpsPreviewInfo(RandomAccessFile file) throws IOException {
    	long startPosition = file.getFilePointer();
    	
        // Check first four bytes for eps preview information
        int[] bytes = {0xC5, 0xD0, 0xD3, 0xC6};
        for (int i = 0 ; i < 4 ; i++) {
            if (file.read() != bytes[i]) {
            	file.seek(startPosition);
                return null;
            }
        }
        
        // Read starting position of PostScript code
        for (int i = 0 ; i < 4 ; i++) {
            bytes[i] = file.read();
        }
        int startPos = bytes[0] + 256*(bytes[1] + 256*(bytes[2] + 256*bytes[3]));
        
        // Read length of section with PostScript code
        for (int i = 0 ; i < 4 ; i++) {
            bytes[i] = file.read();
        }
        int length = bytes[0] + 256*(bytes[1] + 256*(bytes[2] + 256*bytes[3]));
        
        file.seek(startPosition);
        int[] dim = {startPos, length};
        return dim;
    }
    
    /**
     * Gets the information from the header of a PFB (binary font file)
     * 
     * WARNING  WARNING
     * PFB support is not (yet) completely implemented. A PFB
     * file contains multiple segments with ASCII or binary data.
     * Currently, only the first segment is read. At this moment I
     * consider implementing proper PFB support not worth the effect.
     * Maybe in the future.
     * 
     * @param file the file
     * 
     * @return two integers. The first is the start position of
     * PostScript section and the second is the length of PostScript
     * section.
     *  
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static int[] getPfbInfo(RandomAccessFile file) throws IOException {
    	long startPosition = file.getFilePointer();
    	
    	// Check first byte
    	if (file.read() != 128) {
    		file.seek(startPosition);
    		return null;
    	}
    	
    	// Check second byte
    	int sectionStart;
    	int sectionLength;
    	int secondByte = file.read(); 
    	if ( secondByte == 3 ) {
    		// End-of-file section
    		sectionStart = 2;
    		sectionLength = 0;
    	} else if ((secondByte == 1) || (secondByte == 2)) {
    		// ASCII or binary data section
    		int bytes[] = new int[4];
            // Read length of section with PostScript code
            for (int i = 0 ; i < 4 ; i++) {
                bytes[i] = file.read();
            }
            sectionStart = 6;
            sectionLength = bytes[0] + 256*(bytes[1] + 256*(bytes[2] + 256*bytes[3]));    		
    	} else {
    		file.seek(startPosition);
    		return null;
    	}
    	
    	file.seek(startPosition);
    	int[] dim = {sectionStart, sectionLength};
    	System.out.println("-=-=- " + dim[0] + " " + dim[1]);
    	return dim;
    }
}
