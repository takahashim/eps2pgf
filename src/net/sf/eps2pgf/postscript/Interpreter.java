/*
 * Interpreter.java
 *
 * This file is part of Eps2pgf.
 *
 * Copyright (C) 2007 Paul Wagenaars <pwagenaars@fastmail.fm>
 *
 * Eps2pgf is free software; you can redistribute it and/or modify it under
 * the terms of version 2 of the GNU General Public License as published by the
 * Free Software Foundation.
 *
 * Eps2pgf is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */

package net.sf.eps2pgf.postscript;

import java.io.*;
import java.util.*;
import java.util.logging.*;

import org.fontbox.afm.*;
import org.fontbox.util.BoundingBox;

import net.sf.eps2pgf.*;
import net.sf.eps2pgf.collections.ArrayStack;
import net.sf.eps2pgf.output.*;
import net.sf.eps2pgf.postscript.errors.*;

/**
 * Interprets a PostScript document and produces output
 * @author Paul Wagenaars
 */
public class Interpreter {
    // Operand stack (see PostScript manual for more info)
    ArrayStack<PSObject> opStack = new ArrayStack<PSObject>();
    
    // Dictionary stack
    DictStack dictStack = new DictStack(this);
    
    // Base of execution stack (this is basically the document)
    List<PSObject> docObjects;
    
    // Graphics state
    GstateStack gstate = new GstateStack();
    
    // Fonts resources
    Fonts fonts;
    
    // Exporter, writes the graphics data to file in another format (such as pgf)
    Exporter exp;
    
    // Text handler, handles text in the postscript code
    TextHandler textHandler = new TextHandler(gstate);
    
    Logger log = Logger.getLogger("global");
    
    /**
     * Creates a new instance of Interpreter
     * @param in PostScript document
     * @param out Destination for output code
     * @throws java.io.FileNotFoundException Unable to find font resources
     */
    public Interpreter(List<PSObject> in, Writer out) throws FileNotFoundException {
        docObjects = in;
        
        // Initialize character encodings
        Encoding.initialize();
        fonts = new Fonts();
        
        // Create new exporter
        exp = new PGFExport(out);
    }
    
    /**
     * Start interpreting PostScript document
     * @throws java.lang.Exception Something went wrong in the interpretation process
     */
    public void start() throws Exception {
        exp.init();
        try {
            processObjects(docObjects);
        } catch (PSError e) {
            System.out.println("----- Start of stack");
            op_pstack();
            System.out.println("----- End of stack");
            dictStack.dumpFull();
            exp.finish();
            throw e;
        }
        exp.finish();
    }
    
    /**
     * Process/interpret a list of PostScript objects
     * @param objList List with PostScript objects to process
     * @throws java.lang.Exception Something went wrong while processing the object
     */
    public void processObjects(List<PSObject> objList) throws Exception {
        for (int i = 0 ; i < objList.size() ; i++) {
            objList.get(i).process(this);
        }        
    }
    
    /**
     * PostScript op: add
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorStackUnderflow Not enough object on stack
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorTypeCheck One or both objects on stack are not numeric
     */
    public void op_add() throws PSErrorStackUnderflow, PSErrorTypeCheck {
        PSObject num2 = opStack.pop();
        PSObject num1 = opStack.pop();
        if ( (num1 instanceof PSObjectInt) && (num2 instanceof PSObjectInt) ) {
            opStack.push(new PSObjectInt( num1.toInt() + num2.toInt() ));
        } else {
            opStack.push(new PSObjectReal( num1.toReal() + num2.toReal() ));
        }
    }
    
    /**
     * PostScript op: arc
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorUnimplemented This operator is not (yet) implemented
     */
    public void op_arc() throws PSErrorUnimplemented {
        throw new PSErrorUnimplemented("operator: arc");
    }
    
    /**
     * PostScript op: arcn
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorUnimplemented This operator is not (yet) implemented
     */
    public void op_arcn() throws PSErrorUnimplemented {
        throw new PSErrorUnimplemented("operator: arcn");
    }
    
    /**
     * PostScript op: array
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorStackUnderflow Not enough objects on stack
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorTypeCheck Topmost object is not a number
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorRangeCheck Topmost object is negative
     */
    public void op_array() throws PSErrorStackUnderflow, PSErrorTypeCheck, PSErrorRangeCheck {
        int n = opStack.pop().toNonNegInt();
        op_sqBrackLeft();
        PSObjectNull nullObj = new PSObjectNull();
        for (int i = 0 ; i < n ; i++) {
            opStack.push(nullObj);
        }
        try {
            op_sqBrackRight();
        } catch (PSErrorUnmatchedMark e) {
            // Since the op_sqBrackLeft call is a few lines up this error can never happen
        }
    }
    
