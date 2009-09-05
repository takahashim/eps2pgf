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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.logging.Logger;

import net.sf.eps2pgf.ProgramError;
import net.sf.eps2pgf.ps.errors.PSError;
import net.sf.eps2pgf.ps.errors.PSErrorRangeCheck;
import net.sf.eps2pgf.ps.errors.PSErrorTypeCheck;
import net.sf.eps2pgf.ps.errors.PSErrorUnregistered;
import net.sf.eps2pgf.ps.objects.PSObject;
import net.sf.eps2pgf.ps.objects.PSObjectArray;
import net.sf.eps2pgf.ps.objects.PSObjectBool;
import net.sf.eps2pgf.ps.objects.PSObjectFile;
import net.sf.eps2pgf.ps.objects.PSObjectInt;
import net.sf.eps2pgf.ps.objects.PSObjectName;
import net.sf.eps2pgf.ps.objects.PSObjectNull;
import net.sf.eps2pgf.ps.objects.PSObjectOperator;
import net.sf.eps2pgf.ps.objects.PSObjectReal;
import net.sf.eps2pgf.ps.objects.PSObjectSave;
import net.sf.eps2pgf.ps.objects.PSObjectString;
import net.sf.eps2pgf.util.ArrayStack;


/**
 * Contains the operators functions for operators starting with letter P to R.
 */
public final class OperatorsPtoR extends OperatorContainer {
    
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
    public OperatorsPtoR(final Interpreter interpreter) throws ProgramError {
        super(interpreter);
    }
    
    /**
     * PostScript op: pathbbox.
     */
    public class Opathbbox extends PSObjectOperator {
        /**
         * Invokes this operator.
         *
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            double[] bbox = gsCurrent().pathbbox();
            for (int i = 0; i < 4; i++) {
                getOpStack().push(new PSObjectReal(bbox[i]));
            }
        }
    }

    /**
     * PostScript op: pathforall.
     */
    public class Opathforall extends PSObjectOperator {
        /**
         * Invokes this operator.
         *
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            ArrayStack<PSObject> os = getOpStack();
            PSObjectArray close = os.pop().toProc();
            PSObjectArray curve = os.pop().toProc();
            PSObjectArray line = os.pop().toProc();
            PSObjectArray move = os.pop().toProc();
            
            ArrayList<PathSection> sects = gsCurrent().getPath().getSections();
            PSObjectArray path = new PSObjectArray(getVm());
            for (int i = 0; i < sects.size(); i++) {
                path.addToEnd(sects.get(i));
            }
            
            // Push objects on execution stack
            getExecStack().push(getOpsEps2pgf().eps2pgfPathforall);
        
            // Push arguments on continuation stack
            ArrayStack<PSObject> cs = getContStack();
            cs.push(new PSObjectNull());
            cs.push(path);
            cs.push(move);
            cs.push(line);
            cs.push(curve);
            cs.push(close);
        }
    }

    /**
     * PostScript op: picstr.
     */
    public class Opicstr extends PSObjectOperator {
        /**
         * Invokes this operator.
         *
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            throw new PSErrorUnregistered("operator: picstr");
        }
    }

    /**
     * PostScript op: pop.
     */
    public class Opop extends PSObjectOperator {
        /**
         * Invokes this operator.
         *
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            getOpStack().pop();
        }
    }

    /**
     * PostScript op: pstack.
     */
    public class Opstack extends PSObjectOperator {
        /**
         * Invokes this operator.
         *
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            for (int i = getOpStack().size() - 1; i >= 0; i--) {
                System.out.println(getOpStack().get(i).isis());
            }
        }
    }

    /**
     * PostScript op: put.
     */
    public class Oput extends PSObjectOperator {
        /**
         * Invokes this operator.
         *
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            PSObject any = getOpStack().pop();
            PSObject indexKey = getOpStack().pop();
            PSObject obj = getOpStack().pop();
            obj.checkAccess(false, false, true);
            obj.put(indexKey, any);
        }
    }

    /**
     * PostScript op: putinterval.
     */
    public class Oputinterval extends PSObjectOperator {
        /**
         * Invokes this operator.
         *
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            PSObject subseq = getOpStack().pop();
            subseq.checkAccess(false, true, false);
            int index = getOpStack().pop().toNonNegInt();
            PSObject seq = getOpStack().pop();
            seq.checkAccess(false, false, true);
            
            seq.putinterval(index, subseq);
        }
    }

    /**
     * PostScript op: quit.
     */
    public class Oquit extends PSObjectOperator {
        /**
         * Invokes this operator.
         *
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            ExecStack estack = getExecStack();
            while (estack.pop() != null) {
                /* empty block */
            }
    
        }
    }

