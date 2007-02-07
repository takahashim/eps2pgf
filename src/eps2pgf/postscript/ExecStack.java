/*
 * ExecStack.java
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

package eps2pgf.postscript;

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
