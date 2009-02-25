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

import java.io.InputStream;

import net.sf.eps2pgf.ps.VM;
import net.sf.eps2pgf.ps.errors.PSError;
import net.sf.eps2pgf.ps.errors.PSErrorTypeCheck;
import net.sf.eps2pgf.ps.errors.PSErrorUnregistered;
import net.sf.eps2pgf.ps.objects.PSObject;
import net.sf.eps2pgf.ps.objects.PSObjectArray;
import net.sf.eps2pgf.ps.objects.PSObjectDict;
import net.sf.eps2pgf.ps.objects.PSObjectFile;
import net.sf.eps2pgf.ps.objects.PSObjectInt;
import net.sf.eps2pgf.ps.objects.PSObjectName;
import net.sf.eps2pgf.ps.objects.PSObjectString;
import net.sf.eps2pgf.util.ArrayStack;

/**
 * Utility class for the creation of encode and decode filters.
 */
public final class FilterManager {
    
    /** Key of CloseSource field in parameter dictionary. */
    public static final PSObjectName KEY_CLOSESOURCE =
        new PSObjectName("/CloseSource");
    
    /** Key of CloseTarget field in parameter dictionary. */
    public static final PSObjectName KEY_CLOSETARGET =
        new PSObjectName("/CloseTarget");
    
    /** Name of ASCIIHexDecode filter. */
    public static final PSObjectName FILTER_ASCIIHEXDECODE =
        new PSObjectName("/ASCIIHexDecode");
    
    /** Name of ASCII85Decode filter. */
    public static final PSObjectName FILTER_ASCII85DECODE =
        new PSObjectName("/ASCII85Decode");
    
    /** Name of FlateDecode filter. */
    public static final PSObjectName FILTER_FLATEDECODE =
        new PSObjectName("/FlateDecode");
    
    /** Name of RunLengthDecode filter. */
    public static final PSObjectName FILTER_RUNLENGTHDECODE =
        new PSObjectName("/RunLengthDecode");
    
    /** Name of RunLengthEncode filter. */
    public static final PSObjectName FILTER_RUNLENGTHENCODE =
        new PSObjectName("/RunLengthEncode");
    
    /** Name of SubFileDecode filter. */
    public static final PSObjectName FILTER_SUBFILEDECODE =
        new PSObjectName("/SubFileDecode");
    
    
    /**
     * "Hidden" constructor.
     */
    private FilterManager() {
        /* empty block */
    }
    
    /**
     * Reads the filter arguments (if any) from the stack and puts them in a
     * dictionary.
     * 
     * @param stack The stack from which parameters are read.
     * @param name The name of the filter for which the parameters must be read.
     * @param vm The virtual memory manager.
     * 
     * @return Dictionary with parameters.
     * 
     * @throws PSError A PostScript error occurred.
     */
    public static PSObjectDict getParameters(final PSObjectName name,
            final ArrayStack<PSObject> stack, final VM vm)
            throws PSError {
        
        PSObjectDict dict;
        
        // There are some filters which have different possibilities for
        // arguments. 
        if (name.eq(FILTER_SUBFILEDECODE)) {
            dict = getParametersSubFileDecode(name, stack, vm);
        } else if (name.eq(FILTER_RUNLENGTHENCODE)) {
            dict = getParametersRunLengthEncode(name, stack, vm);
        } else {
            // The parameters of the filter have the same possibilities:
            // For example for the DCTDecode filter:
            // source /DCTDecode filter
            // source dictionary /DCTDecode filter
            PSObject obj = stack.pop();
            if (obj instanceof PSObjectDict) {
                dict = (PSObjectDict) obj;
            } else {
                stack.push(obj);
                dict = new PSObjectDict(vm);
            }
        }
        
        return dict;
    }
    
    
    /**
     * Reads the filter arguments (if any) for the SubFileDecode filter from the
     * stack and puts them in a dictionary.
     * 
     * From the PostScript manual:
     * source EODCount EODString /SubFileDecode filter
     * source dictionary EODCount EODString /SubFileDecode filter
     * source dictionary /SubFileDecode filter
     * 
     * @param stack The stack from which parameters are read.
     * @param name The name of the filter for which the parameters must be read.
     * @param vm The virtual memory manager.
     * 
     * @return Dictionary with parameters.
     * 
     * @throws PSError A PostScript error occurred.
     */
    private static PSObjectDict getParametersSubFileDecode(
            final PSObjectName name, final ArrayStack<PSObject> stack,
            final VM vm) throws PSError {
        
        PSObjectDict dict;
        PSObject obj = stack.pop();
        if (obj instanceof PSObjectDict) {
            dict = (PSObjectDict) obj;
        } else {
            PSObjectString eodString = obj.toPSString();
            PSObjectInt eodCount = new PSObjectInt(stack.pop().toInt());
            obj = stack.pop();
            if (obj instanceof PSObjectDict) {
                dict = (PSObjectDict) obj;
            } else {
                stack.push(obj);
                dict = new PSObjectDict(vm);
            }
            dict.setKey(SubFileDecode.KEY_EODSTRING, eodString);
            dict.setKey(SubFileDecode.KEY_EODCOUNT, eodCount);
        }

        return dict;
    }
    
    
    /**
     * Reads the filter arguments (if any) for the RunLengthEncode filter from
     * the stack and puts them in a dictionary.
     * 
     * From the PostScript manual:
     * target recordsize /RunLengthEncode filter
     * target dictionary recordsize /RunLengthEncode filter
     * 
     * @param stack The stack from which parameters are read.
     * @param name The name of the filter for which the parameters must be read.
     * @param vm The virtual memory manager.
     * 
     * @return Dictionary with parameters.
     * 
     * @throws PSError A PostScript error occurred.
     */
    private static PSObjectDict getParametersRunLengthEncode(
            final PSObjectName name, final ArrayStack<PSObject> stack,
            final VM vm) throws PSError {
        
        int recordSize = stack.pop().toInt();
        PSObjectDict dict;
        PSObject obj = stack.pop();
        if (obj instanceof PSObjectDict) {
            dict = (PSObjectDict) obj;
        } else {
            stack.push(obj);
            dict = new PSObjectDict(vm);
        }
        dict.setKey("RecordSize", new PSObjectInt(recordSize));

        return dict;
    }

