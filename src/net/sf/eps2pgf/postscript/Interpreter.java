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
import net.sf.eps2pgf.collections.ArrayStack;
import net.sf.eps2pgf.output.*;
import net.sf.eps2pgf.postscript.errors.*;

/**
 *
 * @author Paul Wagenaars
 */
public class Interpreter {
    // Operand stack (see PostScript manual for more info)
    ArrayStack<PSObject> opStack = new ArrayStack<PSObject>();
    
    // Execution stack
    ExecStack execStack = new ExecStack();
    
    // Dictionary stack
    DictStack dictStack = new DictStack(this);
    
    // Graphics state
    GstateStack gstate = new GstateStack();
    
    // Exporter, writes the graphics data to file in another format (such as pgf)
    Exporter exp;
    
    /** Creates a new instance of Interpreter */
    public Interpreter(LinkedList<PSObject> in, Writer out) {
        execStack.addObjectList(in);
        
        // Initialize character encodings
        Encoding.initialize();
        
        // Create new exporter
        exp = new PGFExport(out);
    }
    
    public void start() throws Exception {
        exp.init();
        try {
            while (!execStack.empty()) {
                processObject(execStack.getNext());
            }
        } catch (PSError e) {
            System.out.println("----- Start of stack");
            op_pstack();
            System.out.println("----- End of stack");
            dictStack.dumpFull();
            exp.finish();
            throw e;
        }
        System.out.println("----- Start of stack");
        op_pstack();
        System.out.println("----- End of stack");
        dictStack.dumpFull();
        exp.finish();
    }
    
    public void processObject(PSObject obj) throws Exception {
        //System.out.println("-=- " + obj.isis());
        if (obj.isLiteral) {
            opStack.push(obj);
        } else {
            obj.execute(this);
        }
    }
    
    
    /** PostScript op: add */
    public void op_add() throws PSError {
        PSObject num2 = opStack.pop();
        PSObject num1 = opStack.pop();
        if ( (num1 instanceof PSObjectInt) && (num2 instanceof PSObjectInt) ) {
            opStack.push(new PSObjectInt( num1.toInt() + num2.toInt() ));
        } else {
            opStack.push(new PSObjectReal( num1.toReal() + num2.toReal() ));
        }
    }
    
    /** PostScript op: arc */
    public void op_arc() throws PSError {
        throw new PSErrorUnimplemented("operator: arc");
    }
    
    /** PostScript op: arcn */
    public void op_arcn() throws PSError {
        throw new PSErrorUnimplemented("operator: arcn");
    }
    
    /** PostScript op: array */
    public void op_array() throws PSError {
        int n = opStack.pop().toNonNegInt();
        op_sqBrackLeft();
        PSObjectNull nullObj = new PSObjectNull();
        for (int i = 0 ; i < n ; i++) {
            opStack.push(nullObj);
        }
        op_sqBrackRight();
    }
    
    /** PostScript op: astore */
    public void op_astore() throws PSError {
        PSObject obj = opStack.pop();
        if (!(obj instanceof PSObjectArray)) {
            throw new PSErrorTypeCheck();
        }
        PSObjectArray array = (PSObjectArray)obj;
        int n = array.size();
        for (int i = (n-1) ; i >= 0 ; i--) {
            array.set(i, opStack.pop());
        }
        opStack.push(array);
    }
    
    /** PostScript op: begin */
    public void op_begin() throws PSError {
        PSObject dict = opStack.pop();
        if (!(dict instanceof PSObjectDict)) {
            throw new PSErrorTypeCheck();
        }
        dictStack.pushDict((PSObjectDict)dict);
    }
    
