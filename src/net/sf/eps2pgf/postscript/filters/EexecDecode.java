/*
 * EexecDecode.java
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

package net.sf.eps2pgf.postscript.filters;

import java.io.InputStream;
import java.io.IOException;

/**
 * Applies eexec decryption to an InputStream. See "Adobe Type 1 Font Format"
 * for for information on this encryption.
 * @author Paul Wagenaars
 */
public class EexecDecode extends InputStream {
    /** <code>InputStream</code> from which encrypted data is read. */
    private InputStream in;
    
    /** Number of random bytes at start of encrypted data. */
    private int n;
    
    /** Random variable used for decryption. */
    private int r;
    
    /** Random variable used for decryption. */
    private int c1;
    
    /** Random variable used for decryption. */
    private int c2;
    
    /** Value of R at the time of the last <code>mark()</code>. */
    private int markedR;
    
    
    /**
     * Wrap a eexec decryption layer around and input stream.
     * 
     * @param pIn Stream from which encrypted data will be read
     */
    public EexecDecode(final InputStream pIn) {
        in = new HexDecode(pIn);
        n = 4;
        r = 55665;
        c1 = 52845;
        c2 = 22719;
    }
    
    /**
     * Wrap a eexec decryption layer around and input stream.
     * 
     * @param pIn Stream from which encrypted data will be read
     */
    /**
     * @param pIn Input stream.
     * @param password Password used from decryption.
     * @param binaryInput Is the input binary.
     */
    public EexecDecode(final InputStream pIn, final int password,
            final boolean binaryInput) {
        
        if (binaryInput) {
            this.in = pIn;
        } else {
            this.in = new HexDecode(pIn);
        }
        n = 4;
        r = password;
        c1 = 52845;
        c2 = 22719;
    }
    
    /**
     * Estimate number of available characters.
     * 
     * @return Estimated number of available characters.
     * 
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Override
    public int available() throws IOException {
        return (in.available() - n);
    }
    
    /**
     * Closes this input stream and releases any system resources associated
     * with the stream.
     * 
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Override
    public void close() throws IOException {
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
        in.mark(readlimit + n);
        markedR = r;
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
     * Reads the next byte of data from the input stream. The value byte is
     * returned as an int in the range 0 to 255. If no byte is available because
     * the end of the stream has been reached, the value -1 is returned. This
     * method blocks until input data is available, the end of the stream is
     * detected, or an exception is thrown.
     * 
     * @throws IOException One or more invalid characters in input stream or an
     * I/O error occurred.
     * 
     * @return The next byte of data, or -1 if the end of the stream is reached.
     */
    @Override
    public int read() throws IOException {
        if (in == null) {
            return -1;
        }

        int c = in.read();
        if (c == -1) {
            return -1;
        } else if (c > 255) {
            throw new IOException();
        }
        
        int t = r >>> 8;
        int p = (c ^ t);
        r = ((c + r) * c1 + c2) & 0x0000FFFF;
        
        if (n > 0) {
            n--;
            p = this.read();
        }
        
        return p;
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
        r = markedR;
    }
    
}
