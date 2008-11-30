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

package net.sf.eps2pgf.ps.objects;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.eps2pgf.ProgramError;
import net.sf.eps2pgf.io.PSStringInputStream;
import net.sf.eps2pgf.io.StringInputStream;
import net.sf.eps2pgf.ps.Interpreter;
import net.sf.eps2pgf.ps.Parser;
import net.sf.eps2pgf.ps.errors.PSError;
import net.sf.eps2pgf.ps.errors.PSErrorIOError;
import net.sf.eps2pgf.ps.errors.PSErrorRangeCheck;
import net.sf.eps2pgf.ps.errors.PSErrorTypeCheck;
import net.sf.eps2pgf.ps.resources.filters.ASCII85Decode;
import net.sf.eps2pgf.ps.resources.filters.ASCIIHexDecode;

/**
 * String PostScript object.
 * @author Wagenaars
 */
public class PSObjectString extends PSObject {
    
    /** Value of this PostScript string. */
    private StringBuilder value;
    
    /**
     * Offset, skip this number of characters at the start of
     * <code>value</code>.
     */
    private int offset;
    
    /** Number of characters in this string. */
    private int count;
    
    /**
     * Create a new PostScript string with n \u0000 characters.
     * 
     * @param n Number of \u0000 characters in the new string
     * 
     * @throws PSErrorRangeCheck <code>n</code> is less than zero
     */
    public PSObjectString(final int n) throws PSErrorRangeCheck {
        if (n < 0) {
            throw new PSErrorRangeCheck();
        }
        StringBuilder str = new StringBuilder(n);
        for (int i = 0; i < n; i++) {
            str.append("\u0000");
        }
        
        value = new StringBuilder(str.toString());
        offset = 0;
        count = value.length();
    }
    
    /**
     * Creates a new instance of PSObjectString.
     * 
     * @param str This object is initialized with a copy of this string.
     */
    public PSObjectString(final String str) {
        value = new StringBuilder(str);
        offset = 0;
        count = value.length();
    }
    
    /**
     * Creates a new instance of PSObjectString.
     * 
     * @param pStr The value of the new PSObjectString will have this value
     * @param isPostScript Interpret the string as PostScript string?
     * 
     * @throws PSError A PostScript error occurred.
     */
    public PSObjectString(final String pStr, final boolean isPostScript)
            throws PSError {
        
        String str = pStr;
        if (isPostScript) {
            int len = str.length();
            if ((len > 0) && (str.charAt(0) == '(')
                    && (str.charAt(len - 1) == ')')) {
                
                str = parse(str.substring(1, len - 1));
                
            } else if ((len > 0) && str.substring(0, 2).equals("<~")
                    && str.substring(len - 2).equals("~>")) {
                
                str = parseBase85(str.substring(2, len - 2));
                
            } else if ((len > 0) && (str.charAt(0) == '<')
                    && (str.charAt(len - 1) == '>')) {
                
                str = parseHex(str.substring(1, len - 1));
            }
        }
        value = new StringBuilder(str);
        offset = 0;
        count = value.length();
    }
    
    /**
     * Create new instance of PSObjectString. The new instance is a subset
     * of an existing stringbuffer.
     * 
     * @param index Index in strBuf of first character
     * @param length Number of characters in new string
     * @param str The string.
     * 
     * @throws PSErrorRangeCheck A PostScript rangecheck error occurred.
     */
    public PSObjectString(final PSObjectString str, final int index,
            final int length) throws PSErrorRangeCheck {
        
        if ((index < 0) || (length < 0)) {
            throw new PSErrorRangeCheck();
        }
        if ((index + length) > str.count) {
            throw new PSErrorRangeCheck();
        }
        value = str.value;
        offset = index + str.offset;
        count = length;
        copyCommonAttributes(str);
    }
    
