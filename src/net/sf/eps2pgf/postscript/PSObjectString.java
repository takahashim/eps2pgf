/*
 * PSObjectString.java
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
import java.util.regex.*;

import org.freehep.util.io.ASCII85InputStream;

import net.sf.eps2pgf.util.*;
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
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorRangeCheck <code>n</code> is less than zero
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
     * @param str This object is initialized with a copy of this string. The
     *            string has to be a valid PostScript string enclosed in:
     *            (..), <..> or <~..~>.
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorIOError The supplied string is invalid
     */
    public PSObjectString(String str) {
        value = new StringBuffer(str);
        offset = 0;
        count = value.length();
    }
    
    /**
     * Creates a new instance of PSObjectString
     * @param str The value of the new PSObjectString will have this value
     * @param dummy As the name suggests, it doesn't do anything.
     */
    public PSObjectString(String str, boolean isPostScript) throws PSErrorIOError {
        if (isPostScript) {
            int len = str.length();
            if ( (len > 0) && (str.charAt(0) == '(') && (str.charAt(len-1) == ')') ) {
                str = parse(str.substring(1, len-1));
            } else if ( (len > 0) && str.substring(0,2).equals("<~") && str.substring(len-2).equals("~>") ) {
                str = parseBase85(str.substring(2,len-2));
            } else if ( (len > 0) && (str.charAt(0) == '<') && (str.charAt(len-1) == '>') ) {
                str = parseHex(str.substring(1, len-1));
            }
        }
        value = new StringBuffer(str);
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
    public PSObjectString(PSObjectString str, int index, int length) throws PSErrorRangeCheck {
        if ((index < 0) || (length < 0)) {
            throw new PSErrorRangeCheck();
        }
        if ((index+length) > str.count) {
            throw new PSErrorRangeCheck();
        }
        value = str.value;
        offset = index + str.offset;
        count = length;
        copyCommonAttributes(str);
    }
    
    /**
     * PostScript operator: anchorsearch
     * @param seek Checks whether this string starts with seek.
     * @return List with PSObjects. See the PostScript manual for more info.
     *         If found, list with {post, match, true}.
     *         If not found, list with {string, false}.
     */
    public List<PSObject> anchorsearch(String seek) throws PSErrorInvalidAccess {
        String string = toString();
        int n = string.length();
        int m = seek.length();
        List<PSObject> result = new LinkedList<PSObject>();
        
        if (!string.startsWith(seek)) {
            // seek not found
            result.add(this);
            result.add(new PSObjectBool(false));
        } else {
            try {
                result.add(getinterval(m, n-m));
                result.add(getinterval(0, m));
            } catch (PSErrorRangeCheck e) {
                // This can never happen
            }
            result.add(new PSObjectBool(true));
        }
        return result;
    }
    
    /**
     * Creates an exact deep copy of this object.
     */
    public PSObjectString clone() {
        return new PSObjectString(toString());
    }

    /**
     * PostScript operator copy. Copies values from obj1 to this object.
     * @param obj1 Copy values from obj1
     * @return Returns subsequence of this object
     */
    public PSObject copy(PSObject obj1) throws PSErrorRangeCheck, PSErrorTypeCheck,
            PSErrorInvalidAccess {
        String obj1Str = obj1.toPSString().toString();
        putinterval(0, obj1Str);
        return getinterval(0, obj1Str.length());
    }

    /**
     * PostScript operator 'cvi'. Convert this object to an integer
     */
    public int cvi() throws PSErrorTypeCheck, PSErrorInvalidAccess {
        PSObjectReal ro = new PSObjectReal(value.toString());
        return ro.cvi();
    }
    
    /**
     * PostScript operator 'cvn'. Convert this object to a name object.
     */
    public PSObjectName cvn() throws PSErrorInvalidAccess {
        return new PSObjectName(value.toString(), isLiteral);
    }

    /**
     * PostScript operator 'cvr'. Convert this object to a real
     */
    public double cvr() throws PSErrorTypeCheck, PSErrorInvalidAccess {
        PSObjectReal ro = new PSObjectReal(value.toString());
        return ro.toReal();
    }
    
    /**
     * Produce a text representation of this object (see PostScript
     * operator 'cvs' for more info)
     * @return Text representation
     */
    public String cvs() throws PSErrorInvalidAccess {
        return toString();
    }

    /**
     * Convert this string to an array with character names
     * @param encoding Encoding to use to decode this string
     * @return Array with character names
     */
    public PSObjectArray decode(PSObjectArray encoding) throws PSErrorRangeCheck,
            PSErrorInvalidAccess {
        PSObjectArray arr = new PSObjectArray();
        for (int i = 0 ; i < count ; i++) {
            int chr = get(i);
            arr.addAt(i, encoding.get(chr));
        }
        return arr;
    }
    
    /**
     * PostScript operator 'dup'. Create a copy of this object. The values
     * of composite object is not copied, but shared.
     */
    public PSObjectString dup() {
        try {
            return new PSObjectString(this, 0, count);
        } catch (PSErrorRangeCheck e) {
            // this can never happen
            return null;
        }
    }
    
    /**
     * Compare this object with another object and return true if they are equal.
     * See PostScript manual on what's equal and what's not.
     * @param obj Object to compare this object with
     * @return True if objects are equal, false otherwise
     */
    public boolean eq(PSObject obj) throws PSErrorInvalidAccess {
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
     * PostScript operator 'executeonly'. Set access attribute to executeonly.
     */
    public void executeonly() {
        access = ACCESS_EXECUTEONLY;
    }

    /**
     * Gets the (integer) value of a character in this string.
     * @param index Index of character (first character has index 0)
     * @return Integer value of requested character
     */
    public int get(int index) throws PSErrorRangeCheck, PSErrorInvalidAccess {
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
    public PSObject get(PSObject index) throws PSErrorTypeCheck, PSErrorRangeCheck,
            PSErrorInvalidAccess {
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
    public PSObjectString getinterval(int index, int count) throws PSErrorRangeCheck,
            PSErrorInvalidAccess {
        return new PSObjectString(this, index, count);
    }
    
    /**
     * Returns a list with all items in object.
     * @return List with all items in this object. The first object (with
     *         index 0) is always a PSObjectInt with the number of object
     *         in a single item. For most object types this is 1, but for
     *         dictionaries this is 2. All consecutive items (index 1 and
     *         up) are the object's items.
     */
    public List<PSObject> getItemList() throws PSErrorTypeCheck,
            PSErrorInvalidAccess {
        List<PSObject> items = new LinkedList<PSObject>();
        items.add(new PSObjectInt(1));
        
        for(PSObject chr : this) {
            items.add(chr);
        }
        //for (int i = 0 ; i < count ; i++) {
        //    items.add(new PSObjectInt(get(i)));
        //}

        return items;
    }
    
    /**
     * PostScript operator 'gt'
     * @param obj2 Object to compare this object to
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorTypeCheck Unable to compare the type of this object and/or obj2
     * @return Returns true when this object is greater than obj2, return false
     * otherwise.
     */
    public boolean gt(PSObject obj2) throws PSErrorTypeCheck, PSErrorInvalidAccess {
        String obj1Str = value.toString();
        String obj2Str = obj2.toPSString().toString();
        return (obj1Str.compareTo(obj2Str) > 0);
    }
    
    /** Return PostScript text representation of this object. See the
     * PostScript manual under the == operator
     */
    public String isis() {
        return "(" + toString() + ")";
    }
    
    /**
     * Check whether a string is a PostScript string
     * @param str String to check.
     * @return Returns true when str is a valid PostScript string. Returns false otherwise.
     */
    public static boolean isType(String str) {
        int len = str.length();
        if ( (len >= 2) && (str.charAt(0) == '(') && (str.charAt(len-1) == ')') ) {
            return true;
        } else if ( (len >= 4) && str.substring(0,2).equals("<~") && str.substring(len-2).equals("~>") ) {
            return true;
        } else if ( (len >= 2) && (str.charAt(0) == '<') && (str.charAt(len-1) == '>') ) {
            return true;
        } else {
            return false;
        }
    }
    
    /**
     * Implements PostScript operate: length
     * @return Length of this object
     */
    public int length() throws PSErrorInvalidAccess {
        return count;
    }

    /**
     * PostScript operator: 'noaccess'
     */
    public void noaccess() {
        access = ACCESS_NONE;
    }
    
    /**
     * Overwrites this string with a new value.
     * @param newStr New value for this object
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorRangeCheck The new string is longer than the current string
     */
    public void overwrite(String newStr) throws PSErrorRangeCheck, PSErrorInvalidAccess {
        putinterval(0, newStr);
        count = newStr.length();
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
     * Decode an ASCII base-85 string
     * @param str String to decode
     * @return Decoded string
     */
    public String parseBase85(String str) throws PSErrorIOError {
        str = str.replaceAll("\\s+", "");
        str = str.replaceAll("z", "!!!!!");
        
        StringReader strReader = new StringReader(str);
        ReaderInputStream strInputStream = new ReaderInputStream(strReader);
        ASCII85InputStream a85Stream = new ASCII85InputStream(strInputStream);
        
        StringBuilder parsedStr = new StringBuilder();
        try {
            int i = a85Stream.read();
            while (i != -1) {
                parsedStr.append((char)i);
                i = a85Stream.read();
            }
        } catch (IOException e) {
            // end of string has been reached
        }
        
        return parsedStr.toString();
    }
    
    /**
     * Decode a hex string
     * @param str String to decode
     * @return Decoded string
     */
    public String parseHex(String str) throws PSErrorIOError {
        str = str.replaceAll("\\s+", "");
        if ( (str.length() % 2) != 0 ) {
            str += "0";
        }
        
        int n = str.length()/2;
        StringBuilder parsedStr = new StringBuilder(n);
        for (int i = 0 ; i < n ; i++) {
            String currChar = str.substring(2*i, 2*(i+1));
            try {
                parsedStr.append((char)Integer.parseInt(currChar, 16));
            } catch (NumberFormatException e) {
                throw new PSErrorIOError();
            }
        }
        return parsedStr.toString();
    }
    
    /**
     * PostScript operator put. Replace a single character in this string.
     * @param index Index or key for new value
     * @param newValue New value
     */
    public void put(PSObject index, PSObject newValue) throws PSErrorRangeCheck,
            PSErrorTypeCheck, PSErrorInvalidAccess {
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
    public void putinterval(int index, String newStr) throws PSErrorRangeCheck,
            PSErrorInvalidAccess {
        if ((index < 0) || (newStr.length() > (count-index))) {
            throw new PSErrorRangeCheck();
        }
        value.replace(offset+index, offset+index+newStr.length(), newStr);
    }
    
    /**
     * PostScript operator putinterval
     * @param index Start index of subsequence
     * @param obj Subsequence
     */
    public void putinterval(int index, PSObject obj) throws PSErrorTypeCheck, PSErrorRangeCheck,
            PSErrorInvalidAccess {
        String str = obj.toPSString().toString();
        putinterval(index, str);
    }
    
    /**
     * PostScript operator 'rcheck'. Checks whether the access attribute is
     * 'unlimited' or 'readonly'.
     */
    public boolean rcheck() {
        if ( (access == ACCESS_UNLIMITED) || (access == ACCESS_READONLY) ) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * PostScript operator: 'readonly'
     */
    public void readonly() throws PSErrorInvalidAccess {
        access = ACCESS_READONLY;
    }
    
    /**
     * PostScript operator: search
     * @param seek Look for this string in this PSObjectString.
     * @return List with PSObjects. See the PostScript manual for more info.
     *         If found, list with {post, match, pre, true}.
     *         If not found, list with {string, false}.
     */
    public List<PSObject> search(String seek) throws PSErrorInvalidAccess {
        String string = toString();
        int n = string.length();
        int m = seek.length();
        List<PSObject> result = new LinkedList<PSObject>();
        
        int k = string.indexOf(seek);
        if (k == -1) {
            // seek not found
            result.add(this);
            result.add(new PSObjectBool(false));
        } else {
            try {
                result.add(getinterval(k+m, n-k-m));
                result.add(getinterval(k, m));
                result.add(getinterval(0, k));
            } catch (PSErrorRangeCheck e) {
                // This can never happen
            }
            result.add(new PSObjectBool(true));
        }
        return result;
    }

    /**
     * Convert this object to a string object, if possible
     * @return PostScript string object representation of this object
     */
    public PSObjectString toPSString() {
        return this;
    }

    /** Convert this object to dictionary key, if possible. */
    public String toDictKey() throws PSErrorInvalidAccess {
        return toString();
    }
    
    /**
     * Converts this object to a human readable string.
     * @return Human readable string.
     */
    public String toString() {
        return value.substring(offset, offset+count);
    }
    
    /**
     * Reads characters from this object, interpreting them as PostScript
     * code, until it has scanned and constructed an entire object.
     * @throws net.sf.eps2pgf.postscript.errors.PSError Unable to read a token from this object
     * @return List with one or more objects. See PostScript manual under the
     * 'token' operator for more info.
     */
    public List<PSObject> token() throws PSError {
        Reader rdr = new StringReader(toString());
        PSObject any;
        try {
            any = Parser.convertSingle(rdr);
        } catch (IOException e) {
            any = null;
        }
        
        List<PSObject> lst = new ArrayList<PSObject>();
        if (any != null) {
            int chrs = Parser.charsLastConvert;
            PSObjectString post;
            try {
                post = getinterval(chrs, count-chrs);
            } catch (PSErrorRangeCheck e) {
                post = new PSObjectString("");
            }
            lst.add(post);
            lst.add(any);
            lst.add(new PSObjectBool(true));
        } else {
            lst.add(new PSObjectBool(false));
        }
        return lst;
    }
    
    /**
     * Returns the type of this object
     * @return Type of this object (see PostScript manual for possible values)
     */
    public String type() {
        return "stringtype";
    }

    /**
     * PostScript operator 'wcheck'. Checks whether the access attribute is
     * 'unlimited'.
     */
    public boolean wcheck() {
        return (access == ACCESS_UNLIMITED);
    }
}
