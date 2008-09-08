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

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

import net.sf.eps2pgf.ps.errors.PSError;
import net.sf.eps2pgf.ps.objects.PSObject;
import net.sf.eps2pgf.ps.objects.PSObjectDict;

/**
 * FlateEncode filter.
 * 
 * @author Paul Wagenaars
 */
public class FlateEncode extends DeflaterOutputStream {
    
    /** Indicates whether this filter is closed. */
    private boolean isClosed = false;
    
    /** CloseTarget parameter. */
    private boolean closeTarget;
    
    /**
     * Creates a new FlateEncode filter.
     * 
     * @param out The output stream to which encoded bytes are written.
     * @param dict The parameter dictionary.
     * 
     * @throws PSError A PostScript error occurred.
     */
    public FlateEncode(final OutputStream out, final PSObjectDict dict)
            throws PSError {
        
        super(out, new Deflater(Deflater.BEST_COMPRESSION));
        
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
     * Flushes this output stream and forces any buffered output bytes to be
     * written out.
     * 
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Override
    public void flush() throws IOException {
        if (isClosed) {
            throw new IOException();
        }
        super.flush();
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
     * Writes b.length bytes from the specified byte array to this output
     * stream.
     * 
     * @param b The data.
     * 
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Override
    public void write(final byte[] b) throws IOException {
        if (isClosed) {
            throw new IOException();
        }
        super.write(b);
    }
    
    /**
     * Writes len bytes from the specified byte array starting at offset off to
     * this output stream.
     * 
     * @param b The data.
     * @param off The start offset in the data.
     * @param len The number of bytes to write.
     * 
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Override
    public void write(final byte[] b, final int off, final int len)
            throws IOException {
        
        if (isClosed) {
            throw new IOException();
        }
        super.write(b, off, len);
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
        
        if (closeTarget) {
            super.close();
        }
        isClosed = true;
    }
    
}
