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

package net.sf.eps2pgf.util;

import net.sf.eps2pgf.ProgramError;

/**
 * Similar to Cloneable, except that it defines clone methods which takes
 * CloneMappings as argument.
 * 
 * @author Paul Wagenaars
 *
 */
public interface MapCloneable {
    
    /**
     * Create a deep copy of this object. Using the clone map references between
     * objects are maintained.
     * 
     * @param cloneMap The clone mappings.
     * 
     * @return Deep copy of this object.
     * 
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    Object clone(final CloneMappings cloneMap) throws ProgramError;
}
