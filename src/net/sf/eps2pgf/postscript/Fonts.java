/*
 * Fonts.java
 *
 * This file is part of Eps2pgf.
 *
 * Copyright (C) 2007 Paul Wagenaars <pwagenaars@fastmail.fm>
 *
 * Eps2pgf is free software; you can redistribute it and/or modify it under
 * the terms of version 2 of the GNU General Public License as published by the
 * Free Software Foundation.
 *
 * Eps2pgf is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */

package net.sf.eps2pgf.postscript;

import java.io.*;
import java.util.*;
import java.util.logging.*;

import net.sf.eps2pgf.*;
import net.sf.eps2pgf.postscript.errors.*;

/**
 * Manages font resources
 * @author Paul Wagenaars
 */
public class Fonts {
    private Map<String, PSObjectFont> fonts = new HashMap<String, PSObjectFont>();
    String defaultFont = "Times-Roman";
    
    File resourceDir;
    
    Logger log = Logger.getLogger("global");
    
    /**
     * Create a new Fonts instance
     */
    public Fonts() throws FileNotFoundException {
        // Try to find the resource dir
        File classPath = new File(System.getProperty("java.class.path"));
        if (!(classPath.isAbsolute())) {
            classPath = new File(System.getProperty("user.dir"), 
                    System.getProperty("java.class.path"));
        }
        while (classPath != null) {
            // Check whether this dir has a valid resource subdir
            // It just checks for a "resources" directory with two known subdirectories
            resourceDir = new File(classPath, "resources");
            if (resourceDir.exists()) {
            File afmDir = new File(resourceDir, "afm");
                if (afmDir.exists()) {
                    File fontDescDir = new File(resourceDir, "fontdescriptions");
                    if (fontDescDir.exists()) {
                        break;
                    }
                }
            }
            resourceDir = null;
            classPath = classPath.getParentFile();
        }
        if (resourceDir == null) {
            throw new FileNotFoundException("Unable to find resource dir.");
        }
    }
    
    /**
     * Search a font and return it's corresponding font dictionary.
     */
    public PSObjectFont findFont(PSObject fontNameObj) throws PSErrorTypeCheck, 
            PSErrorInvalidFont, ProgramError {
        String fontName = fontNameObj.toDictKey();
        return findFont(fontName);
    }
    
    /**
     * Search a font and return it's corresponding font dictionary.
     * @param fontName Name of the font
     * @return Font (dictionary) of the requested font
     * @throws ProgramError Unable to load the requested font and the default font
     */
    public PSObjectFont findFont(String fontName) throws ProgramError {
        // First we search whether this font has already been loaded
        if (fonts.containsKey(fontName)) {
            return fonts.get(fontName);
        }
        
        // Appartly the font hasn't already been loaded.
        try {
            return loadFont(fontName);
        } catch (PSErrorInvalidFont e) {
            if (fontName.equals(defaultFont)) {
                throw new ProgramError("Unable to load " + defaultFont + " font.");
            } else {
                log.info("Unable to find this font. Substituting font "
                        + defaultFont + " for " + fontName + ".");
                return findFont(defaultFont);
            }
        }
    }
    
    /**
     * Load a font from the resource directory.
     * @param fontName Name of the font
     * @return Font dictionary of the loaded font
     */
    public PSObjectFont loadFont(String fontName) throws PSErrorInvalidFont {
        log.info("Loading " + fontName + " font from " + resourceDir);
        PSObjectFont font = new PSObjectFont(resourceDir, fontName);
        
        // Now the font is loaded, add it to the fonts list so that it
        // doesn't need to loaded again.
        fonts.put(fontName, font);
        
        return font;
    }
    
    /**
     * Define a new font and associate it with a key.
     * @return Defined font
     * @param key Key to associate the font with
     * @param font Font to define
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorUnimplemented Part of this method is not implemented
     * @throws PSErrorTypeCheck Unable to use the key as dictionary key
     */
    public PSObjectFont defineFont(PSObject key, PSObjectFont font) 
            throws PSErrorTypeCheck, PSErrorUnimplemented {
        if (font.getFID() >= 0) {
            throw new PSErrorUnimplemented("Associating a font with more than one key.");
        } else {
            font.setFID();
            fonts.put(key.toDictKey(), font);
        }
        return font;
    }
    
    /**
     * Convert an array of character names to LaTeX code
     */
    public String charNames2Latex(PSObjectArray charNames, PSObjectDict font) 
            throws PSErrorRangeCheck, PSErrorTypeCheck, PSErrorUnimplemented {
        StringBuilder str = new StringBuilder(charNames.size());
        PSObjectDict charStrings = (PSObjectDict)font.lookup("CharStrings");
        PSObjectString preCode = (PSObjectString)font.lookup("LatexPreCode");
        PSObjectString postCode = (PSObjectString)font.lookup("LatexPostCode");
        
        str.append(preCode.value);
        for (int i = 0 ; i < charNames.size() ; i++) {
            PSObjectString code = (PSObjectString)charStrings.lookup(charNames.get(i));
            if (code == null) {
                throw new PSErrorUnimplemented("CharString for "
                        + charNames.get(i).isis() + " is unknown.");
            }
            str.append(code.value);
        }
        str.append(postCode.value);
        
        return str.toString();
    }

}
