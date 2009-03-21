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


package net.sf.eps2pgf.ps.resources.encodings;

import net.sf.eps2pgf.ProgramError;
import net.sf.eps2pgf.ps.VM;
import net.sf.eps2pgf.ps.errors.PSError;
import net.sf.eps2pgf.ps.objects.PSObjectArray;
import net.sf.eps2pgf.ps.objects.PSObjectName;
import net.sf.eps2pgf.ps.resources.ResourceManager;

/**
 * Manages different encoding vectors.
 * 
 * @author Paul Wagenaars
 *
 */
public final class EncodingManager {
    
    // Make constants for some commonly used symbols
    /** .notdef symbol. */
    public static final PSObjectName SYMB_NOTDEF = new PSObjectName("/.notdef");
    
    /** space symbol. */
    public static final PSObjectName SYMB_SPACE = new PSObjectName("/space");
    
    /** exclam symbol. */
    public static final PSObjectName SYMB_EXCLAM = new PSObjectName("/exclam");
    
    /** zero symbol. */
    public static final PSObjectName SYMB_ZERO = new PSObjectName("/zero");
    
    /** one symbol. */
    public static final PSObjectName SYMB_ONE = new PSObjectName("/one");
    
    /** two symbol. */
    public static final PSObjectName SYMB_TWO = new PSObjectName("/two");
    
    /** three symbol. */
    public static final PSObjectName SYMB_THREE = new PSObjectName("/three");
    
    /** four symbol. */
    public static final PSObjectName SYMB_FOUR = new PSObjectName("/four");
    
    /** five symbol. */
    public static final PSObjectName SYMB_FIVE = new PSObjectName("/five");
    
    /** six symbol. */
    public static final PSObjectName SYMB_SIX = new PSObjectName("/six");
    
    /** seven symbol. */
    public static final PSObjectName SYMB_SEVEN = new PSObjectName("/seven");
    
    /** eight symbol. */
    public static final PSObjectName SYMB_EIGHT = new PSObjectName("/eight");
    
    /** nine symbol. */
    public static final PSObjectName SYMB_NINE = new PSObjectName("/nine");
    
    /**
     * "Hidden" constructor.
     */
    private EncodingManager() {
        /* empty block */
    }
    
    
    /**
     * Initialize the encoding manager. It defines the standard encodings in
     * the /Encoding resource category.
     * 
     * @param manager The resource manager to which the standard encodings are
     * added.
     * @param vm The virtual memory manager.
     * 
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     * @throws PSError A PostScript error occurred.
     */
    public static void initialize(final ResourceManager manager, final VM vm)
            throws PSError, ProgramError {
        
        manager.defineResource(ResourceManager.CAT_ENCODING, 
                StandardEncoding.NAME, 
                new PSObjectArray(StandardEncoding.get(), vm));
        
        manager.defineResource(ResourceManager.CAT_ENCODING, 
                ISOLatin1Encoding.NAME,
                new PSObjectArray(ISOLatin1Encoding.get(), vm));
        
        manager.defineResource(ResourceManager.CAT_ENCODING, 
                SymbolEncoding.NAME, 
                new PSObjectArray(SymbolEncoding.get(), vm));
    }
}
