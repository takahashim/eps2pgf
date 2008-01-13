/*
 * Interpreter.java
 *
 * This file is part of Eps2pgf.
 *
 * Copyright 2007, 2008 Paul Wagenaars <paul@wagenaars.org>
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

package net.sf.eps2pgf.postscript;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import net.sf.eps2pgf.Options;
import net.sf.eps2pgf.ProgramError;
import net.sf.eps2pgf.io.PSStringInputStream;
import net.sf.eps2pgf.io.TextHandler;
import net.sf.eps2pgf.io.TextReplacements;
import net.sf.eps2pgf.io.devices.CacheDevice;
import net.sf.eps2pgf.io.devices.LOLDevice;
import net.sf.eps2pgf.io.devices.NullDevice;
import net.sf.eps2pgf.io.devices.OutputDevice;
import net.sf.eps2pgf.io.devices.PGFDevice;
import net.sf.eps2pgf.postscript.colors.PSColor;
import net.sf.eps2pgf.postscript.colors.RGB;
import net.sf.eps2pgf.postscript.errors.PSError;
import net.sf.eps2pgf.postscript.errors.PSErrorDictStackUnderflow;
import net.sf.eps2pgf.postscript.errors.PSErrorIOError;
import net.sf.eps2pgf.postscript.errors.PSErrorInvalidExit;
import net.sf.eps2pgf.postscript.errors.PSErrorInvalidStop;
import net.sf.eps2pgf.postscript.errors.PSErrorRangeCheck;
import net.sf.eps2pgf.postscript.errors.PSErrorStackUnderflow;
import net.sf.eps2pgf.postscript.errors.PSErrorTypeCheck;
import net.sf.eps2pgf.postscript.errors.PSErrorUndefined;
import net.sf.eps2pgf.postscript.errors.PSErrorUnimplemented;
import net.sf.eps2pgf.postscript.errors.PSErrorUnmatchedMark;
import net.sf.eps2pgf.postscript.filters.EexecDecode;
import net.sf.eps2pgf.util.ArrayStack;

/**
 * Interprets a PostScript document and produces output.
 * 
 * @author Paul Wagenaars
 */
public class Interpreter {
    /** Operand stack (see PostScript manual for more info). */
    private ArrayStack<PSObject> opStack = new ArrayStack<PSObject>();
    
    /** Dictionary stack. */
    private DictStack dictStack;
    
    /** Execution stack. */
    private ExecStack execStack = new ExecStack();
    
    /** Graphics state. */
    private GstateStack gstate;
    
    /** Text handler, handles text in the postscript code. */
    private TextHandler textHandler;
    
    /** Header information of the file being interpreted. */
    private DSCHeader header;
    
    /** Default clipping path. */
    private Path defaultClippingPath;
    
    /** Font directory. */
    private FontManager fontDirectory;
    
    /** Log information. */
    private final Logger log = Logger.getLogger("net.sourceforge.eps2pgf");
    
    /**
     * Creates a new instance of interpreter.
     * 
     * @param opts Configuration options.
     * @param fileHeader The file header.
     * @param textReplace The text replacements.
     * @param outputWriter Output is written to this device.
     * 
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     * @throws PSError A PostScript error occurred.
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public Interpreter(final Writer outputWriter, final Options opts,
            final DSCHeader fileHeader,
            final TextReplacements textReplace)
            throws ProgramError, PSError, IOException {
        
        // Create graphics state stack with output device
        OutputDevice output;
        switch (opts.getOutputType()) {
            case PGF:
                output = new PGFDevice(outputWriter);
                break;
            case LOL:
                output = new LOLDevice(outputWriter);
                break;
            default:
                throw new ProgramError("Unknown output device ("
                        + opts.getOutputType() + ").");
        }
        this.gstate = new GstateStack(output);
        
        this.header = fileHeader;
        
        // Text handler
        this.textHandler = new TextHandler(this.gstate, textReplace,
                opts.getTextmode());
        
        this.initialize();
    }
    
    /**
     * Creates a new instance of Interpreter with nulldevice as output and
     * (virtually) infinite bounding box.
     * 
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     * @throws PSError A PostScript error occurred.
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public Interpreter() throws ProgramError, PSError, IOException {
        // Create graphics state stack with output device
        OutputDevice output = new NullDevice();
        gstate = new GstateStack(output);
        
        // "Infinite" bounding box (square box from (-10m,-10m) to (10m,10m))
        double[] bbox = {-28346.46, -28346.46, 28346.46, 28346.46};
        header = new DSCHeader(bbox);

        // Text handler
        textHandler = new TextHandler(gstate);
        
        initialize();
    }
    
    /**
     * Do some initialization tasks.
     * 
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws PSError A PostScript error occurred.
     * @throws ProgramError the program error
     */
    void initialize() throws IOException, PSError, ProgramError {
        // Initialize character encodings and fonts
        Encoding.initialize();
        fontDirectory = new FontManager();
        
        // Create dictionary stack
        setDictStack(new DictStack(this));

        gstate.current().getDevice().init(gstate.current());
        
        gstate.current().setcolorspace(
                new PSObjectName("DeviceGray", true), true);
        
        // An eps-file defines a bounding box. Set this bounding box as the
        // default clipping path.
        double[] bbox = header.getBoundingBox();
        double left, right, top, bottom;
        if (bbox != null) {
            left = bbox[0];
            bottom = bbox[1];
            right = bbox[2];
            top = bbox[3];
        } else {
            // If no bounding box is default we use A4 paper. Note that this
            // bounding box is not written to the output.
            left = 0;
            bottom = 0;
            right = 595.276;
            top = 841.890;
        }
        gstate.current().moveto(left, bottom);
        gstate.current().lineto(right, bottom);
        gstate.current().lineto(right, top);
        gstate.current().lineto(left, top);
        gstate.current().getPath().closepath();
        defaultClippingPath = gstate.current().getPath();
        op_newpath();
        if (bbox != null) {
            op_initclip();
        } else {
            gstate.current().setClippingPath(defaultClippingPath.clone());
        }
    }
    
    /**
     * Set the dictionary stack. 
     * 
     * @param pDictStack the dictStack to set
     */
    void setDictStack(final DictStack pDictStack) {
        dictStack = pDictStack;
    }

    /**
     * Get the dictionary stack.
     * 
     * @return The current dictionary stack.
     */
    DictStack getDictStack() {
        return dictStack;
    }

    /**
     * Gets the current execution stack.
     * 
     * @return The current execution stack.
     */
    public ExecStack getExecStack() {
        return execStack;
    }

    /**
     * Get the operand stack.
     * 
     * @return The current operand stack.
     */
    public ArrayStack<PSObject> getOpStack() {
        return opStack;
    }

    /**
     * @return The FontDirectory
     */
    public FontManager getFontDirectory() {
        return fontDirectory;
    }

    /**
     * Start interpreting PostScript document.
     * 
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     * @throws PSError A PostScript error occurred.
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public void start() throws ProgramError, PSError, IOException {
        try {
            run();
        } catch (PSError e) {
            log.severe("A PostScript error occurred.");
            log.severe("    Type: " + e.getMessage());
            
            int n = Math.min(getOpStack().size(), 10);
            log.severe("    Operand stack (max top 10 items):");
            if (n == 0) {
                log.severe("      (empty)");
            } else {
                for (int i = 0; i < n; i++) {
                    log.severe("      |- " + getOpStack().peek(i).isis());
                }
                if (n < getOpStack().size()) {
                    log.severe("      (rest of stack suppressed)");
                }
            }
            this.gstate.current().getDevice().finish();
            throw e;
        }
        this.gstate.current().getDevice().finish();
    }
    
    /**
     * Execute all objects on the execution stack one by one.
     * 
     * @throws PSError A PostScript error occurred.
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    public void run() throws PSError, ProgramError {
        while (this.getExecStack().size() > 0) {
            PSObject obj = this.getExecStack().getNextToken();
            if (obj != null) {
                executeObject(obj, false);
            }
        }
    }
    
    /**
     * Look at the current element at the top of the execution stack, then
     * execute the supplied object and start running until the same object is
     * again on top of the execution stack.
     * 
     * @param objectToRun The object to run.
     * 
     * @throws PSError A PostScript error occurred.
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    public void runObject(final PSObject objectToRun)
            throws PSError, ProgramError {
        PSObject topAtStart = execStack.getTop();
        executeObject(objectToRun);
        try {
            while (execStack.getTop() != topAtStart) {
                PSObject obj = getExecStack().getNextToken();
                if (obj != null) {
                    executeObject(obj, false);
                }            
            }
        } catch (PSError e) {
            // There was an error. Restore the execution stack to its original
            // state. After that the error is thrown again.
            while (execStack.getTop() != topAtStart) {
                getExecStack().pop();
            }
            throw e;
        }
    }
    
    /**
     * Execute/process an object in this interpreter. How the object is exactly
     * executed depends on the object type and properties. The object is
     * executed indirectly (executed as a result of executing another object).
     * See section "3.5.5 Execution of specific types" of the PostScript manual
     * for more info.
     * 
     * @param obj Object that is to be executed
     * 
     * @throws PSError A PostScript error occurred.
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    public void executeObject(final PSObject obj) throws PSError, ProgramError {
        executeObject(obj, true);
    }
    
    /**
     * Execute/process an object in this interpreter. How the object is exactly
     * executed depends on the object type and properties.
     * See section "3.5.5 Execution of specific types" of the PostScript manual
     * for more info.
     * 
     * @param obj Object that is to be executed
     * @param indirect Indicates how the object was encoutered: directly
     * (encountered by the interpreter) or indirect (as a
     * result of executing some other object)
     * 
     * @throws PSError A PostScript error occurred.
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    public void executeObject(final PSObject obj, final boolean indirect)
            throws PSError, ProgramError {
        if (obj.isLiteral()) {
            // Object is literal
            getOpStack().push(obj);
        } else {
            // Object is executable
            if (obj instanceof PSObjectArray) {
                if (indirect) {
                    getExecStack().push(obj);
                } else {
                    // directly encountered by interpreter
                    getOpStack().push(obj);
                }
            } else if (obj instanceof PSObjectString) {
                getExecStack().push(obj);
            } else if (obj instanceof PSObjectFile) {
                getExecStack().push(obj);
            } else if (obj instanceof PSObjectName) {
                PSObjectName key = obj.toName();
                PSObject value = getDictStack().lookup(key);
                if (value == null) {
                    throw new PSErrorUndefined(key.toString());
                } else {
                    executeObject(value.dup());
                }
            } else if (obj instanceof PSObjectOperator) {
                Throwable except = null;
                try {
                    ((PSObjectOperator) obj).getOpMethod().invoke(this);
                } catch (InvocationTargetException e) {
                    if (e.getCause() instanceof PSError) {
                        throw (PSError) e.getCause();
                    } else if (e.getCause() instanceof ProgramError) {
                        throw (ProgramError) e.getCause();
                    } else {
                        except = e.getCause();
                    }
                } catch (IllegalAccessException e) {
                    except = e;
                } finally {
                    if (except != null) {
                        log.severe("An unexpected exception occurred during "
                                + "execution of an operator.");
                        log.severe("    Type: " + except);
                        log.severe("    Message: " + except.getMessage());
                        log.severe("    Cause: " + except.getCause());
                        log.severe("    Stack trace:");
                        for (StackTraceElement elem : except.getStackTrace()) {
                            log.severe("      |- " + elem);
                        }
                        
                        throw new ProgramError("Unexpected exception during in"
                                + " operator call.");
                    }
                }
            } else if (obj instanceof PSObjectNull) {
                // don't do anything with an executable null
            } 
        }  // end of check whether object is literal
    }
    
    /**
     * PostScript op: abs.
     * 
     * @throws PSErrorStackUnderflow Tried to pop an object from an empty stack.
     * @throws PSErrorTypeCheck A PostScript typecheck error occurred.
     */
    public void op_abs() throws PSErrorStackUnderflow, PSErrorTypeCheck {
        PSObject obj = this.getOpStack().pop();
        this.getOpStack().push(obj.abs());
    }
    