    /**
     * PostScript op: ashow
     */
    public void op_ashow() throws PSErrorTypeCheck, PSErrorStackUnderflow,
            PSErrorRangeCheck, PSErrorUnimplemented, PSErrorUndefined, 
            PSErrorNoCurrentPoint, IOException {
        log.fine("ashow operator encoutered. ashow is not implemented, instead the normal show is used.");
        PSObjectString string = opStack.pop().toPSString();
        double ay = opStack.pop().toReal();
        double ax = opStack.pop().toReal();
        opStack.push(string);
        op_show();
    }
    
    
    /**
     * PostScript op: astore
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorStackUnderflow Operand stack underflow
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorTypeCheck Operand of wrong type
     */
    public void op_astore() throws PSErrorStackUnderflow, PSErrorTypeCheck {
        PSObjectArray array = opStack.pop().toArray();
        int n = array.size();
        try {
            for (int i = (n-1) ; i >= 0 ; i--) {
                array.set(i, opStack.pop());
            }
        } catch (PSErrorRangeCheck e) {
            // due to the for-loop this can never happen
        }
        opStack.push(array);
    }
    
    /**
     * PostScript op: begin
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorStackUnderflow Operand stack underflow
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorTypeCheck Operand is not a dictionary
     */
    public void op_begin() throws PSErrorStackUnderflow, PSErrorTypeCheck {
        PSObjectDict dict = opStack.pop().toDict();
        dictStack.pushDict(dict);
    }
    
    /**
     * PostScript op: bind
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorStackUnderflow Operand stack underflow
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorTypeCheck Operand is not object type that can be binded
     */
    public void op_bind() throws PSErrorStackUnderflow, PSErrorTypeCheck {
        PSObject obj = opStack.peek();
        if (obj instanceof PSObjectProc) {
            PSObjectProc proc = (PSObjectProc)obj;
            proc.bind(this);            
        } else if (obj instanceof PSObjectArray) {
            PSObjectArray array = (PSObjectArray)obj;
            array.bind(this);
        } else {
            throw new PSErrorTypeCheck();
        }
    }
    
    /** PostScript op: clear */
    public void op_clear() {
        opStack.clear();
    }
    
    /**
     * PostScript op: cleartomark
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorUnmatchedMark Unable to find mark object on stack
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorStackUnderflow Operand stack underflow
     */
    public void op_cleartomark() throws PSErrorUnmatchedMark, PSErrorStackUnderflow {
        int n = opStack.size();
        for (int i = 0 ; i < n ; i++) {
            if (opStack.pop() instanceof PSObjectMark) {
                return;
            }
        }
        throw new PSErrorUnmatchedMark();
    }
    
    /**
     * PostScript op: clip
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorUnimplemented Operator not fully implemented
     * @throws java.io.IOException Unable to write output
     */
    public void op_clip() throws PSErrorUnimplemented, IOException {
        gstate.current.clip();
        exp.clip(gstate.current.clippingPath);
    }
    
    /** PostScript op: closepath */
    public void op_closepath() {
        double[] startPos = gstate.current.path.closepath();
        if (startPos != null) {
            gstate.current.moveto(startPos[0], startPos[1]);
        }
    }
    
    /** PostScript op: concat */
    public void op_concat() throws PSError {
        PSObjectMatrix matrix = opStack.pop().toMatrix();
        gstate.current.CTM.concat(matrix);
    }
    
    /** PostScript op: copy */
    public void op_copy() throws PSError {
        PSObject obj = opStack.pop();
        if ( (obj instanceof PSObjectInt) || (obj instanceof PSObjectReal) ) {
            // Get n, the number of copies to make
            int n = obj.toNonNegInt();
            int stackSize = opStack.size();
        
            for (int i = stackSize-n ; i < stackSize ; i++) {
                opStack.push(opStack.get(i));
            }
        } else if (obj instanceof PSObjectArray) {
            PSObjectArray array2 = (PSObjectArray)obj;
            obj = opStack.pop();
            if (!(obj instanceof PSObjectArray)) {
                throw new PSErrorTypeCheck();
            }
            PSObjectArray array1 = (PSObjectArray)obj;
            array2.copyFrom(array1);
            opStack.push(array2);
        } else {
            throw new PSErrorTypeCheck();
        }
    }
    
    /** PostScript op: count */
    public void op_count() {
        int count = opStack.size();
        PSObjectInt n = new PSObjectInt(count);
        opStack.push(n);
    }
    
    /** PostScript op: counttomark */
    public void op_counttomark() throws PSErrorUnmatchedMark {
        int n = opStack.size();
        for (int i = n-1 ; i >= 0 ; i--) {
            if (opStack.get(i) instanceof PSObjectMark) {
                opStack.push(new PSObjectInt(n-1-i));
                return;
            }
        }
        throw new PSErrorUnmatchedMark();
    }
    
