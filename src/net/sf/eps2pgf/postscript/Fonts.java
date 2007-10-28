/*
 * Fonts.java
 *
 * This file is part of Eps2pgf.
 *
 * Copyright 2007 Paul Wagenaars <paul@wagenaars.org>
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
import net.sf.eps2pgf.postscript.errors.PSErrorInvalidFont;
import net.sf.eps2pgf.postscript.errors.PSErrorTypeCheck;
import net.sf.eps2pgf.postscript.errors.PSErrorUndefined;
import net.sf.eps2pgf.postscript.errors.PSErrorUnimplemented;

/**
 * Manages font resources
 * @author Paul Wagenaars
 */
public class Fonts {
    static PSObjectDict FontDirectory = new PSObjectDict();
    
    static String defaultFont = "Times-Roman";
    
    static File resourceDir;
    
    static Properties fontSubstitutions;
    
    static Map<String,Properties> allTexStrings;
    
    static Logger log = Logger.getLogger("global");
    
    static final String RESOURCE_DIR_NAME = "resources";
    static final String AFM_DIR_NAME = "afm";
    static final String FONTDESC_DIR_NAME = "fontdescriptions";
    static final String TEXSTRINGS_DIR_NAME = "texstrings";
    
    static final String TEXSTRINGS_ORDER = "eps2pgf_select_order";
    static final String TEXSTRINGS_REGEXP = "eps2pgf_select_regexp";
    
    /**
     * Initializes Fonts 
     * @throws java.io.FileNotFoundException Unable to find resource dir
     */
    public static void initialize() throws ProgramError {
        // Make FontDirectory read-only (for the user)
        FontDirectory.readonly();
        
        // Try to find the resource dir
        File classPath = new File(System.getProperty("java.class.path"));
        File userDir = new File(System.getProperty("user.dir"));
        if (!(classPath.isAbsolute())) {
            classPath = new File(userDir, classPath.getPath()); 
        }
        resourceDir = findResourceDirInPath(classPath);
        if (resourceDir == null) {
        	resourceDir = findResourceDirInPath(userDir);
        }
        if (resourceDir == null) {
            throw new ProgramError("Unable to find resource dir.");
        }
        
        fontSubstitutions = loadFontSubstitutions(new File(resourceDir, "fontSubstitution.xml"));
        allTexStrings = loadAllTexstrings();
    }
    
    /**
     * Define a new font and associate it with a key.
     * @return Defined font
     * @param key Key to associate the font with
     * @param font Font to define
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorUnimplemented Part of this method is not implemented
     * @throws PSErrorTypeCheck Unable to use the key as dictionary key
     */
    public static PSObjectFont defineFont(PSObject key, PSObjectFont font) 
            throws PSErrorTypeCheck, PSErrorUnimplemented {
        if (font.getFID() >= 0) {
            throw new PSErrorUnimplemented("Associating a font with more than one key.");
        } else {
            font.setFID();
            FontDirectory.setKey(key, font);
        }
        return font;
    }
    
    /**
     * Search a font and return it's corresponding font dictionary.
     * @param fontNameObj PostScript object defining the font. The toDictKey method is used to get
     * the font name from the object.
     * @return Requested font
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorTypeCheck Supplied opject is not the correct type
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorInvalidFont Unable to find the requested font.
     * @throws net.sf.eps2pgf.ProgramError This should never happen. This error indicates a bug in Eps2pgf.
     */
    public static PSObjectFont findFont(PSObject fontNameObj) throws PSErrorTypeCheck, 
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
    public static PSObjectFont findFont(String fontName) throws ProgramError {
        String subFontName = findSubstitutionFont(fontName);
        if (subFontName != null) {
            log.info("Substituting font " + subFontName + " for " + fontName);
            fontName = subFontName;
        }
        
        // First we search whether this font has already been loaded
        try {
            PSObject font = FontDirectory.get(fontName);
            if (!(font instanceof PSObjectFont)) {
                throw new ProgramError("Non PSObjectFont object found in FontDirectory");
            }
            return (PSObjectFont)font;
        } catch (PSErrorUndefined e) {
            // The font was not found in the FontDirectory, so we will have to
            // load it from disk.
        }
        
        // Apparently the font hasn't already been loaded.
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
     * Try to find the resource in the current directory or its parent
     * directories.
     * @param dir 
     */
    public static File findResourceDirInPath(File dir) {
    	File returnDir = null;
    	
        while (dir != null) {
            // Check whether this dir has a valid resource subdir
            // It just checks for a "resources" directory with two known subdirectories
            returnDir = new File(dir, RESOURCE_DIR_NAME);
            if (returnDir.exists()) {
                File afmDir = new File(returnDir, AFM_DIR_NAME);
                if (afmDir.exists()) {
                    File fontDescDir = new File(returnDir, FONTDESC_DIR_NAME);
                    if (fontDescDir.exists()) {
                        break;
                    }
                }
            }
            returnDir = null;
            dir = dir.getParentFile();
        }
        
        return returnDir;
    }
    
    /**
     * Search the font substitution list for a font.
     * @return If the requested font is found in the substitution list the
     *         substitution font is returned. If the requested font is not
     *         found, the requested font itself is returned.
     * @param fontName Name of the font for which a substitute is searched
     */
    public static String findSubstitutionFont(String fontName) {
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
    public static PSObjectFont loadFont(String fontName) throws PSErrorInvalidFont {
        log.info("Loading " + fontName + " font from " + resourceDir);
        PSObjectFont font = new PSObjectFont(resourceDir, fontName);
        
        // Now the font is loaded, add it to the fonts list so that it
        // doesn't need to loaded again.
        FontDirectory.setKey(fontName, font);
        
        return font;
    }
    
    /**
     * Load list with font substitutions
     * @param fontSubFile File from which the substitution list will be loaded
     * @return List with font substitution. Returns null if the list could not
     *         be loaded.
     */
    public static Properties loadFontSubstitutions(File fontSubFile) {
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
     * Load the list with texstring (string to produce a certain character in LaTeX)
     * @param texstringsFile File from which texstrings have to be loaded.
     * @throws net.sf.eps2pgf.ProgramError Unable to read the texstrings from file.
     * @return Dictionary with all loaded "character names -> texstring" pairs.
     */
    public static Map<String, Properties> loadAllTexstrings() throws ProgramError {
    	File texStringsDir = new File(resourceDir, TEXSTRINGS_DIR_NAME);
    	File[] texStringFiles = texStringsDir.listFiles();
    	Map<String, Properties> texStrings = new HashMap<String, Properties>();
    	
    	for (int i = 0 ; i < texStringFiles.length ; i++) {
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
            	name = name.substring(0, name.length()-4);
            }
            texStrings.put(name, props);
    	}
        
        return texStrings;
    }
    
    /**
     * Retrieves a set of TeX strings specified by the filename.
     * @param name Filename of the set of TeX strings, e.g. default
     */
    public static PSObjectDict getTexStringDict(String filename) {
    	Properties props = allTexStrings.get(filename);
    	PSObjectDict texStringDict = new PSObjectDict();
    	
    	for(Enumeration<Object> e = props.keys() ; e.hasMoreElements() ;) {
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
     * TeX strings with a regexp that matches the fontname. If there
     * are multiple matches the set with lowest order is selected.
     */
    public static PSObjectDict getTexStringDictByFontname(String fontname) {
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
}
