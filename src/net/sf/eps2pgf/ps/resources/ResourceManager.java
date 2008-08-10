/*
 * ResourceManager.java
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

package net.sf.eps2pgf.ps.resources;

import java.util.logging.Logger;

import net.sf.eps2pgf.ProgramError;
import net.sf.eps2pgf.ps.errors.PSError;
import net.sf.eps2pgf.ps.errors.PSErrorUnimplemented;
import net.sf.eps2pgf.ps.objects.PSObject;
import net.sf.eps2pgf.ps.objects.PSObjectArray;
import net.sf.eps2pgf.ps.objects.PSObjectBool;
import net.sf.eps2pgf.ps.objects.PSObjectDict;
import net.sf.eps2pgf.ps.objects.PSObjectInt;
import net.sf.eps2pgf.ps.objects.PSObjectName;
import net.sf.eps2pgf.ps.resources.colors.ColorManager;
import net.sf.eps2pgf.ps.resources.filters.FilterManager;
import net.sf.eps2pgf.ps.resources.fonts.FontManager;

/**
 * Manages all resources (or delegates other managers for specific resource
 * types).
 * 
 * @author Paul Wagenaars
 */
public final class ResourceManager {
    
    /** The log. */
    private static final Logger LOG
        = Logger.getLogger("net.sourceforge.eps2pgf");

    /** Key of /Font resource category. */
    public static final PSObjectName CAT_FONT
        = new PSObjectName("/Font");
    
    /** Key of /CIDFont resource category. */
    public static final PSObjectName CAT_CIDFONT
        = new PSObjectName("/CIDFont");
    
    /** Key of /CMAP resource category. */
    public static final PSObjectName CAT_CMAP
        = new PSObjectName("/CMAP");
    
    /** Key of /FontSet resource category. */
    public static final PSObjectName CAT_FONTSET
        = new PSObjectName("/FontSet");
    
    /** Key of /Encoding resource category. */
    public static final PSObjectName CAT_ENCODING
        = new PSObjectName("/Encoding");
    
    /** Key of /Form resource category. */
    public static final PSObjectName CAT_FORM
        = new PSObjectName("/Form");
    
    /** Key of /Pattern resource category. */
    public static final PSObjectName CAT_PATTERN
        = new PSObjectName("/Pattern");
    
    /** Key of /ProcSet resource category. */
    public static final PSObjectName CAT_PROCSET
        = new PSObjectName("/ProcSet");
    
    /** Key of /ColorSpace resource category. */
    public static final PSObjectName CAT_COLORSPACE
        = new PSObjectName("/ColorSpace");
    
    /** Key of /Halftone resource category. */
    public static final PSObjectName CAT_HALFTONE
        = new PSObjectName("/Halftone");
    
    /** Key of /ColorRendering resource category. */
    public static final PSObjectName CAT_COLORRENDERING
        = new PSObjectName("/ColorRendering");
    
    /** Key of /IdiomSet resource category. */
    public static final PSObjectName CAT_IDIOMSET
        = new PSObjectName("/IdiomSet");
    
    /** Key of /InkParams resource category. */
    public static final PSObjectName CAT_INKPARAMS
        = new PSObjectName("/InkParams");
    
    /** Key of /TrapParams resource category. */
    public static final PSObjectName CAT_TRAPPARAMS
        = new PSObjectName("/TrapParams");
    
    /** Key of /OutputDevice resource category. */
    public static final PSObjectName CAT_OUTPUTDEVICE
        = new PSObjectName("/OutputDevice");
    
    /** Key of /ControlLanguage resource category. */
    public static final PSObjectName CAT_CONTROLLANGUAGE
        = new PSObjectName("/ControlLanguage");
    
    /** Key of /Localization resource category. */
    public static final PSObjectName CAT_LOCALIZATION
        = new PSObjectName("/Localization");
    
    /** Key of /PDL resource category. */
    public static final PSObjectName CAT_PDL
        = new PSObjectName("/PDL");
    