    /** PostScript op: currentdict */
    public void op_currentdict() throws PSError {
        opStack.push(dictStack.peekDict());
    }
    
    /** PostScript op: currentfile */
    public void op_currentfile() throws PSError {
        throw new PSErrorUnimplemented("operator: currentfile");
    }
    
    /** PostScript op: currentmatrix */
    public void op_currentmatrix() throws PSError {
        PSObject obj = opStack.pop();
        PSObjectMatrix matrix = obj.toMatrix();
        matrix.copyValuesFrom(gstate.current.CTM);
        obj.copyValuesFrom(matrix);
        opStack.push(obj);
    }
    
    /**
     * PostScript op: currentpoint
     */
    public void op_currentpoint() throws PSErrorNoCurrentPoint {
        double[] currentDevice = gstate.current.getCurrentPosInDeviceSpace();
        double[] currentUser = gstate.current.CTM.inverseApply(currentDevice);
        opStack.push(new PSObjectReal(currentUser[0]));
        opStack.push(new PSObjectReal(currentUser[1]));
    }
    
    /**
     * PostScript op: curveto
     */
    public void op_curveto() throws PSErrorStackUnderflow, PSErrorTypeCheck {
        double y3 = opStack.pop().toReal();
        double x3 = opStack.pop().toReal();
        double y2 = opStack.pop().toReal();
        double x2 = opStack.pop().toReal();
        double y1 = opStack.pop().toReal();
        double x1 = opStack.pop().toReal();
        gstate.current.curveto(x1,y1, x2,y2, x3,y3);
    }
    
    /** PostScript op: def */
    public void op_def() throws PSError {
        PSObject value = opStack.pop();
        PSObject key = opStack.pop();
        dictStack.def(key, value);
    }
    
    /** PostScript op: definefont */
    public void op_definefont() throws PSError {
        PSObjectFont font = opStack.pop().toFont();
        PSObject key = opStack.pop();
        opStack.push( fonts.defineFont(key, font) );
    }
    
    /** PostScript op: dict */
    public void op_dict() throws PSError {
        int capacity = opStack.pop().toNonNegInt();
        opStack.push(new PSObjectDict(capacity));
    }
    
    /** PostScript op: div */
    public void op_div() throws PSError {
        double num2 = opStack.pop().toReal();
        double num1 = opStack.pop().toReal();
        opStack.push(new PSObjectReal( num1 / num2 ));
    }
    
    /** PostScript op: dup */
    public void op_dup() throws PSError {
        opStack.push(opStack.peek());
    }
    
    /** PostScript op: end */
    public void op_end() throws PSError {
        dictStack.popDict();
    }
    
    /** PostScript op: eofill */
    public void op_eofill() throws PSError, IOException {
        exp.eofill(gstate.current.path);
        op_newpath();
    }
    
    /**
     * PostScript op: errordict
     */
    public void op_errordict() throws PSErrorUnimplemented {
        throw new PSErrorUnimplemented("errordict operator");
    }
    
    /** PostScript op: eq */
    public void op_eq() throws PSErrorTypeCheck, PSErrorStackUnderflow {
        PSObject any2 = opStack.pop();
        PSObject any1 = opStack.pop();
        boolean bool;
        if ( (any1 instanceof PSObjectInt) || (any1 instanceof PSObjectReal) ) {
            bool = (any1.toReal() == any2.toReal());
        } else if ( (any1 instanceof PSObjectString) || (any1 instanceof PSObjectName) ) {
            bool = any1.toDictKey().equals(any2.toDictKey());
        } else {
            bool = (any1 == any2);
        }
        opStack.push(new PSObjectBool(bool));
    }
    
    /** PostScript op: exch */
    public void op_exch() throws PSError {
        PSObject any2 = opStack.pop();
        PSObject any1 = opStack.pop();
        opStack.push(any2);
        opStack.push(any1);
    }
    
    /** PostScript op: false */
    public void op_false() {
        opStack.push(new PSObjectBool(false));
    }
    
    /** PostScript op: fill */
    public void op_fill() throws PSError, IOException {
        exp.fill(gstate.current.path);
        op_newpath();
    }
    
    /** PostScript op: findfont */
    public void op_findfont() throws PSError, ProgramError {
        PSObject key = opStack.pop();
        opStack.push(fonts.findFont(key));
    }
    
