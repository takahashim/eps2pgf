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

import net.sf.eps2pgf.ps.objects.PSObjectName;

/**
 * Defines the standard encoding vector.
 * 
 * @author Paul Wagenaars
 *
 */
public final class SymbolEncoding {

    /** Name of this encoding. */
    public static final PSObjectName NAME =
        new PSObjectName("/SymbolEncoding");

    /**
     * "Hidden" constructor.
     */
    private SymbolEncoding() {
        /* empty block */
    }
    
    /**
     * Gets the encoding vector.
     * 
     * @return The vector.
     */
    public static PSObjectName[] get() {
        PSObjectName[] vector = new PSObjectName[256];
        vector[0000] = EncodingManager.SYMB_NOTDEF;     //000
        vector[0001] = EncodingManager.SYMB_NOTDEF;     //001
        vector[0002] = EncodingManager.SYMB_NOTDEF;     //002
        vector[0003] = EncodingManager.SYMB_NOTDEF;     //003
        vector[0004] = EncodingManager.SYMB_NOTDEF;     //004
        vector[0005] = EncodingManager.SYMB_NOTDEF;     //005
        vector[0006] = EncodingManager.SYMB_NOTDEF;     //006
        vector[0007] = EncodingManager.SYMB_NOTDEF;     //007
        vector[0010] = EncodingManager.SYMB_NOTDEF;     //010
        vector[0011] = EncodingManager.SYMB_NOTDEF;     //011
        vector[0012] = EncodingManager.SYMB_NOTDEF;     //012
        vector[0013] = EncodingManager.SYMB_NOTDEF;     //013
        vector[0014] = EncodingManager.SYMB_NOTDEF;     //014
        vector[0015] = EncodingManager.SYMB_NOTDEF;     //015
        vector[0016] = EncodingManager.SYMB_NOTDEF;     //016
        vector[0017] = EncodingManager.SYMB_NOTDEF;     //017
        vector[0020] = EncodingManager.SYMB_NOTDEF;     //020
        vector[0021] = EncodingManager.SYMB_NOTDEF;     //021
        vector[0022] = EncodingManager.SYMB_NOTDEF;     //022
        vector[0023] = EncodingManager.SYMB_NOTDEF;     //023
        vector[0024] = EncodingManager.SYMB_NOTDEF;     //024
        vector[0025] = EncodingManager.SYMB_NOTDEF;     //025
        vector[0026] = EncodingManager.SYMB_NOTDEF;     //026
        vector[0027] = EncodingManager.SYMB_NOTDEF;     //027
        vector[0030] = EncodingManager.SYMB_NOTDEF;     //030
        vector[0031] = EncodingManager.SYMB_NOTDEF;     //031
        vector[0032] = EncodingManager.SYMB_NOTDEF;     //032
        vector[0033] = EncodingManager.SYMB_NOTDEF;     //033
        vector[0034] = EncodingManager.SYMB_NOTDEF;     //034
        vector[0035] = EncodingManager.SYMB_NOTDEF;     //035
        vector[0036] = EncodingManager.SYMB_NOTDEF;     //036
        vector[0037] = EncodingManager.SYMB_NOTDEF;     //037
        vector[0040] = EncodingManager.SYMB_SPACE;       //040
        vector[0041] = EncodingManager.SYMB_EXCLAM;      //041
        vector[0042] = new PSObjectName("/universal");   //042
        vector[0043] = new PSObjectName("/numbersign");  //043
        vector[0044] = new PSObjectName("/existential"); //044
        vector[0045] = new PSObjectName("/percent");     //045
        vector[0046] = new PSObjectName("/ampersand");   //046
        vector[0047] = new PSObjectName("/suchthat");    //047
        vector[0050] = new PSObjectName("/parenleft");   //050
        vector[0051] = new PSObjectName("/parenright");  //051
        vector[0052] = new PSObjectName("/asterisk");    //052
        vector[0053] = new PSObjectName("/plus");        //053
        vector[0054] = new PSObjectName("/comma");       //054
        vector[0055] = new PSObjectName("/minus");       //055
        vector[0056] = new PSObjectName("/period");      //056
        vector[0057] = new PSObjectName("/slash");       //057
        vector[0060] = EncodingManager.SYMB_ZERO;        //060
        vector[0061] = EncodingManager.SYMB_ONE;         //061
        vector[0062] = EncodingManager.SYMB_TWO;         //062
        vector[0063] = EncodingManager.SYMB_THREE;       //063
        vector[0064] = EncodingManager.SYMB_FOUR;        //064
        vector[0065] = EncodingManager.SYMB_FIVE;        //065
        vector[0066] = EncodingManager.SYMB_SIX;         //066
        vector[0067] = EncodingManager.SYMB_SEVEN;       //067
        vector[0070] = EncodingManager.SYMB_EIGHT;       //070
        vector[0071] = EncodingManager.SYMB_NINE;        //071
        vector[0072] = new PSObjectName("/colon");       //072
        vector[0073] = new PSObjectName("/semicolon");   //073
        vector[0074] = new PSObjectName("/less");        //074
        vector[0075] = new PSObjectName("/equal");       //075
        vector[0076] = new PSObjectName("/greater");     //076
        vector[0077] = new PSObjectName("/question");    //077
        vector[0100] = new PSObjectName("/congruent");   //100
        vector[0101] = new PSObjectName("/Alpha");       //101
        vector[0102] = new PSObjectName("/Beta");        //102
        vector[0103] = new PSObjectName("/Chi");         //103
        vector[0104] = new PSObjectName("/Delta");       //104
        vector[0105] = new PSObjectName("/Epsilon");     //105
        vector[0106] = new PSObjectName("/Phi");         //106
        vector[0107] = new PSObjectName("/Gamma");       //107
        vector[0110] = new PSObjectName("/Eta");         //110
        vector[0111] = new PSObjectName("/Iota");        //111
        vector[0112] = new PSObjectName("/theta1");      //112
        vector[0113] = new PSObjectName("/Kappa");       //113
        vector[0114] = new PSObjectName("/Lambda");      //114
        vector[0115] = new PSObjectName("/Mu");          //115
        vector[0116] = new PSObjectName("/Nu");          //116
        vector[0117] = new PSObjectName("/Omicron");     //117
        vector[0120] = new PSObjectName("/Pi");          //120
        vector[0121] = new PSObjectName("/Theta");       //121
        vector[0122] = new PSObjectName("/Rho");         //122
        vector[0123] = new PSObjectName("/Sigma");       //123
        vector[0124] = new PSObjectName("/Tau");         //124
        vector[0125] = new PSObjectName("/Upsilon");     //125
        vector[0126] = new PSObjectName("/sigma1");      //126
        vector[0127] = new PSObjectName("/Omega");       //127
        vector[0130] = new PSObjectName("/Xi");          //130
        vector[0131] = new PSObjectName("/Psi");         //131
        vector[0132] = new PSObjectName("/Zeta");        //132
        vector[0133] = new PSObjectName("/bracketleft"); //133
        vector[0134] = new PSObjectName("/therefore");    //134
        vector[0135] = new PSObjectName("/bracketright"); //135
        vector[0136] = new PSObjectName("/perpendicular"); //136
        vector[0137] = new PSObjectName("/underscore");  //137
        vector[0140] = new PSObjectName("/radicalex");   //140
        vector[0141] = new PSObjectName("/alpha");       //141
        vector[0142] = new PSObjectName("/beta");        //142
        vector[0143] = new PSObjectName("/chi");         //143
        vector[0144] = new PSObjectName("/delta");       //144
        vector[0145] = new PSObjectName("/epsilon");     //145
        vector[0146] = new PSObjectName("/phi");         //146
        vector[0147] = new PSObjectName("/gamma");       //147
        vector[0150] = new PSObjectName("/eta");         //150
        vector[0151] = new PSObjectName("/iota");        //151
        vector[0152] = new PSObjectName("/phi1");        //152
        vector[0153] = new PSObjectName("/kappa");       //153
        vector[0154] = new PSObjectName("/lambda");      //154
        vector[0155] = new PSObjectName("/mu");          //155
        vector[0156] = new PSObjectName("/nu");          //156
        vector[0157] = new PSObjectName("/omicron");     //157
        vector[0160] = new PSObjectName("/pi");          //160
        vector[0161] = new PSObjectName("/theta");       //161
        vector[0162] = new PSObjectName("/rho");         //162
        vector[0163] = new PSObjectName("/sigma");       //163
        vector[0164] = new PSObjectName("/tau");         //164
        vector[0165] = new PSObjectName("/upsilon");     //165
        vector[0166] = new PSObjectName("/omega1");      //166
        vector[0167] = new PSObjectName("/omega");       //167
        vector[0170] = new PSObjectName("/xi");          //170
        vector[0171] = new PSObjectName("/psi");         //171
        vector[0172] = new PSObjectName("/zeta");        //172
        vector[0173] = new PSObjectName("/braceleft");   //173
        vector[0174] = new PSObjectName("/bar");         //174
        vector[0175] = new PSObjectName("/braceright");  //175
        vector[0176] = new PSObjectName("/similar");     //176
        vector[0177] = EncodingManager.SYMB_NOTDEF;     //177
        vector[0200] = EncodingManager.SYMB_NOTDEF;     //200
        vector[0201] = EncodingManager.SYMB_NOTDEF;     //201
        vector[0202] = EncodingManager.SYMB_NOTDEF;     //202
        vector[0203] = EncodingManager.SYMB_NOTDEF;     //203
        vector[0204] = EncodingManager.SYMB_NOTDEF;     //204
        vector[0205] = EncodingManager.SYMB_NOTDEF;     //205
        vector[0206] = EncodingManager.SYMB_NOTDEF;     //206
        vector[0207] = EncodingManager.SYMB_NOTDEF;     //207
        vector[0210] = EncodingManager.SYMB_NOTDEF;     //210
        vector[0211] = EncodingManager.SYMB_NOTDEF;     //211
        vector[0212] = EncodingManager.SYMB_NOTDEF;     //212
        vector[0213] = EncodingManager.SYMB_NOTDEF;     //213
        vector[0214] = EncodingManager.SYMB_NOTDEF;     //214
        vector[0215] = EncodingManager.SYMB_NOTDEF;     //215
        vector[0216] = EncodingManager.SYMB_NOTDEF;     //216
        vector[0217] = EncodingManager.SYMB_NOTDEF;     //217
        vector[0220] = EncodingManager.SYMB_NOTDEF;     //220
        vector[0221] = EncodingManager.SYMB_NOTDEF;     //221
        vector[0222] = EncodingManager.SYMB_NOTDEF;     //222
        vector[0223] = EncodingManager.SYMB_NOTDEF;     //223
        vector[0224] = EncodingManager.SYMB_NOTDEF;     //224
        vector[0225] = EncodingManager.SYMB_NOTDEF;     //225
        vector[0226] = EncodingManager.SYMB_NOTDEF;     //226
        vector[0227] = EncodingManager.SYMB_NOTDEF;     //227
        vector[0230] = EncodingManager.SYMB_NOTDEF;     //230
        vector[0231] = EncodingManager.SYMB_NOTDEF;     //231
        vector[0232] = EncodingManager.SYMB_NOTDEF;     //232
        vector[0233] = EncodingManager.SYMB_NOTDEF;     //233
        vector[0234] = EncodingManager.SYMB_NOTDEF;     //234
        vector[0235] = EncodingManager.SYMB_NOTDEF;     //235
        vector[0236] = EncodingManager.SYMB_NOTDEF;     //236
        vector[0237] = EncodingManager.SYMB_NOTDEF;     //237
        vector[0240] = new PSObjectName("/Euro");        //240
        vector[0241] = new PSObjectName("/Upsilon1");    //241
        vector[0242] = new PSObjectName("/minute");      //242
        vector[0243] = new PSObjectName("/lessequal");   //243
        vector[0244] = new PSObjectName("/fraction");    //244
        vector[0245] = new PSObjectName("/infinity");    //245
        vector[0246] = new PSObjectName("/florin");      //246
        vector[0247] = new PSObjectName("/club");        //247
        vector[0250] = new PSObjectName("/diamond");     //250
        vector[0251] = new PSObjectName("/heart");       //251
        vector[0252] = new PSObjectName("/spade");       //252
        vector[0253] = new PSObjectName("/arrowboth");   //253
        vector[0254] = new PSObjectName("/arrowleft");   //254
        vector[0255] = new PSObjectName("/arrowup");     //255
        vector[0256] = new PSObjectName("/arrowright");  //256
        vector[0257] = new PSObjectName("/arrowdown");   //257
        vector[0260] = new PSObjectName("/degree");      //260
        vector[0261] = new PSObjectName("/plusminus");   //261
        vector[0262] = new PSObjectName("/second");       //262
        vector[0263] = new PSObjectName("/greaterequal"); //263
        vector[0264] = new PSObjectName("/multiply");     //264
        vector[0265] = new PSObjectName("/proportional"); //265
        vector[0266] = new PSObjectName("/partialdiff");  //266
        vector[0267] = new PSObjectName("/bullet");       //267
        vector[0270] = new PSObjectName("/divide");       //270
        vector[0271] = new PSObjectName("/notequal");     //271
        vector[0272] = new PSObjectName("/equivalence");  //272
        vector[0273] = new PSObjectName("/approxequal");  //273
        vector[0274] = new PSObjectName("/ellipsis");     //274
        vector[0275] = new PSObjectName("/arrowvertex");  //275        
        vector[0276] = new PSObjectName("/arrowhorizex"); //276
        vector[0277] = new PSObjectName("/carriagereturn"); //277
        vector[0300] = new PSObjectName("/aleph");        //300
        vector[0301] = new PSObjectName("/Ifraktur");     //301
        vector[0302] = new PSObjectName("/Rfraktur");     //302
        vector[0303] = new PSObjectName("/weierstrass");  //303
        vector[0304] = new PSObjectName("/circlemultiply"); //304
        vector[0305] = new PSObjectName("/circleplus");   //305
        vector[0306] = new PSObjectName("/emptyset");     //306
        vector[0307] = new PSObjectName("/intersection"); //307
        vector[0310] = new PSObjectName("/union");        //310
        vector[0311] = new PSObjectName("/propersuperset"); //311
        vector[0312] = new PSObjectName("/reflexsuperset"); //312
        vector[0313] = new PSObjectName("/notsubset");    //313
        vector[0314] = new PSObjectName("/propersubset"); //314
        vector[0315] = new PSObjectName("/reflexsubset"); //315
        vector[0316] = new PSObjectName("/element");      //316
        vector[0317] = new PSObjectName("/notelement");   //317
        vector[0320] = new PSObjectName("/angle");        //320
        vector[0321] = new PSObjectName("/gradient");     //321
        vector[0322] = new PSObjectName("/registerserif"); //322
        vector[0323] = new PSObjectName("/copyrightserif"); //323
        vector[0324] = new PSObjectName("/trademarkserif"); //324
        vector[0325] = new PSObjectName("/product");       //325
        vector[0326] = new PSObjectName("/radical");       //326
        vector[0327] = new PSObjectName("/dotmath");       //327
        vector[0330] = new PSObjectName("/logicalnot");    //330
        vector[0331] = new PSObjectName("/logicaland");    //331
        vector[0332] = new PSObjectName("/logicalor");     //332
        vector[0333] = new PSObjectName("/arrowdblboth");  //333
        vector[0334] = new PSObjectName("/arrowdblleft");  //334
        vector[0335] = new PSObjectName("/arrowdblup");    //335
        vector[0336] = new PSObjectName("/arrowdblright"); //336
        vector[0337] = new PSObjectName("/arrowdbldown");  //337
        vector[0340] = new PSObjectName("/lozenge");       //340
        vector[0341] = new PSObjectName("/angleleft");     //341
        vector[0342] = new PSObjectName("/registersans");  //342
        vector[0343] = new PSObjectName("/copyrightsans"); //343
        vector[0344] = new PSObjectName("/trademarksans"); //344
        vector[0345] = new PSObjectName("/summation");     //345
        vector[0346] = new PSObjectName("/parenlefttp");   //346
        vector[0347] = new PSObjectName("/parenleftex");   //347
        vector[0350] = new PSObjectName("/parenleftbt");   //350
        vector[0351] = new PSObjectName("/bracketlefttp"); //351
        vector[0352] = new PSObjectName("/bracketleftex"); //352
        vector[0353] = new PSObjectName("/bracketleftbt"); //353
        vector[0354] = new PSObjectName("/bracelefttp");   //354
        vector[0355] = new PSObjectName("/braceleftmid");  //355
        vector[0356] = new PSObjectName("/braceleftbt");   //356
        vector[0357] = new PSObjectName("/braceex");       //357
        vector[0360] = EncodingManager.SYMB_NOTDEF;       //360
        vector[0361] = new PSObjectName("/angleright");    //361
        vector[0362] = new PSObjectName("/integral");      //362
        vector[0363] = new PSObjectName("/integraltp");    //363
        vector[0364] = new PSObjectName("/integralex");    //364
        vector[0365] = new PSObjectName("/integralbt");    //365
        vector[0366] = new PSObjectName("/parenrighttp");  //366
        vector[0367] = new PSObjectName("/parenrightex");  //367
        vector[0370] = new PSObjectName("/parenrightbt");  //370
        vector[0371] = new PSObjectName("/bracketrighttp"); //371
        vector[0372] = new PSObjectName("/bracketrightex"); //372
        vector[0373] = new PSObjectName("/bracketrightbt"); //373
        vector[0374] = new PSObjectName("/bracerighttp");  //374
        vector[0375] = new PSObjectName("/bracerightmid"); //375
        vector[0376] = new PSObjectName("/bracerightbt");  //376
        vector[0377] = EncodingManager.SYMB_NOTDEF;       //377
        
        return vector;
    }
}
