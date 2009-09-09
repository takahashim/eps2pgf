/*
 * This file is part of Eps2pgf.
 *
 * Copyright 2007-2009 Paul Wagenaars
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

package net.sf.eps2pgf.testsuite.pstests;

import net.sf.eps2pgf.ProgramError;
import net.sf.eps2pgf.io.StringInputStream;
import net.sf.eps2pgf.ps.Interpreter;
import net.sf.eps2pgf.ps.objects.PSObject;
import net.sf.eps2pgf.ps.objects.PSObjectArray;
import net.sf.eps2pgf.ps.objects.PSObjectBool;
import net.sf.eps2pgf.ps.objects.PSObjectDict;
import net.sf.eps2pgf.ps.objects.PSObjectFile;
import net.sf.eps2pgf.util.ArrayStack;

/**
 * Some utility methods to run the tests.
 */
public final class Common {
    
    /**
     * "Hidden" constructor.
     */
    private Common() {
        /* empty block */
    }
    
    /**
     * Test some PostScript commands.
     * 
     * @param postscriptCommands The postscript commands.
     * @param interp The interpreter in which the commands are tested.
     * @param nrStackObj The number of object that should be on the operand
     * stack after running.
     * 
     * @return True, if all booleans where true. False, if there were no
     * booleans, or if at least one of the booleans was false.
     * 
     * @throws Exception the exception
     */
    public static boolean testString(final Interpreter interp,
            final String postscriptCommands, final int nrStackObj)
            throws Exception {
        
        PSObjectFile cmds =
            new PSObjectFile(new StringInputStream(postscriptCommands), interp);

        PSObjectArray he = new PSObjectArray("{errordict /handleerror"
                + " {/errorhandled true def eps2pgfhandleerror} put}", interp);
        
        interp.getExecStack().push(cmds);
        interp.getExecStack().push(he);
        interp.start();
        
        // Check whether an error was handled by the interpreter
        PSObject err = interp.getDictStack().lookup("errorhandled");
        if ((err != null) && err.toBool()) {
            PSObjectDict de = interp.getDictStack().lookup("$error").toDict();
            throw new ProgramError(de.lookup("errorname").isis() + " in "
                    + de.lookup("command").isis());
        }

        // Check for number of item on operand stack
        if (nrStackObj != interp.getOpStack().size()) {
            return false;
        }
        
        // Check all booleans on the stack
        boolean boolFound = false;
        ArrayStack<PSObject> opStack = interp.getOpStack();
        while (!opStack.empty()) {
            PSObject obj = opStack.pop();
            if (obj instanceof PSObjectBool) {
                boolFound = true;
                if (!((PSObjectBool) obj).toBool()) {
                    return false;
                }
            } else {
                // All objects must be booleans
                return false;
            }
        }
        // If we found a boolean, it must have been true.
        return boolFound;
    }
}
