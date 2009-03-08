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

package net.sf.eps2pgf.ps.resources.encodings;

import net.sf.eps2pgf.ProgramError;
import net.sf.eps2pgf.ps.VM;
import net.sf.eps2pgf.ps.errors.PSErrorRangeCheck;
import net.sf.eps2pgf.ps.errors.PSErrorVMError;
import net.sf.eps2pgf.ps.objects.PSObjectArray;
import net.sf.eps2pgf.ps.objects.PSObjectName;

/**
 * Defines the standard encoding vector.
 * 
 * @author Paul Wagenaars
 *
 */
public final class SymbolEncoding {
    
    /** Encoding vector. */
    private static PSObjectArray vector = null;
    
    /**
     * "Hidden" constructor.
     */
    private SymbolEncoding() {
        /* empty block */
    }
    
    /**
     * Initialize the encoding vector.
     * 
     * @param vm The VM manager.
     * 
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     * @throws PSErrorVMError Virtual memory error.
     */
    private static void initializeVector(final VM vm)
            throws ProgramError, PSErrorVMError {
        
        vector = new PSObjectArray(vm);
        try {
            vector.addToEnd(EncodingManager.SYMB_NOTDEF);     //000
            vector.addToEnd(EncodingManager.SYMB_NOTDEF);     //001
            vector.addToEnd(EncodingManager.SYMB_NOTDEF);     //002
            vector.addToEnd(EncodingManager.SYMB_NOTDEF);     //003
            vector.addToEnd(EncodingManager.SYMB_NOTDEF);     //004
            vector.addToEnd(EncodingManager.SYMB_NOTDEF);     //005
            vector.addToEnd(EncodingManager.SYMB_NOTDEF);     //006
            vector.addToEnd(EncodingManager.SYMB_NOTDEF);     //007
            vector.addToEnd(EncodingManager.SYMB_NOTDEF);     //010
            vector.addToEnd(EncodingManager.SYMB_NOTDEF);     //011
            vector.addToEnd(EncodingManager.SYMB_NOTDEF);     //012
            vector.addToEnd(EncodingManager.SYMB_NOTDEF);     //013
            vector.addToEnd(EncodingManager.SYMB_NOTDEF);     //014
            vector.addToEnd(EncodingManager.SYMB_NOTDEF);     //015
            vector.addToEnd(EncodingManager.SYMB_NOTDEF);     //016
            vector.addToEnd(EncodingManager.SYMB_NOTDEF);     //017
            vector.addToEnd(EncodingManager.SYMB_NOTDEF);     //020
            vector.addToEnd(EncodingManager.SYMB_NOTDEF);     //021
            vector.addToEnd(EncodingManager.SYMB_NOTDEF);     //022
            vector.addToEnd(EncodingManager.SYMB_NOTDEF);     //023
            vector.addToEnd(EncodingManager.SYMB_NOTDEF);     //024
            vector.addToEnd(EncodingManager.SYMB_NOTDEF);     //025
            vector.addToEnd(EncodingManager.SYMB_NOTDEF);     //026
            vector.addToEnd(EncodingManager.SYMB_NOTDEF);     //027
            vector.addToEnd(EncodingManager.SYMB_NOTDEF);     //030
            vector.addToEnd(EncodingManager.SYMB_NOTDEF);     //031
            vector.addToEnd(EncodingManager.SYMB_NOTDEF);     //032
            vector.addToEnd(EncodingManager.SYMB_NOTDEF);     //033
            vector.addToEnd(EncodingManager.SYMB_NOTDEF);     //034
            vector.addToEnd(EncodingManager.SYMB_NOTDEF);     //035
            vector.addToEnd(EncodingManager.SYMB_NOTDEF);     //036
            vector.addToEnd(EncodingManager.SYMB_NOTDEF);     //037
            vector.addToEnd(new PSObjectName("/space"));       //040
            vector.addToEnd(new PSObjectName("/exclam"));      //041
            vector.addToEnd(new PSObjectName("/universal"));   //042
            vector.addToEnd(new PSObjectName("/numbersign"));  //043
            vector.addToEnd(new PSObjectName("/existential")); //044
            vector.addToEnd(new PSObjectName("/percent"));     //045
            vector.addToEnd(new PSObjectName("/ampersand"));   //046
            vector.addToEnd(new PSObjectName("/suchthat"));    //047
            vector.addToEnd(new PSObjectName("/parenleft"));   //050
            vector.addToEnd(new PSObjectName("/parenright"));  //051
            vector.addToEnd(new PSObjectName("/asterisk"));    //052
            vector.addToEnd(new PSObjectName("/plus"));        //053
            vector.addToEnd(new PSObjectName("/comma"));       //054
            vector.addToEnd(new PSObjectName("/minus"));       //055
            vector.addToEnd(new PSObjectName("/period"));      //056
            vector.addToEnd(new PSObjectName("/slash"));       //057
            vector.addToEnd(new PSObjectName("/zero"));        //060
            vector.addToEnd(new PSObjectName("/one"));         //061
            vector.addToEnd(new PSObjectName("/two"));         //062
            vector.addToEnd(new PSObjectName("/three"));       //063
            vector.addToEnd(new PSObjectName("/four"));        //064
            vector.addToEnd(new PSObjectName("/five"));        //065
            vector.addToEnd(new PSObjectName("/six"));         //066
            vector.addToEnd(new PSObjectName("/seven"));       //067
            vector.addToEnd(new PSObjectName("/eight"));       //070
            vector.addToEnd(new PSObjectName("/nine"));        //071
            vector.addToEnd(new PSObjectName("/colon"));       //072
            vector.addToEnd(new PSObjectName("/semicolon"));   //073
            vector.addToEnd(new PSObjectName("/less"));        //074
            vector.addToEnd(new PSObjectName("/equal"));       //075
            vector.addToEnd(new PSObjectName("/greater"));     //076
            vector.addToEnd(new PSObjectName("/question"));    //077
            vector.addToEnd(new PSObjectName("/congruent"));   //100
            vector.addToEnd(new PSObjectName("/Alpha"));       //101
            vector.addToEnd(new PSObjectName("/Beta"));        //102
            vector.addToEnd(new PSObjectName("/Chi"));         //103
            vector.addToEnd(new PSObjectName("/Delta"));       //104
            vector.addToEnd(new PSObjectName("/Epsilon"));     //105
            vector.addToEnd(new PSObjectName("/Phi"));         //106
            vector.addToEnd(new PSObjectName("/Gamma"));       //107
            vector.addToEnd(new PSObjectName("/Eta"));         //110
            vector.addToEnd(new PSObjectName("/Iota"));        //111
            vector.addToEnd(new PSObjectName("/theta1"));      //112
            vector.addToEnd(new PSObjectName("/Kappa"));       //113
            vector.addToEnd(new PSObjectName("/Lambda"));      //114
            vector.addToEnd(new PSObjectName("/Mu"));          //115
            vector.addToEnd(new PSObjectName("/Nu"));          //116
            vector.addToEnd(new PSObjectName("/Omicron"));     //117
            vector.addToEnd(new PSObjectName("/Pi"));          //120
            vector.addToEnd(new PSObjectName("/Theta"));       //121
            vector.addToEnd(new PSObjectName("/Rho"));         //122
            vector.addToEnd(new PSObjectName("/Sigma"));       //123
            vector.addToEnd(new PSObjectName("/Tau"));         //124
            vector.addToEnd(new PSObjectName("/Upsilon"));     //125
            vector.addToEnd(new PSObjectName("/sigma1"));      //126
            vector.addToEnd(new PSObjectName("/Omega"));       //127
            vector.addToEnd(new PSObjectName("/Xi"));          //130
            vector.addToEnd(new PSObjectName("/Psi"));         //131
            vector.addToEnd(new PSObjectName("/Zeta"));        //132
            vector.addToEnd(new PSObjectName("/bracketleft")); //133
            vector.addToEnd(new PSObjectName("/therefore"));    //134
            vector.addToEnd(new PSObjectName("/bracketright")); //135
            vector.addToEnd(new PSObjectName("/perpendicular")); //136
            vector.addToEnd(new PSObjectName("/underscore"));  //137
            vector.addToEnd(new PSObjectName("/radicalex"));   //140
            vector.addToEnd(new PSObjectName("/alpha"));       //141
            vector.addToEnd(new PSObjectName("/beta"));        //142
            vector.addToEnd(new PSObjectName("/chi"));         //143
            vector.addToEnd(new PSObjectName("/delta"));       //144
            vector.addToEnd(new PSObjectName("/epsilon"));     //145
            vector.addToEnd(new PSObjectName("/phi"));         //146
            vector.addToEnd(new PSObjectName("/gamma"));       //147
            vector.addToEnd(new PSObjectName("/eta"));         //150
            vector.addToEnd(new PSObjectName("/iota"));        //151
            vector.addToEnd(new PSObjectName("/phi1"));        //152
            vector.addToEnd(new PSObjectName("/kappa"));       //153
            vector.addToEnd(new PSObjectName("/lambda"));      //154
            vector.addToEnd(new PSObjectName("/mu"));          //155
            vector.addToEnd(new PSObjectName("/nu"));          //156
            vector.addToEnd(new PSObjectName("/omicron"));     //157
            vector.addToEnd(new PSObjectName("/pi"));          //160
            vector.addToEnd(new PSObjectName("/theta"));       //161
            vector.addToEnd(new PSObjectName("/rho"));         //162
            vector.addToEnd(new PSObjectName("/sigma"));       //163
            vector.addToEnd(new PSObjectName("/tau"));         //164
            vector.addToEnd(new PSObjectName("/upsilon"));     //165
            vector.addToEnd(new PSObjectName("/omega1"));      //166
            vector.addToEnd(new PSObjectName("/omega"));       //167
            vector.addToEnd(new PSObjectName("/xi"));          //170
            vector.addToEnd(new PSObjectName("/psi"));         //171
            vector.addToEnd(new PSObjectName("/zeta"));        //172
            vector.addToEnd(new PSObjectName("/braceleft"));   //173
            vector.addToEnd(new PSObjectName("/bar"));         //174
            vector.addToEnd(new PSObjectName("/braceright"));  //175
            vector.addToEnd(new PSObjectName("/similar"));     //176
            vector.addToEnd(EncodingManager.SYMB_NOTDEF);     //177
            vector.addToEnd(EncodingManager.SYMB_NOTDEF);     //200
            vector.addToEnd(EncodingManager.SYMB_NOTDEF);     //201
            vector.addToEnd(EncodingManager.SYMB_NOTDEF);     //202
            vector.addToEnd(EncodingManager.SYMB_NOTDEF);     //203
            vector.addToEnd(EncodingManager.SYMB_NOTDEF);     //204
            vector.addToEnd(EncodingManager.SYMB_NOTDEF);     //205
            vector.addToEnd(EncodingManager.SYMB_NOTDEF);     //206
            vector.addToEnd(EncodingManager.SYMB_NOTDEF);     //207
            vector.addToEnd(EncodingManager.SYMB_NOTDEF);     //210
            vector.addToEnd(EncodingManager.SYMB_NOTDEF);     //211
            vector.addToEnd(EncodingManager.SYMB_NOTDEF);     //212
            vector.addToEnd(EncodingManager.SYMB_NOTDEF);     //213
            vector.addToEnd(EncodingManager.SYMB_NOTDEF);     //214
            vector.addToEnd(EncodingManager.SYMB_NOTDEF);     //215
            vector.addToEnd(EncodingManager.SYMB_NOTDEF);     //216
            vector.addToEnd(EncodingManager.SYMB_NOTDEF);     //217
            vector.addToEnd(EncodingManager.SYMB_NOTDEF);     //220
            vector.addToEnd(EncodingManager.SYMB_NOTDEF);     //221
            vector.addToEnd(EncodingManager.SYMB_NOTDEF);     //222
            vector.addToEnd(EncodingManager.SYMB_NOTDEF);     //223
            vector.addToEnd(EncodingManager.SYMB_NOTDEF);     //224
            vector.addToEnd(EncodingManager.SYMB_NOTDEF);     //225
            vector.addToEnd(EncodingManager.SYMB_NOTDEF);     //226
            vector.addToEnd(EncodingManager.SYMB_NOTDEF);     //227
            vector.addToEnd(EncodingManager.SYMB_NOTDEF);     //230
            vector.addToEnd(EncodingManager.SYMB_NOTDEF);     //231
            vector.addToEnd(EncodingManager.SYMB_NOTDEF);     //232
            vector.addToEnd(EncodingManager.SYMB_NOTDEF);     //233
            vector.addToEnd(EncodingManager.SYMB_NOTDEF);     //234
            vector.addToEnd(EncodingManager.SYMB_NOTDEF);     //235
            vector.addToEnd(EncodingManager.SYMB_NOTDEF);     //236
            vector.addToEnd(EncodingManager.SYMB_NOTDEF);     //237
            vector.addToEnd(new PSObjectName("/Euro"));        //240
            vector.addToEnd(new PSObjectName("/Upsilon1"));    //241
            vector.addToEnd(new PSObjectName("/minute"));      //242
            vector.addToEnd(new PSObjectName("/lessequal"));   //243
            vector.addToEnd(new PSObjectName("/fraction"));    //244
            vector.addToEnd(new PSObjectName("/infinity"));    //245
            vector.addToEnd(new PSObjectName("/florin"));      //246
            vector.addToEnd(new PSObjectName("/club"));        //247
            vector.addToEnd(new PSObjectName("/diamond"));     //250
            vector.addToEnd(new PSObjectName("/heart"));       //251
            vector.addToEnd(new PSObjectName("/spade"));       //252
            vector.addToEnd(new PSObjectName("/arrowboth"));   //253
            vector.addToEnd(new PSObjectName("/arrowleft"));   //254
            vector.addToEnd(new PSObjectName("/arrowup"));     //255
            vector.addToEnd(new PSObjectName("/arrowright"));  //256
            vector.addToEnd(new PSObjectName("/arrowdown"));   //257
            vector.addToEnd(new PSObjectName("/degree"));      //260
            vector.addToEnd(new PSObjectName("/plusminus"));   //261
            vector.addToEnd(new PSObjectName("/second"));       //262
            vector.addToEnd(new PSObjectName("/greaterequal")); //263
            vector.addToEnd(new PSObjectName("/multiply"));     //264
            vector.addToEnd(new PSObjectName("/proportional")); //265
            vector.addToEnd(new PSObjectName("/partialdiff"));  //266
            vector.addToEnd(new PSObjectName("/bullet"));       //267
            vector.addToEnd(new PSObjectName("/divide"));       //270
            vector.addToEnd(new PSObjectName("/notequal"));     //271
            vector.addToEnd(new PSObjectName("/equivalence"));  //272
            vector.addToEnd(new PSObjectName("/approxequal"));  //273
            vector.addToEnd(new PSObjectName("/ellipsis"));     //274
            vector.addToEnd(new PSObjectName("/arrowvertex"));  //275        
            vector.addToEnd(new PSObjectName("/arrowhorizex")); //276
            vector.addToEnd(new PSObjectName("/carriagereturn")); //277
            vector.addToEnd(new PSObjectName("/aleph"));        //300
            vector.addToEnd(new PSObjectName("/Ifraktur"));     //301
            vector.addToEnd(new PSObjectName("/Rfraktur"));     //302
            vector.addToEnd(new PSObjectName("/weierstrass"));  //303
            vector.addToEnd(new PSObjectName("/circlemultiply")); //304
            vector.addToEnd(new PSObjectName("/circleplus"));   //305
            vector.addToEnd(new PSObjectName("/emptyset"));     //306
            vector.addToEnd(new PSObjectName("/intersection")); //307
            vector.addToEnd(new PSObjectName("/union"));        //310
            vector.addToEnd(new PSObjectName("/propersuperset")); //311
            vector.addToEnd(new PSObjectName("/reflexsuperset")); //312
            vector.addToEnd(new PSObjectName("/notsubset"));    //313
            vector.addToEnd(new PSObjectName("/propersubset")); //314
            vector.addToEnd(new PSObjectName("/reflexsubset")); //315
            vector.addToEnd(new PSObjectName("/element"));      //316
            vector.addToEnd(new PSObjectName("/notelement"));   //317
            vector.addToEnd(new PSObjectName("/angle"));        //320
            vector.addToEnd(new PSObjectName("/gradient"));     //321
            vector.addToEnd(new PSObjectName("/registerserif")); //322
            vector.addToEnd(new PSObjectName("/copyrightserif")); //323
            vector.addToEnd(new PSObjectName("/trademarkserif")); //324
            vector.addToEnd(new PSObjectName("/product"));       //325
            vector.addToEnd(new PSObjectName("/radical"));       //326
            vector.addToEnd(new PSObjectName("/dotmath"));       //327
            vector.addToEnd(new PSObjectName("/logicalnot"));    //330
            vector.addToEnd(new PSObjectName("/logicaland"));    //331
            vector.addToEnd(new PSObjectName("/logicalor"));     //332
            vector.addToEnd(new PSObjectName("/arrowdblboth"));  //333
            vector.addToEnd(new PSObjectName("/arrowdblleft"));  //334
            vector.addToEnd(new PSObjectName("/arrowdblup"));    //335
            vector.addToEnd(new PSObjectName("/arrowdblright")); //336
            vector.addToEnd(new PSObjectName("/arrowdbldown"));  //337
            vector.addToEnd(new PSObjectName("/lozenge"));       //340
            vector.addToEnd(new PSObjectName("/angleleft"));     //341
            vector.addToEnd(new PSObjectName("/registersans"));  //342
            vector.addToEnd(new PSObjectName("/copyrightsans")); //343
            vector.addToEnd(new PSObjectName("/trademarksans")); //344
            vector.addToEnd(new PSObjectName("/summation"));     //345
            vector.addToEnd(new PSObjectName("/parenlefttp"));   //346
            vector.addToEnd(new PSObjectName("/parenleftex"));   //347
            vector.addToEnd(new PSObjectName("/parenleftbt"));   //350
            vector.addToEnd(new PSObjectName("/bracketlefttp")); //351
            vector.addToEnd(new PSObjectName("/bracketleftex")); //352
            vector.addToEnd(new PSObjectName("/bracketleftbt")); //353
            vector.addToEnd(new PSObjectName("/bracelefttp"));   //354
            vector.addToEnd(new PSObjectName("/braceleftmid"));  //355
            vector.addToEnd(new PSObjectName("/braceleftbt"));   //356
            vector.addToEnd(new PSObjectName("/braceex"));       //357
            vector.addToEnd(EncodingManager.SYMB_NOTDEF);       //360
            vector.addToEnd(new PSObjectName("/angleright"));    //361
            vector.addToEnd(new PSObjectName("/integral"));      //362
            vector.addToEnd(new PSObjectName("/integraltp"));    //363
            vector.addToEnd(new PSObjectName("/integralex"));    //364
            vector.addToEnd(new PSObjectName("/integralbt"));    //365
            vector.addToEnd(new PSObjectName("/parenrighttp"));  //366
            vector.addToEnd(new PSObjectName("/parenrightex"));  //367
            vector.addToEnd(new PSObjectName("/parenrightbt"));  //370
            vector.addToEnd(new PSObjectName("/bracketrighttp")); //371
            vector.addToEnd(new PSObjectName("/bracketrightex")); //372
            vector.addToEnd(new PSObjectName("/bracketrightbt")); //373
            vector.addToEnd(new PSObjectName("/bracerighttp"));  //374
            vector.addToEnd(new PSObjectName("/bracerightmid")); //375
            vector.addToEnd(new PSObjectName("/bracerightbt"));  //376
            vector.addToEnd(EncodingManager.SYMB_NOTDEF);       //377
        } catch (PSErrorRangeCheck e) {
            throw new ProgramError("A range check error occurred where it"
                    + " shouldn't.");
        }
    }
    
    /**
     * Gets the encoding vector.
     * 
     * @param vm The virtual memory manager.
     * 
     * @return The vector.
     * 
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     * @throws PSErrorVMError Virtual memory error.
     */
    public static PSObjectArray get(final VM vm)
            throws ProgramError, PSErrorVMError {
        
        if (vector == null) {
            initializeVector(vm);
        }
        
        return vector;
    }
    
}
