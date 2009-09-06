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

import java.io.Writer;

import net.sf.eps2pgf.Options;
import net.sf.eps2pgf.ProgramError;
import net.sf.eps2pgf.io.TextHandler;
import net.sf.eps2pgf.io.TextReplacements;
import net.sf.eps2pgf.ps.errors.PSError;
import net.sf.eps2pgf.ps.errors.PSErrorTypeCheck;
import net.sf.eps2pgf.ps.errors.PSErrorUndefined;
import net.sf.eps2pgf.ps.objects.PSObject;
import net.sf.eps2pgf.ps.objects.PSObjectArray;
import net.sf.eps2pgf.ps.objects.PSObjectDict;
import net.sf.eps2pgf.ps.objects.PSObjectFile;
import net.sf.eps2pgf.ps.objects.PSObjectName;
import net.sf.eps2pgf.ps.objects.PSObjectNull;
import net.sf.eps2pgf.ps.objects.PSObjectOperator;
import net.sf.eps2pgf.ps.objects.PSObjectString;
import net.sf.eps2pgf.ps.resources.ResourceManager;
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
    /** Virtual Memory (VM) manager. */
    private final VM vm = new VM();
    
    /** Resource manager. */
    private final ResourceManager resourceManager = new ResourceManager(this);
    
    /** Operand stack (see PostScript manual for more info). */
    private ArrayStack<PSObject> opStack = new ArrayStack<PSObject>();
    
    /** Dictionary stack. */
    private final DictStack dictStack = new DictStack(this);
    
    /**
     * Continuation stack. Arguments for continuation operators are stored on
     * this stack. These special operators are for example used in loops: such
     * as 'repeat', 'for' and 'forall'. Each time a continuation function is
     * pushed onto the execution stack its accompanying arguments are pushed
     * onto this stack preceded by a null object. I.e. first a null object is
     * pushed on the continuation stack, followed by the arguments. 
     */
    private final ArrayStack<PSObject> contStack = new ArrayStack<PSObject>();
    
    /** Execution stack. */
    private final ExecStack execStack = new ExecStack(vm);
    
    /** Operators starting with A to C. */
    private final OperatorsAtoC opsAC = new OperatorsAtoC(this);
    
    /** Operators starting with D to F. */
    private final OperatorsDtoF opsDF = new OperatorsDtoF(this);
    
    /** Operators starting with G to I. */
    private final OperatorsGtoI opsGI = new OperatorsGtoI(this);
    
    /** Operators starting with J to L. */
    private final OperatorsJtoL opsJL = new OperatorsJtoL(this);
    
    /** Operators starting with M to O. */
    private final OperatorsMtoO opsMO = new OperatorsMtoO(this);
    
    /** Operators starting with P to R. */
    private final OperatorsPtoR opsPR = new OperatorsPtoR(this);
    
    /** Operators starting with S. */
    private final OperatorsS opsS = new OperatorsS(this);
    
    /** Operators starting with T to Z. */
    private final OperatorsTtoZ opsTZ = new OperatorsTtoZ(this);
    
    /** Operators specific to Eps2pgf. */
    private final OperatorsEps2pgf opsEps2pgf = new OperatorsEps2pgf(this);
    
    /** Operators with special characters in the name. */
    private final OperatorsSpecialChar opsSpecialChar =
        new OperatorsSpecialChar(this);
    
    /** Graphics state. */
    private final GstateStack gstate;
    
    /** Text handler, handles text in the postscript code. */
    private TextHandler textHandler;
    
    /** Header information of the file being interpreted. */
    private DSCHeader header;
    
    /** Default clipping path. */
    private Path defaultClippingPath;
    
    /** User-defined options. */
    private Options options;
    
    /** Interpreter parameters. */
    private final InterpParams interpParams = new InterpParams(this);
    
    /** Initialization time of interpreter. (milliseconds since 1970). */
    private final long initializationTime = System.currentTimeMillis();
    
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
     */
    public Interpreter(final Writer outputWriter, final Options opts,
            final DSCHeader fileHeader,
            final TextReplacements textReplace)
            throws ProgramError, PSError {
        
        options = opts;        

        // Create graphics state stack with output device
        OutputDevice output;
        switch (opts.getOutputType()) {
            case PGF:
                output = new PGFDevice(outputWriter, this);
                break;
            case LOL:
                output = new LOLDevice(outputWriter);
                break;
            default:
                throw new ProgramError("Unknown output device ("
                        + opts.getOutputType() + ").");
        }
        
        gstate = new GstateStack(output, this);
        textHandler = new TextHandler(gstate, textReplace, opts.getTextmode());
        header = fileHeader;
        
        // Initialization procedure that is the same for all constructors
        initialize();
    }
    
    /**
     * Creates a new instance of Interpreter with nulldevice as output and
     * (virtually) infinite bounding box.
     * 
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     * @throws PSError A PostScript error occurred.
     */
    public Interpreter() throws ProgramError, PSError {
        options = new Options();

        // Create graphics state stack with output device
        OutputDevice output = new NullDevice();
        gstate = new GstateStack(output, this);
        
        // Text handler
        textHandler = new TextHandler(gstate);
        
        // "Infinite" bounding box (square box from (-10m,-10m) to (10m,10m))
        double[] bbox = {-28346.46, -28346.46, 28346.46, 28346.46};
        header = new DSCHeader(bbox);
     
        // Initialization procedure that is the same for all constructors
        initialize();
    }
    
    /**
     * Do some initialization tasks.
     * 
     * @throws PSError A PostScript error occurred.
     * @throws ProgramError the program error
     */
    void initialize() throws PSError, ProgramError {

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
        executeOperator("newpath");
        if (bbox != null) {
            executeOperator("initclip");
        } else {
            gstate.current().setClippingPath(getDefaultClippingPath().clone());
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
     * Gets the class implementation operators starting with A to C.
     * 
     * @return The operator class.
     */
    public OperatorsAtoC getOpsAC() {
        return opsAC;
    }
    
    /**
     * Gets the class implementation operators starting with D to F.
     * 
     * @return The operator class.
     */
    public OperatorsDtoF getOpsDF() {
        return opsDF;
    }
    
    /**
     * Gets the class implementation internal eps2pgf operators.
     * 
     * @return The operator class.
     */
    public OperatorsEps2pgf getOpsEps2pgf() {
        return opsEps2pgf;
    }
    
    /**
     * Gets the class implementation operators starting with G to I.
     * 
     * @return The operator class.
     */
    public OperatorsGtoI getOpsGI() {
        return opsGI;
    }
    
    /**
     * Gets the class implementation operators starting with J to L.
     * 
     * @return The operator class.
     */
    public OperatorsJtoL getOpsJL() {
        return opsJL;
    }
    
    /**
     * Gets the class implementation operators starting with M to O.
     * 
     * @return The operator class.
     */
    public OperatorsMtoO getOpsMO() {
        return opsMO;
    }
    
    /**
     * Gets the class implementation operators starting with P to R.
     * 
     * @return The operator class.
     */
    public OperatorsPtoR getOpsPR() {
        return opsPR;
    }
    
    /**
     * Gets the class implementation operators starting with S.
     * 
     * @return The operator class.
     */
    public OperatorsS getOpsS() {
        return opsS;
    }
    
    /**
     * Gets the class implementation operators with special characters.
     * 
     * @return The operator class.
     */
    public OperatorsSpecialChar getOpsSpecialChar() {
        return opsSpecialChar;
    }
    
    /**
     * Gets the class implementation operators starting with T to Z.
     * 
     * @return The operator class.
     */
    public OperatorsTtoZ getOpsTZ() {
        return opsTZ;
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
     */
    public void start() throws ProgramError, PSError {
        try {
            // Make sure that the allocation mode start in local
            getVm().setGlobal(false);
            
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
                    + " should not occur. ");
        } finally {
            this.gstate.current().getDevice().finish();
        }
    }
    
    /**
     * Execute objects on the execution stack one by one until the execution
     * stack is empty.
     * 
     * @throws PSError A PostScript error occurred.
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    public void run() throws PSError, ProgramError {
        run(null);
    }
    
    /**
     * Execute objects on the execution stack one by one until the specified
     * item is at the top of the execution stack.
     * 
     * @param stopAt Stop and return from this function when this item is at the
     * top of the execution stack. If this is <code>null</code> the entire
     * execution stack is executed.
     * 
     * @throws PSError A PostScript error occurred.
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    public void run(final PSObject stopAt) throws PSError, ProgramError {
        ExecStack es = getExecStack();
        while (es.getTop() != stopAt) {
            PSObject obj = es.getNextToken(this, stopAt);
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
        getExecStack().push(objectToRun);
        run(topAtStart);
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
                ((PSObjectOperator) obj).invoke();
            } else if (obj instanceof PSObjectNull) {
                // don't do anything with an executable null
            } 
        }  // end of check whether object is literal
    }
    
    /**
     * Gets an operator from the system dictionary (WARNING: it always uses the
     * system dictionary, even if an operator has been redefined).
     * 
     * @param opName The operator name.
     * 
     * @throws PSError A PostScript error occurred.
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    public void executeOperator(final String opName) throws PSError,
            ProgramError {
        
        PSObject obj = dictStack.getSystemDict().lookup(opName);
        if (obj == null) {
            throw new ProgramError("Unable to execute operator (" + opName
                    + ") because it is not defined in the system dictionary.");
        }
        PSObjectOperator op;
        try {
            op = obj.toOperator();
        } catch (PSErrorTypeCheck e) {
            throw new ProgramError("Unable to execute operator (" + opName
                    + ") because it is not a PSObjectOperator.");
        }
        
        op.invoke();
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
     * Get the resource manager.
     * 
     * @return the resource manager.
     */
    public ResourceManager getResourceManager() {
        return resourceManager;
    }
    
    /**
     * Gets the VM manager.
     * 
     * @return The VM manager.
     */
    public VM getVm() {
        return vm;
    }

    /**
     * Gets the current value of the interpreter counter.
     * 
     * @return The current value of the interpreter counter.
     */
    public int getInterpCounter() {
        return interpCounter;
    }

    /**
     * Gets the text handler.
     * 
     * @return The text handler.
     */
    public TextHandler getTextHandler() {
        return textHandler;
    }

    /**
     * @return Gets the defaultClippingPath
     */
    public Path getDefaultClippingPath() {
        return defaultClippingPath;
    }

    /**
     * @return the initializationTime
     */
    public long getInitTime() {
        return initializationTime;
    }
}
