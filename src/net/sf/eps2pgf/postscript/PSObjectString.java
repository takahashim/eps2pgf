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

import java.util.LinkedList;
import java.util.List;
import java.util.regex.*;

import net.sf.eps2pgf.postscript.errors.*;

/**
 * String PostScript object.
 * @author Wagenaars
 */
public class PSObjectString extends PSObject {
    private StringBuffer value;
    private int offset;
    private int count;
    
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
        
        value = new StringBuffer(str.toString());
        offset = 0;
        count = value.length();
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
            value = new StringBuffer(parse(str.substring(1, len-1)));
        } else {
            value = new StringBuffer(str);
        }
        offset = 0;
        count = value.length();
    }
    
    /**
     * Create new instance of PSObjectString. The new instance is a subset
     * of an existing stringbuffer.
     * @param strBuf The new instance is a subset of this stringbuffer.
     *               Normally, this is the stringbuffer of another PSObjectString.
     * @param index Index in strBuf of first character
     * @param length Number of characters in new string
     */
    public PSObjectString(StringBuffer strBuf, int index, int length) throws PSErrorRangeCheck {
        if ((index < 0) || (length < 0)) {
            throw new PSErrorRangeCheck();
        }
        if ((index+length) > strBuf.length()) {
            throw new PSErrorRangeCheck();
        }
        value = strBuf;
        offset = index;
        count = length;
    }
    
    /**
     * Produce a text representation of this object (see PostScript
     * operator 'cvs' for more info)
     * @return Text representation
     */
    public String cvs() {
        return toString();
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
        return count;
    }

    /** Return PostScript text representation of this object. See the
     * PostScript manual under the == operator
     */
    public String isis() {
        return "(" + toString() + ")";
    }
    
    /**
     * Gets the (integer) value of a character in this string.
     * @param index Index of character (first character has index 0)
     * @return Integer value of requested character
     */
    public int get(int index) throws PSErrorRangeCheck {
        if ( (index < 0) || (index >= count) ) {
            throw new PSErrorRangeCheck();
        }
        
        return value.charAt(index + offset);
    }
    
    /**
     * Gets the (integer) value of a character in this string.
     * @param index Index of character (first character has index 0)
     * @return Integer value of requested character
     */
    public PSObject get(PSObject index) throws PSErrorTypeCheck, PSErrorRangeCheck {
        int chr = get(index.toInt());
        return new PSObjectInt(chr);
    }
    
    /**
     * Implements PostScript operator getinterval. Returns a new object
     * with an interval from this object.
     * @param index Index of the first character of substring
     * @param count Number of characters in the substring
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorRangeCheck Invalid index or number of elements
     * @return Object representing a subarray of this object. The data is shared
     * between both objects.
     */
    public PSObject getinterval(int index, int count) throws PSErrorRangeCheck {
        return new PSObjectString(value, index, count);
    }
    
    /**
     * Returns a list with all items in object.
     * @return List with all items in this object. The first object (with
     *         index 0) is always a PSObjectInt with the number of object
     *         in a single item. For most object types this is 1, but for
     *         dictionaries this is 2. All consecutive items (index 1 and
     *         up) are the object's items.
     */
    public List<PSObject> getItemList() throws PSErrorTypeCheck {
        List<PSObject> items = new LinkedList<PSObject>();
        items.add(new PSObjectInt(1));
        try {
            for (int i = 0 ; i < count ; i++) {
                items.add(new PSObjectInt(get(i)));
            }
        } catch (PSErrorRangeCheck e) {
            // This can never happen due to the for-loop.
        }
        return items;
    }
    
    /**
     * PostScript operator copy. Copies values from obj1 to this object.
     * @param obj1 Copy values from obj1
     * @return Returns subsequence of this object
     */
    public PSObject copy(PSObject obj1) throws PSErrorRangeCheck, PSErrorTypeCheck {
        String obj1Str = obj1.toPSString().toString();
        putinterval(0, obj1Str);
        return getinterval(0, obj1Str.length());
    }

    /**
     * Convert this string to an array with character names
     * @param encoding Encoding to use to decode this string
     * @return Array with character names
     */
    public PSObjectArray decode(PSObjectArray encoding) throws PSErrorRangeCheck {
        PSObjectArray arr = new PSObjectArray();
        for (int i = 0 ; i < count ; i++) {
            int chr = get(i);
            arr.addAt(i, encoding.get(chr));
        }
        return arr;
    }
    
    /**
     * Compare this object with another object and return true if they are equal.
     * See PostScript manual on what's equal and what's not.
     * @param obj Object to compare this object with
     * @return True if objects are equal, false otherwise
     */
    public boolean eq(PSObject obj) {
        if (obj instanceof PSObjectName) {
            PSObjectName objName = (PSObjectName)obj;
            return (toString().equals(objName.name));
        } else if (obj instanceof PSObjectString) {
            PSObjectString objStr = (PSObjectString)obj;
            return (toString().equals(objStr.toString()));
        } else {
            return false;
        }
    }
    
    /**
     * Overwrites this string with a new value.
     * @param newStr New value for this object
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorRangeCheck The new string is longer than the current string
     */
    public void overwrite(String newStr) throws PSErrorRangeCheck {
        putinterval(0, newStr);
        count = newStr.length();
    }
    
    /**
     * PostScript operator put. Replace a single character in this string.
     * @param index Index or key for new value
     * @param newValue New value
     */
    public void put(PSObject index, PSObject newValue) throws PSErrorRangeCheck,
            PSErrorTypeCheck {
        int idx = index.toInt();
        if ( (idx < 0) || (idx >= count) ) {
            throw new PSErrorRangeCheck();
        }
        
        int chrInt = newValue.toNonNegInt();
        if (chrInt > 255) {
            throw new PSErrorRangeCheck();
        }
        char chr = (char)chrInt;
        
        value.setCharAt(idx, chr);
    }
    
    /**
     * PostScript operator putinterval
     * @param index Start index of subsequence
     * @param newStr String to put in this string
     */
    public void putinterval(int index, String newStr) throws PSErrorRangeCheck {
        if (newStr.length() > (count-index)) {
            throw new PSErrorRangeCheck();
        }
        value.replace(offset+index, offset+index+newStr.length(), newStr);
    }
    
    /**
     * PostScript operator putinterval
     * @param index Start index of subsequence
     * @param obj Subsequence
     */
    public void putinterval(int index, PSObject obj) throws PSErrorTypeCheck, PSErrorRangeCheck {
        String str = obj.toPSString().toString();
        putinterval(index, str);
    }

    /**
     * Convert this object to a string object, if possible
     * @return PostScript string object representation of this object
     */
    public PSObjectString toPSString() {
        return this;
    }

    /**
     * Converts this object to a human readable string.
     * @return Human readable string.
     */
    public String toString() {
        return value.substring(offset, offset+count);
    }

    /** Convert this object to dictionary key, if possible. */
    public String toDictKey() {
        return toString();
    }
    
    /**
     * Creates an exact deep copy of this object.
     */
    public PSObjectString clone() {
        return new PSObjectString(toString());
    }

    /**
     * Returns the type of this object
     * @return Type of this object (see PostScript manual for possible values)
     */
    public String type() {
        return "stringtype";
    }
}
