/*
 * This file is part of Eps2pgf.
 *
 * Copyright 2007-2008 Paul Wagenaars <paul@wagenaars.org>
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

import net.sf.eps2pgf.ps.objects.PSObjectName;

/**
 * Superclass for internal signaling of the program. These are not standard
 * PostScript errors.
 * 
 * @author Paul Wagenaars
 *
 */
public class InternalSignal extends PSError {

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new internal error.
     * 
     * @param errorType The error type.
     */
    protected InternalSignal(final PSObjectName errorType) {
        super(errorType);
    }

    /**
     * Instantiates a new internal error.
     * 
     * @param errorType The error type.
     * @param message A custom error message.
     */
    protected InternalSignal(final PSObjectName errorType,
            final String message) {
        
        super(errorType, message);
    }

}
