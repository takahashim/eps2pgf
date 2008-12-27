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

package net.sf.eps2pgf.ps.resources.fonts;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

import net.sf.eps2pgf.ProgramError;
import net.sf.eps2pgf.ps.errors.PSError;
import net.sf.eps2pgf.ps.errors.PSErrorInvalidFont;
import net.sf.eps2pgf.ps.errors.PSErrorTypeCheck;
import net.sf.eps2pgf.ps.errors.PSErrorUndefined;
import net.sf.eps2pgf.ps.errors.PSErrorUnregistered;
import net.sf.eps2pgf.ps.objects.PSObject;
import net.sf.eps2pgf.ps.objects.PSObjectDict;
import net.sf.eps2pgf.ps.objects.PSObjectFont;
import net.sf.eps2pgf.ps.objects.PSObjectName;
import net.sf.eps2pgf.ps.objects.PSObjectString;
import net.sf.eps2pgf.ps.resources.Utils;

/**
 * Manages font resources and serves as FontDirectory.
 * 
 * @author Paul Wagenaars
 */
public final class FontManager extends PSObjectDict {
    
    /** All static fields need to be initialized only once. */
    private static boolean alreadyInitialized = false;

    /** Default font to use when actual font can not be found. */
    private static final PSObjectName DEFAULT_FONT =
                                          new PSObjectName("Times-Roman", true);
    
    /** Some fonts are substituted by another font. This list describes this. */
    private static Properties fontSubstitutions;
    
    /**
     * All TeX strings. A TeX string is a portion of LaTeX code describing a
     * certain character.
     */
    private static Map<String, Properties> allTexStrings;
    
    /** The logger. */
    private static final Logger LOG =
                                    Logger.getLogger("net.sourceforge.eps2pgf");
    
    /** Name of directory (within resource dir) with afm files. */
    public static final String AFM_DIR_NAME = "afm";
    
    /** Name of directory (within resource dir) with font descriptions. */
    public static final String FONTDESC_DIR_NAME = "fontdescriptions";
    
    /** Name of directory (within resource dir) with TeX strings. */
    private static final String TEXSTRINGS_DIR_NAME = "texstrings";
    
    /**
     * Key name of entry in text strings file with regexp that determines for
     * which font names this texstrings file is valid.
     */ 
    private static final String TEXSTRINGS_REGEXP = "eps2pgf_select_regexp";

    /**
     * Key name of entry in TeX strings file describing priority of list. If
     * more than one regexp matches a font name, the one with the lowest order
     * is used for the TeX strings.
     */
    private static final String TEXSTRINGS_ORDER = "eps2pgf_select_order";
    
