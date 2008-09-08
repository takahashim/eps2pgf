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
import net.sf.eps2pgf.ps.errors.PSErrorRangeCheck;
import net.sf.eps2pgf.ps.objects.PSObject;
import net.sf.eps2pgf.ps.objects.PSObjectDict;
import net.sf.eps2pgf.ps.objects.PSObjectName;

/**
 * SubFile decoding wrapper around an <code>InputStream</code>.
 * See PostScript manual for more info on SubFileDecode filter.
 * 
 * @author Paul Wagenaars
 */
public class SubFileDecode extends InputStream {
    
    /** Key of eodCount field in parameter dictionary. */
    public static final PSObjectName KEY_EODCOUNT
                                            = new PSObjectName("/EODCount");
    
    /** Key of eodString field in parameter dictionary. */
    public static final PSObjectName KEY_EODSTRING
                                            = new PSObjectName("/EODString");
    
    
    /** InputStream from which raw characters are read. */
    private InputStream source;
    
    /**
     * The number of occurrences of eodString that will be passed through the
     * filter and made available for reading.
     */
    private int eodCount;
    
    /** The end-of-data (EOD) string. */
    private String eodString;

    /** Does closing this filter also close its data source. */
    private boolean closeSource;
    
    /** The number of EOD occurrences so far. */
    private int eodOccurrences = 0;
    
    /**
     * Read buffer. If eodCount = 0, this is a buffer that reads ahead of the
     * characters read by the read() method. If eodCount > 0, this is a buffer
     * of the last read bytes.
     */
    private int[] readBuffer;
    
    /** Pointer in read-ahead buffer. */
    private int bufferPtr = 0;
    
    /** Length of read buffer. */
    private int n;
    
    /** Buffer with eodString converted to int-values. */
    private int[] eodStringBuffer;
    
    
    /**
     * Creates a new instance of ASCII85Decode.
     * 
     * @param pSource <code>InputStream</code> from which data is read.
     * @param dict Dictionary with filter parameters.
     * 
     * @throws PSError A PostScript error occurred.
     */
    public SubFileDecode(final InputStream pSource, final PSObjectDict dict)
            throws PSError {
        
        source = pSource;
        
        // EODCount field
        PSObject obj = dict.lookup(KEY_EODCOUNT);
        if (obj != null) {
            eodCount = obj.toInt();
            if (eodCount < 0) {
                throw new PSErrorRangeCheck();
            }
        } else {
            eodCount = 1;
        }
        
        // EODString field
        obj = dict.lookup(KEY_EODSTRING);
        if (obj != null) {
            eodString = obj.toString();
        } else {
            eodString = "%%EndBinary";
        }
        n = eodString.length();
        eodStringBuffer = new int[n];
        for (int i = 0; i < n; i++) {
            eodStringBuffer[i] = eodString.charAt(i);
        }
        
        // CloseSource field
        obj = dict.lookup(FilterManager.KEY_CLOSESOURCE);
        if (obj != null) {
            closeSource = obj.toBool();
        } else {
            closeSource = false;
        }
        
        // The read buffer has the same length as the eodString.
        // If EODCount = 0 it's a read-ahead buffer, otherwise it is
        // initialized with '-99' values to prevent problems when eodString has
        // '0' values.
        readBuffer = new int[n];
        blankBuffer();
    }
    
    
    /**
     * Gives an estimation of the number of bytes that can still be read. Due to
     * the nature of the SubFileDecode filter this is not guaranteed to be
     * correct.
     * 
     * @throws java.io.IOException An I/O error occurred.
     * 
     * @return The number of bytes that can be read from this input stream
     * without blocking.
     */
    @Override
    public int available() throws IOException {
        if (source != null) {
            return source.available();
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
            source.close();
        }
        source = null;
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
        return false;
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
        if (n == 0) {
            return readNEqualsZero();
        } else {
            return readNLargerThanZero();
        }
    }
    
    
    /**
     * Reads the next byte from the data stream. This is a special read function
     * that is used when n == 0.
     * 
     * @return The next byte, or -1 if end-of-data is reached.
     * 
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private int readNEqualsZero() throws IOException {
        if (eodOccurrences >= eodCount) {
            return -1;
        } else {
            eodOccurrences++;
            return source.read();
        }
    }
    

    /**
     * Reads the next byte from the data stream. This is a special read function
     * that is used when n == 0.
     * 
     * @return The next byte, or -1 if end-of-data is reached.
     * 
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private int readNLargerThanZero() throws IOException {
        if ((readBuffer[0] == -99) && (eodCount == 0)) {
            fillBuffer();
        }
        
        if (buffersEqual()) {
            eodOccurrences++;
        }
        
        if (((eodCount == 0) && (eodOccurrences > 0))
            || ((eodCount > 0) && (eodOccurrences >= eodCount))) {
            
            return -1;
        }
        
        int value;
        if (eodCount == 0) {
            value = readBuffer[bufferPtr];
            readBuffer[bufferPtr] = source.read();
        } else {
            value = source.read();
            readBuffer[bufferPtr] = value;
        }
        bufferPtr++;
        if (bufferPtr >= eodString.length()) {
            bufferPtr = 0;
        }
        
        return value;
    }
    

    /**
     * Compare the eodStringBuffer with the read buffer.
     * 
     * @return True when buffers are equal, false otherwise.
     */
    private boolean buffersEqual() {
        for (int i = 0; i < n; i++) {
            int index = (i + bufferPtr) % n;
            if (readBuffer[index] != eodStringBuffer[i]) {
                return false;
            }
        }
        blankBuffer();
        return true;
    }
    
    
    /**
     * Fills the buffer with -99 values.
     */
    private void blankBuffer() {
        for (int i = 0; i < n; i++) {
            readBuffer[i] = -99;
        }
    }
    
    /**
     * Fills the read buffer with data from the source input stream.
     */
    private void fillBuffer() {
        for (int i = 0; i < n; i++) {
            try {
                readBuffer[i] = source.read();
            } catch (IOException e) {
                readBuffer[i] = -99;
            }
        }
    }
}
