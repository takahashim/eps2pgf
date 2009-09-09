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
import net.sf.eps2pgf.ps.errors.PSErrorTypeCheck;
import net.sf.eps2pgf.ps.errors.PSErrorUnregistered;
import net.sf.eps2pgf.ps.objects.PSObject;
import net.sf.eps2pgf.ps.objects.PSObjectArray;
import net.sf.eps2pgf.ps.objects.PSObjectBool;
import net.sf.eps2pgf.ps.objects.PSObjectDict;
import net.sf.eps2pgf.ps.objects.PSObjectFile;
import net.sf.eps2pgf.ps.objects.PSObjectInt;
import net.sf.eps2pgf.ps.objects.PSObjectName;
import net.sf.eps2pgf.ps.objects.PSObjectOperator;
import net.sf.eps2pgf.ps.objects.PSObjectReal;
import net.sf.eps2pgf.ps.objects.PSObjectString;


/**
 * Contains the operator functions for operators starting with letter T to Z.
 */
public final class OperatorsTtoZ extends OperatorContainer {
    
    /** Log information. */
    private final Logger log = Logger.getLogger("net.sourceforge.eps2pgf");
    
    /**
     * Create a new set of operators and add them to the system dictionary of
     * the interpreter.
     * 
     * @param interpreter The interpreter
     * 
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    public OperatorsTtoZ(final Interpreter interpreter) throws ProgramError {
        super(interpreter);
    }
    
    /**
     * PostScript op: token.
     */
    public class Otoken extends PSObjectOperator {
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
            
            if (!(obj instanceof PSObjectString)
                    && !(obj instanceof PSObjectFile)) {
                throw new PSErrorTypeCheck();
            }
            for (PSObject item : obj.token()) {
                getOpStack().push(item);
            }
        }
    }
    
    /**
     * PostScript op: transform.
     */
    public class Otransform extends PSObjectOperator {
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
            double[] transformed = matrix.transform(x, y);
            getOpStack().push(new PSObjectReal(transformed[0]));
            getOpStack().push(new PSObjectReal(transformed[1]));
        }
    }
    
    /**
     * PostScript op: translate.
     */
    public class Otranslate extends PSObjectOperator {
        /**
         * Invokes this operator.
         * 
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            PSObject obj = getOpStack().pop();
            double tx, ty;
            if (obj instanceof PSObjectArray) {
                PSObjectArray array = obj.toArray();
                Matrix matrix = array.toMatrix();
                ty = getOpStack().pop().toReal();
                tx = getOpStack().pop().toReal();
                matrix.translate(tx, ty);
                array.copy(matrix);
                getOpStack().push(array);
            } else {
                ty = obj.toReal();
                tx = getOpStack().pop().toReal();
                getGstate().current().getCtm().translate(tx, ty);
                getGstate().current().updatePosition();
            }
        }
    }
    
    /**
     * PostScript op: true.
     */
    public class Otrue extends PSObjectOperator {
        /**
         * Invokes this operator.
         * 
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            getOpStack().push(new PSObjectBool(true));
        }
    }
    
    /**
     * PostScript op: truncate.
     */
    public class Otruncate extends PSObjectOperator {
        /**
         * Invokes this operator.
         * 
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            PSObject obj = getOpStack().pop();
            getOpStack().push(obj.truncate());
        }
    }
    
    /**
     * PostScript op: type.
     */
    public class Otype extends PSObjectOperator {
        /**
         * Invokes this operator.
         * 
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            PSObject any = getOpStack().pop();
            getOpStack().push(new PSObjectName(any.type(), false));
        }
    }
    
    /**
     * PostScript op: undef.
     */
    public class Oundef extends PSObjectOperator {
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
            dict.checkAccess(false, false, true);
            
            dict.undef(key);
        }
    }
    
    /**
     * PostScript op: undefineresource.
     */
    public class Oundefineresource extends PSObjectOperator {
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
            getResourceManager().undefineResource(category, key);
        }
    }
    
    /**
     * PostScript op: usertime.
     */
    public class Ousertime extends PSObjectOperator {
        /**
         * Invokes this operator.
         * 
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            long currentTime = System.currentTimeMillis();
            int userTime = (int) (currentTime - getInterp().getInitTime());
            getOpStack().push(new PSObjectInt(userTime));
        }
    }
    
    /**
     * PostScript op: wcheck.
     */
    public class Owcheck extends PSObjectOperator {
        /**
         * Invokes this operator.
         * 
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            PSObject obj = getOpStack().pop();
            boolean chk = obj.wcheck();
            getOpStack().push(new PSObjectBool(chk));
        }
    }
    
    /**
     * PostScript op: where.
     */
    public class Owhere extends PSObjectOperator {
        /**
         * Invokes this operator.
         * 
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            PSObject key = getOpStack().pop();
            PSObjectDict dict = getDictStack().where(key);
            if (dict == null) {
                getOpStack().push(new PSObjectBool(false));
            } else {
                getOpStack().push(dict);
                getOpStack().push(new PSObjectBool(true));
            }
        }
    }
    
    /**
     * PostScript op: widthshow.
     */
    public class Owidthshow extends PSObjectOperator {
        /**
         * Invokes this operator.
         * 
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            throw new PSErrorUnregistered("widthshow operator");
        }
    }
   
    /**
     * PostScript op: xcheck.
     */
    public class Oxcheck extends PSObjectOperator {
        /**
         * Invokes this operator.
         * 
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            PSObject any = getOpStack().pop();
            PSObjectBool check = new PSObjectBool(any.xcheck());
            getOpStack().push(check);
        }
    }
    
    /**
     * PostScript op: xor.
     */
    public class Oxor extends PSObjectOperator {
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
            getOpStack().push(obj1.xor(obj2));
        }
    }
    
    /**
     * PostScript op: xshow.
     */
    public class Oxshow extends PSObjectOperator {
        /**
         * Invokes this operator.
         * 
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            log.info("xshow operator encoutered. xshow is not implemented, "
                    + "instead the normal show is used.");
            getOpStack().pop(); // read the numarray/string object
            executeOperator("show");
        }
    }
    
    /**
     * PostScript op: xyshow.
     */
    public class Oxyshow extends PSObjectOperator {
        /**
         * Invokes this operator.
         * 
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            log.info("xyshow operator encoutered. xyshow is not implemented, "
                    + "instead the normal show is used.");
            getOpStack().pop(); // read the numarray/string object
            executeOperator("show");
        }
    }
    
    /**
     * PostScript op: yshow.
     */
    public class Oyshow extends PSObjectOperator {
        /**
         * Invokes this operator.
         * 
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            log.info("yshow operator encoutered. yshow is not implemented, "
                    + "instead the normal show is used.");
            getOpStack().pop(); // read the numarray/string object
            executeOperator("show");
        }
    }
    
}
