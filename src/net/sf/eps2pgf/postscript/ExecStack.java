/*
 * ExecStack.java
 *
 * This file is part of Eps2pgf.
 *
 * Copyright 2007 Paul Wagenaars <pwagenaars@fastmail.fm>
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

import java.util.*;

/**
 *
 * @author Paul Wagenaars
 */
public class ExecStack {
    // Execution stack (see PostScript manual for more info)
    Stack<LinkedList<PSObject>> execStack = new Stack<LinkedList<PSObject>>();

    /** Add a list with PSObjects to the execution stack */
    public void addObjectList(LinkedList<PSObject> newList) {
        execStack.push(newList);
    }
    
    /** Copy and add a list with PSObjects to the execution stack */
    public void copyAndAddObjectList(LinkedList<PSObject> oldList) {
        LinkedList<PSObject> newList = new LinkedList<PSObject>();
        for(int i = 0 ; i < oldList.size() ; i++) {
            newList.add(oldList.get(i));
        }
        addObjectList(newList);
    }
    
    /** Checks whether the execution stack is empty */
    public boolean empty() {
        return execStack.empty();
    }
    
    /** Retrieves the next object */
    public PSObject getNext() {
        LinkedList<PSObject> currentExecList = execStack.peek();
        PSObject obj = currentExecList.remove();
        if (currentExecList.isEmpty()) {
            execStack.pop();
        }
        return obj;
    }
}
