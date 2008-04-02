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
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

/**
 * FlateEncode filter.
 * 
 * @author Paul Wagenaars
 */
public class FlateEncode extends DeflaterOutputStream {
    
    /** Indicates whether this filter is closed. */
    private boolean isClosed = false;
    
    /**
     * Creates a new FlateEncode filter.
     * 
     * @param out The output stream to which encoded bytes are written.
     */
    public FlateEncode(final OutputStream out) {
        super(out, new Deflater(Deflater.BEST_COMPRESSION));
    }
    
    /**
     * Compress a single byte and write it to the output stream.
     * 
     * @param b The byte.
     * 
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Override
    public void write(final int b) throws IOException {
        if (isClosed) {
            throw new IOException();
        }
        super.write(b);
    }
    
    /**
     * Closes this filter. Note that this function does not close the output
     * stream.
     * 
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Override
    public void close() throws IOException {
        finish();
        isClosed = true;
    }
    
}
