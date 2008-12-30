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
import java.io.InputStream;
import java.util.zip.InflaterInputStream;

import net.sf.eps2pgf.ps.errors.PSError;
import net.sf.eps2pgf.ps.objects.PSObject;
import net.sf.eps2pgf.ps.objects.PSObjectDict;

/**
 * FlateEncode filter.
 * 
 * @author Paul Wagenaars
 */
public class FlateDecode extends InflaterInputStream {
    
    /** Indicates whether this filter is closed. */
    private boolean isClosed = false;
    
    /** CloseSource parameter. */
    private boolean closeSource;
    
    /**
     * Creates a new FlateDecode filter.
     * 
     * @param in The source input stream.
     * @param dict The parameter dictionary.
     * 
     * @throws PSError A PostScript error occurred.
     */
    public FlateDecode(final InputStream in, final PSObjectDict dict)
            throws PSError {
        
        super(in);
        
        PSObject obj = dict.lookup(FilterManager.KEY_CLOSESOURCE);
        if (obj != null) {
            closeSource = obj.toBool();
        } else {
            closeSource = false;
        }

    }
    
    /**
     * Returns 0 after EOF has been reached, otherwise always return 1.
     * 
     * @return Returns 0 after EOF has been reached, otherwise always return 1.
     * 
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Override
    public int available() throws IOException {
        if (isClosed) {
            throw new IOException();
        }
        return super.available();
    }
    
    /**
     * Reads a byte of uncompressed data. This method will block until enough
     * input is available for decompression.
     * 
     * @return The byte read, or -1 if end of compressed input is reached.
     * 
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Override
    public int read() throws IOException {
        if (isClosed) {
            throw new IOException();
        }
        return super.read();
    }
    
    /**
     * Reads uncompressed data into an array of bytes. If len is not zero, the
     * method will block until some input can be decompressed; otherwise, no
     * bytes are read and 0 is returned.
     * 
     * @param b The buffer into which the data is read.
     * @param off The start offset in the destination array b.
     * @param len The maximum number of bytes read.
     * 
     * @return The actual number of bytes read, or -1 if the end of the
     * compressed input is reached or a preset dictionary is needed.
     * 
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Override
    public int read(final byte[] b, final int off, final int len)
            throws IOException {
        
        if (isClosed) {
            throw new IOException();
        }
        return super.read(b, off, len);
    }
    
    /**
     * Closes this filter. Note that this function does not close the source
     * input stream.
     * 
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Override
    public void close() throws IOException {
        if (closeSource) {
            super.close();
        }
        isClosed = true;
    }
    
}
