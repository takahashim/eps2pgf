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

import net.sf.eps2pgf.ProgramError;
import net.sf.eps2pgf.ps.errors.PSError;
import net.sf.eps2pgf.ps.errors.PSErrorIOError;
import net.sf.eps2pgf.ps.errors.PSErrorRangeCheck;
import net.sf.eps2pgf.ps.errors.PSErrorUnmatchedMark;
import net.sf.eps2pgf.ps.errors.PSErrorUnregistered;
import net.sf.eps2pgf.ps.objects.PSObject;
import net.sf.eps2pgf.ps.objects.PSObjectArray;
import net.sf.eps2pgf.ps.objects.PSObjectBool;
import net.sf.eps2pgf.ps.objects.PSObjectDict;
import net.sf.eps2pgf.ps.objects.PSObjectFile;
import net.sf.eps2pgf.ps.objects.PSObjectInt;
import net.sf.eps2pgf.ps.objects.PSObjectMark;
import net.sf.eps2pgf.ps.objects.PSObjectNull;
import net.sf.eps2pgf.ps.objects.PSObjectOperator;
import net.sf.eps2pgf.ps.objects.PSObjectReal;
import net.sf.eps2pgf.ps.objects.PSObjectString;
import net.sf.eps2pgf.ps.resources.colors.DeviceCMYK;
import net.sf.eps2pgf.ps.resources.colors.DeviceGray;
import net.sf.eps2pgf.ps.resources.colors.DeviceRGB;
import net.sf.eps2pgf.ps.resources.colors.PSColor;


/**
 * Contains the operator functions for operators starting with letter A to C.
 */
public final class OperatorsAtoC extends OperatorContainer {
    
    /**
     * Create a new set of operators and add them to the system dictionary of
     * the interpreter.
     * 
     * @param interpreter The interpreter
     * 
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    public OperatorsAtoC(final Interpreter interpreter) throws ProgramError {
        super(interpreter);
    }
    
    /**
     * PostScript op: abs.
     */
    public class Oabs extends PSObjectOperator {
        /**
         * Invokes this operator.
         * 
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            PSObject obj = osPop();
            osPush(obj.abs());
        }
    }
    
    /**
     * PostScript op: add.
     */
    public class Oadd extends PSObjectOperator {
        
        /**
         * Invokes this operator.
         * 
         * @throws PSError A PostScript error occurred.
         */
        @Override
        public void invoke() throws PSError {
            PSObject num2 = osPop();
            PSObject num1 = osPop();
            osPush(num1.add(num2));
        }
    }
    
    /**
     * PostScript op: aload.
     */
    public class Oaload extends PSObjectOperator {
        /**
         * Invokes this operator.
         *
         * @throws PSError A PostScript error occurred.
         */
        @Override
        public void invoke() throws PSError {
            PSObject array = osPop();
            array.checkAccess(false, true, false);
            for (PSObject obj : array.toArray()) {
                osPush(obj);
            }
            osPush(array);
        }
    }


