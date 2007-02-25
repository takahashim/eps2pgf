/*
 * PSObjectFont.java
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

import net.sf.eps2pgf.ProgramError;
import net.sf.eps2pgf.postscript.errors.*;

/**
 *
 * @author Paul Wagenaars
 */
public class PSObjectFont extends PSObject implements Cloneable {
    private static int nextFID = 0;
    PSObjectDict dict;
    Logger log = Logger.getLogger("global");
    
    /**
     * Creates a new instance of PSObjectFont by loading it from disk.
     * @param resourceDir Resource directory with font information
     * @param fontName Name of the font to load
     */
    public PSObjectFont(File resourceDir, String fontName) throws PSErrorInvalidFont {
        File fontFile = new File(resourceDir, "fontdescriptions" + 
                File.separator + fontName + ".font");

        Properties props;
        try {
            FileInputStream in = new FileInputStream(fontFile);
            props = new Properties();
            props.loadFromXML(in);
            in.close();
        } catch (FileNotFoundException e) {
            throw new PSErrorInvalidFont();
        } catch (IOException e) {
            throw new PSErrorInvalidFont();
        }
        
        // Setting the dictionary keys with font info
        dict = new PSObjectDict();
        dict.setKey("FontType", new PSObjectInt(1));
        dict.setKey("FontMatrix", new PSObjectMatrix(1,0,0,1,0,0));
        dict.setKey("FontName", new PSObjectName(fontName, true));
        setFID();
        String encoding = props.getProperty("encoding", "Standard");
        if (encoding.equals("Standard")) {
            dict.setKey("Encoding", new PSObjectArray(Encoding.getStandardVector()));
        } else if (encoding.equals("ISOLatin1")) {
            dict.setKey("Encoding", new PSObjectArray(Encoding.getISOLatin1Vector()));
        } else if (encoding.equals("Symbol")) {
            dict.setKey("Encoding", new PSObjectArray(Encoding.getSymbolVector()));
        } else {
            log.severe("Unknown encoding: " + encoding);
            throw new PSErrorInvalidFont();
        }
        dict.setKey("PaintType", new PSObjectInt(2));
        dict.setKey("LatexPreCode", props.getProperty("latexprecode", ""));
        dict.setKey("LatexPostCode", props.getProperty("latexpostcode", ""));
        String charStrings = props.getProperty("charstrings", "StandardLatin");
        dict.setKey("CharStrings", loadCharStrings(resourceDir, charStrings));
        
//      dict.setKey("FontBBox", new PSObjectArray(fontBbox));
    }
    
    /**
     * Creates a new font dictionary with aDict as dictionary.
     * @param aDict Dictionary to use as font dictionary
     */
    public PSObjectFont(PSObjectDict aDict) throws PSErrorTypeCheck {
        // Check some (not all) required font dictionary fields
        if ((!aDict.containsKey("FontType")) || (!aDict.containsKey("FontMatrix"))) {
            throw new PSErrorTypeCheck();
        }
        
        dict = aDict;
    }
    
    /**
     * Load a file with charString (= character names -> latex code)
     * @param resourceDir Resource directory
     * @param charStringsName Name of the charStrings list to load
     * @return Dictionary with character names and associated latex code
     */
    PSObjectDict loadCharStrings(File resourceDir, String charStringsName) throws PSErrorInvalidFont {
        File charStrFile = new File(resourceDir, "charstrings" + 
                File.separator + charStringsName + ".charstrings");
        
        Properties props;
        try {
            FileInputStream in = new FileInputStream(charStrFile);
            props = new Properties();
            props.loadFromXML(in);
            in.close();
        } catch (FileNotFoundException e) {
            log.severe("Unable to load charStrings " + charStringsName + ".\n");
            throw new PSErrorInvalidFont();
        } catch (IOException e) {
            throw new PSErrorInvalidFont();
        }
        
        // Now load charStrings from the props
        PSObjectDict dict = new PSObjectDict();
        Set<String> allCharNames = Encoding.getCharNames().keySet();
        for (String charName : allCharNames) {
            // Check whether this character name in defined in this
            // charStrings list.
            String propValue = props.getProperty(charName);
            if (propValue != null) {
                dict.setKey(charName, propValue);
            }
        }

        return dict;
    }
    
    /**
     * Returns the fontname of this font
     * @return Fontname
     */
    String getFontName() {
        try {
            return dict.lookup("FontName").toName().name;
        } catch (PSErrorTypeCheck e) {
            // No font name is defined
            return "";
        }
    }
    
    /**
     * Returns the font ID, if defined. Returns -1 when no FID is defined.
     */
    int getFID() {
        try {
            return dict.lookup("FID").toInt();
        } catch (PSErrorTypeCheck e) {
            // FID is not an integer
            return -1;
        } catch (NullPointerException e) {
            // No FID is defined
            return -1;
        }
    }
    
    /**
     * Set the font ID for this font to the next available ID
     * @return New font ID
     */
    int setFID() {
        int FID = nextFID++;
        dict.setKey("FID", new PSObjectInt(FID));
        return FID;
    }
    
    /**
     * Return the dictionary used by this font
     */
    public PSObjectDict toDict() {
        return dict;
    }
    
    /**
     * Return this object.
     * @return This object
     */
    public PSObjectFont toFont() {
        return this;
    }

    /**
     * Creates an exact deep copy of this font.
     * @return Created copy
     */
    public PSObjectFont clone() throws CloneNotSupportedException {
        PSObjectDict newDict = dict.clone();
        PSObjectFont newFont = null;
        try {
            newFont = new PSObjectFont(newDict);
        } catch (PSErrorTypeCheck e) {
            // Assuming that "this object" is a valid font this should never happen.
        }
        return newFont;
    }
    
    /**
     * Creates a human-readable string representation of this font.
     */
    public String isis() {
        return "-font " + getFontName() + " (FID " + getFID() + ")-";
    }
}