    /**
     * PostScript operator: anchorsearch.
     * 
     * @param seek Checks whether this string starts with seek.
     * 
     * @return List with PSObjects. See the PostScript manual for more info.
     * If found, list with {post, match, true}.
     * If not found, list with {string, false}.
     */
    public List<PSObject> anchorsearch(final String seek) {
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
                result.add(getinterval(m, n - m));
                result.add(getinterval(0, m));
            } catch (PSErrorRangeCheck e) {
                // This can never happen
            }
            result.add(new PSObjectBool(true));
        }
        return result;
    }
    
    /**
     * Creates a deep copy of this object.
     * 
     * @return Deep copy of this object.
     */
    @Override
    public PSObjectString clone() {
        PSObjectString copy = (PSObjectString) super.clone();
        copy.value = new StringBuilder(value);
        return copy;
    }

    /**
     * PostScript operator copy. Copies values from obj1 to this object.
     * 
     * @param obj1 Copy values from obj1
     * 
     * @return Returns subsequence of this object
     * 
     * @throws PSErrorRangeCheck A PostScript rangecheck error occurred.
     * @throws PSErrorTypeCheck A PostScript typecheck error occurred.
     */
    @Override
    public PSObject copy(final PSObject obj1) throws PSErrorRangeCheck,
            PSErrorTypeCheck {
        
        String obj1Str = obj1.toPSString().toString();
        putinterval(0, obj1Str);
        return getinterval(0, obj1Str.length());
    }

    /**
     * PostScript operator 'cvi'. Convert this object to an integer
     * 
     * @return the int
     * 
     * @throws PSError A PostScript error occurred.
     */
    @Override
    public int cvi() throws PSError {
        PSObjectReal ro = new PSObjectReal(value.toString());
        return ro.cvi();
    }
    
    /**
     * PostScript operator 'cvn'. Convert this object to a name object.
     * 
     * @return This object converted to name object.
     */
    public PSObjectName cvn() {
        return new PSObjectName(value.toString(), isLiteral());
    }

    /**
     * PostScript operator 'cvr'. Convert this object to a real
     * 
     * @return This object converted to double.
     * 
     * @throws PSError A PostScript error occurred.
     */
    @Override
    public double cvr() throws PSError {
        PSObjectReal ro = new PSObjectReal(value.toString());
        return ro.toReal();
    }
    
    /**
     * Produce a text representation of this object (see PostScript
     * operator 'cvs' for more info).
     * 
     * @return Text representation
     */
    @Override
    public String cvs() {
        return toString();
    }

    /**
     * Convert this string to an array with character names.
     * 
     * @param encoding Encoding to use to decode this string
     * 
     * @return Array with character names
     * 
     * @throws PSErrorRangeCheck A PostScript rangecheck error occurred.
     */
    public PSObjectArray decode(final PSObjectArray encoding)
            throws PSErrorRangeCheck {
        
        PSObjectArray arr = new PSObjectArray();
        for (int i = 0; i < count; i++) {
            int chr = get(i);
            arr.addAt(i, encoding.get(chr));
        }
        return arr;
    }
    
    /**
     * PostScript operator 'dup'. Create a copy of this object. The values
     * of composite object is not copied, but shared.
     * 
     * @return Shallow copy of this object.
     */
    @Override
    public PSObjectString dup() {
        try {
            return new PSObjectString(this, 0, count);
        } catch (PSErrorRangeCheck e) {
            // this can never happen
            return null;
        }
    }
    
    /**
     * Compare this object with another object and return true if they are
     * equal. See PostScript manual on what's equal and what's not.
     * 
     * @param obj Object to compare this object with
     * 
     * @return True if objects are equal, false otherwise
     */
    @Override
    public boolean eq(final PSObject obj) {
        if (obj instanceof PSObjectName) {
            PSObjectName objName = (PSObjectName) obj;
            return (toString().equals(objName.toString()));
        } else if (obj instanceof PSObjectString) {
            PSObjectString objStr = (PSObjectString) obj;
            return (toString().equals(objStr.toString()));
        } else {
            return false;
        }
    }
    
    /**
     * Indicates whether some other object is equal to this one.
     * Required when used as index in PSObjectDict
     * 
     * @param obj The object to compare to.
     * 
     * @return True, if equal.
     */
    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof PSObject) {
            return eq((PSObject) obj);
        } else {
            return false;
        }
    }
    
    /**
     * Gets the (integer) value of a character in this string.
     * 
     * @param index Index of character (first character has index 0)
     * 
     * @return Integer value of requested character
     * 
     * @throws PSErrorRangeCheck A PostScript rangecheck error occurred.
     */
    public int get(final int index) throws PSErrorRangeCheck {
        if ((index < 0) || (index >= count)) {
            throw new PSErrorRangeCheck();
        }
        
        return value.charAt(index + offset);
    }
    
    /**
     * Gets the (integer) value of a character in this string.
     * 
     * @param index Index of character (first character has index 0)
     * 
     * @return Integer value of requested character
     * 
     * @throws PSErrorTypeCheck A PostScript typecheck error occurred.
     * @throws PSErrorRangeCheck A PostScript rangecheck error occurred.
     */
    @Override
    public PSObject get(final PSObject index) throws PSErrorTypeCheck,
            PSErrorRangeCheck {
        
        int chr = get(index.toInt());
        return new PSObjectInt(chr);
    }
    
    /**
     * Implements PostScript operator getinterval. Returns a new object
     * with an interval from this object.
     * 
     * @param index Index of the first character of substring
     * @param pCount Number of characters in the substring
     * 
     * @return Object representing a subarray of this object. The data is shared
     * between both objects.
     * 
     * @throws PSErrorRangeCheck Invalid index or number of elements
     */
    @Override
    public PSObjectString getinterval(final int index, final int pCount)
            throws PSErrorRangeCheck {
        
        return new PSObjectString(this, index, pCount);
    }
    
    /**
     * Returns a list with all items in object.
     * 
     * @return List with all items in this object. The first object (with index
     * 0) is always a PSObjectInt with the number of object in a single item.
     * For most object types this is 1, but for dictionaries this is 2. All
     * consecutive items (index 1 and up) are the object's items.
     * 
     * @throws PSErrorTypeCheck A PostScript typecheck error occurred.
     */
    @Override
    public List<PSObject> getItemList() throws PSErrorTypeCheck {
        List<PSObject> items = new LinkedList<PSObject>();
        items.add(new PSObjectInt(1));
        
        for (PSObject chr : this) {
            items.add(chr);
        }

        return items;
    }
    
    /**
     * PostScript operator 'gt'.
     * 
     * @param obj2 Object to compare this object to
     * 
     * @return Returns true when this object is greater than obj2, return false
     * otherwise.
     * 
     * @throws PSErrorTypeCheck A PostScript typecheck error occurred.
     */
    @Override
    public boolean gt(final PSObject obj2) throws PSErrorTypeCheck {
        String obj1Str = value.toString();
        String obj2Str = obj2.toPSString().toString();
        return (obj1Str.compareTo(obj2Str) > 0);
    }
    
    /**
     * Return a hash code for this object.
     * 
     * @return The hash code.
     */
    @Override
    public int hashCode() {
        return this.toString().hashCode();
    }

    /**
     * Return PostScript text representation of this object. See the
     * PostScript manual under the == operator
     * 
     * @return PostScript representation of this object.
     */
    @Override
    public String isis() {
        // Determine whether to use to normal or the hex version.
        int nrPrint = 0;
        int nrNonPrint = 0;
        try {
            for (int i = 0; i < length(); i++) {
                int c = get(i);
                if ((c >= 32) && (c <= 127)) {
                    nrPrint++;
                } else {
                    nrNonPrint++;
                }
            }
        } catch (PSErrorRangeCheck e) {
            // this can never happen
        }
        if ((nrNonPrint > 5) && ((2 * nrNonPrint) >= nrPrint)) {
            return isisHex();
        } else {
            return isisNormal();
        }
    }
    
    /**
     * Return PostScript text representation of this object. See the
     * PostScript manual under the == operator
     * 
     * @return PostScript representation of this object.
     */
    private String isisNormal() {
        StringBuilder str = new StringBuilder();
        String rawString = toString();
        str.append('(');
        for (int i = 0; i < rawString.length(); i++) {
            char c = rawString.charAt(i);
            if (c == '(') {
                str.append("\\(");
            } else if (c == ')') {
                str.append("\\)");
            } else if (c == '\\') {
                str.append("\\\\");
            } else if ((c >= 32) && (c <= 126)) {
                // Printable character
                str.append(c);
            } else if (c == '\r') {
                str.append("\\r");
            } else if (c == '\n') {
                str.append("\\n");
            } else if (c == '\t') {
                str.append("\\t");
            } else if (c == '\b') {
                str.append("\\b");
            } else if (c == '\f') {
                str.append("\\f");
            } else {
                // Non-printable character -> use octal code
                str.append('\\');
                str.append(Integer.toOctalString(c));
            }
        }
        str.append(')');
        return str.toString();
    }
    
    /**
     * Return hex string representation of this object. See the
     * PostScript manual under the == operator
     * 
     * @return Hex string of this object.
     */
    private String isisHex() {
        StringBuilder str = new StringBuilder();
        String rawString = toString();
        str.append("<\n");
        for (int i = 0; i < rawString.length(); i++) {
            int c = rawString.charAt(i);
            str.append(String.format("%02x", c));
            if ((i % 40) == 39) {
                str.append('\n');
            }
        }
        str.append("\n>");
        return str.toString();
    }
    
    
    /**
     * Check whether a string is a PostScript string.
     * 
     * @param str String to check.
     * 
     * @return Returns true when str is a valid PostScript string. Returns false
     * otherwise.
     */
    public static boolean isType(final String str) {
        int len = str.length();
        if ((len >= 2) && (str.charAt(0) == '(')
                && (str.charAt(len - 1) == ')')) {
            
            return true;
            
        } else if ((len >= 4) && str.substring(0, 2).equals("<~")
                && str.substring(len - 2).equals("~>")) {
            
            return true;
            
        } else {
            return ((len >= 2) && (str.charAt(0) == '<')
                    && (str.charAt(len - 1) == '>'));
        }
    }
    
    /**
     * Implements PostScript operate: length.
     * 
     * @return Length of this object
     */
    @Override
    public int length() {
        return count;
    }

    /**
     * Overwrites this string with a new value.
     * 
     * @param newStr New value for this object
     * 
     * @throws PSErrorRangeCheck The new string is longer than the current
     * string.
     */
    public void overwrite(final String newStr) throws PSErrorRangeCheck {
        putinterval(0, newStr);
        count = newStr.length();
    }
    
    /**
     * Parse a postscript string.
     * 
     * @param str The string to parse.
     * 
     * @return The parsed string.
     */
    public String parse(final String str) {
        StringBuilder newStr = new StringBuilder();
        char chr;
        for (int i = 0; i < str.length(); i++) {
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
                    chr = (char) Integer.parseInt(m.group().substring(1), 8);
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
     * Decode an ASCII base-85 string.
     * 
     * @param str String to decode
     * 
     * @return Decoded string
     * 
     * @throws PSError A PostScript error occurred.
     */
    public String parseBase85(final String str) throws PSError {
        InputStream stringStream = new StringInputStream(str);
        PSObjectDict dict = new PSObjectDict();
        InputStream inStream = new ASCII85Decode(stringStream, dict);
        
        StringBuilder parsedStr = new StringBuilder();
        int c;
        try {
            while ((c = inStream.read()) != -1) {
                parsedStr.append((char) c);
            }
        } catch (IOException e) {
            throw new PSErrorIOError();
        }

        return parsedStr.toString();
    }
    
    /**
     * Decode a hex string.
     * 
     * @param str String to decode
     * 
     * @return Decoded string
     * 
     * @throws PSError A PostScript error occurred.
     */
    public String parseHex(final String str) throws PSError {
        InputStream stringStream = new StringInputStream(str);
        PSObjectDict dict = new PSObjectDict();
        InputStream inStream = new ASCIIHexDecode(stringStream, dict);
        
        StringBuilder parsedStr = new StringBuilder();
        int c;
        try {
            while ((c = inStream.read()) != -1) {
                parsedStr.append((char) c);
            }
        } catch (IOException e) {
            throw new PSErrorIOError();
        }

        return parsedStr.toString();
    }
    
    /**
     * PostScript operator put. Replace a single character in this string.
     * 
     * @param index Index or key for new value
     * @param newValue New value
     * 
     * @throws PSErrorRangeCheck A PostScript rangecheck error occurred.
     * @throws PSErrorTypeCheck A PostScript typecheck error occurred.
     */
    @Override
    public void put(final PSObject index, final PSObject newValue)
            throws PSErrorRangeCheck, PSErrorTypeCheck {
        
        int idx = index.toInt();
        if ((idx < 0) || (idx >= count)) {
            throw new PSErrorRangeCheck();
        }
        
        int chrInt = newValue.toNonNegInt();
        if (chrInt > 255) {
            throw new PSErrorRangeCheck();
        }
        char chr = (char) chrInt;
        
        value.setCharAt(idx, chr);
    }
    
    /**
     * PostScript operator putinterval.
     * 
     * @param index Start index of subsequence
     * @param newStr String to put in this string
     * 
     * @throws PSErrorRangeCheck A PostScript rangecheck error occurred.
     */
    public void putinterval(final int index, final String newStr)
            throws PSErrorRangeCheck {
        
        if ((index < 0) || (newStr.length() > (count - index))) {
            throw new PSErrorRangeCheck();
        }
        value.replace(offset + index, offset + index + newStr.length(), newStr);
    }
    
    /**
     * PostScript operator putinterval.
     * 
     * @param index Start index of subsequence
     * @param obj Subsequence
     * 
     * @throws PSErrorTypeCheck A PostScript typecheck error occurred.
     * @throws PSErrorRangeCheck A PostScript rangecheck error occurred.
     */
    @Override
    public void putinterval(final int index, final PSObject obj)
            throws PSErrorTypeCheck, PSErrorRangeCheck {
        
        String str = obj.toPSString().toString();
        putinterval(index, str);
    }
    
    /**
     * PostScript operator: search.
     * 
     * @param seek Look for this string in this PSObjectString.
     * 
     * @return List with PSObjects. See the PostScript manual for more info.
     * If found, list with {post, match, pre, true}.
     * If not found, list with {string, false}.
     */
    public List<PSObject> search(final String seek) {
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
                result.add(getinterval(k + m, n - k - m));
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
     * Changes a single character in this string.
     * 
     * @param index Index of the character to be replaced
     * @param chr New character.
     * 
     * @throws PSErrorRangeCheck A PostScript rangecheck error occurred.
     */
    public void set(final int index, final char chr) throws PSErrorRangeCheck {
        if ((index < 0) || (index >= count)) {
            throw new PSErrorRangeCheck();
        }
        
        value.setCharAt(index + offset, chr);
    }

    /**
     * Convert this object to a string object, if possible.
     * 
     * @return PostScript string object representation of this object
     */
    @Override
    public PSObjectString toPSString() {
        return this;
    }

    /**
     * Converts this object to a human readable string.
     * 
     * @return Human readable string.
     */
    @Override
    public String toString() {
        return value.substring(offset, offset + count);
    }
    
    /**
     * Reads characters from this object, interpreting them as PostScript
     * code, until it has scanned and constructed an entire object.
     * 
     * @param interp The interpreter. (only required if string contains
     * immediately evaluated names).
     * 
     * @return List with one or more objects. See PostScript manual under the
     * 'token' operator for more info.
     * 
     * @throws PSError Unable to read a token from this object
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    @Override
    public List<PSObject> token(final Interpreter interp)
            throws PSError, ProgramError {
        
        InputStream inStream = new PSStringInputStream(this);
        PSObject any;
        try {
            any = Parser.convertSingle(inStream, interp);
        } catch (IOException e) {
            any = null;
        }
        
        List<PSObject> lst = new ArrayList<PSObject>();
        if (any != null) {
            int chrs = Parser.getCharsLastConvert();
            PSObjectString post;
            try {
                post = getinterval(chrs, count - chrs);
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
     * Returns the type of this object.
     * 
     * @return Type of this object (see PostScript manual for possible values)
     */
    @Override
    public String type() {
        return "stringtype";
    }
    
    /**
     * Treat this string as an encoded number string and get the length.
     * 
     * @return The number of number in this string.
     * 
     * @throws PSError A PostScript error occurred.
     */
    public int numstringLength() throws PSError {
        // Check first byte
        if (get(0) != 149) {
            throw new PSErrorTypeCheck();
        }
        
        int numRepr = get(1);
        int len;
        if (numRepr < 128) {
            len = (get(2) << 8) | get(3);
        } else {
            len = (get(3) << 8) | get(2);
        }
        return len;
    }
    
    /**
     * Treat this string as an encoded number string and get a number.
     * 
     * @param i The index of the number.
     * 
     * @return The requested number from the number string.
     * 
     * @throws PSError A PostScript error occurred.
     */
    public double numstringGet(final int i) throws PSError {
        // Check first byte
        if (get(0) != 149) {
            throw new PSErrorTypeCheck();
        }        
        int numRepr = get(1);
        boolean lsbLast;
        if (numRepr < 128) {
            lsbLast = true;
        } else {
            lsbLast = false;
            numRepr -= 128;
        }
        
        double num;
        if (numRepr < 32) {
            int numint;
            if (lsbLast) {
                numint = (get(4 * i + 4) << 24) | (get(4 * i + 5) << 16)
                        | (get(4 * i + 6) << 8) | get(4 * i + 7);
            } else {
                numint = (get(4 * i + 7) << 24) | (get(4 * i + 6) << 16)
                        | (get(4 * i + 5) << 8) | get(4 * i + 4);
            }
            num = ((double) numint) / Math.pow(2.0, numRepr);
        } else if (numRepr < 48) {
            short numint;
            if (lsbLast) {
                numint = (short) ((short) (get(2 * i + 4) << 8)
                        | get(2 * i + 5));
            } else {
                numint = (short) ((short) (get(2 * i + 5) << 8)
                        | get(2 * i + 4));
            }
            num = ((double) numint) / Math.pow(2.0, numRepr - 32.0);
        } else if (numRepr < 50) {
            // See http://babbage.cs.qc.edu/IEEE-754/Decimal.html for easy
            // converted from real to hex.
            int numint;
            if (lsbLast) {
                numint = (get(4 * i + 4) << 24) | (get(4 * i + 5) << 16)
                        | (get(4 * i + 6) << 8) | get(4 * i + 7);
            } else {
                numint = (get(4 * i + 7) << 24) | (get(4 * i + 6) << 16)
                        | (get(4 * i + 5) << 8) | get(4 * i + 4);
            }
            num = (double) Float.intBitsToFloat(numint);
        } else {
            throw new PSErrorTypeCheck();
        }
        return num;
    }

}
