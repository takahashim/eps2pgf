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

package net.sf.eps2pgf.ps.resources.filters;

import java.io.InputStream;
import java.io.IOException;

import net.sf.eps2pgf.ps.errors.PSError;
import net.sf.eps2pgf.ps.objects.PSObject;
import net.sf.eps2pgf.ps.objects.PSObjectDict;

/**
 * Hex decoding wrappper around an InputStream.
 * @author Paul Wagenaars
 */
public class ASCIIHexDecode extends InputStream {
    
    /** Encoded characters are read from this stream. */
    private InputStream in;
    
    /** CloseSource parameter. */
    private boolean closeSource;
    
    
    /**
     * Creates a new instance of ASCIIHexDecode.
     * 
     * @param pIn <code>InputStream</code> from which hex data will be read.
     * @param dict The parameter dictionary.
     * 
     * @throws PSError A PostScript error occurred.
     */
    public ASCIIHexDecode(final InputStream pIn, final PSObjectDict dict)
           throws PSError {
        
        in = pIn;
        
        PSObject obj = dict.lookup(FilterManager.KEY_CLOSESOURCE);
        if (obj != null) {
            closeSource = obj.toBool();
        } else {
            closeSource = false;
        }

    }
    
    /**
     * Returns the number of bytes that can be read (or skipped over) from this
     * input stream without blocking by the next caller of a method for this
     * input stream.
     * 
     * @throws IOException An I/O error occurred.
     * 
     * @return The number of bytes that can be read from this input stream
     * without blocking.
     */
    @Override
    public int available() throws IOException {
        if (in != null) {
            int n = in.available();
            n = (n + 1) / 2;
            return n;
        } else {
            throw new IOException();
        }
    }
    
    /**
     * Closes this input stream and releases any system resources associated
     * with the stream.
     * @throws IOException An I/O error occurred.
     */
    @Override
    public void close() throws IOException {
        if (closeSource) {
            in.close();
        }
        in = null;
    }
    
    /**
     * Marks the current position in this input stream. A subsequent call to the
     * reset method repositions this stream at the last marked position so that
     * subsequent reads re-read the same bytes.
     * 
     * The readlimit arguments tells this input stream to allow that many bytes
     * to be read before the mark position gets invalidated.
     * 
     * The general contract of mark is that, if the method markSupported returns
     * true, the stream somehow remembers all the bytes read after the call to
     * mark and stands ready to supply those same bytes again if and whenever
     * the method reset is called. However, the stream is not required to
     * remember any data at all if more than readlimit bytes are read from the
     * stream before reset is called.
     * 
     * @param readlimit The maximum limit of bytes that can be read before the
     * mark position becomes invalid.
     */
    @Override
    public void mark(final int readlimit) {
        in.mark(readlimit);
    }
    
    /**
     * Tests if this input stream supports the mark and reset methods. Whether
     * or not mark and reset are supported is an invariant property of a
     * particular input stream instance.
     * 
     * @return <code>true</code> if this stream instance supports the mark and
     * reset methods; <code>false</code> otherwise.
     */
    @Override
    public boolean markSupported() {
        return in.markSupported();
    }
    
    /**
     * Reads the next byte of data from this input stream. The value byte is
     * returned as an int in the range 0 to 255. If no byte is available because
     * the end of the stream has been reached, the value -1 is returned. This
     * method blocks until input data is available, the end of the stream is
     * detected, or an exception is thrown.
     * 
     * @throws IOException An I/O exception occurred.
     * 
     * @return The next byte of data, or -1 if the end of the stream is reached.
     */
    @Override
    public int read() throws IOException {
        if (in == null) {
            return -1;
        }
        
        int c1 = readNextHexChar();
        if (c1 == -1) {
            return -1;
        }
        int c2 = readNextHexChar();
        if (c2 == -1) {
            c2 = 0;
        }
        return (16 * c1 + c2);
    }
    
    /**
     * Read the next hex value from the InputStream <code>in</code>. Whitespace
     * charcters are automatically skipped.
     * 
     * @return Value of next hex value ranging from 0-15. Returns -1 at
     * end-of-file.
     * 
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private int readNextHexChar() throws IOException {
        int c;
        while (true) {
            c = in.read();
            // See if it is a valid hex value
            if ((c >= 48) && (c <= 57)) {
                c -= 48;
            } else if ((c >= 65) && (c <= 70)) {
                c -= 55;
            } else if ((c >= 97) && (c <= 102)) {
                c -= 87;
            } else if (c == -1) {
                // nothing to to
            } else {
                continue;
            }
            break;
        }
        
        return c;
    }
    
    /**
     * Repositions this stream to the position at the time the mark method was
     * last called on this input stream.
     * 
     * @throws IOException An I/O exception occurred.
     */
    @Override
    public void reset() throws IOException {
        if (in == null) {
            throw new IOException();
        }
        in.reset();
    }
    
    /**
     * Skips over and discards n bytes of data from this input stream. The skip
     * method may, for a variety of reasons, end up skipping over some smaller
     * number of bytes, possibly 0. This may result from any of a number of
     * conditions; reaching end of file before n bytes have been skipped is only
     * one possibility. The actual number of bytes skipped is returned. If n is
     * negative, no bytes are skipped.
     * 
     * @param n The number of bytes to be skipped.
     * 
     * @throws IOException An I/O exception occurred.
     * 
     * @return The actual number of bytes skipped.
     */
    @Override
    public long skip(final long n) throws IOException {
        return in.skip(2 * n);
    }
}