    /**
     * PostScript op: add.
     * 
     * @throws PSErrorStackUnderflow Tried to pop an object from an empty stack.
     * @throws PSErrorTypeCheck A PostScript typecheck error occurred.
     */
    public void op_add() throws PSErrorStackUnderflow, PSErrorTypeCheck {
        PSObject num2 = this.getOpStack().pop();
        PSObject num1 = this.getOpStack().pop();
        this.getOpStack().push(num1.add(num2));
    }
    
    /**
     * PostScript op: aload.
     * 
     * @throws PSError A PostScript error occurred.
     */
    public void op_aload() throws PSError {
        PSObject array = this.getOpStack().pop();
        array.checkAccess(false, true, false);
        for (PSObject obj : array.toArray()) {
            this.getOpStack().push(obj);
        }
        this.getOpStack().push(array);
    }
    
    /**
     * PostScript op: anchorsearch.
     * 
     * @throws PSError A PostScript error occurred.
     */
    public void op_anchorsearch() throws PSError {
        PSObjectString seekObj = getOpStack().pop().toPSString();
        seekObj.checkAccess(false, true, false);
        PSObjectString string = getOpStack().pop().toPSString();
        seekObj.checkAccess(false, true, false);
        
        String seek = seekObj.toString();
        List<PSObject> result = string.anchorsearch(seek);
        while (!result.isEmpty()) {
            getOpStack().push(result.remove(0));
        }
    }
    
    /**
     * PostScript op: and.
     * 
     * @throws PSErrorStackUnderflow Tried to pop an object from an empty stack.
     * @throws PSErrorTypeCheck A PostScript typecheck error occurred.
     */
    public void op_and() throws PSErrorStackUnderflow, PSErrorTypeCheck {
        PSObject obj2 = getOpStack().pop();
        PSObject obj1 = getOpStack().pop();
        getOpStack().push(obj1.and(obj2));
    }
    
    /**
     * PostScript op: arc.
     * 
     * @throws PSError A PostScript error occurred.
     */
    public void op_arc() throws PSError {
        double angle2 = getOpStack().pop().toReal();
        double angle1 = getOpStack().pop().toReal();
        double r = getOpStack().pop().toReal();
        double y = getOpStack().pop().toReal();
        double x = getOpStack().pop().toReal();
        gstate.current().arc(x, y, r, angle1, angle2, true);
    }
    
    /**
     * PostScript op: arcn.
     * 
     * @throws PSError A PostScript error occurred.
     */
    public void op_arcn() throws PSError {
        double angle2 = getOpStack().pop().toReal();
        double angle1 = getOpStack().pop().toReal();
        double r = getOpStack().pop().toNonNegReal();
        double y = getOpStack().pop().toReal();
        double x = getOpStack().pop().toReal();
        gstate.current().arc(x, y, r, angle1, angle2, false);
    }
    
    /**
     * PostScript op: acrt.
     * 
     * @throws PSError A PostScript error occurred.
     */
    public void op_arct() throws PSError {
        double r = this.getOpStack().pop().toNonNegReal();
        double y2 = this.getOpStack().pop().toReal();
        double x2 = this.getOpStack().pop().toReal();
        double y1 = this.getOpStack().pop().toReal();
        double x1 = this.getOpStack().pop().toReal();
        this.gstate.current().arcto(x1, y1, x2, y2, r);
    }
    
    /**
     * PostScript op: acrto.
     * 
     * @throws PSError A PostScript error occurred.
     */
    public void op_arcto() throws PSError {
        double r = this.getOpStack().pop().toNonNegReal();
        double y2 = this.getOpStack().pop().toReal();
        double x2 = this.getOpStack().pop().toReal();
        double y1 = this.getOpStack().pop().toReal();
        double x1 = this.getOpStack().pop().toReal();
        double[] t1t2 = this.gstate.current().arcto(x1, y1, x2, y2, r);
        for (int i = 0; i < t1t2.length; i++) {
            this.getOpStack().push(new PSObjectReal(t1t2[i]));
        }
    }
    
    /**
     * PostScript op: array.
     * 
     * @throws PSErrorStackUnderflow Tried to pop an object from an empty stack.
     * @throws PSErrorTypeCheck A PostScript typecheck error occurred.
     * @throws PSErrorRangeCheck A PostScript rangecheck error occurred.
     */
    public void op_array() throws PSErrorStackUnderflow, PSErrorTypeCheck,
            PSErrorRangeCheck {
        int n = getOpStack().pop().toNonNegInt();
        op_sqBrackLeft();
        PSObjectNull nullObj = new PSObjectNull();
        for (int i = 0; i < n; i++) {
            getOpStack().push(nullObj);
        }
        try {
            op_sqBrackRight();
        } catch (PSErrorUnmatchedMark e) {
            // Since the op_sqBrackLeft call is a few lines up this error can
            // never happen.
        }
    }
    
    /**
     * PostScript op: ashow.
     * 
     * @throws PSError A PostScript error occurred.
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    public void op_ashow() throws PSError, IOException, ProgramError {
        log.info("ashow operator encoutered. ashow is not implemented, "
                + "instead the normal show is used.");
        PSObjectString string = getOpStack().pop().toPSString();
        string.checkAccess(false, true, false);
        getOpStack().pop().toReal(); // read ay
        getOpStack().pop().toReal();  // read ax
        getOpStack().push(string);
        op_show();
    }
    
    
    /**
     * PostScript op: astore.
     * 
     * @throws PSError A PostScript error occurred.
     */
    public void op_astore() throws PSError {
        PSObjectArray array = getOpStack().pop().toArray();
        array.checkAccess(false, true, true);
        int n = array.size();
        try {
            for (int i = (n - 1); i >= 0; i--) {
                array.set(i, getOpStack().pop());
            }
        } catch (PSErrorRangeCheck e) {
            // due to the for-loop this can never happen
        }
        getOpStack().push(array);
    }
    
    /**
     * PostScript op: atan.
     * 
     * @throws PSErrorStackUnderflow Tried to pop an object from an empty stack.
     * @throws PSErrorTypeCheck A PostScript typecheck error occurred.
     */
    public void op_atan() throws PSErrorStackUnderflow, PSErrorTypeCheck {
        double den = getOpStack().pop().toReal();
        double num = getOpStack().pop().toReal();
        double result = Math.atan2(num, den) / Math.PI * 180;
        // Java atan method returns in range -180 to 180, while the PostScript
        // function should return in range 0-360
        result = (result + 360.0) % 360.0;
        getOpStack().push(new PSObjectReal(result));
    }
    
    /**
     * PostScript op: begin.
     * 
     * @throws PSError A PostScript error occurred.
     */
    public void op_begin() throws PSError {
        PSObjectDict dict = getOpStack().pop().toDict();
        dict.checkAccess(true, true, false);
        getDictStack().pushDict(dict);
    }
    
    /**
     * PostScript op: bind.
     * 
     * @throws PSErrorStackUnderflow Tried to pop an object from an empty stack.
     * @throws PSErrorTypeCheck A PostScript typecheck error occurred.
     */
    public void op_bind() throws PSErrorStackUnderflow, PSErrorTypeCheck {
        getOpStack().peek().toArray().bind(this);
    }
    
    /**
     * PostScript op: bitshift.
     * 
     * @throws PSErrorStackUnderflow Tried to pop an object from an empty stack.
     * @throws PSErrorTypeCheck A PostScript typecheck error occurred.
     */
    public void op_bitshift() throws PSErrorStackUnderflow, PSErrorTypeCheck {
        int shift = getOpStack().pop().toInt();
        int int1 = getOpStack().pop().toInt();
        if (shift >= 0) {
            int1 <<= shift;
        } else {
            int1 >>>= -shift;
        }
        getOpStack().push(new PSObjectInt(int1));
    }
    
    /**
     * PostScript op: ceiling.
     * 
     * @throws PSErrorStackUnderflow Tried to pop an object from an empty stack.
     * @throws PSErrorTypeCheck A PostScript typecheck error occurred.
     */
    public void op_ceiling() throws PSErrorStackUnderflow, PSErrorTypeCheck {
        PSObject obj = getOpStack().pop();
        getOpStack().push(obj.ceiling());
    }
    
    /**
     * PostScript op: charpath.
     * Eps2pgf does currently not come with the fonts (only with the metrics
     * describing the outlines). So, instead of the fonts themselves, the
     * bounding box is added.
     * 
     * @throws PSError A PostScript error occurred.
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    public void op_charpath() throws PSError, ProgramError {
        getOpStack().pop().toBool();  // bool
        PSObjectString string = getOpStack().pop().toPSString();
        textHandler.charpath(string);
    }
    
    /**
     * PostScript op: clear.
     */
    public void op_clear() {
        getOpStack().clear();
    }
    
    /**
     * PostScript op: cleardictstack.
     */
    public void op_cleardictstack() {
        getDictStack().cleardictstack();
    }
    
    /**
     * PostScript op: cleartomark.
     * 
     * @throws PSErrorStackUnderflow Tried to pop an object from an empty stack.
     * @throws PSErrorUnmatchedMark An operator is seeking a mark object on the
     * operand stack, but none is present.
     */
    public void op_cleartomark() throws PSErrorUnmatchedMark,
            PSErrorStackUnderflow {
        int n = getOpStack().size();
        for (int i = 0; i < n; i++) {
            if (getOpStack().pop() instanceof PSObjectMark) {
                return;
            }
        }
        throw new PSErrorUnmatchedMark();
    }
    
    /**
     * PostScript op: clip.
     * 
     * @throws PSErrorUnimplemented Encountered a PostScript feature that is not
     * (yet) implemented.
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public void op_clip() throws PSErrorUnimplemented, IOException {
        gstate.current().clip();
        gstate.current().getDevice().clip(gstate.current().getClippingPath());
    }
    
    /**
     * PostScript op: clippath.
     */
    public void op_clippath() {
        gstate.current().setPath(gstate.current().getClippingPath().clone());
    }
    
    /**
     * PostScript op: closefile.
     * 
     * @throws PSError A PostScript error occurred.
     */
    public void op_closefile() throws PSError {
        PSObjectFile file = getOpStack().pop().toFile();
        file.closefile();
    }
    
