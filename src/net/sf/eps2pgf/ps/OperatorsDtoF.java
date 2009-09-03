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

import java.io.InputStream;
import java.util.List;

import net.sf.eps2pgf.ProgramError;
import net.sf.eps2pgf.io.PSStringInputStream;
import net.sf.eps2pgf.ps.errors.PSError;
import net.sf.eps2pgf.ps.errors.PSErrorInvalidExit;
import net.sf.eps2pgf.ps.errors.PSErrorStackUnderflow;
import net.sf.eps2pgf.ps.errors.PSErrorTypeCheck;
import net.sf.eps2pgf.ps.errors.PSErrorUnregistered;
import net.sf.eps2pgf.ps.objects.PSObject;
import net.sf.eps2pgf.ps.objects.PSObjectArray;
import net.sf.eps2pgf.ps.objects.PSObjectBool;
import net.sf.eps2pgf.ps.objects.PSObjectDict;
import net.sf.eps2pgf.ps.objects.PSObjectFile;
import net.sf.eps2pgf.ps.objects.PSObjectFont;
import net.sf.eps2pgf.ps.objects.PSObjectInt;
import net.sf.eps2pgf.ps.objects.PSObjectName;
import net.sf.eps2pgf.ps.objects.PSObjectNull;
import net.sf.eps2pgf.ps.objects.PSObjectOperator;
import net.sf.eps2pgf.ps.objects.PSObjectReal;
import net.sf.eps2pgf.ps.objects.PSObjectString;
import net.sf.eps2pgf.ps.resources.ResourceManager;
import net.sf.eps2pgf.ps.resources.filters.EexecDecode;
import net.sf.eps2pgf.ps.resources.filters.FilterManager;
import net.sf.eps2pgf.ps.resources.fonts.FontManager;
import net.sf.eps2pgf.util.ArrayStack;


/**
 * Contains the operator functions for operators starting with letter D to F.
 */
public final class OperatorsDtoF extends OperatorContainer {
    
    /**
     * Create a new set of operators and add them to the system dictionary of
     * the interpreter.
     * 
     * @param interpreter The interpreter
     * 
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    public OperatorsDtoF(final Interpreter interpreter) throws ProgramError {
        super(interpreter);
    }
    
    /**
     * PostScript op: def.
     * 
     * @throws PSError A PostScript error occurred.
     */
    public class Odef extends PSObjectOperator {
        /**
         * Invokes this operator.
         * 
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            PSObject value = getOpStack().pop();
            PSObject key = getOpStack().pop();
            getDictStack().checkAccess(false, false, true);
            getDictStack().def(key, value);
        }
    }
    
    /**
     * PostScript op: defaultmatrix.
     * 
     * @throws PSError A PostScript error occurred.
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    public class Odefaultmatrix extends PSObjectOperator {
        /**
         * Invokes this operator.
         * 
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            PSObjectArray matrix = getOpStack().pop().toArray();
            matrix.copy(getGstate().current().getDevice().defaultCTM());
            getOpStack().push(matrix);
        }
    }
    
    /**
     * PostScript op: definefont.
     * 
     * @throws PSError A PostScript error occurred.
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    public class Odefinefont extends PSObjectOperator {
        /**
         * Invokes this operator.
         * 
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            PSObjectFont font = getOpStack().pop().toFont();
            PSObject key = getOpStack().pop();
            PSObject obj = getResourceManager().defineResource(
                    ResourceManager.CAT_FONT, key, font);
            getOpStack().push(obj);
        }
    }
    
    /**
     * PostScript op: defineresource.
     * 
     * @throws PSError A PostScript error occurred.
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    public class Odefineresource extends PSObjectOperator {
        /**
         * Invokes this operator.
         * 
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            PSObjectName category = getOpStack().pop().toName();
            PSObject instance = getOpStack().pop();
            PSObject key = getOpStack().pop();
            PSObject obj = getResourceManager().defineResource(
                    category, key, instance);
            getOpStack().push(obj);
        }
    }
    
    /**
     * PostScript op: dict.
     * 
     * @throws PSErrorStackUnderflow Tried to pop an object from an empty stack.
     * @throws PSErrorTypeCheck A PostScript typecheck error occurred.
     * @throws PSErrorRangeCheck A PostScript rangecheck error occurred.
     * @throws PSErrorVMError Virtual memory error.
     */
    public class Odict extends PSObjectOperator {
        /**
         * Invokes this operator.
         * 
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            int capacity = getOpStack().pop().toNonNegInt();
            getOpStack().push(new PSObjectDict(capacity, getVm()));
        }
    }
    
    /**
     * PostScript op: dictstack.
     * 
     * @throws PSError A PostScript error occurred.
     */
    public class Odictstack extends PSObjectOperator {
        /**
         * Invokes this operator.
         * 
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            PSObjectArray array = getOpStack().pop().toArray();
            array.checkAccess(false, false, true);
            getOpStack().push(getDictStack().dictstack(array));
        }
    }
    
    /**
     * PostScript op: div.
     * 
     * @throws PSError A PostScript error occurred.
     */
    public class Odiv extends PSObjectOperator {
        /**
         * Invokes this operator.
         * 
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            double num2 = getOpStack().pop().toReal();
            double num1 = getOpStack().pop().toReal();
            getOpStack().push(new PSObjectReal(num1 / num2));
        }
    }
    
    /**
     * PostScript op: dtransform.
     * 
     * @throws PSError A PostScript error occurred.
     */
    public class Odtransform extends PSObjectOperator {
        /**
         * Invokes this operator.
         * 
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            PSObject obj = getOpStack().pop();
            Matrix matrix = null;
            try {
                matrix = obj.toArray().toMatrix();
            } catch (PSErrorTypeCheck e) {
                /* empty block */
            }
            double dy;
            if (matrix == null) {
                matrix = getGstate().current().getCtm();
                dy = obj.toReal();
            } else {
                dy = getOpStack().pop().toReal();
            }
            double dx = getOpStack().pop().toReal();
            double[] transformed = matrix.dtransform(dx, dy);
            getOpStack().push(new PSObjectReal(transformed[0]));
            getOpStack().push(new PSObjectReal(transformed[1]));
        }
    }
    