    /** PostScript op: forall */
    public void op_forall() throws Exception {
        PSObjectProc proc = opStack.pop().toProc();
        PSObject obj = opStack.pop();
        if ((obj instanceof PSObjectDict) || (obj instanceof PSObjectFont)) {
            PSObjectDict dict = obj.toDict();
            Set<PSObject> keys = dict.keySet();
            Iterator<PSObject> iter = keys.iterator();
            while (iter.hasNext()) {
                PSObject key = iter.next();
                PSObject value = dict.lookup(key);
                opStack.push(key);
                opStack.push(value);
                proc.execute(this);
            }
        } else {
            throw new PSErrorUnimplemented("operator: forall not FULLY implemented");
        }
    }
    
    /** PostScript op: get */
    public void op_get() throws PSError {
        PSObject indexKey = opStack.pop();
        PSObject obj = opStack.pop();
        if ((obj instanceof PSObjectDict) || (obj instanceof PSObjectFont)) {
            PSObjectDict dict = obj.toDict();
            opStack.push(dict.lookup(indexKey.toDictKey()));
        } else {
            throw new PSErrorUnimplemented("operator: get not FULLY implemented.");
        }
    }
    
    /** PostScript op: getinterval */
    public void op_getinterval() throws PSError {
        int count = opStack.pop().toNonNegInt();
        int index = opStack.pop().toNonNegInt();
        PSObject obj = opStack.pop();
        opStack.push(obj.getinterval(index, count));
    }
    
    /** PostScript op: grestore */
    public void op_grestore() throws PSError, IOException {
        gstate.restoreGstate();
        exp.endScope();
    }
    
    /** PostScript op: gsave */
    public void op_gsave() throws PSError, IOException {
        gstate.saveGstate();
        exp.startScope();
    }
    
    /** PostScript op: if */
    public void op_if() throws Exception {
        PSObjectProc proc = opStack.pop().toProc();
        boolean bool = opStack.pop().toBool();
        if (bool) {
            proc.execute(this);
        }
    }
    
    /** PostScript op: ifelse */
    public void op_ifelse() throws Exception {
        PSObjectProc proc2 = opStack.pop().toProc();
        PSObjectProc proc1 = opStack.pop().toProc();
        boolean bool = opStack.pop().toBool();
        
        if (bool) {
            proc1.execute(this);
        } else {
            proc2.execute(this);
        }
    }
    
    /** PostScript op: image */
    public void op_image() throws PSError {
        throw new PSErrorUnimplemented("operator: image");
    }
    
    /** PostScript op: index */
    public void op_index() throws PSError {
        // Get n, the index of the element to retrieve
        int n = opStack.pop().toNonNegInt();
        
        opStack.push(opStack.peek(n));
    }
    
    /** PostScript "op": ISOLatin1Encoding */
    public void op_ISOLatin1Encoding() {
        PSObjectName[] encodingVector = Encoding.getISOLatin1Vector();
        opStack.push(new PSObjectArray(encodingVector));
    }
    
    /**
     * PostScript op: itransform
     */
    public void op_itransform() throws PSErrorStackUnderflow, PSErrorTypeCheck, 
            PSErrorRangeCheck {
        PSObject obj = opStack.pop();
        PSObjectMatrix matrix = null;
        try {
            matrix = obj.toMatrix();
        } catch (PSErrorTypeCheck e) {
            
        }
        double y;
        if (matrix == null) {
            matrix = gstate.current.CTM;
            y = obj.toReal();
        } else {
            y = opStack.pop().toReal();
        }
        double x = opStack.pop().toReal();
        
        double[] itransformed = matrix.inverseApply(x, y);
        opStack.push(new PSObjectReal(itransformed[0]));
        opStack.push(new PSObjectReal(itransformed[1]));
    }
    
    /** PostScript op: known */
    public void op_known() throws PSError {
        PSObject key = opStack.pop();
        PSObjectDict dict = opStack.pop().toDict();
        boolean containsKey = dict.containsKey(key);
        opStack.push(new PSObjectBool(containsKey));
    }
    
    /** PostScript op: load */
    public void op_load() throws PSError {
        String key = opStack.pop().toDictKey();
        PSObject value = dictStack.lookup(key);
        if (value == null) {
            opStack.push(new PSObjectName("/"+key));
            throw new PSErrorUndefined();
        }
        opStack.push(value);
    }
    
    /** PostScript op: length */
    public void op_length() throws PSError {
        PSObject obj = opStack.pop();
        if ((obj instanceof PSObjectDict) || (obj instanceof PSObjectFont)) {
            PSObjectDict dict = obj.toDict();
            opStack.push(new PSObjectInt(dict.length()));
        } else {
            throw new PSErrorUnimplemented("operator: length not FULLY implemented");
        }
    }
    
    /** PostScript op: lineto */
    public void op_lineto() throws PSError {
        double y = opStack.pop().toReal();
        double x = opStack.pop().toReal();
        gstate.current.lineto(x, y);
    }
    
