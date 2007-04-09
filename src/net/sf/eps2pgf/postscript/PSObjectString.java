/*
 * PSObjectString.java
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

import java.util.regex.*;

import net.sf.eps2pgf.postscript.errors.*;

/**
 * String PostScript object.
 * @author Wagenaars
 */
public class PSObjectString extends PSObject {
    String value;
    
    /**
     * Creates a new empty PostScript string
     */
    public PSObjectString() {
        value = "";
    }
    
    /**
     * Create a new PostScript string with n \u0000 characters
     * @param n Number of \u0000 characters in the new string
     */
    public PSObjectString(int n) throws PSErrorRangeCheck {
        if (n < 0) {
            throw new PSErrorRangeCheck();
        }
        StringBuilder str = new StringBuilder(n);
        for (int i = 0 ; i < n ; i++) {
            str.append("\u0000");
        }
        
        value = str.toString();
    }
    
    /**
     * Creates a new instance of PSObjectString
     * @param str This object is initialized with a copy of this string. If
     *            the string is enclosed in round parenthesis ( ) it is
     *            parsed as PostScript string.
     */
    public PSObjectString(String str) {
        int len = str.length();
        if ( (len > 0) && (str.charAt(0) == '(') && (str.charAt(len-1) == ')') ) {
            value = parse(str.substring(1, len-1));
        } else {
            value = new String(str);
        }
    }
    
    /** Parse a postscript string. */
    public String parse(String str) {
        StringBuilder newStr = new StringBuilder();
        char chr;
        for (int i = 0 ; i < str.length() ; i++) {
            chr = str.charAt(i);
            if (chr != '\\') {
                newStr.append(chr);
                continue;
            }
            
            // Is it an octal character
            Pattern p = Pattern.compile("\\\\[0-7]{1,3}");
            Matcher m = p.matcher(str);
            if (m.find(i)) {
                if (m.start() == i) {
                    chr = (char)Integer.parseInt(m.group().substring(1), 8);
                    newStr.append(chr);
                    i = i + m.end() - m.start() - 1;
                    continue;
                }
            }
            
            // Is it a backslash followed by a newline
            p = Pattern.compile("\\\\[\r\n]{1,2}");
            m = p.matcher(str);
            if (m.find(i)) {
                if (m.start() == i) {
                    i = i + m.end() - m.start() - 1;
                    continue;
                }
            }
            
            // Is it a special character \n \r \t \b \f
            i++;
            if (str.charAt(i) == 'n') {
                newStr.append('\n');
            } else if (str.charAt(i) == 'r') {
                newStr.append('\r');
            } else if (str.charAt(i) == 't') {
                newStr.append('\t');
            } else if (str.charAt(i) == 'b') {
                newStr.append('\b');
            } else if (str.charAt(i) == 'f') {
                newStr.append('\f');
            } else {
                // Simply add the next character
                newStr.append(str.charAt(i));
            }
        }
        //System.out.println(str + " -> " + newStr);
        return newStr.toString();
    }
    
    /**
     * Check whether a string is a PostScript string
     * @param str String to check.
     * @return Returns true when str is a valid PostScript string. Returns false otherwise.
     */
    public static boolean isType(String str) {
        int len = str.length();
        if ( (str.charAt(0) == '(') && (str.charAt(len-1) == ')') ) {
            return true;
        } else {
            return false;
        }
    }
    
    /**
     * Implements PostScript operate: length
     * @return Length of this object
     */
    public int length() {
        return value.length();
    }

    /**
     * Convert this object to a string object, if possible
     * @return PostScript string object representation of this object
     */
    public PSObjectString toPSString() {
        return this;
    }

    /** Return PostScript text representation of this object. See the
     * PostScript manual under the == operator
     */
    public String isis() {
        return "(" + value + ")";
    }
    
    /**
     * Convert this string to an array with character names
     * @param encoding Encoding to use to decode this string
     * @return Array with character names
     */
    public PSObjectArray decode(PSObjectArray encoding) throws PSErrorRangeCheck {
        PSObjectArray arr = new PSObjectArray();
        for (int i = 0 ; i < value.length() ; i++) {
            int chr = value.charAt(i);
            arr.addAt(i, encoding.get(chr));
        }
        return arr;
    }

    /**
     * Converts this object to a human readable string.
     * @return Human readable string.
     */
    public String toString() {
        return "String: " + value;
    }

    /** Convert this object to dictionary key, if possible. */
    public String toDictKey() {
        return value;
    }
    
    /**
     * Creates an exact deep copy of this object.
     */
    public PSObjectString clone() {
        return new PSObjectString(value);
    }
}