    /**
     * PostScript op: rcheck.
     */
    public class Orcheck extends PSObjectOperator {
        /**
         * Invokes this operator.
         *
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            PSObject obj = getOpStack().pop();
            boolean chk = obj.rcheck();
            getOpStack().push(new PSObjectBool(chk));
        }
    }

    /**
     * PostScript op: rcurveto.
     */
    public class Orcurveto extends PSObjectOperator {
        /**
         * Invokes this operator.
         *
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            double dy3 = getOpStack().pop().toReal();
            double dx3 = getOpStack().pop().toReal();
            double dy2 = getOpStack().pop().toReal();
            double dx2 = getOpStack().pop().toReal();
            double dy1 = getOpStack().pop().toReal();
            double dx1 = getOpStack().pop().toReal();
            gsCurrent().rcurveto(dx1, dy1, dx2, dy2, dx3, dy3);
        }
    }

    /**
     * PostScript op: readhexstring.
     */
    public class Oreadhexstring extends PSObjectOperator {
        /**
         * Invokes this operator.
         *
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            PSObjectString string = getOpStack().pop().toPSString();
            string.checkAccess(false, true, false);
            if (string.length() == 0) {
                throw new PSErrorRangeCheck();
            }
            
            PSObject nextObj = getOpStack().peek();
            if (!(nextObj instanceof PSObjectFile)) {
                throw new PSErrorTypeCheck();
            }
            nextObj.checkAccess(false, false, true);
            getOpStack().push(new PSObjectName("/ASCIIHexDecode"));
            getInterp().executeOperator("filter");
            PSObjectFile file = getOpStack().pop().toFile();
            
            PSObjectString substring = file.readstring(string);
            boolean bool = (string.length() == substring.length());
            
            getOpStack().push(substring);
            getOpStack().push(new PSObjectBool(bool));
        }
    }

    /**
     * PostScript op: readline.
     */
    public class Oreadline extends PSObjectOperator {
        /**
         * Invokes this operator.
         *
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            PSObjectString string = getOpStack().pop().toPSString();
            PSObjectFile file = getOpStack().pop().toFile();
            PSObjectArray arr = file.readLine(string);
            getOpStack().push(arr.get(0));
            getOpStack().push(arr.get(1));
        }
    }

    /**
     * PostScript op: readonly.
     */
    public class Oreadonly extends PSObjectOperator {
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
            obj.readonly();
            getOpStack().push(obj);
        }
    }

