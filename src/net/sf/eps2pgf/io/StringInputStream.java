/*
 * StringInputStream.java
 *
 * This file is part of Eps2pgf.
 *
 * Copyright 2007, 2008 Paul Wagenaars <paul@wagenaars.org>
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

/**
 * Wraps a string in an InputStream. All characters in the string MUST be in the
 * range 0-255, otherwise an IOException is thrown. This creates a copy of the
 * supplied String, so it's better not to use it on very long Strings.
 * @author Paul Wagenaars
 */
public class StringInputStream extends InputStream {
    /**
     * StringBuffer that holds a copy of the string.
     */
    private StringBuffer string;
    
    /**
     * Keeps track of when the last <code>mark()</code> occurred.
     */
    private int lastMark = 0;
    
    /**
     * Keeps track of the next character to be read.
     */
    private int ptr = 0;
    
    /**
     * Creates a new instance of StringInputStream.
     * 
     * @param in String from which characters will be read
     */
    public StringInputStream(final String in) {
        string = new StringBuffer(in);
    }
    
    /**
     * Returns the number of bytes that can be read (or skipped over) from this
     * input stream without blocking by the next caller of a method for this
     * input stream. The next caller might be the same thread or another thread.
     * 
     * @return the number of bytes that can be read from this input stream
     * without blocking.
     */
    @Override
    public int available() {
        return (string.length() - ptr);
    }
    
    /**
     * Marks the current position in this input stream. A subsequent call to the
     * reset method repositions this stream at the last marked position so that
     * subsequent reads re-read the same bytes.
     * 
     * @param readlimit this parameter is ignored.
     */
    @Override
    public void mark(final int readlimit) {
        lastMark = ptr;
    }
    
    /**
     * Tests if this input stream supports the mark and reset methods. Whether
     * or not mark and reset are supported is an invariant property of a
     * particular input stream instance. The markSupported method of this class
     * return <code>true</code>.
     * @return always returns <code>true</code>.
     */
    @Override
    public boolean markSupported() {
        return true;
    }
    
    /**
     * Read the next character from this stream. The character must be in the
     * range 0-255. If the character in the source string is outside this range
     * an IOException is thrown.
     * 
     * @return returns value in the range 0-255. Or returns -1 in case the end
     * of the string has been reached.
     * 
     * @throws IOException Character encountered in String that has a value not
     * in the range 0-255.
     */
    @Override
    public int read() throws IOException {
        int c;
        try {
            c = string.charAt(ptr);
        } catch (IndexOutOfBoundsException e) {
            return -1;
        }
        
        if (c > 255) {
            throw new IOException();
        }
        
        ptr++;
        return c;
    }

    /**
     * Repositions this stream to the position at the time the mark method was
     * last called on this input stream.
     */
    @Override
    public void reset() {
        ptr = lastMark;
    }
    
    /**
     * Skips over and discards n bytes of data from this input stream. The skip
     * method may, for a variety of reasons, end up skipping over some smaller
     * number of bytes, possibly 0. This may result from any of a number of
     * conditions; reaching end of file before n bytes have been skipped is only
     * one possibility. The actual number of bytes skipped is returned. If n is
     * negative, no bytes are skipped.
     * 
     * @param pN Number of characters to skip.
     * 
     * @return Number of characters that were actually skipped.
     */
    @Override
    public long skip(final long pN) {
        long n = pN;
        if (n < 0) {
            n = 0;
        }
        long i = Math.min(n, (string.length() - ptr));
        ptr += i;
        return i;
    }
    
}
