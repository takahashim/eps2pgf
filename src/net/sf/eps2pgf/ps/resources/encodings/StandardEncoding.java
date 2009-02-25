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
public final class StandardEncoding {
    
    /** Encoding vector. */
    private static PSObjectArray vector = null;
    
    /**
     * "Hidden" constructor.
     */
    private StandardEncoding() {
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
            vector.addToEnd(new PSObjectName("/quotedbl"));    //042
            vector.addToEnd(new PSObjectName("/numbersign"));  //043
            vector.addToEnd(new PSObjectName("/dollar"));      //044
            vector.addToEnd(new PSObjectName("/percent"));     //045
            vector.addToEnd(new PSObjectName("/ampersand"));   //046
            vector.addToEnd(new PSObjectName("/quoteright"));  //047
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
            vector.addToEnd(new PSObjectName("/at"));          //100
            vector.addToEnd(new PSObjectName("/A"));           //101
            vector.addToEnd(new PSObjectName("/B"));           //102
            vector.addToEnd(new PSObjectName("/C"));           //103
            vector.addToEnd(new PSObjectName("/D"));           //104
            vector.addToEnd(new PSObjectName("/E"));           //105
            vector.addToEnd(new PSObjectName("/F"));           //106
            vector.addToEnd(new PSObjectName("/G"));           //107
            vector.addToEnd(new PSObjectName("/H"));           //110
            vector.addToEnd(new PSObjectName("/I"));           //111
            vector.addToEnd(new PSObjectName("/J"));           //112
            vector.addToEnd(new PSObjectName("/K"));           //113
            vector.addToEnd(new PSObjectName("/L"));           //114
            vector.addToEnd(new PSObjectName("/M"));           //115
            vector.addToEnd(new PSObjectName("/N"));           //116
            vector.addToEnd(new PSObjectName("/O"));           //117
            vector.addToEnd(new PSObjectName("/P"));           //120
            vector.addToEnd(new PSObjectName("/Q"));           //121
            vector.addToEnd(new PSObjectName("/R"));           //122
            vector.addToEnd(new PSObjectName("/S"));           //123
            vector.addToEnd(new PSObjectName("/T"));           //124
            vector.addToEnd(new PSObjectName("/U"));           //125
            vector.addToEnd(new PSObjectName("/V"));           //126
            vector.addToEnd(new PSObjectName("/W"));           //127
            vector.addToEnd(new PSObjectName("/X"));           //130
            vector.addToEnd(new PSObjectName("/Y"));           //131
            vector.addToEnd(new PSObjectName("/Z"));           //132
            vector.addToEnd(new PSObjectName("/bracketleft")); //133
            vector.addToEnd(new PSObjectName("/backslash"));   //134
            vector.addToEnd(new PSObjectName("/bracketright")); //135
            vector.addToEnd(new PSObjectName("/asciicircum")); //136
            vector.addToEnd(new PSObjectName("/underscore"));  //137
            vector.addToEnd(new PSObjectName("/quoteleft"));   //140
            vector.addToEnd(new PSObjectName("/a"));           //141
            vector.addToEnd(new PSObjectName("/b"));           //142
            vector.addToEnd(new PSObjectName("/c"));           //143
            vector.addToEnd(new PSObjectName("/d"));           //144
            vector.addToEnd(new PSObjectName("/e"));           //145
            vector.addToEnd(new PSObjectName("/f"));           //146
            vector.addToEnd(new PSObjectName("/g"));           //147
            vector.addToEnd(new PSObjectName("/h"));           //150
            vector.addToEnd(new PSObjectName("/i"));           //151
            vector.addToEnd(new PSObjectName("/j"));           //152
            vector.addToEnd(new PSObjectName("/k"));           //153
            vector.addToEnd(new PSObjectName("/l"));           //154
            vector.addToEnd(new PSObjectName("/m"));           //155
            vector.addToEnd(new PSObjectName("/n"));           //156
            vector.addToEnd(new PSObjectName("/o"));           //157
            vector.addToEnd(new PSObjectName("/p"));           //160
            vector.addToEnd(new PSObjectName("/q"));           //161
            vector.addToEnd(new PSObjectName("/r"));           //162
            vector.addToEnd(new PSObjectName("/s"));           //163
            vector.addToEnd(new PSObjectName("/t"));           //164
            vector.addToEnd(new PSObjectName("/u"));           //165
            vector.addToEnd(new PSObjectName("/v"));           //166
            vector.addToEnd(new PSObjectName("/w"));           //167
            vector.addToEnd(new PSObjectName("/x"));           //170
            vector.addToEnd(new PSObjectName("/y"));           //171
            vector.addToEnd(new PSObjectName("/z"));           //172
            vector.addToEnd(new PSObjectName("/braceleft"));   //173
            vector.addToEnd(new PSObjectName("/bar"));         //174
            vector.addToEnd(new PSObjectName("/braceright"));  //175
            vector.addToEnd(new PSObjectName("/asciitilde"));  //176
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
            vector.addToEnd(EncodingManager.SYMB_NOTDEF);     //240
            vector.addToEnd(new PSObjectName("/exclamdown"));  //241
            vector.addToEnd(new PSObjectName("/cent"));        //242
            vector.addToEnd(new PSObjectName("/sterling"));    //243
            vector.addToEnd(new PSObjectName("/fraction"));    //244
            vector.addToEnd(new PSObjectName("/yen"));         //245
            vector.addToEnd(new PSObjectName("/florin"));      //246
            vector.addToEnd(new PSObjectName("/section"));     //247
            vector.addToEnd(new PSObjectName("/currency"));    //250
            vector.addToEnd(new PSObjectName("/quotesingle")); //251
            vector.addToEnd(new PSObjectName("/quotedblleft")); //252
            vector.addToEnd(new PSObjectName("/guillemotleft")); //253
            vector.addToEnd(new PSObjectName("/guilsinglleft")); //254
            vector.addToEnd(new PSObjectName("/guilsinglright")); //255
            vector.addToEnd(new PSObjectName("/fi"));          //256
            vector.addToEnd(new PSObjectName("/fl"));          //257
            vector.addToEnd(EncodingManager.SYMB_NOTDEF);     //260
            vector.addToEnd(new PSObjectName("/endash"));      //261
            vector.addToEnd(new PSObjectName("/dagger"));      //262
            vector.addToEnd(new PSObjectName("/daggerdbl"));   //263
            vector.addToEnd(new PSObjectName("/periodcentered")); //264
            vector.addToEnd(EncodingManager.SYMB_NOTDEF);     //265
            vector.addToEnd(new PSObjectName("/paragraph"));   //266
            vector.addToEnd(new PSObjectName("/bullet"));      //267
            vector.addToEnd(new PSObjectName("/quotesinglbase")); //270
            vector.addToEnd(new PSObjectName("/quotedblbase")); //271
            vector.addToEnd(new PSObjectName("/quotedblright"));  //272
            vector.addToEnd(new PSObjectName("/guillemotright")); //273
            vector.addToEnd(new PSObjectName("/ellipsis"));    //274
            vector.addToEnd(new PSObjectName("/perthousand")); //275        
            vector.addToEnd(EncodingManager.SYMB_NOTDEF);     //276        
            vector.addToEnd(new PSObjectName("/questiondown")); //277
            vector.addToEnd(EncodingManager.SYMB_NOTDEF);     //300
            vector.addToEnd(new PSObjectName("/grave"));       //301
            vector.addToEnd(new PSObjectName("/acute"));       //302
            vector.addToEnd(new PSObjectName("/circumflex"));  //303
            vector.addToEnd(new PSObjectName("/tilde"));       //304
            vector.addToEnd(new PSObjectName("/macron"));      //305
            vector.addToEnd(new PSObjectName("/breve"));       //306
            vector.addToEnd(new PSObjectName("/dotaccent"));   //307
            vector.addToEnd(new PSObjectName("/dieresis"));    //310
            vector.addToEnd(EncodingManager.SYMB_NOTDEF);     //311
            vector.addToEnd(new PSObjectName("/ring"));        //312
            vector.addToEnd(new PSObjectName("/cedilla"));     //313
            vector.addToEnd(EncodingManager.SYMB_NOTDEF);     //314
            vector.addToEnd(new PSObjectName("/hungarumlaut")); //315
            vector.addToEnd(new PSObjectName("/ogonek"));      //316
            vector.addToEnd(new PSObjectName("/caron"));       //317
            vector.addToEnd(new PSObjectName("/emdash"));      //320
            vector.addToEnd(EncodingManager.SYMB_NOTDEF);     //321
            vector.addToEnd(EncodingManager.SYMB_NOTDEF);     //322
            vector.addToEnd(EncodingManager.SYMB_NOTDEF);     //323
            vector.addToEnd(EncodingManager.SYMB_NOTDEF);     //324
            vector.addToEnd(EncodingManager.SYMB_NOTDEF);     //325
            vector.addToEnd(EncodingManager.SYMB_NOTDEF);     //326
            vector.addToEnd(EncodingManager.SYMB_NOTDEF);     //327
            vector.addToEnd(EncodingManager.SYMB_NOTDEF);     //330
            vector.addToEnd(EncodingManager.SYMB_NOTDEF);     //331
            vector.addToEnd(EncodingManager.SYMB_NOTDEF);     //332
            vector.addToEnd(EncodingManager.SYMB_NOTDEF);     //333
            vector.addToEnd(EncodingManager.SYMB_NOTDEF);     //334
            vector.addToEnd(EncodingManager.SYMB_NOTDEF);     //335
            vector.addToEnd(EncodingManager.SYMB_NOTDEF);     //336
            vector.addToEnd(EncodingManager.SYMB_NOTDEF);     //337
            vector.addToEnd(EncodingManager.SYMB_NOTDEF);     //340
            vector.addToEnd(new PSObjectName("/AE"));          //341
            vector.addToEnd(EncodingManager.SYMB_NOTDEF);     //342
            vector.addToEnd(new PSObjectName("/ordfeminine")); //343
            vector.addToEnd(EncodingManager.SYMB_NOTDEF);     //344
            vector.addToEnd(EncodingManager.SYMB_NOTDEF);     //345
            vector.addToEnd(EncodingManager.SYMB_NOTDEF);     //346
            vector.addToEnd(EncodingManager.SYMB_NOTDEF);     //347
            vector.addToEnd(new PSObjectName("/Lslash"));      //350
            vector.addToEnd(new PSObjectName("/Oslash"));      //351
            vector.addToEnd(new PSObjectName("/OE"));          //352
            vector.addToEnd(new PSObjectName("/ordmasculine")); //353
            vector.addToEnd(EncodingManager.SYMB_NOTDEF);     //354
            vector.addToEnd(EncodingManager.SYMB_NOTDEF);     //355
            vector.addToEnd(EncodingManager.SYMB_NOTDEF);     //356
            vector.addToEnd(EncodingManager.SYMB_NOTDEF);     //357
            vector.addToEnd(EncodingManager.SYMB_NOTDEF);     //360
            vector.addToEnd(new PSObjectName("/ae"));          //361
            vector.addToEnd(EncodingManager.SYMB_NOTDEF);     //362
            vector.addToEnd(EncodingManager.SYMB_NOTDEF);     //363
            vector.addToEnd(EncodingManager.SYMB_NOTDEF);     //364
            vector.addToEnd(new PSObjectName("/dotlessi"));    //365
            vector.addToEnd(EncodingManager.SYMB_NOTDEF);     //366
            vector.addToEnd(EncodingManager.SYMB_NOTDEF);     //367
            vector.addToEnd(new PSObjectName("/lslash"));      //370
            vector.addToEnd(new PSObjectName("/oslash"));      //371
            vector.addToEnd(new PSObjectName("/oe"));          //372
            vector.addToEnd(new PSObjectName("/germandbls"));  //373
            vector.addToEnd(EncodingManager.SYMB_NOTDEF);     //374
            vector.addToEnd(EncodingManager.SYMB_NOTDEF);     //375
            vector.addToEnd(EncodingManager.SYMB_NOTDEF);     //376
            vector.addToEnd(EncodingManager.SYMB_NOTDEF);     //377
        } catch (PSErrorRangeCheck e) {
            throw new ProgramError("A range check error occurred where it"
                    + " shouldn't.");
        }
    }
    
    /**
     * Gets the encoding vector.
     * 
     * @param vm The VM manager.
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