    /**
     * PostScript op: readstring.
     */
    public class Oreadstring extends PSObjectOperator {
        /**
         * Invokes this operator.
         *
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            PSObjectString string = getOpStack().pop().toPSString();
            string.checkAccess(false, true, false);
            if (string.length() == 0) {
                throw new PSErrorRangeCheck();
            }
            PSObjectFile file = getOpStack().pop().toFile();
            file.checkAccess(false, false, true);
            
            PSObjectString substring = file.readstring(string);
            boolean bool = (string.length() == substring.length());
            
            getOpStack().push(substring);
            getOpStack().push(new PSObjectBool(bool));
        }
    }

    /**
     * PostScript op: realtime.
     */
    public class Orealtime extends PSObjectOperator {
        /**
         * Invokes this operator.
         *
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            // Get the number of milliseconds since midnight
            Calendar now = Calendar.getInstance();
            int realtime = now.get(Calendar.HOUR_OF_DAY);
            realtime = 60 * realtime + now.get(Calendar.MINUTE);
            realtime = 60 * realtime + now.get(Calendar.SECOND);
            realtime = 1000 * realtime + now.get(Calendar.MILLISECOND);
            getOpStack().push(new PSObjectInt(realtime));
        }
    }

    /**
     * PostScript op: rectclip.
     */
    public class Orectclip extends PSObjectOperator {
        /**
         * Invokes this operator.
         *
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            rectPath();
            getInterp().executeOperator("clip");
            getInterp().executeOperator("newpath");
        }
    }
    
    /**
     * PostScript op: rectfill.
     */
    public class Orectfill extends PSObjectOperator {
        /**
         * Invokes this operator.
         *
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            getInterp().executeOperator("gsave");
            rectPath();
            getInterp().executeOperator("fill");
            getInterp().executeOperator("grestore");
        }
    }

    /**
     * PostScript op: rectstroke.
     */
    public class Orectstroke extends PSObjectOperator {
        /**
         * Invokes this operator.
         *
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            getInterp().executeOperator("gsave");
            
            // Check whether the top-most object is a matrix
            Matrix matrix = null;
            try {
                matrix = getOpStack().peek().toArray().toMatrix();
                getOpStack().pop();
            } catch (PSErrorTypeCheck e) {
                /* apparently the first object is not a matrix */
            } catch (PSErrorRangeCheck e) {
                /* apparently the first object is not a matrix */
            }
            
            rectPath();
            
            if (matrix != null) {
                gsCurrent().getCtm().concat(matrix);
                gsCurrent().updatePosition();
            }
            
            getInterp().executeOperator("stroke");
            getInterp().executeOperator("grestore");
        }
    }

    /**
     * PostScript op: repeat.
     */
    public class Orepeat extends PSObjectOperator {
        /**
         * Invokes this operator.
         *
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            PSObjectArray proc = getOpStack().pop().toProc();
            PSObject objN = getOpStack().pop();
            objN.toNonNegInt();
            
            ArrayStack<PSObject> cs = getContStack();
            
            cs.push(new PSObjectNull());
            cs.push(objN);
            cs.push(proc);
            
            getExecStack().push(getOpsEps2pgf().eps2pgfRepeat);
        }
    }

    /**
     * PostScript op: resourceforall.
     */
    public class Oresourceforall extends PSObjectOperator {
        /**
         * Invokes this operator.
         *
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            throw new PSErrorUnregistered("resourceforall operator");
        }
    }

    /**
     * PostScript op: resourcestatus.
     */
    public class Oresourcestatus extends PSObjectOperator {
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
            PSObjectArray ret =
                getInterp().getResourceManager().resourceStatus(category, key);
            for (int i = 0; i < ret.size(); i++) {
                getOpStack().push(ret.get(i));
            }
        }
    }

