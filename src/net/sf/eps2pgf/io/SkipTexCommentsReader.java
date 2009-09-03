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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

/**
 * The Class SkipTexCommentsReader.
 * This is a filter that skips all TeX comments.
 */
public class SkipTexCommentsReader extends Reader {
    
    /** Reader from which the characters are read. */
    private Reader reader;
    
    /** Last read character. */
    private int lastChar;
    
    /** lastChar as it was at the time of the last mark(). */
    private int lastMarkLastChar;
    
    /**
     * Instantiates a new reader.
     * 
     * @param pReader The reader from which characters are read.
     */
    public  SkipTexCommentsReader(final Reader pReader) {
        super();
        
        if (pReader.markSupported()) {
            reader = pReader;
        } else {
            reader = new BufferedReader(pReader);
        }
        lastChar = -1;
        lastMarkLastChar = -1;
    }

    /**
     * Close this reader.
     * 
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Override
    public void close() throws IOException {
        reader.close();
        lastChar = -1;
        lastMarkLastChar = -1;
    }
    
    /**
     * Marks the present position in the stream. Subsequent calls to reset()
     * will attempt to reposition the stream to this point. Not all
     * character-input streams support the mark() operation.
     * 
     * @param readlimit Limit on the number of characters that may be read
     * while still preserving the mark. After reading this many characters,
     * attempting to reset the stream may fail.
     * 
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Override
    public void mark(final int readlimit) throws IOException {
        // The output might contain comments. Therefore, we increase the read
        // the readlimit by 160, representing two comment lines.
        reader.mark(readlimit + 160);
        
        lastMarkLastChar = lastChar;
    }
    
    /**
     * Tells whether this stream supports the mark() operation.
     * 
     * @return Returns always true.
     */
    @Override
    public boolean markSupported() {
        return true;
    }
    
    /**
     * Read a single character.
     * 
     * @return Character code.
     * 
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Override
    public int read() throws IOException {
        boolean inComment = false;
        boolean consumeNext = false;
        int c;
        do {
            c = reader.read();
            if (!inComment && (c == '%') && (this.lastChar != '\\')) {
                inComment = true;
            } else if (inComment && (c == '\n')) {
                inComment = false;
                consumeNext = true;
            } else if (inComment && (c == '\r')) {
                // On windows newline are formed by \r\n, we consume the \n too
                reader.mark(1);
                if (reader.read() != '\n') {
                    reader.reset();
                }
                inComment = false;
                consumeNext = true;
            } else if (inComment && (c == -1)) {
                inComment = false;
                consumeNext = false;
            } else {
                consumeNext = false;
            }
        } while (inComment || consumeNext);
        this.lastChar = c;

        return c;        
    }

    /**
     * Read a set of characters and stores them in the provided buffer.
     * 
     * @param cbuf Buffer to store the characters.
     * @param off Offset at which to start storing characters.
     * @param len Maximum number of characters to read.
     * 
     * @return The number of characters read, or -1 if the end of the stream
     * has been reached.
     * 
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Override
    public int read(final char[] cbuf, final int off, final int len)
            throws IOException {
        
        // CHECKSTYLE:OFF
        /*
         * The code below is copied from InputStream.java from the Apache
         * Harmony class library, with some small adjustments for Eps2pgf.
         * 
         * Copyright 2006, 2007 The Apache Software Foundation
         * 
         */
        
        // avoid int overflow, check null cbuf
        if (off > cbuf.length || off < 0 || len < 0
                || len > cbuf.length - off) {
            throw new ArrayIndexOutOfBoundsException();
        }
        for (int i = 0; i < len; i++) {
            int c;
            try {
                
                if ((c = read()) == -1) {
                    return i == 0 ? -1 : i;
                }
                
            } catch (IOException e) {
                if (i != 0) {
                    return i;
                }
                throw e;
            }
            cbuf[off + i] = (char) c;
        }
        return len;
        // CHECKSTYLE:ON
    }
    
    /**
     * Tells whether this stream is ready to be read.
     * 
     * @return True if the next read() is guaranteed not to block for input,
     * false otherwise. Note that returning false does not guarantee that the
     * next read will block.
     * 
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Override
    public boolean ready() throws IOException {
        return reader.ready();
    }
    
    /**
     * Resets the stream. If the stream has been marked, then attempt to
     * reposition it at the mark. If the stream has not been marked, then
     * attempt to reset it in some way appropriate to the particular stream, for
     * example by repositioning it to its starting point. Not all
     * character-input streams support the reset() operation, and some support
     * reset() without supporting mark().
     * 
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Override
    public void reset() throws IOException {
        reader.reset();
        lastChar = lastMarkLastChar;
    }

}
