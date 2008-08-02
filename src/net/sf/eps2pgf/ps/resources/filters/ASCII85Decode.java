/*
 * ASCII85Decode.java
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

package net.sf.eps2pgf.ps.resources.filters;

import java.io.InputStream;
import java.io.IOException;
import java.util.ArrayList;

import net.sf.eps2pgf.ps.errors.PSError;
import net.sf.eps2pgf.ps.objects.PSObject;
import net.sf.eps2pgf.ps.objects.PSObjectDict;

/**
 * ASCII Base-85 decoding wrapper around an <code>InputStream</code>.
 * 
 * @author Paul Wagenaars
 */
public class ASCII85Decode extends InputStream {
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
    
    
    /** Buffer with all raw (encoded) characters. */
    private ArrayList<Integer> rawChars;
    
    /** Pointer to next raw character to be read. */
    private int rawPtr;
    
    /** Pointer in raw character array during last mark(). */
    private int lastMarkPtr = -1;
    
    /** Four byte block of decoded bytes. */
    private int[] decoded;
    
    /** Pointer to next decoded char to be read from. */
    private int decodedPtr;
    
    /** Number of valid decoded characters. */
    private int goodChars;
    
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
    public ASCII85Decode(final InputStream pIn, final PSObjectDict dict)
            throws PSError {
        
        in = pIn;
        decoded = new int[4];
        decodedPtr = 5;
        goodChars = 0;

        PSObject obj = dict.lookup(Filter.KEY_CLOSESOURCE);
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
        int n = rawChars.size() - rawPtr;
        n = (n * 4) / 5;
        return n;
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
     * Decode characters.
     * 
     * @param rawBytes The raw bytes.
     * @param decodedBytes The decoded bytes.
     * 
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private static void decodeChars(final long[] rawBytes,
            final int[] decodedBytes) throws IOException {
        
        long d = 0;
        for (int j = 0; j < 5; j++) {
            d = 85L * d + rawBytes[j] - 33L;
        }
        
        if (d >= 256L * 256L * 256L * 256L) {
            throw new IOException();
        }
        decodedBytes[0] = (int) ((d >> 24) & 0x00FF);
        decodedBytes[1] = (int) ((d >> 16) & 0x00FF);
        decodedBytes[2] = (int) ((d >> 8) & 0x00FF);
        decodedBytes[3] = (int) (d & 0x00FF);
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
        lastMarkPtr = rawPtr;
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
        if (rawChars == null) {
            rawChars = readAllRawChars(in);
            rawPtr = 0;
        }
        
        if (decodedPtr >= goodChars) {
            long[] raw = new long[5];
            int goodRawChars = getNextFiveChars(raw);            
            if (goodRawChars < 2) {
                return -1;
            }
            
            decodeChars(raw, decoded);
            decodedPtr = 0;
            goodChars = goodRawChars - 1;
        }
        
        return decoded[decodedPtr++];
    }
    
    
    /**
     * Read all raw/encoded characters from a stream and return a list with all
     * character codes.
     * 
     * @param inStream The input stream.
     * 
     * @return List with all character codes.
     */
    private static ArrayList<Integer> readAllRawChars(
            final InputStream inStream) {
        
        // Read all encoded characters from the input stream until EOD or EOF
        // is reached.
        ArrayList<Integer> rawChars = new ArrayList<Integer>();
        int c;
        
        try {
            while ((c = inStream.read()) != -1) {
                // Check for end-of-data (EOD = ~>)
                if (c == 62) {
                    int lastChar = rawChars.get(rawChars.size() - 1);
                    if (lastChar == 126) {
                        rawChars.set(rawChars.size() - 1, EOD_CHAR);
                        break;
                    }
                }
                rawChars.add(c);
            }
        } catch (IOException e)  {
            rawChars.add(IOEXCEPTION_CHAR);
        }
        rawChars.add(EOF_CHAR);
        
        return rawChars;
    }
    
    /**
     * Read the next five characters from raw character buffer. This will skip
     * whitespace.
     * 
     * @param c Store characters in this buffer.
     * 
     * @return Returns number of characters correctly written. If this is less
     * than five, the remaining characters are set to '!'. Returns -1 when there
     * are no new characters.
     * 
     * @throws IOException An invalid character encountered or and I/O error
     * occurred.
     */
    private int getNextFiveChars(final long[] c)
            throws IOException {
        
        int ptr = 0;
        int goodCharsRead = 0;
        
        while (ptr < 5) {
            int chr = rawChars.get(rawPtr++);
            if ((chr >= '!') && (chr <= 'u')) {
                c[ptr++] = chr;
                goodCharsRead++;
            } else if ((ptr == 0) && (chr == 'z')) {
                c[0] = '!';
                c[1] = '!';
                c[2] = '!';
                c[3] = '!';
                c[4] = '!';
                ptr = 5;
                goodCharsRead = 5;
            } else if ((chr == EOF_CHAR) || (chr == EOD_CHAR)) {
                rawPtr--;
                if (ptr > 0) {
                    c[ptr++] = '/';
                } else {
                    return -1;
                }
            } else if ((chr == 0) || (chr == 9) || (chr == 10) || (chr == 12)
                    || (chr == 13) || (chr == 32)) {
                // Skip all whitespace characters
            } else {
                // Not a valid base-85 character
                throw new IOException();
            }
        }
        return goodCharsRead;
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
        rawPtr = lastMarkPtr;
        decodedPtr = 5;
    }
}
