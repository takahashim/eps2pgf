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
public final class StandardEncoding {
    
    /** Name of this encoding. */
    public static final PSObjectName NAME =
        new PSObjectName("/StandardEncoding");

    
    /**
     * "Hidden" constructor.
     */
    private StandardEncoding() {
        /* empty block */
    }
    
    /**
     * Gets the encoding vector.
     * 
     * @return The vector.
     */
    public static PSObjectName[] get() {
        PSObjectName[] vector = new PSObjectName[256];
        vector[0000] = EncodingManager.S_NOTDEF;         //000
        vector[0001] = EncodingManager.S_NOTDEF;         //001
        vector[0002] = EncodingManager.S_NOTDEF;         //002
        vector[0003] = EncodingManager.S_NOTDEF;         //003
        vector[0004] = EncodingManager.S_NOTDEF;         //004
        vector[0005] = EncodingManager.S_NOTDEF;         //005
        vector[0006] = EncodingManager.S_NOTDEF;         //006
        vector[0007] = EncodingManager.S_NOTDEF;         //007
        vector[0010] = EncodingManager.S_NOTDEF;         //010
        vector[0011] = EncodingManager.S_NOTDEF;         //011
        vector[0012] = EncodingManager.S_NOTDEF;         //012
        vector[0013] = EncodingManager.S_NOTDEF;         //013
        vector[0014] = EncodingManager.S_NOTDEF;         //014
        vector[0015] = EncodingManager.S_NOTDEF;         //015
        vector[0016] = EncodingManager.S_NOTDEF;         //016
        vector[0017] = EncodingManager.S_NOTDEF;         //017
        vector[0020] = EncodingManager.S_NOTDEF;         //020
        vector[0021] = EncodingManager.S_NOTDEF;         //021
        vector[0022] = EncodingManager.S_NOTDEF;         //022
        vector[0023] = EncodingManager.S_NOTDEF;         //023
        vector[0024] = EncodingManager.S_NOTDEF;         //024
        vector[0025] = EncodingManager.S_NOTDEF;         //025
        vector[0026] = EncodingManager.S_NOTDEF;         //026
        vector[0027] = EncodingManager.S_NOTDEF;         //027
        vector[0030] = EncodingManager.S_NOTDEF;         //030
        vector[0031] = EncodingManager.S_NOTDEF;         //031
        vector[0032] = EncodingManager.S_NOTDEF;         //032
        vector[0033] = EncodingManager.S_NOTDEF;         //033
        vector[0034] = EncodingManager.S_NOTDEF;         //034
        vector[0035] = EncodingManager.S_NOTDEF;         //035
        vector[0036] = EncodingManager.S_NOTDEF;         //036
        vector[0037] = EncodingManager.S_NOTDEF;         //037
        vector[0040] = EncodingManager.S_SPACE;          //040
        vector[0041] = EncodingManager.S_EXCLAM;         //041
        vector[0042] = EncodingManager.S_QUOTEDBL;       //042
        vector[0043] = EncodingManager.S_NUMBERSIGN;     //043
        vector[0044] = EncodingManager.S_DOLLAR;         //044
        vector[0045] = EncodingManager.S_PERCENT;        //045
        vector[0046] = EncodingManager.S_AMPERSAND;      //046
        vector[0047] = EncodingManager.S_QUOTERIGHT;     //047
        vector[0050] = EncodingManager.S_PARENLEFT;      //050
        vector[0051] = EncodingManager.S_PARENRIGHT;     //051
        vector[0052] = EncodingManager.S_ASTERISK;       //052
        vector[0053] = EncodingManager.S_PLUS;           //053
        vector[0054] = EncodingManager.S_COMMA;          //054
        vector[0055] = EncodingManager.S_MINUS;          //055
        vector[0056] = EncodingManager.S_PERIOD;         //056
        vector[0057] = EncodingManager.S_SLASH;          //057
        vector[0060] = EncodingManager.S_ZERO;           //060
        vector[0061] = EncodingManager.S_ONE;            //061
        vector[0062] = EncodingManager.S_TWO;            //062
        vector[0063] = EncodingManager.S_THREE;          //063
        vector[0064] = EncodingManager.S_FOUR;           //064
        vector[0065] = EncodingManager.S_FIVE;           //065
        vector[0066] = EncodingManager.S_SIX;            //066
        vector[0067] = EncodingManager.S_SEVEN;          //067
        vector[0070] = EncodingManager.S_EIGHT;          //070
        vector[0071] = EncodingManager.S_NINE;           //071
        vector[0072] = new PSObjectName("/colon");       //072
        vector[0073] = new PSObjectName("/semicolon");   //073
        vector[0074] = new PSObjectName("/less");        //074
        vector[0075] = new PSObjectName("/equal");       //075
        vector[0076] = new PSObjectName("/greater");     //076
        vector[0077] = new PSObjectName("/question");    //077
        vector[0100] = new PSObjectName("/at");          //100
        vector[0101] = new PSObjectName("/A");           //101
        vector[0102] = new PSObjectName("/B");           //102
        vector[0103] = new PSObjectName("/C");           //103
        vector[0104] = new PSObjectName("/D");           //104
        vector[0105] = new PSObjectName("/E");           //105
        vector[0106] = new PSObjectName("/F");           //106
        vector[0107] = new PSObjectName("/G");           //107
        vector[0110] = new PSObjectName("/H");           //110
        vector[0111] = new PSObjectName("/I");           //111
        vector[0112] = new PSObjectName("/J");           //112
        vector[0113] = new PSObjectName("/K");           //113
        vector[0114] = new PSObjectName("/L");           //114
        vector[0115] = new PSObjectName("/M");           //115
        vector[0116] = new PSObjectName("/N");           //116
        vector[0117] = new PSObjectName("/O");           //117
        vector[0120] = new PSObjectName("/P");           //120
        vector[0121] = new PSObjectName("/Q");           //121
        vector[0122] = new PSObjectName("/R");           //122
        vector[0123] = new PSObjectName("/S");           //123
        vector[0124] = new PSObjectName("/T");           //124
        vector[0125] = new PSObjectName("/U");           //125
        vector[0126] = new PSObjectName("/V");           //126
        vector[0127] = new PSObjectName("/W");           //127
        vector[0130] = new PSObjectName("/X");           //130
        vector[0131] = new PSObjectName("/Y");           //131
        vector[0132] = new PSObjectName("/Z");           //132
        vector[0133] = new PSObjectName("/bracketleft"); //133
        vector[0134] = new PSObjectName("/backslash");   //134
        vector[0135] = new PSObjectName("/bracketright"); //135
        vector[0136] = new PSObjectName("/asciicircum"); //136
        vector[0137] = new PSObjectName("/underscore");  //137
        vector[0140] = new PSObjectName("/quoteleft");   //140
        vector[0141] = new PSObjectName("/a");           //141
        vector[0142] = new PSObjectName("/b");           //142
        vector[0143] = new PSObjectName("/c");           //143
        vector[0144] = new PSObjectName("/d");           //144
        vector[0145] = new PSObjectName("/e");           //145
        vector[0146] = new PSObjectName("/f");           //146
        vector[0147] = new PSObjectName("/g");           //147
        vector[0150] = new PSObjectName("/h");           //150
        vector[0151] = new PSObjectName("/i");           //151
        vector[0152] = new PSObjectName("/j");           //152
        vector[0153] = new PSObjectName("/k");           //153
        vector[0154] = new PSObjectName("/l");           //154
        vector[0155] = new PSObjectName("/m");           //155
        vector[0156] = new PSObjectName("/n");           //156
        vector[0157] = new PSObjectName("/o");           //157
        vector[0160] = new PSObjectName("/p");           //160
        vector[0161] = new PSObjectName("/q");           //161
        vector[0162] = new PSObjectName("/r");           //162
        vector[0163] = new PSObjectName("/s");           //163
        vector[0164] = new PSObjectName("/t");           //164
        vector[0165] = new PSObjectName("/u");           //165
        vector[0166] = new PSObjectName("/v");           //166
        vector[0167] = new PSObjectName("/w");           //167
        vector[0170] = new PSObjectName("/x");           //170
        vector[0171] = new PSObjectName("/y");           //171
        vector[0172] = new PSObjectName("/z");           //172
        vector[0173] = new PSObjectName("/braceleft");   //173
        vector[0174] = new PSObjectName("/bar");         //174
        vector[0175] = new PSObjectName("/braceright");  //175
        vector[0176] = new PSObjectName("/asciitilde");  //176
        vector[0177] = EncodingManager.S_NOTDEF;      //177
        vector[0200] = EncodingManager.S_NOTDEF;      //200
        vector[0201] = EncodingManager.S_NOTDEF;      //201
        vector[0202] = EncodingManager.S_NOTDEF;      //202
        vector[0203] = EncodingManager.S_NOTDEF;      //203
        vector[0204] = EncodingManager.S_NOTDEF;      //204
        vector[0205] = EncodingManager.S_NOTDEF;      //205
        vector[0206] = EncodingManager.S_NOTDEF;      //206
        vector[0207] = EncodingManager.S_NOTDEF;      //207
        vector[0210] = EncodingManager.S_NOTDEF;      //210
        vector[0211] = EncodingManager.S_NOTDEF;      //211
        vector[0212] = EncodingManager.S_NOTDEF;      //212
        vector[0213] = EncodingManager.S_NOTDEF;      //213
        vector[0214] = EncodingManager.S_NOTDEF;      //214
        vector[0215] = EncodingManager.S_NOTDEF;      //215
        vector[0216] = EncodingManager.S_NOTDEF;      //216
        vector[0217] = EncodingManager.S_NOTDEF;      //217
        vector[0220] = EncodingManager.S_NOTDEF;      //220
        vector[0221] = EncodingManager.S_NOTDEF;      //221
        vector[0222] = EncodingManager.S_NOTDEF;      //222
        vector[0223] = EncodingManager.S_NOTDEF;      //223
        vector[0224] = EncodingManager.S_NOTDEF;      //224
        vector[0225] = EncodingManager.S_NOTDEF;      //225
        vector[0226] = EncodingManager.S_NOTDEF;      //226
        vector[0227] = EncodingManager.S_NOTDEF;      //227
        vector[0230] = EncodingManager.S_NOTDEF;      //230
        vector[0231] = EncodingManager.S_NOTDEF;      //231
        vector[0232] = EncodingManager.S_NOTDEF;      //232
        vector[0233] = EncodingManager.S_NOTDEF;      //233
        vector[0234] = EncodingManager.S_NOTDEF;      //234
        vector[0235] = EncodingManager.S_NOTDEF;      //235
        vector[0236] = EncodingManager.S_NOTDEF;      //236
        vector[0237] = EncodingManager.S_NOTDEF;      //237
        vector[0240] = EncodingManager.S_NOTDEF;      //240
        vector[0241] = new PSObjectName("/exclamdown");  //241
        vector[0242] = new PSObjectName("/cent");        //242
        vector[0243] = new PSObjectName("/sterling");    //243
        vector[0244] = new PSObjectName("/fraction");    //244
        vector[0245] = new PSObjectName("/yen");         //245
        vector[0246] = new PSObjectName("/florin");      //246
        vector[0247] = new PSObjectName("/section");     //247
        vector[0250] = new PSObjectName("/currency");    //250
        vector[0251] = new PSObjectName("/quotesingle"); //251
        vector[0252] = new PSObjectName("/quotedblleft");   //252
        vector[0253] = new PSObjectName("/guillemotleft");  //253
        vector[0254] = new PSObjectName("/guilsinglleft");  //254
        vector[0255] = new PSObjectName("/guilsinglright"); //255
        vector[0256] = new PSObjectName("/fi");          //256
        vector[0257] = new PSObjectName("/fl");          //257
        vector[0260] = EncodingManager.S_NOTDEF;      //260
        vector[0261] = new PSObjectName("/endash");      //261
        vector[0262] = new PSObjectName("/dagger");      //262
        vector[0263] = new PSObjectName("/daggerdbl");   //263
        vector[0264] = new PSObjectName("/periodcentered"); //264
        vector[0265] = EncodingManager.S_NOTDEF;      //265
        vector[0266] = new PSObjectName("/paragraph");   //266
        vector[0267] = new PSObjectName("/bullet");      //267
        vector[0270] = new PSObjectName("/quotesinglbase"); //270
        vector[0271] = new PSObjectName("/quotedblbase");   //271
        vector[0272] = new PSObjectName("/quotedblright");  //272
        vector[0273] = new PSObjectName("/guillemotright"); //273
        vector[0274] = new PSObjectName("/ellipsis");    //274
        vector[0275] = new PSObjectName("/perthousand"); //275        
        vector[0276] = EncodingManager.S_NOTDEF;      //276        
        vector[0277] = new PSObjectName("/questiondown"); //277
        vector[0300] = EncodingManager.S_NOTDEF;      //300
        vector[0301] = new PSObjectName("/grave");       //301
        vector[0302] = new PSObjectName("/acute");       //302
        vector[0303] = new PSObjectName("/circumflex");  //303
        vector[0304] = new PSObjectName("/tilde");       //304
        vector[0305] = new PSObjectName("/macron");      //305
        vector[0306] = new PSObjectName("/breve");       //306
        vector[0307] = new PSObjectName("/dotaccent");   //307
        vector[0310] = new PSObjectName("/dieresis");    //310
        vector[0311] = EncodingManager.S_NOTDEF;      //311
        vector[0312] = new PSObjectName("/ring");        //312
        vector[0313] = new PSObjectName("/cedilla");     //313
        vector[0314] = EncodingManager.S_NOTDEF;      //314
        vector[0315] = new PSObjectName("/hungarumlaut"); //315
        vector[0316] = new PSObjectName("/ogonek");      //316
        vector[0317] = new PSObjectName("/caron");       //317
        vector[0320] = new PSObjectName("/emdash");      //320
        vector[0321] = EncodingManager.S_NOTDEF;      //321
        vector[0322] = EncodingManager.S_NOTDEF;      //322
        vector[0323] = EncodingManager.S_NOTDEF;      //323
        vector[0324] = EncodingManager.S_NOTDEF;      //324
        vector[0325] = EncodingManager.S_NOTDEF;      //325
        vector[0326] = EncodingManager.S_NOTDEF;      //326
        vector[0327] = EncodingManager.S_NOTDEF;      //327
        vector[0330] = EncodingManager.S_NOTDEF;      //330
        vector[0331] = EncodingManager.S_NOTDEF;      //331
        vector[0332] = EncodingManager.S_NOTDEF;      //332
        vector[0333] = EncodingManager.S_NOTDEF;      //333
        vector[0334] = EncodingManager.S_NOTDEF;      //334
        vector[0335] = EncodingManager.S_NOTDEF;      //335
        vector[0336] = EncodingManager.S_NOTDEF;      //336
        vector[0337] = EncodingManager.S_NOTDEF;      //337
        vector[0340] = EncodingManager.S_NOTDEF;      //340
        vector[0341] = new PSObjectName("/AE");          //341
        vector[0342] = EncodingManager.S_NOTDEF;      //342
        vector[0343] = new PSObjectName("/ordfeminine"); //343
        vector[0344] = EncodingManager.S_NOTDEF;      //344
        vector[0345] = EncodingManager.S_NOTDEF;      //345
        vector[0346] = EncodingManager.S_NOTDEF;      //346
        vector[0347] = EncodingManager.S_NOTDEF;      //347
        vector[0350] = new PSObjectName("/Lslash");      //350
        vector[0351] = new PSObjectName("/Oslash");      //351
        vector[0352] = new PSObjectName("/OE");          //352
        vector[0353] = new PSObjectName("/ordmasculine"); //353
        vector[0354] = EncodingManager.S_NOTDEF;      //354
        vector[0355] = EncodingManager.S_NOTDEF;      //355
        vector[0356] = EncodingManager.S_NOTDEF;      //356
        vector[0357] = EncodingManager.S_NOTDEF;      //357
        vector[0360] = EncodingManager.S_NOTDEF;      //360
        vector[0361] = new PSObjectName("/ae");          //361
        vector[0362] = EncodingManager.S_NOTDEF;      //362
        vector[0363] = EncodingManager.S_NOTDEF;      //363
        vector[0364] = EncodingManager.S_NOTDEF;      //364
        vector[0365] = new PSObjectName("/dotlessi");    //365
        vector[0366] = EncodingManager.S_NOTDEF;      //366
        vector[0367] = EncodingManager.S_NOTDEF;      //367
        vector[0370] = new PSObjectName("/lslash");      //370
        vector[0371] = new PSObjectName("/oslash");      //371
        vector[0372] = new PSObjectName("/oe");          //372
        vector[0373] = new PSObjectName("/germandbls");  //373
        vector[0374] = EncodingManager.S_NOTDEF;      //374
        vector[0375] = EncodingManager.S_NOTDEF;      //375
        vector[0376] = EncodingManager.S_NOTDEF;      //376
        vector[0377] = EncodingManager.S_NOTDEF;      //377
        
        return vector;
    }
}
