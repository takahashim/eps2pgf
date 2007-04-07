============================================================================
                              Eps2pgf v0.2.0
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
License
----------------------------------------------------------------------------
Eps2pgf is free software; you can redistribute it and/or modify it under
the terms of version 2 of the GNU General Public License as published by the
Free Software Foundation.

Eps2pgf is distributed in the hope that it will be useful, but WITHOUT ANY
WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
details.

You should have received a copy of the GNU General Public License along with
this program; if not, write to the Free Software Foundation, Inc.,
51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA. 
----------------------------------------------------------------------------

----------------------------------------------------------------------------
Changelog
----------------------------------------------------------------------------
v0.2.0 (2007-04-06)
  - Added: added several PostScript commands
  - Added: radial shadings 

v0.1.0 (2007-03-11)
  - Initial release