    /**
     * PostScript op: closepath.
     * 
     * @throws PSErrorRangeCheck A PostScript rangecheck error occurred.
     * @throws PSErrorTypeCheck A PostScript typecheck error occurred.
     */
    public void op_closepath() throws PSErrorRangeCheck, PSErrorTypeCheck {
        double[] startPos = gstate.current().getPath().closepath();
        if (startPos != null) {
            gstate.current().moveto(startPos[0], startPos[1]);
        }
    }
    
    /**
     * PostScript op: concat.
     * 
     * @throws PSError A PostScript error occurred.
     */
    public void op_concat() throws PSError {
        PSObjectMatrix matrix = getOpStack().pop().toMatrix();
        gstate.current().getCtm().concat(matrix);
        gstate.current().updatePosition();
    }
    
    /**
     * PostScript op: concantmatrix.
     * 
     * @throws PSError A PostScript error occurred.
     */
    public void op_concatmatrix() throws PSError {
        PSObjectMatrix matrix3 = getOpStack().pop().toMatrix();
        PSObjectMatrix matrix2 = getOpStack().pop().toMatrix();
        PSObjectMatrix matrix1 = getOpStack().pop().toMatrix();
        matrix3.copy(matrix2);
        matrix3.concat(matrix1);
        getOpStack().push(matrix3);
    }
    
    /**
     * PostScript op: copy.
     * 
     * @throws PSError A PostScript error occurred.
     */
    public void op_copy() throws PSError {
        PSObject obj = getOpStack().pop();
        if (obj instanceof PSObjectInt) {
            // Get n, the number of copies to make
            int n = obj.toNonNegInt();
            int stackSize = getOpStack().size();
        
            for (int i = stackSize - n; i < stackSize; i++) {
                getOpStack().push(getOpStack().get(i));
            }
        } else {
            PSObject obj1 = getOpStack().pop();
            obj.checkAccess(false, false, true);
            obj1.checkAccess(false, true, false);
            PSObject subseq = obj.copy(obj1);
            getOpStack().push(subseq);
        }
    }
    
    /**
     * PostScript op: cos.
     * 
     * @throws PSErrorStackUnderflow Tried to pop an object from an empty stack.
     * @throws PSErrorTypeCheck A PostScript typecheck error occurred.
     */
    public void op_cos() throws PSErrorStackUnderflow, PSErrorTypeCheck {
        double angle = getOpStack().pop().toReal();
        getOpStack().push(new PSObjectReal(Math.cos(angle * Math.PI / 180.0)));
    }
    
    /**
     * PostScript op: count.
     */
    public void op_count() {
        int count = getOpStack().size();
        PSObjectInt n = new PSObjectInt(count);
        getOpStack().push(n);
    }
    
    /**
     * PostScript op: countdictstack.
     */
    public void op_countdictstack() {
        getOpStack().push(new PSObjectInt(getDictStack().countdictstack()));
    }
    
    /**
     * PostScript op: countexecstack.
     */
    public void op_countexecstack() {
        getOpStack().push(new PSObjectInt(getExecStack().size()));
    }
    
    /**
     * PostScript op: counttomark.
     * 
     * @throws PSErrorUnmatchedMark An operator is seeking a mark object on the
     * operand stack, but none is present.
     */
    public void op_counttomark() throws PSErrorUnmatchedMark {
        int n = getOpStack().size();
        for (int i = n - 1; i >= 0; i--) {
            if (getOpStack().get(i) instanceof PSObjectMark) {
                getOpStack().push(new PSObjectInt(n - 1 - i));
                return;
            }
        }
        throw new PSErrorUnmatchedMark();
    }
    
    /**
     * PostScript op: currentcmykcolor.
     * 
     * @throws PSError A PostScript error occurred.
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    public void op_currentcmykcolor() throws PSError, ProgramError {
        double[] cmyk = gstate.current().currentcmykcolor();
        for (int i = 0; i < cmyk.length; i++) {
            getOpStack().push(new PSObjectReal(cmyk[i]));
        }
    }
    
    /**
     * PostScript op: currentcolor.
     */
    public void op_currentcolor() {
        PSColor color = gstate.current().getColor();
        for (int i = 0; i < color.getNrComponents(); i++) {
            getOpStack().push(new PSObjectReal(color.getLevel(i)));
        }
    }
    
    /**
     * PostScript op: currentcolorspace.
     */
    public void op_currentcolorspace() {
        getOpStack().push(gstate.current().getColor().getColorSpace());
    }
    
    /**
     * PostScript op: currentdict.
     */
    public void op_currentdict() {
        getOpStack().push(getDictStack().peekDict());
    }
    
    /**
     * PostScript op: currentdash.
     */
    public void op_currentdash() {
        opStack.push(gstate.current().getDashPattern().clone());
        opStack.push(new PSObjectReal(gstate.current().getDashOffset()));
    }
    
    /**
     * PostScript op: currentfile.
     * 
     * @throws PSError A PostScript error occurred.
     */
    public void op_currentfile() throws PSError {
        PSObjectFile file = getExecStack().getTopmostFile();
        if (file == null) {
            file = new PSObjectFile(null);
        } else {
            file = file.dup();
        }
        file.cvlit();
        getOpStack().push(file);
    }
    
    /**
     * PostScript op: currentflat.
     */
    public void op_currentflat() {
        // pgf does not support changing the flatness
        getOpStack().push(new PSObjectReal(0.2));
    }
    
    /**
     * PostScript op: currentgray.
     * 
     * @throws PSError A PostScript error occurred.
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    public void op_currentgray() throws PSError, ProgramError {
        double gray = gstate.current().currentgray();
        getOpStack().push(new PSObjectReal(gray));
    }
    
    /**
     * PostScript op: currenthsbcolor.
     * 
     * @throws PSError A PostScript error occurred.
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    public void op_currenthsbcolor() throws PSError, ProgramError {
        double[] hsb = gstate.current().currenthsbcolor();
        for (int i = 0; i < hsb.length; i++) {
            getOpStack().push(new PSObjectReal(hsb[i]));
        }
    }
    
    /**
     * PostScript op: currentlinewidth.
     */
    public void op_currentlinewidth() {
        getOpStack().push(new PSObjectReal(gstate.current().getLineWidth()));
    }
    
    /**
     * PostScript op: currentmatrix.
     * 
     * @throws PSError A PostScript error occurred.
     */
    public void op_currentmatrix() throws PSError {
        PSObjectMatrix matrix = getOpStack().pop().toMatrix();
        matrix.copy(gstate.current().getCtm());
        getOpStack().push(matrix);
    }
    
    /**
     * PostScript op: currentpoint.
     * 
     * @throws PSError A PostScript error occurred.
     */
    public void op_currentpoint() throws PSError {
        double[] currentDevice = gstate.current().getCurrentPosInDeviceSpace();
        double[] currentUser = gstate.current().getCtm()
                                                    .itransform(currentDevice);
        getOpStack().push(new PSObjectReal(currentUser[0]));
        getOpStack().push(new PSObjectReal(currentUser[1]));
    }
    
    /**
     * PostScript op: currentrgbcolor.
     * 
     * @throws PSError A PostScript error occurred.
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    public void op_currentrgbcolor() throws PSError, ProgramError {
        double[] rgb = gstate.current().currentrgbcolor();
        for (int i = 0; i < rgb.length; i++) {
            getOpStack().push(new PSObjectReal(rgb[i]));
        }
    }
    
    /**
     * PostScript op: currentscreen.
     * 
     * @throws PSError A PostScript error occurred.
     */
    public void op_currentscreen() throws PSError {
        // this operator is not really meaningfull for Eps2pgf. Therefore it
        // just returns some values.
        getOpStack().push(new PSObjectReal(150.0));
        getOpStack().push(new PSObjectReal(45.0));
        try {
            // Spot function from PostScript Reference Manual p.486 
            // { 180 mul cos
            //   exch 180 mul cos
            //   add
            //   2 div
            // }
            String spotFunction = "{ 180 mul cos exch 180 mul cos add 2 div }";
            getOpStack().push(Parser.convertToPSObject(spotFunction));
        } catch (PSErrorIOError ex) {
            // this can never happen
        } catch (IOException ex) {
            // this can never happen
        }
    }
    
    /**
     * PostScript op: curveto.
     * 
     * @throws PSError A PostScript error occurred.
     */
    public void op_curveto() throws PSError {
        double y3 = getOpStack().pop().toReal();
        double x3 = getOpStack().pop().toReal();
        double y2 = getOpStack().pop().toReal();
        double x2 = getOpStack().pop().toReal();
        double y1 = getOpStack().pop().toReal();
        double x1 = getOpStack().pop().toReal();
        gstate.current().curveto(x1, y1, x2, y2, x3, y3);
    }
    
    /**
     * PostScrip op: cvi.
     * 
     * @throws PSError A PostScript error occurred.
     */
    public void op_cvi() throws PSError {
        PSObject obj = getOpStack().pop();
        obj.checkAccess(false, true, false);
        getOpStack().push(new PSObjectInt(obj.cvi()));
    }
    
    /**
     * PostScript op: cvlit.
     * 
     * @throws PSErrorStackUnderflow Tried to pop an object from an empty stack.
     * @throws PSErrorUnimplemented Encountered a PostScript feature that is not
     * (yet) implemented.
     */
    public void op_cvlit() throws PSErrorStackUnderflow, PSErrorUnimplemented {
        PSObject any = getOpStack().pop();
        getOpStack().push(any.cvlit());
    }
    
    /**
     * PostScript op: cvn.
     * 
     * @throws PSError A PostScript error occurred.
     */
    public void op_cvn() throws PSError {
        PSObjectString str = getOpStack().pop().toPSString();
        str.checkAccess(false, true, false);
        getOpStack().push(str.cvn());
    }
    
    /**
     * PostScript op: cvr.
     * 
     * @throws PSError A PostScript error occurred.
     */
    public void op_cvr() throws PSError {
        PSObject any = getOpStack().pop();
        any.checkAccess(false, true, false);
        getOpStack().push(new PSObjectReal(any.cvr()));
    }
    
    /**
     * PostScript op: cvrs.
     * 
     * @throws PSError A PostScript error occurred.
     */
    public void op_cvrs() throws PSError {
        PSObjectString string = getOpStack().pop().toPSString();
        string.checkAccess(false, false, true);
        int radix = getOpStack().pop().toInt();
        PSObject num = getOpStack().pop();
        getOpStack().push(new PSObjectString(num.cvrs(radix)));
    }
    
    /**
     * PostScript op: cvs.
     * 
     * @throws PSError A PostScript error occurred.
     */
    public void op_cvs() throws PSError {
        PSObjectString string = getOpStack().pop().toPSString();
        PSObject any = getOpStack().pop();
        string.checkAccess(false, false, true);
        any.checkAccess(false, true, false);
        string.overwrite(any.cvs());
        getOpStack().push(string);
    }
    
    /**
     * PostScript op: cvx.
     * 
     * @throws PSErrorStackUnderflow Tried to pop an object from an empty stack.
     */
    public void op_cvx() throws PSErrorStackUnderflow {
        PSObject any = getOpStack().pop();
        getOpStack().push(any.cvx());
    }
    
