============================================================================
                              Eps2pgf v0.4.0
============================================================================

Website: http://sourceforge.net/projects/eps2pgf
Author: Paul Wagenaars <pwagenaars@fastmail.fm>

----------------------------------------------------------------------------
Introduction
----------------------------------------------------------------------------
Convert Encapsulated PostScript (EPS) graphics files to the Portable
Graphics Format (PGF, http://sourceforge.net/projects/pgf/) for inclusion in
LaTeX documents. Texts in the figure are typeset by LaTeX
----------------------------------------------------------------------------

----------------------------------------------------------------------------
Usage
----------------------------------------------------------------------------
Run Eps2pgf (e.g. java -jar eps2pgf.jar) with the --help option to get
information on how to convert an EPS file to PGF. After the PGF file has
been created it can be included in a LaTeX document. A minimal example LaTeX
document is listed below.

\documentclass{article}

\usepackage{pgf}

\begin{document}
  \begin{figure}
    \centering
    \input{figure.pgf}
    \caption{Figure created by Eps2pgf}
  \end{figure}	
\end{document}
----------------------------------------------------------------------------

----------------------------------------------------------------------------
Copyright and License
----------------------------------------------------------------------------
Copyright 2007 Paul Wagenaars <pwagenaars@fastmail.fm>

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License. 

-----
Third-party components distributed with Eps2pgf may be covered by a different
license. The license of each component is distributed along with it.
  - 14 Core PostScript AFM files (see resources\afm\MustRead.html)
  - FontBox (see lib\LICENSE_FontBox.txt)
  - JSAP (see lib\LICENSE_JSAP.txt)

----------------------------------------------------------------------------

----------------------------------------------------------------------------
Changelog
----------------------------------------------------------------------------
v0.4.0 (2007-07-??)
  - Changed: Eps2pgf is now distributed under the Apache license
  - Added: lots of PostScript commands
  - Added: Font substitution list

v0.3.0 (2007-05-28)
  - Added: tons of PostScript commands
  - Added: automated test scripts

v0.2.0 (2007-04-06)
  - Added: added several PostScript commands
  - Added: radial shadings 

v0.1.0 (2007-03-11)
  - Initial release
