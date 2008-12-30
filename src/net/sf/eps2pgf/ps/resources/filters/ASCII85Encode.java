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

package net.sf.eps2pgf.ps.resources.filters;

import java.io.IOException;
import java.io.OutputStream;

import net.sf.eps2pgf.ps.errors.PSError;
import net.sf.eps2pgf.ps.objects.PSObject;
import net.sf.eps2pgf.ps.objects.PSObjectDict;

/**
 * Implements the ASCII base-85 encoding filter.
 * 
 * @author Paul Wagenaars
 *
 */
public class ASCII85Encode extends OutputStream {
    /** OutputStream to which encoded characters are read. */
    private OutputStream out;
    
    /** Buffer with not yet encoded bytes. */
    private int[] buffer = new int[4];
    
    /** Number of bytes in the buffer. */
    private int bufferSize = 0;
    
    /** Buffer for encoded characters. */
    private byte[] asciiChars = new byte[5];
    
    /** Number of characters in current line. */
    private int currentLineLength = 0;
    
    /** CloseTarget parameter. */
    private boolean closeTarget;
    
    /**
     * Creates a new ASCII85Encode filter.
     * 
     * @param pOut The output stream to which encoded bytes are written.
     * @param dict The parameter dictionary.
     * 
     * @throws PSError A PostScript error occurred.
     */
    public ASCII85Encode(final OutputStream pOut, final PSObjectDict dict)
            throws PSError {
        
        out = pOut;
        
        PSObject obj = null;
        if (dict != null) {
            obj = dict.lookup(FilterManager.KEY_CLOSETARGET);
        }
        if (obj != null) {
            closeTarget = obj.toBool();
        } else {
            closeTarget = false;
        }
    }
    

    /**
     * Writes a byte to this output stream. The bytes will be ASCII base-85
     * encoded and written to the output stream.
     * 
     * @param arg0 The byte.
     * 
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Override
    public void write(final int arg0) throws IOException {
        buffer[bufferSize++] = arg0 & 0xff;
        if (bufferSize == 4) {
            encodeAndWriteBuffer();
        }
    }
    
    /**
     * Encodes the current buffer and writes the result to the output stream.
     * 
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private void encodeAndWriteBuffer() throws IOException {
        int goodEncodedBytes = encodeBytes(buffer, bufferSize, asciiChars);
        out.write(asciiChars, 0, goodEncodedBytes);
        bufferSize = 0;
        currentLineLength += goodEncodedBytes;
        if (currentLineLength > 75) {
            out.write('\n');
            currentLineLength = 0;
        }
    }
    
    /**
     * Encodes a buffer with four raw bytes to ASCII base-85 encoded bytes.
     * 
     * @param rawBytes The raw bytes.
     * @param goodChars The good chars.
     * @param targetBuffer The target buffer (must be five bytes).
     * 
     * @return The number of 'good' encoded bytes in the target buffer. The
     * other bytes should be discarded. 
     */
    private int encodeBytes(final int[] rawBytes, final int goodChars,
            final byte[] targetBuffer) {
        
        // Append zeros if necessary.
        if (goodChars < 4) {
            for (int i = goodChars; i < 4; i++) {
                rawBytes[i] = 0;
            }
        }

        // Combine all bytes in a single long
        long d = 0;
        for (int i = 0; i < 4; i++) {
            d = (d << 8) | buffer[i];
        }
        
        // Convert the long to encoded  bytes.
        int goodEncodedBytes;
        if (d > 0) {
            for (int i = 4; i >= 0; i--) {
                targetBuffer[i] = (byte) (33 + (d % 85));
                d /= 85;
            }
            goodEncodedBytes = goodChars + 1;
        } else {
            targetBuffer[0] = 'z';
            goodEncodedBytes = 1;
        }
        
        return goodEncodedBytes;
    }
    
    /**
     * Flushes this output stream and forces any buffered output bytes to be
     * written out. Note that this does not flush bytes in the buffer that are
     * not yet converted. This is not possible because four bytes are required
     * for the encoding.
     * 
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Override
    public void flush() throws IOException {
        if (out == null) {
            throw new IOException();
        } else {
            out.flush();
        }
    }
    
    /**
     * Closes this filter. Note that this function does not close the output
     * stream.
     * 
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Override
    public void close() throws IOException {
        encodeAndWriteBuffer();
        String data = "\n~>\n";
        out.write(data.getBytes());
        
        if (closeTarget) {
            out.close();
        }
        out = null;
    }

}