    /**
     * Key in font dictionary which contains the FontDirectory key with which
     * this font is associated.
     */
    public static final PSObjectName FONT_DICT_KEY =
                                          new PSObjectName("/FontDirectoryKey");
    
    
    /**
     * Create a new FontDirectory and makes sure the FontManager is initialized.
     * 
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    public FontManager() throws ProgramError {
        super();
        
        // Make sure the FontManager is initialized
        initialize();
        
        // Make the FontManager/FontDirectory, which is also a dictionary,
        // read only.
        try {
            readonly();
        } catch (PSErrorTypeCheck e) {
            /* this can never happen */
        }
    }
    
    /**
     * Initializes all static fields in the FontManager.
     * 
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    public static void initialize() throws ProgramError {
        if (!alreadyInitialized) {
            
            fontSubstitutions = loadFontSubstitutions(
                    new File(Utils.getResourceDir(), "fontSubstitution.xml"));
            allTexStrings = loadAllTexstrings();
            
            alreadyInitialized = true;
        }
        
    }
    
    /**
     * Define a new font and associate it with a key.
     * 
     * @param key Key to associate the font with
     * @param font Font to define
     * 
     * @return Defined font
     * 
     * @throws PSError A PostScript error occurred.
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    public PSObjectFont defineFont(final PSObject key, final PSObjectFont font)
            throws PSError, ProgramError {
        
        font.setFID();
        font.toDict().setKey(FONT_DICT_KEY, key);
        try {
            font.assertValidFont();
        } catch (PSErrorUnregistered e) {
            // At this point this error is not fatal. So for now we just ignore
            // it. When the font is actually used it will be fatal.
        }
        setKey(key, font);
        
        return font;
    }
    
    /**
     * Search a font and return it's corresponding font dictionary.
     * 
     * @param pFontName Name of the font
     * 
     * @return Font (dictionary) of the requested font
     * 
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     * @throws PSError A PostScript error occurred.
     */
    public PSObjectFont findFont(final PSObject pFontName)
            throws ProgramError, PSError {
        PSObject fontName = pFontName;
        PSObject subFontName = findSubstitutionFont(fontName);
        if (subFontName != null) {
            LOG.info("Substituting font " + subFontName + " for " + fontName);
            fontName = subFontName;
        }
        
        // First we search whether this font has already been loaded
        try {
            PSObject font = this.get(fontName);
            if (!(font instanceof PSObjectFont)) {
                throw new ProgramError("Non PSObjectFont object found in"
                        + " FontDirectory");
            }
            return (PSObjectFont) font;
        } catch (PSErrorUndefined e) {
            // The font was not found in the FontDirectory, so we will have to
            // load it from disk.
        }
        
        // Apparently the font hasn't already been loaded.
        try {
            return loadFont(fontName);
        } catch (PSErrorInvalidFont e) {
            if (fontName.equals(DEFAULT_FONT)) {
                throw new ProgramError("Unable to load " + DEFAULT_FONT
                        + " font.");
            } else {
                LOG.info("Unable to find this font. Substituting font "
                        + DEFAULT_FONT + " for " + fontName + ".");
                return findFont(DEFAULT_FONT);
            }
        }
    }
    
    /**
     * Search the font substitution list for a font.
     * 
     * @return If the requested font is found in the substitution list the
     *         substitution font is returned. If the requested font is not
     *         found, the requested font itself is returned.
     *         
     * @param fontNameObj Name of the font for which a substitute is searched
     */
    private static PSObject findSubstitutionFont(final PSObject fontNameObj) {
        String fontName;
        if ((fontNameObj instanceof PSObjectName)
                || (fontNameObj instanceof PSObjectString)) {
            fontName = fontNameObj.toString();
        } else {
            fontName = fontNameObj.isis();
        }
        String subFont = fontSubstitutions.getProperty(fontName);
        if (subFont == null) {
            return null;
        } else {
            return new PSObjectName(subFont, true);
        }
    }
    
    /**
     * Load a font from the resource directory.
     * 
     * @param fontKey The font key.
     * 
     * @return Font dictionary of the loaded font
     * 
     * @throws PSError A PostScript error occurred.
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    private PSObjectFont loadFont(final PSObject fontKey)
            throws PSError, ProgramError {
        String fontName = fontKey.toString();
        LOG.info("Loading " + fontName + " font from "
                + Utils.getResourceDir());
        PSObjectFont font = new PSObjectFont(Utils.getResourceDir(), fontName);
        
        // Now the font is loaded, add it to the fonts list so that it
        // doesn't need to loaded again.
        defineFont(fontKey, font);
        
        return font;
    }
    
    /**
     * Load list with font substitutions.
     * 
     * @param fontSubFile File from which the substitution list will be loaded.
     * 
     * @return List with font substitution. Returns null if the list could not
     * be loaded.
     */
    private static Properties loadFontSubstitutions(final File fontSubFile) {
        Properties fontSubList;
        try {
            FileInputStream in = new FileInputStream(fontSubFile);
            fontSubList = new Properties();
            fontSubList.loadFromXML(in);
            in.close();
        } catch (FileNotFoundException e) {
            return null;
        } catch (IOException e) {
            return null;
        }
        return fontSubList;
    }
    
    /**
     * Load the list with texstring (string to produce a certain character in
     * TeX).
     * 
     * @throws ProgramError Unable to read the texstrings from file.
     * 
     * @return Dictionary with all loaded "character names -> texstring" pairs.
     */
    private static Map<String, Properties> loadAllTexstrings()
            throws ProgramError {
        
        File texStringsDir = new File(Utils.getResourceDir(),
                TEXSTRINGS_DIR_NAME);
        File[] texStringFiles = texStringsDir.listFiles();
        Map<String, Properties> texStrings = new HashMap<String, Properties>();
        
        for (int i = 0; i < texStringFiles.length; i++) {
            Properties props;
            try {
                FileInputStream in = new FileInputStream(texStringFiles[i]);
                props = new Properties();
                props.loadFromXML(in);
                in.close();
            } catch (IOException e) {
                continue;
            }
            String name = texStringFiles[i].getName();
            if (name.endsWith(".xml")) {
                name = name.substring(0, name.length() - 4);
            }
            texStrings.put(name, props);
        }
        
        return texStrings;
    }
    
    /**
     * Retrieves a set of TeX strings specified by the filename.
     * 
     * @param filename The filename.
     * 
     * @return The TeX strings dictionary.
     */
    public static PSObjectDict getTexStringDict(final String filename) {
        Properties props = allTexStrings.get(filename);
        PSObjectDict texStringDict = new PSObjectDict();
        
        for (Enumeration<Object> e = props.keys(); e.hasMoreElements();) {
            String key = e.nextElement().toString();
            if (key.startsWith("eps2pgf")) {
                continue;
            }
            texStringDict.setKey(key, props.getProperty(key));
        }
        
        return texStringDict;
    }

    /**
     * Retrieve a set of TeX strings specified by the font name and
     * the regexps in the texstrings files. This selects the set of
     * TeX strings with a regexp that matches the font name. If there
     * are multiple matches the set with lowest order is selected.
     * 
     * @param fontname The font name.
     * 
     * @return the TeX strings dictionary for the requested font name.
     */
    public static PSObjectDict getTexStringDictByFontname(
            final String fontname) {
        
        String matchName = "default";
        int matchOrder = Integer.MAX_VALUE; 
        for (String texStringName : allTexStrings.keySet()) {
            Properties props = allTexStrings.get(texStringName);
            String orderStr = props.getProperty(TEXSTRINGS_ORDER, "999999");
            int order = Integer.parseInt(orderStr);
            if (order > matchOrder) {
                continue;
            }
            String regexp = props.getProperty(TEXSTRINGS_REGEXP, "^$");
            if (fontname.matches(regexp)) {
                matchName = texStringName;
                matchOrder = order;
            }
        }
        return getTexStringDict(matchName);
    }
    
    /**
     * Checks whether a specific font type is supported or not.
     * 
     * @param key Key describing font type. It should be a number.
     * 
     * @return true if font type is supported, false if type is not
     * supported.
     * 
     * @throws PSError A PostScript error occurred.
     */
    public boolean fontTypeStatus(final PSObject key) throws PSError {

        String name = String.format("net.sf.eps2pgf.ps.resources.fonts.Type%d",
                key.toInt());
        
        try {
            Class.forName(name);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
    
}
