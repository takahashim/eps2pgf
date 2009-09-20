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
import net.sf.eps2pgf.ps.errors.PSErrorInvalidAccess;
import net.sf.eps2pgf.ps.errors.PSErrorTypeCheck;
import net.sf.eps2pgf.ps.errors.PSErrorUnregistered;
import net.sf.eps2pgf.ps.objects.PSObject;
import net.sf.eps2pgf.ps.objects.PSObjectArray;
import net.sf.eps2pgf.ps.objects.PSObjectBool;
import net.sf.eps2pgf.ps.objects.PSObjectDict;
import net.sf.eps2pgf.ps.objects.PSObjectFont;
import net.sf.eps2pgf.ps.objects.PSObjectInt;
import net.sf.eps2pgf.ps.objects.PSObjectOperator;
import net.sf.eps2pgf.ps.objects.PSObjectReal;
import net.sf.eps2pgf.ps.objects.PSObjectString;
import net.sf.eps2pgf.ps.resources.colors.DeviceGray;
import net.sf.eps2pgf.ps.resources.colors.PSColor;


/**
 * Contains the operator functions for operators starting with letter G to I.
 */
public final class OperatorsGtoI extends OperatorContainer {
    
    /**
     * Quick access for a few operators.
     */
    // CHECKSTYLE:OFF
    public PSObjectOperator gsave;
    public PSObjectOperator grestore;
    //CHECKSTYLE:ON
    
    /**
     * Create a new set of operators and add them to the system dictionary of
     * the interpreter.
     * 
     * @param interpreter The interpreter
     * 
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    public OperatorsGtoI(final Interpreter interpreter) throws ProgramError {
        super(interpreter);
        try {
            DictStack dictStack = getDictStack();
            gsave = dictStack.lookup("gsave").toOperator();
            grestore = dictStack.lookup("grestore").toOperator();
        } catch (PSErrorTypeCheck e) {
            throw new ProgramError("Object in dictstack has incorrect"
                    + " type for quick access constants.");
        }
    }
    
    /**
     * PostScript op: gcheck.
     * 
     * @throws PSErrorStackUnderflow Tried to pop an object from an empty stack.
     */
    public class Ogcheck extends PSObjectOperator {
        /**
         * Invokes this operator.
         * 
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            PSObject any = getOpStack().pop();
            boolean inGlobal = any.gcheck();
            getOpStack().push(new PSObjectBool(inGlobal));
        }
    }
    
    /**
     * PostScript op: ge.
     */
    public class Oge extends PSObjectOperator {
        /**
         * Invokes this operator.
         * 
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            PSObject obj2 = getOpStack().pop();
            obj2.checkAccess(false, true, false);
            PSObject obj1 = getOpStack().pop();
            obj1.checkAccess(false, true, false);
            
            boolean gt = obj1.gt(obj2);
            boolean eq = obj1.eq(obj2);
            getOpStack().push(new PSObjectBool(gt || eq));
        }
    }
    
    /**
     * PostScript op: get.
     */
    public class Oget extends PSObjectOperator {
        /**
         * Invokes this operator.
         * 
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            PSObject indexKey = getOpStack().pop();
            PSObject obj = getOpStack().pop();
            obj.checkAccess(false, true, false);
            
            getOpStack().push(obj.get(indexKey));
        }
    }
    
    /**
     * PostScript op: getinterval.
     */
    public class Ogetinterval extends PSObjectOperator {
        /**
         * Invokes this operator.
         * 
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            int count = getOpStack().pop().toNonNegInt();
            int index = getOpStack().pop().toNonNegInt();
            PSObject obj = getOpStack().pop();
            obj.checkAccess(false, true, false);
            
            getOpStack().push(obj.getinterval(index, count));
        }
    }
    
    /**
     * Postscript op: glyphshow.
     */
    public class Oglyphshow extends PSObjectOperator {
        /**
         * Invokes this operator.
         * 
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            PSObject nameOrCid = getOpStack().pop();
            PSObjectFont font = getGstate().current().getFont();
            
            // Convert the name/CID to a string. This is done so that
            // text replacements can be applied.
            PSObjectString string = font.encodeChar(nameOrCid, getInterp());
            
            // Write the string to the output device
            double[] dpos = getTextHandler().showText(
                    getGstate().current().getDevice(), string);
            getGstate().current().rmoveto(dpos[0], dpos[1]);
        }
    }
    
    /**
     * PostScript op: grestore.
     */
    public class Ogrestore extends PSObjectOperator {
        /**
         * Invokes this operator.
         * 
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            getGstate().restoreGstate(true);
        }
    }
    
    /**
     * PostScript op: grestoreall.
     */
    public class Ogrestoreall extends PSObjectOperator {
        /**
         * Invokes this operator.
         * 
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            getGstate().restoreAllGstate(true);
        }
    }
    
    /**
     * PostScript op: gsave.
     */
    public class Ogsave extends PSObjectOperator {
        /**
         * Invokes this operator.
         * 
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            getGstate().saveGstate(true);
        }
    }
    
    /**
     * PostScript op: gt.
     */
    public class Ogt extends PSObjectOperator {
        /**
         * Invokes this operator.
         * 
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            PSObject obj2 = getOpStack().pop();
            obj2.checkAccess(false, true, false);
            PSObject obj1 = getOpStack().pop();
            obj1.checkAccess(false, true, false);
            
            boolean chk = obj1.gt(obj2);
            getOpStack().push(new PSObjectBool(chk));
        }
    }
    
    /**
     * PostScript op: identmatrix.
     */
    public class Oidentmatrix extends PSObjectOperator {
        /**
         * Invokes this operator.
         * 
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            PSObjectArray matrix = getOpStack().pop().toArray();
            matrix.copy(new Matrix());
            getOpStack().push(matrix);
        }
    }
    
    /**
     * PostScript op: idiv.
     */
    public class Oidiv extends PSObjectOperator {
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
            int quotient = int1 / int2;
            getOpStack().push(new PSObjectInt(quotient));
        }
    }
    
