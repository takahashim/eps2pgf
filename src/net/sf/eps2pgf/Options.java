/*
 * Options.java
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

package net.sf.eps2pgf;

import com.martiansoftware.jsap.FlaggedOption;
import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPException;
import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.Switch;
import com.martiansoftware.jsap.UnflaggedOption;
import com.martiansoftware.jsap.StringParser;
import com.martiansoftware.jsap.stringparsers.EnumeratedStringParser;

/**
 * Parses command line arguments and manages settings and options by the user
 *
 * @author Paul Wagenaars
 */
public class Options extends JSAP {
    JSAPResult args;
    
    String input;
    String output;
    String outputtype;
    
    /**
     * Creates a new instance of Options
     */
    public Options() {
        super();
        
        try {
            UnflaggedOption opt2 = new UnflaggedOption("inputfilename")
                                          .setStringParser(JSAP.STRING_PARSER)
                                          .setRequired(true);
            opt2.setHelp("(Encapsulated) PostScript (EPS or PS) input file");
            registerParameter(opt2);
            
            Switch sw = new Switch("verbose")
                        .setLongFlag("verbose");
            sw.setHelp("Display more information during the conversion process");
            registerParameter(sw);
            
            FlaggedOption opt1 = new FlaggedOption("outputfilename")
                                       .setShortFlag('o')
                                       .setLongFlag("output")
                                       .setStringParser(JSAP.STRING_PARSER)
                                       .setDefault("<input file with .pgf extension>")
                                       .setRequired(true);
            opt1.setHelp("Write output to this file");
            registerParameter(opt1);
            
            StringParser outputtypeParser = EnumeratedStringParser.getParser("pgf; lol",
            		false, false); 
            FlaggedOption opt3 = new FlaggedOption("outputtype")
            						   .setShortFlag('t')
            						   .setLongFlag("output-type")
            						   .setStringParser(outputtypeParser)
            						   .setDefault("pgf");
            opt3.setHelp("Type of output file. Accepted values: 'pgf' or 'lol'.");
            registerParameter(opt3);
            
            sw = new Switch("version")
                                .setLongFlag("version");
            sw.setHelp("Display version information");
            registerParameter(sw);
            
            sw = new Switch("help")
                        .setLongFlag("help")
                        .setShortFlag('h');
            sw.setHelp("Display program usage");
            registerParameter(sw);
            
        } catch (JSAPException e) {
            // Since all above code is fixed, without variables, this
            // should never happen.
        }
    }
    
    /**
     * Parse a set of command-line arguments
     */
    public JSAPResult parse(String[] arguments) {
        args = super.parse(arguments);
        
        postParse();
        
        return args;
    }
    
    /**
     * Post-process parsing results. Copy settings to this object, check
     * validity, etc...
     */
    private void postParse() {
        input = args.getString("inputfilename", "");
        outputtype = args.getString("outputtype", "pgf");
        
        if (args.getString("outputfilename").startsWith("<")) {
            String lowerInput = input.toLowerCase();
            if (lowerInput.endsWith(".eps")) {
            	output = input.substring(0, input.length()-4) + "." + outputtype;
            } else if (lowerInput.endsWith(".ps")) {
            	output = input.substring(0, input.length()-3) + "." + outputtype;
            } else {
            	output = input + "." + outputtype;
            }
        } else {
            // An output filename was specified on the command-line
            output = args.getString("outputfilename");
        }
        
        
    }
    
}
