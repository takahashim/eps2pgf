/*
 * PSStringInputStream.java
 *
 * This file is part of Eps2pgf.
 *
 * Copyright 2007 Paul Wagenaars <pwagenaars@fastmail.fm>
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
import java.io.InputStream;

import net.sf.eps2pgf.postscript.PSObjectString;
import net.sf.eps2pgf.postscript.errors.PSErrorRangeCheck;

/**
 * Create an InputStream from a PSObjectString
 * @author Paul Wagenaars
 */
public class PSStringInputStream extends InputStream {
    /**
     * PostScript string from which characters are read
     */
    private PSObjectString string;
    
    /**
     * Pointer to the next character to be read.
     */
    private int ptr;
    
    /**
     * Creates a new instance of PSStringInputStream
     * @param psString PostScript string from which the character will be read
     */
    public PSStringInputStream(PSObjectString psString) {
        string = psString;
        ptr = 0;
    }
    
    /**
     * Reads the next character from the PostScript string.
     * @throws java.io.IOException A character in the PostScript string is not in the 0-255 range
     * @return Integer value of the next character (in the range 0-255)
     */
    public int read() throws IOException {
        try {
            int chr = string.get(ptr);
            if ( (chr < 0) || (chr > 255) ) {
                throw new IOException();
            }
            ptr++;
            return chr;
        } catch (PSErrorRangeCheck e) {
            return -1;
        }
    }
}