    /**
     * PostScript op: dup.
     * 
     * @throws PSErrorStackUnderflow Tried to pop an object from an empty stack.
     */
    public class Odup extends PSObjectOperator {
        /**
         * Invokes this operator.
         * 
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            getOpStack().push(getOpStack().peek().dup());
        }
    }
    
    /**
     * PostScript op: eexec.
     * 
     * @throws PSError A PostScript error occurred.
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    public class Oeexec extends PSObjectOperator {
        /**
         * Invokes this operator.
         * 
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            PSObject obj = getOpStack().pop();
            obj.checkAccess(true, false, false);
            
            InputStream rawInStream;
            if (obj instanceof PSObjectFile) {
                rawInStream = ((PSObjectFile) obj).getStream();
            } else if (obj instanceof PSObjectString) {
                rawInStream = new PSStringInputStream(((PSObjectString) obj));
            } else {
                throw new PSErrorTypeCheck();
            }
            InputStream eexecInStream = new EexecDecode(rawInStream, getVm());
            PSObjectFile eexecFile = new PSObjectFile(eexecInStream, getVm());
            
            getDictStack().pushDict(
                    getDictStack().lookup("systemdict").toDict());
            
            getExecStack().push(getDictStack().eps2pgfEexec);
            getContStack().push(new PSObjectNull());
            
            getExecStack().push(eexecFile);
        }
    }
    
    /**
     * PostScript op: end.
     * 
     * @throws PSError A PostScript error occurred.
     */
    public class Oend extends PSObjectOperator {
        /**
         * Invokes this operator.
         * 
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            getDictStack().popDict();
        }
    }
    
    /**
     * PostScript op: eoclip.
     * 
     * @throws PSError A PostScript error occurred.
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    public class Oeoclip extends PSObjectOperator {
        /**
         * Invokes this operator.
         * 
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            GstateStack gstate = getGstate();
            gstate.current().clip();
            gstate.current().getDevice().eoclip(gstate.current());
        }
    }
    
    /**
     * PostScript op: eofill.
     * 
     * @throws PSError A PostScript error occurred.
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    public class Oeofill extends PSObjectOperator {
        /**
         * Invokes this operator.
         * 
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            GstateStack gstate = getGstate();
            gstate.current().getDevice().eofill(gstate.current());
            getInterp().executeOperator("newpath");
        }
    }
    
    /**
     * PostScript op: errordict.
     * 
     * @throws PSErrorUnregistered Encountered a PostScript feature that is not
     * (yet) implemented.
     */
    public class Oerrordict extends PSObjectOperator {
        /**
         * Invokes this operator.
         * 
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            throw new PSErrorUnregistered("errordict operator");
        }
    }
    
    /**
     * PostScript op: eq.
     * 
     * @throws PSError A PostScript error occurred.
     */
    public class Oeq extends PSObjectOperator {
        /**
         * Invokes this operator.
         * 
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            PSObject any2 = getOpStack().pop();
            any2.checkAccess(false, true, false);
            PSObject any1 = getOpStack().pop();
            any1.checkAccess(false, true, false);
            getOpStack().push(new PSObjectBool(any1.eq(any2)));
        }
    }
    
    /**
     * PostScript op: exch.
     * 
     * @throws PSErrorStackUnderflow Tried to pop an object from an empty stack.
     */
    public class Oexch extends PSObjectOperator {
        /**
         * Invokes this operator.
         * 
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            PSObject any2 = getOpStack().pop();
            PSObject any1 = getOpStack().pop();
            getOpStack().push(any2);
            getOpStack().push(any1);
        }
    }
    
    /**
     * PostScript op: exec.
     * 
     * @throws PSError A PostScript error occurred.
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    public class Oexec extends PSObjectOperator {
        /**
         * Invokes this operator.
         * 
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            PSObject any = getOpStack().pop();
            getInterp().executeObject(any);
        }
    }
    
    /**
     * PostScript op: execstack.
     * 
     * @throws PSError A PostScript error occurred.
     */
    public class Oexecstack extends PSObjectOperator {
        /**
         * Invokes this operator.
         * 
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            PSObjectArray array = getOpStack().pop().toArray();
            array.checkAccess(false, false, true);
            PSObject subArray = array.copy(getExecStack().getStack());
            getOpStack().push(subArray);
        }
    }
    
    /**
     * PostScript op: executeonly.
     * 
     * @throws PSError A PostScript error occurred.
     */
    public class Oexecuteonly extends PSObjectOperator {
        /**
         * Invokes this operator.
         * 
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            PSObject obj = getOpStack().pop();
            obj.checkAccess(true, false, false);
            obj.executeonly();
            getOpStack().push(obj);
        }
    }
    
    /**
     * PostScript op: exit.
     * 
     * @throws PSErrorInvalidExit 'exit' operator at invalid location.
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    public class Oexit extends PSObjectOperator {
        /**
         * Invokes this operator.
         * 
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            ExecStack es = getExecStack();
            PSObject obj;
            DictStack ds = getDictStack();
            while ((obj = es.pop()) != null) {
                if (ds.isLoopingContext(obj)) {
                    // Also pop down the continuation stack
                    try {
                        ArrayStack<PSObject> cs = getContStack();
                        while (cs.size() > 0) {
                            if (cs.pop() instanceof PSObjectNull) {
                                break;
                            }
                        }
                    } catch (PSErrorStackUnderflow e) {
                        /* empty block */
                    }
                    
