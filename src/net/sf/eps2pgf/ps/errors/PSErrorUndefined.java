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

package net.sf.eps2pgf.ps.errors;

/**
 *
 * @author Paul Wagenaars
 */
public class PSErrorUndefined extends PSError {
    /** Serial version UID. */
    private static final long serialVersionUID = 1L;
    
    /**
     * Create a new PSErrorUndefined exception.
     */
    public PSErrorUndefined() {
        super(PSError.UNDEFINED);
    }
    
    /**
     * Create a new PSErrorUndefined exception.
     * 
     * @param op The undefined operator.
     */
    public PSErrorUndefined(final String op) {
        super(PSError.UNDEFINED, "(operator: " + op + ")");
    }    
}
