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
import com.martiansoftware.jsap.StringParser;
import com.martiansoftware.jsap.Switch;
import com.martiansoftware.jsap.UnflaggedOption;
import com.martiansoftware.jsap.stringparsers.EnumeratedStringParser;

/**
 * Parses command line arguments and manages settings and options by the user.
 *
 * @author Paul Wagenaars
 */
public class Options extends JSAP {
    
    /** Result of parsed command line arguments. */
    private JSAPResult args;
    
    /** The input file. */
    private String input;
    
    /** The output file. */
    private String output;
    
    /** The type of output. */
    private String outputtype;
    
    /** The PSfrag compatible file describing text replacements. */
    private String textreplacefile;
    
    /**
     * Creates a new instance of Options.
     */
    public Options() {
        super();
        
        try {
            UnflaggedOption opt2 = new UnflaggedOption("inputfilename")
                                          .setStringParser(JSAP.STRING_PARSER)
                                          .setRequired(true);
            opt2.setHelp("(Encapsulated) PostScript (EPS or PS) input file.");
            registerParameter(opt2);
            
            Switch sw = new Switch("verbose")
                        .setLongFlag("verbose");
            sw.setHelp("Display more information during the conversion.");
            registerParameter(sw);
            
            FlaggedOption opt1 = new FlaggedOption("outputfilename")
                                       .setShortFlag('o')
                                       .setLongFlag("output")
                                       .setStringParser(JSAP.STRING_PARSER)
                                       .setDefault("<input file with .pgf"
                                               + " extension>")
                                       .setRequired(true);
            opt1.setHelp("Write output to this file.");
            registerParameter(opt1);
            
            StringParser outputtypeParser =
                EnumeratedStringParser.getParser("pgf; lol", false, false); 
            FlaggedOption opt3 = new FlaggedOption("outputtype")
            						   .setShortFlag('t')
            						   .setLongFlag("output-type")
            						   .setStringParser(outputtypeParser)
            						   .setDefault("pgf");
            opt3.setHelp("Type of output file. Accepted values: 'pgf' or"
                    + " 'lol'.");
            registerParameter(opt3);
            
            FlaggedOption opt4 = new FlaggedOption("textreplacefile")
            							.setLongFlag("text-replace")
            							.setStringParser(JSAP.STRING_PARSER)
            							.setRequired(false);
            opt4.setHelp("File containing PSfrag commands describing text "
                    + "replacements.");
            registerParameter(opt4);
            
            sw = new Switch("version")
                                .setLongFlag("version");
            sw.setHelp("Display version information.");
            registerParameter(sw);
            
            sw = new Switch("help")
                        .setLongFlag("help")
                        .setShortFlag('h');
            sw.setHelp("Display program usage.");
            registerParameter(sw);
            
        } catch (JSAPException e) {
            // Since all above code is fixed, without variables, this
            // should never happen.
        }
    }
    
    /**
     * Parse a set of command-line arguments.
     * 
     * @param arguments Command line arguments.
     * 
     * @return Processed arguments
     */
    public JSAPResult parse(final String[] arguments) {
        setArgs(super.parse(arguments));
        
        postParse();
        
        return getArgs();
    }
    
    /**
     * Post-process parsing results. Copy settings to this object, check
     * validity, etc...
     */
    private void postParse() {
        setInput(getArgs().getString("inputfilename", ""));
        setOutputtype(getArgs().getString("outputtype", "pgf"));
        
        if (getArgs().getString("outputfilename").startsWith("<")) {
            String lowerInput = getInput().toLowerCase();
            if (lowerInput.endsWith(".eps")) {
            	setOutput(getInput().substring(0, getInput().length() - 4) + "."
            	        + getOutputtype());
            } else if (lowerInput.endsWith(".ps")) {
            	setOutput(getInput().substring(0, getInput().length() - 3) + "."
            	        + getOutputtype());
            } else {
            	setOutput(getInput() + "." + getOutputtype());
            }
        } else {
            // An output filename was specified on the command-line
            setOutput(getArgs().getString("outputfilename"));
        }
        
        setTextreplacefile(getArgs().getString("textreplacefile", ""));
        
    }

    /**
     * @param pTextreplacefile the textreplacefile to set
     */
    public void setTextreplacefile(final String pTextreplacefile) {
        textreplacefile = pTextreplacefile;
    }

    /**
     * @return the textreplacefile
     */
    public String getTextreplacefile() {
        return textreplacefile;
    }

    /**
     * @param pOutputtype the outputtype to set
     */
    public void setOutputtype(final String pOutputtype) {
        outputtype = pOutputtype;
    }

    /**
     * @return the outputtype
     */
    public String getOutputtype() {
        return outputtype;
    }

    /**
     * @param pOutput the output to set
     */
    void setOutput(final String pOutput) {
        this.output = pOutput;
    }

    /**
     * @return the output
     */
    String getOutput() {
        return output;
    }

    /**
     * @param pInput the input to set
     */
    void setInput(final String pInput) {
        this.input = pInput;
    }

    /**
     * @return the input
     */
    String getInput() {
        return input;
    }

    /**
     * @param pArgs the args to set
     */
    void setArgs(final JSAPResult pArgs) {
        this.args = pArgs;
    }

    /**
     * @return the args
     */
    JSAPResult getArgs() {
        return args;
    }
    
}
