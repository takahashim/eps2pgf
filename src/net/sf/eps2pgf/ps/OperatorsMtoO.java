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
import net.sf.eps2pgf.ps.errors.PSErrorUnregistered;
import net.sf.eps2pgf.ps.objects.PSObject;
import net.sf.eps2pgf.ps.objects.PSObjectArray;
import net.sf.eps2pgf.ps.objects.PSObjectDict;
import net.sf.eps2pgf.ps.objects.PSObjectInt;
import net.sf.eps2pgf.ps.objects.PSObjectMark;
import net.sf.eps2pgf.ps.objects.PSObjectNull;
import net.sf.eps2pgf.ps.objects.PSObjectOperator;
import net.sf.eps2pgf.ps.objects.PSObjectReal;
import net.sf.eps2pgf.ps.resources.outputdevices.NullDevice;
import net.sf.eps2pgf.ps.resources.outputdevices.OutputDevice;


/**
 * Contains the operator functions for operators starting with letter M to O.
 */
public final class OperatorsMtoO extends OperatorContainer {
    
    /**
     * Create a new set of operators and add them to the system dictionary of
     * the interpreter.
     * 
     * @param interpreter The interpreter
     * 
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    public OperatorsMtoO(final Interpreter interpreter) throws ProgramError {
        super(interpreter);
    }
    
    /**
     * PostScript op: makefont.
     */
    public class Omakefont extends PSObjectOperator {
        /**
         * Invokes this operator.
         * 
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            Matrix matrix = getOpStack().pop().toArray().toMatrix();
            PSObjectDict font = getOpStack().pop().toDict();
            font = font.clone();
            PSObjectArray fontMatrixArray = font.lookup("FontMatrix").toArray();
            Matrix fontMatrix = fontMatrixArray.toMatrix();
            
            // Concatenate matrix to fontMatrix and store it back in font
            fontMatrix.concat(matrix);
            fontMatrixArray.copy(fontMatrix);
            font.setKey("FontMatrix", fontMatrixArray);
            
            // Calculate the fontsize in LaTeX points
            Matrix ctm = getGstate().current().getCtm().clone();
            ctm.concat(fontMatrix);
            double fontSize = ctm.getMeanScaling() / 2.54 * 72.27;
            font.setKey("FontSize", new PSObjectReal(fontSize));
            
            getOpStack().push(font);
        }
    }
    
    /**
     * PostScript op: makepattern.
     */
    public class Omakepattern extends PSObjectOperator {
        /**
         * Invokes this operator.
         * 
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            //Matrix matrix = getOpStack().pop().toMatrix();
            //PSObjectDict dict = getOpStack().pop().toDict();
            throw new PSErrorUnregistered("makepattern operator");
        }
    }
    
    /**
     * Postscript op: mark.
     */
    public class Omark extends PSObjectOperator {
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
    }
    
    /**
     * Postscript op: matrix.
     */
    public class Omatrix extends PSObjectOperator {
        /**
         * Invokes this operator.
         * 
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            getOpStack().push((new Matrix()).toArray(getInterp()));
        }
    }
    
    /**
     * PostScript op: maxlength.
     */
    public class Omaxlength extends PSObjectOperator {
        /**
         * Invokes this operator.
         * 
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            PSObjectDict dict = getOpStack().pop().toDict();
            dict.checkAccess(false, true, false);
            
            getOpStack().push(new PSObjectInt(dict.maxlength()));
        }
    }
    
    /**
     * PostScript operator: mod.
     */
    public class Omod extends PSObjectOperator {
        /**
         * Invokes this operator.
         * 
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            int int2 = getOpStack().pop().toInt();
            int int1 = getOpStack().pop().toInt();
            getOpStack().push(new PSObjectInt(int1 % int2));
        }
    }
    
    /**
     * PostScript op: moveto.
     */
    public class Omoveto extends PSObjectOperator {
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
            getGstate().current().moveto(x, y);
        }
    }
    
    /**
     * PostScript op: mul.
     */
    public class Omul extends PSObjectOperator {
        /**
         * Invokes this operator.
         * 
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            PSObject num2 = getOpStack().pop();
            PSObject num1 = getOpStack().pop();
            getOpStack().push(num1.mul(num2));
        }
    }
    
    /**
     * PostScript op: ne.
     */
    public class One extends PSObjectOperator {
        /**
         * Invokes this operator.
         * 
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            executeOperator("eq");
            executeOperator("not");
        }
    }

    /**
     * PostScript op: neg.
     */
    public class Oneg extends PSObjectOperator {
        /**
         * Invokes this operator.
         * 
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            PSObject obj = getOpStack().pop();
            getOpStack().push(obj.neg());
        }
    }

    /**
     * PostScript op: newpath.
     */
    public class Onewpath extends PSObjectOperator {
        /**
         * Invokes this operator.
         * 
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            getGstate().current().setPath(new Path(getGstate()));
            getGstate().current().setPosition(Double.NaN, Double.NaN);
        }
    }
    
    /**
     * PostScript op: noaccess.
     */
    public class Onoaccess extends PSObjectOperator {
        /**
         * Invokes this operator.
         * 
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            PSObject obj = getOpStack().pop();
            if (obj instanceof PSObjectDict) {
                obj.checkAccess(false, false, true);
            }
            obj.noaccess();
            getOpStack().push(obj);
        }
    }
    
    /**
     * PostScript op: not.
     */
    public class Onot extends PSObjectOperator {
        /**
         * Invokes this operator.
         * 
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            PSObject obj = getOpStack().pop();
            getOpStack().push(obj.not());
        }
    }
    
    /**
     * PostScript op: null.
     */
    public class Onull extends PSObjectOperator {
        /**
         * Invokes this operator.
         * 
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            getOpStack().push(new PSObjectNull());
        }
    }
    
    /**
     * PostScript op: nulldevice.
     */
    public class Onulldevice extends PSObjectOperator {
        /**
         * Invokes this operator.
         * 
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            OutputDevice nullDevice = new NullDevice();
            getGstate().current().setDevice(nullDevice);
            getGstate().current().initmatrix();
        }
    }

    /**
     * PostScript op: or.
     */
    public class Oor extends PSObjectOperator {
        /**
         * Invokes this operator.
         * 
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            PSObject obj2 = getOpStack().pop();
            PSObject obj1 = getOpStack().pop();
            getOpStack().push(obj1.or(obj2));
        }
    }
    

    
}
