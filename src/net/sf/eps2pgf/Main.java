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

package net.sf.eps2pgf;

import java.io.IOException;
import java.text.ParseException;
import java.util.Iterator;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.martiansoftware.jsap.JSAPResult;

import net.sf.eps2pgf.ps.errors.PSError;
import net.sf.eps2pgf.ps.errors.QuitOpExecuted;
import net.sf.eps2pgf.util.Eps2pgfFormatter;
import net.sf.eps2pgf.util.Eps2pgfHandler;

/**
 * Main class of Eps2pgf program.
 * 
 * @author Paul Wagenaars
 */
public final class Main {
    
    /** Application name. */
    public static final String APP_NAME = "Eps2pgf";
    
    /** Application version. */
    public static final String APP_VERSION = "@VERSION@";
    
    /** Date application was build. */
    public static final String APP_BUILD_DATE = "@BUILDDATE@";
    
    /** Handles program options. */
    private static Options opts = new Options();
    
    /** The log. */
    private static final Logger LOG
                                  = Logger.getLogger("net.sourceforge.eps2pgf");
    
    
    /**
     * "Hidden" constructor.
     */
    private Main() {
        /* empty block */
    }
    
    /**
     * Main program method.
     * 
     * @param args the command line arguments
     */
    public static void main(final String[] args) {
        // Remove the ConsoleHandler from the root logger
        Logger rootLogger = LOG.getParent();
        Handler[] rootHandlers = rootLogger.getHandlers();
        rootLogger.removeHandler(rootHandlers[0]);
        
        Handler handler = new Eps2pgfHandler();
        handler.setFormatter(new Eps2pgfFormatter());
        LOG.addHandler(handler);
        
        JSAPResult parseResult = opts.parse(args);
        
        if (opts.isHelpFlagSet()) {
            printVersionCopyright();
            printHelp();
            System.exit(0);
        }
        
        if (opts.isVersionFlagSet()) {
            printVersionCopyright();
            System.exit(0);
        }
        
        if (!parseResult.success()) {
            System.err.println();

            for (Iterator< ? > errs = parseResult.getErrorMessageIterator();
                    errs.hasNext();) {
                System.err.println("Error: " + errs.next());
            }
            
            System.exit(1);
        }
        
        String debug = System.getenv("EPS2PGF_DEBUG");
        if (debug != null) {
            if (debug.equals("fine")) {
                LOG.setLevel(Level.FINE);
            } else if (debug.equals("finer")) {
                LOG.setLevel(Level.FINER);
            } else {
                LOG.setLevel(Level.ALL);
            }
        } else {
            if (opts.isVerboseFlagSet()) {
                LOG.setLevel(Level.INFO);
            } else {
                LOG.setLevel(Level.WARNING);
            }
        }
        
        Converter cnv = new Converter(opts);
        try {
            cnv.convert();
        } catch (IOException e) {
            LOG.severe("Execution failed due to an error while reading "
                    + "from or writing to a file.");
        } catch (ParseException e) {
            LOG.severe("Execution failed due to an error while parsing the text"
                    + " replacements file.");
        } catch (QuitOpExecuted e) {
            /* empty block */
        } catch (PSError e) {
            LOG.severe("Execution failed due to a PostScript error in the input"
                    + " file.");
        } catch (ProgramError e) {
            LOG.severe("Execution failed due to an error in the program. Please"
                    + " report this to the author.");
        }
    }
    
    /**
     * Creates a string with the program name and version.
     * 
     * @return Application name followed by the version number
     */
    public static String getNameVersion() {
        return APP_NAME + " " + APP_VERSION + " (build on " + APP_BUILD_DATE
                + ")";
    }
    
    /**
     * Print version and copyright information.
     */
    public static void printVersionCopyright() {
        System.out.println(getNameVersion());
        System.out.println("");
        System.out.println("Copyright 2007-2008 Paul Wagenaars"
                + " <paul@wagenaars.org>");
        System.out.println("");
        System.out.println("Licensed under the Apache License, Version 2.0 (the"
                + " \"License\");");
        System.out.println("you may not use this file except in compliance with"
                + " the License.");
        System.out.println("You may obtain a copy of the License at");
        System.out.println("");
        System.out.println("    http://www.apache.org/licenses/LICENSE-2.0");
        System.out.println("");
        System.out.println("Unless required by applicable law or agreed to in "
                + "writing, software");
        System.out.println("distributed under the License is distributed on an "
                + "\"AS IS\" BASIS,");
        System.out.println("WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,"
                + " either express or implied.");
        System.out.println("See the License for the specific language governing"
                + " permissions and");
        System.out.println("limitations under the License.");
        System.out.println("");
        System.out.println("See NOTICE.txt for more information and LICENSE.txt"
                + " for the complete license.");
        System.out.println("");
    }

    /**
     * Prints information on program usage.
     */
    public static void printHelp() {
        System.out.println();
        System.out.println("Usage: " + APP_NAME.toLowerCase() + " "
                           + opts.getUsage());
        System.out.println();
        System.out.println(opts.getHelp());        
    }
}