    /** PostScript op: lt */
    public void op_lt() throws PSError {
        double num2 = opStack.pop().toReal();
        double num1 = opStack.pop().toReal();
        if (num1 < num2) {
            opStack.push(new PSObjectBool(true));
        } else {
            opStack.push(new PSObjectBool(false));
        }
    }
    
    /** PostScript op: makefont */
    public void op_makefont() throws PSErrorStackUnderflow, PSErrorTypeCheck, 
            PSErrorRangeCheck, CloneNotSupportedException {
        PSObjectMatrix matrix = opStack.pop().toMatrix();
        PSObjectDict font = opStack.pop().toDict();
        font = font.clone();
        PSObjectMatrix fontMatrix = font.lookup("FontMatrix").toMatrix();
        
        // Concatenate matrix to fontMatrix and store it back in font
        fontMatrix.concat(matrix);
        font.setKey("FontMatrix", fontMatrix);
        
        // Calculate the fontsize in LaTeX points
        PSObjectMatrix ctm = gstate.current.CTM.clone();
        ctm.concat(fontMatrix);
        double fontSize = ctm.getMeanScaling() / 2.54 * 72.27;
        font.setKey("FontSize", new PSObjectReal(fontSize));
        
        opStack.push(font);
    }
    
    /** Postscript op: mark */
    public void op_mark() {
        opStack.push(new PSObjectMark());
    }
    
    /** Postscript op: matrix */
    public void op_matrix() throws PSError {
        double[] identityMatrix = {1, 0, 0, 1, 0, 0};
        op_sqBrackLeft();
        for(int i = 0 ; i < identityMatrix.length ; i++) {
            opStack.push(new PSObjectReal(identityMatrix[i]));
        }
        op_sqBrackRight();
    }
    
    /** PostScript op: moveto */
    public void op_moveto() throws PSError {
        double y = opStack.pop().toReal();
        double x = opStack.pop().toReal();
        gstate.current.moveto(x, y);
    }
    
    /** PostScript op: mul */
    public void op_mul() throws PSError {
        PSObject num2 = opStack.pop();
        PSObject num1 = opStack.pop();
        if ( (num1 instanceof PSObjectInt) && (num2 instanceof PSObjectInt) ) {
            opStack.push(new PSObjectInt( num1.toInt() * num2.toInt() ));
        } else {
            opStack.push(new PSObjectReal( num1.toReal() * num2.toReal() ));
        }
    }
    
    /** PostScript op: ne */
    public void op_ne() throws PSError {
        op_eq();
        op_not();
    }

    /** PostScript op: neg */
    public void op_neg() throws PSError {
        PSObject obj = opStack.pop();
        if (obj instanceof PSObjectInt) {
            PSObjectInt intObj = (PSObjectInt)obj;
            intObj.value = -intObj.value;
            opStack.push(intObj);
        } else if (obj instanceof PSObjectReal) {
            PSObjectReal realObj = (PSObjectReal)obj;
            realObj.value = -realObj.value;
            opStack.push(realObj);
        } else {
            throw new PSErrorTypeCheck();
        }
    }

    /** PostScript op: newpath */
    public void op_newpath() throws PSError {
        gstate.current.path = new Path();
        gstate.current.position[0] = Double.NaN;
        gstate.current.position[1] = Double.NaN;
    }
    
    /** PostScript op: not */
    public void op_not() throws PSError {
        PSObject obj = opStack.pop();
        if (obj instanceof PSObjectBool) {
            PSObjectBool bool = (PSObjectBool)obj;
            opStack.push(new PSObjectBool( !bool.toBool() ));
        } else {
            throw new PSErrorUnimplemented("not operator not FULLY implemented.");
        }
    }
    
    /** PostScript op: null */
    public void op_null() throws PSError {
        opStack.push(new PSObjectNull());
    }

    /** PostScript op: picstr */
    public void op_picstr() throws PSError {
        throw new PSErrorUnimplemented("operator: picstr");
    }
    
    /** PostScript op: pop */
    public void op_pop() throws PSError {
        opStack.pop();
    }
    
    /** 
     * PostScript op: pstack
     */
    public void op_pstack() {
        for (int i = opStack.size()-1 ; i >= 0 ; i--) {
            System.out.println(opStack.get(i).isis());
        }        
    }
    
    /** PostScript op: put */
    public void op_put() throws PSError {
        PSObject any = opStack.pop();
        PSObject indexKey = opStack.pop();
        PSObject obj = opStack.pop();
        if (obj instanceof PSObjectDict) {
            PSObjectDict dict = (PSObjectDict)obj;
            dict.setKey(indexKey, any);
        } else {
            throw new PSErrorUnimplemented("operator put not FULLY implemented.");
        }
    }
    
    /** PostScript op: readhexstring */
    public void op_readhexstring() throws PSError {
        throw new PSErrorUnimplemented("operator: readhexstring");
    }
    
