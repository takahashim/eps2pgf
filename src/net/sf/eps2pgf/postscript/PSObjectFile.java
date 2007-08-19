/*
 * PSObjectFile.java
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

import net.sf.eps2pgf.postscript.errors.*;

/**
 * PostScript file object
 * @author Paul Wagenaars
 */
public class PSObjectFile extends PSObject {
    /**
     * Reader (should be a file, other readers are possible) from which the
     * data will be read.
     */
    Reader rdr;
    
    /**
     * Creates a new instance of PSObjectFile
     * @param fileReader Reader to access the file
     */
    public PSObjectFile(Reader fileReader) {
        this.rdr = fileReader;
        this.isLiteral = false;
    }
    
    /**
     * PostScript operator 'dup'. Create a (shallow) copy of this object. The values
     * of composite object is not copied, but shared.
     * @return Shallow copy of this object.
     */
    public PSObjectFile dup() {
        PSObjectFile dupFile = new PSObjectFile(rdr);
        dupFile.copyCommonAttributes(this);
        return dupFile;
    }
    
    /**
     * PostScript operator 'executeonly'. Set access attribute to executeonly.
     */
    public void executeonly() {
        access = ACCESS_EXECUTEONLY;
    }
    
    /**
     * Return PostScript text representation of this object. See the
     * PostScript manual under the == operator
     * @return Text representation of this object.
     */
    public String isis() {
        return "-file-";
    }
    
    /**
     * PostScript operator: 'noaccess'
     */
    public void noaccess() {
        access = ACCESS_NONE;
    }
    
    /**
     * Returns this object
     * @return File object representation of this object
     */
    public PSObjectFile toFile() {
        return this;
    }
    
    /**
     * Reads characters from this object, interpreting them as PostScript
     * code, until it has scanned and constructed an entire object.
     * @throws net.sf.eps2pgf.postscript.errors.PSError Unable to read a token from this object
     * @return List with one or more objects. See PostScript manual under the
     * 'token' operator for more info.
     */
    public List<PSObject> token() throws PSError {
        PSObject any;
        try {
            any = Parser.convertSingle(rdr);
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
     * Returns the type of this object
     * @return Type of this object (see PostScript manual for possible values)
     */
    public String type() {
        return "filetype";
    }

}
