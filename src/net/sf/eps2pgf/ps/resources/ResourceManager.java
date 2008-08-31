/*
 * ResourceManager.java
 *
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

package net.sf.eps2pgf.ps.resources;

import java.util.logging.Logger;

import net.sf.eps2pgf.ProgramError;
import net.sf.eps2pgf.ps.errors.PSError;
import net.sf.eps2pgf.ps.errors.PSErrorTypeCheck;
import net.sf.eps2pgf.ps.errors.PSErrorUndefined;
import net.sf.eps2pgf.ps.errors.PSErrorUndefinedResource;
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
    
    /** Array with all regular resources. */
    private static final PSObjectName[] REGULAR_CATS = {CAT_FONT,
        CAT_CIDFONT, CAT_CMAP, CAT_FONTSET, CAT_ENCODING, CAT_FORM, CAT_PATTERN,
        CAT_PROCSET, CAT_COLORSPACE, CAT_HALFTONE, CAT_COLORRENDERING,
        CAT_IDIOMSET, CAT_INKPARAMS, CAT_TRAPPARAMS, CAT_OUTPUTDEVICE,
        CAT_CONTROLLANGUAGE, CAT_LOCALIZATION, CAT_PDL, CAT_HWOPTIONS};
   
    
    /** Key of /Filter resource category. */
    public static final PSObjectName CAT_FILTER
        = new PSObjectName("/Filter");
    
    /** Key of /ColorSpaceFamily resource category. */
    public static final PSObjectName CAT_COLORSPACEFAMILY
        = new PSObjectName("/ColorSpaceFamily");
    
    /** Key of /Emulator resource category. */
    public static final PSObjectName CAT_EMULATOR
        = new PSObjectName("/Emulator");
    
    /** Key of /IODevice resource category. */
    public static final PSObjectName CAT_IODEVICE
        = new PSObjectName("/IODevice");
    
    /** Key of /ColorRenderingType resource category. */
    public static final PSObjectName CAT_COLORRENDERINGTYPE
        = new PSObjectName("/ColorRenderingType");
    
    /** Key of /FMapType resource category. */
    public static final PSObjectName CAT_FMAPTYPE
        = new PSObjectName("/FMapType");
    
    /** Key of /FontType resource category. */
    public static final PSObjectName CAT_FONTTYPE
        = new PSObjectName("/FontType");
    
    /** Key of /FormType resource category. */
    public static final PSObjectName CAT_FORMTYPE
        = new PSObjectName("/FormType");
    
    /** Key of /HalftoneType resource category. */
    public static final PSObjectName CAT_HALFTONETYPE
        = new PSObjectName("/HalftoneType");
    
    /** Key of /ImageType resource category. */
    public static final PSObjectName CAT_IMAGETYPE
        = new PSObjectName("/ImageType");
    
    /** Key of /PatternType resource category. */
    public static final PSObjectName CAT_PATTERNTYPE
        = new PSObjectName("/PatternType");
    
    /** Key of /FunctionType resource category. */
    public static final PSObjectName CAT_FUNCTIONTYPE
        = new PSObjectName("/FunctionType");
    
    /** Key of /ShadingType resource category. */
    public static final PSObjectName CAT_SHADINGTYPE
        = new PSObjectName("/ShadingType");
    
    /** Key of /TrappingType resource category. */
    public static final PSObjectName CAT_TRAPPINGTYPE
        = new PSObjectName("/TrappingType");
    
    /** Array with all implicit resources. */
    private static final PSObjectName[] IMPLICIT_CATS = {CAT_FILTER,
        CAT_COLORSPACEFAMILY, CAT_EMULATOR, CAT_IODEVICE,
        CAT_COLORRENDERINGTYPE, CAT_FMAPTYPE, CAT_FONTTYPE, CAT_FORMTYPE,
        CAT_HALFTONETYPE, CAT_IMAGETYPE, CAT_PATTERNTYPE, CAT_FUNCTIONTYPE,
        CAT_SHADINGTYPE, CAT_TRAPPINGTYPE
    };
    
    // Define the special "categories" for defining new resources
    /** Key of /Category resource category. */
    public static final PSObjectName CAT_CATEGORY
        = new PSObjectName("/Category");
    
    /** Key of /Generic resource category. */
    public static final PSObjectName CAT_GENERIC
        = new PSObjectName("/Generic");
    
    // Keys in the resource implementation dictionary.
    /** Name object for DefineResource. */
    private static final PSObjectName KEY_DEFINERESOURCE
        = new PSObjectName("/DefineResource");
    
    /** Name object for UndefineResource. */
    private static final PSObjectName KEY_UNDEFINERESOURCE
        = new PSObjectName("/UndefineResource");
    
    /** Name object for FindResource. */
    private static final PSObjectName KEY_FINDRESOURCE
        = new PSObjectName("/FindResource");
    
    /** Name object for ResourceStatus. */
    private static final PSObjectName KEY_RESOURCESTATUS
        = new PSObjectName("/ResourceStatus");
    
    /** Name object for ResourceForAll. */
    private static final PSObjectName KEY_RESOURCEFORALL
        = new PSObjectName("/ResourceForAll");
    
    /** Name object for Category. */
    private static final PSObjectName KEY_CATEGORY
        = new PSObjectName("/Category");
    
    /** Generic entry for most keys in the Generic implementation dictionary. */
    private static final PSObjectName GENERIC_PROC_NAME
        = new PSObjectName("/Eps2pgfGenericProc");
    
    
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
        
        // Set the generic implementation dictionary for each resource category.
        PSObjectDict genericDict = createGenericDict(CAT_GENERIC);
        PSObjectDict categoryDict = new PSObjectDict();
        categoryDict.setKey(CAT_GENERIC, genericDict);
        categoryDict.setKey(CAT_CATEGORY, genericDict);
        
        resources = new PSObjectDict();
        resources.setKey(CAT_CATEGORY, categoryDict);
        resources.setKey(CAT_GENERIC, genericDict);
    }
    
    /**
     * Create a new generic implementation dictionary.
     * 
     * @param catName The cat name.
     * 
     * @return A new generic implementation dictionary.
     */
    private static PSObjectDict createGenericDict(final PSObjectName catName) {
        
        // Create a generic procedure
        PSObjectArray genericProc = new PSObjectArray();
        genericProc.setLiteral(false);
        genericProc.addToEnd(GENERIC_PROC_NAME);
        
        // Create the generic implementation dictionary
        PSObjectDict dict = new PSObjectDict();
        dict.setKey(KEY_DEFINERESOURCE, genericProc);
        dict.setKey(KEY_UNDEFINERESOURCE, genericProc);
        dict.setKey(KEY_FINDRESOURCE, genericProc);
        dict.setKey(KEY_RESOURCESTATUS, genericProc);
        dict.setKey(KEY_RESOURCEFORALL, genericProc);
        dict.setKey(KEY_CATEGORY, catName);
        try {
            dict.readonly();
        } catch (PSErrorTypeCheck e) {
            /* this can never happen */
        }
        return dict;
    }
    
    /**
     * Gets the implementation dictionary for the specified category. If the
     * specified category does not exist as PSErrorUndefined error is thrown.
     * 
     * @param catName The category name.
     * 
     * @return The requested implementation dictionary.
     * 
     * @throws PSError A PostScript error occurred.
     */
    private PSObjectDict getImplementationDict(final PSObjectName catName)
            throws PSError {
        
        PSObjectDict catDict = resources.get(CAT_CATEGORY).toDict();
        
        // First, we check whether it is already defined in the /Category
        // dictionary.
        if (catDict.known(catName)) {
            return catDict.get(catName).toDict();
        } else {
            // Check whether it is a standard category. It that case a generic
            // dictionary is created on the fly, added to the /Category
            // dictionary and returned.
            PSObjectName stdName = isStandardResourceCategory(catName);
            if (stdName != null) {
                PSObjectDict genericDict = createGenericDict(stdName);
                catDict.setKey(stdName, genericDict);
                return genericDict;
            }
        }
        
        throw new PSErrorUndefined();
    }
    
    /**
     * Check whether a category is a standard resource category.
     * 
     * @param name The category name to check.
     * 
     * @return The key to the requested resource category. If the requested
     * category is not a standard category <code>null</code> is returned.
     */
    private PSObjectName isStandardResourceCategory(final PSObjectName name) {
        for (int i = 0; i < REGULAR_CATS.length; i++) {
            if (name.eq(REGULAR_CATS[i])) {
                return REGULAR_CATS[i];
            }
        }

        return isImplicitResourceCategory(name);
    }
    
    /**
     * Check whether a category is an implicit resource category.
     * 
     * @param name The category name to check.
     * 
     * @return The key to the requested resource category. If the requested
     * category is not an implicit category <code>null</code> is returned.
     */
    private PSObjectName isImplicitResourceCategory(final PSObjectName name) {
        for (int i = 0; i < IMPLICIT_CATS.length; i++) {
            if (name.eq(IMPLICIT_CATS[i])) {
                return IMPLICIT_CATS[i];
            }
        }
    
        return null;
    }
    
    /**
     * Gets the dictionary with all resources from the specified category. If
     * the specified category does not exist an undefined error is thrown.
     * 
     * @param category The category.
     * 
     * @return The dictionary with resources in the requested category.
     * 
     * @throws PSError A PostScript error occurred.
     */
    private PSObjectDict getResourceDict(final PSObjectName category)
            throws PSError {
        
        if (resources.known(category)) {
            return resources.get(category).toDict();
        } else {
            // The dictionary does not exist. Lets check whether we need to
            // create it on the fly, or throw an error.
            PSObjectName catName = isStandardResourceCategory(category);
            if (catName == null) {
                // If it is not a standard category it might be a new type that
                // is already defined with an implementation dictionary. If it
                // isn't an undefined error is thrown automatically.
                getImplementationDict(category);
                catName = category;
            }
            
            PSObjectDict dict = new PSObjectDict();
            resources.setKey(catName, dict);
            return dict;
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
        
        LOG.fine("Defining resource: category = " + category.isis()
                + ", key = " + key.isis() + ", instance = " + instance.isis());
        
        // Get the procedure that implements this operator from the
        // implementation dictionary.
        PSObjectDict implDict = getImplementationDict(category);
        PSObjectArray proc = implDict.get(KEY_DEFINERESOURCE).toProc();
        
        if ((proc.size() > 0) && GENERIC_PROC_NAME.eq(proc.get(0))) {
            return defineResourceGeneric(category, key, instance);
        } else {
            throw new PSErrorUnimplemented("Using categories with custom"
                    + " implementation dictionaries.");
        }
    }
    
    /**
     * Define a new resource of the given category and with the given key.
     * 
     * This is a generic method that ignores the implementation dictionary.
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
    private PSObject defineResourceGeneric(final PSObjectName category,
            final PSObject key, final PSObject instance)
            throws PSError, ProgramError {
        
        // Do a type check of the new instance
        // Some types require special handling.
        if (category.eq(CAT_FONT)) {
            // Font resources are handled separately by a FontManager.
            // Therefore control in handed over to it.
            return fontManager.defineFont(key, instance.toFont());
        } else if (category.eq(CAT_COLORSPACE)) {
            // Check if it is a valid color space.
            ColorManager.autoSetColorSpace(instance);
        } else if (category.eq(CAT_CATEGORY)) {
            instance.toDict().setKey(KEY_CATEGORY, key);
        }
        
        // Define the new resource in the dictionary.
        PSObjectDict catDict = getResourceDict(category);
        catDict.setKey(key, instance);
        
        instance.readonly();
       
        return instance;
    }
    
    /**
     * Removes a named resource identified by the specified key from the
     * specified resource category.
     * 
     * @param category The category from which the resource is removed.
     * @param key The key of the resource to remove.
     * 
     * @throws PSError A PostScript error occurred.
     */
    public void undefineResource(final PSObjectName category,
            final PSObject key) throws PSError {
        
        LOG.fine("Removing resource: category = " + category.isis()
                + ", key = " + key.isis());
        
        // Get the procedure that implements this operator from the
        // implementation dictionary.
        PSObjectDict implDict = getImplementationDict(category);
        PSObjectArray proc = implDict.get(KEY_UNDEFINERESOURCE).toProc();
        
        if ((proc.size() > 0) && GENERIC_PROC_NAME.eq(proc.get(0))) {
            undefineResourceGeneric(category, key);
        } else {
            throw new PSErrorUnimplemented("Using categories with custom"
                    + " implementation dictionaries.");
        }        
    }

    /**
     * Removes a named resource identified by the specified key from the
     * specified resource category.
     * 
     * This is a generic method that ignores the implementation dictionary.
     * 
     * @param category The category from which the resource is removed.
     * @param key The key of the resource to remove.
     * 
     * @throws PSError A PostScript error occurred.
     */
    private void undefineResourceGeneric(final PSObjectName category,
            final PSObject key) throws PSError {
        
        getResourceDict(category).undef(key);
    }
        
    /**
     * Try to obtain a named resource in a specified category.
     * This function implements the 'findresource' operator.
     * 
     * @param category The resource category.
     * @param key The key of the named resource.
     * 
     * @return The requested resource.
     * 
     * @throws PSError A PostScript error occurred.
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    public PSObject findResource(final PSObjectName category,
            final PSObject key) throws PSError, ProgramError {

        LOG.fine("Finding resource: category = " + category.isis()
                + ", key = " + key.isis());
        
        // Get the procedure that implements this operator from the
        // implementation dictionary.
        PSObjectDict implDict = getImplementationDict(category);
        PSObjectArray proc = implDict.get(KEY_FINDRESOURCE).toProc();
        
        if ((proc.size() > 0) && GENERIC_PROC_NAME.eq(proc.get(0))) {
            return findResourceGeneric(category, key);
        } else {
            throw new PSErrorUnimplemented("Using categories with custom"
                    + " implementation dictionaries.");
        }
    }
    
    /**
     * Try to obtain a named resource in a specified category.
     * This function implements the 'findresource' operator.
     * 
     * This is a generic method that ignores the implementation dictionary.
     * 
     * @param category The resource category.
     * @param key The key of the named resource.
     * 
     * @return The requested resource.
     * 
     * @throws PSError A PostScript error occurred.
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    private PSObject findResourceGeneric(final PSObjectName category,
            final PSObject key) throws PSError, ProgramError {
        
        PSObject obj = null;
        
        // Fonts are managed separately by the font manager.
        if (category.eq(CAT_FONT)) {
            obj = fontManager.findFont(key);
        } else {
            obj = getResourceDict(category).lookup(key);
            if (obj == null) {
                throw new PSErrorUndefinedResource();
            }
        }
        
        return obj;
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
        
        LOG.fine("Quering status of resource: category = " + category.isis()
                + ", key = " + key.isis());
        
        // Get the procedure that implements this operator from the
        // implementation dictionary.
        PSObjectDict implDict = getImplementationDict(category);
        PSObjectArray proc = implDict.get(KEY_RESOURCESTATUS).toProc();
        
        if ((proc.size() > 0) && GENERIC_PROC_NAME.eq(proc.get(0))) {
            return resourceStatusGeneric(category, key);
        } else {
            throw new PSErrorUnimplemented("Using categories with custom"
                    + " implementation dictionaries.");
        }
    }
    
    /**
     * Check the status of a resource. Check the PostScript manual for the
     * operator 'resourcestatus'.
     * 
     * This is a generic method that ignores the implementation dictionary.
     * 
     * @param category The category.
     * @param key The key.
     * 
     * @return the pS object array
     * 
     * @throws PSError A PostScript error occurred.
     */
    private PSObjectArray resourceStatusGeneric(final PSObjectName category,
            final PSObject key) throws PSError {
        
        boolean supported;
        
        if (category.eq(CAT_FONTTYPE)) {
            supported = fontManager.fontTypeStatus(key);
        } else if (category.eq(CAT_FILTER)) {
            supported = FilterManager.filterStatus(key);
        } else if (category.eq(CAT_COLORSPACEFAMILY)) {
            supported = ColorManager.colorSpaceFamilyStatus(key);
        } else if (isImplicitResourceCategory(category) != null) {
            throw new PSErrorUnimplemented("Checking resource status of"
                    + " resource of type " + category.isis());
        } else {
            PSObjectDict resourceDict = getResourceDict(category);
            supported = resourceDict.known(key);
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