    /**
     * PostScript op: restore.
     */
    public class Orestore extends PSObjectOperator {
        /**
         * Invokes this operator.
         *
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            log.fine("Operator: restore");
            Interpreter interp = getInterp();
            PSObjectSave save = getOpStack().pop().toSave();
            save.restore(interp);
            getInterp().getGstate().restoreAllGstate(false);
        }
    }

    /**
     * PostScript op: rmoveto.
     */
    public class Orlineto extends PSObjectOperator {
        /**
         * Invokes this operator.
         *
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            double dy = getOpStack().pop().toReal();
            double dx = getOpStack().pop().toReal();
            gsCurrent().rlineto(dx, dy);
        }
    }

    /**
     * PostScript op: rmoveto.
     */
    public class Ormoveto extends PSObjectOperator {
        /**
         * Invokes this operator.
         *
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            double dy = getOpStack().pop().toReal();
            double dx = getOpStack().pop().toReal();
            gsCurrent().rmoveto(dx, dy);
        }
    }

    /**
     * PostScript op: roll.
     */
    public class Oroll extends PSObjectOperator {
        /**
         * Invokes this operator.
         *
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            int j = getOpStack().pop().toInt();
            int n = getOpStack().pop().toNonNegInt();
            if (n == 0) {
                return;
            }
    
            // Pop top n element from the stack
            PSObject[] lst = new PSObject[n];
            for (int i = n - 1; i >= 0; i--) {
                lst[i] = getOpStack().pop();
            }
            
            // Roll elements
            j = j % n;
            if (j < 0) {
                j = j + n;
            }
            PSObject[] rolledList = new PSObject[n];
            for (int i = 0; i < n; i++) {
                int rolledIndex = (i + j) % n;
                rolledList[rolledIndex] = lst[i];
            }
            
            // Push rolled list back on the stack
            for (int i = 0; i < n; i++) {
                getOpStack().push(rolledList[i]);
            }
        }
    }

    /**
     * PostScript op: rotate.
     */
    public class Orotate extends PSObjectOperator {
        /**
         * Invokes this operator.
         *
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            PSObject obj = getOpStack().pop();
            double angle;
            if (obj instanceof PSObjectArray) {
                PSObjectArray array = obj.toArray();
                Matrix matrix = array.toMatrix();
                angle = getOpStack().pop().toReal();
                matrix.rotate(angle);
                array.copy(matrix);
                getOpStack().push(array);
            } else {
                angle = obj.toReal();
                gsCurrent().getCtm().rotate(angle);
                gsCurrent().updatePosition();
            }
        }
    }

    /**
     * PostScript op: round.
     */
    public class Oround extends PSObjectOperator {
        /**
         * Invokes this operator.
         *
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            PSObject obj = getOpStack().pop();
            getOpStack().push(obj.round());
        }
    }    
    /**
     * Code common to all rect* operators. It starts with 'newpath' and ends
     * with 'closepath'.
     * 
     * @throws PSError A PostScript error occurred.
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    private void rectPath() throws PSError, ProgramError {
        getInterp().executeOperator("newpath");
        PSObject obj = getOpStack().pop();
        if ((obj instanceof PSObjectInt) || (obj instanceof PSObjectReal)) {
            double height = obj.toReal();
            double width = getOpStack().pop().toReal();
            double y = getOpStack().pop().toReal();
            double x = getOpStack().pop().toReal();
            rectPathSingle(x, y, width, height);
        } else if (obj instanceof PSObjectArray) {
            PSObjectArray numarray = obj.toArray();
            if ((numarray.length() % 4) != 0) {
                throw new PSErrorRangeCheck();
            }
            int nrRect = numarray.length() / 4;
            for (int i = 0; i < nrRect; i++) {
                double x = numarray.get(4 * i).toReal();
                double y = numarray.get(4 * i + 1).toReal();
                double width = numarray.get(4 * i + 2).toReal();
                double height = numarray.get(4 * i + 3).toReal();
                rectPathSingle(x, y, width, height);
            }
        } else if (obj instanceof PSObjectString) {
            PSObjectString numstring = obj.toPSString();
            int nrRect = numstring.numstringLength() / 4;
            for (int i = 0; i < nrRect; i++) {
                double x = numstring.numstringGet(4 * i);
                double y = numstring.numstringGet(4 * i + 1);
                double width = numstring.numstringGet(4 * i + 2);
                double height = numstring.numstringGet(4 * i + 3);
                rectPathSingle(x, y, width, height);
            }
        } else {
            throw new PSErrorTypeCheck();
        }
        
    }
    
    /**
     * Appends a single rectangle to the current path.
     * 
     * @param x The X-coordinate of lower-left corner.
     * @param y The Y-coordinate of lower-left corner.
     * @param width The width.
     * @param height The height.
     * 
     * @throws PSError A PostScript error occurred.
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    private void rectPathSingle(final double x, final double y,
            final double width, final double height) throws PSError,
            ProgramError {
        
        GraphicsState gstate = gsCurrent();
        gstate.moveto(x, y);
        gstate.rlineto(width, 0.0);
        gstate.rlineto(0.0, height);
        gstate.rlineto(-width, 0.0);
        getInterp().executeOperator("closepath");
    }

}
