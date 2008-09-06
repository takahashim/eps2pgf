/*
 * InterpParams.java
 *
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

package net.sf.eps2pgf.ps;

import java.io.File;
import java.util.List;

import net.sf.eps2pgf.ps.objects.PSObject;
import net.sf.eps2pgf.ps.objects.PSObjectDict;

/**
 * This class manages interpreter parameters. See appendix C of the PostScript
 * reference for more information.
 * 
 * @author Paul Wagenaars
 *
 */
public class InterpParams {
    
    /** User parameters. */
    private PSObjectDict userParams = null;
    
    /** System parameters. */
    private PSObjectDict systemParams = null;
    
    /** Device parameters. */
    private PSObjectDict deviceParams = null;
    
    /** Interpreter with which these parameters are associated. */
    private Interpreter interp;
    
    /**
     * Create a new instance of this class.
     * 
     * @param interpreter The interpreter with which these parameters are
     * associated.
     */
    public InterpParams(final Interpreter interpreter) {
        interp = interpreter;
    }
    
    /**
     * Returns a dictionary containing the keys and current values of all user
     * parameters.
     * 
     * @return A copy of the dictionary with user parameters.
     */
    public PSObjectDict currentUserParams() {
        return getUserParamsDict().clone();
    }
    
    /**
     * Sets new user parameters.
     * 
     * @param newParams The dictionary with new user parameters.
     */
    public void setUserParams(final PSObjectDict newParams) {
        List<PSObject> items = newParams.getItemList();
        PSObjectDict usrParams = getUserParamsDict();
        for (int i = 1; i < items.size(); i += 2) {
            PSObject key = items.get(i);
            if (usrParams.known(key)) {
                PSObject value = items.get(i + 1);
                usrParams.setKey(key, value);
            }
        }
    }
    
    /**
     * Gets the user parameters dictionary.
     * 
     * @return The user parameters dictionary.
     */
    private PSObjectDict getUserParamsDict() {
        if (userParams == null) {
            userParams = createDefaultUserParams();
        }
        return userParams;
    }
    
    /**
     * Create a new dictionary with all user parameters with default values.
     * 
     * @return A new dictionary with user parameters with default values.
     */
    private PSObjectDict createDefaultUserParams() {
        PSObjectDict dict = new PSObjectDict();
        
        dict.setKey("AccurateScreens", false);
        dict.setKey("HalftoneMode", 0);
        dict.setKey("IdiomRecognition", false);
        File inputFile = interp.getOptions().getInputFile();
        String jobname;
        if (inputFile != null) {
            jobname = inputFile.getName();
            if (jobname.length() > 100) {
                jobname = jobname.substring(0, 100);
            }
        } else {
            jobname = "Unnamed job";
        }
        dict.setKey("JobName", jobname);
        dict.setKey("MaxDictStack", Integer.MAX_VALUE);
        dict.setKey("MaxExecStack", Integer.MAX_VALUE);
        dict.setKey("MaxFontItem", Integer.MAX_VALUE);
        dict.setKey("MaxFormItem", Integer.MAX_VALUE);
        dict.setKey("MaxLocalVM", Integer.MAX_VALUE);
        dict.setKey("MaxOpStack", Integer.MAX_VALUE);
        dict.setKey("MaxPatternItem", Integer.MAX_VALUE);
        dict.setKey("MaxScreenItem", Integer.MAX_VALUE);
        dict.setKey("MaxSuperScreen", Integer.MAX_VALUE);
        dict.setKey("MaxUPathItem", Integer.MAX_VALUE);
        dict.setKey("MinFontCompress", Integer.MAX_VALUE);
        dict.setKey("VMReclaim", 0);
        dict.setKey("VMThreshold", 1000000);
        
        return dict;
    }

}
