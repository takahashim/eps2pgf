/*
 * Options.java
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

import java.io.File;

import com.martiansoftware.jsap.FlaggedOption;
import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPException;
import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.StringParser;
import com.martiansoftware.jsap.Switch;
import com.martiansoftware.jsap.UnflaggedOption;
import com.martiansoftware.jsap.stringparsers.EnumeratedStringParser;
import com.martiansoftware.jsap.stringparsers.FileStringParser;


/**
 * Parses command line arguments and manages settings and options by the user.
 *
 * @author Paul Wagenaars
 */
public class Options extends JSAP {
    
    /** The input file. */
    private File inputFile;
    
    /** The output file. */
    private File outputFile;
    
    /** The Enum for different text modes. */
    public enum TextMode { EXACT, DIRECT_COPY };
    
    /** Text handling: exact or directcopy. */
    private TextMode textMode;
    
    /** The enum for different output types. */
    public enum OutputType { PGF, LOL };
    
    /** The type of output. */
    private OutputType outputType;
    
    /** The PSfrag compatible file describing text replacements. */
    private File textreplacefile;
    
    /** Indicates whether version flag is set. */
    private boolean versionFlagSet;
    
    /** Indicates whether help flag is set. */
    private boolean helpFlagSet;
    
    /** Indicates whether verbose flag is set. */
    private boolean verboseFlagSet;
    
