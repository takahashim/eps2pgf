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
import net.sf.eps2pgf.ps.errors.PSErrorUndefined;
import net.sf.eps2pgf.ps.errors.PSErrorUnregistered;
import net.sf.eps2pgf.ps.objects.PSObject;
import net.sf.eps2pgf.ps.objects.PSObjectBool;
import net.sf.eps2pgf.ps.objects.PSObjectDict;
import net.sf.eps2pgf.ps.objects.PSObjectInt;
import net.sf.eps2pgf.ps.objects.PSObjectName;
import net.sf.eps2pgf.ps.objects.PSObjectNull;
import net.sf.eps2pgf.ps.objects.PSObjectOperator;
import net.sf.eps2pgf.ps.objects.PSObjectReal;
import net.sf.eps2pgf.util.ArrayStack;


/**
 * Contains the operator functions for operators starting with letter J to L.
 */
public final class OperatorsJtoL extends OperatorContainer {
    
    /**
     * Create a new set of operators and add them to the system dictionary of
     * the interpreter.
     * 
     * @param interpreter The interpreter
     * 
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    public OperatorsJtoL(final Interpreter interpreter) throws ProgramError {
        super(interpreter);
    }
    
    /**
     * PostScript op: known.
     */
    public class Oknown extends PSObjectOperator {
        /**
         * Invokes this operator.
         * 
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            PSObject key = getOpStack().pop();
            PSObjectDict dict = getOpStack().pop().toDict();
            dict.checkAccess(false, true, false);
            
            getOpStack().push(new PSObjectBool(dict.known(key)));
        }
    }
    
    /**
     * PostScript op: kshow.
     */
    public class Okshow extends PSObjectOperator {
        /**
         * Invokes this operator.
         * 
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            throw new PSErrorUnregistered("kshow operator");
        }
    }
    
    /**
     * PostScript op: le.
     */
    public class Ole extends PSObjectOperator {
        /**
         * Invokes this operator.
         * 
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            executeOperator("gt");
            executeOperator("not");
        }
    }
    
    /**
     * PostScript op: length.
     */
    public class Olength extends PSObjectOperator {
        /**
         * Invokes this operator.
         * 
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            PSObject obj = getOpStack().pop();
            obj.checkAccess(false, true, false);
            
            getOpStack().push(new PSObjectInt(obj.length()));
        }
    }
    
    /**
     * PostScript op: lineto.
     */
    public class Olineto extends PSObjectOperator {
        /**
         * Invokes this operator.
         * 
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            double y = getOpStack().pop().toReal();
            double x = getOpStack().pop().toReal();
            getGstate().current().lineto(x, y);
        }
    }
    
    /**
     * PostScript op: ln.
     */
    public class Oln extends PSObjectOperator {
        /**
         * Invokes this operator.
         * 
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            double num = getOpStack().pop().toReal();
            double result = Math.log(num);
            getOpStack().push(new PSObjectReal(result));
        }
    }
    
    /**
     * PostScript op: load.
     */
    public class Oload extends PSObjectOperator {
        /**
         * Invokes this operator.
         * 
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            PSObject key = getOpStack().pop();
            PSObjectDict definedInDict = getDictStack().where(key);
            if (definedInDict == null) {
                throw new PSErrorUndefined("--load-- (" + key.isis() + ")");
            }
            definedInDict.checkAccess(false, true, false);
            
            PSObject value = getDictStack().lookup(key);
            if (value == null) {
                getOpStack().push(new PSObjectName("/" + key));
                throw new PSErrorUndefined();
            }
            getOpStack().push(value);
        }
    }
    
    /**
     * PostScript op: log.
     */
    public class Olog extends PSObjectOperator {
        /**
         * Invokes this operator.
         * 
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            double num = getOpStack().pop().toReal();
            double result = Math.log10(num);
            getOpStack().push(new PSObjectReal(result));
        }
    }
    
    /**
     * PostScript op: loop.
     */
    public class Oloop extends PSObjectOperator {
        /**
         * Invokes this operator.
         * 
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            ExecStack es = getExecStack();
            ArrayStack<PSObject> cs = getContStack();
            
            es.push(getDictStack().eps2pgfLoop);
            
            cs.push(new PSObjectNull());
            cs.push(getOpStack().pop());
        }
    }
    
    /**
     * PostScript op: lt.
     */
    public class Olt extends PSObjectOperator {
        /**
         * Invokes this operator.
         * 
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            executeOperator("ge");
            executeOperator("not");
        }
    }
   
}
