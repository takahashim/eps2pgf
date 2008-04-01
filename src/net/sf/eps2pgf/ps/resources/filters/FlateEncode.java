/*
 * FlateEncode.java
 *
 * This file is part of Eps2pgf.
 *
 * Copyright 2008 Paul Wagenaars <paul@wagenaars.org>
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
import java.util.ArrayList;
import java.util.zip.Deflater;

/**
 * FlateEncode filter.
 * 
 * @author Paul Wagenaars
 */
public class FlateEncode extends OutputStream {
    
    /** Output stream to which compressed bytes are written. */
    private OutputStream out;
    
    /** Buffer with uncompressed bytes. */
    private ArrayList<Byte> buffer = new ArrayList<Byte>();
    
    /**
     * Creates a new FlateEncode filter.
     * 
     * @param pOut The output stream to which encoded bytes are written.
     */
    public FlateEncode(final OutputStream pOut) {
        out = pOut;
    }
    
    /**
     * Write a single byte to this output stream.
     * 
     * @param b The byte to write.
     * 
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Override
    public void write(final int b) throws IOException {
        if (out == null) {
            throw new IOException();
        }
        buffer.add((byte) (b & 0xff));
    }
    
    /**
     * Flushes all data in buffers, is possible.
     * 
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Override
    public void flush() throws IOException {
        if (out == null) {
            throw new IOException();
        }
        out.flush();
    }

    /**
     * Closes this filter. Note that this function does not close the output
     * stream.
     * 
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Override
    public void close() throws IOException {
        byte[] inputData = new byte[buffer.size()];
        for (int i = 0; i < inputData.length; i++) {
            inputData[i] = (byte) (buffer.get(i) & 0xff);
        }
        buffer.clear();
        
        Deflater deflater = new Deflater(Deflater.BEST_COMPRESSION);
        deflater.setInput(inputData);
        deflater.finish();
        byte[] output = new byte[1024];
        while (true) {
            int compressedDataLength = deflater.deflate(output);
            if (compressedDataLength == 0) {
                break;
            }
            out.write(output, 0, compressedDataLength);
        }
        out = null;
    }
    
}
