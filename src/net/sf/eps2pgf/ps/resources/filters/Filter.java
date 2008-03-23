/*
 * Filter.java
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

import java.io.InputStream;

import net.sf.eps2pgf.ps.errors.PSErrorTypeCheck;
import net.sf.eps2pgf.ps.errors.PSErrorUnimplemented;
import net.sf.eps2pgf.ps.objects.PSObjectArray;
import net.sf.eps2pgf.ps.objects.PSObjectDict;
import net.sf.eps2pgf.ps.objects.PSObjectFile;
import net.sf.eps2pgf.ps.objects.PSObjectName;

/**
 * Utility class for the creation of encode and decode filters.
 */
public final class Filter {
    
    /**
     * "Hidden" constructor.
     */
    private Filter() {
        /* empty block */
    }
    
    /**
     * Implementation of filter operator. It sets up a new file object that
     * encodes or decodes data from another file object.
     * 
     * @param data The data source or target (depending on filter type)
     * @param dict The dictionary with filter parameters.
     * @param params Additional parameters used by some filters.
     * @param filterName The filter name.
     * 
     * @return The new file object with encode/decode filter wrapped around it.
     * 
     * @throws PSErrorUnimplemented Encountered a PostScript feature that is not
     * (yet) implemented.
     * @throws PSErrorTypeCheck A PostScript typecheck error occurred.
     */
    public static PSObjectFile filter(final PSObjectFile data,
            final PSObjectDict dict, final PSObjectArray params,
            final PSObjectName filterName)
            throws PSErrorUnimplemented, PSErrorTypeCheck {
        
        String name = filterName.toString();
        PSObjectFile filterFile;
        
        if (name.equals("ASCIIHexDecode")) {
            InputStream inStream = data.getStream();
            InputStream filterStream = new ASCIIHexDecode(inStream);
            filterFile = new PSObjectFile(filterStream);
        } else if (name.equals("ASCII85Decode")) {
            InputStream inStream = data.getStream();
            InputStream filterStream = new ASCII85Decode(inStream);
            filterFile = new PSObjectFile(filterStream);
        } else if (name.equals("LZWDecode")) {
            throw new PSErrorUnimplemented("Filter type: " + name);
        } else if (name.equals("FlateDecode")) {
            throw new PSErrorUnimplemented("Filter type: " + name);
        } else if (name.equals("RunLengthDecode")) {
            InputStream inStream = data.getStream();
            InputStream filterStream = new RunLengthDecode(inStream);
            filterFile = new PSObjectFile(filterStream);
        } else if (name.equals("CCITTFaxDecode")) {
            throw new PSErrorUnimplemented("Filter type: " + name);
        } else if (name.equals("DCTDecode")) {
            throw new PSErrorUnimplemented("Filter type: " + name);
        } else if (name.equals("SubFileDecode")) {
            throw new PSErrorUnimplemented("Filter type: " + name);
        } else if (name.equals("ReusableStreamDecode")) {
            throw new PSErrorUnimplemented("Filter type: " + name);
        } else if (name.equals("ASCIIHexEncode")) {
            throw new PSErrorUnimplemented("Filter type: " + name);
        } else if (name.equals("ASCII85Encode")) {
            throw new PSErrorUnimplemented("Filter type: " + name);
        } else if (name.equals("LZWEncode")) {
            throw new PSErrorUnimplemented("Filter type: " + name);
        } else if (name.equals("FlateEncode")) {
            throw new PSErrorUnimplemented("Filter type: " + name);
        } else if (name.equals("RunLengthEncode")) {
            throw new PSErrorUnimplemented("Filter type: " + name);
        } else if (name.equals("CCITTFaxEncode")) {
            throw new PSErrorUnimplemented("Filter type: " + name);
        } else if (name.equals("DCTEncode")) {
            throw new PSErrorUnimplemented("Filter type: " + name);
        } else if (name.equals("NullEncode")) {
            throw new PSErrorUnimplemented("Filter type: " + name);
        } else {
            throw new PSErrorTypeCheck();
        }
        
        return filterFile;
    }
    
    /**
     * Returns the number of param_i operands required by a filter.
     * 
     * @param filterName Filter name
     * 
     * @return Number of required param operands.
     */
    public static int getNrParamOperands(final PSObjectName filterName) {
        return 0;
    }
}
