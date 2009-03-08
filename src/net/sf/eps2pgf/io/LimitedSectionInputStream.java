/*
 * This file is part of Eps2pgf.
 *
 * Copyright 2007-2009 Paul Wagenaars
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
 * Sets a maximum to the number of characters to be read from a Reader. Attempts
 * to read more characters results is an EOF.
 * @author Paul Wagenaars
 */
public class LimitedSectionInputStream extends InputStream {
    /**
     * Reader from which characters are read.
     */
    private InputStream rdr;
    
    /**
     * Maximum number of characters that will be read. EOF is returned if more
     * characters are requested.
     */
    private int length;
    
    /**
     * Number of characters that have been read so far.
     */
    private int charsRead = 0;
    
    /**
     * Number of characters read at time of last mark() operation.
     */
    private int lastMark = -1;
    
    /**
     * Creates a new instance of LimitedLengthReader.
     * 
     * @param rdrIn Reader from which characters will be read.
     * @param maxLength Maximum number of characters to read from the supplied
     * Reader.
     * @param offset Skip this number of characters
     * 
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public LimitedSectionInputStream(final InputStream rdrIn, final int offset,
            final int maxLength) throws IOException {
        length = maxLength;
        rdr = rdrIn;
        long skipped = rdr.skip(offset);
        if (skipped != offset) {
            throw new IOException("Unable to skip the requested number of "
                    + "characters (" + offset + ").");
        }
    }
    
    /**
     * Close the stream. Once a stream has been closed, further read(),
     * ready(), mark(), or reset() invocations will throw an IOException.
     * Closing a previously-closed stream, however, has no effect.
     * @throws java.io.IOException If an I/O error occurs
     */
    @Override
    public void close() throws IOException {
        rdr.close();
    }
    
    /**
     * Mark the present position in the stream. Subsequent calls to reset()
     * will attempt to reposition the stream to this point. Not all
     * character-input streams support the mark() operation.
     * 
     * @param readAheadLimit Limit on the number of characters that may be read
     * while still preserving the mark. After reading this many characters,
     * attempting to reset the stream may fail.
     */
    @Override
    public void mark(final int readAheadLimit) {
        rdr.mark(readAheadLimit);
        lastMark = charsRead;
    }
    
    /**
     * Tell whether this stream supports the mark() operation, which it does.
     * @return Returns always true if the Reader supports mark()
     */
    @Override
    public boolean markSupported() {
        return rdr.markSupported();
    }
    
    /**
     * Read a single character. This method will block until a character is
     * available, an I/O error occurs, or the end of the stream is reached.
     * @throws java.io.IOException If an I/O error occurs
     * @return The character read, as an integer in the range 0 to 65535
     * (0x00-0xffff), or -1 if the end of the stream has been reached
     */
    @Override
    public int read() throws IOException {
        if (charsRead >= length) {
            return -1;
        } else {
            charsRead++;
            return rdr.read();
        }
    }
    
    /**
     * Read characters into an array. This method will block until some input
     * is available, an I/O error occurs, or the end of the stream is reached.
     * 
     * @param cbuf Destination buffer
     * 
     * @return The number of characters read, or -1 if the end of the stream has
     * been reached
     * 
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Override
    public int read(final byte[] cbuf) throws IOException {
        int len = Math.min(length - charsRead, cbuf.length);
        int n = rdr.read(cbuf, 0, len);
        if (n > 0) {
            charsRead += n;
        } else {
            n = -1;
        }
        return n;
    }
    
    /**
     * Read characters into a portion of an array. This method will block until
     * some input is available, an I/O error occurs, or the end of the stream
     * is reached.
     * 
     * @param cbuf Destination buffer
     * @param off Offset at which to start storing characters
     * @param len The number of characters read, or -1 if the end of the stream
     * has been reached.
     * 
     * @return The number of characters read, or -1 if the end of the stream has
     * been reached
     * 
     * @throws IOException If an I/O error occurs
     */
    @Override
    public int read(final byte[] cbuf, final int off, final int len)
            throws IOException {
        int maxLength = Math.min(length - charsRead, len);
        int n = rdr.read(cbuf, 0, maxLength);
        if (n > 0) {
            charsRead += n;
        } else {
            n = -1;
        }
        return n;
    }
    
    /**
     * Reset the stream. If the stream has been marked, then attempt to
     * reposition it at the mark. If the stream has not been marked, then
     * attempt to reset it in some way appropriate to the particular stream,
     * for example by repositioning it to its starting point. Not all
     * character-input streams support the reset() operation, and some support
     * reset() without supporting mark().
     * @throws java.io.IOException If an I/O error occurs
     */
    @Override
    public void reset() throws IOException {
        charsRead = lastMark;
        rdr.reset();
    }
    
    /**
     * Skip characters. This method will block until some characters are
     * available, an I/O error occurs, or the end of the stream is reached.
     * 
     * @param pN The number of characters to skip
     * 
     * @return The number of characters actually skipped
     * 
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Override
    public long skip(final long pN) throws IOException {
        long n = rdr.skip(pN);
        charsRead += n;
        return n;
    }
}
