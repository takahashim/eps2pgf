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

import java.util.logging.Logger;

import net.sf.eps2pgf.ProgramError;
import net.sf.eps2pgf.ps.errors.PSError;
import net.sf.eps2pgf.ps.objects.PSObject;
import net.sf.eps2pgf.ps.objects.PSObjectArray;
import net.sf.eps2pgf.ps.objects.PSObjectBool;
import net.sf.eps2pgf.ps.objects.PSObjectDict;
import net.sf.eps2pgf.ps.objects.PSObjectInt;
import net.sf.eps2pgf.ps.objects.PSObjectName;
import net.sf.eps2pgf.ps.objects.PSObjectNull;
import net.sf.eps2pgf.ps.objects.PSObjectOperator;
import net.sf.eps2pgf.ps.objects.PSObjectReal;
import net.sf.eps2pgf.util.ArrayStack;

/**
 * Contain the operator function for operators specific to Eps2pgf.
 */
public final class OperatorsEps2pgf extends OperatorContainer {
    
    /** Log information. */
    private final Logger log = Logger.getLogger("net.sourceforge.eps2pgf");

    /**
     * Create a new instance with Eps2pgf operators.
     * 
     * @param interpreter The interpreter
     * 
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    protected OperatorsEps2pgf(final Interpreter interpreter)
            throws ProgramError {
        
        super(interpreter);
        
        //TODO register some functions as quick access functions
//        private void defineQuickAccessConstants() throws ProgramError {
//            try {
//                // Looping context procedures
//                eps2pgfCshow = lookup("eps2pgfcshow").toOperator();
//                eps2pgfFilenameforall =
//                    lookup("eps2pgffilenameforall").toOperator();
//                eps2pgfFor = lookup("eps2pgffor").toOperator();
//                eps2pgfForall = lookup("eps2pgfforall").toOperator();
//                eps2pgfKshow = lookup("eps2pgfkshow").toOperator();
//                eps2pgfLoop = lookup("eps2pgfloop").toOperator();
//                eps2pgfPathforall = lookup("eps2pgfpathforall").toOperator();
//                eps2pgfRepeat = lookup("eps2pgfrepeat").toOperator();
//                eps2pgfResourceforall =
//                    lookup("eps2pgfresourceforall").toOperator();
//
//                // Other continuation functions
//                eps2pgfEexec = lookup("eps2pgfeexec").toOperator();
//                eps2pgfStopped = lookup("eps2pgfstopped").toOperator();
//            } catch (PSErrorTypeCheck e) {
//                throw new ProgramError("Object in dictstack has incorrect"
//                        + " type for quick access constants.");
//            }
//        }

    }
    
    /**
     * Internal Eps2pgf operator. Continuation function for 'for' operator.
     * Input arguments: null current increment limit proc
     * Note: right is top of stack
     * 
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    public class Oeps2pgffor extends PSObjectOperator {
        /**
         * Invokes this operator.
         *
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            ArrayStack<PSObject> cs = getContStack();
            ArrayStack<PSObject> os = getOpStack();
            ExecStack es = getExecStack();
            try {
                // Pop input arguments from continuation stack
                PSObject proc = cs.pop();
                PSObject objLimit = cs.pop();
                double limit = objLimit.toReal();
                PSObject objIncr = cs.pop();
                double incr = objIncr.toReal();
                double current = cs.pop().toReal();
                cs.pop().toNull();
                
                // Check whether limit, incr and current are all three integers
                boolean allIntegers = false;
                if ((limit == Math.round(limit)) && (incr == Math.round(incr))
                        && (current == Math.round(current))) {
                    allIntegers = true;
                }
    
                // Execute one iteration of the loop, and prepare next
                if (((incr > 0) && (current <= limit))
                    || ((incr < 0) && (current >= limit))) {
                    
                    // Push value to op stack
                    if (allIntegers) {
                        os.push(new PSObjectInt(current));
                    } else {
                        os.push(new PSObjectReal(current));
                    }
                    
                    // Push objects to execution stack
                    es.push(getDictStack().eps2pgfFor);
                    es.push(proc);
                    
                    // Push arguments to continuation stack
                    cs.push(new PSObjectNull());
                    cs.push(new PSObjectReal(current + incr));
                    cs.push(objIncr);
                    cs.push(objLimit);
                    cs.push(proc);
                }
            } catch (PSError e) {
                throw new ProgramError(e.getErrorName().isis()
                        + " in continuation function");
            }
        }
    }
    
    /**
     * Internal Eps2pgf operator. Continuation function for 'forall' operator.
     * Input arguments: null nrItemsPerLoop itemList proc
     * Note: right is top of stack
     * 
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    public class Oeps2pgfforall extends PSObjectOperator {
        /**
         * Invokes this operator.
         *
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            ArrayStack<PSObject> cs = getContStack();
            ArrayStack<PSObject> os = getOpStack();
            ExecStack es = getExecStack();
            try {
                // Get arguments from continuation stack.
                PSObject proc = cs.pop();
                PSObjectArray itemList = cs.pop().toArray();
                PSObject objNrItemsPerLoop = cs.pop();
                int nrItemsPerLoop = objNrItemsPerLoop.toInt();
                cs.pop().toNull();
                
                if (itemList.size() > 0) {
                    // Push object on operand stack
                    for (int i = 0; i < nrItemsPerLoop; i++) {
                        os.push(itemList.remove(0));
                    }
                    
                    // Push objects on execution stack
                    es.push(getDictStack().eps2pgfForall);
                    es.push(proc);
                    
                    // Push arguments on continuation stack
                    cs.push(new PSObjectNull());
                    cs.push(objNrItemsPerLoop);
                    cs.push(itemList);
                    cs.push(proc);
                }
            } catch (PSError e) {
                throw new ProgramError(e.getErrorName().isis()
                        + " in continuation function");
            }
        }
    }
    
    /**
     * Internal Eps2pgf operator: eps2pgfendofstopped. It indicates that the
     * end of a 'stopped' context has been reached.
     */
    public class Oeps2pgfstopped extends PSObjectOperator {
        /**
         * Invokes this operator.
         *
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            ArrayStack<PSObject> cs = getContStack();
            try {
                // Pop arguments from continuation stack
                cs.pop().toNull();
                
                // Get current value of 'newerror' and set it to false
                PSObjectDict dollarError =
                    getDictStack().lookup("$error").toDict();
                boolean newError = dollarError.lookup("newerror").toBool();
                dollarError.setKey("newerror", false);
                
                // Push results on operand stack
                getOpStack().push(new PSObjectBool(newError));
            } catch (PSError e) {
                throw new ProgramError("An PS error: " + e.getMessage());
            }
        }
    }
    
    /**
     * Internal Eps2pgf operator: eps2pgfgetmetrics.
     */
    public class Oeps2pgfgetmetrics extends PSObjectOperator {
        /**
         * Invokes this operator.
         *
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            double[] metrics =
                getGstate().current().getDevice().eps2pgfGetMetrics();
            PSObjectArray array = new PSObjectArray(metrics, getVm());
            getOpStack().push(array);
        }
    }
    
    /**
     * Internal Eps2pgf operator: continuation function for looping context
     * operator.
     */
    public class Oeps2pgfcshow extends PSObjectOperator {
        /**
         * Invokes this operator.
         *
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            throw new ProgramError("Continuation function not yet implemented");
        }
    }
    
    /**
     * Internal Eps2pgf operator. Continuation function for 'eexec' operator.
     */
    public class Oeps2pgfeexec extends PSObjectOperator {
        /**
         * Invokes this operator.
         *
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            ArrayStack<PSObject> cs = getContStack();
            try {
                // Pop arguments from continuation stack
                cs.pop().toNull();
                
                // Remove top systemdict from dictstack
                PSObjectDict dict = getDictStack().popDict();
                PSObjectDict sysdict =
                    getDictStack().lookup("systemdict").toDict();
                if (dict != sysdict) {
                    throw new ProgramError("Top dict inequal to systemdict.");
                }
            } catch (PSError e) {
                throw new ProgramError("An PS error: " + e.getMessage());
            }
        }
    }
    
    /**
     * Internal Eps2pgf operator: implements default error-handling procedure.
     */
    public class Oeps2pgferrorproc extends PSObjectOperator {
        /**
         * Invokes this operator.
         *
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            ArrayStack<PSObject> os = getOpStack();
            PSObjectName errName = os.pop().toName();
            PSObjectDict dollarError = getDictStack().lookup("$error").toDict();
            dollarError.setKey("newerror", true);
            dollarError.setKey("errorname", errName);
            dollarError.setKey("command", os.pop());
            dollarError.setKey("errorinfo", new PSObjectNull());
            
            boolean recordStacks = dollarError.get("recordstacks").toBool();
            if (recordStacks) {
                PSObjectArray arr = new PSObjectArray(getVm());
                for (int i = 0; i < os.size(); i++) {
                    arr.addToEnd(os.peek(os.size() - i - 1));
                }
                dollarError.setKey("ostack", arr);
                
                os.push(new PSObjectArray(getExecStack().size(), getVm()));
                getInterp().executeOperator("execstack");
                PSObjectArray estack = os.pop().toArray();
                estack = estack.getinterval(0, estack.size() - 1);
                dollarError.setKey("estack", estack);
                
                os.push(new PSObjectArray(getDictStack().countdictstack(),
                                          getVm()));
                getInterp().executeOperator("dictstack");
                dollarError.setKey("dstack", getOpStack().pop());
            }
        }
    }
    
    /**
     * Internal Eps2pgf operator: continuation function for looping context
     * operator.
     */
    public class Oeps2pgffilenameforall extends PSObjectOperator {
        /**
         * Invokes this operator.
         *
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            throw new ProgramError("Continuation function not yet implemented");
        }
    }
    
    /**
     * Internal Eps2pgf operator. Default handleerror procedure.
     */
    public class Oeps2pgfhandleerror extends PSObjectOperator {
        /**
         * Invokes this operator.
         *
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            PSObjectDict de = getDictStack().lookup("$error").toDict();
            de.setKey("newerror", false);
    
            log.severe("A PostScript error occurred.");
            log.severe("    Type: " + de.lookup("errorname").isis());
            log.severe("    While executing: " + de.lookup("command").isis());
            
            if (de.lookup("recordstacks").toBool()) {
                log.severe("    Operand stack:");
                PSObjectArray ostack = de.lookup("ostack").toArray();
                int nos = ostack.size();
                int n = Math.min(nos, 15);
                for (int i = (nos - 1); i >= (nos - n); i--) {
                    log.severe("      |- " + ostack.get(i).isis());
                }
                if (n < ostack.size()) {
                    log.severe("      (rest of stack suppressed, "
                            + (nos - n) + " items)");
                }
            } else {
                log.severe("    Record stacks disabled");
            }
            
            log.severe("Execution failed due to a PostScript error in the"
                    + " input file.");
        }
    }
    
    /**
     * Internal Eps2pgf operator: continuation function for looping context
     * operator.
     */
    public class Oeps2pgfkshow extends PSObjectOperator {
        /**
         * Invokes this operator.
         *
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            throw new ProgramError("Continuation function not yet implemented");
        }
    }
    
    /**
     * Internal Eps2pgf operator. Continuation function for 'loop' operator.
     * Input arguments: null proc
     * Note: right is top of stack
     * 
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    public class Oeps2pgfloop extends PSObjectOperator {
        /**
         * Invokes this operator.
         *
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            ArrayStack<PSObject> cs = getContStack();
            ExecStack es = getExecStack();
            try {
                // Get arguments from continuation stack.
                PSObject proc = cs.pop();
                cs.pop().toNull();
                
                es.push(getDictStack().eps2pgfLoop);
                es.push(proc);
                
                cs.push(new PSObjectNull());
                cs.push(proc);
            } catch (PSError e) {
                throw new ProgramError(e.getErrorName().isis()
                        + " in continuation function");
            }
        }
    }
    
    /**
     * Internal Eps2pgf operator: continuation function for looping context
     * operator.
     * Input arguments: null path move line curve close
     * Note: right is top of continuation stack 
     * 
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    public class Oeps2pgfpathforall extends PSObjectOperator {
        /**
         * Invokes this operator.
         *
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            ArrayStack<PSObject> cs = getContStack();
            ArrayStack<PSObject> os = getOpStack();
            ExecStack es = getExecStack();
            Matrix ctm = getGstate().current().getCtm();
            
            try {
                // Get arguments from continuation stack.
                PSObject close = cs.pop();
                PSObject curve = cs.pop();
                PSObject line = cs.pop();
                PSObject move = cs.pop();
                PSObjectArray path = cs.pop().toArray();
                cs.pop().toNull();
                
                if (path.size() > 0) {
                    PathSection section = path.remove(0).toPathSection();
                    int nrCoors = 0;
                    PSObject proc = close;
                    if (section instanceof Moveto) {
                        nrCoors = 1;
                        proc = move;
                    } else if (section instanceof Lineto) {
                        nrCoors = 1;
                        proc = line;
                    } else if (section instanceof Curveto) {
                        nrCoors = 3;
                        proc = curve;
                    }
                    
                    // Push objects on operand stack
                    for (int j = 0; j < nrCoors; j++) {
                        double x = section.getParam(2 * j);
                        double y = section.getParam(2 * j + 1);
                        double[] coor = ctm.itransform(x, y);
                        os.push(new PSObjectReal(coor[0]));
                        os.push(new PSObjectReal(coor[1]));
                    }
                    
                    // Push objects on execution stack
                    es.push(getDictStack().eps2pgfPathforall);
                    es.push(proc);
                
                    // Push arguments on continuation stack
                    cs.push(new PSObjectNull());
                    cs.push(path);
                    cs.push(move);
                    cs.push(line);
                    cs.push(curve);
                    cs.push(close);
                }
                
            } catch (PSError e) {
                throw new ProgramError(e.getErrorName().isis()
                        + " in continuation function");
            }
        }
    }
    
    /**
     * Internal Eps2pgf operator. Continuation function for 'repeat' operator.
     * Input arguments: null repeatcount proc
     * Note: right is top of stack
     * 
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    public class Oeps2pgfrepeat extends PSObjectOperator {
        /**
         * Invokes this operator.
         *
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            ArrayStack<PSObject> cs = getContStack();
            ExecStack es = getExecStack();
            try {
                // Get arguments from continuation stack.
                PSObject proc = cs.pop();
                int repeatCount = cs.pop().toNonNegInt();
                cs.pop().toNull();
                
                if (repeatCount > 0) {
                    // Push objects on execution stack
                    es.push(getDictStack().eps2pgfRepeat);
                    es.push(proc);
                
                    // Push objects on continuation stack
                    cs.push(new PSObjectNull());
                    cs.push(new PSObjectInt(repeatCount - 1));
                    cs.push(proc);
                }
            } catch (PSError e) {
                throw new ProgramError(e.getErrorName().isis()
                        + " in continuation function");
            }
        }
    }
    
    /**
     * Internal Eps2pgf operator: continuation function for looping context
     * operator.
     * 
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    public class Oeps2pgfresourceforall extends PSObjectOperator {
        /**
         * Invokes this operator.
         *
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            throw new ProgramError("Continuation function not yet implemented");
        }
    }
}