    /**
     * PostScript op: readonly
     */
    public void op_readonly() {
        log.info("readonly operator encountered. This feature is not implemented. This may lead to incorrect results.");
    }
    
    /** PostScript op: rectclip */
    public void op_rectclip() throws PSError, IOException {
        PSObject heightObj = opStack.pop();
        if ( (heightObj instanceof PSObjectArray) || (heightObj instanceof PSObjectString) ) {
            throw new PSErrorUnimplemented("rectclip operator not fully implemented");
        }
        double height = heightObj.toReal();
        double width = opStack.pop().toReal();
        double y = opStack.pop().toReal();
        double x = opStack.pop().toReal();
        
        // rectclip implemented in PostScript. See PostScript manual for
        // the code below.
        op_newpath();
        gstate.current.moveto(x, y);
        gstate.current.rlineto(width, 0);
        gstate.current.rlineto(0, height);
        gstate.current.rlineto(-width, 0);
        op_closepath();
        op_clip();
        op_newpath();
    }
    
    /** PostScript op: rectfill */
    public void op_rectfill() throws PSError, IOException {
        PSObject heightObj = opStack.pop();
        if ( (heightObj instanceof PSObjectArray) || (heightObj instanceof PSObjectString) ) {
            throw new PSErrorUnimplemented("rectclip operator not fully implemented");
        }
        double height = heightObj.toReal();
        double width = opStack.pop().toReal();
        double y = opStack.pop().toReal();
        double x = opStack.pop().toReal();
        
        // rectfill implemented in PostScript. See PostScript manual for
        // the code below.
        op_gsave();
        op_newpath();
        gstate.current.moveto(x, y);
        gstate.current.rlineto(width, 0);
        gstate.current.rlineto(0, height);
        gstate.current.rlineto(-width, 0);
        op_closepath();
        op_fill();
        op_grestore();
    }    
    
    /** PostScript op: repeat */
    public void op_repeat() throws Exception {
        PSObjectProc proc = opStack.pop().toProc();
        int n = opStack.pop().toNonNegInt();
        
        for (int i = 0 ; i < n ; i++) {
            proc.execute(this);
        }
    }    
    
    /** PostScript op: restore */
    public void op_restore() throws PSErrorTypeCheck, PSErrorStackUnderflow {
        PSObject obj = opStack.pop();
        
        // Check whether the popped object is a save object (see op_save())
        if (!(obj instanceof PSObjectName)) {
            throw new PSErrorTypeCheck();
        }
        if (!((PSObjectName)obj).name.equals("-save- (dummy)")) {
            throw new PSErrorTypeCheck();
        }
        
        log.info("restore operator ignored. This might have an effect on the result.");
    }    
    
    /** PostScript op: rmoveto */
    public void op_rlineto() throws PSError {
        double dy = opStack.pop().toReal();
        double dx = opStack.pop().toReal();
        gstate.current.rlineto(dx, dy);
    }    

    /** PostScript op: rmoveto */
    public void op_rmoveto() throws PSError {
        throw new PSErrorUnimplemented("operator: rmoveto");
    }    

    /** PostScript op: roll */
    public void op_roll() throws PSError {
        int j = opStack.pop().toInt();
        int n = opStack.pop().toNonNegInt();
        if (n == 0) {
            return;
        }

        // Pop top n element from the stack
        PSObject[] lst = new PSObject[n];
        for (int i = n-1 ; i >= 0 ; i--) {
            lst[i] = opStack.pop();
        }
        
        // Roll elements
        j = j % n;
        if (j < 0) {
            j = j + n;
        }
        PSObject[] rolledList = new PSObject[n];
        for (int i = 0 ; i < n ; i++) {
            int rolledIndex = (i+j) % n;
            rolledList[rolledIndex] = lst[i];
        }
        
        // Push rolled list back on the stack
        for (int i = 0 ; i < n ; i++) {
            opStack.push(rolledList[i]);
        }
    }
    
    /** PostScript op: rotate */
    public void op_rotate() throws PSError {
        PSObject obj = opStack.pop();
        double angle;
        if ( (obj instanceof PSObjectArray) || (obj instanceof PSObjectMatrix) ) {
            PSObjectMatrix matrix = obj.toMatrix();
            angle = opStack.pop().toReal();
            throw new PSErrorUnimplemented("scale operator not yet fully implemented.");
        } else {
            angle = obj.toReal();
            gstate.current.CTM.rotate(angle);
        }
    }
    
    /**
     * PostScript op: round
     */
    public void op_round() throws PSErrorStackUnderflow, PSErrorTypeCheck {
        PSObject obj = opStack.pop();
        if (obj instanceof PSObjectInt) {
            opStack.push(obj);
        } else {
            PSObjectReal rounded = new PSObjectReal(Math.round(obj.toReal()));
            opStack.push(rounded);
        }
    }
   