    /** Key of /HWOptions resource category. */
    public static final PSObjectName CAT_HWOPTIONS
        = new PSObjectName("/HWOptions");
    
    /** Key of /FontType resource category. */
    public static final PSObjectName CAT_COLORSPACEFAMILY
        = new PSObjectName("/ColorSpaceFamily");
    
    /** Key of /FontType resource category. */
    public static final PSObjectName CAT_FONTTYPE
        = new PSObjectName("/FontType");
    
    /** Key of /Filter resource category. */
    public static final PSObjectName CAT_FILTER
        = new PSObjectName("/Filter");
    
    /** Array with all regular resources. */
    private static final PSObjectName[] REGULAR_CATS = {CAT_FONT,
        CAT_CIDFONT, CAT_CMAP, CAT_FONTSET, CAT_ENCODING, CAT_FORM, CAT_PATTERN,
        CAT_PROCSET, CAT_COLORSPACE, CAT_HALFTONE, CAT_COLORRENDERING,
        CAT_IDIOMSET, CAT_INKPARAMS, CAT_TRAPPARAMS, CAT_OUTPUTDEVICE,
        CAT_CONTROLLANGUAGE, CAT_LOCALIZATION, CAT_PDL, CAT_HWOPTIONS};
   
    /** Font manager. */
    private FontManager fontManager;
    
    /** Dictionary with a dictionary for each resource category. */
    private PSObjectDict resources;
    
    /**
     * Create a new resource manager.
     * 
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    public ResourceManager() throws ProgramError {
        fontManager = new FontManager();
        
        resources = new PSObjectDict();
        for (int i = 0; i < REGULAR_CATS.length; i++) {
            resources.setKey(REGULAR_CATS[i], new PSObjectDict());
        }
    }
    
    /**
     * Define a new resource of the given category and with the given key.
     * 
     * @param category Resource category.
     * @param key Key to be associated with this resource.
     * @param instance The resource itself.
     * 
     * @return The new resource instance.
     * 
     * @throws PSError A PostScript error occurred.
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    public PSObject defineResource(final PSObjectName category,
            final PSObject key, final PSObject instance)
            throws PSError, ProgramError {
        
        LOG.fine("Defining resource " + category.isis()
                + " " + key.isis() + " " + instance.isis());
        // Do a type check of the new instance
        if (category.eq(CAT_FONT)) {
            // Font resources are handled separately by a FontManager.
            // Therefore control in handed over to it.
            return fontManager.defineFont(key, instance.toFont());
        } else if (category.eq(CAT_COLORSPACE)) {
            ColorManager.autoSetColorSpace(instance);
        }
        
        // Define the new resource in the dictionary.
        PSObjectDict catDict = resources.get(category).toDict();
        catDict.setKey(key, instance);
        
        instance.readonly();
       
        return instance;
    }
    
    /**
     * Check the status of a resource. Check the PostScript manual for the
     * operator 'resourcestatus'.
     * 
     * @param category The category.
     * @param key The key.
     * 
     * @return the pS object array
     * 
     * @throws PSError A PostScript error occurred.
     */
    public PSObjectArray resourceStatus(final PSObjectName category,
            final PSObject key) throws PSError {
        
        boolean supported;
        
        if (category.eq(CAT_FONTTYPE)) {
            supported = fontManager.fontTypeStatus(key);
        } else if (category.eq(CAT_FILTER)) {
            supported = FilterManager.filterStatus(key);
        } else if (category.eq(CAT_COLORSPACEFAMILY)) {
            supported = ColorManager.colorSpaceFamilyStatus(key);
        } else {
            throw new PSErrorUnimplemented("Checking resources of type: "
                    + category.isis());
        }
        
        PSObjectArray ret = new PSObjectArray();
        if (supported) {
            ret.addToEnd(new PSObjectInt(0));
            ret.addToEnd(new PSObjectInt(0));
        }
        ret.addToEnd(new PSObjectBool(supported));
        
        return ret;
    }
    
    /**
     * Gets the font manager.
     * 
     * @return The font manager.
     */
    public FontManager getFontManager() {
        return fontManager;
    }

}
