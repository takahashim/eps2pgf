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

import java.util.List;
import java.util.logging.Logger;

import net.sf.eps2pgf.ProgramError;
import net.sf.eps2pgf.ps.errors.PSError;
import net.sf.eps2pgf.ps.errors.PSErrorRangeCheck;
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
import net.sf.eps2pgf.ps.objects.PSObjectSave;
import net.sf.eps2pgf.ps.objects.PSObjectString;
import net.sf.eps2pgf.ps.resources.colors.DeviceRGB;
import net.sf.eps2pgf.ps.resources.outputdevices.CacheDevice;
import net.sf.eps2pgf.util.ArrayStack;


/**
 * Contains the operator functions for operators starting with letter T to Z.
 */
public final class OperatorsS extends OperatorContainer {
    
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
    public OperatorsS(final Interpreter interpreter) throws ProgramError {
        super(interpreter);
    }
    
    /**
     * PostScript op: save.
     */
    public class Osave extends PSObjectOperator {
        /**
         * Invokes this operator.
         * 
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            log.fine("Operator: save");
            getOpStack().push(new PSObjectSave(getInterp()));
            getGstate().saveGstate(false);
        }
    }
   
    /**
     * PostScript op: scale.
     */
    public class Oscale extends PSObjectOperator {
        /**
         * Invokes this operator.
         * 
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            PSObject obj = getOpStack().pop();
            double sx, sy;
            if (obj instanceof PSObjectArray) {
                PSObjectArray array = obj.toArray();
                Matrix matrix = array.toMatrix();
                sy = getOpStack().pop().toReal();
                sx = getOpStack().pop().toReal();
                matrix.scale(sx, sy);
                array.copy(matrix);
                getOpStack().push(array);
            } else {
                sy = obj.toReal();
                sx = getOpStack().pop().toReal();
                getGstate().current().getCtm().scale(sx, sy);
                getGstate().current().updatePosition();
            }
        }
    }
    
    /**
     * PostScript operator: scalefont.
     */
    public class Oscalefont extends PSObjectOperator {
        /**
         * Invokes this operator.
         * 
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            double scale = getOpStack().pop().toReal();
            
            // "font scale scalefont" is equivalent to 
            // "font [scale 0 0 scale 0 0] makefont""
            executeOperator("[");
            getOpStack().push(new PSObjectReal(scale));
            getOpStack().push(new PSObjectReal(0));
            getOpStack().push(new PSObjectReal(0));
            getOpStack().push(new PSObjectReal(scale));
            getOpStack().push(new PSObjectReal(0));
            getOpStack().push(new PSObjectReal(0));
            executeOperator("]");
            executeOperator("makefont");
        }
    }
    
    /**
     * PostScript op: search.
     */
    public class Osearch extends PSObjectOperator {
        /**
         * Invokes this operator.
         * 
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            PSObjectString seekObj = getOpStack().pop().toPSString();
            seekObj.checkAccess(false, true, false);
            PSObjectString string = getOpStack().pop().toPSString();
            string.checkAccess(false, true, false);
            
            String seek = seekObj.toString();
            List<PSObject> result = string.search(seek);
            while (!result.isEmpty()) {
                getOpStack().push(result.remove(0));
            }
        }
    }
    
    /**
     * PostScript op: selectfont.
     */
    public class Oselectfont extends PSObjectOperator {
        /**
         * Invokes this operator.
         * 
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            PSObject obj = getOpStack().pop();
            executeOperator("findfont");
            getOpStack().push(obj);
            if ((obj instanceof PSObjectInt) || (obj instanceof PSObjectReal)) {
                executeOperator("scalefont");
            } else if (obj instanceof PSObjectArray) {
                executeOperator("makefont");
            } else {
                throw new PSErrorTypeCheck();
            }
            executeOperator("setfont");
        }
    }
    
    /**
     * PostScript op: setblackgeneration.
     */
    public class Osetblackgeneration extends PSObjectOperator {
        /**
         * Invokes this operator.
         * 
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            PSObjectArray proc = getOpStack().pop().toProc();
            getGstate().current().setBlackGeneration(proc);
        }
    }
    
    /**
     * PostScript op: setcachedevice.
     */
    public class Osetcachedevice extends PSObjectOperator {
        /**
         * Invokes this operator.
         * 
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            double ury = getOpStack().pop().toReal();
            double urx = getOpStack().pop().toReal();
            double lly = getOpStack().pop().toReal();
            double llx = getOpStack().pop().toReal();
            double wy = getOpStack().pop().toReal();
            double wx = getOpStack().pop().toReal();
            getGstate().current().setDevice(new CacheDevice(wx, wy, llx, lly,
                    urx, ury, getVm()));
            getGstate().current().initmatrix();
        }
    }
    
    /**
     * PostScript op: setcachedevice2.
     */
    public class Osetcachedevice2 extends PSObjectOperator {
        /**
         * Invokes this operator.
         * 
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            getOpStack().pop(); // pop vy
            getOpStack().pop(); // pop vx
            getOpStack().pop(); // pop w1y
            getOpStack().pop(); // pop w1x
            double ury = getOpStack().pop().toReal();
            double urx = getOpStack().pop().toReal();
            double lly = getOpStack().pop().toReal();
            double llx = getOpStack().pop().toReal();
            double w0y = getOpStack().pop().toReal();
            double w0x = getOpStack().pop().toReal();
            getGstate().current().setDevice(
                    new CacheDevice(w0x, w0y, llx, lly, urx, ury, getVm()));
            getGstate().current().initmatrix();
        }
    }
    
    /**
     * PostScript op: setcmykcolor.
     */
    public class Osetcmykcolor extends PSObjectOperator {
        /**
         * Invokes this operator.
         * 
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            double k = getOpStack().pop().toReal();
            double y = getOpStack().pop().toReal();
            double m = getOpStack().pop().toReal();
            double c = getOpStack().pop().toReal();
            double[] cmykValues = {c, m, y, k};
            getGstate().current().setcolorspace(
                    new PSObjectName("DeviceCMYK", true));
            getGstate().current().setcolor(cmykValues);
        }
    }
    
    /**
     * PostScript op: setcolor.
     */
    public class Osetcolor extends PSObjectOperator {
        /**
         * Invokes this operator.
         * 
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            int n = getGstate().current().getColor().getNrInputValues();
            double[] newColor = new double[n];
            for (int i = 0; i < n; i++) {
                newColor[n - i - 1] = getOpStack().pop().toReal();
            }
            getGstate().current().setcolor(newColor);
        }
    }
    
    /**
     * PostScript op: setcolorrendering.
     */
    public class Osetcolorrendering extends PSObjectOperator {
        /**
         * Invokes this operator.
         * 
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            PSObjectDict dict = getOpStack().pop().toDict();
            getGstate().current().setColorRendering(dict);
        }
    }
   
    /**
     * PostScript op: setcolorspace.
     */
    public class Osetcolorspace extends PSObjectOperator {
        /**
         * Invokes this operator.
         * 
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            PSObject arrayOrName = getOpStack().pop();
            getGstate().current().setcolorspace(arrayOrName);
        }
    }
    
    /**
     * PostScrip op: setcolortransfer.
     */
    public class Osetcolortransfer extends PSObjectOperator {
        /**
         * Invokes this operator.
         * 
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            PSObjectArray grayProc = getOpStack().pop().toProc();
            PSObjectArray blueProc = getOpStack().pop().toProc();
            PSObjectArray greenProc = getOpStack().pop().toProc();
            PSObjectArray redProc = getOpStack().pop().toProc();
            getGstate().current().setColorTransfer(redProc, greenProc, blueProc,
                    grayProc);
        }
    }
   
    /**
     * PostScript op: setdash.
     */
    public class Osetdash extends PSObjectOperator {
        /**
         * Invokes this operator.
         * 
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            double offset = getOpStack().pop().toReal();
            PSObjectArray array = getOpStack().pop().toArray();
            
            getGstate().current().setDashPattern(array);
            getGstate().current().setDashOffset(offset);
        }
    }
    
    /**
     * PostScript op: setdevparams.
     */
    public class Osetdevparams extends PSObjectOperator {
        /**
         * Invokes this operator.
         * 
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            PSObjectDict dict = getOpStack().pop().toDict();
            String device = getOpStack().pop().toPSString().toString();
            getInterpParams().setDeviceParams(device, dict);
        }
    }
    
    /**
     * PostScript op: setflat.
     */
    public class Osetflat extends PSObjectOperator {
        /**
         * Invokes this operator.
         * 
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            double num = getOpStack().pop().toReal();
            num = Math.max(num, 0.2);
            num = Math.min(num, 100);
            getGstate().current().setFlatness(num);
        }
    }
   
    /**
     * PostScript op: setfont.
     */
    public class Osetfont extends PSObjectOperator {
        /**
         * Invokes this operator.
         * 
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            PSObjectFont font = getOpStack().pop().toFont();
            getGstate().current().setFont(font);
        }
    }
    
    /**
     * PostScript op: setglobal.
     */
    public class Osetglobal extends PSObjectOperator {
        /**
         * Invokes this operator.
         * 
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            boolean bool = getOpStack().pop().toBool();
            getVm().setGlobal(bool);
        }
    }
    
    /**
     * PostScript op: setgray.
     */
    public class Osetgray extends PSObjectOperator {
        /**
         * Invokes this operator.
         * 
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            double[] num = { getOpStack().pop().toReal() };
            getGstate().current().setcolorspace(
                    new PSObjectName("DeviceGray", true));
            getGstate().current().setcolor(num);
        }
    }
    
    /**
     * PostScript op: sethalftone.
     */
    public class Osethalftone extends PSObjectOperator {
        /**
         * Invokes this operator.
         * 
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            PSObject halftone = getOpStack().pop();
            getGstate().current().setHalftone(halftone);
        }
    }
    
    /**
     * PostScript op: sethsbcolor.
     */
    public class Osethsbcolor extends PSObjectOperator {
        /**
         * Invokes this operator.
         * 
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            double brightness = getOpStack().pop().toReal();
            double saturaration = getOpStack().pop().toReal();
            double hue = getOpStack().pop().toReal();
            double[] rgbValues = DeviceRGB.convertHSBtoRGB(
                    hue, saturaration, brightness);
            getGstate().current().setcolorspace(
                    new PSObjectName("DeviceRGB", true));
            getGstate().current().setcolor(rgbValues);
        }
    }
   
    /**
     * PostScript op: setlinecap.
     */
    public class Osetlinecap extends PSObjectOperator {
        /**
         * Invokes this operator.
         * 
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            int cap = getOpStack().pop().toNonNegInt();
            getGstate().current().setLineCap(cap);
        }
    }
   
    /**
     * PostScript op: setlinejoin.
     */
    public class Osetlinejoin extends PSObjectOperator {
        /**
         * Invokes this operator.
         * 
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            int join = getOpStack().pop().toNonNegInt();
            getGstate().current().setLineJoin(join);
        }
    }
   
    /**
     * PostScript op: setlinewidth.
     */
    public class Osetlinewidth extends PSObjectOperator {
        /**
         * Invokes this operator.
         * 
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            double lineWidth = getOpStack().pop().toReal();
            getGstate().current().setLineWidth(Math.abs(lineWidth));
        }
    }
   
    /**
     * PostScript op: setmatrix.
     */
    public class Osetmatrix extends PSObjectOperator {
        /**
         * Invokes this operator.
         * 
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            Matrix matrix = getOpStack().pop().toArray().toMatrix();
            getGstate().current().getCtm().copy(matrix);
            getGstate().current().updatePosition();
        }
    }
    
    /**
     * PostScript op: setmiterlimit.
     */
    public class Osetmiterlimit extends PSObjectOperator {
        /**
         * Invokes this operator.
         * 
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            double num = getOpStack().pop().toReal();
            if (num < 1.0) {
                throw new PSErrorRangeCheck();
            }
            getGstate().current().setMiterLimit(num);
        }
    }
    
    /**
     * PostScript op: setoverprint.
     */
    public class Osetoverprint extends PSObjectOperator {
        /**
         * Invokes this operator.
         * 
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            boolean overprint = getOpStack().pop().toBool();
            getGstate().current().setOverprint(overprint);
        }
    }
    
    /**
     * PostScript op: setpacking.
     */
    public class Osetpacking extends PSObjectOperator {
        /**
         * Invokes this operator.
         * 
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            boolean bool = getOpStack().pop().toBool();
            PSObjectDict dict = getDictStack().lookup("userdict").toDict();
            dict.setKey("currentpacking", new PSObjectBool(bool));
        }
    }
    
    /**
     * PostScript op: setpagedevice.
     */
    public class Osetpagedevice extends PSObjectOperator {
        /**
         * Invokes this operator.
         * 
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            // Just pop the dictionary from the stack and don't do anything with
            // it.
            getOpStack().pop().toDict();
        }
    }
    
    /**
     * PostScript op: setrgbcolor.
     */
    public class Osetrgbcolor extends PSObjectOperator {
        /**
         * Invokes this operator.
         * 
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            double blue = getOpStack().pop().toReal();
            double green = getOpStack().pop().toReal();
            double red = getOpStack().pop().toReal();
            double[] rgbValues = {red, green, blue};
            getGstate().current().setcolorspace(
                    new PSObjectName("DeviceRGB", true));
            getGstate().current().setcolor(rgbValues);
        }
    }
    
    /**
     * PostScript op: setscreen.
     */
    public class Osetscreen extends PSObjectOperator {
        /**
         * Invokes this operator.
         * 
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            // This operator does not have any meaning in Eps2pgf. It just pops
            // the arguments and continues.
            getOpStack().pop();  // pop proc/halftone
            getOpStack().pop();  // pop angle
            getOpStack().pop();  // pop frequency
        }
    }
    
    /**
     * PostScript op: setsmoothness.
     */
    public class Osetsmoothness extends PSObjectOperator {
        /**
         * Invokes this operator.
         * 
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            double smoothness = getOpStack().pop().toReal();
            getGstate().current().setSmoothness(smoothness);
        }
    }
    
    /**
     * PostScript op: setstrokeadjust.
     */
    public class Osetstrokeadjust extends PSObjectOperator {
        /**
         * Invokes this operator.
         * 
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            boolean bool = getOpStack().pop().toBool();
            getGstate().current().setStrokeAdjust(bool);
        }
    }
    
    /**
     * PostScript op: setsystemparams.
     */
    public class Osetsystemparams extends PSObjectOperator {
        /**
         * Invokes this operator.
         * 
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            PSObjectDict dict = getOpStack().pop().toDict();
            getInterpParams().setSystemParams(dict);
        }
    }
    
    /**
     * PostScript op: settransfer.
     */
    public class Osettransfer extends PSObjectOperator {
        /**
         * Invokes this operator.
         * 
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            PSObjectArray proc = getOpStack().pop().toProc();
            getGstate().current().setTransfer(proc);
        }
    }
    
    /**
     * PostScript op: setundercolorremoval.
     */
    public class Osetundercolorremoval extends PSObjectOperator {
        /**
         * Invokes this operator.
         * 
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            PSObjectArray proc = getOpStack().pop().toProc();
            getGstate().current().setUndercolorRemoval(proc);
        }
    }
    
    /**
     * PostScript op: setuserparams.
     */
    public class Osetuserparams extends PSObjectOperator {
        /**
         * Invokes this operator.
         * 
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            PSObjectDict dict = getOpStack().pop().toDict();
            getInterpParams().setUserParams(dict);
        }
    }
   
    /**
     * PostScript op: shfill.
     */
    public class Oshfill extends PSObjectOperator {
        /**
         * Invokes this operator.
         * 
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            PSObjectDict dict = getOpStack().pop().toDict();
            getGstate().current().getDevice().shfill(
                    dict, getGstate().current());
        }
    }
    
    /**
     * PostScript op: show.
     */
    public class Oshow extends PSObjectOperator {
        /**
         * Invokes this operator.
         * 
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            PSObjectString string = getOpStack().pop().toPSString();
            double[] dpos = getTextHandler().showText(
                    getGstate().current().getDevice(), string);
            getGstate().current().rmoveto(dpos[0], dpos[1]);
        }
    }
    
    /**
     * PostScript op: showpage.
     */
    public class Oshowpage extends PSObjectOperator {
        /**
         * Invokes this operator.
         * 
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            // This operator has no meaning in eps2pgf
        }
    }
    
    /**
     * PostScript op: sin.
     */
    public class Osin extends PSObjectOperator {
        /**
         * Invokes this operator.
         * 
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            double angle = getOpStack().pop().toReal();
            getOpStack().push(new PSObjectReal(
                    Math.sin(angle * Math.PI / 180.0)));
        }
    }
    
    /**
     * PostScript op: sqrt.
     */
    public class Osqrt extends PSObjectOperator {
        /**
         * Invokes this operator.
         * 
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            double x = getOpStack().pop().toNonNegReal();
            x = Math.sqrt(x);
            getOpStack().push(new PSObjectReal(x));
        }
    }
    
    /**
     * PostScript op: status.
     */
    public class Ostatus extends PSObjectOperator {
        /**
         * Invokes this operator.
         * 
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            PSObject obj = getOpStack().pop();
            if (obj instanceof PSObjectFile) {
                boolean status = ((PSObjectFile) obj).status();
                getOpStack().push(new PSObjectBool(status));
            } else {
                throw new PSErrorUnregistered("'status' operator of non-file"
                        + " object.");
            }
        }
    }
   
    /**
     * PostScript op: stop.
     */
    public class Ostop extends PSObjectOperator {
        /**
         * Invokes this operator.
         * 
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            OperatorsEps2pgf opse = getOpsEps2pgf();
            ExecStack es = getExecStack();
            PSObject obj;
            while ((obj = es.pop()) != null) {
                if (obj == opse.eps2pgfStopped) {
                    // Set 'newerror' in $error to indicate that an "error"
                    // occurred.
                    try {
                        PSObjectDict dollarError =
                            getDictStack().lookup("$error").toDict();
                        dollarError.setKey("newerror", true);
                    } catch (PSErrorTypeCheck e) {
                        throw new ProgramError("$error is not a dictionary.");
                    }
    
                    // Push eps2pgfStopped operator back on the execution stack
                    // so that it gets executed.
                    es.push(obj);
                    break;
                } else if (opse.isContinuationFunction(obj)) {
                    // Pop continuation stack down to a Null object. These are
                    // the arguments for the continuation function just popped
                    // from the execution stack.
                    ArrayStack<PSObject> cs = getContStack();
                    try {
                        while (true) {
                            if (cs.pop() instanceof PSObjectNull) {
                                break;
                            }
                        }
                    } catch (PSErrorStackUnderflow e) {
                        throw new ProgramError("No null object found on"
                                + " continuation stack.");
                    }
                }
            }
        }
    }
    
    /**
     * PostScript op: stopped.
     */
    public class Ostopped extends PSObjectOperator {
        /**
         * Invokes this operator.
         * 
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            // Push proc and continuation function on stack
            getExecStack().push(getOpsEps2pgf().eps2pgfStopped);
            getExecStack().push(getOpStack().pop());
    
            // Push arguments of continuation function on c stack
            getContStack().push(new PSObjectNull());
        }
    }
    
    /**
     * PostScript op: string.
     */
    public class Ostring extends PSObjectOperator {
        /**
         * Invokes this operator.
         * 
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            int n = getOpStack().pop().toNonNegInt();
            getOpStack().push(new PSObjectString(n, getInterp()));
        }
    }
   
    /**
     * PostScript op: stringwidth.
     */
    public class Ostringwidth extends PSObjectOperator {
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
            
            double[] dpos = getTextHandler().showText(
                    getGstate().current().getDevice(), string, true);
            getOpStack().push(new PSObjectReal(dpos[0]));
            getOpStack().push(new PSObjectReal(dpos[1]));
        }
    }

    /**
     * PostScript op: stroke.
     */
    public class Ostroke extends PSObjectOperator {
        /**
         * Invokes this operator.
         * 
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            getGstate().current().getDevice().stroke(getGstate().current());
            executeOperator("newpath");
        }
    }
   
    /**
     * PostScript op: store.
     */
    public class Ostore extends PSObjectOperator {
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
            
            PSObject dictWithKey = getDictStack().where(key);
            if (dictWithKey != null) {
                getDictStack().where(key).checkAccess(false, false, true);
            }
            
            getDictStack().store(key, value);
        }
    }

    /**
     * PostScript op: sub.
     */
    public class Osub extends PSObjectOperator {
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
            getOpStack().push(num1.sub(num2));
        }
    }
    
}