    /**
     * PostScript op: >>.
     * this operator is equivalent to the following code (from PostScript
     * manual)
     * counttomark 2 idiv
     * dup dict
     * begin
     * {def} repeat
     * pop
     * currentdict
     * end.
     * 
     * @throws PSError A PostScript error occurred.
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    public void op_dblGreaterBrackets() throws PSError, IOException,
            ProgramError {
        PSObjectArray defProc = new PSObjectArray("{def}");
        
        op_counttomark(); getOpStack().push(new PSObjectInt(2)); op_idiv();
        op_dup(); op_dict();
        op_begin();
          getOpStack().push(defProc); op_repeat();
          op_pop();
          op_currentdict();
        op_end();
    }
    
    /**
     * PostScript op: <<.
     */
    public void op_dblLessBrackets() {
        op_mark();
    }
    
    /**
     * PostScript op: def.
     * 
     * @throws PSError A PostScript error occurred.
     */
    public void op_def() throws PSError {
        PSObject value = getOpStack().pop();
        PSObject key = getOpStack().pop();
        getDictStack().checkAccess(false, false, true);
        getDictStack().def(key, value);
    }
    
    /**
     * PostScript op: defaultmatrix.
     * 
     * @throws PSError A PostScript error occurred.
     */
    public void op_defaultmatrix() throws PSError {
        PSObjectMatrix matrix = getOpStack().pop().toMatrix();
        matrix.copy(gstate.current().getDevice().defaultCTM());
        getOpStack().push(matrix);
    }
    
    /**
     * PostScript op: definefont.
     * 
     * @throws PSError A PostScript error occurred.
     */
    public void op_definefont() throws PSError {
        PSObjectFont font = getOpStack().pop().toFont();
        PSObject key = getOpStack().pop();
        getOpStack().push(fontDirectory.defineFont(key, font));
    }
    
    /**
     * PostScript op: dict.
     * 
     * @throws PSErrorStackUnderflow Tried to pop an object from an empty stack.
     * @throws PSErrorTypeCheck A PostScript typecheck error occurred.
     * @throws PSErrorRangeCheck A PostScript rangecheck error occurred.
     */
    public void op_dict() throws PSErrorStackUnderflow, PSErrorTypeCheck, 
            PSErrorRangeCheck {
        int capacity = getOpStack().pop().toNonNegInt();
        getOpStack().push(new PSObjectDict(capacity));
    }
    
    /**
     * PostScript op: dictstack.
     * 
     * @throws PSError A PostScript error occurred.
     */
    public void op_dictstack() throws PSError {
        PSObjectArray array = getOpStack().pop().toArray();
        array.checkAccess(false, false, true);
        getOpStack().push(getDictStack().dictstack(array));
    }
    
    /**
     * PostScript op: div.
     * 
     * @throws PSError A PostScript error occurred.
     */
    public void op_div() throws PSError {
        double num2 = getOpStack().pop().toReal();
        double num1 = getOpStack().pop().toReal();
        getOpStack().push(new PSObjectReal(num1 / num2));
    }
    
    /**
     * PostScript op: dtransform.
     * 
     * @throws PSError A PostScript error occurred.
     */
    public void op_dtransform() throws PSError {
        PSObject obj = getOpStack().pop();
        PSObjectMatrix matrix = null;
        try {
            matrix = obj.toMatrix();
        } catch (PSErrorTypeCheck e) {
            //
        }
        double dy;
        if (matrix == null) {
            matrix = gstate.current().getCtm();
            dy = obj.toReal();
        } else {
            dy = getOpStack().pop().toReal();
        }
        double dx = getOpStack().pop().toReal();
        double[] transformed = matrix.dtransform(dx, dy);
        getOpStack().push(new PSObjectReal(transformed[0]));
        getOpStack().push(new PSObjectReal(transformed[1]));
    }
    
    /**
     * PostScript op: dup.
     * 
     * @throws PSErrorStackUnderflow Tried to pop an object from an empty stack.
     */
    public void op_dup() throws PSErrorStackUnderflow {
        getOpStack().push(getOpStack().peek().dup());
    }
    
    /**
     * PostScript op: eexec.
     * 
     * @throws PSError A PostScript error occurred.
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    public void op_eexec() throws PSError, ProgramError {
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
        InputStream eexecInStream = new EexecDecode(rawInStream);
        PSObjectFile eexecFile = new PSObjectFile(eexecInStream);
        
        getOpStack().push(getDictStack().lookup("systemdict"));
        op_begin();
        runObject(eexecFile);
        op_end();
    }
    
    /**
     * PostScript op: end.
     * 
     * @throws PSErrorDictStackUnderflow the PS error dict stack underflow
     */
    public void op_end() throws PSErrorDictStackUnderflow {
        getDictStack().popDict();
    }
    
    /**
     * PostScript op: eoclip.
     * 
     * @throws PSErrorUnimplemented Encountered a PostScript feature that is not
     * (yet) implemented.
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public void op_eoclip() throws PSErrorUnimplemented, IOException {
        gstate.current().clip();
        gstate.current().getDevice().eoclip(gstate.current().getClippingPath());
    }
    
    /**
     * PostScript op: eofill.
     * 
     * @throws PSError A PostScript error occurred.
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public void op_eofill() throws PSError, IOException {
        gstate.current().getDevice().eofill(gstate.current().getPath());
        op_newpath();
    }
    
    /**
     * Internal Eps2pgf operator: eps2pgfgetmetrics.
     */
    public void op_eps2pgfgetmetrics() {
        double[] metrics = gstate.current().getDevice().eps2pgfGetMetrics();
        PSObjectArray array = new PSObjectArray(metrics);
        getOpStack().push(array);
    }
    
    /**
     * PostScript op: errordict.
     * 
     * @throws PSErrorUnimplemented Encountered a PostScript feature that is not
     * (yet) implemented.
     */
    public void op_errordict() throws PSErrorUnimplemented {
        throw new PSErrorUnimplemented("errordict operator");
    }
    
    /**
     * PostScript op: eq.
     * 
     * @throws PSError A PostScript error occurred.
     */
    public void op_eq() throws PSError {
        PSObject any2 = getOpStack().pop();
        any2.checkAccess(false, true, false);
        PSObject any1 = getOpStack().pop();
        any1.checkAccess(false, true, false);
        getOpStack().push(new PSObjectBool(any1.eq(any2)));
    }
    
    /**
     * PostScript op: exch.
     * 
     * @throws PSErrorStackUnderflow Tried to pop an object from an empty stack.
     */
    public void op_exch() throws PSErrorStackUnderflow {
        PSObject any2 = getOpStack().pop();
        PSObject any1 = getOpStack().pop();
        getOpStack().push(any2);
        getOpStack().push(any1);
    }
    
    /**
     * PostScript op: exec.
     * 
     * @throws PSError A PostScript error occurred.
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    public void op_exec() throws PSError, ProgramError {
        PSObject any = getOpStack().pop();
        executeObject(any);
    }
    
    /**
     * PostScript op: execstack.
     * 
     * @throws PSError A PostScript error occurred.
     */
    public void op_execstack() throws PSError {
        PSObjectArray array = getOpStack().pop().toArray();
        array.checkAccess(false, false, true);
        PSObject subArray = array.copy(getExecStack().getStack());
        getOpStack().push(subArray);
    }
    
    /**
     * PostScript op: executeonly.
     * 
     * @throws PSError A PostScript error occurred.
     */
    public void op_executeonly() throws PSError {
        PSObject obj = getOpStack().pop();
        obj.checkAccess(true, false, false);
        obj.executeonly();
        getOpStack().push(obj);
    }
    
    /**
     * PostScript op: exit.
     * 
     * @throws PSErrorInvalidExit Exit not allowed at this position.
     */
    public void op_exit() throws PSErrorInvalidExit {
        throw new PSErrorInvalidExit();
    }
    
    /**
     * PostScript op: exp.
     * 
     * @throws PSErrorStackUnderflow Tried to pop an object from an empty stack.
     * @throws PSErrorTypeCheck A PostScript typecheck error occurred.
     */
    public void op_exp() throws PSErrorStackUnderflow, PSErrorTypeCheck {
        double exponent = getOpStack().pop().toReal();
        double base = getOpStack().pop().toReal();
        double result = Math.pow(base, exponent);
        getOpStack().push(new PSObjectReal(result));
    }
    
    /**
     * PostScript op: false.
     */
    public void op_false() {
        getOpStack().push(new PSObjectBool(false));
    }
    
    /**
     * PostScript op: fill.
     * 
     * @throws PSError A PostScript error occurred.
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public void op_fill() throws PSError, IOException {
        gstate.current().getDevice().fill(gstate.current().getPath());
        op_newpath();
    }
    
    /**
     * PostScript op: findfont.
     * 
     * @throws PSError A PostScript error occurred.
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    public void op_findfont() throws PSError, ProgramError {
        PSObject key = getOpStack().pop();
        getOpStack().push(fontDirectory.findFont(key));
    }
    
    /**
     * PostScript op: flattenpath.
     * 
     * @throws PSError A PostScript error occurred.
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    public void op_flattenpath() throws PSError, ProgramError {
        gstate.current().flattenpath();
    }
    
    /**
     * PostScript op: floor.
     * 
     * @throws PSErrorStackUnderflow Tried to pop an object from an empty stack.
     * @throws PSErrorTypeCheck A PostScript typecheck error occurred.
     */
    public void op_floor() throws PSErrorStackUnderflow, PSErrorTypeCheck {
        PSObject obj = getOpStack().pop();
        getOpStack().push(obj.floor());
    }
    
    /**
     * PostScript op: for.
     * 
     * @throws PSError A PostScript error occurred.
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    public void op_for() throws PSError, ProgramError {
        PSObjectArray proc = getOpStack().pop().toProc();
        double limit = getOpStack().pop().toReal();
        double inc = getOpStack().pop().toReal();
        double initial = getOpStack().pop().toReal();
        
        // Check whether limit, inc and initial are all three integers
        boolean allIntegers = false;
        if ((limit == Math.round(limit)) && (inc == Math.round(inc))
                && (initial == Math.round(initial))) {
            allIntegers = true;
        }
        
        // Prevent (virtually) infinite loops
        if (inc == 0) {
            return;
        } else if ((inc > 0) && (limit < initial)) {
            return;
        } else if ((inc < 0) && (limit > initial)) {
            return;
        }
        
        // Execute the for loop
        double control = initial;
        try {
            while (true) {
                if ((inc > 0) && (control > limit)) {
                    break;
                } else if ((inc < 0) && (control < limit)) {
                    break;
                }

                if (allIntegers) {
                    getOpStack().push(new PSObjectInt(control));
                } else {
                    getOpStack().push(new PSObjectReal(control));
                }
                
                runObject(proc);

                control += inc;
            }
        } catch (PSErrorInvalidExit e) {
            // 'exit' operator called from within this loop
        }
    }
    
    /**
     * PostScript op: forall.
     * 
     * @throws PSError A PostScript error occurred.
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    public void op_forall() throws PSError, ProgramError {
        PSObjectArray proc = getOpStack().pop().toProc();
        proc.checkAccess(true, false, false);
        PSObject obj = getOpStack().pop();
        obj.checkAccess(false, true, false);
        
        List<PSObject> items = obj.getItemList();
        int nr = items.remove(0).toNonNegInt();
        try {
            while (!items.isEmpty()) {
                for (int i = 0; i < nr; i++) {
                     getOpStack().push(items.remove(0));
                }
                runObject(proc);
            }
        } catch (PSErrorInvalidExit e) {
            // 'exit' operator called from within this loop
        }
    }
    
    /**
     * PostScript op: ge.
     * 
     * @throws PSError A PostScript error occurred.
     */
    public void op_ge() throws PSError {
        PSObject obj2 = getOpStack().pop();
        obj2.checkAccess(false, true, false);
        PSObject obj1 = getOpStack().pop();
        obj1.checkAccess(false, true, false);
        
        boolean gt = obj1.gt(obj2);
        boolean eq = obj1.eq(obj2);
        getOpStack().push(new PSObjectBool(gt || eq));
    }
    
