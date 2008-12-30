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

package net.sf.eps2pgf.io;

import java.io.IOException;
import java.io.InputStream;

import net.sf.eps2pgf.ps.errors.PSErrorRangeCheck;
import net.sf.eps2pgf.ps.objects.PSObjectString;

/**
 * Create an InputStream from a PSObjectString.
 * 
 * @author Paul Wagenaars
 */
public class PSStringInputStream extends InputStream {
    /**
     * PostScript string from which characters are read.
     */
    private PSObjectString string;
    
    /**
     * Pointer to the next character to be read.
     */
    private int ptr;
    
    /** Position the last this mark() was called. */
    private int lastMark;
    
    /**
     * Creates a new instance of PSStringInputStream.
     * 
     * @param psString PostScript string from which the character will be read
     */
    public PSStringInputStream(final PSObjectString psString) {
        string = psString;
        ptr = 0;
        lastMark = -1;
    }
    
    /**
     * Marks the current position in this input stream. A subsequent call to
     * the reset method repositions this stream at the last marked position so
     * that subsequent reads re-read the same bytes.
     * 
     * @param readlimit This parameter has no meaning for PSStringInputStream
     */
    @Override
    public void mark(final int readlimit) {
        lastMark = ptr;
    }
    
    /**
     * Tests if this input stream supports the mark and reset methods. Whether
     * or not mark and reset are supported is an invariant property of a
     * particular input stream instance. The markSupported method of
     * this object returns <code>false</code>.
     * @return Always returns <code>true</code>
     */
    @Override
    public boolean markSupported() {
        return true;
    }
    
    /**
     * Reads the next character from the PostScript string.
     * 
     * @return Integer value of the next character (in the range 0-255)
     * 
     * @throws IOException A character in the PostScript string is not in the
     * 0-255 range.
     */
    @Override
    public int read() throws IOException {
        try {
            int chr = string.get(ptr);
            if ((chr < 0) || (chr > 255)) {
                throw new IOException();
            }
            ptr++;
            return chr;
        } catch (PSErrorRangeCheck e) {
            return -1;
        }
    }
    
    /**
     * Repositions this stream to the position at the time the mark method was
     * last called on this input stream.
     * @throws IOException This stream has not yet been <code>mark</code>ed.
     */
    @Override
    public void reset() throws IOException {
        if (lastMark < 0) {
            throw new IOException();
        }
        ptr = lastMark;
    }
}