    /**
     * PostScript op: anchorsearch.
     */
    public class Oanchorsearch extends PSObjectOperator {
        /**
         * Invokes this operator.
         *
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            PSObjectString seekObj = osPop().toPSString();
            seekObj.checkAccess(false, true, false);
            PSObjectString string = osPop().toPSString();
            seekObj.checkAccess(false, true, false);
            
            String seek = seekObj.toString();
            List<PSObject> result = string.anchorsearch(seek);
            while (!result.isEmpty()) {
                osPush(result.remove(0));
            }
        }
    }

    /**
     * PostScript op: and.
     */
    public class Oand extends PSObjectOperator {
        /**
         * Invokes this operator.
         *
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            PSObject obj2 = osPop();
            PSObject obj1 = osPop();
            osPush(obj1.and(obj2));
        }
    }

    /**
     * PostScript op: arc.
     */
    public class Oarc extends PSObjectOperator {
        /**
         * Invokes this operator.
         *
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            double angle2 = osPop().toReal();
            double angle1 = osPop().toReal();
            double r = osPop().toReal();
            double y = osPop().toReal();
            double x = osPop().toReal();
            gsCurrent().arc(x, y, r, angle1, angle2, true);
        }
    }

    /**
     * PostScript op: arcn.
     */
    public class Oarcn extends PSObjectOperator {
        /**
         * Invokes this operator.
         *
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            double angle2 = osPop().toReal();
            double angle1 = osPop().toReal();
            double r = osPop().toNonNegReal();
            double y = osPop().toReal();
            double x = osPop().toReal();
            gsCurrent().arc(x, y, r, angle1, angle2, false);
        }
    }

    /**
     * PostScript op: acrt.
     */
    public class Oarct extends PSObjectOperator {
        /**
         * Invokes this operator.
         *
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            double r = osPop().toNonNegReal();
            double y2 = osPop().toReal();
            double x2 = osPop().toReal();
            double y1 = osPop().toReal();
            double x1 = osPop().toReal();
            gsCurrent().arcto(x1, y1, x2, y2, r);
        }
    }

    /**
     * PostScript op: acrto.
     */
    public class Oarcto extends PSObjectOperator {
        /**
         * Invokes this operator.
         *
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            double r = osPop().toNonNegReal();
            double y2 = osPop().toReal();
            double x2 = osPop().toReal();
            double y1 = osPop().toReal();
            double x1 = osPop().toReal();
            double[] t1t2 = gsCurrent().arcto(x1, y1, x2, y2, r);
            for (int i = 0; i < t1t2.length; i++) {
                osPush(new PSObjectReal(t1t2[i]));
            }
        }
    }

    /**
     * PostScript op: array.
     */
    public class Oarray extends PSObjectOperator {
        /**
         * Invokes this operator.
         *
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            int n = osPop().toNonNegInt();
            executeOperator("[");
            PSObjectNull nullObj = new PSObjectNull();
            for (int i = 0; i < n; i++) {
                osPush(nullObj);
            }
            executeOperator("]");
        }
    }

    /**
     * PostScript op: ashow.
     */
    public class Oashow extends PSObjectOperator {
        /**
         * Invokes this operator.
         *
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            PSObjectString string = osPop().toPSString();
            string.checkAccess(false, true, false);
            osPop().toReal(); // read ay
            osPop().toReal();  // read ax
            osPush(string);
            getInterp().executeOperator("show");
        }
    }

    /**
     * PostScript op: astore.
     */
    public class Oastore extends PSObjectOperator {
        /**
         * Invokes this operator.
         *
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            PSObjectArray array = osPop().toArray();
            array.checkAccess(false, true, true);
            int n = array.size();
            try {
                for (int i = (n - 1); i >= 0; i--) {
                    array.put(i, osPop());
                }
            } catch (PSErrorRangeCheck e) {
                // due to the for-loop getInterp().can never happen
            }
            osPush(array);
        }
    }

    /**
     * PostScript op: atan.
     */
    public class Oatan extends PSObjectOperator {
        /**
         * Invokes this operator.
         *
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            double den = osPop().toReal();
            double num = osPop().toReal();
            double result = Math.atan2(num, den) / Math.PI * 180;
            // Java atan method returns in range -180 to 180, while the
            // PostScript function should return in range 0-360
            result = (result + 360.0) % 360.0;
            osPush(new PSObjectReal(result));
        }
    }

    /**
     * PostScript op: awidthshow.
     */
    public class Oawidthshow extends PSObjectOperator {
        /**
         * Invokes this operator.
         *
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            throw new PSErrorUnregistered("awidthshow operator");
        }
    }

    /**
     * PostScript op: begin.
     */
    public class Obegin extends PSObjectOperator {
        /**
         * Invokes this operator.
         *
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            PSObjectDict dict = osPop().toDict();
            dict.checkAccess(true, true, false);
            getDictStack().pushDict(dict);
        }
    }

    /**
     * PostScript op: bind.
     */
    public class Obind extends PSObjectOperator {
        /**
         * Invokes this operator.
         *
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            getOpStack().peek().toArray().bind(getInterp());
        }
    }

    /**
     * PostScript op: bitshift.
     */
    public class Obitshift extends PSObjectOperator {
        /**
         * Invokes this operator.
         *
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            int shift = osPop().toInt();
            int int1 = osPop().toInt();
            if (shift >= 0) {
                int1 <<= shift;
            } else {
                int1 >>>= -shift;
            }
            osPush(new PSObjectInt(int1));
        }
    }

    /**
     * PostScript op: ceiling.
     */
    public class Oceiling extends PSObjectOperator {
        /**
         * Invokes this operator.
         *
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            PSObject obj = osPop();
            osPush(obj.ceiling());
        }
    }

    /**
     * PostScript op: charpath.
     * Eps2pgf does currently not come with the fonts (only with the metrics
     * describing the outlines). So, instead of the fonts themselves, the
     * bounding box is added.
     */
    public class Ocharpath extends PSObjectOperator {
        /**
         * Invokes this operator.
         *
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            osPop().toBool();  // bool
            PSObjectString string = osPop().toPSString();
            getInterp().getTextHandler().charpath(string);
        }
    }

    /**
     * PostScript op: clear.
     */
    public class Oclear extends PSObjectOperator {
        /**
         * Invokes this operator.
         *
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            getOpStack().clear();
        }
    }

    /**
     * PostScript op: cleardictstack.
     */
    public class Ocleardictstack extends PSObjectOperator {
        /**
         * Invokes this operator.
         *
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            getDictStack().cleardictstack();
        }
    }

    /**
     * PostScript op: cleartomark.
     */
    public class Ocleartomark extends PSObjectOperator {
        /**
         * Invokes this operator.
         *
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            int n = getOpStack().size();
            for (int i = 0; i < n; i++) {
                if (osPop() instanceof PSObjectMark) {
                    return;
                }
            }
            throw new PSErrorUnmatchedMark();
        }
    }

    /**
     * PostScript op: clip.
     */
    public class Oclip extends PSObjectOperator {
        /**
         * Invokes this operator.
         *
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            gsCurrent().clip();
            gsCurrent().getDevice().clip(gsCurrent().getClippingPath());
        }
    }

    /**
     * PostScript op: clippath.
     */
    public class Oclippath extends PSObjectOperator {
        /**
         * Invokes this operator.
         *
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            gsCurrent().setPath(
                    gsCurrent().getClippingPath().clone());
        }
    }

    /**
     * PostScript op: cliprestore.
     */
    public class Ocliprestore extends PSObjectOperator {
        /**
         * Invokes this operator.
         *
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            throw new PSErrorUnregistered("PostScript operator 'cliprestore'");
        }
    }

    /**
     * PostScript op: clipsave.
     */
    public class Oclipsave extends PSObjectOperator {
        /**
         * Invokes this operator.
         *
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            throw new PSErrorUnregistered("PostScript operator 'clipsave'");
        }
    }

    /**
     * PostScript op: closefile.
     */
    public class Oclosefile extends PSObjectOperator {
        /**
         * Invokes this operator.
         *
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            PSObjectFile file = osPop().toFile();
            file.closeFile();
        }
    }

    /**
     * PostScript op: closepath.
     */
    public class Oclosepath extends PSObjectOperator {
        /**
         * Invokes this operator.
         *
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            double[] startPos = gsCurrent().getPath().closepath();
            if (startPos != null) {
                gsCurrent().moveto(startPos[0], startPos[1]);
            }
        }
    }

    /**
     * PostScript op: colorimage.
     */
    public class Ocolorimage extends PSObjectOperator {
        /**
         * Invokes this operator.
         *
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            // Number of color components
            PSColor colorSpace;
            int ncomp = osPop().toInt();
            if (ncomp == 1) {
                colorSpace = new DeviceGray();
            } else if (ncomp == 3) {
                colorSpace = new DeviceRGB();
            } else if (ncomp == 4) {
                colorSpace = new DeviceCMYK();
            } else {
                throw new PSErrorRangeCheck();
            }
            
            // Does it have multiple data sources?
            boolean multi = osPop().toBool();
            
            // Get the data source(s)
            PSObject dataSource;
            if (multi) {
                PSObject[] sources = new PSObject[ncomp];
                for (int i = (ncomp - 1); i >= 0; i++) {
                    sources[i] = osPop();
                }
                dataSource = new PSObjectArray(sources, getInterp());
            } else {
                dataSource = osPop();
            }
            
            // Read last four arguments
            PSObjectArray matrix = osPop().toArray();
            Matrix.checkArray(matrix);
            int bitsPerComponent = osPop().toInt();
            int height = osPop().toInt();
            int width = osPop().toInt();
            
            // Construct an image dictionary
            PSObjectDict dict = new PSObjectDict(getInterp());
            dict.setKey(Image.IMAGE_TYPE, new PSObjectInt(1));
            dict.setKey(Image.WIDTH, new PSObjectInt(width));
            dict.setKey(Image.HEIGHT, new PSObjectInt(height));
            dict.setKey(Image.IMAGE_MATRIX, matrix);
            dict.setKey(Image.DATA_SOURCE, dataSource);
            dict.setKey(Image.BITS_PER_COMPONENT,
                    new PSObjectInt(bitsPerComponent));        
            double[] decode = new double[2 * ncomp];
            for (int i = 0; i < ncomp; i++) {
                decode[2 * i] = 0.0;
                decode[2 * i + 1] = 1.0;
            }
            dict.setKey(Image.DECODE, new PSObjectArray(decode, getInterp()));
            
            Image image = new Image(dict, getInterp(), colorSpace);
            gsCurrent().getDevice().image(image);
        }
    }

    /**
     * PostScript op: concat.
     */
    public class Oconcat extends PSObjectOperator {
        /**
         * Invokes this operator.
         *
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            Matrix matrix = osPop().toArray().toMatrix();
            gsCurrent().getCtm().concat(matrix);
            gsCurrent().updatePosition();
        }
    }

    /**
     * PostScript op: concantmatrix.
     */
    public class Oconcatmatrix extends PSObjectOperator {
        /**
         * Invokes this operator.
         *
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            PSObjectArray array3 = osPop().toArray();
            Matrix matrix2 = osPop().toArray().toMatrix();
            Matrix matrix1 = osPop().toArray().toMatrix();
            matrix2.concat(matrix1);
            array3.copy(matrix2);
            osPush(array3);
        }
    }

    /**
     * PostScript op: copy.
     */
    public class Ocopy extends PSObjectOperator {
        /**
         * Invokes this operator.
         *
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            PSObject obj = osPop();
            if (obj instanceof PSObjectInt) {
                // Get n, the number of copies to make
                int n = obj.toNonNegInt();
                int stackSize = getOpStack().size();
            
                for (int i = stackSize - n; i < stackSize; i++) {
                    osPush(getOpStack().get(i));
                }
            } else {
                PSObject obj1 = osPop();
                obj.checkAccess(false, false, true);
                obj1.checkAccess(false, true, false);
                PSObject subseq = obj.copy(obj1);
                osPush(subseq);
            }
        }
    }

    /**
     * PostScript op: cos.
     */
    public class Ocos extends PSObjectOperator {
        /**
         * Invokes this operator.
         *
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            double angle = osPop().toReal();
            osPush(new PSObjectReal(Math.cos(angle * Math.PI / 180.0)));
        }
    }

    /**
     * PostScript op: count.
     */
    public class Ocount extends PSObjectOperator {
        /**
         * Invokes this operator.
         *
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            int count = getOpStack().size();
            PSObjectInt n = new PSObjectInt(count);
            osPush(n);
        }
    }

    /**
     * PostScript op: countdictstack.
     */
    public class Ocountdictstack extends PSObjectOperator {
        /**
         * Invokes this operator.
         *
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            osPush(new PSObjectInt(getDictStack().countdictstack()));
        }
    }

    /**
     * PostScript op: countexecstack.
     */
    public class Ocountexecstack extends PSObjectOperator {
        /**
         * Invokes this operator.
         *
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            osPush(new PSObjectInt(getExecStack().size()));
        }
    }

    /**
     * PostScript op: counttomark.
     */
    public class Ocounttomark extends PSObjectOperator {
        /**
         * Invokes this operator.
         *
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            int n = getOpStack().size();
            for (int i = n - 1; i >= 0; i--) {
                if (getOpStack().get(i) instanceof PSObjectMark) {
                    osPush(new PSObjectInt(n - 1 - i));
                    return;
                }
            }
            throw new PSErrorUnmatchedMark();
        }
    }

    /**
     * PostScript op: currentblackgeneration.
     */
    public class Ocurrentblackgeneration extends PSObjectOperator {
        /**
         * Invokes this operator.
         *
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            osPush(gsCurrent().currentBlackGeneration());
        }
    }

    /**
     * PostScript op: currentcmykcolor.
     */
    public class Ocurrentcmykcolor extends PSObjectOperator {
        /**
         * Invokes this operator.
         *
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            double[] cmyk = gsCurrent().currentcmykcolor();
            for (int i = 0; i < cmyk.length; i++) {
                osPush(new PSObjectReal(cmyk[i]));
            }
        }
    }

    /**
     * PostScript op: currentcolor.
     */
    public class Ocurrentcolor extends PSObjectOperator {
        /**
         * Invokes this operator.
         *
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            PSColor color = gsCurrent().getColor();
            for (int i = 0; i < color.getNrComponents(); i++) {
                osPush(new PSObjectReal(color.getLevel(i)));
            }
        }
    }

    /**
     * PostScript op: currentcolorrendering.
     */
    public class Ocurrentcolorrendering extends PSObjectOperator {
        /**
         * Invokes this operator.
         *
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            osPush(gsCurrent().currentColorRendering());
        }
    }

    /**
     * PostScript op: currentcolorspace.
     */
    public class Ocurrentcolorspace extends PSObjectOperator {
        /**
         * Invokes this operator.
         *
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            osPush(gsCurrent().getColor().getColorSpace(getInterp()));
        }
    }

    /**
     * PostScript op: currentcolortransfer.
     */
    public class Ocurrentcolortransfer extends PSObjectOperator {
        /**
         * Invokes this operator.
         *
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            PSObjectArray procs = gsCurrent().currentColorTransfer();
            try {
                for (int i = 0; i < 4; i++) {
                    osPush(procs.get(i));
                }
            } catch (PSErrorRangeCheck e) {
                throw new ProgramError("rangecheck in currentcolortransfer()");
            }
        }
    }

    /**
     * PostScript op: currentdevparams.
     */
    public class Ocurrendevparams extends PSObjectOperator {
        /**
         * Invokes this operator.
         *
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            String device = osPop().toPSString().toString();
            osPush(getInterpParams().currentDeviceParams(device));
        }
    }

    /**
     * PostScript op: currentdict.
     */
    public class Ocurrentdict extends PSObjectOperator {
        /**
         * Invokes this operator.
         *
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            osPush(getDictStack().peekDict());
        }
    }

    /**
     * PostScript op: currentdash.
     */
    public class Ocurrentdash extends PSObjectOperator {
        /**
         * Invokes this operator.
         *
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            List<Double> pattern = gsCurrent().getDashPattern();
            PSObjectArray arr = new PSObjectArray(pattern.size(), getInterp());
            for (int i = 0; i < pattern.size(); i++) {
                arr.put(i, new PSObjectReal(pattern.get(i)));
            }
            osPush(arr);
            osPush(new PSObjectReal(gsCurrent().getDashOffset()));
        }
    }

    /**
     * PostScript op: currentfile.
     */
    public class Ocurrentfile extends PSObjectOperator {
        /**
         * Invokes this operator.
         *
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            PSObjectFile file = getExecStack().getTopmostFile();
            if (file == null) {
                file = new PSObjectFile(null, getInterp());
            } else {
                file = file.dup();
            }
            file.cvlit();
            osPush(file);
        }
    }

    /**
     * PostScript op: currentflat.
     */
    public class Ocurrentflat extends PSObjectOperator {
        /**
         * Invokes this operator.
         *
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            double flat = gsCurrent().currentFlatness();
            osPush(new PSObjectReal(flat));
        }
    }

    /**
     * PostScript op: currentglobal.
     */
    public class Ocurrentglobal extends PSObjectOperator {
        /**
         * Invokes this operator.
         *
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            PSObjectBool bool = new PSObjectBool(getVm().currentGlobal());
            osPush(bool);
        }
    }

    /**
     * PostScript op: currentgray.
     */
    public class Ocurrentgray extends PSObjectOperator {
        /**
         * Invokes this operator.
         *
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            double gray = gsCurrent().currentgray();
            osPush(new PSObjectReal(gray));
        }
    }

    /**
     * PostScript op: currenthalftone.
     */
    public class Ocurrenthalftone extends PSObjectOperator {
        /**
         * Invokes this operator.
         *
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            osPush(gsCurrent().currentHalftone());
        }
    }

    /**
     * PostScript op: currenthsbcolor.
     */
    public class Ocurrenthsbcolor extends PSObjectOperator {
        /**
         * Invokes this operator.
         *
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            double[] hsb = gsCurrent().currenthsbcolor();
            for (int i = 0; i < hsb.length; i++) {
                osPush(new PSObjectReal(hsb[i]));
            }
        }
    }

    /**
     * PostScript op: currentlinewidth.
     */
    public class Ocurrentlinewidth extends PSObjectOperator {
        /**
         * Invokes this operator.
         *
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            osPush(new PSObjectReal(gsCurrent().getLineWidth()));
        }
    }

    /**
     * PostScript op: currentmatrix.
     */
    public class Ocurrentmatrix extends PSObjectOperator {
        /**
         * Invokes this operator.
         *
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            PSObjectArray matrix = osPop().toArray();
            matrix.copy(gsCurrent().getCtm());
            osPush(matrix);
        }
    }

    /**
     * PostScript op: currentoverprint.
     */
    public class Ocurrentoverprint extends PSObjectOperator {
        /**
         * Invokes this operator.
         *
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            boolean overprint = gsCurrent().currentOverprint();
            osPush(new PSObjectBool(overprint));
        }
    }

    /**
     * PostScript op: currentpagedevice.
     */
    public class Ocurrentpagedevice extends PSObjectOperator {
        /**
         * Invokes this operator.
         *
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            // Currently, this will always return an empty dictionary
            // indicating that there is no page.
            PSObjectDict emptyDict = new PSObjectDict(getInterp());
            osPush(emptyDict);
        }
    }

    /**
     * PostScript op: currentpoint.
     */
    public class Ocurrentpoint extends PSObjectOperator {
        /**
         * Invokes this operator.
         *
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            double[] curDevice = gsCurrent().getCurrentPosInDeviceSpace();
            double[] curUser = gsCurrent().getCtm().itransform(curDevice);
            osPush(new PSObjectReal(curUser[0]));
            osPush(new PSObjectReal(curUser[1]));
        }
    }

    /**
     * PostScript op: currentrgbcolor.
     */
    public class Ocurrentrgbcolor extends PSObjectOperator {
        /**
         * Invokes this operator.
         *
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            double[] rgb = gsCurrent().currentrgbcolor();
            for (int i = 0; i < rgb.length; i++) {
                osPush(new PSObjectReal(rgb[i]));
            }
        }
    }

    /**
     * PostScript op: currentscreen.
     */
    public class Ocurrentscreen extends PSObjectOperator {
        /**
         * Invokes this operator.
         *
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            // This is not really meaningfull for Eps2pgf. Therefore it
            // just returns some values.
            osPush(new PSObjectReal(150.0));
            osPush(new PSObjectReal(45.0));
            try {
                // Spot function from PostScript Reference Manual p.486 
                // { 180 mul cos
                //   exch 180 mul cos
                //   add
                //   2 div
                // }
                String spotFunc = "{ 180 mul cos exch 180 mul cos add 2 div }";
                osPush(Parser.convertToPSObject(spotFunc, getInterp()));
            } catch (PSErrorIOError ex) {
                // this can never happen
            }
        }
    }

    /**
     * PostScript op: currentsmoothness.
     */
    public class Ocurrentsmoothness extends PSObjectOperator {
        /**
         * Invokes this operator.
         *
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            double smoothness = gsCurrent().currentSmoothness();
            osPush(new PSObjectReal(smoothness));
        }
    }

    /**
     * PostScript op: currentstrokeadjust.
     */
    public class Ocurrentstrokeadjust extends PSObjectOperator {
        /**
         * Invokes this operator.
         *
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            osPush(new PSObjectBool(gsCurrent().getStrokeAdjust()));
        }
    }

    /**
     * PostScript op: currentsystemparams.
     */
    public class Ocurrentsystemparams extends PSObjectOperator {
        /**
         * Invokes this operator.
         *
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            osPush(getInterpParams().currentSystemParams());
        }
    }

    /**
     * PostScript op: currenttransfer.
     */
    public class Ocurrenttransfer extends PSObjectOperator {
        /**
         * Invokes this operator.
         *
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            osPush(gsCurrent().currentTransfer());
        }
    }

    /**
     * PostScript op: currentundercolorremoval.
     */
    public class Ocurrentundercolorremoval extends PSObjectOperator {
        /**
         * Invokes this operator.
         *
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            osPush(gsCurrent().currentUndercolorRemoval());
        }
    }

    /**
     * PostScript op: currentuserparams.
     */
    public class Ocurrentuserparams extends PSObjectOperator {
        /**
         * Invokes this operator.
         *
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            osPush(getInterpParams().currentUserParams());
        }
    }

    /**
     * PostScript op: curveto.
     */
    public class Ocurveto extends PSObjectOperator {
        /**
         * Invokes this operator.
         *
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            double y3 = osPop().toReal();
            double x3 = osPop().toReal();
            double y2 = osPop().toReal();
            double x2 = osPop().toReal();
            double y1 = osPop().toReal();
            double x1 = osPop().toReal();
            gsCurrent().curveto(x1, y1, x2, y2, x3, y3);
        }
    }

    /**
     * PostScrip op: cvi.
     */
    public class Ocvi extends PSObjectOperator {
        /**
         * Invokes this operator.
         *
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            PSObject obj = osPop();
            obj.checkAccess(false, true, false);
            osPush(new PSObjectInt(obj.cvi()));
        }
    }

    /**
     * PostScript op: cvlit.
     */
    public class Ocvlit extends PSObjectOperator {
        /**
         * Invokes this operator.
         *
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            PSObject any = osPop();
            osPush(any.cvlit());
        }
    }

    /**
     * PostScript op: cvn.
     */
    public class Ocvn extends PSObjectOperator {
        /**
         * Invokes this operator.
         *
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            PSObjectString str = osPop().toPSString();
            str.checkAccess(false, true, false);
            osPush(str.cvn());
        }
    }

    /**
     * PostScript op: cvr.
     */
    public class Ocvr extends PSObjectOperator {
        /**
         * Invokes this operator.
         *
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            PSObject any = osPop();
            any.checkAccess(false, true, false);
            osPush(new PSObjectReal(any.cvr()));
        }
    }

    /**
     * PostScript op: cvrs.
     */
    public class Ocvrs extends PSObjectOperator {
        /**
         * Invokes this operator.
         *
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            PSObjectString string = osPop().toPSString();
            string.checkAccess(false, false, true);
            int radix = osPop().toInt();
            PSObject num = osPop();
            osPush(new PSObjectString(num.cvrs(radix), getInterp()));
        }
    }

    /**
     * PostScript op: cvs.
     */
    public class Ocvs extends PSObjectOperator {
        /**
         * Invokes this operator.
         *
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            PSObjectString string = osPop().toPSString();
            PSObject any = osPop();
            string.checkAccess(false, false, true);
            any.checkAccess(false, true, false);
            string.overwrite(any.cvs());
            osPush(string);
        }
    }

    /**
     * PostScript op: cvx.
     */
    public class Ocvx extends PSObjectOperator {
        /**
         * Invokes this operator.
         *
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            PSObject any = osPop();
            osPush(any.cvx());
        }
    }

    /**
     * PostScript op: cshow.
     */
    public class Ocshow extends PSObjectOperator {
        /**
         * Invokes this operator.
         *
         * @throws PSError A PostScript error occurred.
         * @throws ProgramError This shouldn't happen, it indicates a bug.
         */
        @Override
        public void invoke() throws PSError, ProgramError {
            throw new PSErrorUnregistered("cshow operator");
        }
    }
    
}
