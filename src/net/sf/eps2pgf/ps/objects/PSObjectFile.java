/*
 * PSObjectFile.java
 *
 * This file is part of Eps2pgf.
 *
 * Copyright 2007, 2008 Paul Wagenaars <paul@wagenaars.org>
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

import net.sf.eps2pgf.io.StringInputStream;
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
    public void closefile() throws PSErrorIOError {
        try {
            getStream().close();
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
        PSObjectFile dupFile = new PSObjectFile(getStream());
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
                int chr = getStream().read();
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
     * @throws PSError Unable to read a token from this object.
     * 
     * @return List with one or more objects. See PostScript manual under the
     * 'token' operator for more info.
     */
    @Override
    public List<PSObject> token() throws PSError {
        PSObject any;
        try {
            any = Parser.convertSingle(getStream());
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