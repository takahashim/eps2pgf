/*
 * Converter.java
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

package net.sf.eps2pgf;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.text.ParseException;

import net.sf.eps2pgf.io.LimitedSectionInputStream;
import net.sf.eps2pgf.io.TextReplacements;
import net.sf.eps2pgf.ps.DSCHeader;
import net.sf.eps2pgf.ps.Header;
import net.sf.eps2pgf.ps.Interpreter;
import net.sf.eps2pgf.ps.errors.PSError;
import net.sf.eps2pgf.ps.objects.PSObjectFile;

/**
 * Object that converts Encapsulated PostScript (EPS) to Portable Graphics
 * Format (PGF).
 *
 * @author Paul Wagenaars
 */
public class Converter {
    
    /** Options describing behavior of program. */
    private Options opts;
    
    /**
     * Creates a new instance of Converter.
     * 
     * @param pOpts command line options passed to the program
     */
    public Converter(final Options pOpts) {
        opts = pOpts;
    }
    
    /**
     * Starts the actual conversion.
     * 
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws PSError A PostScript error occurred.
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     * @throws ParseException There was an error parsing a file.
     */
    public final void convert() throws IOException, PSError, ProgramError,
            ParseException {
        // Check for a binary header
        int[] dim = Header.getPostScriptSection(opts.getInputFile());
        
        // Open the file for reading the postscript code
        InputStream in = new BufferedInputStream(
                new FileInputStream(opts.getInputFile()));
        
        // If it has a binary header, read only the postscript code and skip
        // binary data.
        if (dim != null) {
            in = new LimitedSectionInputStream(in, dim[0], dim[1]);
        }
        
        // Read info from the DSC header
        DSCHeader header = new DSCHeader(in);
        
        // Read text replacements file
        TextReplacements textReplace = null;
        if (opts.getTextreplacefile() != null) {
            textReplace = new TextReplacements(opts.getTextreplacefile());
        }
        
        Writer out = new BufferedWriter(
                new FileWriter(opts.getOutputFile()));
        
        // Create PostScript interpreter and add file to execution stack
        Interpreter interp = new Interpreter(out, opts, header, textReplace);
        interp.getExecStack().push(new PSObjectFile(in));
        
        // Run the interpreter
        try {
            interp.start();
        } catch (PSError e) {
            in.close();
            out.close();
            throw e;
        } catch (ProgramError e) {
            in.close();
            out.close();
            throw e;
        } catch (IOException e) {
            in.close();
            out.close();
            throw e;
        }

        in.close();
        out.close();
    }

    /**
     * Sets the options used for the conversion process.
     * 
     * @param pOpts The options to set
     */
    public void setOpts(final Options pOpts) {
        opts = pOpts;
    }

    /**
     * Gets the current options.
     * 
     * @return the opts
     */
    public Options getOpts() {
        return opts;
    }
    
}