    /**
     * PostScript op: get.
     * 
     * @throws PSError A PostScript error occurred.
     */
    public void op_get() throws PSError {
        PSObject indexKey = getOpStack().pop();
        PSObject obj = getOpStack().pop();
        obj.checkAccess(false, true, false);
        
        getOpStack().push(obj.get(indexKey));
    }
    
    /**
     * PostScript op: getinterval.
     * 
     * @throws PSError A PostScript error occurred.
     */
    public void op_getinterval() throws PSError {
        int count = getOpStack().pop().toNonNegInt();
        int index = getOpStack().pop().toNonNegInt();
        PSObject obj = getOpStack().pop();
        obj.checkAccess(false, true, false);
        
        getOpStack().push(obj.getinterval(index, count));
    }
    
    /**
     * PostScript op: grestore.
     * 
     * @throws PSError A PostScript error occurred.
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    public void op_grestore() throws PSError, IOException, ProgramError {
        gstate.restoreGstate();
        gstate.current().getDevice().endScope();
    }
    
    /**
     * PostScript op: gsave.
     * 
     * @throws PSError A PostScript error occurred.
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    public void op_gsave() throws PSError, IOException, ProgramError {
        gstate.saveGstate();
        gstate.current().getDevice().startScope();
    }
    
    /**
     * PostScript op: gt.
     * 
     * @throws PSError A PostScript error occurred.
     */
    public void op_gt() throws PSError {
        PSObject obj2 = getOpStack().pop();
        obj2.checkAccess(false, true, false);
        PSObject obj1 = getOpStack().pop();
        obj1.checkAccess(false, true, false);
        
        boolean chk = obj1.gt(obj2);
        getOpStack().push(new PSObjectBool(chk));
    }
    
    /**
     * PostScript op: identmatrix.
     * 
     * @throws PSError A PostScript error occurred.
     */
    public void op_identmatrix() throws PSError {
        PSObjectMatrix matrix = getOpStack().pop().toMatrix();
        matrix.copy(new PSObjectMatrix());
        getOpStack().push(matrix);
    }
    
    /**
     * PostScript op: idiv.
     * 
     * @throws PSErrorStackUnderflow Tried to pop an object from an empty stack.
     * @throws PSErrorTypeCheck A PostScript typecheck error occurred.
     */
    public void op_idiv() throws PSErrorStackUnderflow, PSErrorTypeCheck {
        int int2 = getOpStack().pop().toInt();
        int int1 = getOpStack().pop().toInt();
        int quotient = int1 / int2;
        getOpStack().push(new PSObjectInt(quotient));
    }
    
    /**
     * PostScript op: idtransform.
     * 
     * @throws PSError A PostScript error occurred.
     */
    public void op_idtransform() throws PSError {
        PSObject obj = getOpStack().pop();
        PSObjectMatrix matrix = null;
        try {
            matrix = obj.toMatrix();
        } catch (PSErrorTypeCheck e) {
            
        }
        double dy;
        if (matrix == null) {
            matrix = gstate.current().getCtm();
            dy = obj.toReal();
        } else {
            dy = getOpStack().pop().toReal();
        }
        double dx = getOpStack().pop().toReal();
        double[] transformed = matrix.idtransform(dx, dy);
        getOpStack().push(new PSObjectReal(transformed[0]));
        getOpStack().push(new PSObjectReal(transformed[1]));
    }
    
    /**
     * PostScript op: if.
     * 
     * @throws PSError A PostScript error occurred.
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    public void op_if() throws PSError, ProgramError {
        PSObjectArray proc = getOpStack().pop().toProc();
        boolean bool = getOpStack().pop().toBool();
        if (bool) {
            runObject(proc);
        }
    }
    
    /**
     * PostScript op: ifelse.
     * 
     * @throws PSError A PostScript error occurred.
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    public void op_ifelse() throws PSError, ProgramError {
        PSObjectArray proc2 = getOpStack().pop().toProc();
        PSObjectArray proc1 = getOpStack().pop().toProc();
        boolean bool = getOpStack().pop().toBool();
        
        if (bool) {
            runObject(proc1);
        } else {
            runObject(proc2);
        }
    }
    
    /**
     * PostScript op: image.
     * 
     * @throws PSError A PostScript error occurred.
     */
    public void op_image() throws PSError {
        throw new PSErrorUnimplemented("operator: image");
    }
    
    /**
     * PostScript op: index.
     * 
     * @throws PSError A PostScript error occurred.
     */
    public void op_index() throws PSError {
        // Get n, the index of the element to retrieve
        int n = getOpStack().pop().toNonNegInt();
        
        getOpStack().push(getOpStack().peek(n));
    }
    
    /**
     * PostScript op: invertmatrix.
     * 
     * @throws PSError A PostScript error occurred.
     */
    public void op_invertmatrix() throws PSError {
        PSObjectMatrix matrix2 = getOpStack().pop().toMatrix();
        PSObjectMatrix matrix1 = getOpStack().pop().toMatrix();
        matrix2.copy(matrix1);
        matrix2.invert();
        getOpStack().push(matrix2);
    }
    
    /**
     * PostScript op: initclip.
     * 
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws PSErrorUnimplemented Encountered a PostScript feature that is not
     * (yet) implemented.
     */
    public void op_initclip() throws IOException, PSErrorUnimplemented {
        gstate.current().setClippingPath(defaultClippingPath.clone());
        gstate.current().getDevice().clip(gstate.current().getClippingPath());
    }
    
    /**
     * PostScript op: initmatrix.
     */
    public void op_initmatrix() {
        gstate.current().initmatrix();
    }
    
    /**
     * PostScript op: ==.
     * 
     * @throws PSErrorStackUnderflow Tried to pop an object from an empty stack.
     */
    public void op_isis() throws PSErrorStackUnderflow {
        PSObject obj = opStack.pop();
        System.out.println(obj.isis());
    }
    
    /**
     * PostScript op: itransform.
     * 
     * @throws PSError A PostScript error occurred.
     */
    public void op_itransform() throws PSError {
        PSObject obj = getOpStack().pop();
        PSObjectMatrix matrix = null;
        try {
            matrix = obj.toMatrix();
        } catch (PSErrorTypeCheck e) {
            
        }
        double y;
        if (matrix == null) {
            matrix = gstate.current().getCtm();
            y = obj.toReal();
        } else {
            y = getOpStack().pop().toReal();
        }
        double x = getOpStack().pop().toReal();
        
        double[] itransformed = matrix.itransform(x, y);
        getOpStack().push(new PSObjectReal(itransformed[0]));
        getOpStack().push(new PSObjectReal(itransformed[1]));
    }
    
    /**
     * PostScript op: known.
     * 
     * @throws PSError A PostScript error occurred.
     */
    public void op_known() throws PSError {
        PSObject key = getOpStack().pop();
        PSObjectDict dict = getOpStack().pop().toDict();
        dict.checkAccess(false, true, false);
        
        getOpStack().push(new PSObjectBool(dict.known(key)));
    }
    
    /**
     * PostScript op: le.
     * 
     * @throws PSError A PostScript error occurred.
     */
    public void op_le() throws PSError {
        op_gt();
        op_not();
    }
    
    /**
     * PostScript op: length.
     * 
     * @throws PSError A PostScript error occurred.
     */
    public void op_length() throws PSError {
        PSObject obj = getOpStack().pop();
        obj.checkAccess(false, true, false);
        
        getOpStack().push(new PSObjectInt(obj.length()));
    }
    
    /**
     * PostScript op: lineto.
     * 
     * @throws PSError A PostScript error occurred.
     */
    public void op_lineto() throws PSError {
        double y = getOpStack().pop().toReal();
        double x = getOpStack().pop().toReal();
        gstate.current().lineto(x, y);
    }
    
    /**
     * PostScript op: ln.
     * 
     * @throws PSErrorStackUnderflow Tried to pop an object from an empty stack.
     * @throws PSErrorTypeCheck A PostScript typecheck error occurred.
     */
    public void op_ln() throws PSErrorStackUnderflow, PSErrorTypeCheck {
        double num = getOpStack().pop().toReal();
        double result = Math.log(num);
        getOpStack().push(new PSObjectReal(result));
    }
    
    /**
     * PostScript op: load.
     * 
     * @throws PSError A PostScript error occurred.
     */
    public void op_load() throws PSError {
        PSObject key = getOpStack().pop();
        PSObjectDict definedInDict = getDictStack().where(key);
        definedInDict.checkAccess(false, true, false);
        
        PSObject value = getDictStack().lookup(key);
        if (value == null) {
            getOpStack().push(new PSObjectName("/" + key));
            throw new PSErrorUndefined();
        }
        getOpStack().push(value);
    }
    
    /**
     * PostScript op: log.
     * 
     * @throws PSErrorStackUnderflow Tried to pop an object from an empty stack.
     * @throws PSErrorTypeCheck A PostScript typecheck error occurred.
     */
    public void op_log() throws PSErrorStackUnderflow, PSErrorTypeCheck {
        double num = getOpStack().pop().toReal();
        double result = Math.log10(num);
        getOpStack().push(new PSObjectReal(result));
    }
    
    /**
     * PostScript op: loop.
     * 
     * @throws PSError A PostScript error occurred.
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    public void op_loop() throws PSError, ProgramError {
        PSObjectArray proc = getOpStack().pop().toProc();
        
        try {
            while (true) {
                runObject(proc);
            }
        } catch (PSErrorInvalidExit e) {
            // 'exit' operator called from within this loop
        } catch (PSErrorInvalidStop e) {
            // 'stop' operator called from within this loop
        }
    }
    
    /**
     * PostScript op: lt.
     * 
     * @throws PSError A PostScript error occurred.
     */
    public void op_lt() throws PSError {
        op_ge();
        op_not();
    }
    
    /**
     * PostScript op: makefont.
     * 
     * @throws PSError A PostScript error occurred.
     */
    public void op_makefont() throws PSError {
        PSObjectMatrix matrix = getOpStack().pop().toMatrix();
        PSObjectDict font = getOpStack().pop().toDict();
        font = font.clone();
        PSObjectMatrix fontMatrix = font.lookup("FontMatrix").toMatrix();
        
        // Concatenate matrix to fontMatrix and store it back in font
        fontMatrix.concat(matrix);
        font.setKey("FontMatrix", fontMatrix);
        
        // Calculate the fontsize in LaTeX points
        PSObjectMatrix ctm = gstate.current().getCtm().clone();
        ctm.concat(fontMatrix);
        double fontSize = ctm.getMeanScaling() / 2.54 * 72.27;
        font.setKey("FontSize", new PSObjectReal(fontSize));
        
        getOpStack().push(font);
    }
    