    /** PostScript op: save */
    public void op_save() throws PSError {
        opStack.push(new PSObjectName("/-save- (dummy)"));
        log.info("save operator ignored. This might have an effect on the result.");
    }
   
    /** PostScript op: scale */
    public void op_scale() throws PSError {
        PSObject obj = opStack.pop();
        double sx, sy;
        if ( (obj instanceof PSObjectArray) || (obj instanceof PSObjectMatrix) ) {
            PSObjectMatrix matrix = obj.toMatrix();
            sy = opStack.pop().toReal();
            sx = opStack.pop().toReal();
            throw new PSErrorUnimplemented("scale operator not yet fully implemented.");
        } else {
            sy = obj.toReal();
            sx = opStack.pop().toReal();
            gstate.current.CTM.scale(sx, sy);
        }
    }
    
    /**
     * PostScript operator: scalefont
     */
    public void op_scalefont() throws PSErrorStackUnderflow, PSErrorTypeCheck, CloneNotSupportedException {
        double scale = opStack.pop().toReal();
        
        // "font scale scalefont" is equivalent to 
        // "font [scale 0 0 scale 0 0] makefont""
        op_sqBrackLeft();
        opStack.push(new PSObjectReal(scale));
        opStack.push(new PSObjectReal(0));
        opStack.push(new PSObjectReal(0));
        opStack.push(new PSObjectReal(scale));
        opStack.push(new PSObjectReal(0));
        opStack.push(new PSObjectReal(0));
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
   
    /** PostScript op: setcmykcolor */
    public void op_setcmykcolor() throws PSError {
        throw new PSErrorUnimplemented("operator: setcmykcolor");
    }
   
    /** PostScript op: setdash */
    public void op_setdash() throws PSError, IOException {
        double offset = opStack.pop().toReal();
        PSObjectArray array = opStack.pop().toArray();
        
        // Scale all distances
        double scaling = gstate.current.CTM.getMeanScaling();
        offset *= scaling;
        for (int i = 0 ; i < array.size() ; i++) {
            double value = array.get(i).toReal();
            array.set(i, new PSObjectReal(value*scaling));
        }
        
        exp.setDash(array, offset);
    }
    
    /**
     * PostScript op: setflat
     */
    public void op_setflat() throws PSErrorStackUnderflow, PSErrorTypeCheck {
        double num = opStack.pop().toReal();
        num = Math.max(num, 0.2);
        num = Math.min(num, 100);
        // This operator doesn't do anything in eps2pgf. It is handled by PGF.
    }
   
    /** PostScript op: setfont */
    public void op_setfont() throws PSErrorTypeCheck, PSErrorStackUnderflow {
        PSObjectFont font = opStack.pop().toFont();
        gstate.current.font = font;
    }
   
    /** PostScript op: setrgbcolor */
    public void op_setrgbcolor() throws PSError, IOException {
        double blue = opStack.pop().toReal();
        double green = opStack.pop().toReal();
        double red = opStack.pop().toReal();
        exp.setColor(red, green, blue);
    }
   
    /** PostScript op: setgray */
    public void op_setgray() throws PSError, IOException {
        double level = opStack.pop().toReal();
        exp.setColor(level);
    }
   
    /** PostScript op: setlinecap */
    public void op_setlinecap() throws PSError, IOException {
        int cap = opStack.pop().toNonNegInt();
        exp.setlinecap(cap);
    }
   
    /** PostScript op: setlinejoin */
    public void op_setlinejoin() throws PSError, IOException {
        int join = opStack.pop().toNonNegInt();
        exp.setlinejoin(join);
    }
   
    /** PostScript op: setlinewidth */
    public void op_setlinewidth() throws PSError, IOException {
        double lineWidth = opStack.pop().toReal();
        
        // Apply CTM to linewidth, now the line width is in micrometer
        lineWidth *= gstate.current.CTM.getMeanScaling();
        // Convert micrometer to mm
        lineWidth /= 1000;
        
        exp.setlinewidth(lineWidth);
    }
   
    /** PostScript op: setmatrix */
    public void op_setmatrix() throws PSError {
        throw new PSErrorUnimplemented("operator: setmatrix");
    }
    
    /**
     * PostScript op: setmiterlimit
     */
    public void op_setmiterlimit() throws PSErrorStackUnderflow, PSErrorTypeCheck, 
            PSErrorRangeCheck, IOException {
        double num = opStack.pop().toReal();
        if (num < 1.0) {
            throw new PSErrorRangeCheck();
        }
        exp.setmiterlimit(num);
    }
   
    /** PostScript op: show */
    public void op_show() throws PSErrorTypeCheck, PSErrorStackUnderflow,
            PSErrorRangeCheck, PSErrorUnimplemented, PSErrorUndefined, 
            PSErrorNoCurrentPoint, IOException {
        PSObjectString string = opStack.pop().toPSString();
        
        double[] dpos = textHandler.showText(exp, string);
        
    }
    
    /** PostScript op: showpage */
    public void op_showpage() {
        // This operator has no meaning in eps2pgf
    }
   
    /** PostScript op: StandardEncoding */
    public void op_StandardEncoding() {
        PSObjectName[] encodingVector = Encoding.getStandardVector();
        opStack.push(new PSObjectArray(encodingVector));
    }
    
    /**
     * PostScript op: stopped
     * @throws PSErrorStackUnderflow Operand stack is empty
     * @throws PSErrorUnimplemented Executed code contains postscript code that is not yet implemented in eps2pgf
     * @throws Another exception occured. If this happens it's a bug in eps2pgf
     */
    public void op_stopped() throws PSErrorStackUnderflow, PSErrorUnimplemented, Exception {
        PSObject any = opStack.pop();
        try {
            any.execute(this);
        } catch (PSErrorUnimplemented e) {
            // Don't catch unimplemented errors since they indicate that
            // eps2pgf is not fully implemented. It is not an actual
            // postscript error
            throw e;
        } catch (PSError e) {
            opStack.push(new PSObjectBool(true));
            return;
        } catch (Exception e) {
            throw e;
        }
        opStack.push(new PSObjectBool(false));
    }
   
    /** PostScript op: stroke */
    public void op_stroke() throws PSError, IOException {
        exp.stroke(gstate.current.path);
        op_newpath();
    }
   
    /** PostScript op: [ */
    public void op_sqBrackLeft() {
        opStack.push(new PSObjectMark());
    }
    
    /** PostScript op: ] */
    public void op_sqBrackRight() throws PSErrorStackUnderflow, PSErrorTypeCheck, PSErrorUnmatchedMark {
        op_counttomark();
        int n = opStack.pop().toInt();
        PSObject[] objs = new PSObject[n];
        for (int i = n-1 ; i >= 0 ; i--) {
            objs[i] = opStack.pop();
        }
        opStack.pop();  // clear mark
        opStack.push(new PSObjectArray(objs));
        
        
    }
    
    /** PostScript op: store */
    public void op_store() throws PSError {
        PSObject value = opStack.pop();
        PSObject key = opStack.pop();
        dictStack.store(key, value);
    }

    /** PostScript op: sub */
    public void op_sub() throws PSError {
        PSObject num2 = opStack.pop();
        PSObject num1 = opStack.pop();
        if ( (num1 instanceof PSObjectInt) && (num2 instanceof PSObjectInt) ) {
            opStack.push(new PSObjectInt( num1.toInt() - num2.toInt() ));
        } else {
            opStack.push(new PSObjectReal( num1.toReal() - num2.toReal() ));
        }
    }
    
    /**
     * PostScript op: transform
     */
    public void op_transform() throws PSErrorStackUnderflow, PSErrorTypeCheck, PSErrorRangeCheck {
        PSObject obj = opStack.pop();
        PSObjectMatrix matrix = null;
        try {
            matrix = obj.toMatrix();
        } catch (PSErrorTypeCheck e) {
            
        }
        double y;
        if (matrix == null) {
            matrix = gstate.current.CTM;
            y = obj.toReal();
        } else {
            y = opStack.pop().toReal();
        }
        double x = opStack.pop().toReal();
        double transformed[] = matrix.apply(x, y);
        opStack.push(new PSObjectReal(transformed[0]));
        opStack.push(new PSObjectReal(transformed[1]));
    }
    
    /** PostScript op: translate */
    public void op_translate() throws PSError {
        PSObject obj = opStack.pop();
        double tx, ty;
        if ( (obj instanceof PSObjectArray) || (obj instanceof PSObjectMatrix) ) {
            PSObjectMatrix matrix = obj.toMatrix();
            ty = opStack.pop().toReal();
            tx = opStack.pop().toReal();
            throw new PSErrorUnimplemented("translate operator not yet fully implemented.");
        } else {
            ty = obj.toReal();
            tx = opStack.pop().toReal();
            gstate.current.CTM.translate(tx, ty);
        }
    }
    
    /** PostScript op: true */
    public void op_true() {
        opStack.push(new PSObjectBool(true));
    }
    
    /** PostScript op: where */
    public void op_where() throws PSError {
        PSObject key = opStack.pop();
        PSObjectDict dict = dictStack.where(key);
        if (dict == null) {
            opStack.push(new PSObjectBool(false));
        } else {
            opStack.push(dict);
            opStack.push(new PSObjectBool(true));
        }
    }
   
}