    /**
     * Creates a new instance of Options.
     */
    public Options() {
        super();
        
        try {
            FileStringParser fileParser = FileStringParser.getParser()
                                            .setMustExist(true)
                                            .setMustBeFile(true);
            UnflaggedOption optInput = new UnflaggedOption("inputfile")
                                          .setStringParser(fileParser)
                                          .setRequired(true);
            optInput.setHelp("(Encapsulated) PostScript (EPS or PS) input "
                    + "file.");
            registerParameter(optInput);
            
            FlaggedOption optOutput = new FlaggedOption("outputfilename")
                                       .setShortFlag('o')
                                       .setLongFlag("output")
                                       .setStringParser(JSAP.STRING_PARSER)
                                       .setDefault("<input file with .pgf"
                                               + " extension>")
                                       .setRequired(true);
            optOutput.setHelp("Write output to this file.");
            registerParameter(optOutput);
            
            StringParser textmodeParser = EnumeratedStringParser
                    .getParser("exact; directcopy", false, false);
            FlaggedOption optTextmode = new FlaggedOption("textmode")
                                        .setShortFlag('m')
                                        .setLongFlag("text-mode")
                                        .setStringParser(textmodeParser)
                                        .setDefault("exact");
            optTextmode.setHelp("Text label handling. Accepted values: 'exact' "
                    + "(text is reproduced as closely as possible), or "
                    + "'directcopy' (text is directly copied to the output and "
                    + "scanned for embedded PSfrag text replacement rules).");
            registerParameter(optTextmode);
            
            FlaggedOption optPsfrag = new FlaggedOption("textreplacefile")
                                  .setLongFlag("text-replace")
                                  .setStringParser(fileParser)
                                  .setRequired(false);
            optPsfrag.setHelp("File containing PSfrag commands describing text "
                    + "replacements.");
            registerParameter(optPsfrag);

            StringParser outputtypeParser =
                EnumeratedStringParser.getParser("pgf; lol", false, false); 
            FlaggedOption optOutputType = new FlaggedOption("outputtype")
                                       .setShortFlag('t')
                                       .setLongFlag("output-type")
                                       .setStringParser(outputtypeParser)
                                       .setDefault("pgf");
            optOutputType.setHelp("Type of output file. Accepted values: 'pgf' "
                    + "or 'lol'.");
            registerParameter(optOutputType);
            
            Switch sw = new Switch("verbose").setLongFlag("verbose");
            sw.setHelp("Display more information during the conversion.");
            registerParameter(sw);
            
            sw = new Switch("version").setLongFlag("version");
            sw.setHelp("Display version information.");
            registerParameter(sw);
            
            sw = new Switch("help").setLongFlag("help").setShortFlag('h');
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
        JSAPResult args = super.parse(arguments);
        
        postParse(args);
        
        return args;
    }
    
    /**
     * Post-process parsing results. Copy settings to this object, check
     * validity, etc...
     * 
     * @param args Parsed command line arguments.
     */
    private void postParse(final JSAPResult args) {
        setInputFile(args.getFile("inputfile"));
        
        if (args.getString("outputtype").equals("pgf")) {
            setOutputType(OutputType.PGF);
        } else {
            setOutputType(OutputType.LOL);
        }
        
        if (args.getString("textmode", "exact").equals("exact")) {
            setTextmode(TextMode.EXACT);
        } else {
            setTextmode(TextMode.DIRECT_COPY);
        }
        
        String outputPath;
        if (args.getString("outputfilename").startsWith("<")) {
            String inputPath = getInputFile().getPath();
            if (inputPath.toLowerCase().endsWith(".eps")) {
                outputPath = inputPath.substring(0, inputPath.length() - 4)
                               + "." + getOutputType().toString().toLowerCase();
            } else if (inputPath.toLowerCase().endsWith(".ps")) {
                outputPath = inputPath.substring(0, inputPath.length() - 3)
                               + "." + getOutputType().toString().toLowerCase();
            } else {
                outputPath = inputPath + "."
                        + getOutputType().toString().toLowerCase();
            }
        } else {
            // An output filename was specified on the command-line
            outputPath = args.getString("outputfilename");
        }
        setOutputFile(new File(outputPath));
        
        setTextreplacefile(args.getFile("textreplacefile"));
        
        setHelpFlag(args.getBoolean("help"));
        
        setVersionFlag(args.getBoolean("version"));
        
        setVerboseFlag(args.getBoolean("verbose"));
    }

    /**
     * @param pTextreplacefile the textreplacefile to set
     */
    public void setTextreplacefile(final File pTextreplacefile) {
        textreplacefile = pTextreplacefile;
    }

    /**
     * @return the textreplacefile
     */
    public File getTextreplacefile() {
        return textreplacefile;
    }

    /**
     * @param pOutputtype the outputtype to set
     */
    public void setOutputType(final OutputType pOutputtype) {
        outputType = pOutputtype;
    }

    /**
     * @return the outputtype
     */
    public OutputType getOutputType() {
        return outputType;
    }

    /**
     * @param pOutput the output to set
     */
    public void setOutputFile(final File pOutput) {
        if (pOutput != null) {
            outputFile = pOutput;
        } else {
            outputFile = new File("");
        }
    }

    /**
     * @return the output
     */
    public File getOutputFile() {
        return outputFile;
    }

    /**
     * @param pInput the input to set
     */
    public void setInputFile(final File pInput) {
        if (pInput != null) {
            inputFile = pInput;
        } else {
            inputFile = new File("");
        }
    }

    /**
     * @return the input
     */
    public File getInputFile() {
        return inputFile;
    }

    /**
     * @param pTextmode the textmode to set
     */
    public void setTextmode(final TextMode pTextmode) {
        textMode = pTextmode;
    }

    /**
     * @return the textmode
     */
    public TextMode getTextmode() {
        return textMode;
    }

    /**
     * @param pVersionFlag the versionFlag to set
     */
    public void setVersionFlag(final boolean pVersionFlag) {
        versionFlagSet = pVersionFlag;
    }

    /**
     * @return the versionFlag
     */
    public boolean isVersionFlagSet() {
        return versionFlagSet;
    }

    /**
     * @param pHelpFlag the helpFlag to set
     */
    public void setHelpFlag(final boolean pHelpFlag) {
        helpFlagSet = pHelpFlag;
    }

    /**
     * @return the helpFlag
     */
    public boolean isHelpFlagSet() {
        return helpFlagSet;
    }

    /**
     * @param pVerboseFlagSet the verboseFlagSet to set
     */
    public void setVerboseFlag(final boolean pVerboseFlagSet) {
        verboseFlagSet = pVerboseFlagSet;
    }

    /**
     * @return the verboseFlagSet
     */
    public boolean isVerboseFlagSet() {
        return verboseFlagSet;
    }
    
}
