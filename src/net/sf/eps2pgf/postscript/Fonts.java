/*
 * Fonts.java
 *
 * This file is part of Eps2pgf.
 *
 * Copyright 2007 Paul Wagenaars <pwagenaars@fastmail.fm>
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
    
    Properties fontSubstitutions;
    
    Logger log = Logger.getLogger("global");
    
    /**
     * Create a new Fonts instance
     * @throws java.io.FileNotFoundException Unable to find resource dir
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
        
        fontSubstitutions = loadFontSubstitutions(new File(resourceDir, "fontSubstitution.xml"));
    }
    
    /**
     * Search a font and return it's corresponding font dictionary.
     * @param fontNameObj PostScript object defining the font. The toDictKey method is used to get
     * the font name from the object.
     * @return Requested font
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorTypeCheck Supplied opject is not the correct type
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorInvalidFont Unable to find the requested font.
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorInvalidAccess No access to a required resource.
     * @throws net.sf.eps2pgf.ProgramError This should never happen. This error indicates a bug in Eps2pgf.
     */
    public PSObjectFont findFont(PSObject fontNameObj) throws PSErrorTypeCheck, 
            PSErrorInvalidFont, PSErrorInvalidAccess, ProgramError {
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
        String subFontName = findSubstitutionFont(fontName);
        if (subFontName != null) {
            log.info("Substituting font " + subFontName + " for " + fontName);
            fontName = subFontName;
        }
        
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
     * Search the font substitution list for a font.
     * @return If the requested font is found in the substitution list the
     *         substitution font is returned. If the requested font is not
     *         found, the requested font itself is returned.
     * @param fontName Name of the font for which a substitute is searched
     */
    public String findSubstitutionFont(String fontName) {
        String subFont = fontSubstitutions.getProperty(fontName);
        if (subFont == null) {
            return null;
        } else {
            return subFont;
        }
    }
    
    /**
     * Load a font from the resource directory.
     * @return Font dictionary of the loaded font
     * @param fontName Name of the font
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorInvalidFont Unable to find requested font.
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
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorInvalidAccess No access to a required resource.
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorUnimplemented Part of this method is not implemented
     * @throws PSErrorTypeCheck Unable to use the key as dictionary key
     */
    public PSObjectFont defineFont(PSObject key, PSObjectFont font) 
            throws PSErrorTypeCheck, PSErrorUnimplemented, PSErrorInvalidAccess {
        if (font.getFID() >= 0) {
            throw new PSErrorUnimplemented("Associating a font with more than one key.");
        } else {
            font.setFID();
            fonts.put(key.toDictKey(), font);
        }
        return font;
    }
    
    /**
     * Load list with font substitutions
     * @param fontSubFile File from which the substitution list will be loaded
     * @return List with font substitution. Returns null if the list could not
     *         be loaded.
     */
    public Properties loadFontSubstitutions(File fontSubFile) {
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

}
