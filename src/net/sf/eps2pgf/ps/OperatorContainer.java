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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import net.sf.eps2pgf.ProgramError;
import net.sf.eps2pgf.io.TextHandler;
import net.sf.eps2pgf.ps.errors.PSError;
import net.sf.eps2pgf.ps.errors.PSErrorStackUnderflow;
import net.sf.eps2pgf.ps.objects.PSObject;
import net.sf.eps2pgf.ps.objects.PSObjectDict;
import net.sf.eps2pgf.ps.objects.PSObjectOperator;
import net.sf.eps2pgf.ps.resources.ResourceManager;
import net.sf.eps2pgf.util.ArrayStack;


/**
 * Base class for classing implementing PostScript operators.
 */
public class OperatorContainer {
    
    /** The interpreter. */
    private Interpreter interp;
    
    /**
     * Instantiates a new OperatorsAtoC.
     * 
     * @param interpreter The interpreter.
     * 
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    protected OperatorContainer(final Interpreter interpreter)
            throws ProgramError {
        
        interp = interpreter;
        
        PSObjectDict systemdict;
        try {
            systemdict = getDictStack().lookup("systemdict").toDict();
        } catch (PSError e) {
            throw new ProgramError("Unable to find the system dictionary.");
        }
        
        @SuppressWarnings("unchecked")
        Class<PSObjectOperator>[] cls =
            (Class<PSObjectOperator>[]) getClass().getClasses();
        for (int i = 0; i < cls.length; i++) {
            try {
                // Create a new instance of the PostScript operator object.
                Constructor<PSObjectOperator> cons =
                    cls[i].getConstructor(getClass());
                PSObjectOperator op = cons.newInstance(this);
                String name = op.getName();
                
                if (systemdict.known(name)) {
                    throw new ProgramError("Trying to add " + name
                            + " to systemdict, but it is already defined in"
                            + " systemdict.");
                }
                systemdict.setKey(name, op);
                
            } catch (InstantiationException e) {
                throw new ProgramError("Error initiating/registering operator"
                        + " instance: " + e.getMessage());
            } catch (IllegalAccessException e) {
                throw new ProgramError("Error initiating/registering operator"
                        + " instance: " + e.getMessage());
            } catch (NoSuchMethodException e) {
                throw new ProgramError("Error initiating/registering operator"
                        + " instance: " + e.getMessage());
            } catch (InvocationTargetException e) {
                throw new ProgramError("Error initiating/registering operator"
                        + " instance: " + e.getMessage());
            } catch (ClassCastException e) {
                throw new ProgramError("Operator container class ("
                        + getClass().getSimpleName()
                        + ") contains inner class ("
                        + cls[i].getSimpleName()
                        + ") that is not a PSObjectOperator.");
            }
            
        }
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
    public void executeOperator(final String opName)
            throws PSError, ProgramError {
        
        interp.executeOperator(opName);
    }
    
    /**
     * Push an object on the operand stack.
     * 
     * @param obj The object to push.
     */
    protected final void osPush(final PSObject obj) {
        interp.getOpStack().push(obj);
    }
    
    /**
     * Pop an object from the operand stack.
     * 
     * @return The object popped from the stack.
     * 
     * @throws PSErrorStackUnderflow Tried to pop an object from an empty stack.
     */
    protected final PSObject osPop() throws PSErrorStackUnderflow {
        return interp.getOpStack().pop();
    }
    
    /**
     * Gets the continuation stack of the interpreter.
     * 
     * @return The continuation stack.
     */
    protected final ArrayStack<PSObject> getContStack() {
        return interp.getContStack();
    }
    
    /**
     * Gets the dictionary stack of the interpreter.
     * 
     * @return The dictionary stack.
     */
    protected final DictStack getDictStack() {
        return interp.getDictStack();
    }
    
    /** 
     * Gets the execution stack of the interpreter.
     * 
     * @return The execution stack.
     */
    protected final ExecStack getExecStack() {
        return interp.getExecStack();
    }
    
    /**
     * Get the graphics state stack of the interpreter.
     * 
     * @return The graphics state stack.
     */
    protected final GstateStack getGstate() {
        return interp.getGstate();
    }
    
    /**
     * Gets the interpreter.
     * 
     * @return The interpreter.
     */
    protected final Interpreter getInterp() {
        return interp;
    }
    
    /**
     * Gets the interpreter parameters.
     * 
     * @return The interpreter parameters.
     */
    protected final InterpParams getInterpParams() {
        return interp.getInterpParams();
    }
    
    
    /**
     * Gets the operand stack of the interpreter.
     * 
     * @return The operand stack.
     */
    protected final ArrayStack<PSObject> getOpStack() {
        return interp.getOpStack();
    }
    
    /**
     * Get the resource manager.
     * 
     * @return the resource manager.
     */
    public ResourceManager getResourceManager() {
        return interp.getResourceManager();
    }
    
    /**
     * Gets the text handler.
     * 
     * @return The text handler.
     */
    public TextHandler getTextHandler() {
        return interp.getTextHandler();
    }

    /**
     * Gets the virtual memory of the interpreter.
     * 
     * @return The VM.
     */
    protected final VM getVm() {
        return interp.getVm();
    }
    
    /**
     * Gets the current graphics state.
     * 
     * @return The current graphics state.
     */
    protected final GraphicsState gsCurrent() {
        return interp.getGstate().current(); 
    } 
}
