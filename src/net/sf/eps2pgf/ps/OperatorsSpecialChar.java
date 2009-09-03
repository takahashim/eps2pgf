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

package net.sf.eps2pgf.ps;

import net.sf.eps2pgf.ProgramError;
import net.sf.eps2pgf.ps.errors.PSError;
import net.sf.eps2pgf.ps.errors.PSErrorRangeCheck;
import net.sf.eps2pgf.ps.objects.PSObject;
import net.sf.eps2pgf.ps.objects.PSObjectArray;
import net.sf.eps2pgf.ps.objects.PSObjectDict;
import net.sf.eps2pgf.ps.objects.PSObjectMark;
import net.sf.eps2pgf.ps.objects.PSObjectOperator;
import net.sf.eps2pgf.util.ArrayStack;


/**
 * Contains the operator functions for operators starting with letter A to C.
 */
public final class OperatorsSpecialChar extends OperatorContainer {
    
    /**
     * Create a new set of operators and add them to the system dictionary of
     * the interpreter.
     * 
     * @param interpreter The interpreter
     * 
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    public OperatorsSpecialChar(final Interpreter interpreter)
            throws ProgramError {
        
        super(interpreter);
    }
    
    /**
     * PostScript op: >>.
     * 
     * @throws PSError A PostScript error occurred.
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    public class OdblGreaterBrackets extends PSObjectOperator {
        /**
         * Invokes this operator.
         * 
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            ArrayStack<PSObject> os = getOpStack();
            PSObjectDict dict = new PSObjectDict(getVm());
            while (true) {
                PSObject value = os.pop();
                if (value instanceof PSObjectMark) {
                    break;
                }
                PSObject key = os.pop();
                if (value instanceof PSObjectMark) {
                    throw new PSErrorRangeCheck();
                }
                dict.setKey(key, value);
            }
            os.push(dict);
        }
        
        /**
         * Gets the name of this operator.
         * 
         * @return The name of this operator.
         */
        @Override
        public String getName() {
            return ">>";
        }
    }
    
    /**
     * PostScript op: <<.
     * 
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     * @throws PSError A PostScript error occurred.
     */
    public class OdblLessBrackets extends PSObjectOperator {
        /**
         * Invokes this operator.
         * 
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            getInterp().executeOperator("mark");
        }
        
        /**
         * Gets the name of this operator.
         * 
         * @return The name of this operator.
         */
        @Override
        public String getName() {
            return "<<";
        }
    }
    
    /**
     * PostScript op: ==.
     */
    public class Oisis extends PSObjectOperator {
        /**
         * Invokes this operator.
         * 
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            PSObject obj = getOpStack().pop();
            System.out.println(obj.isis());
        }
        
        /**
         * Gets the name of this operator.
         * 
         * @return The name of this operator.
         */
        @Override
        public String getName() {
            return "==";
        }
    }
    
    /**
     * PostScript op: [.
     */
    public class OsqBrackLeft extends PSObjectOperator {
        /**
         * Invokes this operator.
         * 
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            getOpStack().push(new PSObjectMark());
        }
        
        /**
         * Gets the name of this operator.
         * 
         * @return The name of this operator.
         */
        @Override
        public String getName() {
            return "[";
        }
    }
    
    /**
     * PostScript op: ].
     */
    public class OsqBrackRight extends PSObjectOperator {
        /**
         * Invokes this operator.
         * 
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            executeOperator("counttomark");
            int n = getOpStack().pop().toInt();
            PSObject[] objs = new PSObject[n];
            for (int i = n - 1; i >= 0; i--) {
                objs[i] = getOpStack().pop();
            }
            getOpStack().pop();  // clear mark
            getOpStack().push(new PSObjectArray(objs, getVm()));
        }
        
        /**
         * Gets the name of this operator.
         * 
         * @return The name of this operator.
         */
        @Override
        public String getName() {
            return "]";
        }
    }
    
}