    /** PostScript op: bind */
    public void op_bind() throws PSError {
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
    
    /** PostScript op: cleartomark */
    public void op_cleartomark() throws PSError {
        for (int i = 0 ; i < opStack.size() ; i++) {
            if (opStack.pop() instanceof PSObjectMark) {
                return;
            }
        }
        throw new PSErrorUnmatchedMark();
    }
    
    /** PostScript op: clip */
    public void op_clip() throws PSError, IOException {
        gstate.current.clip();
        exp.clip(gstate.current.clippingPath);
    }
    
    /** PostScript op: closepath */
    public void op_closepath() throws PSError {
        double[] startPos = gstate.current.path.closepath();
        if (startPos != null) {
            gstate.current.moveto(startPos[0], startPos[1]);
        }
    }
    
    /** PostScript op: concat */
    public void op_concat() throws PSError {
        throw new PSErrorUnimplemented("operator: concat");
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
    public void op_counttomark() throws PSError {
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
        throw new PSErrorUnimplemented("operator: currentmatrix");
    }
    
    /** PostScript op: def */
    public void op_def() throws PSError {
        PSObject value = opStack.pop();
        PSObject key = opStack.pop();
        dictStack.def(key, value);
    }
    
    /** PostScript op: definefont */
    public void op_definefont() throws PSError {
        throw new PSErrorUnimplemented("operator: definefont");
    }
    
    /** PostScript op: dict */
    public void op_dict() throws PSError {
        PSObject capacity = opStack.pop();
        if ( !(capacity instanceof PSObjectReal) && 
                !(capacity instanceof PSObjectInt) ) {
            throw new PSErrorTypeCheck();
        }
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
        exp.fill(gstate.current.path);
        op_newpath();
    }
    
    /** PostScript op: eq */
    public void op_eq() throws PSError {
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
    public void op_findfont() throws PSError {
        throw new PSErrorUnimplemented("operator: findfont");
    }
    
    /** PostScript op: forall */
    public void op_forall() throws PSError {
        throw new PSErrorUnimplemented("operator: forall");
    }
    
    /** PostScript op: get */
    public void op_get() throws PSError {
        throw new PSErrorUnimplemented("operator: get");
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
        PSObject proc = opStack.pop();
        if (!(proc instanceof PSObjectProc)) {
            throw new PSErrorTypeCheck();
        }
        boolean bool = opStack.pop().toBool();
        if (bool) {
            proc.execute(this);
        }
    }
    
    /** PostScript op: ifelse */
    public void op_ifelse() throws Exception {
        PSObject obj2 = opStack.pop();
        PSObject obj1 = opStack.pop();
        boolean bool = opStack.pop().toBool();
        PSObjectProc proc1;
        PSObjectProc proc2;
        if (obj1 instanceof PSObjectProc) {
            proc1 = (PSObjectProc)obj1;
        } else {
            throw new PSErrorTypeCheck();
        }
        if (obj2 instanceof PSObjectProc) {
            proc2 = (PSObjectProc)obj2;
        } else {
            throw new PSErrorTypeCheck();
        }
        
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
    
    /** PostScript op: known */
    public void op_known() throws PSError {
        throw new PSErrorUnimplemented("operator: known");
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
        throw new PSErrorUnimplemented("operator: length");
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
    public void op_makefont() throws PSError {
        throw new PSErrorUnimplemented("operator: makefont");
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
        throw new PSErrorUnimplemented("operator: ne");
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
    
    /** PostScript op: pstack */
    public void op_pstack() throws PSError {
        for (int i = opStack.size()-1 ; i >= 0 ; i--) {
            System.out.println(opStack.get(i).isis());
        }        
    }
    
    /** PostScript op: readhexstring */
    public void op_readhexstring() throws PSError {
        throw new PSErrorUnimplemented("operator: readhexstring");
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
        PSObject obj = opStack.pop();
        if (!(obj instanceof PSObjectProc)) {
            throw new PSErrorTypeCheck();
        }
        int n = opStack.pop().toNonNegInt();
        
        for (int i = 0 ; i < n ; i++) {
            obj.execute(this);
        }
    }    
    
    /** PostScript op: restore */
    public void op_restore() throws PSError {
        throw new PSErrorUnimplemented("operator: restore");
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
        if (obj instanceof PSObjectArray) {
            PSObjectArray matrix = (PSObjectArray)obj;
            angle = opStack.pop().toReal();
            throw new PSErrorUnimplemented("scale operator not yet fully implemented.");
        } else {
            angle = obj.toReal();
            gstate.current.rotate(angle);
        }
    }
   
    /** PostScript op: save */
    public void op_save() throws PSError {
        opStack.push(new PSObjectName("/-save- (dummy)"));
        System.out.println("WARNING: save operator ignored. This might have an effect on the result.");
    }
   
    /** PostScript op: scale */
    public void op_scale() throws PSError {
        PSObject obj = opStack.pop();
        double sx, sy;
        if (obj instanceof PSObjectArray) {
            PSObjectArray matrix = (PSObjectArray)obj;
            sy = opStack.pop().toReal();
            sx = opStack.pop().toReal();
            throw new PSErrorUnimplemented("scale operator not yet fully implemented.");
        } else {
            sy = obj.toReal();
            sx = opStack.pop().toReal();
            gstate.current.scale(sx, sy);
        }
    }
   
    /** PostScript op: setcmykcolor */
    public void op_setcmykcolor() throws PSError {
        throw new PSErrorUnimplemented("operator: setcmykcolor");
    }
   
    /** PostScript op: setdash */
    public void op_setdash() throws PSError, IOException {
        double offset = opStack.pop().toReal();
        PSObject obj = opStack.pop();
        if (!(obj instanceof PSObjectArray)) {
            throw new PSErrorTypeCheck();
        }
        PSObjectArray array = (PSObjectArray)obj;
        
        // Scale all distances
        double scaling = gstate.current.getMeanScaling();
        offset *= scaling;
        for (int i = 0 ; i < array.size() ; i++) {
            double value = array.get(i).toReal();
            array.set(i, new PSObjectReal(value*scaling));
        }
        
        exp.setDash(array, offset);
    }
   
    /** PostScript op: setfont */
    public void op_setfont() throws PSError {
        throw new PSErrorUnimplemented("operator: setfont");
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
        
        // Apply CTM to linewidth, now the line width is in cm
        lineWidth *= gstate.current.getMeanScaling();
        
        exp.setlinewidth(lineWidth);
    }
   
    /** PostScript op: setmatrix */
    public void op_setmatrix() throws PSError {
        throw new PSErrorUnimplemented("operator: setmatrix");
    }
   
    /** PostScript op: show */
    public void op_show() throws PSError {
        throw new PSErrorUnimplemented("operator: show");
    }
   
    /** PostScript op: StandardEncoding */
    public void op_StandardEncoding() {
        PSObjectName[] encodingVector = Encoding.getStandardVector();
        opStack.push(new PSObjectArray(encodingVector));
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
    
    /** PostScript op: [ */
    public void op_sqBrackRight() throws PSError {
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
    
    /** PostScript op: translate */
    public void op_translate() throws PSError {
        PSObject obj = opStack.pop();
        double tx, ty;
        if (obj instanceof PSObjectArray) {
            PSObjectArray matrix = (PSObjectArray)obj;
            ty = opStack.pop().toReal();
            tx = opStack.pop().toReal();
            throw new PSErrorUnimplemented("translate operator not yet fully implemented.");
        } else {
            ty = obj.toReal();
            tx = opStack.pop().toReal();
            gstate.current.translate(tx, ty);
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
