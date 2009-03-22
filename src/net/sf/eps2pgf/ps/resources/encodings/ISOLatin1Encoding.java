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
public final class ISOLatin1Encoding {
    
    /** Name of this encoding. */
    public static final PSObjectName NAME =
        new PSObjectName("/ISOLatin1Encoding");
    
    /**
     * "Hidden" constructor.
     */
    private ISOLatin1Encoding() {
        /* empty block */
    }
    
    /**
     * Gets a copy of the encoding vector.
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
        vector[0177] = EncodingManager.S_NOTDEF;     //177
        vector[0200] = EncodingManager.S_NOTDEF;     //200
        vector[0201] = EncodingManager.S_NOTDEF;     //201
        vector[0202] = EncodingManager.S_NOTDEF;     //202
        vector[0203] = EncodingManager.S_NOTDEF;     //203
        vector[0204] = EncodingManager.S_NOTDEF;     //204
        vector[0205] = EncodingManager.S_NOTDEF;     //205
        vector[0206] = EncodingManager.S_NOTDEF;     //206
        vector[0207] = EncodingManager.S_NOTDEF;     //207
        vector[0210] = EncodingManager.S_NOTDEF;     //210
        vector[0211] = EncodingManager.S_NOTDEF;     //211
        vector[0212] = EncodingManager.S_NOTDEF;     //212
        vector[0213] = EncodingManager.S_NOTDEF;     //213
        vector[0214] = EncodingManager.S_NOTDEF;     //214
        vector[0215] = EncodingManager.S_NOTDEF;     //215
        vector[0216] = EncodingManager.S_NOTDEF;     //216
        vector[0217] = EncodingManager.S_NOTDEF;     //217
        vector[0220] = new PSObjectName("/dotlessi");    //220
        vector[0221] = new PSObjectName("/grave");       //221
        vector[0222] = new PSObjectName("/acute");       //222
        vector[0223] = new PSObjectName("/circumflex");  //223
        vector[0224] = new PSObjectName("/tilde");       //224
        vector[0225] = new PSObjectName("/macron");      //225
        vector[0226] = new PSObjectName("/breve");       //226
        vector[0227] = new PSObjectName("/dotaccent");   //227
        vector[0230] = new PSObjectName("/dieresis");    //230
        vector[0231] = EncodingManager.S_NOTDEF;     //231
        vector[0232] = new PSObjectName("/ring");        //232
        vector[0233] = new PSObjectName("/cedilla");     //233
        vector[0234] = EncodingManager.S_NOTDEF;     //234
        vector[0235] = new PSObjectName("/hungarumlaut"); //235
        vector[0236] = new PSObjectName("/ogonek");      //236
        vector[0237] = new PSObjectName("/caron");       //237
        vector[0240] = new PSObjectName("/space");       //240
        vector[0241] = new PSObjectName("/exclamdown");  //241
        vector[0242] = new PSObjectName("/cent");        //242
        vector[0243] = new PSObjectName("/sterling");    //243
        vector[0244] = new PSObjectName("/currency");    //244
        vector[0245] = new PSObjectName("/yen");         //245
        vector[0246] = new PSObjectName("/brokenbar");   //246
        vector[0247] = new PSObjectName("/section");     //247
        vector[0250] = new PSObjectName("/dieresis");    //250
        vector[0251] = new PSObjectName("/copyright");   //251
        vector[0252] = new PSObjectName("/ordfeminine"); //252
        vector[0253] = new PSObjectName("/guillemotleft"); //253
        vector[0254] = new PSObjectName("/logicalnot");  //254
        vector[0255] = new PSObjectName("/hyphen");      //255
        vector[0256] = new PSObjectName("/registered");  //256
        vector[0257] = new PSObjectName("/macron");      //257
        vector[0260] = new PSObjectName("/degree");      //260
        vector[0261] = new PSObjectName("/plusminus");   //261
        vector[0262] = new PSObjectName("/twosuperior"); //262
        vector[0263] = new PSObjectName("/threesuperior"); //263
        vector[0264] = new PSObjectName("/acute");       //264
        vector[0265] = new PSObjectName("/mu");          //265
        vector[0266] = new PSObjectName("/paragraph");   //266
        vector[0267] = new PSObjectName("/periodcentered"); //267
        vector[0270] = new PSObjectName("/cedilla");     //270
        vector[0271] = new PSObjectName("/onesuperior"); //271
        vector[0272] = new PSObjectName("/ordmasculine"); //272
        vector[0273] = new PSObjectName("/guillemotright"); //273
        vector[0274] = new PSObjectName("/onequarter");  //274
        vector[0275] = new PSObjectName("/onehalf");     //275        
        vector[0276] = new PSObjectName("/threequarters"); //276        
        vector[0277] = new PSObjectName("/questiondown"); //277
        vector[0300] = new PSObjectName("/Agrave");      //300
        vector[0301] = new PSObjectName("/Aacute");      //301
        vector[0302] = new PSObjectName("/Acircumflex"); //302
        vector[0303] = new PSObjectName("/Atilde");      //303
        vector[0304] = new PSObjectName("/Adieresis");   //304
        vector[0305] = new PSObjectName("/Aring");       //305
        vector[0306] = new PSObjectName("/AE");          //306
        vector[0307] = new PSObjectName("/Ccedilla");    //307
        vector[0310] = new PSObjectName("/Egrave");      //310
        vector[0311] = new PSObjectName("/Eacute");      //311
        vector[0312] = new PSObjectName("/Ecircumflex"); //312
        vector[0313] = new PSObjectName("/Edieresis");   //313
        vector[0314] = new PSObjectName("/Igrave");      //314
        vector[0315] = new PSObjectName("/Iacute");      //315
        vector[0316] = new PSObjectName("/Icircumflex"); //316
        vector[0317] = new PSObjectName("/Idieresis");   //317
        vector[0320] = new PSObjectName("/Eth");         //320
        vector[0321] = new PSObjectName("/Ntilde");      //321
        vector[0322] = new PSObjectName("/Ograve");      //322
        vector[0323] = new PSObjectName("/Oacute");      //323
        vector[0324] = new PSObjectName("/Ocircumflex"); //324
        vector[0325] = new PSObjectName("/Otilde");      //325
        vector[0326] = new PSObjectName("/Odieresis");   //326
        vector[0327] = new PSObjectName("/multiply");    //327
        vector[0330] = new PSObjectName("/Oslash");      //330
        vector[0331] = new PSObjectName("/Ugrave");      //331
        vector[0332] = new PSObjectName("/Uacute");      //332
        vector[0333] = new PSObjectName("/Ucircumflex"); //333
        vector[0334] = new PSObjectName("/Udieresis");   //334
        vector[0335] = new PSObjectName("/Yacute");      //335
        vector[0336] = new PSObjectName("/Thorn");       //336
        vector[0337] = new PSObjectName("/germandbls");  //337
        vector[0340] = new PSObjectName("/agrave");      //340
        vector[0341] = new PSObjectName("/aacute");      //341
        vector[0342] = new PSObjectName("/acircumflex"); //342
        vector[0343] = new PSObjectName("/atilde");      //343
        vector[0344] = new PSObjectName("/adieresis");   //344
        vector[0345] = new PSObjectName("/aring");       //345
        vector[0346] = new PSObjectName("/ae");          //346
        vector[0347] = new PSObjectName("/ccedilla");    //347
        vector[0350] = new PSObjectName("/egrave");      //350
        vector[0351] = new PSObjectName("/eacute");      //351
        vector[0352] = new PSObjectName("/ecircumflex"); //352
        vector[0353] = new PSObjectName("/edieresis");   //353
        vector[0354] = new PSObjectName("/igrave");      //354
        vector[0355] = new PSObjectName("/iacute");      //355
        vector[0356] = new PSObjectName("/icircumflex"); //356
        vector[0357] = new PSObjectName("/idieresis");   //357
        vector[0360] = new PSObjectName("/eth");         //360
        vector[0361] = new PSObjectName("/ntilde");      //361
        vector[0362] = new PSObjectName("/ograve");      //362
        vector[0363] = new PSObjectName("/oacute");      //363
        vector[0364] = new PSObjectName("/ocircumflex"); //364
        vector[0365] = new PSObjectName("/otilde");      //365
        vector[0366] = new PSObjectName("/odieresis");   //366
        vector[0367] = new PSObjectName("/divide");      //367
        vector[0370] = new PSObjectName("/oslash");      //370
        vector[0371] = new PSObjectName("/ugrave");      //371
        vector[0372] = new PSObjectName("/uacute");      //372
        vector[0373] = new PSObjectName("/ucircumflex"); //373
        vector[0374] = new PSObjectName("/udieresis");   //374
        vector[0375] = new PSObjectName("/yacute");      //375
        vector[0376] = new PSObjectName("/thorn");       //376
        vector[0377] = new PSObjectName("/ydieresis");   //377
        
        return vector;
    }
}
