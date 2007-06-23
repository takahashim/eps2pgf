/*
 * Interpreter.java
 *
 * This file is part of Eps2pgf.
 *
 * Copyright 2007 Paul Wagenaars <pwagenaars@fastmail.fm>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
    
    // Header information of the file being interpreted
    DSCHeader header;
    
    // Default clipping path
    Path defaultClippingPath;
    
    Logger log = Logger.getLogger("global");
    
    /**
     * Creates a new instance of Interpreter
     * @param in PostScript document
     * @param out Destination for output code
     * @throws java.io.FileNotFoundException Unable to find font resources
     */
    public Interpreter(List<PSObject> in, Writer out, DSCHeader fileHeader) throws FileNotFoundException {
        docObjects = in;
        header = fileHeader;
        
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
        initialize();
        
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
     * Do some initialization tasks
     **/
    void initialize() throws IOException, PSErrorUnimplemented,
            PSErrorInvalidAccess, PSErrorRangeCheck, PSErrorTypeCheck {
        exp.init();
        
        // Default line width in PostScript is 1pt, while in PGF it is 0.4pt
        exp.setlinewidth(gstate.current.CTM.getMeanScaling());
        
        // An eps-file defines a bounding box. Set this bounding box as the
        // default clipping path.
        double[] bbox = header.boundingBox;
        double left, right, top, bottom;
        if (bbox != null) {
            left = bbox[0];
            bottom = bbox[1];
            right = bbox[2];
            top = bbox[3];
        } else {
            // If no bounding box is default we use A4 paper. But the figure is
            // not automatically clipped by this, so in most situation it won't
            // have any effect on the result (only in situation where e.g.
            // 'initclip' is used).
            left = 0;
            bottom = 0;
            right = 595;
            top = 842;
        }
        gstate.current.moveto(left, bottom);
        gstate.current.lineto(right, bottom);
        gstate.current.lineto(right, top);
        gstate.current.lineto(left, top);
        gstate.current.path.closepath();
        defaultClippingPath = gstate.current.path;
        op_newpath();
        if (bbox != null) {
            op_initclip();
        }
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
    
    /** PostScript op: abs */
    public void op_abs() throws PSErrorStackUnderflow, PSErrorTypeCheck {
        PSObject obj = opStack.pop();
        opStack.push(obj.abs());
    }
    
    /** PostScript op: add */
    public void op_add() throws PSErrorStackUnderflow, PSErrorTypeCheck {
        PSObject num2 = opStack.pop();
        PSObject num1 = opStack.pop();
        opStack.push(num1.add(num2));
    }
    
    /** PostScript op: aload */
    public void op_aload() throws PSErrorStackUnderflow, PSErrorTypeCheck {
        PSObject array = opStack.pop();
        for (PSObject obj : array.toArray()) {
            opStack.push(obj);
        }
        opStack.push(array);
    }
    
    /** PostScript op: anchorsearch */
    public void op_anchorsearch() throws PSErrorStackUnderflow, PSErrorTypeCheck,
            PSErrorInvalidAccess {
        String seek = opStack.pop().toPSString().toString();
        PSObjectString string = opStack.pop().toPSString();
        List<PSObject> result = string.anchorsearch(seek);
        while (!result.isEmpty()) {
            opStack.push(result.remove(0));
        }
    }
    
    /** PostScript op: and */
    public void op_and() throws PSErrorStackUnderflow, PSErrorTypeCheck {
        PSObject obj2 = opStack.pop();
        PSObject obj1 = opStack.pop();
        opStack.push(obj1.and(obj2));
    }
    
    /** PostScript op: arc */
    public void op_arc() throws PSErrorUnimplemented {
        throw new PSErrorUnimplemented("operator: arc");
    }
    
    /** PostScript op: arcn */
    public void op_arcn() throws PSErrorUnimplemented {
        throw new PSErrorUnimplemented("operator: arcn");
    }
    
    /** PostScript op: array */
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
    
    /** PostScript op: ashow */
    public void op_ashow() throws PSErrorTypeCheck, PSErrorStackUnderflow,
            PSErrorRangeCheck, PSErrorUnimplemented, PSErrorUndefined, 
            PSErrorNoCurrentPoint, PSErrorInvalidAccess, IOException {
        log.fine("ashow operator encoutered. ashow is not implemented, instead the normal show is used.");
        PSObjectString string = opStack.pop().toPSString();
        double ay = opStack.pop().toReal();
        double ax = opStack.pop().toReal();
        opStack.push(string);
        op_show();
    }
    
    
    /** PostScript op: astore */
    public void op_astore() throws PSErrorStackUnderflow, PSErrorTypeCheck,
            PSErrorInvalidAccess {
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
    
    /** PostScript op: atan */
    public void op_atan() throws PSErrorStackUnderflow, PSErrorTypeCheck {
        double den = opStack.pop().toReal();
        double num = opStack.pop().toReal();
        double result = Math.atan2(num, den) / Math.PI * 180;
        // Java atan method returns in range -180 to 180, while the PostScript
        // function should return in range 0-360
        result = (result + 360.0) % 360.0;
        opStack.push(new PSObjectReal(result));
    }
    
    /** PostScript op: begin */
    public void op_begin() throws PSErrorStackUnderflow, PSErrorTypeCheck {
        PSObjectDict dict = opStack.pop().toDict();
        dictStack.pushDict(dict);
    }
    
    /** PostScript op: bind */
    public void op_bind() throws PSErrorStackUnderflow, PSErrorTypeCheck {
        PSObjectArray obj = opStack.peek().toArray().bind(this);
    }
    
    /** PostScript op: bitshift */
    public void op_bitshift() throws PSErrorStackUnderflow, PSErrorTypeCheck {
        int shift = opStack.pop().toInt();
        int int1 = opStack.pop().toInt();
        if (shift >= 0) {
            int1 <<= shift;
        } else {
            int1 >>>= -shift;
        }
        opStack.push(new PSObjectInt(int1));
    }
    
    /** PostScript op: ceiling */
    public void op_ceiling() throws PSErrorStackUnderflow, PSErrorTypeCheck {
        PSObject obj = opStack.pop();
        opStack.push(obj.ceiling());
    }
    
    /** PostScript op: clear */
    public void op_clear() {
        opStack.clear();
    }
    
    /** PostScript op: cleardictstack */
    public void op_cleardictstack() {
        dictStack.cleardictstack();
    }
    
    /** PostScript op: cleartomark */
    public void op_cleartomark() throws PSErrorUnmatchedMark, PSErrorStackUnderflow {
        int n = opStack.size();
        for (int i = 0 ; i < n ; i++) {
            if (opStack.pop() instanceof PSObjectMark) {
                return;
            }
        }
        throw new PSErrorUnmatchedMark();
    }
    
    /** PostScript op: clip */
    public void op_clip() throws PSErrorUnimplemented, IOException {
        gstate.current.clip();
        exp.clip(gstate.current.clippingPath);
    }
    
    /** PostScript op: clippath */
    public void op_clippath() {
        gstate.current.path = gstate.current.clippingPath.clone();
    }
    
    /** PostScript op: closepath */
    public void op_closepath() throws PSErrorInvalidAccess, PSErrorRangeCheck,
            PSErrorTypeCheck {
        double[] startPos = gstate.current.path.closepath();
        if (startPos != null) {
            gstate.current.moveto(startPos[0], startPos[1]);
        }
    }
    
    /** PostScript op: concat */
    public void op_concat() throws PSError {
        PSObjectMatrix matrix = opStack.pop().toMatrix();
        gstate.current.CTM.concat(matrix);
        gstate.current.updatePosition();
    }
    
    /** PostScript op: concantmatrix */
    public void op_concatmatrix() throws PSErrorStackUnderflow, PSErrorRangeCheck,
            PSErrorTypeCheck, PSErrorInvalidAccess {
        PSObjectMatrix matrix3 = opStack.pop().toMatrix();
        PSObjectMatrix matrix2 = opStack.pop().toMatrix();
        PSObjectMatrix matrix1 = opStack.pop().toMatrix();
        matrix3.copy(matrix2);
        matrix3.concat(matrix1);
        opStack.push(matrix3);
    }
    
    /** PostScript op: copy */
    public void op_copy() throws PSError {
        PSObject obj = opStack.pop();
        if (obj instanceof PSObjectInt) {
            // Get n, the number of copies to make
            int n = obj.toNonNegInt();
            int stackSize = opStack.size();
        
            for (int i = stackSize-n ; i < stackSize ; i++) {
                opStack.push(opStack.get(i));
            }
        } else {
            PSObject obj1 = opStack.pop();
            PSObject subseq = obj.copy(obj1);
            opStack.push(subseq);
        }
    }
    
    /** PostScript op: cos */
    public void op_cos() throws PSErrorStackUnderflow, PSErrorTypeCheck {
        double angle = opStack.pop().toReal();
        opStack.push(new PSObjectReal(Math.cos(angle*Math.PI/180)));
    }
    
    /** PostScript op: count */
    public void op_count() {
        int count = opStack.size();
        PSObjectInt n = new PSObjectInt(count);
        opStack.push(n);
    }
    
    /** PostScript op: countdictstack */
    public void op_countdictstack() {
        opStack.push(new PSObjectInt(dictStack.countdictstack()));
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
    public void op_currentdict() {
        opStack.push(dictStack.peekDict());
    }
    
    /** PostScript op: currentfile */
    public void op_currentfile() throws PSError {
        throw new PSErrorUnimplemented("operator: currentfile");
    }
    
    /** PostScript op: currentflat */
    public void op_currentflat() {
        // pgf does not support changing the flatness
        opStack.push(new PSObjectReal(0.2));
    }
    
    /** PostScript op: currentmatrix */
    public void op_currentmatrix() throws PSError {
        PSObjectMatrix matrix = opStack.pop().toMatrix();
        matrix.copy(gstate.current.CTM);
        opStack.push(matrix);
    }
    
    /** PostScript op: currentpoint */
    public void op_currentpoint() throws PSErrorNoCurrentPoint, PSErrorTypeCheck,
            PSErrorRangeCheck, PSErrorInvalidAccess {
        double[] currentDevice = gstate.current.getCurrentPosInDeviceSpace();
        double[] currentUser = gstate.current.CTM.inverseApply(currentDevice);
        opStack.push(new PSObjectReal(currentUser[0]));
        opStack.push(new PSObjectReal(currentUser[1]));
    }
    
    /** PostScript op: curveto */
    public void op_curveto() throws PSErrorStackUnderflow, PSErrorTypeCheck,
            PSErrorInvalidAccess, PSErrorRangeCheck {
        double y3 = opStack.pop().toReal();
        double x3 = opStack.pop().toReal();
        double y2 = opStack.pop().toReal();
        double x2 = opStack.pop().toReal();
        double y1 = opStack.pop().toReal();
        double x1 = opStack.pop().toReal();
        gstate.current.curveto(x1,y1, x2,y2, x3,y3);
    }
    
    /** PostScrip op: cvi */
    public void op_cvi() throws PSErrorStackUnderflow, PSErrorTypeCheck,
            PSErrorInvalidAccess {
        PSObject obj = opStack.pop();
        opStack.push(new PSObjectInt(obj.cvi()));
    }
    
    /** PostScript op: cvlit */
    public void op_cvlit() throws PSErrorStackUnderflow, PSErrorUnimplemented {
        PSObject any = opStack.pop();
        opStack.push(any.cvlit());
    }
    
    /** PostScript op: cvn */
    public void op_cvn() throws PSErrorStackUnderflow, PSErrorTypeCheck,
            PSErrorInvalidAccess {
        PSObjectString str = opStack.pop().toPSString();
        opStack.push(str.cvn());
    }
    
    /** PostScript op: cvr */
    public void op_cvr() throws PSErrorStackUnderflow, PSErrorTypeCheck,
            PSErrorInvalidAccess {
        PSObject any = opStack.pop();
        opStack.push(new PSObjectReal(any.cvr()));
    }
    
    /** PostScript op: cvrs */
    public void op_cvrs() throws PSErrorStackUnderflow, PSErrorTypeCheck,
            PSErrorRangeCheck {
        PSObjectString string = opStack.pop().toPSString();
        int radix = opStack.pop().toInt();
        PSObject num = opStack.pop();
        opStack.push(new PSObjectString(num.cvrs(radix)));
    }
    
    /** PostScript op: cvs */
    public void op_cvs() throws PSErrorStackUnderflow, PSErrorTypeCheck,
            PSErrorRangeCheck, PSErrorInvalidAccess {
        PSObjectString string = opStack.pop().toPSString();
        PSObject any = opStack.pop();
        string.overwrite(any.cvs());
        opStack.push(string);
    }
    
    /** PostScript op: cvx */
    public void op_cvx() throws PSErrorStackUnderflow {
        PSObject any = opStack.pop();
        opStack.push(any.cvx());
    }
    
    /**
     * PostScript op: >>
     * this operator is equivalent to the following code (from PostScript manual)
     * counttomark 2 idiv
     * dup dict
     * begin
     *   {def} repeat
     *   pop
     *   currentdict
     * end
     */
    public void op_dblGreaterBrackets() throws PSErrorStackUnderflow, 
            PSErrorUnmatchedMark, PSErrorTypeCheck, PSErrorRangeCheck, 
            PSErrorDictStackUnderflow, IOException, Exception {
        PSObjectArray defProc = new PSObjectArray("{def}");
        
        op_counttomark(); opStack.push(new PSObjectInt(2)); op_idiv();
        op_dup(); op_dict();
        op_begin();
          opStack.push(defProc); op_repeat();
          op_pop();
          op_currentdict();
        op_end();
    }
    
    /** PostScript op: << */
    public void op_dblLessBrackets() {
        op_mark();
    }
    
    /** PostScript op: def */
    public void op_def() throws PSError {
        PSObject value = opStack.pop();
        PSObject key = opStack.pop();
        dictStack.def(key, value);
    }
    
    /** PostScript op: defaultmatrix */
    public void op_defaultmatrix() throws PSErrorStackUnderflow,
            PSErrorRangeCheck, PSErrorTypeCheck, PSErrorInvalidAccess {
        PSObjectMatrix matrix = opStack.pop().toMatrix();
        matrix.copy(gstate.current.defaultCTM);
        opStack.push(matrix);
    }
    
    /** PostScript op: definefont */
    public void op_definefont() throws PSError {
        PSObjectFont font = opStack.pop().toFont();
        PSObject key = opStack.pop();
        opStack.push( fonts.defineFont(key, font) );
    }
    
    /** PostScript op: dict */
    public void op_dict() throws PSErrorStackUnderflow, PSErrorTypeCheck, 
            PSErrorRangeCheck {
        
        int capacity = opStack.pop().toNonNegInt();
        opStack.push(new PSObjectDict(capacity));
    }
    
    /** PostScript op: dictstack */
    public void op_dictstack() throws PSErrorStackUnderflow,
            PSErrorTypeCheck, PSErrorRangeCheck, PSErrorInvalidAccess {
        PSObjectArray array = opStack.pop().toArray();
        opStack.push(dictStack.dictstack(array));
    }
    
    /** PostScript op: div */
    public void op_div() throws PSError {
        double num2 = opStack.pop().toReal();
        double num1 = opStack.pop().toReal();
        opStack.push(new PSObjectReal( num1 / num2 ));
    }
    
    /** PostScript op: dup */
    public void op_dup() throws PSErrorStackUnderflow {
        opStack.push(opStack.peek().dup());
    }
    
    /** PostScript op: end */
    public void op_end() throws PSErrorDictStackUnderflow {
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
    public void op_eq() throws PSErrorTypeCheck, PSErrorStackUnderflow, PSErrorInvalidAccess {
        PSObject any2 = opStack.pop();
        PSObject any1 = opStack.pop();
        opStack.push(new PSObjectBool(any1.eq(any2)));
    }
    
    /**
     * PostScript op: exch
     */
    public void op_exch() throws PSErrorStackUnderflow {
        PSObject any2 = opStack.pop();
        PSObject any1 = opStack.pop();
        opStack.push(any2);
        opStack.push(any1);
    }
    
    /**
     * PostScript op: exec
     */
    public void op_exec() throws PSErrorStackUnderflow, Exception {
        PSObject any = opStack.pop();
        any.execute(this);
    }
    
    /** PostScript op: executeonly */
    public void op_executeonly() throws PSErrorStackUnderflow,
            PSErrorTypeCheck, PSErrorInvalidAccess {
        PSObject obj = opStack.pop();
        obj.executeonly();
        opStack.push(obj);
    }
    
    /**
     * PostScript op: exp
     */
    public void op_exp() throws PSErrorStackUnderflow, PSErrorTypeCheck {
        double exponent = opStack.pop().toReal();
        double base = opStack.pop().toReal();
        double result = Math.pow(base, exponent);
        opStack.push(new PSObjectReal(result));
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
    
    /**
     * PostScript op: floor
     */
    public void op_floor() throws PSErrorStackUnderflow, PSErrorTypeCheck {
        PSObject obj = opStack.pop();
        opStack.push(obj.floor());
    }
    
    /**
     * PostScript op: for
     */
    public void op_for() throws PSErrorStackUnderflow, PSErrorTypeCheck,
            Exception {
        PSObjectArray proc = opStack.pop().toProc();
        double limit = opStack.pop().toReal();
        double inc = opStack.pop().toReal();
        double initial = opStack.pop().toReal();
        
        // Check whether limit, inc and initial are all three integers
        boolean allIntegers = false;
        if ( (limit == Math.round(limit)) && (inc == Math.round(inc)) && 
                (initial == Math.round(initial)) ) {
            allIntegers = true;
        }
        
        // Prevent (virtually) infinite loops
        if (inc == 0) {
            return;
        } else if ( (inc > 0) && (limit <= initial) ) {
            return;
        } else if ( (inc < 0) && (limit >= initial) ) {
            return;
        }
        
        // Execute the for loop
        double control = initial;
        while (true) {
            if ( (inc > 0) && (control > limit) ) {
                break;
            } else if ( (inc < 0) && (control < limit) ) {
                break;
            }
            
            if (allIntegers) {
                opStack.push(new PSObjectInt(control));
            } else {
                opStack.push(new PSObjectReal(control));
            }
            proc.execute(this);
            
            control += inc;
        }
    }
    
    /** PostScript op: forall */
    public void op_forall() throws Exception {
        PSObjectArray proc = opStack.pop().toProc();
        PSObject obj = opStack.pop();
        List<PSObject> items = obj.getItemList();
        int N = items.remove(0).toNonNegInt();
        while (!items.isEmpty()) {
            for (int i = 0 ; i < N ; i++) {
                 opStack.push(items.remove(0));
            }
            proc.execute(this);
        }
    }
    
    /** PostScript op: ge */
    public void op_ge() throws PSErrorStackUnderflow, PSErrorTypeCheck,
            PSErrorInvalidAccess {
        PSObject obj2 = opStack.pop();
        PSObject obj1 = opStack.pop();
        boolean gt = obj1.gt(obj2);
        boolean eq = obj1.eq(obj2);
        opStack.push(new PSObjectBool(gt || eq));
    }
    
    /** PostScript op: get */
    public void op_get() throws PSErrorStackUnderflow, PSErrorTypeCheck,
            PSErrorUndefined, PSErrorRangeCheck, PSErrorInvalidAccess {
        PSObject indexKey = opStack.pop();
        PSObject obj = opStack.pop();
        opStack.push(obj.get(indexKey));
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
    
    /** PostScript op: gt */
    public void op_gt() throws PSErrorStackUnderflow, PSErrorTypeCheck,
            PSErrorInvalidAccess {
        PSObject obj2 = opStack.pop();
        PSObject obj1 = opStack.pop();
        boolean chk = obj1.gt(obj2);
        opStack.push(new PSObjectBool(chk));
    }
    
    /** PostScript op: identmatrix */
    public void op_identmatrix() throws PSErrorStackUnderflow, PSErrorRangeCheck,
            PSErrorTypeCheck, PSErrorInvalidAccess {
        PSObjectMatrix matrix = opStack.pop().toMatrix();
        matrix.copy(new PSObjectMatrix());
        opStack.push(matrix);
    }
    
    /** PostScript op: idiv */
    public void op_idiv() throws PSErrorStackUnderflow, PSErrorTypeCheck {
        int int2 = opStack.pop().toInt();
        int int1 = opStack.pop().toInt();
        int quotient = int1 / int2;
        opStack.push(new PSObjectInt(quotient));
    }
    
    /** PostScript op: if */
    public void op_if() throws Exception {
        PSObjectArray proc = opStack.pop().toProc();
        boolean bool = opStack.pop().toBool();
        if (bool) {
            proc.execute(this);
        }
    }
    
    /** PostScript op: ifelse */
    public void op_ifelse() throws Exception {
        PSObjectArray proc2 = opStack.pop().toProc();
        PSObjectArray proc1 = opStack.pop().toProc();
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
    
    /** PostScript op: initclip */
    public void op_initclip() throws IOException, PSErrorUnimplemented {
        gstate.current.clippingPath = defaultClippingPath.clone();
        exp.clip(gstate.current.clippingPath);
    }
    
    /** PostScript op: initmatrix */
    public void op_initmatrix() {
        gstate.current.initmatrix();
    }
    
    /** PostScript op: itransform */
    public void op_itransform() throws PSErrorStackUnderflow, PSErrorTypeCheck, 
            PSErrorRangeCheck, PSErrorInvalidAccess {
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
        opStack.push(new PSObjectBool(dict.known(key)));
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
    
    /** PostScript op: le */
    public void op_le() throws PSErrorStackUnderflow, PSErrorTypeCheck,
            PSErrorInvalidAccess {
        op_gt();
        op_not();
    }
    
    /** PostScript op: length */
    public void op_length() throws PSErrorStackUnderflow, PSErrorTypeCheck,
            PSErrorInvalidAccess {
        PSObject obj = opStack.pop();
        opStack.push(new PSObjectInt(obj.length()));
    }
    
    /** PostScript op: lineto */
    public void op_lineto() throws PSError {
        double y = opStack.pop().toReal();
        double x = opStack.pop().toReal();
        gstate.current.lineto(x, y);
    }
    
    /**
     * PostScript op: ln
     */
    public void op_ln() throws PSErrorStackUnderflow, PSErrorTypeCheck {
        double num = opStack.pop().toReal();
        double result = Math.log(num);
        opStack.push(new PSObjectReal(result));
    }
    
    /**
     * PostScript op: log
     */
    public void op_log() throws PSErrorStackUnderflow, PSErrorTypeCheck {
        double num = opStack.pop().toReal();
        double result = Math.log10(num);
        opStack.push(new PSObjectReal(result));
    }
    
    /** PostScript op: lt */
    public void op_lt() throws PSErrorStackUnderflow, PSErrorTypeCheck,
            PSErrorInvalidAccess {
        op_ge();
        op_not();
    }
    
    /** PostScript op: makefont */
    public void op_makefont() throws PSErrorStackUnderflow, PSErrorTypeCheck, 
            PSErrorRangeCheck, PSErrorInvalidAccess {
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
        opStack.push(new PSObjectMatrix());
    }
    
    /** PostScript op: maxlength */
    public void op_maxlength() throws PSErrorStackUnderflow, PSErrorTypeCheck,
            PSErrorInvalidAccess {
        PSObjectDict dict = opStack.pop().toDict();
        opStack.push(new PSObjectInt(dict.maxlength()));
    }
    
    /** PostScript operator: mod */
    public void op_mod() throws PSErrorStackUnderflow, PSErrorTypeCheck {
        int int2 = opStack.pop().toInt();
        int int1 = opStack.pop().toInt();
        opStack.push(new PSObjectInt( int1 % int2 ));
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
        opStack.push(num1.mul(num2));;
    }
    
    /** PostScript op: ne */
    public void op_ne() throws PSError {
        op_eq();
        op_not();
    }

    /** PostScript op: neg */
    public void op_neg() throws PSError {
        PSObject obj = opStack.pop();
        opStack.push(obj.neg());
    }

    /** PostScript op: newpath */
    public void op_newpath() {
        gstate.current.path = new Path(gstate);
        gstate.current.position[0] = Double.NaN;
        gstate.current.position[1] = Double.NaN;
    }
    
    /** PostScript op: noaccess */
    public void op_noaccess() throws PSErrorStackUnderflow, PSErrorTypeCheck,
            PSErrorInvalidAccess {
        PSObject obj = opStack.pop();
        obj.noaccess();
        opStack.push(obj);
    }
    
    /** PostScript op: not */
    public void op_not() throws PSErrorStackUnderflow, PSErrorTypeCheck {
        PSObject obj = opStack.pop();
        opStack.push(obj.not());
    }
    
    /** PostScript op: null */
    public void op_null() throws PSError {
        opStack.push(new PSObjectNull());
    }

    /** PostScript op: or */
    public void op_or() throws PSErrorStackUnderflow, PSErrorTypeCheck {
        PSObject obj2 = opStack.pop();
        PSObject obj1 = opStack.pop();
        opStack.push(obj1.or(obj2));
    }
    
    /** PostScript op: picstr */
    public void op_picstr() throws PSError {
        throw new PSErrorUnimplemented("operator: picstr");
    }
    
    /** PostScript op: pop */
    public void op_pop() throws PSErrorStackUnderflow {
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
        obj.put(indexKey, any);
    }
    
    /** PostScript op: putinterval */
    public void op_putinterval() throws PSErrorRangeCheck,
            PSErrorStackUnderflow, PSErrorTypeCheck, PSErrorInvalidAccess {
        PSObject subseq = opStack.pop();
        int index = opStack.pop().toNonNegInt();
        PSObject seq = opStack.pop();
        seq.putinterval(index, subseq);
    }
    
    /** PostScript op: rcheck */
    public void op_rcheck() throws PSErrorStackUnderflow, PSErrorTypeCheck {
        PSObject obj = opStack.pop();
        boolean chk = obj.rcheck();
        opStack.push(new PSObjectBool(chk));
    }
    
    /** PostScript op: readhexstring */
    public void op_readhexstring() throws PSError {
        throw new PSErrorUnimplemented("operator: readhexstring");
    }
    
    /**
     * PostScript op: readonly
     */
    public void op_readonly() throws PSErrorStackUnderflow, PSErrorTypeCheck,
            PSErrorInvalidAccess {
        PSObject obj = opStack.pop();
        obj.readonly();
        opStack.push(obj);
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
    public void op_repeat() throws PSErrorStackUnderflow, PSErrorTypeCheck, 
            PSErrorRangeCheck, Exception {
        PSObjectArray proc = opStack.pop().toProc();
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
    public void op_rlineto() throws PSErrorStackUnderflow, PSErrorTypeCheck,
            PSErrorNoCurrentPoint, PSErrorRangeCheck, PSErrorInvalidAccess {
        double dy = opStack.pop().toReal();
        double dx = opStack.pop().toReal();
        gstate.current.rlineto(dx, dy);
    }    

    /** PostScript op: rmoveto */
    public void op_rmoveto() throws PSErrorStackUnderflow, PSErrorTypeCheck,
            PSErrorNoCurrentPoint, PSErrorInvalidAccess, PSErrorRangeCheck {
        double dy = opStack.pop().toReal();
        double dx = opStack.pop().toReal();
        gstate.current.rmoveto(dx, dy);
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
        if ( obj instanceof PSObjectArray ) {
            PSObjectMatrix matrix = obj.toMatrix();
            angle = opStack.pop().toReal();
            matrix.rotate(angle);
            opStack.push(matrix);
        } else {
            angle = obj.toReal();
            gstate.current.CTM.rotate(angle);
            gstate.current.updatePosition();
        }
    }
    
    /**
     * PostScript op: round
     */
    public void op_round() throws PSErrorStackUnderflow, PSErrorTypeCheck {
        PSObject obj = opStack.pop();
        opStack.push(obj.round());
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
        if ( obj instanceof PSObjectArray ) {
            PSObjectMatrix matrix = obj.toMatrix();
            sy = opStack.pop().toReal();
            sx = opStack.pop().toReal();
            matrix.scale(sx, sy);
            opStack.push(matrix);
        } else {
            sy = obj.toReal();
            sx = opStack.pop().toReal();
            gstate.current.CTM.scale(sx, sy);
            gstate.current.updatePosition();
        }
    }
    
    /**
     * PostScript operator: scalefont
     */
    public void op_scalefont() throws PSErrorStackUnderflow, PSErrorTypeCheck,
            PSErrorInvalidAccess {
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
    
    /** PostScript op: search */
    public void op_search() throws PSErrorStackUnderflow, PSErrorTypeCheck,
            PSErrorInvalidAccess {
        String seek = opStack.pop().toPSString().toString();
        PSObjectString string = opStack.pop().toPSString();
        List<PSObject> result = string.search(seek);
        while (!result.isEmpty()) {
            opStack.push(result.remove(0));
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
        
        exp.setlinewidth(lineWidth);
    }
   
    /** PostScript op: setmatrix */
    public void op_setmatrix() throws PSError {
        PSObjectMatrix matrix = opStack.pop().toMatrix();
        gstate.current.CTM.copy(matrix);
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
    
    /**
     * PostScript op: shfill
     */
    public void op_shfill() throws PSErrorStackUnderflow, PSErrorTypeCheck, 
            PSErrorUnimplemented, PSErrorRangeCheck, PSErrorUndefined, IOException,
            PSErrorInvalidAccess {
        PSObjectDict dict = opStack.pop().toDict();
        exp.shfill(dict, gstate.current);
    }
   
    /** PostScript op: show */
    public void op_show() throws PSErrorTypeCheck, PSErrorStackUnderflow,
            PSErrorRangeCheck, PSErrorUnimplemented, PSErrorUndefined, 
            PSErrorNoCurrentPoint, PSErrorInvalidAccess, IOException {
        
        PSObjectString string = opStack.pop().toPSString();
        double[] dpos = textHandler.showText(exp, string);
        gstate.current.rmoveto(dpos[0], dpos[1]);
    }
    
    /** PostScript op: showpage */
    public void op_showpage() {
        // This operator has no meaning in eps2pgf
    }
    
    /**
     * PostScript op: sin
     */
    public void op_sin() throws PSErrorStackUnderflow, PSErrorTypeCheck {
        double angle = opStack.pop().toReal();
        opStack.push(new PSObjectReal(Math.sin(angle*Math.PI/180)));
    }
    
    /**
     * PostScript op: sqrt
     */
    public void op_sqrt() throws PSErrorStackUnderflow, PSErrorTypeCheck,
            PSErrorRangeCheck {
        double x = opStack.pop().toNonNegReal();
        x = Math.sqrt(x);
        opStack.push(new PSObjectReal(x));
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
    
    /**
     * PostScript op: string
     */
    public void op_string() throws PSErrorStackUnderflow, PSErrorTypeCheck,
            PSErrorRangeCheck {
        int n = opStack.pop().toNonNegInt();
        opStack.push(new PSObjectString(n));
    }
   
    /**
     * PostScript op: stringwidth
     */
    public void op_stringwidth() throws PSErrorTypeCheck, PSErrorStackUnderflow,
            PSErrorRangeCheck, PSErrorUnimplemented, PSErrorUndefined, 
            PSErrorNoCurrentPoint, PSErrorInvalidAccess, IOException {
        
        PSObjectString string = opStack.pop().toPSString();
        double[] dpos = textHandler.showText(exp, string, true);
        opStack.push(new PSObjectReal(dpos[0]));
        opStack.push(new PSObjectReal(dpos[1]));
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
        opStack.push(num1.sub(num2));
    }
    
    /** PostScript op: token */
    public void op_token() throws PSErrorStackUnderflow, PSErrorTypeCheck,
            PSErrorInvalidAccess, PSErrorIOError {
        PSObject obj = opStack.pop();
        for (PSObject item : obj.token()) {
            opStack.push(item);
        }
    }
    
    /** PostScript op: transform */
    public void op_transform() throws PSErrorStackUnderflow, PSErrorTypeCheck,
            PSErrorRangeCheck, PSErrorInvalidAccess {
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
        if ( obj instanceof PSObjectArray ) {
            PSObjectMatrix matrix = obj.toMatrix();
            ty = opStack.pop().toReal();
            tx = opStack.pop().toReal();
            matrix.translate(tx, ty);
            opStack.push(matrix);
        } else {
            ty = obj.toReal();
            tx = opStack.pop().toReal();
            gstate.current.CTM.translate(tx, ty);
            gstate.current.updatePosition();
        }
    }
    
    /** PostScript op: true */
    public void op_true() {
        opStack.push(new PSObjectBool(true));
    }
    
    /** PostScript op: truncate */
    public void op_truncate() throws PSErrorStackUnderflow, PSErrorTypeCheck {
        PSObject obj = opStack.pop();
        opStack.push(obj.truncate());
    }
    
    /** PostScript op: type */
    public void op_type() throws PSErrorStackUnderflow {
        PSObject any = opStack.pop();
        opStack.push(new PSObjectName(any.type(), false));
    }
    
    /** PostScript op: undef */
    public void op_undef() throws PSErrorStackUnderflow, PSErrorTypeCheck,
            PSErrorInvalidAccess {
        PSObject key = opStack.pop();
        PSObjectDict dict = opStack.pop().toDict();
        dict.undef(key);
    }
    
    /** PostScript op: wcheck */
    public void op_wcheck() throws PSErrorStackUnderflow, PSErrorTypeCheck {
        PSObject obj = opStack.pop();
        boolean chk = obj.wcheck();
        opStack.push(new PSObjectBool(chk));
    }
    
    /** PostScript op: where */
    public void op_where() throws PSErrorStackUnderflow, PSErrorTypeCheck,
            PSErrorInvalidAccess {
        PSObject key = opStack.pop();
        PSObjectDict dict = dictStack.where(key);
        if (dict == null) {
            opStack.push(new PSObjectBool(false));
        } else {
            opStack.push(dict);
            opStack.push(new PSObjectBool(true));
        }
    }
   
    /**
     * PostScript op: xcheck
     */
    public void op_xcheck() throws PSErrorStackUnderflow {
        PSObject any = opStack.pop();
        PSObjectBool check = new PSObjectBool(any.xcheck());
        opStack.push(check);
    }
    
    /** PostScript op: xor */
    public void op_xor() throws PSErrorStackUnderflow, PSErrorTypeCheck {
        PSObject obj2 = opStack.pop();
        PSObject obj1 = opStack.pop();
        opStack.push(obj1.xor(obj2));
    }
    
}
