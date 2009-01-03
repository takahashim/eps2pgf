/*
 * This file is part of Eps2pgf.
 *
 * Copyright 2007-2009 Paul Wagenaars <paul@wagenaars.org>
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

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Logger;

import net.sf.eps2pgf.Main;
import net.sf.eps2pgf.Options;
import net.sf.eps2pgf.ProgramError;
import net.sf.eps2pgf.io.PSStringInputStream;
import net.sf.eps2pgf.io.TextHandler;
import net.sf.eps2pgf.io.TextReplacements;
import net.sf.eps2pgf.ps.errors.PSError;
import net.sf.eps2pgf.ps.errors.PSErrorIOError;
import net.sf.eps2pgf.ps.errors.PSErrorInvalidAccess;
import net.sf.eps2pgf.ps.errors.PSErrorInvalidExit;
import net.sf.eps2pgf.ps.errors.PSErrorRangeCheck;
import net.sf.eps2pgf.ps.errors.PSErrorStackUnderflow;
import net.sf.eps2pgf.ps.errors.PSErrorTypeCheck;
import net.sf.eps2pgf.ps.errors.PSErrorUndefined;
import net.sf.eps2pgf.ps.errors.PSErrorUnmatchedMark;
import net.sf.eps2pgf.ps.errors.PSErrorUnregistered;
import net.sf.eps2pgf.ps.objects.PSObject;
import net.sf.eps2pgf.ps.objects.PSObjectArray;
import net.sf.eps2pgf.ps.objects.PSObjectBool;
import net.sf.eps2pgf.ps.objects.PSObjectDict;
import net.sf.eps2pgf.ps.objects.PSObjectFile;
import net.sf.eps2pgf.ps.objects.PSObjectFont;
import net.sf.eps2pgf.ps.objects.PSObjectInt;
import net.sf.eps2pgf.ps.objects.PSObjectMark;
import net.sf.eps2pgf.ps.objects.PSObjectMatrix;
import net.sf.eps2pgf.ps.objects.PSObjectName;
import net.sf.eps2pgf.ps.objects.PSObjectNull;
import net.sf.eps2pgf.ps.objects.PSObjectOperator;
import net.sf.eps2pgf.ps.objects.PSObjectReal;
import net.sf.eps2pgf.ps.objects.PSObjectSave;
import net.sf.eps2pgf.ps.objects.PSObjectString;
import net.sf.eps2pgf.ps.resources.ResourceManager;
import net.sf.eps2pgf.ps.resources.colors.DeviceCMYK;
import net.sf.eps2pgf.ps.resources.colors.DeviceGray;
import net.sf.eps2pgf.ps.resources.colors.PSColor;
import net.sf.eps2pgf.ps.resources.colors.DeviceRGB;
import net.sf.eps2pgf.ps.resources.filters.EexecDecode;
import net.sf.eps2pgf.ps.resources.filters.FilterManager;
import net.sf.eps2pgf.ps.resources.fonts.FontManager;
import net.sf.eps2pgf.ps.resources.outputdevices.CacheDevice;
import net.sf.eps2pgf.ps.resources.outputdevices.LOLDevice;
import net.sf.eps2pgf.ps.resources.outputdevices.NullDevice;
import net.sf.eps2pgf.ps.resources.outputdevices.OutputDevice;
import net.sf.eps2pgf.ps.resources.outputdevices.PGFDevice;
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
    
    /**
     * Continuation stack. Arguments for continuation operators are stored on
     * this stack. These special operators are for example used in loops: such
     * as 'repeat', 'for' and 'forall'. Each time a continuation function is
     * pushed onto the execution stack its accompanying arguments are pushed
     * onto this stack preceded by a null object. I.e. first a null object is
     * pushed on the continuation stack, followed by the arguments. 
     */
    private ArrayStack<PSObject> contStack = new ArrayStack<PSObject>();
    
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
    
    /** Resource manager. */
    private ResourceManager resourceManager;
    
    /** User-defined options. */
    private Options options;
    
    /** Interpreter parameters. */
    private InterpParams interpParams;
    
    /** Log information. */
    private final Logger log = Logger.getLogger("net.sourceforge.eps2pgf");
    
    /** Initialization time of interpreter. (milliseconds since 1970). */
    private long initializationTime;
    
    /**
     * Counter that increases by one each time the interpreter executes an
     * object.
     */
    private int interpCounter = 0;
    
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
        
        options = opts;        

        // Create graphics state stack with output device
        OutputDevice output;
        switch (opts.getOutputType()) {
            case PGF:
                output = new PGFDevice(outputWriter, options);
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
        
        options = new Options();

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
        // Initialization time of interpreter
        initializationTime = System.currentTimeMillis();
        
        // Initialize character encodings and fonts
        resourceManager = new ResourceManager();
        
        // Initialize interpreter parameters
        interpParams = new InterpParams(this);
        
        // Create dictionary stack
        setDictStack(new DictStack(this));

        gstate.current().getDevice().init();
        
        gstate.current().setcolorspace(new PSObjectName("DeviceGray", true));
        
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
            gstate.current().setClippingPath(defaultClippingPath.clone(null));
        }
    }
    
    /**
     * Get the continuation arguments stack.
     * 
     * @return The continuation stack.
     */
    public ArrayStack<PSObject> getContStack() {
        return contStack;
    }

    /**
     * Get the dictionary stack.
     * 
     * @return The current dictionary stack.
     */
    public DictStack getDictStack() {
        return dictStack;
    }

    /**
     * Set the dictionary stack.
     * 
     * @param newDictStack The new dictionary stack.
     */
    public void setDictStack(final DictStack newDictStack) {
        dictStack = newDictStack;
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
     * Get the interpreter options.
     * 
     * @return The current interpreter options.
     */
    public Options getOptions() {
        return options;
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
            
            // Do some error reporting using the handleerror procedure if an
            // error occurred.
            PSObjectDict dollarError = dictStack.lookup("$error").toDict();
            if (dollarError.lookup("newerror").toBool()) {
                PSObjectDict errorDict = dictStack.lookup("errordict").toDict();
                PSObject handleError = errorDict.get("handleerror");
                execStack.push(handleError);
                start();
            }
        } catch (PSError e) {
            throw new ProgramError("Encountered a PostScript error were they"
                    + " should not occur.");
        } finally {
            this.gstate.current().getDevice().finish();
        }
    }
    
    /**
     * Execute all objects on the execution stack one by one.
     * 
     * @throws PSError A PostScript error occurred.
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    public void run() throws PSError, ProgramError {
        while (this.getExecStack().size() > 0) {
            PSObject obj = this.getExecStack().getNextToken(this);
            if (obj != null) {
                interpCounter++;
                ArrayStack<PSObject> opStackCopy = getOpStack().clone();
                try {
                    executeObject(obj, false);
                } catch (PSError e) {
                    opStack = opStackCopy;
                    opStack.push(obj);
                    PSObjectDict errordict = 
                        dictStack.lookup("errordict").toDict();
                    PSObject errorproc = errordict.get(e.getErrorName());
                    execStack.push(errorproc);
                }
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
        //TODO reimplement methods that use this method. I think it's better to
        //     implement them by working with the stack directly.
        PSObject topAtStart = execStack.getTop();
        executeObject(objectToRun);
        try {
            while (execStack.getTop() != topAtStart) {
                PSObject obj = getExecStack().getNextToken(this);
                if (obj != null) {
                    executeObject(obj, false);
                } else {
                    break;
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
     * @param indirect Indicates how the object was encountered: directly
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
                        
                        throw new ProgramError("Unexpected exception during"
                                + " operator call.");
                    }
                }
            } else if (obj instanceof PSObjectNull) {
                // don't do anything with an executable null
            } 
        }  // end of check whether object is literal
    }
    
    //TODO remove operators from main Interpreter class and put them in several
    //      sub-classes: e.g. InterpreterOpsA, InterpreterOpsB, etc... Each of
    //      these classes is passed the actual Interpreter upon construction.
    //      This means that all operator implementations must be changed to
    //      refer to the interp.<vars> instead of <vars> directly.
    
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
     * PostScript op: awidthshow.
     * 
     * @throws PSErrorUnregistered Encountered a PostScript feature that is not
     * (yet) implemented.
     */
    public void op_awidthshow() throws PSErrorUnregistered {
        throw new PSErrorUnregistered("awidthshow operator");
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
     * @throws PSErrorUnregistered Encountered a PostScript feature that is not
     * (yet) implemented.
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    public void op_clip()
            throws ProgramError, PSErrorUnregistered, IOException {
        
        gstate.current().clip();
        gstate.current().getDevice().clip(gstate.current().getClippingPath());
    }
    
    /**
     * PostScript op: clippath.
     * 
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    public void op_clippath() throws ProgramError {
        gstate.current().setPath(
                gstate.current().getClippingPath().clone(null));
    }
    
    /**
     * PostScript op: cliprestore.
     * 
     * @throws PSErrorUnregistered Encountered a PostScript feature that is not
     * (yet) implemented.
     */
    public void op_cliprestore() throws PSErrorUnregistered {
        throw new PSErrorUnregistered("PostScript operator 'cliprestore'");
    }
    
    /**
     * PostScript op: clipsave.
     * 
     * @throws PSErrorUnregistered Encountered a PostScript feature that is not
     * (yet) implemented.
     */
    public void op_clipsave() throws PSErrorUnregistered {
        throw new PSErrorUnregistered("PostScript operator 'clipsave'");
    }
    
    /**
     * PostScript op: closefile.
     * 
     * @throws PSError A PostScript error occurred.
     */
    public void op_closefile() throws PSError {
        PSObjectFile file = getOpStack().pop().toFile();
        file.closeFile();
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
     * PostScript op: colorimage.
     * 
     * @throws PSError A PostScript error occurred.
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public void op_colorimage() throws PSError, ProgramError, IOException {
        // Number of color components
        PSColor colorSpace;
        int ncomp = getOpStack().pop().toInt();
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
        boolean multi = getOpStack().pop().toBool();
        
        // Get the data source(s)
        PSObject dataSource;
        if (multi) {
            PSObject[] sources = new PSObject[ncomp];
            for (int i = (ncomp - 1); i >= 0; i++) {
                sources[i] = getOpStack().pop();
            }
            dataSource = new PSObjectArray(sources);
        } else {
            dataSource = getOpStack().pop();
        }
        
        // Read last four arguments
        PSObjectMatrix matrix = getOpStack().pop().toMatrix();
        int bitsPerComponent = getOpStack().pop().toInt();
        int height = getOpStack().pop().toInt();
        int width = getOpStack().pop().toInt();
        
        // Construct an image dictionary
        PSObjectDict dict = new PSObjectDict();
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
        dict.setKey(Image.DECODE, new PSObjectArray(decode));
        
        Image image = new Image(dict, this, colorSpace);
        gstate.current().getDevice().image(image);
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
     * PostScript op: currentblackgeneration.
     */
    public void op_currentblackgeneration() {
        getOpStack().push(gstate.current().currentBlackGeneration());
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
     * PostScript op: currentcolorrendering.
     */
    public void op_currentcolorrendering() {
        getOpStack().push(gstate.current().currentColorRendering());
    }
    
    /**
     * PostScript op: currentcolorspace.
     */
    public void op_currentcolorspace() {
        getOpStack().push(gstate.current().getColor().getColorSpace());
    }
    
    /**
     * PostScript op: currentcolortransfer.
     * 
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    public void op_currentcolortransfer() throws ProgramError {
        PSObjectArray procs = gstate.current().currentColorTransfer();
        try {
            for (int i = 0; i < 4; i++) {
                getOpStack().push(procs.get(i));
            }
        } catch (PSErrorRangeCheck e) {
            throw new ProgramError("rangecheck in op_currentcolortransfer()");
        }
    }
    
    /**
     * PostScript op: currentdevparams.
     * 
     * @throws PSError A PostScript error occurred.
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    public void op_currendevparams() throws PSError, ProgramError {
        String device = getOpStack().pop().toPSString().toString();
        getOpStack().push(interpParams.currentDeviceParams(device));
    }
    
    /**
     * PostScript op: currentdict.
     */
    public void op_currentdict() {
        getOpStack().push(getDictStack().peekDict());
    }
    
    /**
     * PostScript op: currentdash.
     * 
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    public void op_currentdash() throws ProgramError {
        opStack.push(gstate.current().getDashPattern().clone(null));
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
        double flat = gstate.current().currentFlatness();
        getOpStack().push(new PSObjectReal(flat));
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
     * PostScript op: currenthalftone.
     */
    public void op_currenthalftone() {
        getOpStack().push(gstate.current().currentHalftone());
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
     * PostScript op: currentoverprint.
     */
    public void op_currentoverprint() {
        boolean overprint = gstate.current().currentOverprint();
        getOpStack().push(new PSObjectBool(overprint));
    }
    
    /**
     * PostScript op: currentpagedevice.
     */
    public void op_currentpagedevice() {
        // Currently, this operator will always return an empty dictionary
        // indicating that there is no page.
        PSObjectDict emptyDict = new PSObjectDict();
        getOpStack().push(emptyDict);
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
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    public void op_currentscreen() throws PSError, ProgramError {
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
            getOpStack().push(Parser.convertToPSObject(spotFunction, this));
        } catch (PSErrorIOError ex) {
            // this can never happen
        } catch (IOException ex) {
            // this can never happen
        }
    }
    
    /**
     * PostScript op: currentsmoothness.
     */
    public void op_currentsmoothness() {
        double smoothness = gstate.current().currentSmoothness();
        getOpStack().push(new PSObjectReal(smoothness));
    }
    
    /**
     * PostScript op: currentstrokeadjust.
     */
    public void op_currentstrokeadjust() {
        getOpStack().push(new PSObjectBool(gstate.current().getStrokeAdjust()));
    }
    
    /**
     * PostScript op: currentsystemparams.
     * 
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    public void op_currentsystemparams() throws ProgramError {
        getOpStack().push(interpParams.currentSystemParams());
    }
    
    /**
     * PostScript op: currenttransfer.
     * 
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    public void op_currenttransfer() throws ProgramError {
        getOpStack().push(gstate.current().currentTransfer());
    }
    
    /**
     * PostScript op: currentundercolorremoval.
     */
    public void op_currentundercolorremoval() {
        getOpStack().push(gstate.current().currentUndercolorRemoval());
    }
    
    /**
     * PostScript op: currentuserparams.
     * 
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    public void op_currentuserparams() throws ProgramError {
        getOpStack().push(interpParams.currentUserParams());
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
     * @throws PSError A PostScript error occurred.
     */
    public void op_cvlit() throws PSError {
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
     * PostScript op: cshow.
     * 
     * @throws PSErrorUnregistered Encountered a PostScript feature that is not
     * (yet) implemented.
     */
    public void op_cshow() throws PSErrorUnregistered {
        throw new PSErrorUnregistered("cshow operator");
    }
    
    /**
     * PostScript op: >>.
     * 
     * @throws PSError A PostScript error occurred.
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    public void op_dblGreaterBrackets() throws PSError, ProgramError {
        ArrayStack<PSObject> os = getOpStack();
        PSObjectDict dict = new PSObjectDict();
        while (true) {
            PSObject value = os.pop();
            if (value instanceof PSObjectMark) {
                break;
            }
            PSObject key = os.pop();
            if (value instanceof PSObjectMark) {
                throw new PSErrorRangeCheck();
            }
            dict.setKey(key, value);
        }
        os.push(dict);
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
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    public void op_definefont() throws PSError, ProgramError {
        PSObjectFont font = getOpStack().pop().toFont();
        PSObject key = getOpStack().pop();
        PSObject obj = resourceManager.defineResource(ResourceManager.CAT_FONT,
                key, font);
        getOpStack().push(obj);
    }
    
    /**
     * PostScript op: defineresource.
     * 
     * @throws PSError A PostScript error occurred.
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    public void op_defineresource() throws PSError, ProgramError {
        PSObjectName category = getOpStack().pop().toName();
        PSObject instance = getOpStack().pop();
        PSObject key = getOpStack().pop();
        PSObject obj = resourceManager.defineResource(category, key, instance);
        getOpStack().push(obj);
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
        
        getDictStack().pushDict(getDictStack().lookup("systemdict").toDict());
        
        getExecStack().push(getDictStack().eps2pgfEexec);
        getContStack().push(new PSObjectNull());
        
        getExecStack().push(eexecFile);
    }
    
    /**
     * PostScript op: end.
     * 
     * @throws PSError A PostScript error occurred.
     */
    public void op_end() throws PSError {
        getDictStack().popDict();
    }
    
    /**
     * PostScript op: eoclip.
     * 
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws PSError A PostScript error occurred.
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    public void op_eoclip() throws ProgramError, PSError, IOException {
        gstate.current().clip();
        gstate.current().getDevice().eoclip(gstate.current());
    }
    
    /**
     * PostScript op: eofill.
     * 
     * @throws PSError A PostScript error occurred.
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    public void op_eofill() throws PSError, IOException, ProgramError {
        gstate.current().getDevice().eofill(gstate.current());
        op_newpath();
    }
    
    /**
     * Internal Eps2pgf operator. Continuation function for 'for' operator.
     * Input arguments: null current increment limit proc
     * Note: right is top of stack
     * 
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    public void op_eps2pgffor() throws ProgramError {
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
    
    /**
     * Internal Eps2pgf operator. Continuation function for 'forall' operator.
     * Input arguments: null nrItemsPerLoop itemList proc
     * Note: right is top of stack
     * 
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    public void op_eps2pgfforall() throws ProgramError {
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
    
    /**
     * Internal Eps2pgf operator: eps2pgfendofstopped. It indicates that the
     * end of a 'stopped' context has been reached.
     * 
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    public void op_eps2pgfstopped() throws ProgramError {
        ArrayStack<PSObject> cs = getContStack();
        try {
            // Pop arguments from continuation stack
            cs.pop().toNull();
            
            // Get current value of 'newerror' and set it to false
            PSObjectDict dollarError = dictStack.lookup("$error").toDict();
            boolean newError = dollarError.lookup("newerror").toBool();
            dollarError.setKey("newerror", false);
            
            // Push results on operand stack
            getOpStack().push(new PSObjectBool(newError));
        } catch (PSError e) {
            throw new ProgramError("An PS error: " + e.getMessage());
        }
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
     * Internal Eps2pgf operator: continuation function for looping context
     * operator.
     * 
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    public void op_eps2pgfcshow() throws ProgramError {
        throw new ProgramError("Continuation function not yet implemented.");
    }
    
    /**
     * Internal Eps2pgf operator. Continuation function for 'eexec' operator.
     * 
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    public void op_eps2pgfeexec() throws ProgramError {
        ArrayStack<PSObject> cs = getContStack();
        try {
            // Pop arguments from continuation stack
            cs.pop().toNull();
            
            // Remove top systemdict from dictstack
            PSObjectDict dict = getDictStack().popDict();
            PSObjectDict sysdict = getDictStack().lookup("systemdict").toDict();
            if (dict != sysdict) {
                throw new ProgramError("Top dict is not equal to systemdict.");
            }
        } catch (PSError e) {
            throw new ProgramError("An PS error: " + e.getMessage());
        }        
    }
    
    /**
     * Internal Eps2pgf operator: implements default error-handling procedure.
     * 
     * @throws PSError A PostScript error occurred.
     */
    public void op_eps2pgferrorproc() throws PSError {
        ArrayStack<PSObject> os = getOpStack();
        PSObjectName errName = os.pop().toName();
        PSObjectDict dollarError = dictStack.lookup("$error").toDict();
        dollarError.setKey("newerror", true);
        dollarError.setKey("errorname", errName);
        dollarError.setKey("command", os.pop());
        dollarError.setKey("errorinfo", new PSObjectNull());
        
        boolean recordStacks = dollarError.get("recordstacks").toBool();
        if (recordStacks) {
            PSObjectArray arr = new PSObjectArray();
            for (int i = 0; i < os.size(); i++) {
                arr.addToEnd(os.peek(os.size() - i - 1));
            }
            dollarError.setKey("ostack", arr);
            
            os.push(new PSObjectArray(execStack.size()));
            op_execstack();
            PSObjectArray estack = os.pop().toArray();
            estack = estack.getinterval(0, estack.size() - 1);
            dollarError.setKey("estack", estack);
            
            os.push(new PSObjectArray(dictStack.countdictstack()));
            op_dictstack();
            dollarError.setKey("dstack", opStack.pop());
        }
    }
    
    /**
     * Internal Eps2pgf operator: continuation function for looping context
     * operator.
     * 
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    public void op_eps2pgffilenameforall() throws ProgramError {
        throw new ProgramError("Continuation function not yet implemented.");
    }
    
    /**
     * Internal Eps2pgf operator. Default handleerror procedure.
     * 
     * @throws PSError A PostScript error occurred.
     */
    public void op_eps2pgfhandleerror() throws PSError {
        PSObjectDict de = dictStack.lookup("$error").toDict();
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
    
    /**
     * Internal Eps2pgf operator: continuation function for looping context
     * operator.
     * 
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    public void op_eps2pgfkshow() throws ProgramError {
        throw new ProgramError("Continuation function not yet implemented.");
    }
    
    /**
     * Internal Eps2pgf operator. Continuation function for 'loop' operator.
     * Input arguments: null proc
     * Note: right is top of stack
     * 
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    public void op_eps2pgfloop() throws ProgramError {
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
    
    /**
     * Internal Eps2pgf operator: continuation function for looping context
     * operator.
     * Input arguments: null path move line curve close
     * Note: right is top of continuation stack 
     * 
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    public void op_eps2pgfpathforall() throws ProgramError {
        ArrayStack<PSObject> cs = getContStack();
        ArrayStack<PSObject> os = getOpStack();
        ExecStack es = getExecStack();
        PSObjectMatrix ctm = gstate.current().getCtm();
        
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
    
    /**
     * Internal Eps2pgf operator. Continuation function for 'repeat' operator.
     * Input arguments: null repeatcount proc
     * Note: right is top of stack
     * 
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    public void op_eps2pgfrepeat() throws ProgramError {
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
    
    /**
     * Internal Eps2pgf operator: continuation function for looping context
     * operator.
     * 
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    public void op_eps2pgfresourceforall() throws ProgramError {
        throw new ProgramError("Continuation function not yet implemented.");
    }
    
    /**
     * PostScript op: errordict.
     * 
     * @throws PSErrorUnregistered Encountered a PostScript feature that is not
     * (yet) implemented.
     */
    public void op_errordict() throws PSErrorUnregistered {
        throw new PSErrorUnregistered("errordict operator");
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
     * @throws PSErrorInvalidExit 'exit' operator at invalid location.
     */
    public void op_exit() throws PSErrorInvalidExit {
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
                // Push obj back on execution stack to make sure we're still in
                // the 'stopped' context.
                es.push(obj);
                throw new PSErrorInvalidExit();
            }
        }
        
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
     * PostScript op: filenameforall.
     * 
     * @throws PSErrorUnregistered Encountered a PostScript feature that is not
     * (yet) implemented.
     */
    public void op_filenameforall() throws PSErrorUnregistered {
        throw new PSErrorUnregistered("filenameforall operator");
    }
    

    
    /**
     * PostScript op: fill.
     * 
     * @throws PSError A PostScript error occurred.
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    public void op_fill() throws PSError, IOException, ProgramError {
        gstate.current().getDevice().fill(gstate.current());
        op_newpath();
    }
    
    /**
     * PostScript op: filter.
     * 
     * @throws PSError A PostScript error occurred.
     */
    public void op_filter() throws PSError {
        PSObjectName name = getOpStack().pop().toName();
        PSObjectDict paramDict =
            FilterManager.getParameters(name, getOpStack());
        PSObject sourceOrTarget = getOpStack().pop();
        PSObjectFile file =
            FilterManager.filter(name, paramDict, sourceOrTarget);
        getOpStack().push(file);
    }
    
    /**
     * PostScript op: findencoding.
     * 
     * @throws PSError A PostScript error occurred.
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    public void op_findencoding() throws PSError, ProgramError {
        getOpStack().push(new PSObjectName("/Encoding"));
        op_findresource();
    }
    
    /**
     * PostScript op: findfont.
     * 
     * @throws PSError A PostScript error occurred.
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    public void op_findfont() throws PSError, ProgramError {
        PSObject key = getOpStack().pop();
        FontManager fontManager = resourceManager.getFontManager();
        getOpStack().push(fontManager.findFont(key));
    }
    
    /**
     * PostScript op: findresource.
     * 
     * @throws PSError A PostScript error occurred.
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    public void op_findresource() throws PSError, ProgramError {
        PSObjectName category = getOpStack().pop().toName();
        PSObject key = getOpStack().pop();
        PSObject obj = resourceManager.findResource(category, key);
        getOpStack().push(obj);
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
     * PostScript op: flushfile.
     * 
     * @throws PSError A PostScript error occurred.
     */
    public void op_flushfile() throws PSError {
        PSObjectFile file = getOpStack().pop().toFile();
        file.flushFile();
    }
    
    /**
     * PostScript op: for.
     * 
     * @throws PSError A PostScript error occurred.
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    public void op_for() throws PSError, ProgramError {
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
    
    /**
     * PostScript op: forall.
     * 
     * @throws PSError A PostScript error occurred.
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    public void op_forall() throws PSError, ProgramError {
        ArrayStack<PSObject> os = getOpStack();
        ExecStack es = getExecStack();
        ArrayStack<PSObject> cs = getContStack();

        PSObject proc = os.pop();
        proc.checkAccess(true, false, false);
        PSObject obj = os.pop();
        obj.checkAccess(false, true, false);
        
        List<PSObject> items = obj.getItemList();
        int nr = items.remove(0).toNonNegInt();
        PSObjectArray itemListArray = new PSObjectArray(items);
        
        cs.push(new PSObjectNull());
        cs.push(new PSObjectInt(nr));
        cs.push(itemListArray);
        cs.push(proc);

        es.push(getDictStack().eps2pgfForall);        
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
        gstate.restoreGstate(true, null);
    }
    
    /**
     * PostScript op: grestoreall.
     * 
     * @throws PSError A PostScript error occurred.
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    public void op_grestoreall() throws PSError, IOException, ProgramError {
        gstate.restoreAllGstate(true, null);
    }
    
    /**
     * PostScript op: gsave.
     * 
     * @throws PSError A PostScript error occurred.
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    public void op_gsave() throws PSError, IOException, ProgramError {
        gstate.saveGstate(true, null);
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
            getExecStack().push(proc);
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
            getExecStack().push(proc1);
        } else {
            getExecStack().push(proc2);
        }
    }
    
    /**
     * PostScript op: image.
     * 
     * @throws PSError A PostScript error occurred.
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public void op_image() throws PSError, ProgramError, IOException {
        PSObject dictOrDataSrc = getOpStack().pop();
        PSObjectDict dict;
        
        PSColor colorSpace;
        if (dictOrDataSrc instanceof PSObjectDict) {
            // We have the one argument image operand
            // dict image.
            dict = dictOrDataSrc.toDict();
            colorSpace = gstate.current().getColor();
        } else {
            // We have the five argument image operand
            // width height bits/sample matrix datasrc image.
            PSObjectMatrix matrix = getOpStack().pop().toMatrix();
            int bitsPerSample = getOpStack().pop().toInt();
            int height = getOpStack().pop().toInt();
            int width = getOpStack().pop().toInt();
            
            dict = new PSObjectDict();
            dict.setKey(Image.IMAGE_TYPE, new PSObjectInt(1));
            dict.setKey(Image.WIDTH, new PSObjectInt(width));
            dict.setKey(Image.HEIGHT, new PSObjectInt(height));
            dict.setKey(Image.IMAGE_MATRIX, matrix);
            dict.setKey(Image.DATA_SOURCE, dictOrDataSrc);
            dict.setKey(Image.BITS_PER_COMPONENT,
                    new PSObjectInt(bitsPerSample));
            double[] decode = {0.0, 1.0};
            dict.setKey(Image.DECODE, new PSObjectArray(decode));
            colorSpace = new DeviceGray();
        }
        
        Image image = new Image(dict, this, colorSpace);
        gstate.current().getDevice().image(image);
    }
    
    /**
     * PostScript op: imagemask.
     * 
     * @throws PSErrorUnregistered Encountered a PostScript feature that is not
     * (yet) implemented.
     */
    public void op_imagemask() throws PSErrorUnregistered {
        throw new PSErrorUnregistered("imagemask operator");
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
     * @throws PSErrorUnregistered Encountered a PostScript feature that is not
     * (yet) implemented.
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    public void op_initclip()
            throws ProgramError, IOException, PSErrorUnregistered {
        
        gstate.current().setClippingPath(defaultClippingPath.clone(null));
        gstate.current().getDevice().clip(gstate.current().getClippingPath());
    }
    
    /**
     * PostScript op: initmatrix.
     */
    public void op_initmatrix() {
        gstate.current().initmatrix();
    }
    
    /**
     * PostScript op: internaldict.
     * 
     * @throws PSError A PostScript error occurred.
     */
    public void op_internaldict() throws PSError {
        int nr = getOpStack().pop().toInt();
        if (nr != 1183615869) {
            throw new PSErrorInvalidAccess();
        }
        PSObjectDict systemdict = dictStack.lookup("systemdict").toDict();
        getOpStack().push(systemdict.get(DictStack.KEY_INTERNALDICT));
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
     * PostScript op: kshow.
     * 
     * @throws PSErrorUnregistered Encountered a PostScript feature that is not
     * (yet) implemented.
     */
    public void op_kshow() throws PSErrorUnregistered {
        throw new PSErrorUnregistered("kshow operator");
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
        ExecStack es = getExecStack();
        ArrayStack<PSObject> cs = getContStack();
        
        es.push(getDictStack().eps2pgfLoop);
        
        cs.push(new PSObjectNull());
        cs.push(getOpStack().pop());
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
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    public void op_makefont() throws PSError, ProgramError {
        PSObjectMatrix matrix = getOpStack().pop().toMatrix();
        PSObjectDict font = getOpStack().pop().toDict();
        font = font.clone(null);
        PSObjectMatrix fontMatrix = font.lookup("FontMatrix").toMatrix();
        
        // Concatenate matrix to fontMatrix and store it back in font
        fontMatrix.concat(matrix);
        font.setKey("FontMatrix", fontMatrix);
        
        // Calculate the fontsize in LaTeX points
        PSObjectMatrix ctm = gstate.current().getCtm().clone(null);
        ctm.concat(fontMatrix);
        double fontSize = ctm.getMeanScaling() / 2.54 * 72.27;
        font.setKey("FontSize", new PSObjectReal(fontSize));
        
        getOpStack().push(font);
    }
    
    /**
     * PostScript op: makepattern.
     * 
     * @throws PSError A PostScript error occurred.
     */
    public void op_makepattern() throws PSError {
        //PSObjectMatrix matrix = getOpStack().pop().toMatrix();
        //PSObjectDict dict = getOpStack().pop().toDict();
        throw new PSErrorUnregistered("makepattern operator");
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
        ArrayStack<PSObject> os = getOpStack();
        PSObjectArray close = os.pop().toProc();
        PSObjectArray curve = os.pop().toProc();
        PSObjectArray line = os.pop().toProc();
        PSObjectArray move = os.pop().toProc();
        
        ArrayList<PathSection> sects = gstate.current().getPath().getSections();
        PSObjectArray path = new PSObjectArray();
        for (int i = 0; i < sects.size(); i++) {
            path.addToEnd(sects.get(i));
        }
        
        // Push objects on execution stack
        getExecStack().push(getDictStack().eps2pgfPathforall);
    
        // Push arguments on continuation stack
        ArrayStack<PSObject> cs = getContStack();
        cs.push(new PSObjectNull());
        cs.push(path);
        cs.push(move);
        cs.push(line);
        cs.push(curve);
        cs.push(close);
    }
    
    /**
     * PostScript op: picstr.
     * 
     * @throws PSError A PostScript error occurred.
     */
    public void op_picstr() throws PSError {
        throw new PSErrorUnregistered("operator: picstr");
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
     * PostScript op: product.
     */
    public void op_product() {
        getOpStack().push(new PSObjectString(Main.APP_NAME));
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
     * PostScript op: quit.
     * 
     * @throws PSError A PostScript error occurred.
     */
    public void op_quit() throws PSError {
        ExecStack estack = getExecStack();
        while (estack.pop() != null) {
            /* empty block */
        }

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
        op_filter();
        PSObjectFile file = getOpStack().pop().toFile();
        
        PSObjectString substring = file.readstring(string);
        boolean bool = (string.length() == substring.length());
        
        getOpStack().push(substring);
        getOpStack().push(new PSObjectBool(bool));
    }
    
    /**
     * PostScript op: readline.
     * 
     * @throws PSError A PostScript error occurred.
     */
    public void op_readline() throws PSError {
        PSObjectString string = getOpStack().pop().toPSString();
        PSObjectFile file = getOpStack().pop().toFile();
        PSObjectArray arr = file.readLine(string);
        getOpStack().push(arr.get(0));
        getOpStack().push(arr.get(1));
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
     * PostScript op: realtime.
     */
    public void op_realtime() {
        // Get the number of milliseconds since midnight
        Calendar now = Calendar.getInstance();
        int realtime = now.get(Calendar.HOUR_OF_DAY);
        realtime = 60 * realtime + now.get(Calendar.MINUTE);
        realtime = 60 * realtime + now.get(Calendar.SECOND);
        realtime = 1000 * realtime + now.get(Calendar.MILLISECOND);
        getOpStack().push(new PSObjectInt(realtime));
    }
    
    /**
     * PostScript op: rectclip.
     * 
     * @throws PSError A PostScript error occurred.
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    public void op_rectclip() throws ProgramError, PSError, IOException {
        rectPath();
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
        op_gsave();
        rectPath();
        op_fill();
        op_grestore();
    }
    
    /**
     * PostScript op: rectstroke.
     * 
     * @throws PSError A PostScript error occurred.
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public void op_rectstroke() throws PSError, ProgramError, IOException {
        op_gsave();
        
        // Check whether the top-most object is a matrix
        PSObjectMatrix matrix = null;
        try {
            matrix = getOpStack().peek().toMatrix();
            getOpStack().pop();
        } catch (PSErrorTypeCheck e) {
            /* apparently the first object is not a matrix */
        } catch (PSErrorRangeCheck e) {
            /* apparently the first object is not a matrix */
        }
        
        rectPath();
        
        if (matrix != null) {
            getOpStack().push(matrix);
            op_concat();
        }
        
        op_stroke();
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
        PSObject objN = getOpStack().pop();
        objN.toNonNegInt();
        
        ArrayStack<PSObject> cs = getContStack();
        
        cs.push(new PSObjectNull());
        cs.push(objN);
        cs.push(proc);
        
        getExecStack().push(getDictStack().eps2pgfRepeat);
    }
    
    /**
     * PostScript op: resourceforall.
     * 
     * @throws PSErrorUnregistered Encountered a PostScript feature that is not
     * (yet) implemented.
     */
    public void op_resourceforall() throws PSErrorUnregistered {
        throw new PSErrorUnregistered("resourceforall operator");
    }
    
    /**
     * PostScript op: resourcestatus.
     * 
     * @throws PSError A PostScript error occurred.
     */
    public void op_resourcestatus() throws PSError {
        PSObjectName category = getOpStack().pop().toName();
        PSObject key = getOpStack().pop();
        PSObjectArray ret = resourceManager.resourceStatus(category, key);
        for (int i = 0; i < ret.size(); i++) {
            getOpStack().push(ret.get(i));
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
        PSObjectSave save = getOpStack().pop().toSave();
        save.restore();
        gstate.restoreAllGstate(false, null);
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
        getOpStack().push(new PSObjectSave(this));
        gstate.saveGstate(false, null);
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
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    public void op_scalefont() throws ProgramError, PSError {
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
     * PostScript op: selectfont.
     * 
     * @throws PSError A PostScript error occurred.
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    public void op_selectfont() throws PSError, ProgramError {
        PSObject obj = getOpStack().pop();
        op_findfont();
        getOpStack().push(obj);
        if ((obj instanceof PSObjectInt) || (obj instanceof PSObjectReal)) {
            op_scalefont();
        } else if (obj instanceof PSObjectArray) {
            op_makefont();
        } else {
            throw new PSErrorTypeCheck();
        }
        op_setfont();
    }
    
    /**
     * PostScript op: setblackgeneration.
     * 
     * @throws PSError A PostScript error occurred.
     */
    public void op_setblackgeneration() throws PSError {
        PSObjectArray proc = getOpStack().pop().toProc();
        gstate.current().setBlackGeneration(proc);
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
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    public void op_setcmykcolor() throws PSError, IOException, ProgramError {
        double k = getOpStack().pop().toReal();
        double y = getOpStack().pop().toReal();
        double m = getOpStack().pop().toReal();
        double c = getOpStack().pop().toReal();
        double[] cmykValues = {c, m, y, k};
        gstate.current().setcolorspace(new PSObjectName("DeviceCMYK", true));
        gstate.current().setcolor(cmykValues);
    }
    
    /**
     * PostScript op: setcolor.
     * 
     * @throws PSError A PostScript error occurred.
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    public void op_setcolor() throws PSError, IOException, ProgramError {
        int n = gstate.current().getColor().getNrInputValues();
        double[] newColor = new double[n];
        for (int i = 0; i < n; i++) {
            newColor[n - i - 1] = getOpStack().pop().toReal();
        }
        gstate.current().setcolor(newColor);
    }
    
    /**
     * PostScript op: setcolorrendering.
     * 
     * @throws PSError A PostScript error occurred.
     */
    public void op_setcolorrendering() throws PSError {
        PSObjectDict dict = getOpStack().pop().toDict();
        gstate.current().setColorRendering(dict);
    }
   
    /**
     * PostScript op: setcolorspace.
     * 
     * @throws PSError A PostScript error occurred.
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    public void op_setcolorspace() throws PSError, IOException, ProgramError {
        PSObject arrayOrName = getOpStack().pop();
        gstate.current().setcolorspace(arrayOrName);
    }
    
    /**
     * PostScrip op: setcolortransfer.
     * 
     * @throws PSError A PostScript error occurred.
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    public void op_setcolortransfer() throws PSError, ProgramError {
        PSObjectArray grayProc = getOpStack().pop().toProc();
        PSObjectArray blueProc = getOpStack().pop().toProc();
        PSObjectArray greenProc = getOpStack().pop().toProc();
        PSObjectArray redProc = getOpStack().pop().toProc();
        gstate.current().setColorTransfer(redProc, greenProc, blueProc,
                grayProc);
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
     * PostScript op: setdevparams.
     * 
     * @throws PSError A PostScript error occurred.
     */
    public void op_setdevparams() throws PSError {
        PSObjectDict dict = getOpStack().pop().toDict();
        String device = getOpStack().pop().toPSString().toString();
        interpParams.setDeviceParams(device, dict);
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
        gstate.current().setFlatness(num);
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
     * PostScript op: setglobal.
     * 
     * @throws PSError A PostScript error occurred.
     */
    public void op_setglobal() throws PSError {
        PSObject nextGlobal = opStack.pop();
        if (!(nextGlobal instanceof PSObjectBool)) {
            throw new PSErrorTypeCheck();
        }
        PSObjectDict systemDict = dictStack.lookup("systemdict").toDict();
        systemDict.setKey("currentglobal", nextGlobal);
    }
   
    /**
     * PostScript op: setgray.
     * 
     * @throws PSError A PostScript error occurred.
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    public void op_setgray() throws PSError, IOException, ProgramError {
        double[] num = { getOpStack().pop().toReal() };
        gstate.current().setcolorspace(new PSObjectName("DeviceGray", true));
        gstate.current().setcolor(num);
    }
    
    /**
     * PostScript op: sethalftone.
     * 
     * @throws PSError A PostScript error occurred.
     */
    public void op_sethalftone() throws PSError {
        PSObject halftone = getOpStack().pop();
        gstate.current().setHalftone(halftone);
    }
    
    /**
     * PostScript op: sethsbcolor.
     * 
     * @throws PSError A PostScript error occurred.
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    public void op_sethsbcolor() throws PSError, IOException, ProgramError {
        double brightness = getOpStack().pop().toReal();
        double saturaration = getOpStack().pop().toReal();
        double hue = getOpStack().pop().toReal();
        double[] rgbValues = DeviceRGB.convertHSBtoRGB(hue, saturaration,
                brightness);
        gstate.current().setcolorspace(new PSObjectName("DeviceRGB", true));
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
        gstate.current().setLineCap(cap);
    }
   
    /**
     * PostScript op: setlinejoin.
     * 
     * @throws PSError A PostScript error occurred.
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public void op_setlinejoin() throws PSError, IOException {
        int join = getOpStack().pop().toNonNegInt();
        gstate.current().setLineJoin(join);
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
        gstate.current().setMiterLimit(num);
    }
    
    /**
     * PostScript op: setoverprint.
     * 
     * @throws PSError A PostScript error occurred.
     */
    public void op_setoverprint() throws PSError {
        boolean overprint = getOpStack().pop().toBool();
        gstate.current().setOverprint(overprint);
    }
    
    /**
     * PostScript op: setpacking.
     * 
     * @throws PSErrorStackUnderflow Tried to pop an object from an empty stack.
     * @throws PSErrorTypeCheck A PostScript typecheck error occurred.
     */
    public void op_setpacking() throws PSErrorStackUnderflow,
            PSErrorTypeCheck {
        
        boolean bool = getOpStack().pop().toBool();
        PSObjectDict systemDict = dictStack.lookup("systemdict").toDict();
        systemDict.setKey("currentpacking", new PSObjectBool(bool));
    }
    
    /**
     * PostScript op: setpagedevice.
     * 
     * @throws PSError A PostScript error occurred.
     */
    public void op_setpagedevice() throws PSError {
        // Just pop the dictionary from the stack and don't do anything with it.
        getOpStack().pop().toDict();
    }
    
    /**
     * PostScript op: setrgbcolor.
     * 
     * @throws PSError A PostScript error occurred.
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    public void op_setrgbcolor() throws PSError, IOException, ProgramError {
        double blue = getOpStack().pop().toReal();
        double green = getOpStack().pop().toReal();
        double red = getOpStack().pop().toReal();
        double[] rgbValues = {red, green, blue};
        gstate.current().setcolorspace(new PSObjectName("DeviceRGB", true));
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
     * PostScript op: setsmoothness.
     * 
     * @throws PSError A PostScript error occurred.
     */
    public void op_setsmoothness() throws PSError {
        double smoothness = getOpStack().pop().toReal();
        gstate.current().setSmoothness(smoothness);
    }
    
    /**
     * PostScript op: setstrokeadjust.
     * 
     * @throws PSErrorStackUnderflow Tried to pop an object from an empty stack.
     * @throws PSErrorTypeCheck A PostScript typecheck error occurred.
     */
    public void op_setstrokeadjust() throws PSErrorTypeCheck,
            PSErrorStackUnderflow {
        
        boolean bool = getOpStack().pop().toBool();
        gstate.current().setStrokeAdjust(bool);
    }
    
    /**
     * PostScript op: setsystemparams.
     * 
     * @throws PSError A PostScript error occurred.
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    public void op_setsystemparams() throws PSError, ProgramError {
        PSObjectDict dict = getOpStack().pop().toDict();
        interpParams.setSystemParams(dict);
    }
    
    /**
     * PostScript op: settransfer.
     * 
     * @throws PSError A PostScript error occurred.
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    public void op_settransfer() throws PSError, ProgramError {
        PSObjectArray proc = getOpStack().pop().toProc();
        gstate.current().setTransfer(proc);
    }
    
    /**
     * PostScript op: setundercolorremoval.
     * 
     * @throws PSError A PostScript error occurred.
     */
    public void op_setundercolorremoval() throws PSError {
        PSObjectArray proc = getOpStack().pop().toProc();
        gstate.current().setUndercolorRemoval(proc);
    }
    
    /**
     * PostScript op: setuserparams.
     * 
     * @throws PSError A PostScript error occurred.
     */
    public void op_setuserparams() throws PSError {
        PSObjectDict dict = getOpStack().pop().toDict();
        interpParams.setUserParams(dict);
    }
   
    /**
     * PostScript op: shfill.
     * 
     * @throws PSError A PostScript error occurred.
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    public void op_shfill() throws PSError, IOException, ProgramError {
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
     * PostScript op: status.
     * 
     * @throws PSError A PostScript error occurred.
     */
    public void op_status() throws PSError {
        PSObject obj = getOpStack().pop();
        if (obj instanceof PSObjectFile) {
            boolean status = ((PSObjectFile) obj).status();
            getOpStack().push(new PSObjectBool(status));
        } else {
            throw new PSErrorUnregistered("'status' operator of non-file"
                    + " object.");
        }
    }
   
    /**
     * PostScript op: stop.
     * 
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    public void op_stop() throws ProgramError {
        ExecStack es = getExecStack();
        PSObject obj;
        DictStack ds = getDictStack();
        while ((obj = es.pop()) != null) {
            if (obj == ds.eps2pgfStopped) {
                // Set 'newerror' in $error to indicate that an "error"
                // occurred.
                try {
                    PSObjectDict dollarError = ds.lookup("$error").toDict();
                    dollarError.setKey("newerror", true);
                } catch (PSErrorTypeCheck e) {
                    throw new ProgramError("$error is not a dictionary.");
                }

                // Push eps2pgfStopped operator back on the execution stack
                // so that it gets executed.
                es.push(obj);
                break;
            } else if (ds.isContinuationFunction(obj)) {
                // Pop continuation stack down to a Null object. These are the
                // arguments for the continuation function just popped from the
                // execution stack.
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
    
    /**
     * PostScript op: stopped.
     * 
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     * @throws PSError A PostScript error occurred.
     */
    public void op_stopped() throws PSError, ProgramError {
        
        // Push proc and continuation function on stack
        getExecStack().push(getDictStack().eps2pgfStopped);
        getExecStack().push(getOpStack().pop());

        // Push arguments of continutation function on c stack
        getContStack().push(new PSObjectNull());
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
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    public void op_stroke() throws PSError, IOException, ProgramError {
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
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    public void op_token() throws PSError, ProgramError {
        PSObject obj = getOpStack().pop();
        obj.checkAccess(false, true, false);
        
        if (!(obj instanceof PSObjectString)
                && !(obj instanceof PSObjectFile)) {
            throw new PSErrorTypeCheck();
        }
        for (PSObject item : obj.token(this)) {
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
     * PostScript op: undefineresource.
     * 
     * @throws PSError A PostScript error occurred.
     */
    public void op_undefineresource() throws PSError {
        PSObjectName category = getOpStack().pop().toName();
        PSObject key = getOpStack().pop();
        resourceManager.undefineResource(category, key);
    }
    
    /**
     * PostScript op: usertime.
     */
    public void op_usertime() {
        long currentTime = System.currentTimeMillis();
        int userTime = (int) (currentTime - initializationTime);
        getOpStack().push(new PSObjectInt(userTime));
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
     * PostScript op: widthshow.
     * 
     * @throws PSErrorUnregistered Encountered a PostScript feature that is not
     * (yet) implemented.
     */
    public void op_widthshow() throws PSErrorUnregistered {
        throw new PSErrorUnregistered("widthshow operator");
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
    
    /**
     * PostScript op: xshow.
     * 
     * @throws PSError A PostScript error occurred.
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    public void op_xshow() throws PSError, IOException, ProgramError {
        log.info("xshow operator encoutered. xshow is not implemented, "
                + "instead the normal show is used.");
        getOpStack().pop(); // read the numarray/string object
        op_show();
    }
    
    /**
     * PostScript op: xyshow.
     * 
     * @throws PSError A PostScript error occurred.
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    public void op_xyshow() throws PSError, IOException, ProgramError {
        log.info("xyshow operator encoutered. xyshow is not implemented, "
                + "instead the normal show is used.");
        getOpStack().pop(); // read the numarray/string object
        op_show();
    }
    
    /**
     * PostScript op: yshow.
     * 
     * @throws PSError A PostScript error occurred.
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    public void op_yshow() throws PSError, IOException, ProgramError {
        log.info("yshow operator encoutered. yshow is not implemented, "
                + "instead the normal show is used.");
        getOpStack().pop(); // read the numarray/string object
        op_show();
    }
    
    /**
     * Code common to all rect* operators. It starts with 'newpath' and ends
     * with 'closepath'.
     * 
     * @throws PSError A PostScript error occurred.
     */
    private void rectPath() throws PSError {
        op_newpath();
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
     */
    private void rectPathSingle(final double x, final double y,
            final double width, final double height) throws PSError {
        
        gstate.current().moveto(x, y);
        gstate.current().rlineto(width, 0.0);
        gstate.current().rlineto(0.0, height);
        gstate.current().rlineto(-width, 0.0);
        op_closepath();
    }

    /**
     * Get the graphics state stack.
     * 
     * @return The graphics state stack.
     */
    public GstateStack getGstate() {
        return gstate;
    }
    
    /**
     * Gets the interpreter parameters.
     * 
     * @return the interpreter parameters.
     */
    public InterpParams getInterpParams() {
        return interpParams;
    }
    
    /**
     * Sets the interpreter parameters.
     * 
     * @param newInterpParams The new interpreter parameters.
     */
    public void setInterpParams(final InterpParams newInterpParams) {
        interpParams = newInterpParams;
    }
    
    
    /**
     * Get the resource manager.
     * 
     * @return the resource manager.
     */
    public ResourceManager getResourceManager() {
        return resourceManager;
    }

    /**
     * Gets the current value of the interpreter counter.
     * 
     * @return The current value of the interpreter counter.
     */
    public int getInterpCounter() {
        return interpCounter;
    }
}