    /**
     * Postscript op: mark.
     */
    public void op_mark() {
        getOpStack().push(new PSObjectMark());
    }
    
    /**
     * Postscript op: matrix.
     * 
     * @throws PSError A PostScript error occurred.
     */
    public void op_matrix() throws PSError {
        getOpStack().push(new PSObjectMatrix());
    }
    
    /**
     * PostScript op: maxlength.
     * 
     * @throws PSError A PostScript error occurred.
     */
    public void op_maxlength() throws PSError {
        PSObjectDict dict = getOpStack().pop().toDict();
        dict.checkAccess(false, true, false);
        
        getOpStack().push(new PSObjectInt(dict.maxlength()));
    }
    
    /**
     * PostScript operator: mod.
     * 
     * @throws PSErrorStackUnderflow Tried to pop an object from an empty stack.
     * @throws PSErrorTypeCheck A PostScript typecheck error occurred.
     */
    public void op_mod() throws PSErrorStackUnderflow, PSErrorTypeCheck {
        int int2 = getOpStack().pop().toInt();
        int int1 = getOpStack().pop().toInt();
        getOpStack().push(new PSObjectInt(int1 % int2));
    }
    
    /**
     * PostScript op: moveto.
     * 
     * @throws PSError A PostScript error occurred.
     */
    public void op_moveto() throws PSError {
        double y = getOpStack().pop().toReal();
        double x = getOpStack().pop().toReal();
        gstate.current().moveto(x, y);
    }
    
    /**
     * PostScript op: mul.
     * 
     * @throws PSError PostScript error occurred.
     */
    public void op_mul() throws PSError {
        PSObject num2 = getOpStack().pop();
        PSObject num1 = getOpStack().pop();
        getOpStack().push(num1.mul(num2));
    }
    
    /**
     * PostScript op: ne.
     * 
     * @throws PSError PostScript error occurred.
     */
    public void op_ne() throws PSError {
        op_eq();
        op_not();
    }

    /**
     * PostScript op: neg.
     * 
     * @throws PSError A PostScript error occurred.
     */
    public void op_neg() throws PSError {
        PSObject obj = getOpStack().pop();
        getOpStack().push(obj.neg());
    }

    /**
     * PostScript op: newpath.
     */
    public void op_newpath() {
        gstate.current().setPath(new Path(gstate));
        gstate.current().setPosition(Double.NaN, Double.NaN);
    }
    
    /**
     * PostScript op: noaccess.
     * 
     * @throws PSError A PostScript error occurred.
     */
    public void op_noaccess() throws PSError {
        PSObject obj = getOpStack().pop();
        if (obj instanceof PSObjectDict) {
            obj.checkAccess(false, false, true);
        }
        obj.noaccess();
        getOpStack().push(obj);
    }
    
    /**
     * PostScript op: not.
     * 
     * @throws PSErrorStackUnderflow Tried to pop an object from an empty stack.
     * @throws PSErrorTypeCheck A PostScript typecheck error occurred.
     */
    public void op_not() throws PSErrorStackUnderflow, PSErrorTypeCheck {
        PSObject obj = getOpStack().pop();
        getOpStack().push(obj.not());
    }
    
    /**
     * PostScript op: null.
     * 
     * @throws PSError A PostScript error occurred.
     */
    public void op_null() throws PSError {
        getOpStack().push(new PSObjectNull());
    }
    
    /**
     * PostScript op: nulldevice.
     */
    public void op_nulldevice() {
        OutputDevice nullDevice = new NullDevice();
        gstate.current().setDevice(nullDevice);
        gstate.current().initmatrix();
    }

    /**
     * PostScript op: or.
     * 
     * @throws PSErrorStackUnderflow Tried to pop an object from an empty stack.
     * @throws PSErrorTypeCheck A PostScript typecheck error occurred.
     */
    public void op_or() throws PSErrorStackUnderflow, PSErrorTypeCheck {
        PSObject obj2 = getOpStack().pop();
        PSObject obj1 = getOpStack().pop();
        getOpStack().push(obj1.or(obj2));
    }
    
    /**
     * PostScript op: pathbbox.
     * 
     * @throws PSError A PostScript error occurred.
     */
    public void op_pathbbox() throws PSError {
        double[] bbox = gstate.current().pathbbox();
        for (int i = 0; i < 4; i++) {
            getOpStack().push(new PSObjectReal(bbox[i]));
        }
    }
    
    /**
     * PostScript op: pathforall.
     * 
     * @throws PSError A PostScript error occurred.
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    public void op_pathforall() throws PSError, ProgramError {
        PSObjectArray close = getOpStack().pop().toProc();
        PSObjectArray curve = getOpStack().pop().toProc();
        PSObjectArray line = getOpStack().pop().toProc();
        PSObjectArray move = getOpStack().pop().toProc();
        
        ArrayList<PathSection> path = gstate.current().getPath().getSections();
        PSObjectMatrix ctm = gstate.current().getCtm();
        
        try {
            for (int i = 0; i < path.size(); i++) {
                PathSection section = path.get(i);
                int nrCoors = 0;
                PSObjectArray proc = close;
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
                for (int j = 0; j < nrCoors; j++) {
                    double x = section.getParam(2 * j);
                    double y = section.getParam(2 * j + 1);
                    double[] coor = ctm.itransform(x, y);
                    getOpStack().push(new PSObjectReal(coor[0]));
                    getOpStack().push(new PSObjectReal(coor[1]));
                }
                runObject(proc);
            }
        } catch (PSErrorInvalidExit e) {
            // 'exit' operator executed within this loop
        }
    }
    
    /**
     * PostScript op: picstr.
     * 
     * @throws PSError A PostScript error occurred.
     */
    public void op_picstr() throws PSError {
        throw new PSErrorUnimplemented("operator: picstr");
    }
    
    /**
     * PostScript op: pop.
     * 
     * @throws PSErrorStackUnderflow Tried to pop an object from an empty stack.
     */
    public void op_pop() throws PSErrorStackUnderflow {
        getOpStack().pop();
    }
    
    /**
     * PostScript op: pstack.
     */
    public void op_pstack() {
        for (int i = getOpStack().size() - 1; i >= 0; i--) {
            System.out.println(getOpStack().get(i).isis());
        }
    }
    
    /**
     * PostScript op: put.
     * 
     * @throws PSError A PostScript error occurred.
     */
    public void op_put() throws PSError {
        PSObject any = getOpStack().pop();
        PSObject indexKey = getOpStack().pop();
        PSObject obj = getOpStack().pop();
        obj.checkAccess(false, false, true);
        obj.put(indexKey, any);
    }
    
    /**
     * PostScript op: putinterval.
     * 
     * @throws PSError A PostScript error occurred.
     */
    public void op_putinterval() throws PSError {
        PSObject subseq = getOpStack().pop();
        subseq.checkAccess(false, true, false);
        int index = getOpStack().pop().toNonNegInt();
        PSObject seq = getOpStack().pop();
        seq.checkAccess(false, false, true);
        
        seq.putinterval(index, subseq);
    }
    
    /**
     * PostScript op: rcheck.
     * 
     * @throws PSErrorStackUnderflow Tried to pop an object from an empty stack.
     * @throws PSErrorTypeCheck A PostScript typecheck error occurred.
     */
    public void op_rcheck() throws PSErrorStackUnderflow, PSErrorTypeCheck {
        PSObject obj = getOpStack().pop();
        boolean chk = obj.rcheck();
        getOpStack().push(new PSObjectBool(chk));
    }
    
    /**
     * PostScript op: rcurveto.
     * 
     * @throws PSErrorStackUnderflow Tried to pop an object from an empty stack.
     * @throws PSErrorTypeCheck A PostScript typecheck error occurred.
     * @throws PSErrorRangeCheck A PostScript rangecheck error occurred.
     */
    public void op_rcurveto() throws PSErrorStackUnderflow, PSErrorTypeCheck,
            PSErrorRangeCheck {
        double dy3 = getOpStack().pop().toReal();
        double dx3 = getOpStack().pop().toReal();
        double dy2 = getOpStack().pop().toReal();
        double dx2 = getOpStack().pop().toReal();
        double dy1 = getOpStack().pop().toReal();
        double dx1 = getOpStack().pop().toReal();
        gstate.current().rcurveto(dx1, dy1, dx2, dy2, dx3, dy3);
    }
    
    /**
     * PostScript op: readhexstring.
     * 
     * @throws PSError A PostScript error occurred.
     */
    public void op_readhexstring() throws PSError {
        throw new PSErrorUnimplemented("operator: readhexstring");
    }
    
    /**
     * PostScript op: readonly.
     * 
     * @throws PSError A PostScript error occurred.
     */
    public void op_readonly() throws PSError {
        PSObject obj = getOpStack().pop();
        obj.checkAccess(false, true, false);
        obj.readonly();
        getOpStack().push(obj);
    }
    
