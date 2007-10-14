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
public class Preview {
    
    /**
     * Reads a binary header (with preview information), if present. Most eps
     * files don't have a binary header.
     * For definition of this header see Encapsulated PostScript File Format Specification
     * 
     */
    public static int[] getPostScriptSection(File file) throws IOException {
        RandomAccessFile rFile = new RandomAccessFile(file, "r");
        
        // Check first fout bytes for standard combination
        int[] bytes = {0xC5, 0xD0, 0xD3, 0xC6};
        for (int i = 0 ; i < 4 ; i++) {
            if (rFile.read() != bytes[i]) {
                rFile.close();
                return null;
            }
        }
        
        // Read starting position of PostScript code
        for (int i = 0 ; i < 4 ; i++) {
            bytes[i] = rFile.read();
        }
        int startPos = bytes[0] + 256*(bytes[1] + 256*(bytes[2] + 256*bytes[3]));
        
        // Read length of section with PostScript code
        for (int i = 0 ; i < 4 ; i++) {
            bytes[i] = rFile.read();
        }
        int length = bytes[0] + 256*(bytes[1] + 256*(bytes[2] + 256*bytes[3]));
        
        rFile.close();
        int[] dim = {startPos, length};
        return dim;
    }
}
