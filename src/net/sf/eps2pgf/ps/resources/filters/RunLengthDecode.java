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
import java.util.ArrayList;

import net.sf.eps2pgf.ps.errors.PSError;
import net.sf.eps2pgf.ps.objects.PSObject;
import net.sf.eps2pgf.ps.objects.PSObjectDict;

/**
 * Run-length decoding wrapper around an <code>InputStream</code>.
 * 
 * @author Paul Wagenaars
 */
public class RunLengthDecode extends InputStream {
    /** InputStream from which raw characters are read. */
    private InputStream in;
    
    /** This "character" indicates that the EOF (end-of-file) is encountered. */
    private static final int EOF_CHAR = -1;
    
    /** This "character" indicates that an IOException occurred at this point.
     */
    private static final int IOEXCEPTION_CHAR = -2;
    
    /** This "character" indicates that the EOD (end-of-data) sequence is
     * encountered. */
    private static final int EOD_CHAR = -3;
    
    
    /** Buffer with all decoded characters. */
    private ArrayList<Integer> decodedChars;
    
    /** Pointer to next decoded character to be read. */
    private int decodedPtr;
    
    /** Pointer in decoded character array during last mark(). */
    private int lastMarkPtr = -1;
    
    /** CloseSource parameter. */
    private boolean closeSource;
    
    /**
     * Creates a new instance of ASCII85Decode.
     * 
     * @param pIn <code>InputStream</code> from which base-85 encoded characters
     * are read.
     * @param dict The parameter dictionary.
     * 
     * @throws PSError A PostScript error occurred.
     */
    public RunLengthDecode(final InputStream pIn, final PSObjectDict dict)
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
     * Gives an estimation of the number of bytes that can still be read. Due to
     * the nature of base-85 encoding this is not guaranteed to be correct.
     * @throws java.io.IOException An I/O error occurred.
     * @return The number of bytes that can be read from this input stream
     * without blocking.
     */
    @Override
    public int available() throws IOException {
        if (in != null) {
            return decodedChars.size() - decodedPtr;
        } else {
            throw new IOException();
        }
    }
    
    /**
     * Closes this input stream.
     * 
     * @throws IOException Signals that an I/O exception has occurred.
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
        lastMarkPtr = decodedPtr;
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
        return true;
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
            throw new IOException();
        }
        if (decodedChars == null) {
            decodedChars = readAndDecodeAllChars(in);
            decodedPtr = 0;
        }
        
        int c = decodedChars.get(decodedPtr);
        if (c >= 0) {
            decodedPtr++;
            return c;
        } else if ((c == EOD_CHAR) || (c == EOF_CHAR)) {
            return -1;
        } else {
            throw new IOException();
        }
    }
    
    
    /**
     * Read all raw/encoded characters from a stream, decodes them and returns a
     * list with all (decoded) character codes.
     * 
     * @param inStream The input stream.
     * 
     * @return List with all character codes.
     */
    private static ArrayList<Integer> readAndDecodeAllChars(
            final InputStream inStream) {
        
        // Read all encoded characters from the input stream until EOD or EOF
        // is reached.
        ArrayList<Integer> rawChars = new ArrayList<Integer>();
        int lengthByte;
        
        try {
            while ((lengthByte = inStream.read()) != -1) {
                if (lengthByte <= 127) {
                    for (int i = 0; i < (lengthByte + 1); i++) {
                        rawChars.add(inStream.read());
                    }
                } else if (lengthByte == 128) {
                    rawChars.add(EOD_CHAR);
                    break;
                } else if (lengthByte <= 255) {
                    int c = inStream.read();
                    for (int i = 0; i < (257 - lengthByte); i++) {
                        rawChars.add(c);
                    }
                }
            }
        } catch (IOException e)  {
            rawChars.add(IOEXCEPTION_CHAR);
        }
        
        rawChars.add(EOF_CHAR);
        
        return rawChars;
    }
    
    /**
     * Repositions this stream to the position at the time the mark method was
     * last called on this input stream.
     * 
     * @throws IOException An I/O exception occurred.
     */
    @Override
    public void reset() throws IOException {
        if (lastMarkPtr < 0) {
            throw new IOException();
        }
        decodedPtr = lastMarkPtr;
    }
}