    /**
     * PostScript op: readstring.
     * 
     * @throws PSError A PostScript error occurred.
     */
    public void op_readstring() throws PSError {
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
    
    /**
     * PostScript op: rectclip.
     * 
     * @throws PSError A PostScript error occurred.
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public void op_rectclip() throws PSError, IOException {
        PSObject heightObj = getOpStack().pop();
        if ((heightObj instanceof PSObjectArray)
                || (heightObj instanceof PSObjectString)) {
            throw new PSErrorUnimplemented("rectclip operator not fully "
                    + "implemented");
        }
        double height = heightObj.toReal();
        double width = getOpStack().pop().toReal();
        double y = getOpStack().pop().toReal();
        double x = getOpStack().pop().toReal();
        
        // rectclip implemented in PostScript. See PostScript manual for
        // the code below.
        op_newpath();
        gstate.current().moveto(x, y);
        gstate.current().rlineto(width, 0);
        gstate.current().rlineto(0, height);
        gstate.current().rlineto(-width, 0);
        op_closepath();
        op_clip();
        op_newpath();
    }
    
    /**
     * PostScript op: rectfill.
     * 
     * @throws PSError A PostScript error occurred.
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    public void op_rectfill() throws PSError, IOException, ProgramError {
        PSObject heightObj = getOpStack().pop();
        if ((heightObj instanceof PSObjectArray)
                || (heightObj instanceof PSObjectString)) {
            throw new PSErrorUnimplemented("rectclip operator not fully "
                    + "implemented");
        }
        double height = heightObj.toReal();
        double width = getOpStack().pop().toReal();
        double y = getOpStack().pop().toReal();
        double x = getOpStack().pop().toReal();
        
        // rectfill implemented in PostScript. See PostScript manual for
        // the code below.
        op_gsave();
        op_newpath();
        gstate.current().moveto(x, y);
        gstate.current().rlineto(width, 0);
        gstate.current().rlineto(0, height);
        gstate.current().rlineto(-width, 0);
        op_closepath();
        op_fill();
        op_grestore();
    }    
    
    /**
     * PostScript op: repeat.
     * 
     * @throws PSError A PostScript error occurred.
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    public void op_repeat() throws PSError, ProgramError {
        PSObjectArray proc = getOpStack().pop().toProc();
        int n = getOpStack().pop().toNonNegInt();
        
        try {
            for (int i = 0; i < n; i++) {
                runObject(proc);
            }
        } catch (PSErrorInvalidExit e) {
            // 'exit' operator called from within this loop
        }
    }    
    
    /**
     * PostScript op: restore.
     * 
     * @throws PSError A PostScript error occurred.
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    public void op_restore() throws PSError, IOException, ProgramError {
        PSObjectName obj = getOpStack().pop().toName();
        
        if (!obj.toString().equals("-save- (dummy)")) {
            throw new PSErrorTypeCheck();
        }
        
        // grestore is not full replacement for restore, so the might be some
        // problems.
        op_grestore();
    }    
    
    /**
     * PostScript op: rmoveto.
     * 
     * @throws PSError A PostScript error occurred.
     */
    public void op_rlineto() throws PSError {
        double dy = getOpStack().pop().toReal();
        double dx = getOpStack().pop().toReal();
        gstate.current().rlineto(dx, dy);
    }    

    /**
     * PostScript op: rmoveto.
     * 
     * @throws PSError A PostScript error occurred.
     */
    public void op_rmoveto() throws PSError {
        double dy = getOpStack().pop().toReal();
        double dx = getOpStack().pop().toReal();
        gstate.current().rmoveto(dx, dy);
    }    

    /**
     * PostScript op: roll.
     * 
     * @throws PSError A PostScript error occurred.
     */
    public void op_roll() throws PSError {
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
    
    /**
     * PostScript op: rotate.
     * 
     * @throws PSError A PostScript error occurred.
     */
    public void op_rotate() throws PSError {
        PSObject obj = getOpStack().pop();
        double angle;
        if (obj instanceof PSObjectArray) {
            PSObjectMatrix matrix = obj.toMatrix();
            angle = getOpStack().pop().toReal();
            matrix.rotate(angle);
            getOpStack().push(matrix);
        } else {
            angle = obj.toReal();
            gstate.current().getCtm().rotate(angle);
            gstate.current().updatePosition();
        }
    }
    
    /**
     * PostScript op: round.
     * 
     * @throws PSErrorStackUnderflow Tried to pop an object from an empty stack.
     * @throws PSErrorTypeCheck A PostScript typecheck error occurred.
     */
    public void op_round() throws PSErrorStackUnderflow, PSErrorTypeCheck {
        PSObject obj = getOpStack().pop();
        getOpStack().push(obj.round());
    }
   
    /**
     * PostScript op: save.
     * 
     * @throws PSError A PostScript error occurred.
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    public void op_save() throws PSError, IOException, ProgramError {
        getOpStack().push(new PSObjectName("/-save- (dummy)"));
        // gsave is not a full replacement for save, so there might be some
        // problems.
        op_gsave();
    }
   
    /**
     * PostScript op: scale.
     * 
     * @throws PSError A PostScript error occurred.
     */
    public void op_scale() throws PSError {
        PSObject obj = getOpStack().pop();
        double sx, sy;
        if (obj instanceof PSObjectArray) {
            PSObjectMatrix matrix = obj.toMatrix();
            sy = getOpStack().pop().toReal();
            sx = getOpStack().pop().toReal();
            matrix.scale(sx, sy);
            getOpStack().push(matrix);
        } else {
            sy = obj.toReal();
            sx = getOpStack().pop().toReal();
            gstate.current().getCtm().scale(sx, sy);
            gstate.current().updatePosition();
        }
    }
    
    /**
     * PostScript operator: scalefont.
     * 
     * @throws PSError A PostScript error occurred.
     */
    public void op_scalefont() throws PSError {
        double scale = getOpStack().pop().toReal();
        
        // "font scale scalefont" is equivalent to 
        // "font [scale 0 0 scale 0 0] makefont""
        op_sqBrackLeft();
        getOpStack().push(new PSObjectReal(scale));
        getOpStack().push(new PSObjectReal(0));
        getOpStack().push(new PSObjectReal(0));
        getOpStack().push(new PSObjectReal(scale));
        getOpStack().push(new PSObjectReal(0));
        getOpStack().push(new PSObjectReal(0));
        try {
            op_sqBrackRight();
        } catch (PSErrorUnmatchedMark e) {
            // This can never happen. The left square bracket (op_sqBrackLeft)
            // is a few lines up.
        }
        try {
            op_makefont();
        } catch (PSErrorRangeCheck e) {
            // This can never happen. A correct matrix is created above
        }
    }
    
    /**
     * PostScript op: search.
     * 
     * @throws PSError A PostScript error occurred.
     */
    public void op_search() throws PSError {
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
    
    /**
     * PostScript op: setcachedevice.
     * 
     * @throws PSErrorStackUnderflow Tried to pop an object from an empty stack.
     * @throws PSErrorTypeCheck A PostScript typecheck error occurred.
     */
    public void op_setcachedevice() throws PSErrorStackUnderflow,
            PSErrorTypeCheck {
        double ury = getOpStack().pop().toReal();
        double urx = getOpStack().pop().toReal();
        double lly = getOpStack().pop().toReal();
        double llx = getOpStack().pop().toReal();
        double wy = getOpStack().pop().toReal();
        double wx = getOpStack().pop().toReal();
        gstate.current().setDevice(new CacheDevice(wx, wy, llx, lly, urx,
                ury));
        gstate.current().initmatrix();
    }
    
    /**
     * PostScript op: setcachedevice2.
     * 
     * @throws PSErrorStackUnderflow Tried to pop an object from an empty stack.
     * @throws PSErrorTypeCheck A PostScript typecheck error occurred.
     */
    public void op_setcachedevice2() throws PSErrorStackUnderflow,
            PSErrorTypeCheck {
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
        gstate.current().setDevice(new CacheDevice(w0x, w0y, llx, lly, urx,
                ury));
        gstate.current().initmatrix();
    }
    
    /**
     * PostScript op: setcmykcolor.
     * 
     * @throws PSError A PostScript error occurred.
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public void op_setcmykcolor() throws PSError, IOException {
        double k = getOpStack().pop().toReal();
        double y = getOpStack().pop().toReal();
        double m = getOpStack().pop().toReal();
        double c = getOpStack().pop().toReal();
        double[] cmykValues = {c, m, y, k};
        gstate.current().setcolorspace(new PSObjectName("DeviceCMYK", true),
                                                                         false);
        gstate.current().setcolor(cmykValues);
    }
    
    /**
     * PostScript op: setcolor.
     * 
     * @throws PSError A PostScript error occurred.
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public void op_setcolor() throws PSError, IOException {
        int n = gstate.current().getColor().getNrComponents();
        double[] newColor = new double[n];
        for (int i = 0; i < n; i++) {
            newColor[n - i - 1] = getOpStack().pop().toReal();
        }
        gstate.current().setcolor(newColor);
    }
   
    /**
     * PostScript op: setcolorspace.
     * 
     * @throws PSError A PostScript error occurred.
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public void op_setcolorspace() throws PSError, IOException {
        PSObject arrayOrName = getOpStack().pop();
        gstate.current().setcolorspace(arrayOrName, true);
    }
   
    /**
     * PostScript op: setdash.
     * 
     * @throws PSError A PostScript error occurred.
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public void op_setdash() throws PSError, IOException {
        double offset = getOpStack().pop().toReal();
        PSObjectArray array = getOpStack().pop().toArray();
        
        gstate.current().setDashPattern(array);
        gstate.current().setDashOffset(offset);
    }
    
    /**
     * PostScript op: setflat.
     * 
     * @throws PSErrorStackUnderflow Tried to pop an object from an empty stack.
     * @throws PSErrorTypeCheck A PostScript typecheck error occurred.
     */
    public void op_setflat() throws PSErrorStackUnderflow, PSErrorTypeCheck {
        double num = getOpStack().pop().toReal();
        num = Math.max(num, 0.2);
        num = Math.min(num, 100);
        gstate.current().setFlat(num);
    }
   
    /**
     * PostScript op: setfont.
     * 
     * @throws PSErrorTypeCheck A PostScript typecheck error occurred.
     * @throws PSErrorStackUnderflow Tried to pop an object from an empty stack.
     */
    public void op_setfont() throws PSErrorTypeCheck, PSErrorStackUnderflow {
        PSObjectFont font = getOpStack().pop().toFont();
        gstate.current().setFont(font);
    }
   
    /**
     * PostScript op: setgray.
     * 
     * @throws PSError A PostScript error occurred.
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public void op_setgray() throws PSError, IOException {
        double[] num = { getOpStack().pop().toReal() };
        gstate.current().setcolorspace(new PSObjectName("DeviceGray", true),
                                                                         false);
        gstate.current().setcolor(num);
    }
    
    /**
     * PostScript op: sethsbcolor.
     * 
     * @throws PSError A PostScript error occurred.
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public void op_sethsbcolor() throws PSError, IOException {
        double brightness = getOpStack().pop().toReal();
        double saturaration = getOpStack().pop().toReal();
        double hue = getOpStack().pop().toReal();
        double[] rgbValues = RGB.convertHSBtoRGB(hue, saturaration, brightness);
        gstate.current().setcolorspace(new PSObjectName("DeviceRGB", true),
                                                                         false);
        gstate.current().setcolor(rgbValues);
    }
   
    /**
     * PostScript op: setlinecap.
     * 
     * @throws PSError A PostScript error occurred.
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public void op_setlinecap() throws PSError, IOException {
        int cap = getOpStack().pop().toNonNegInt();
        gstate.current().getDevice().setlinecap(cap);
    }
   
    /**
     * PostScript op: setlinejoin.
     * 
     * @throws PSError A PostScript error occurred.
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public void op_setlinejoin() throws PSError, IOException {
        int join = getOpStack().pop().toNonNegInt();
        gstate.current().getDevice().setlinejoin(join);
    }
   
    /**
     * PostScript op: setlinewidth.
     * 
     * @throws PSError A PostScript error occurred.
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public void op_setlinewidth() throws PSError, IOException {
        double lineWidth = getOpStack().pop().toReal();
        gstate.current().setLineWidth(Math.abs(lineWidth));
    }
   
    /**
     * PostScript op: setmatrix.
     * 
     * @throws PSError A PostScript error occurred.
     */
    public void op_setmatrix() throws PSError {
        PSObjectMatrix matrix = getOpStack().pop().toMatrix();
        gstate.current().getCtm().copy(matrix);
    }
    
    /**
     * PostScript op: setmiterlimit.
     * 
     * @throws PSErrorStackUnderflow Tried to pop an object from an empty stack.
     * @throws PSErrorTypeCheck A PostScript typecheck error occurred.
     * @throws PSErrorRangeCheck A PostScript rangecheck error occurred.
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public void op_setmiterlimit() throws PSErrorStackUnderflow,
            PSErrorTypeCheck, PSErrorRangeCheck, IOException {
        double num = getOpStack().pop().toReal();
        if (num < 1.0) {
            throw new PSErrorRangeCheck();
        }
        gstate.current().getDevice().setmiterlimit(num);
    }
    
    /**
     * PostScript op: setrgbcolor.
     * 
     * @throws PSError A PostScript error occurred.
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public void op_setrgbcolor() throws PSError, IOException {
        double blue = getOpStack().pop().toReal();
        double green = getOpStack().pop().toReal();
        double red = getOpStack().pop().toReal();
        double[] rgbValues = {red, green, blue};
        gstate.current().setcolorspace(new PSObjectName("DeviceRGB", true),
                                                                         false);
        gstate.current().setcolor(rgbValues);
    }
    
    /**
     * PostScript op: setscreen.
     * 
     * @throws PSErrorStackUnderflow Tried to pop an object from an empty stack.
     */
    public void op_setscreen() throws PSErrorStackUnderflow {
        // This operator does not have any meaning in Eps2pgf. It just pops
        // the arguments and continues.
        getOpStack().pop();  // pop proc/halftone
        getOpStack().pop();  // pop angle
        getOpStack().pop();  // pop frequency
    }
   
    /**
     * PostScript op: shfill.
     * 
     * @throws PSError A PostScript error occurred.
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public void op_shfill() throws PSError, IOException {
        PSObjectDict dict = getOpStack().pop().toDict();
        gstate.current().getDevice().shfill(dict, gstate.current());
    }
    
    /**
     * PostScript op: show.
     * 
     * @throws PSError A PostScript error occurred.
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    public void op_show() throws PSError, IOException, ProgramError {
        PSObjectString string = getOpStack().pop().toPSString();
        double[] dpos = textHandler.showText(
                gstate.current().getDevice(), string);
        gstate.current().rmoveto(dpos[0], dpos[1]);
    }
    
    /**
     * PostScript op: showpage.
     */
    public void op_showpage() {
        // This operator has no meaning in eps2pgf
    }
    
    /**
     * PostScript op: sin.
     * 
     * @throws PSErrorStackUnderflow Tried to pop an object from an empty stack.
     * @throws PSErrorTypeCheck A PostScript typecheck error occurred.
     */
    public void op_sin() throws PSErrorStackUnderflow, PSErrorTypeCheck {
        double angle = getOpStack().pop().toReal();
        getOpStack().push(new PSObjectReal(Math.sin(angle * Math.PI / 180.0)));
    }
    
    /**
     * PostScript op: sqrt.
     * 
     * @throws PSErrorStackUnderflow Tried to pop an object from an empty stack.
     * @throws PSErrorTypeCheck A PostScript typecheck error occurred.
     * @throws PSErrorRangeCheck A PostScript rangecheck error occurred.
     */
    public void op_sqrt() throws PSErrorStackUnderflow, PSErrorTypeCheck,
            PSErrorRangeCheck {
        double x = getOpStack().pop().toNonNegReal();
        x = Math.sqrt(x);
        getOpStack().push(new PSObjectReal(x));
    }
   
    /**
     * PostScript op: stop.
     * 
     * @throws PSErrorInvalidStop Stop operator is not allowed here.
     */
    public void op_stop() throws PSErrorInvalidStop {
        throw new PSErrorInvalidStop();
    }
    
    /**
     * PostScript op: stopped.
     * 
     * @throws PSErrorStackUnderflow Tried to pop an object from an empty stack.
     * @throws PSErrorUnimplemented Encountered a PostScript feature that is not
     * (yet) implemented.
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    public void op_stopped() throws PSErrorStackUnderflow, PSErrorUnimplemented,
            ProgramError {
        PSObject any = getOpStack().pop();
        try {
            runObject(any);
        } catch (PSErrorUnimplemented e) {
            // Don't catch unimplemented errors since they indicate that
            // eps2pgf is not fully implemented. It is not an actual
            // postscript error
            throw e;
        } catch (PSError e) {
            getOpStack().push(new PSObjectBool(true));
            return;
        }
        getOpStack().push(new PSObjectBool(false));
    }
    
    /**
     * PostScript op: string.
     * 
     * @throws PSErrorStackUnderflow Tried to pop an object from an empty stack.
     * @throws PSErrorTypeCheck A PostScript typecheck error occurred.
     * @throws PSErrorRangeCheck A PostScript rangecheck error occurred.
     */
    public void op_string() throws PSErrorStackUnderflow, PSErrorTypeCheck,
            PSErrorRangeCheck {
        int n = getOpStack().pop().toNonNegInt();
        getOpStack().push(new PSObjectString(n));
    }
   
    /**
     * PostScript op: stringwidth.
     * 
     * @throws PSError A PostScript error occurred.
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    public void op_stringwidth() throws PSError, IOException, ProgramError {
        PSObjectString string = getOpStack().pop().toPSString();
        string.checkAccess(false, true, false);
        
        double[] dpos = textHandler.showText(gstate.current().getDevice(),
                                             string, true);
        getOpStack().push(new PSObjectReal(dpos[0]));
        getOpStack().push(new PSObjectReal(dpos[1]));
    }

    /**
     * PostScript op: stroke.
     * 
     * @throws PSError A PostScript error occurred.
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public void op_stroke() throws PSError, IOException {
        gstate.current().getDevice().stroke(gstate.current());
        op_newpath();
    }
   
    /**
     * PostScript op: [.
     */
    public void op_sqBrackLeft() {
        getOpStack().push(new PSObjectMark());
    }
    
    /**
     * PostScript op: ].
     * 
     * @throws PSErrorStackUnderflow Tried to pop an object from an empty stack.
     * @throws PSErrorTypeCheck A PostScript typecheck error occurred.
     * @throws PSErrorUnmatchedMark Expected mark not found.
     */
    public void op_sqBrackRight() throws PSErrorStackUnderflow,
            PSErrorTypeCheck, PSErrorUnmatchedMark {
        op_counttomark();
        int n = getOpStack().pop().toInt();
        PSObject[] objs = new PSObject[n];
        for (int i = n - 1; i >= 0; i--) {
            objs[i] = getOpStack().pop();
        }
        getOpStack().pop();  // clear mark
        getOpStack().push(new PSObjectArray(objs));
        
        
    }
    
    /**
     * PostScript op: store.
     * 
     * @throws PSError A PostScript error occurred.
     */
    public void op_store() throws PSError {
        PSObject value = getOpStack().pop();
        PSObject key = getOpStack().pop();
        
        PSObject dictWithKey = getDictStack().where(key);
        if (dictWithKey != null) {
            getDictStack().where(key).checkAccess(false, false, true);
        }
        
        getDictStack().store(key, value);
    }

    /**
     * PostScript op: sub.
     * 
     * @throws PSError A PostScript error occurred.
     */
    public void op_sub() throws PSError {
        PSObject num2 = getOpStack().pop();
        PSObject num1 = getOpStack().pop();
        getOpStack().push(num1.sub(num2));
    }
    
    /**
     * PostScript op: token.
     * 
     * @throws PSError A PostScript error occurred.
     */
    public void op_token() throws PSError {
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
    
    /**
     * PostScript op: transform.
     * 
     * @throws PSError A PostScript error occurred.
     */
    public void op_transform() throws PSError {
        PSObject obj = getOpStack().pop();
        PSObjectMatrix matrix = null;
        try {
            matrix = obj.toMatrix();
        } catch (PSErrorTypeCheck e) {
            
        }
        double y;
        if (matrix == null) {
            matrix = gstate.current().getCtm();
            y = obj.toReal();
        } else {
            y = getOpStack().pop().toReal();
        }
        double x = getOpStack().pop().toReal();
        double[] transformed = matrix.transform(x, y);
        getOpStack().push(new PSObjectReal(transformed[0]));
        getOpStack().push(new PSObjectReal(transformed[1]));
    }
    
    /**
     * PostScript op: translate.
     * 
     * @throws PSError A PostScript error occurred.
     */
    public void op_translate() throws PSError {
        PSObject obj = getOpStack().pop();
        double tx, ty;
        if (obj instanceof PSObjectArray) {
            PSObjectMatrix matrix = obj.toMatrix();
            ty = getOpStack().pop().toReal();
            tx = getOpStack().pop().toReal();
            matrix.translate(tx, ty);
            getOpStack().push(matrix);
        } else {
            ty = obj.toReal();
            tx = getOpStack().pop().toReal();
            gstate.current().getCtm().translate(tx, ty);
            gstate.current().updatePosition();
        }
    }
    
    /**
     * PostScript op: true.
     */
    public void op_true() {
        getOpStack().push(new PSObjectBool(true));
    }
    
    /**
     * PostScript op: truncate.
     * 
     * @throws PSErrorStackUnderflow Tried to pop an object from an empty stack.
     * @throws PSErrorTypeCheck A PostScript typecheck error occurred.
     */
    public void op_truncate() throws PSErrorStackUnderflow, PSErrorTypeCheck {
        PSObject obj = getOpStack().pop();
        getOpStack().push(obj.truncate());
    }
    
    /**
     * PostScript op: type.
     * 
     * @throws PSErrorStackUnderflow Tried to pop an object from an empty stack.
     */
    public void op_type() throws PSErrorStackUnderflow {
        PSObject any = getOpStack().pop();
        getOpStack().push(new PSObjectName(any.type(), false));
    }
    
    /**
     * PostScript op: undef.
     * 
     * @throws PSError A PostScript error occurred.
     */
    public void op_undef() throws PSError {
        PSObject key = getOpStack().pop();
        PSObjectDict dict = getOpStack().pop().toDict();
        dict.checkAccess(false, false, true);
        
        dict.undef(key);
    }
    
    /**
     * PostScript op: wcheck.
     * 
     * @throws PSErrorStackUnderflow Tried to pop an object from an empty stack.
     * @throws PSErrorTypeCheck A PostScript typecheck error occurred.
     */
    public void op_wcheck() throws PSErrorStackUnderflow, PSErrorTypeCheck {
        PSObject obj = getOpStack().pop();
        boolean chk = obj.wcheck();
        getOpStack().push(new PSObjectBool(chk));
    }
    
    /**
     * PostScript op: where.
     * 
     * @throws PSErrorStackUnderflow Tried to pop an object from an empty stack.
     * @throws PSErrorTypeCheck A PostScript typecheck error occurred.
     */
    public void op_where() throws PSErrorStackUnderflow, PSErrorTypeCheck {
        PSObject key = getOpStack().pop();
        PSObjectDict dict = getDictStack().where(key);
        if (dict == null) {
            getOpStack().push(new PSObjectBool(false));
        } else {
            getOpStack().push(dict);
            getOpStack().push(new PSObjectBool(true));
        }
    }
   
    /**
     * PostScript op: xcheck.
     * 
     * @throws PSErrorStackUnderflow Tried to pop an object from an empty stack.
     */
    public void op_xcheck() throws PSErrorStackUnderflow {
        PSObject any = getOpStack().pop();
        PSObjectBool check = new PSObjectBool(any.xcheck());
        getOpStack().push(check);
    }
    
    /**
     * PostScript op: xor.
     * 
     * @throws PSErrorStackUnderflow Tried to pop an object from an empty stack.
     * @throws PSErrorTypeCheck A PostScript typecheck error occurred.
     */
    public void op_xor() throws PSErrorStackUnderflow, PSErrorTypeCheck {
        PSObject obj2 = getOpStack().pop();
        PSObject obj1 = getOpStack().pop();
        getOpStack().push(obj1.xor(obj2));
    }
    
}