    /**
     * Implementation of filter operator. It sets up a new file object that
     * encodes or decodes data from another file object.
     * 
     * @param filterName The filter name.
     * @param paramDict The dictionary with filter parameters
     * @param sourceOrTarget The data source or target (depending whether it's
     * a decode or encode filter).
     * @param vm The virtual memory manager.
     * 
     * @return The new file object with encode/decode filter wrapped around it.
     * 
     * @throws PSError A PostScript error occurred.
     */
    public static PSObjectFile filter(final PSObjectName filterName,
            final PSObjectDict paramDict, final PSObject sourceOrTarget,
            final VM vm) throws PSError {
        
        String name = filterName.toString();
        
        if (name.endsWith("Decode")) {
            return filterDecode(filterName, paramDict, sourceOrTarget, vm);
        } else if (name.endsWith("Encode")) {
            return filterEncode(filterName, paramDict, sourceOrTarget);
        } else {
            throw new PSErrorTypeCheck();
        }
    }
    
    /**
     * Implementation of decode filter.
     * 
     * @param name The filter name.
     * @param paramDict The dictionary with filter parameters
     * @param source The raw data source.
     * @param vm The virtual memory manager.
     * 
     * @return A new file object with decode filter wrapped around it.
     * 
     * @throws PSError A PostScript error occurred.
     */
    private static PSObjectFile filterDecode(final PSObjectName name,
            final PSObjectDict paramDict, final PSObject source,
            final VM vm) throws PSError {
        
        InputStream inStream;
        if (source instanceof PSObjectFile) {
            inStream = source.toFile().getStream();
        } else if (source instanceof PSObjectString) {
            throw new PSErrorUnregistered("Using string source for filter");
        } else if (source instanceof PSObjectArray) {
            @SuppressWarnings("unused")
            PSObjectArray proc = source.toProc();
            throw new PSErrorUnregistered("Using procedure source for filter");
        } else {
            throw new PSErrorTypeCheck();
        }
        
        InputStream filteredStream;
        if (name.eq(FILTER_ASCIIHEXDECODE)) {
            filteredStream = new ASCIIHexDecode(inStream, paramDict);
        } else if (name.eq(FILTER_ASCII85DECODE)) {
            filteredStream = new ASCII85Decode(inStream, paramDict);
        } else if (name.eq(FILTER_FLATEDECODE)) {
            filteredStream = new FlateDecode(inStream, paramDict);
        } else if (name.eq(FILTER_RUNLENGTHDECODE)) {
            filteredStream = new RunLengthDecode(inStream, paramDict);
        } else if (name.eq(FILTER_SUBFILEDECODE)) {
            filteredStream = new SubFileDecode(inStream, paramDict);
        } else {
            throw new PSErrorUnregistered("Decode filter or type " + name);
        }
        
        return new PSObjectFile(filteredStream, vm);
    }
        

    /**
     * Implementation of encode filter.
     * 
     * @param name The filter name.
     * @param paramDict The dictionary with filter parameters
     * @param target The data target.
     * 
     * @return A new file object with encode filter wrapped around it.
     * 
     * @throws PSError A PostScript error occurred.
     */
    private static PSObjectFile filterEncode(final PSObjectName name,
            final PSObjectDict paramDict, final PSObject target)
            throws PSError {
        
        throw new PSErrorUnregistered("Encode filters");
    }

    /**
     * Checks whether a specific filter is supported or not.
     * 
     * @param key Key describing filter.
     * 
     * @return true if filter is supported, false if filter is not
     * supported.
     * 
     * @throws PSError A PostScript error occurred.
     */
    public static boolean filterStatus(final PSObject key)
            throws PSError {

        String name = String.format("net.sf.eps2pgf.ps.resources.filters.%s",
                key.toString());
        
        try {
            Class.forName(name);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
    
}