                    return;
                    
                } else if (obj == ds.eps2pgfStopped) {
                    // Push obj back on execution stack to make sure we're still
                    // in the 'stopped' context.
                    es.push(obj);
                    throw new PSErrorInvalidExit();
                }
            }
            
            throw new PSErrorInvalidExit();
        }
    }
    
    /**
     * PostScript op: exp.
     * 
     * @throws PSErrorStackUnderflow Tried to pop an object from an empty stack.
     * @throws PSErrorTypeCheck A PostScript typecheck error occurred.
     */
    public class Oexp extends PSObjectOperator {
        /**
         * Invokes this operator.
         * 
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            double exponent = getOpStack().pop().toReal();
            double base = getOpStack().pop().toReal();
            double result = Math.pow(base, exponent);
            getOpStack().push(new PSObjectReal(result));
        }
    }
    
    /**
     * PostScript op: false.
     */
    public class Ofalse extends PSObjectOperator {
        /**
         * Invokes this operator.
         * 
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            getOpStack().push(new PSObjectBool(false));
        }
    }
    
    /**
     * PostScript op: filenameforall.
     * 
     * @throws PSErrorUnregistered Encountered a PostScript feature that is not
     * (yet) implemented.
     */
    public class Ofilenameforall extends PSObjectOperator {
        /**
         * Invokes this operator.
         * 
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            throw new PSErrorUnregistered("filenameforall operator");
        }
    }
    

    
    /**
     * PostScript op: fill.
     * 
     * @throws PSError A PostScript error occurred.
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    public class Ofill extends PSObjectOperator {
        /**
         * Invokes this operator.
         * 
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            GstateStack gstate = getGstate();
            gstate.current().getDevice().fill(gstate.current());
            getInterp().executeOperator("newpath");
        }
    }
    
    /**
     * PostScript op: filter.
     * 
     * @throws PSError A PostScript error occurred.
     */
    public class Ofilter extends PSObjectOperator {
        /**
         * Invokes this operator.
         * 
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            VM vm = getVm();
            PSObjectName name = getOpStack().pop().toName();
            PSObjectDict paramDict =
                FilterManager.getParameters(name, getOpStack(), vm);
            PSObject sourceOrTarget = getOpStack().pop();
            PSObjectFile file =
                FilterManager.filter(name, paramDict, sourceOrTarget, vm);
            getOpStack().push(file);
        }
    }
    
    /**
     * PostScript op: findencoding.
     * 
     * @throws PSError A PostScript error occurred.
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    public class Ofindencoding extends PSObjectOperator {
        /**
         * Invokes this operator.
         * 
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            getOpStack().push(new PSObjectName("/Encoding"));
            getInterp().executeOperator("findresource");
        }
    }
    
    /**
     * PostScript op: findfont.
     * 
     * @throws PSError A PostScript error occurred.
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    public class Ofindfont extends PSObjectOperator {
        /**
         * Invokes this operator.
         * 
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            PSObject key = getOpStack().pop();
            FontManager fontManager = getResourceManager().getFontManager();
            getOpStack().push(fontManager.findFont(key));
        }
    }
    
    /**
     * PostScript op: findresource.
     * 
     * @throws PSError A PostScript error occurred.
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    public class Ofindresource extends PSObjectOperator {
        /**
         * Invokes this operator.
         * 
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            PSObjectName category = getOpStack().pop().toName();
            PSObject key = getOpStack().pop();
            PSObject obj = getResourceManager().findResource(category, key);
            getOpStack().push(obj);
        }
    }
    
    /**
     * PostScript op: flattenpath.
     * 
     * @throws PSError A PostScript error occurred.
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    public class Oflattenpath extends PSObjectOperator {
        /**
         * Invokes this operator.
         * 
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            getGstate().current().flattenpath();
        }
    }
    
    /**
     * PostScript op: floor.
     * 
     * @throws PSErrorStackUnderflow Tried to pop an object from an empty stack.
     * @throws PSErrorTypeCheck A PostScript typecheck error occurred.
     */
    public class Ofloor extends PSObjectOperator {
        /**
         * Invokes this operator.
         * 
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            PSObject obj = getOpStack().pop();
            getOpStack().push(obj.floor());
        }
    }
    
    /**
     * PostScript op: flushfile.
     * 
     * @throws PSError A PostScript error occurred.
     */
    public class Oflushfile extends PSObjectOperator {
        /**
         * Invokes this operator.
         * 
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            PSObjectFile file = getOpStack().pop().toFile();
            file.flushFile();
        }
    }
    
    /**
     * PostScript op: for.
     * 
     * @throws PSError A PostScript error occurred.
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    public class Ofor extends PSObjectOperator {
        /**
         * Invokes this operator.
         * 
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            PSObject proc = getOpStack().pop();
            PSObject objLimit = getOpStack().pop();
            PSObject objInc = getOpStack().pop();
            PSObject objInitial = getOpStack().pop();
            
            // Prevent (virtually) infinite loops
            double inc = objInc.toReal();
            double limit = objLimit.toReal();
            double initial = objInitial.toReal();
            if (inc == 0) {
                return;
            } else if ((inc > 0) && (limit < initial)) {
                return;
            } else if ((inc < 0) && (limit > initial)) {
                return;
            }
            
            // Push continuation function to execution stack
            getExecStack().push(getDictStack().eps2pgfFor);
            
            // Push arguments to continuation stack
            ArrayStack<PSObject> cs = getContStack();
            cs.push(new PSObjectNull());
            cs.push(objInitial);
            cs.push(objInc);
            cs.push(objLimit);
            cs.push(proc);
        }
    }
    
    /**
     * PostScript op: forall.
     * 
     * @throws PSError A PostScript error occurred.
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    public class Oforall extends PSObjectOperator {
        /**
         * Invokes this operator.
         * 
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            ArrayStack<PSObject> os = getOpStack();
            ExecStack es = getExecStack();
            ArrayStack<PSObject> cs = getContStack();
    
            PSObject proc = os.pop();
            proc.checkAccess(true, false, false);
            PSObject obj = os.pop();
            obj.checkAccess(false, true, false);
            
            List<PSObject> items = obj.getItemList();
            int nr = items.remove(0).toNonNegInt();
            PSObjectArray itemListArray = new PSObjectArray(items, getVm());
            
            cs.push(new PSObjectNull());
            cs.push(new PSObjectInt(nr));
            cs.push(itemListArray);
            cs.push(proc);
    
            es.push(getDictStack().eps2pgfForall);
        }
    }

}
