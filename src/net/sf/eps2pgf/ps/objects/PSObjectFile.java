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
import java.util.List;

import net.sf.eps2pgf.ProgramError;
import net.sf.eps2pgf.io.StringInputStream;
import net.sf.eps2pgf.ps.Interpreter;
import net.sf.eps2pgf.ps.Parser;
import net.sf.eps2pgf.ps.errors.PSError;
import net.sf.eps2pgf.ps.errors.PSErrorIOError;
import net.sf.eps2pgf.ps.errors.PSErrorRangeCheck;

/**
 * PostScript file object.
 * 
 * @author Paul Wagenaars
 */
public class PSObjectFile extends PSObject {
    
    /** Input stream from which data is read. */
    private InputStream inStr;
    
    /**
     * Creates a new instance of PSObjectFile.
     * 
     * @param fileInputStream Reader to access the file
     */
    public PSObjectFile(final InputStream fileInputStream) {
        if (fileInputStream != null) {
            setStream(fileInputStream);
        } else {
            setStream(new StringInputStream(""));
        }
        setLiteral(false);
    }
    
    /**
     * Creates a deep copy of this object.
     * 
     * @return Deep copy of this object.
     */
    @Override
    public PSObjectFile clone() {
        PSObjectFile copy = (PSObjectFile) super.clone();
        return copy;
    }

    /**
     * PostScript operator 'closefile'. Breaks connection between this file
     * object and the <code>InputStream</code>.
     * 
     * @throws PSErrorIOError the PS error io error
     */
    public void closeFile() throws PSErrorIOError {
        try {
            inStr.close();
        } catch (IOException e) {
            throw new PSErrorIOError();
        }
    }
    
    /**
     * PostScript operator 'dup'. Create a (shallow) copy of this object. The
     * values of composite object is not copied, but shared.
     * 
     * @return Shallow copy of this object.
     */
    @Override
    public PSObjectFile dup() {
        PSObjectFile dupFile = new PSObjectFile(inStr);
        dupFile.copyCommonAttributes(this);
        return dupFile;
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
        if (obj instanceof PSObjectFile) {
            return eq((PSObjectFile) obj);
        } else {
            return false;
        }
    }
    
    /**
     * PostScript operator: flushfile
     * If file is an input file, flushfile reads and discards data from that
     * file until the end-of-file indication is encountered. flushfile does not
     * close the file, unless it is a decoding filter file.
     * 
     * @throws PSError A PostScript error occurred.
     */
    public void flushFile() throws PSError {
        try {
            while (true) {
                int value = inStr.read();
                if (value < 0) {
                    break;
                }
            }
        } catch (IOException e) {
            throw new PSErrorIOError();
        }
        
        String name = inStr.toString();
        if (name.contains("eps2pgf") && name.contains("Decode")) {
            closeFile();
        }
    }
    
    /**
     * Returns a hash code value for the object.
     * 
     * @return Hash code of this object.
     */
    @Override
    public int hashCode() {
        return inStr.hashCode();
    }
    
    /**
     * Return PostScript text representation of this object. See the
     * PostScript manual under the == operator
     * @return Text representation of this object.
     */
    @Override
    public String isis() {
        return "-file-";
    }
    
    /**
     * Reads a line of characters (terminated by a (CR), (LF) or (CR)(LF)) from
     * this file and stores them in a string.
     * 
     * @param string The string.
     * 
     * @return Array with two items. The first item is the substring, the second
     * item is normally true and false when the end-of-file was encountered
     * before the end of the line.
     * 
     * @throws PSError A PostScript error occurred.
     */
    public PSObjectArray readLine(final PSObjectString string)
            throws PSError {
        
        boolean eofNotReached = false;
        int charsRead = -1;
        try {
            for (int i = 0; true; i++) {
                int chr = inStr.read();
                if (chr == 10) {         // line feed (LF)
                    eofNotReached = true;
                    charsRead = i;
                    break;
                } else if (chr == 13) {  // carriage return (CR)
                    // if the next character is a (LF) we need to consume it too
                    inStr.mark(1);
                    chr = inStr.read();
                    if (chr != 10) {
                        inStr.reset();
                    }
                    eofNotReached = true;
                    charsRead = i;
                    break;
                } else if (chr == -1) {  // end-of-file (EOF)
                    charsRead = i;
                } else {
                    string.set(i, (char) chr);
                }
            }
        } catch (IOException e) {
            throw new PSErrorIOError();
        }
        
        PSObjectArray ret = new PSObjectArray();
        ret.addToEnd(string.getinterval(0, charsRead));
        ret.addToEnd(new PSObjectBool(eofNotReached));
        
        return ret;
    }
    
    /**
     * Reads characters from this file and stores them in the supplied string
     * until the string is full or the end-of-file is encountered.
     * 
     * @param string The string.
     * 
     * @return (Sub)string of the supplied string with the new characters. If
     * this string is shorter than the supplied string that indicates
     * that the end-of-file was reached before the string was full.
     * 
     * @throws PSErrorIOError the PS error io error
     */
    public PSObjectString readstring(final PSObjectString string)
            throws PSErrorIOError {
        int n = string.length();
        int length = n;
        try {
            for (int i = 0; i < n; i++) {
                int chr = inStr.read();
                if (chr == -1) {
                    length = i;
                    break;
                }
                string.set(i, (char) chr);
            }
            return string.getinterval(0, length);
        } catch (IOException e) {
            throw new PSErrorIOError();
        } catch (PSErrorRangeCheck e) {
            // this can never happen
        }
        return null;
    }
    
    /**
     * Checks whether this file is open or closed.
     * 
     * @return True, if file is open. False if file is closed.
     */
    public boolean status() {
        int available;
        try {
            available = inStr.available();
        } catch (IOException e) {
            available = 0;
        }
        return (available > 0);
    }
    
    /**
     * Returns this object.
     * 
     * @return File object representation of this object
     */
    @Override
    public PSObjectFile toFile() {
        return this;
    }
    
    /**
     * Reads characters from this object, interpreting them as PostScript
     * code, until it has scanned and constructed an entire object.
     * 
     * @param interp The interpreter (only required in object contains
     * immediately evaluated names).
     * 
     * @return List with one or more objects. See PostScript manual under the
     * 'token' operator for more info.
     * 
     * @throws PSError Unable to read a token from this object.
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    @Override
    public List<PSObject> token(final Interpreter interp)
            throws PSError, ProgramError {
        
        PSObject any;
        try {
            any = Parser.convertSingle(inStr, interp);
        } catch (IOException e) {
            throw new PSErrorIOError();
        }
        List<PSObject> retList = new ArrayList<PSObject>();
        if (any != null) {
            retList.add(any);
            retList.add(new PSObjectBool(true));
        } else {
            retList.add(new PSObjectBool(false));
        }
        return retList;
    }

    /**
     * Returns the type of this object.
     * 
     * @return Type of this object (see PostScript manual for possible values)
     */
    @Override
    public String type() {
        return "filetype";
    }

    /**
     * Sets the stream.
     * 
     * @param inputStream The input stream.
     */
    void setStream(final InputStream inputStream) {
        inStr = inputStream;
    }

    /**
     * @return the inStr
     */
    public InputStream getStream() {
        return inStr;
    }

}
