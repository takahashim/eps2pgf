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

import net.sf.eps2pgf.ProgramError;
import net.sf.eps2pgf.ps.errors.PSError;
import net.sf.eps2pgf.ps.errors.PSErrorUnimplemented;
import net.sf.eps2pgf.ps.objects.PSObject;
import net.sf.eps2pgf.ps.objects.PSObjectArray;
import net.sf.eps2pgf.ps.objects.PSObjectName;
import net.sf.eps2pgf.ps.resources.fonts.FontManager;

/**
 * Manages all resources (or delegates other managers for specific resource
 * types).
 * 
 * @author Paul Wagenaars
 */
public final class ResourceManager {
    
    /** The Constant CAT_FONTTYPE. */
    private static final PSObjectName CAT_FONTTYPE
                                                = new PSObjectName("FontType");
    
    /** Font manager. */
    private FontManager fontManager;
    
    /**
     * Create a new resource manager.
     * 
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    public ResourceManager() throws ProgramError {
        fontManager = new FontManager();
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
        
        PSObjectArray ret;
        
        if (category.eq(CAT_FONTTYPE)) {
            ret = fontManager.fontTypeStatus(key);
        } else {
            throw new PSErrorUnimplemented("Checking resources of type: "
                    + category.isis());
        }
        
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
