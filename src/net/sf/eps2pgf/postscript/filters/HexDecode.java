/*
 * HexDecode.java
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

package net.sf.eps2pgf.postscript.filters;

import java.io.FilterInputStream;
import java.io.InputStream;
import java.io.IOException;

/**
 * Hex decoding wrappper around an InputStream.
 * @author Paul Wagenaars
 */
public class HexDecode extends InputStream {
    private InputStream in;
    
    /** Creates a new instance of HexDecode */
    public HexDecode(InputStream in) {
        this.in = in;
    }
    
    /**
     * Reads the next byte of data from this input stream. The value byte is
     * returned as an int in the range 0 to 255. If no byte is available because
     * the end of the stream has been reached, the value -1 is returned. This
     * method blocks until input data is available, the end of the stream is
     * detected, or an exception is thrown.
     */
    public int read() throws IOException {
        int c1 = readNextHexChar();
        if (c1 == -1) {
            return -1;
        }
        int c2 = readNextHexChar();
        if (c2 == -1) {
            c2 = 0;
        }
        return (16*c1 + c2);
    }
    
    /**
     * Read the next hex value from the InputStream <code>in</code>. Whitespace
     * charcters are automatically skipped.
     * @return Value of next hex value ranging from 0-15. Returns -1 at
     *         end-of-file.
     */
    private int readNextHexChar() throws IOException {
        int c;
        while ( true ) {
            c = in.read();
            // See if it is a valid hex value
            if ( (c >= 48) && (c <= 57) ) {
                c -= 48;
            } else if ( (c >= 65) && (c <= 70) ) {
                c -= 55;
            } else if ( (c >= 97) && (c <= 102) ) {
                c -= 87;
            } else if ( (c == 0) || (c == 9) || (c == 10) || (c == 12) || (c == 13) || (c == 32) ) {
                continue;
            } else if (c == -1) {
                // nothing to to
            } else {
                throw new IOException();
            }
            break;
        }
        
        return c;
    }
    
}
