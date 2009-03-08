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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

/**
 * Provides random write access to an memory buffer that can be used as an
 * output stream. When the RandomAccessOutputStream is closed the buffer is
 * written to the target output stream.
 * 
 * @author Paul Wagenaars
 */
public class RandomAccessOutputStream extends ByteArrayOutputStream {
    
    /**
     * Buffered data will be written to this output stream when the
     * RandomAccessOutputStream is closed.
     */
    private OutputStream targetOut;
    
    /** The maximum count reached so far. */
    private int maxCount;
    
    /**
     * Instantiates a new random access output stream.
     * 
     * @param targetOutputStream The target output stream. When this
     */
    public RandomAccessOutputStream(final OutputStream targetOutputStream) {
        super();
        targetOut = targetOutputStream;
    }
    
    /**
     * Writes the data written to this stream to the target output stream. It is
     * equivalent to the writeTo() function.
     * 
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Override
    public void close() throws IOException {
        super.writeTo(targetOut);
    }
    
    
    /**
     * Gets the current file pointer.
     * 
     * @return The current pointer.
     */
    public int getPointer() {
        return super.count;
    }
    
    /**
     * Sets the file pointer to the given value.
     * 
     * @param newPosition The new position.
     * 
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public void seek(final int newPosition) throws IOException {
        if ((newPosition < 0) || (newPosition > maxCount)) {
            throw new IOException();
        }
        super.count = newPosition;
    }
    
    /**
     * Returns the number of valid bytes in this output stream.
     * 
     * @return The number of valid bytes in this output stream.
     */
    @Override
    public int size() {
        return maxCount;
    }
    
    /**
     * Resets the count field of this byte array output stream to zero, so that
     * all currently accumulated output in the output stream is discarded. The
     * output stream can be used again, reusing the already allocated buffer
     * space.
     */
    @Override
    public void reset() {
        super.reset();
        maxCount = 0;
    }
    
    /**
     * Writes len bytes from the specified byte array starting at offset off to
     * this byte array output stream.
     * 
     * @param b The data.
     * @param off The offset.
     * @param len The number of bytes to write.
     */
    @Override
    public void write(final byte[] b, final int off, final int len) {
        super.write(b, off, len);
        maxCount = Math.max(super.count, maxCount);
    }
    
    /**
     * Writes the specified byte to this byte array output stream.
     * 
     * @param b The byte to be written.
     */
    @Override
    public void write(final int b) {
        super.write(b);
        maxCount = Math.max(super.count, maxCount);
    }
    
    /**
     * Writes a string to this output stream. The string is interpreter using
     * the US-ASCII character set.
     * 
     * @param str The string to write.
     * 
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public void write(final String str) throws IOException {
        try {
            byte[] data = str.getBytes("US-ASCII");
            write(data, 0, data.length);
        } catch (UnsupportedEncodingException e) {
            throw new IOException("System does not support US-ASCII charset.");
        }
    }
    
    /**
     * Creates a newly allocated byte array. Its size is the current size of
     * this output stream and the valid contents of the buffer have been copied
     * into it.
     * 
     * @return A copy of the contents of the current byte array.
     */
    @Override
    public byte[] toByteArray() {
        int prevCount = super.count;
        super.count = maxCount;
        byte[] data = super.toByteArray();
        super.count = prevCount;
        return data;
    }
    
    /**
     * Converts the buffer's contents into a string decoding bytes using the
     * platform's default character set. The length of the new String  is a
     * function of the character set, and hence may not be equal to the size of
     * the buffer.
     * 
     * @return String decoded from the buffer's contents.
     */
    @Override
    public String toString() {
        int prevCount = super.count;
        super.count = maxCount;
        String data = super.toString();
        super.count = prevCount;
        return data;
    }
    
    /**
     * Converts the buffer's contents into a string by decoding the bytes using
     * the specified charsetName. The length of the new String is a function of
     * the charset, and hence may not be equal to the length of the byte array.
     * 
     * @param charsetName The character set name.
     * 
     * @return String decoded from the buffer's contents.
     * 
     * @throws UnsupportedEncodingException the unsupported encoding exception
     */
    @Override
    public String toString(final String charsetName)
            throws UnsupportedEncodingException {
        
        int prevCount = super.count;
        super.count = maxCount;
        String data = super.toString(charsetName);
        super.count = prevCount;
        return data;
    }
    
    /**
     * This function doesn do anything. The data is not written to the output
     * stream until this RandomAccessOutputStream is closed.
     * 
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Override
    public void flush() throws IOException {
        // empty block
    }
}