    /**
     * PostScript op: idtransform.
     */
    public class Oidtransform extends PSObjectOperator {
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
                
            }
            double dy;
            if (matrix == null) {
                matrix = getGstate().current().getCtm();
                dy = obj.toReal();
            } else {
                dy = getOpStack().pop().toReal();
            }
            double dx = getOpStack().pop().toReal();
            double[] transformed = matrix.idtransform(dx, dy);
            getOpStack().push(new PSObjectReal(transformed[0]));
            getOpStack().push(new PSObjectReal(transformed[1]));
        }
    }
    
    /**
     * PostScript op: if.
     */
    public class Oif extends PSObjectOperator {
        /**
         * Invokes this operator.
         * 
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            PSObjectArray proc = getOpStack().pop().toProc();
            boolean bool = getOpStack().pop().toBool();
            if (bool) {
                getExecStack().push(proc);
            }
        }
    }
    
    /**
     * PostScript op: ifelse.
     */
    public class Oifelse extends PSObjectOperator {
        /**
         * Invokes this operator.
         * 
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            PSObjectArray proc2 = getOpStack().pop().toProc();
            PSObjectArray proc1 = getOpStack().pop().toProc();
            boolean bool = getOpStack().pop().toBool();
            
            if (bool) {
                getExecStack().push(proc1);
            } else {
                getExecStack().push(proc2);
            }
        }
    }
    
    /**
     * PostScript op: image.
     */
    public class Oimage extends PSObjectOperator {
        /**
         * Invokes this operator.
         * 
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            PSObject dictOrDataSrc = getOpStack().pop();
            PSObjectDict dict;
            
            PSColor colorSpace;
            if (dictOrDataSrc instanceof PSObjectDict) {
                // We have the one argument image operand
                // dict image.
                dict = dictOrDataSrc.toDict();
                colorSpace = getGstate().current().getColor();
            } else {
                // We have the five argument image operand
                // width height bits/sample matrix datasrc image.
                PSObjectArray matrix = getOpStack().pop().toArray();
                Matrix.checkArray(matrix);
                int bitsPerSample = getOpStack().pop().toInt();
                int height = getOpStack().pop().toInt();
                int width = getOpStack().pop().toInt();
                
                dict = new PSObjectDict(getInterp());
                dict.setKey(Image.IMAGE_TYPE, new PSObjectInt(1));
                dict.setKey(Image.WIDTH, new PSObjectInt(width));
                dict.setKey(Image.HEIGHT, new PSObjectInt(height));
                dict.setKey(Image.IMAGE_MATRIX, matrix);
                dict.setKey(Image.DATA_SOURCE, dictOrDataSrc);
                dict.setKey(Image.BITS_PER_COMPONENT,
                        new PSObjectInt(bitsPerSample));
                double[] decode = {0.0, 1.0};
                dict.setKey(Image.DECODE,
                        new PSObjectArray(decode, getInterp()));
                colorSpace = new DeviceGray();
            }
            
            Image image = new Image(dict, getInterp(), colorSpace);
            getGstate().current().getDevice().image(image);
        }
    }
    
    /**
     * PostScript op: imagemask.
     */
    public class Oimagemask extends PSObjectOperator {
        /**
         * Invokes this operator.
         * 
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            throw new PSErrorUnregistered("imagemask operator");
        }
    }
    
    /**
     * PostScript op: index.
     */
    public class Oindex extends PSObjectOperator {
        /**
         * Invokes this operator.
         * 
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            // Get n, the index of the element to retrieve
            int n = getOpStack().pop().toNonNegInt();
            
            getOpStack().push(getOpStack().peek(n));
        }
    }
    
    /**
     * PostScript op: invertmatrix.
     */
    public class Oinvertmatrix extends PSObjectOperator {
        /**
         * Invokes this operator.
         * 
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            PSObjectArray matrix2 = getOpStack().pop().toArray();
            Matrix matrix1 = getOpStack().pop().toArray().toMatrix();
            matrix1.invert();
            matrix2.copy(matrix1);
            getOpStack().push(matrix2);
        }
    }
    
    /**
     * PostScript op: initclip.
     */
    public class Oinitclip extends PSObjectOperator {
        /**
         * Invokes this operator.
         * 
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            getGstate().current().setClippingPath(
                    getInterp().getDefaultClippingPath().clone());
            getGstate().current().getDevice().clip(getGstate().current()
                    .getClippingPath());
        }
    }
    
    /**
     * PostScript op: initmatrix.
     */
    public class Oinitmatrix extends PSObjectOperator {
        /**
         * Invokes this operator.
         * 
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            getGstate().current().initmatrix();
        }
    }
    
    /**
     * PostScript op: internaldict.
     */
    public class Ointernaldict extends PSObjectOperator {
        /**
         * Invokes this operator.
         * 
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            int nr = getOpStack().pop().toInt();
            if (nr != 1183615869) {
                throw new PSErrorInvalidAccess();
            }
            PSObjectDict systemdict =
                getDictStack().lookup("systemdict").toDict();
            getOpStack().push(systemdict.get(DictStack.KEY_INTERNALDICT));
        }
    }
    
    /**
     * PostScript op: itransform.
     */
    public class Oitransform extends PSObjectOperator {
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
                
            }
            double y;
            if (matrix == null) {
                matrix = getGstate().current().getCtm();
                y = obj.toReal();
            } else {
                y = getOpStack().pop().toReal();
            }
            double x = getOpStack().pop().toReal();
            
            double[] itransformed = matrix.itransform(x, y);
            getOpStack().push(new PSObjectReal(itransformed[0]));
            getOpStack().push(new PSObjectReal(itransformed[1]));
        }
    }
    
}
