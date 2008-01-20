============================================================================
                              Eps2pgf @VERSION@
============================================================================

Website: http://sourceforge.net/projects/eps2pgf
Author: Paul Wagenaars <paul@wagenaars.org>

----------------------------------------------------------------------------
Introduction
----------------------------------------------------------------------------
Convert Encapsulated PostScript (EPS) graphics files to the Portable
Graphics Format (PGF, http://sourceforge.net/projects/pgf/) for inclusion in
LaTeX documents. Texts in the figure are typeset by LaTeX

The goal of Eps2pgf is to support all PostScript figures created by programs
that used regularly by LaTeX users to create figures. That means that
Eps2pgf does not necessarily support every possible PostScript operator, but
you should never encounter a figure that Eps2pgf can not convert. If you
encounter a figure that the latest version of Eps2pgf fails to process,
please report it using the bug tracker:

  http://sourceforge.net/tracker/?group_id=188852&atid=926973
  
or send it to me via email so that I can improve Eps2pgf.
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
See the files NOTICE.txt and LICENSE.txt. Or run Eps2pgf with the command
line option '--version'.
----------------------------------------------------------------------------

----------------------------------------------------------------------------
Changelog
----------------------------------------------------------------------------
v0.6.0 (2007-12-??)
  - Added: PSfrag emulation
  - Added: support for embedded (non-bitmapped) type 3 fonts
  - Added: output device (LOLDevice) that writes only the text labels
  - Fixed: bug #1809102 (partially) Problems with Inkscape figure with text.
  - Fixed: bug #1807713 In some situations Eps2pgf produced lines that are
           too long. 
  - Fixed: bug #1874016 divison by zero in multiplication operator

v0.5.0 (2007-11-04)
  - Added: handling of binary headers found in some eps files
  - Added: support for embedded fonts
  - Added: special support for embedded Mathematica fonts (not yet complete)

v0.4.0 (2007-08-04)
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
