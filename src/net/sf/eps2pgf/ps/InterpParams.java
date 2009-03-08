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

import java.io.File;
import java.util.List;

import net.sf.eps2pgf.ProgramError;
import net.sf.eps2pgf.ps.errors.PSErrorTypeCheck;
import net.sf.eps2pgf.ps.errors.PSErrorUndefined;
import net.sf.eps2pgf.ps.errors.PSErrorVMError;
import net.sf.eps2pgf.ps.objects.PSObject;
import net.sf.eps2pgf.ps.objects.PSObjectDict;
import net.sf.eps2pgf.ps.resources.Utils;

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
     * 
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     * @throws PSErrorVMError Virtual memory error.
     * @throws PSErrorTypeCheck A PostScript typecheck error occurred.
     */
    public PSObjectDict currentUserParams()
            throws PSErrorVMError, PSErrorTypeCheck, ProgramError {
        
        PSObjectDict newDict = new PSObjectDict(interp.getVm());
        PSObjectDict userParamsDict = getUserParamsDict();
        newDict.copy(userParamsDict);
        
        return newDict;
    }
    
    /**
     * Sets new user parameters.
     * 
     * @param newParams The dictionary with new user parameters.
     * 
     * @throws PSErrorVMError Virtual memory error.
     */
    public void setUserParams(final PSObjectDict newParams)
            throws PSErrorVMError {
        
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
     * Returns a dictionary containing the keys and current values of all system
     * parameters.
     * 
     * @return A copy of the dictionary with system parameters.
     * 
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     * @throws PSErrorVMError Virtual memory error.
     * @throws PSErrorTypeCheck A PostScript typecheck error occurred.
     */
    public PSObjectDict currentSystemParams()
            throws ProgramError, PSErrorVMError, PSErrorTypeCheck {
        
        PSObjectDict newDict = new PSObjectDict(interp.getVm());
        PSObjectDict systemParamsDict = getSystemParamsDict();
        newDict.copy(systemParamsDict);
        
        return newDict;
    }
    
    /**
     * Sets new system parameters.
     * 
     * @param newParams The dictionary with new system parameters.
     * 
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     * @throws PSErrorVMError Virtual memory error.
     */
    public void setSystemParams(final PSObjectDict newParams)
            throws ProgramError, PSErrorVMError {
        
        List<PSObject> items = newParams.getItemList();
        PSObjectDict sysParams = getSystemParamsDict();
        for (int i = 1; i < items.size(); i += 2) {
            PSObject key = items.get(i);
            if (sysParams.known(key)) {
                PSObject value = items.get(i + 1);
                sysParams.setKey(key, value);
            }
        }
    }
    
    /**
     * Returns a dictionary containing the keys and current values of parameters
     * of the specified device.
     * 
     * @param device The device for which the parameters are requested.
     * 
     * @return A copy of the dictionary with system parameters.
     * 
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     * @throws PSErrorTypeCheck A PostScript typecheck error occurred.
     * @throws PSErrorVMError Virtual memory error.
     */
    public PSObjectDict currentDeviceParams(final String device)
            throws ProgramError, PSErrorVMError, PSErrorTypeCheck {
        
        PSObjectDict newDict = new PSObjectDict(interp.getVm());
        PSObjectDict deviceParamsDict = getDeviceParamsDict(device);
        newDict.copy(deviceParamsDict);
        
        return newDict;
    }
    
    /**
     * Sets new device parameters.
     * 
     * @param device Name of the device for which the parameters must be
     * changed.
     * @param newParams The dictionary with new system parameters.
     * 
     * @throws PSErrorVMError Virtual memory error.
     */
    public void setDeviceParams(final String device,
            final PSObjectDict newParams) throws PSErrorVMError {
        
        List<PSObject> items = newParams.getItemList();
        PSObjectDict devParams = getDeviceParamsDict(device);
        for (int i = 1; i < items.size(); i += 2) {
            PSObject key = items.get(i);
            PSObject value = items.get(i + 1);
            devParams.setKey(key, value);
        }
    }
    
    /**
     * Gets the user parameters dictionary.
     * 
     * @return The user parameters dictionary.
     * 
     * @throws PSErrorVMError Virtual memory error.
     */
    private PSObjectDict getUserParamsDict() throws PSErrorVMError {
        if (userParams == null) {
            userParams = createDefaultUserParams();
        }
        return userParams;
    }
    
    /**
     * Create a new dictionary with all user parameters with default values.
     * 
     * @return A new dictionary with user parameters with default values.
     * 
     * @throws PSErrorVMError Virtual memory error.
     */
    private PSObjectDict createDefaultUserParams() throws PSErrorVMError {
        PSObjectDict dict = new PSObjectDict(interp.getVm());
        
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

    /**
     * Gets the system parameters dictionary.
     * 
     * @return The system parameters dictionary.
     * 
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     * @throws PSErrorVMError Virtual memory error.
     */
    private PSObjectDict getSystemParamsDict()
            throws ProgramError, PSErrorVMError {
        
        if (systemParams == null) {
            systemParams = createDefaultSystemParams();
        }
        return systemParams;
    }
    
    
    /**
     * Create a new dictionary with all system parameters with default values.
     * 
     * @return A new dictionary with system parameters with default values.
     * 
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     * @throws PSErrorVMError Virtual memory error.
     */
    private PSObjectDict createDefaultSystemParams()
            throws ProgramError, PSErrorVMError {
        
        PSObjectDict dict = new PSObjectDict(interp.getVm());
        dict.setKey("ByteOrder", false);
        dict.setKey("BuildTime", 0);
        dict.setKey("CurDisplayList", 0);
        dict.setKey("CurFontCache", 0);
        dict.setKey("CurFormCache", 0);
        dict.setKey("CurOutlineCache", 0);
        dict.setKey("CurPatternCache", 0);
        dict.setKey("CurScreenStorage", 0);
        dict.setKey("CurSourceList", 0);
        dict.setKey("CurStoredScreenCache", 0);
        dict.setKey("CurUPathCache", 0);
        dict.setKey("FactoryDefaults", false);
        dict.setKey("FontResourceDir", Utils.getResourceDir().toString());
        dict.setKey("GenericResourceDir", Utils.getResourceDir().toString());
        dict.setKey("GenericResourcePathSep",
                System.getProperty("file.separator"));
        dict.setKey("LicenseID", "");
        dict.setKey("MaxDisplayAndSourceList", Integer.MAX_VALUE);
        dict.setKey("MaxDisplayList", Integer.MAX_VALUE);
        dict.setKey("MaxFontCache", Integer.MAX_VALUE);
        dict.setKey("MaxFormCache", Integer.MAX_VALUE);
        dict.setKey("MaxImageBuffer", Integer.MAX_VALUE);
        dict.setKey("MaxOutlineCache", Integer.MAX_VALUE);
        dict.setKey("MaxPatternCache", Integer.MAX_VALUE);
        dict.setKey("MaxScreenStorage", Integer.MAX_VALUE);
        dict.setKey("MaxSourceList", Integer.MAX_VALUE);
        dict.setKey("MaxStoredScreenCache", Integer.MAX_VALUE);
        dict.setKey("MaxUPathCache", Integer.MAX_VALUE);
        dict.setKey("PageCount", 0);
        dict.setKey("PrinterName",
                interp.getDictStack().lookup("product").toString());
        dict.setKey("RealFormat", "IEEE");
        try {
            dict.setKey("Revision",
                    interp.getDictStack().lookup("revision").toInt());
        } catch (PSErrorTypeCheck e) {
            dict.setKey("Revision", 0);
        }
        dict.setKey("StartJobPassword", "");
        dict.setKey("StartupMode", 0);
        dict.setKey("SystemParamsPassword", "");
        
        return dict;
    }
    
    /**
     * Gets the parameters dictionary for certain device.
     * 
     * @param device The name of the device.
     * 
     * @return Dictionary with parameters for the specified device.
     * 
     * @throws PSErrorVMError Virtual memory error.
     */
    private PSObjectDict getDeviceParamsDict(final String device)
            throws PSErrorVMError {
        
        if (deviceParams == null) {
            deviceParams = new PSObjectDict(interp.getVm());
        }
        if (!deviceParams.known(device)) {
            deviceParams.setKey(device, new PSObjectDict(interp.getVm()));
        }
        
        try {
            return deviceParams.get(device).toDict();
        } catch (PSErrorTypeCheck e) {
            // this can never happen
        } catch (PSErrorUndefined e) {
            // this can never happen
        }
        return null;
    }

}
