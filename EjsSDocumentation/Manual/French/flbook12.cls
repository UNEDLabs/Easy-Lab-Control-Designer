%%text width increased to 27p6, gutter reduced to 4p6 5/24/02

%% 9/27/02
%%new specs for running heads
%%drop on chapter openers
%%part numbering
%%8pt running heads
%%9pt captions
%%index turnovers
%%automatic "empty" blank pages
%%double rule on title page
%%chapter entry in TOC; double digit spacing

%% 10/15/02
%%clo inserted into class file


\NeedsTeXFormat{LaTeX2e}[1995/12/01]
\ProvidesClass{flbook}
              [2002/010/15 v1.2 Princeton University Press class]
%_%_%_%_%_%
\def\crops{\vbox to 10.25in{\hsize7.125in\parindent0pt%
\noindent\hbox to .5in{\vrule width .25in height .4pt depth 0pt\hfil \vrule height .5in depth -.25in}\hfill%
\raisebox{18pt}[0pt][0pt]{\jobname\quad\today}%
\hfill\hbox to .5in{\vrule height .5in depth -.25in\hfil\vrule width .25in height .4pt depth 0pt}%
\vfill

\noindent\hbox to .5in{\vrule width .25in height .5in depth -35.6pt\hfil \vrule height .25in depth 0in}\hfill%
\hbox to .5in{\vrule height .25in depth 0in\hfil \vrule width .25in height .5in depth -35.6pt}}}
%_%_%_%_%
%
%_%_%_%_% remove theorem numbering from class???
\newif\if@mytheorems
\@mytheoremsfalse
%
\def\cleardoublepage{\clearpage\if@twoside \ifodd\c@page\else
    \hbox{}\thispagestyle{empty}\newpage\if@twocolumn\hbox{}\newpage\fi\fi\fi}
%
\setlength{\overfullrule}{5pt}
\newcommand\@ptsize{}
\newif\if@restonecol
\newif\if@titlepage
\@titlepagetrue
\newif\if@openright
\newif\if@mainmatter \@mainmattertrue
\if@compatibility\else
\DeclareOption{a4paper}
   {\setlength\paperheight {297mm}%
    \setlength\paperwidth  {210mm}}
\DeclareOption{a5paper}
   {\setlength\paperheight {210mm}%
    \setlength\paperwidth  {148mm}}
\DeclareOption{b5paper}
   {\setlength\paperheight {250mm}%
    \setlength\paperwidth  {176mm}}
\DeclareOption{letterpaper}
   {\setlength\paperheight {11in}%
    \setlength\paperwidth  {8.5in}}
\DeclareOption{legalpaper}
   {\setlength\paperheight {14in}%
    \setlength\paperwidth  {8.5in}}
\DeclareOption{executivepaper}
   {\setlength\paperheight {10.5in}%
    \setlength\paperwidth  {7.25in}}
\DeclareOption{landscape}
   {\setlength\@tempdima   {\paperheight}%
    \setlength\paperheight {\paperwidth}%
    \setlength\paperwidth  {\@tempdima}}
\fi
\if@compatibility
  \renewcommand\@ptsize{0}
\else
\DeclareOption{10pt}{\renewcommand\@ptsize{0}}
\fi
\DeclareOption{11pt}{\renewcommand\@ptsize{1}}
\if@compatibility\else
\DeclareOption{oneside}{\@twosidefalse \@mparswitchfalse}
\fi
\DeclareOption{twoside}{\@twosidetrue  \@mparswitchtrue}
\DeclareOption{draft}{\setlength\overfullrule{5pt}}
\if@compatibility\else
\DeclareOption{final}{\setlength\overfullrule{0pt}}
\fi
\DeclareOption{titlepage}{\@titlepagetrue}
\if@compatibility\else
\DeclareOption{notitlepage}{\@titlepagefalse}
\fi
\if@compatibility
\@openrighttrue
\else
\DeclareOption{openright}{\@openrighttrue}
\DeclareOption{openany}{\@openrightfalse}
\fi
\if@compatibility\else
\DeclareOption{onecolumn}{\@twocolumnfalse}
\fi
\DeclareOption{twocolumn}{\@twocolumntrue}
\DeclareOption{leqno}{\input{leqno.clo}}
\DeclareOption{fleqn}{\input{fleqn.clo}}
\DeclareOption{openbib}{%
  \AtEndOfPackage{%
   \renewcommand\@openbib@code{%
      \advance\leftmargin\bibindent
      \itemindent -\bibindent
      \listparindent \itemindent
      \parsep \z@
      }%
   \renewcommand\newblock{\par}}%
}
%
\DeclareOption{mytheorems}{\@mytheoremstrue}
%
\ExecuteOptions{letterpaper,10pt,twoside,onecolumn,openright}
\ProcessOptions
\newcommand{\@testo}{0}%
\ifx\@ptsize\@testo%_%_%_%_%
\renewcommand\normalsize{%
   \@setfontsize\normalsize\@xpt\@xiipt
   \abovedisplayskip 6\p@ \@plus2\p@ \@minus5\p@
   \abovedisplayshortskip \abovedisplayskip
   \belowdisplayshortskip \abovedisplayskip
   \belowdisplayskip \abovedisplayskip
   \let\@listi\@listI}
\normalsize
\newcommand\small{%
   \@setfontsize\small\@ixpt{11}%
   \abovedisplayskip 8.5\p@ \@plus3\p@ \@minus4\p@
   \abovedisplayshortskip \z@ \@plus2\p@
   \belowdisplayshortskip 4\p@ \@plus2\p@ \@minus2\p@
   \def\@listi{\leftmargin\leftmargini
               \topsep 4\p@ \@plus2\p@ \@minus2\p@
               \parsep 2\p@ \@plus\p@ \@minus\p@
               \itemsep \parsep}%
   \belowdisplayskip \abovedisplayskip
}
\newcommand\footnotesize{%
   \@setfontsize\footnotesize\@viiipt{9.5}%
   \abovedisplayskip 6\p@ \@plus2\p@ \@minus4\p@
   \abovedisplayshortskip \z@ \@plus\p@
   \belowdisplayshortskip 3\p@ \@plus\p@ \@minus2\p@
   \def\@listi{\leftmargin\leftmargini
               \topsep 3\p@ \@plus\p@ \@minus\p@
               \parsep 2\p@ \@plus\p@ \@minus\p@
               \itemsep \parsep}%
   \belowdisplayskip \abovedisplayskip
}
\newcommand\scriptsize{\@setfontsize\scriptsize\@viipt\@viiipt}
\newcommand\tiny{\@setfontsize\tiny\@vpt\@vipt}
\newcommand\large{\@setfontsize\large\@xiipt{14}}
\newcommand\Large{\@setfontsize\Large\@xivpt{18}}
\newcommand\LARGE{\@setfontsize\LARGE\@xviipt{22}}
\newcommand\huge{\@setfontsize\huge\@xxpt{25}}
\newcommand\Huge{\@setfontsize\Huge\@xxvpt{30}}
\if@twocolumn
  \setlength\parindent{1em}
\else
  \setlength\parindent{15\p@}
\fi
\setlength\smallskipamount{3\p@ \@plus 1\p@ \@minus 1\p@}
\setlength\medskipamount{6\p@ \@plus 2\p@ \@minus 2\p@}
\setlength\bigskipamount{12\p@ \@plus 4\p@ \@minus 4\p@}
\setlength\headheight{12\p@}
\setlength\headsep   {.25in}
\setlength\topskip   {10\p@}
\setlength\footskip{.35in}
\if@compatibility \setlength\maxdepth{4\p@} \else
\setlength\maxdepth{.5\topskip} \fi
\if@compatibility
  \if@twocolumn
    \setlength\textwidth{410\p@}
  \else
    \setlength\textwidth{4.5in}
  \fi
\else
  \setlength\@tempdima{\paperwidth}
  \addtolength\@tempdima{-2in}
  \setlength\@tempdimb{345\p@}
  \if@twocolumn
    \ifdim\@tempdima>2\@tempdimb\relax
      \setlength\textwidth{2\@tempdimb}
    \else
      \setlength\textwidth{\@tempdima}
    \fi
  \else
    \ifdim\@tempdima>\@tempdimb\relax
      \setlength\textwidth{\@tempdimb}
    \else
      \setlength\textwidth{\@tempdima}
    \fi
  \fi
\fi
\if@compatibility\else
  \@settopoint\textwidth
\fi
\if@compatibility
  \setlength\textheight{41\baselineskip}
\else
  \setlength\@tempdima{\paperheight}
  \addtolength\@tempdima{-2in}
  \addtolength\@tempdima{-1.5in}
  \divide\@tempdima\baselineskip
  \@tempcnta=\@tempdima
  \setlength\textheight{\@tempcnta\baselineskip}
\fi
\addtolength\textheight{\topskip}
\if@twocolumn
 \setlength\marginparsep {10\p@}
\else
  \setlength\marginparsep{7\p@}
\fi
\setlength\marginparpush{5\p@}
\if@compatibility
   \setlength\oddsidemargin   {.5in}
   \setlength\evensidemargin  {1.5in}
   \setlength\marginparwidth {.75in}
  \if@twocolumn
     \setlength\oddsidemargin  {30\p@}
     \setlength\evensidemargin {30\p@}
     \setlength\marginparwidth {48\p@}
  \fi
\else
  \if@twoside
    \setlength\@tempdima        {\paperwidth}
    \addtolength\@tempdima      {-\textwidth}
    \setlength\oddsidemargin    {.4\@tempdima}
    \addtolength\oddsidemargin  {-1in}
    \setlength\marginparwidth   {.6\@tempdima}
    \addtolength\marginparwidth {-\marginparsep}
    \addtolength\marginparwidth {-0.4in}
  \else
    \setlength\@tempdima        {\paperwidth}
    \addtolength\@tempdima      {-\textwidth}
    \setlength\oddsidemargin    {.5\@tempdima}
    \addtolength\oddsidemargin  {-1in}
    \setlength\marginparwidth   {.5\@tempdima}
    \addtolength\marginparwidth {-\marginparsep}
    \addtolength\marginparwidth {-0.4in}
    \addtolength\marginparwidth {-.4in}
  \fi
  \ifdim \marginparwidth >2in
     \setlength\marginparwidth{2in}
  \fi
  \@settopoint\oddsidemargin
  \@settopoint\marginparwidth
  \setlength\evensidemargin  {\paperwidth}
  \addtolength\evensidemargin{-2in}
  \addtolength\evensidemargin{-\textwidth}
  \addtolength\evensidemargin{-\oddsidemargin}
  \@settopoint\evensidemargin
\fi
\if@compatibility
  \setlength\topmargin{.75in}
\else
  \setlength\topmargin{\paperheight}
  \addtolength\topmargin{-2in}
  \addtolength\topmargin{-\headheight}
  \addtolength\topmargin{-\headsep}
  \addtolength\topmargin{-\textheight}
  \addtolength\topmargin{-\footskip}     % this might be wrong!
  \addtolength\topmargin{-.5\topmargin}
  \@settopoint\topmargin
\fi
\setlength\footnotesep{6.65\p@}
\setlength{\skip\footins}{9\p@ \@plus 4\p@ \@minus 2\p@}
\setlength\floatsep    {12\p@ \@plus 2\p@ \@minus 2\p@}
\setlength\textfloatsep{20\p@ \@plus 2\p@ \@minus 4\p@}
\setlength\intextsep   {12\p@ \@plus 2\p@ \@minus 2\p@}
\setlength\dblfloatsep    {12\p@ \@plus 2\p@ \@minus 2\p@}
\setlength\dbltextfloatsep{20\p@ \@plus 2\p@ \@minus 4\p@}
\setlength\@fptop{0\p@ \@plus 1fil}
\setlength\@fpsep{8\p@ \@plus 2fil}
\setlength\@fpbot{0\p@ \@plus 1fil}
\setlength\@dblfptop{0\p@ \@plus 1fil}
\setlength\@dblfpsep{8\p@ \@plus 2fil}
\setlength\@dblfpbot{0\p@ \@plus 1fil}
\setlength\partopsep{2\p@ \@plus 1\p@ \@minus 1\p@}
\def\@listi{\leftmargin\leftmargini
            \parsep 4\p@ \@plus2\p@ \@minus\p@
            \topsep 8\p@ \@plus2\p@ \@minus4\p@
            \itemsep4\p@ \@plus2\p@ \@minus\p@}
\let\@listI\@listi
\@listi
\def\@listii {\leftmargin\leftmarginii
              \labelwidth\leftmarginii
              \advance\labelwidth-\labelsep
              \topsep    4\p@ \@plus2\p@ \@minus\p@
              \parsep    2\p@ \@plus\p@  \@minus\p@
              \itemsep   \parsep}
\def\@listiii{\leftmargin\leftmarginiii
              \labelwidth\leftmarginiii
              \advance\labelwidth-\labelsep
              \topsep    2\p@ \@plus\p@\@minus\p@
              \parsep    \z@
              \partopsep \p@ \@plus\z@ \@minus\p@
              \itemsep   \topsep}
\def\@listiv {\leftmargin\leftmarginiv
              \labelwidth\leftmarginiv
              \advance\labelwidth-\labelsep}
\def\@listv  {\leftmargin\leftmarginv
              \labelwidth\leftmarginv
              \advance\labelwidth-\labelsep}
\def\@listvi {\leftmargin\leftmarginvi
              \labelwidth\leftmarginvi
              \advance\labelwidth-\labelsep}

\newlength{\chapterdrop}
\setlength{\chapterdrop}{8.5pt}

\newlength{\schapterdrop}
\setlength{\schapterdrop}{6.34pt}

\newlength{\partdrop}
\setlength{\partdrop}{0pt}
\else%_%_%_%_%
\renewcommand\normalsize{%
   \@setfontsize\normalsize\@xipt{13}%
   \abovedisplayskip 9\p@ \@plus3\p@ \@minus3\p@
   \abovedisplayshortskip \abovedisplayskip
   \belowdisplayshortskip \abovedisplayskip
   \belowdisplayskip \abovedisplayskip
   \let\@listi\@listI}
\normalsize
\newcommand\small{%
   \@setfontsize\small\@xpt\@xiipt
   \abovedisplayskip 10\p@ \@plus2\p@ \@minus5\p@
   \abovedisplayshortskip \z@ \@plus3\p@
   \belowdisplayshortskip 6\p@ \@plus3\p@ \@minus3\p@
   \def\@listi{\leftmargin\leftmargini
               \topsep 6\p@ \@plus2\p@ \@minus2\p@
               \parsep 3\p@ \@plus2\p@ \@minus\p@
               \itemsep \parsep}%
   \belowdisplayskip \abovedisplayskip
}
\newcommand\footnotesize{%
   \@setfontsize\footnotesize\@ixpt{11}%
   \abovedisplayskip 8\p@ \@plus2\p@ \@minus4\p@
   \abovedisplayshortskip \z@ \@plus\p@
   \belowdisplayshortskip 4\p@ \@plus2\p@ \@minus2\p@
   \def\@listi{\leftmargin\leftmargini
               \topsep 4\p@ \@plus2\p@ \@minus2\p@
               \parsep 2\p@ \@plus\p@ \@minus\p@
               \itemsep \parsep}%
   \belowdisplayskip \abovedisplayskip
}
\newcommand\scriptsize{\@setfontsize\scriptsize\@viiipt{9.5}}
\newcommand\tiny{\@setfontsize\tiny\@vipt\@viipt}
\newcommand\large{\@setfontsize\large\@xiipt{14}}
\newcommand\Large{\@setfontsize\Large\@xivpt{18}}
\newcommand\LARGE{\@setfontsize\LARGE\@xviipt{22}}
\newcommand\huge{\@setfontsize\huge\@xxpt{25}}
\newcommand\Huge{\@setfontsize\Huge\@xxvpt{30}}
\if@twocolumn
  \setlength\parindent{1em}
\else
  \setlength\parindent{17\p@}
\fi
\setlength\smallskipamount{3\p@ \@plus 1\p@ \@minus 1\p@}
\setlength\medskipamount{6\p@ \@plus 2\p@ \@minus 2\p@}
\setlength\bigskipamount{12\p@ \@plus 4\p@ \@minus 4\p@}
\setlength\headheight{12\p@}
\setlength\headsep   {.275in}
\setlength\topskip   {11\p@}
\setlength\footskip{.38in}
\if@compatibility \setlength\maxdepth{4\p@} \else
\setlength\maxdepth{.5\topskip} \fi
\if@compatibility
  \if@twocolumn
    \setlength\textwidth{410\p@}
  \else
    \setlength\textwidth{5in}
  \fi
\else
  \setlength\@tempdima{\paperwidth}
  \addtolength\@tempdima{-2in}
  \setlength\@tempdimb{360\p@}
  \if@twocolumn
    \ifdim\@tempdima>2\@tempdimb\relax
      \setlength\textwidth{2\@tempdimb}
    \else
      \setlength\textwidth{\@tempdima}
    \fi
  \else
    \ifdim\@tempdima>\@tempdimb\relax
      \setlength\textwidth{\@tempdimb}
    \else
      \setlength\textwidth{\@tempdima}
    \fi
  \fi
\fi
\if@compatibility\else
  \@settopoint\textwidth
\fi
\if@compatibility
  \setlength\textheight{38\baselineskip}
\else
  \setlength\@tempdima{\paperheight}
  \addtolength\@tempdima{-2in}
  \addtolength\@tempdima{-1.5in}
  \divide\@tempdima\baselineskip
  \@tempcnta=\@tempdima
  \setlength\textheight{\@tempcnta\baselineskip}
\fi
\addtolength\textheight{\topskip}
\if@twocolumn
 \setlength\marginparsep {10\p@}
\else
  \setlength\marginparsep{7\p@}
\fi
\setlength\marginparpush{5\p@}
\if@compatibility
   \setlength\oddsidemargin   {.25in}
   \setlength\evensidemargin  {1.25in}
   \setlength\marginparwidth {1in}
  \if@twocolumn
     \setlength\oddsidemargin  {30\p@}
     \setlength\evensidemargin {30\p@}
     \setlength\marginparwidth {48\p@}
  \fi
\else
  \if@twoside
    \setlength\@tempdima        {\paperwidth}
    \addtolength\@tempdima      {-\textwidth}
    \setlength\oddsidemargin    {.4\@tempdima}
    \addtolength\oddsidemargin  {-1in}
    \setlength\marginparwidth   {.6\@tempdima}
    \addtolength\marginparwidth {-\marginparsep}
    \addtolength\marginparwidth {-0.4in}
  \else
    \setlength\@tempdima        {\paperwidth}
    \addtolength\@tempdima      {-\textwidth}
    \setlength\oddsidemargin    {.5\@tempdima}
    \addtolength\oddsidemargin  {-1in}
    \setlength\marginparwidth   {.5\@tempdima}
    \addtolength\marginparwidth {-\marginparsep}
    \addtolength\marginparwidth {-0.4in}
    \addtolength\marginparwidth {-.4in}
  \fi
  \ifdim \marginparwidth >2in
     \setlength\marginparwidth{2in}
  \fi
  \@settopoint\oddsidemargin
  \@settopoint\marginparwidth
  \setlength\evensidemargin  {\paperwidth}
  \addtolength\evensidemargin{-2in}
  \addtolength\evensidemargin{-\textwidth}
  \addtolength\evensidemargin{-\oddsidemargin}
  \@settopoint\evensidemargin
\fi
\if@compatibility
  \setlength\topmargin{.73in}
\else
  \setlength\topmargin{\paperheight}
  \addtolength\topmargin{-2in}
  \addtolength\topmargin{-\headheight}
  \addtolength\topmargin{-\headsep}
  \addtolength\topmargin{-\textheight}
  \addtolength\topmargin{-\footskip}     % this might be wrong!
  \addtolength\topmargin{-.5\topmargin}
  \@settopoint\topmargin
\fi
\setlength\footnotesep{7.7\p@}
\setlength{\skip\footins}{10\p@ \@plus 4\p@ \@minus 2\p@}
\setlength\floatsep    {12\p@ \@plus 2\p@ \@minus 2\p@}
\setlength\textfloatsep{20\p@ \@plus 2\p@ \@minus 4\p@}
\setlength\intextsep   {12\p@ \@plus 2\p@ \@minus 2\p@}
\setlength\dblfloatsep    {12\p@ \@plus 2\p@ \@minus 2\p@}
\setlength\dbltextfloatsep{20\p@ \@plus 2\p@ \@minus 4\p@}
\setlength\@fptop{0\p@ \@plus 1fil}
\setlength\@fpsep{8\p@ \@plus 2fil}
\setlength\@fpbot{0\p@ \@plus 1fil}
\setlength\@dblfptop{0\p@ \@plus 1fil}
\setlength\@dblfpsep{8\p@ \@plus 2fil}
\setlength\@dblfpbot{0\p@ \@plus 1fil}
\setlength\partopsep{3\p@ \@plus 1\p@ \@minus 1\p@}
\def\@listi{\leftmargin\leftmargini
            \parsep 4.5\p@ \@plus2\p@ \@minus\p@
            \topsep 9\p@   \@plus3\p@ \@minus5\p@
            \itemsep4.5\p@ \@plus2\p@ \@minus\p@}
\let\@listI\@listi
\@listi
\def\@listii {\leftmargin\leftmarginii
              \labelwidth\leftmarginii
              \advance\labelwidth-\labelsep
              \topsep    4.5\p@ \@plus2\p@ \@minus\p@
              \parsep    2\p@   \@plus\p@  \@minus\p@
              \itemsep   \parsep}
\def\@listiii{\leftmargin\leftmarginiii
              \labelwidth\leftmarginiii
              \advance\labelwidth-\labelsep
              \topsep    2\p@ \@plus\p@\@minus\p@
              \parsep    \z@
              \partopsep \p@ \@plus\z@ \@minus\p@
              \itemsep   \topsep}
\def\@listiv {\leftmargin\leftmarginiv
              \labelwidth\leftmarginiv
              \advance\labelwidth-\labelsep}
\def\@listv  {\leftmargin\leftmarginv
              \labelwidth\leftmarginv
              \advance\labelwidth-\labelsep}
\def\@listvi {\leftmargin\leftmarginvi
              \labelwidth\leftmarginvi
              \advance\labelwidth-\labelsep}

\newlength{\chapterdrop}
\setlength{\chapterdrop}{12.2pt}

\newlength{\schapterdrop}
\setlength{\schapterdrop}{10.3pt}

\newlength{\partdrop}
\setlength{\partdrop}{-.22pc}
\fi%_%_%_%_%end of size options
\setlength\lineskip{1\p@}
\setlength\normallineskip{1\p@}
\renewcommand\baselinestretch{}
\setlength\parskip{0\p@ \@plus \p@}
\@lowpenalty   51
\@medpenalty  151
\@highpenalty 301
\setcounter{topnumber}{2}
\renewcommand\topfraction{.7}
\setcounter{bottomnumber}{1}
\renewcommand\bottomfraction{.3}
\setcounter{totalnumber}{3}
\renewcommand\textfraction{.2}
\renewcommand\floatpagefraction{.5}
\setcounter{dbltopnumber}{2}
\renewcommand\dbltopfraction{.7}
\renewcommand\dblfloatpagefraction{.5}
\def\ps@plain{\let\@mkboth\@gobbletwo
     \def\@oddhead{\begin{picture}(0,0)\put(-89.43,-650.85){\crops}\end{picture}\hfill}\def\@oddfoot{\reset@font\hfil\thepage
     \hfil}\def\@evenhead{\begin{picture}(0,0)\put(-95.43,-650.85){\crops}\end{picture}\hfill}\let\@evenfoot\@oddfoot}
\def\ps@empty{\let\@mkboth\@gobbletwo
     \def\@oddhead{\begin{picture}(0,0)\put(-89.43,-650.85){\crops}\end{picture}\hfill}\def\@oddfoot{\reset@font\hfil
     \hfil}\def\@evenhead{\begin{picture}(0,0)\put(-95.43,-650.85){\crops}\end{picture}\hfill}\let\@evenfoot\@oddfoot}

\if@twoside
  \def\ps@headings{%
      \let\@oddfoot\@empty\let\@evenfoot\@empty
      \def\@evenhead{\begin{picture}(0,0)\put(-95.33,-650.85){\crops}\end{picture}{\normalfont\fontsize{10}{10pt}\selectfont\sffamily\thepage}\hfil{\fontsize{8}{8pt}\selectfont\sffamily\leftmark}}%
      \def\@oddhead{\begin{picture}(0,0)\put(-89.43,-650.85){\crops}\end{picture}{\fontsize{8}{8pt}\selectfont\sffamily\rightmark}\hfil{\normalfont\fontsize{10}{10pt}\selectfont\sffamily\thepage}}%
      \let\@mkboth\markboth
    \def\chaptermark##1{%
      \markboth {\MakeUppercase{%
        \ifnum \c@secnumdepth >\m@ne
          \if@mainmatter
            \@chapapp\ \thechapter%
          \fi
        \fi
}}{}}%
    \def\sectionmark##1{%
      \markright {\MakeUppercase{%
        \ifnum \c@secnumdepth >\z@
 %
        \fi
        }}}}
\else
  \def\ps@headings{%
    \let\@oddfoot\@empty
    \def\@oddhead{{\slshape\rightmark}\hfil\thepage}%
    \let\@mkboth\markboth
    \def\chaptermark##1{%
      \markright {\MakeUppercase{%
        \ifnum \c@secnumdepth >\m@ne
          \if@mainmatter
            \@chapapp\ \thechapter. \ %
          \fi
        \fi
        ##1}}}}
\fi
%_%_%_%_% New commands...
\def\shorttitle#1{\gdef\@shorttitle{#1}}
\def\@shorttitle{}
\def\subtitle#1{\gdef\@subtitle{#1\vskip60pt}}
\def\@subtitle{}
%_%_%_%_%
\def\ps@myheadings{%
    \let\@oddfoot\@empty\let\@evenfoot\@empty
    \def\@evenhead{\thepage\hfil\slshape\leftmark}%
    \def\@oddhead{{\slshape\rightmark}\hfil\thepage}%
    \let\@mkboth\@gobbletwo
    \let\chaptermark\@gobble
    \let\sectionmark\@gobble
    }
  \if@titlepage
  \newcommand\maketitle{\begin{titlepage}%
  \let\footnotesize\small
  \let\footnoterule\relax
  \let \footnote \thanks
  \null\vfil\fontsize{10}{12pt}\selectfont
  \vspace*{-29pt}
  \begin{center}%
    {\sffamily\fontsize{20}{24pt}\selectfont \par
\rule{\textwidth}{2pt}\vspace*{-17.53pt}\par
\rule{\textwidth}{.5pt}\vspace{18pt}\par
\bgroup\raggedright\advance\leftskip30pt\advance\rightskip18pt
\@title\vspace{6pt}\par
\egroup
\rule{\textwidth}{.5pt} \par}%
   \end{center}   
\vskip22pt

\bgroup
\let \\\@centercr \@rightskip 18.0pt plus 1.0fil \rightskip \@rightskip \leftskip 
18pt \parindent \z@
\parindent0pt
{\fontsize{14}{16pt}\selectfont\sffamily\itshape\@subtitle\par}
\vskip12pt
    {\fontsize{20}{24pt}\selectfont
     
        \sffamily\@author
     \par}%
      \vskip 1.5em%


%\vspace{16.4pc}
\vfill


\fontsize{8}{16pt}\selectfont\sffamily
\noindent\hbox to 146pt{P\hss R\hss I\hss N\hss C\hss E\hss T\hss O\hss N\hspace*{7pt}U\hss N\hss I\hss V\hss E\hss R\hss S\hss I\hss T\hss Y\hspace{7pt}P\hss R\hss E\hss S\hss S}\\
\noindent\hbox to 120pt{P\hss R\hss I\hss N\hss C\hss E\hss T\hss O\hss N\hspace{7pt}A\hss N\hss D\hspace{7pt}O\hss X\hss F\hss O\hss R\hss D}

    {\par}%       % Set date in \large size.
\par
\egroup
  \@thanks
  \vfil\null
  \end{titlepage}%
  \setcounter{footnote}{0}%
  \global\let\thanks\relax
  \global\let\maketitle\relax
  \global\let\@thanks\@empty
  \global\let\@author\@empty
  \global\let\@date\@empty
  %\global\let\@title\@empty
  %\global\let\title\relax
  \global\let\author\relax
  \global\let\date\relax
  \global\let\and\relax
}
\else
\newcommand\maketitle{\par
  \begingroup
    \renewcommand\thefootnote{\@fnsymbol\c@footnote}%
    \def\@makefnmark{\rlap{\@textsuperscript{\normalfont\@thefnmark}}}%
    \long\def\@makefntext##1{\noindent%
{\@textsuperscript{\normalfont\@thefnmark}}##1}%
    \if@twocolumn
      \ifnum \col@number=\@ne
        \@maketitle
      \else
        \twocolumn[\@maketitle]%
      \fi
    \else
      \newpage
      \global\@topnum\z@   % Prevents figures from going at top of page.
      \@maketitle
    \fi
    \thispagestyle{empty}\@thanks
  \endgroup
  \setcounter{footnote}{0}%
  \global\let\thanks\relax
  \global\let\maketitle\relax
  \global\let\@maketitle\relax
  \global\let\@thanks\@empty
  \global\let\@author\@empty
  \global\let\@date\@empty
  \global\let\@title\@empty
  \global\let\title\relax
  \global\let\author\relax
  \global\let\date\relax
  \global\let\and\relax
}
\def\@maketitle{%
  \newpage
  \null
  \vskip 2em%
  \begin{center}%
  \let \footnote \thanks
    {\LARGE \@title \par}%
    \vskip 1.5em%
    {\large
      \lineskip .5em%
      \begin{tabular}[t]{c}%
        \@author
      \end{tabular}\par}%
    \vskip 1em%
    {\large \@date}%
  \end{center}%
  \par
  \vskip 1.5em}
\fi
\newcommand*\chaptermark[1]{}
\setcounter{secnumdepth}{3}
\newcounter {part}
\newcounter {chapter}
\newcounter {section}[chapter]
\newcounter {subsection}[section]
\newcounter {subsubsection}[subsection]
\newcounter {paragraph}[subsubsection]
\newcounter {subparagraph}[paragraph]
\renewcommand \thepart {\@arabic\c@part}
\renewcommand \thechapter {\@arabic\c@chapter}
\renewcommand \thesection {\thechapter.\@arabic\c@section}
\renewcommand\thesubsection   {\thesection.\@arabic\c@subsection}
\renewcommand\thesubsubsection{\thesubsection .\@arabic\c@subsubsection}
\renewcommand\theparagraph    {\thesubsubsection.\@arabic\c@paragraph}
\renewcommand\thesubparagraph {\theparagraph.\@arabic\c@subparagraph}
\newcommand\@chapapp{\chaptername}
\newcommand\frontmatter{%
    \cleardoublepage
  \@mainmatterfalse
  \pagenumbering{roman}}
\newcommand\mainmatter{%
    \cleardoublepage
  \@mainmattertrue
  \pagenumbering{arabic}}
\newcommand\backmatter{\@openrighttrue%
  \if@openright
    \cleardoublepage
  \else
    \clearpage
  \fi
  \@mainmatterfalse\appendixfalse}
\newcommand\part{\@openrighttrue%
  \if@openright
    \cleardoublepage
  \else
    \clearpage
  \fi
  \thispagestyle{empty}%
  \if@twocolumn
    \onecolumn
    \@tempswatrue
  \else
    \@tempswafalse
  \fi
  \null\vfil
  \secdef\@part\@spart}

\def\@part[#1]#2{%
    \ifnum \c@secnumdepth >-2\relax
      \refstepcounter{part}%
      \addcontentsline{toc}{part}{\thepart.\hspace{1em}#1}%
    \else
      \addcontentsline{toc}{part}{#1}%
    \fi
    \markboth{}{}%
    {\parindent0pt\advance\leftskip18pt\fontsize{10}{12pt}\selectfont\vspace*{\partdrop}
     \interlinepenalty \@M
       \ifnum \c@secnumdepth >-2\relax
       \sffamily\fontsize{14}{24pt}\selectfont \expandafter\MakeUppercase{\partname}~\thepart\rule{0pt}{5.67pc}
       \par
       \vskip -4\p@
     \fi
     \sffamily\fontsize{18}{28pt}\selectfont  #2\par}%
    \@endpart}
\def\@spart#1{%
    {\centering
     \interlinepenalty \@M
     \normalfont
     \Huge \bfseries #1\par}%
    \@endpart}
\def\@endpart{\vfill\newpage
              \if@twoside
                \null
                \thispagestyle{empty}%
                \newpage
              \fi
              \if@tempswa
                \twocolumn
              \fi\@openrightfalse}
\newcommand\chapter{\if@openright%\cleardoublepage
\clearpage\if@twoside \ifodd\c@page\else
    \hbox{}\thispagestyle{empty}\newpage\if@twocolumn\hbox{}\newpage\fi\fi\fi
\else\clearpage\fi
                    \thispagestyle{empty}%
                    \global\@topnum\z@
                    \@afterindentfalse
                    \secdef\@chapter\@schapter}
\def\@chapter[#1]#2{\ifnum \c@secnumdepth >\m@ne
                       \if@mainmatter
                         \refstepcounter{chapter}%
                         \typeout{\@chapapp\space\thechapter.}%
                         \addcontentsline{toc}{chapter}%
                                   {\protect\numberline{\thechapter.}#1}%
                       \else
                         \addcontentsline{toc}{chapter}{#1}%
                       \fi
                    \else
                      \addcontentsline{toc}{chapter}{#1}%
                    \fi
                    \chaptermark{#1}%
                    \addtocontents{lof}{\protect\addvspace{10\p@}}%
                    \addtocontents{lot}{\protect\addvspace{10\p@}}%
                    \if@twocolumn
                      \@topnewpage[\@makechapterhead{#2}]%
                    \else
                      \@makechapterhead{#2}%
                      \@afterheading
                    \fi}
\def\@makechapterhead#1{\def\rightmark{\uppercase{#1}}%%%%put the chapter title in right RH
  \vspace*{-\chapterdrop}%
  {\parindent 18pt \raggedright \parindent0pt\normalfont\fontsize{10}{12pt}\selectfont\advance\leftskip18pt
    \ifnum \c@secnumdepth >\m@ne
      \if@mainmatter
        \fontsize{18}{18pt}\selectfont\sffamily{\itshape\@chapapp\space 
\ifappendix
\thechapter
\else
\ifcase\the\c@chapter
\or%
Un\or%
Deux\or%
Trois\or%
Quatre\or%
Cinq\or%
Six\or%
Sept\or%
Huit\or%
Neuf\or%
Dix\or%
Eleven\or%
Twelve\or%
Thirteen\or%
Fourteen\or%
Fifteen\or%
Sixteen\or%
Seventeen\or%
Eighteen\or%
Nineteen\or%
Twenty\or%
Twenty One\or%
Twenty Two\or%
Twenty Three\or%
Twenty Four\or%
Twenty Five\or%
Twenty Six\or%
Twenty Seven\or%
Twenty Eight\or%
Twenty Nine\or%
Thirty\or%
Thirty One\or%
Thirty Two\or%
Thirty Three\or%
Thirty Four\or%
Thirty Five\or%
Thirty Six\or%
Thirty Seven\or%
Thirty Eight\or%
Thirty Nine\or%
Forty\or%
Forty One\or%
Forty Two\or%
Forty Three\or%
Forty Four\or%
Forty Five\or%
Forty Six\or%
Forty Seven\or%
Forty Eight\or%
Forty Nine\or%
Fifty\or%
\else
Your book's too long!
\fi
\fi}        \par\nobreak
        \vskip -4\p@
%_%_%_%_% the double rule
\noindent\hspace{-18pt}\rule{\textwidth}{2pt}\\[-23.55pt]
\noindent\hspace{-18pt}\rule{\textwidth}{0.5pt}\Huge
\vskip6pt

%_%_%_%_%
      \fi
    \fi
    \interlinepenalty\@M
    {\fontsize{14}{24pt}\sffamily #1\par}\nobreak
    \vskip 32\p@
  }}
\def\@schapter#1{\if@twocolumn
                   \@topnewpage[\@makeschapterhead{#1}]%
                 \else
                   \@makeschapterhead{#1}%
                   \@afterheading
                 \fi}
\def\@makeschapterhead#1{%
{\fontsize{10}{12pt}\selectfont  
\vspace*{-\schapterdrop}%
  {\parindent \z@ \raggedright\advance\leftskip18pt
    \interlinepenalty\@M
    \fontsize{16}{18pt}\selectfont
\noindent\hspace*{-18pt}\rule{\textwidth}{2pt}\\[-11.58pt]%\vspace{-11.58pt}\par%%%2pt rule
\noindent\hspace*{-18pt}\rule{\textwidth}{.5pt}\vspace{18pt}\par\sffamily\itshape  #1\par\nobreak
    \vskip 40\p@
  }}\normalfont}
%_%_%_%_%redefine to eliminate hanging indent in section....
\def\@seccntformat#1{\csname the#1\endcsname\enspace}
\def\addcontentsline#1#2#3{%
 \addtocontents{#1}{\protect\contentsline{#2}{#3}{\sffamily\thepage}}}
\def\@sect#1#2#3#4#5#6[#7]#8{%
  \ifnum #2>\c@secnumdepth
    \let\@svsec\@empty
  \else
    \refstepcounter{#1}%
    \protected@edef\@svsec{\@seccntformat{#1}\relax}%
  \fi
  \@tempskipa #5\relax
  \ifdim \@tempskipa>\z@
    \begingroup
      #6{%
        {\noindent\@svsec}%
          \interlinepenalty \@M #8\@@par}%
    \endgroup
    \csname #1mark\endcsname{#7}%
    \addcontentsline{toc}{#1}{%
      \ifnum #2>\c@secnumdepth \else
        \protect\numberline{\csname the#1\endcsname}%
      \fi
      \hspace*{-1em}#7}% <-------- \hspace here to create "hanging indent" in section TOC entry!!!!
  \else
    \def\@svsechd{%
      #6{\hskip #3\relax
      \@svsec #8}%
      \csname #1mark\endcsname{#7}%
      \addcontentsline{toc}{#1}{%
        \ifnum #2>\c@secnumdepth \else
          \protect\numberline{\csname the#1\endcsname}%
        \fi
        #7}}%
  \fi
  \@xsect{#5}}
%_%_%_%_%
\newcommand\section{\@startsection {section}{1}{\z@}%
                                   {-20pt \@plus -1ex \@minus -.2ex}%
                                   {12pt}%
                                   {\normalfont\fontsize{10}{12pt}\selectfont\baselineskip16pt\bfseries\uppercase}}
\newcommand\subsection{\@startsection{subsection}{2}{\z@}%
                                     {-3.25ex\@plus -1ex \@minus -.2ex}%
                                     {1.5ex}%
                                     {\normalfont\fontsize{10}{12pt}\selectfont\bfseries}}
\newcommand\subsubsection{\@startsection{subsubsection}{3}{\z@}%
                                     {-16pt\@plus -1ex \@minus -.2ex}%
                                     {1.5ex}%
                                     {\normalfont\fontsize{10}{12pt}\selectfont\itshape}}
\newcommand\paragraph{\@startsection{paragraph}{4}{\z@}%
                                    {-12pt \@plus-1ex \@minus-.2ex}%
                                    {6pt}%
                                    {\normalfont\fontsize{10}{12pt}\selectfont\scshape}}
\newcommand\subparagraph{\@startsection{subparagraph}{5}{\parindent}%
                                       {3.25ex \@plus1ex \@minus .2ex}%
                                       {-1em}%
                                      {\normalfont\fontsize{10}{12pt}\selectfont\bfseries}}
\if@twocolumn
  \setlength\leftmargini  {2em}
\else
  \setlength\leftmargini  {2.5em}
\fi
\leftmargin  \leftmargini
\setlength\leftmarginii  {2.2em}
\setlength\leftmarginiii {1.87em}
\setlength\leftmarginiv  {1.7em}
\if@twocolumn
  \setlength\leftmarginv  {.5em}
  \setlength\leftmarginvi {.5em}
\else
  \setlength\leftmarginv  {1em}
  \setlength\leftmarginvi {1em}
\fi
\setlength  \labelsep  {.5em}
\setlength  \labelwidth{\leftmargini}
\addtolength\labelwidth{-\labelsep}
\@beginparpenalty -\@lowpenalty
\@endparpenalty   -\@lowpenalty
\@itempenalty     -\@lowpenalty
\renewcommand\theenumi{\@arabic\c@enumi}
\renewcommand\theenumii{\@alph\c@enumii}
\renewcommand\theenumiii{\@roman\c@enumiii}
\renewcommand\theenumiv{\@Alph\c@enumiv}
\newcommand\labelenumi{\theenumi.}
\newcommand\labelenumii{(\theenumii)}
\newcommand\labelenumiii{\theenumiii.}
\newcommand\labelenumiv{\theenumiv.}
\renewcommand\p@enumii{\theenumi}
\renewcommand\p@enumiii{\theenumi(\theenumii)}
\renewcommand\p@enumiv{\p@enumiii\theenumiii}
\newcommand\labelitemi{\textbullet}
\newcommand\labelitemii{\normalfont\bfseries \textendash}
\newcommand\labelitemiii{\textasteriskcentered}
\newcommand\labelitemiv{\textperiodcentered}
\newenvironment{description}
               {\list{}{\labelwidth\z@ \itemindent-\leftmargin
                        \let\makelabel\descriptionlabel}}
               {\endlist}
\newcommand*\descriptionlabel[1]{\hspace\labelsep
                                \normalfont\bfseries #1}
\newenvironment{verse}
               {\let\\\@centercr
                \list{}{\itemsep      \z@
                        \itemindent   -1.5em%
                        \listparindent\itemindent
                        \rightmargin  \leftmargin
                        \advance\leftmargin 1.5em}%
                \item\relax}
               {\endlist}
\newenvironment{quotation}
               {\list{}{\listparindent 1.5em%
                        \itemindent    \listparindent
                        \rightmargin   \leftmargin
                        \parsep        \z@ \@plus\p@}%
                \item\relax}
               {\endlist}
\newenvironment{quote}
               {\list{}{\rightmargin\leftmargin}%
                \item\relax}
               {\endlist}
\if@compatibility
\newenvironment{titlepage}
    {%
      \cleardoublepage
      \if@twocolumn
        \@restonecoltrue\onecolumn
      \else
        \@restonecolfalse\newpage
      \fi
      \thispagestyle{empty}%
      \setcounter{page}\z@
    }%
    {\if@restonecol\twocolumn \else \newpage \fi
    }
\else
\newenvironment{titlepage}
    {%
      \cleardoublepage
      \if@twocolumn
        \@restonecoltrue\onecolumn
      \else
        \@restonecolfalse\newpage
      \fi
      \thispagestyle{empty}%
      %%%%%%%%%removed for correct FM pagination \setcounter{page}\@ne
    }%
    {\if@restonecol\twocolumn \else \newpage \fi
     \if@twoside\else
        \setcounter{page}\@ne
     \fi
    }
\fi
\setlength\arraycolsep{5\p@}
\setlength\tabcolsep{6\p@}
\setlength\arrayrulewidth{.4\p@}
\setlength\doublerulesep{2\p@}
\setlength\tabbingsep{\labelsep}
\skip\@mpfootins = \skip\footins
\setlength\fboxsep{3\p@}
\setlength\fboxrule{.4\p@}
\@addtoreset {equation}{chapter}
\renewcommand\theequation
  {\ifnum \c@chapter>\z@ \thechapter.\fi \@arabic\c@equation}
\newcounter{figure}[chapter]
\renewcommand \thefigure
     {\ifnum \c@chapter>\z@ \thechapter.\fi \@arabic\c@figure}
\def\fps@figure{tbp}
\def\ftype@figure{1}
\def\ext@figure{lof}
\def\fnum@figure{\figurename~\thefigure}
\newenvironment{figure}
               {\@float{figure}}
               {\end@float}
\newenvironment{figure*}
               {\@dblfloat{figure}}
               {\end@dblfloat}
\newcounter{table}[chapter]
\renewcommand \thetable
     {\ifnum \c@chapter>\z@ \thechapter.\fi \@arabic\c@table}
\def\fps@table{tbp}
\def\ftype@table{2}
\def\ext@table{lot}
\def\fnum@table{\tablename~\thetable}
\newenvironment{table}
               {\@float{table}}
               {\end@float}
\newenvironment{table*}
               {\@dblfloat{table}}
               {\end@dblfloat}
\newlength\abovecaptionskip
\newlength\belowcaptionskip
\setlength\abovecaptionskip{6\p@}
\setlength\belowcaptionskip{0\p@}
\def\xtable{table}
\long\def\@makecaption#1#2{%
\ifx\@captype\xtable
  \vskip\abovecaptionskip\fontsize{9}{11pt}\selectfont
  \sbox\@tempboxa{{#1}\enspace#2}%
  \ifdim \wd\@tempboxa >\hsize
    \@hangfrom{#1\enspace}#2\par
  \else
    \global \@minipagefalse
    \hb@xt@\hsize{\hfil\box\@tempboxa\hfil}%
  \fi
  \vskip8.3pt
\else
  \vskip\abovecaptionskip\fontsize{9}{11pt}\selectfont
  \sbox\@tempboxa{{#1}\enspace#2}%
  \ifdim \wd\@tempboxa >\hsize
    \@hangfrom{#1\enspace}#2\par
  \else
    \global \@minipagefalse
    \hb@xt@\hsize{\hfil\box\@tempboxa\hfil}%
  \fi
  \vskip\belowcaptionskip
\fi}

\DeclareOldFontCommand{\rm}{\normalfont\rmfamily}{\mathrm}
\DeclareOldFontCommand{\sf}{\normalfont\sffamily}{\mathsf}
\DeclareOldFontCommand{\tt}{\normalfont\ttfamily}{\mathtt}
\DeclareOldFontCommand{\bf}{\normalfont\bfseries}{\mathbf}
\DeclareOldFontCommand{\it}{\normalfont\itshape}{\mathit}
\DeclareOldFontCommand{\sl}{\normalfont\slshape}{\@nomath\sl}
\DeclareOldFontCommand{\sc}{\normalfont\scshape}{\@nomath\sc}
\DeclareRobustCommand*\cal{\@fontswitch\relax\mathcal}
\DeclareRobustCommand*\mit{\@fontswitch\relax\mathnormal}
\newcommand\@pnumwidth{3em}
\newcommand\@tocrmarg{4em}
\newcommand\@dotsep{-4.5}
%%%%%%%%\setcounter{tocdepth}{2}
\setcounter{tocdepth}{1}
\newcommand\tableofcontents{%
    \if@twocolumn
      \@restonecoltrue\onecolumn
    \else
      \@restonecolfalse
    \fi
 \chapter*{\contentsname
        \@mkboth{%
           \MakeUppercase\contentsname}{\MakeUppercase\contentsname}}%
    \@starttoc{toc}%
    \if@restonecol\twocolumn\fi
    }
\newcommand*\l@part[2]{%
  \ifnum \c@tocdepth >-2\relax
    \addpenalty{-\@highpenalty}%
    \addvspace{2.25em \@plus\p@}%
    \begingroup
      \parindent \z@ \rightskip \@pnumwidth
      \parfillskip -\@pnumwidth
      {\leavevmode\fontsize{9.75}{14pt}\selectfont
       \sffamily PART \uppercase{#1}\hfil \hb@xt@\@pnumwidth{\hss #2}}\par
       \nobreak
         \global\@nobreaktrue
         \everypar{\global\@nobreakfalse\everypar{}}%
    \endgroup
  \fi}
\newcommand*\l@section{\@dottedtocline{1}{1em}{3.3em}}
\newcommand*\l@subsection{\@dottedtocline{2}{3.8em}{3.2em}}
\newcommand*\l@subsubsection{\@dottedtocline{3}{7.0em}{4.1em}}
\newcommand*\l@paragraph{\@dottedtocline{4}{10em}{5em}}
\newcommand*\l@subparagraph{\@dottedtocline{5}{12em}{6em}}
\newcommand\listoffigures{%
    \if@twocolumn
      \@restonecoltrue\onecolumn
    \else
      \@restonecolfalse
    \fi
    \chapter*{\listfigurename
      \@mkboth{\MakeUppercase\listfigurename}%
              {\MakeUppercase\listfigurename}}%
    \@starttoc{lof}%
    \if@restonecol\twocolumn\fi
    }
\newcommand*\l@figure{\@dottedtocline{1}{1.5em}{2.3em}}
\newcommand\listoftables{%
    \if@twocolumn
      \@restonecoltrue\onecolumn
    \else
      \@restonecolfalse
    \fi
    \chapter*{\listtablename
      \@mkboth{%
          \MakeUppercase\listtablename}{\MakeUppercase\listtablename}}%
    \@starttoc{lot}%
    \if@restonecol\twocolumn\fi
    }
\let\l@table\l@figure
\newdimen\bibindent
\setlength\bibindent{1.5em}
\newenvironment{thebibliography}[1]
     {\chapter*{\bibname}
        \@mkboth{\MakeUppercase\bibname}{\MakeUppercase\bibname}\addcontentsline{toc}{chapter}{Bibliography}%
\gdef\rightmark{\MakeUppercase\bibname}
      \list{\@biblabel{\@arabic\c@enumiv}}%
           {\settowidth\labelwidth{\@biblabel{#1}}%
            \leftmargin\labelwidth
            \advance\leftmargin\labelsep
            \@openbib@code
            \usecounter{enumiv}%
            \let\p@enumiv\@empty
            \renewcommand\theenumiv{\@arabic\c@enumiv}}%
      \sloppy
      \clubpenalty4000
      \@clubpenalty \clubpenalty
      \widowpenalty4000%
      \sfcode`\.\@m}
     {\def\@noitemerr
       {\@latex@warning{Empty `thebibliography' environment}}%
      \endlist}
\newcommand\newblock{\hskip .11em\@plus.33em\@minus.07em}
\let\@openbib@code\@empty
\newenvironment{theindex}
               {\cleardoublepage\if@twocolumn
                  \@restonecolfalse
                \else
                  \@restonecoltrue
                \fi
                \columnseprule \z@
                \columnsep 35\p@
                \twocolumn[\vspace*{26pt}\@makeschapterhead{\indexname}]%
\addcontentsline{toc}{chapter}{Index}
                \@mkboth{\MakeUppercase\indexname}%
                        {\MakeUppercase\indexname}%
\gdef\rightmark{\MakeUppercase\indexname}%
                \thispagestyle{empty}\parindent\z@
                \parskip\z@ \@plus .3\p@\relax
                \let\item\@idxitem\footnotesize}
               {\if@restonecol\onecolumn\else\clearpage\fi}
\newcommand\@idxitem{\par\hangindent 16\p@}
\newcommand\subitem{\@idxitem \hspace*{8\p@}}
\newcommand\subsubitem{\@idxitem \hspace*{18\p@}}
\newcommand\indexspace{\par \vskip 1\baselineskip \@plus5\p@ \@minus3\p@\relax}
\renewcommand\footnoterule{%
  \kern10\p@
  \hrule\@width\columnwidth\@height.5pt
  \kern2.6\p@}
\@addtoreset{footnote}{chapter}
\newcommand\@makefntext[1]{%
\parindent1.25em\indent{\@makefnmark}#1}
\newcommand\contentsname{Contents}
\newcommand\listfigurename{List of Figures}
\newcommand\listtablename{List of Tables}
\newcommand\bibname{Bibliography}
\newcommand\indexname{Index}
\newcommand\figurename{Figure}
\newcommand\tablename{Table}
\newcommand\partname{Part}
\newcommand\chaptername{Chapter}
\newcommand\appendixname{Appendix}
\def\today{\ifcase\month\or
  January\or February\or March\or April\or May\or June\or
  July\or August\or September\or October\or November\or December\fi
  \space\number\day, \number\year}
\setlength\columnsep{10\p@}
\setlength\columnseprule{0\p@}
\pagestyle{headings}
\pagenumbering{arabic}
\if@twoside
\else
  \raggedbottom
\fi
\if@twocolumn
  \twocolumn
  \sloppy
  \flushbottom
\else
  \onecolumn
\fi

%_%_%_%_%
%_%_%_%_% New definitions
%_%_%_%_%
\setlength{\textwidth}{27.5pc}
\setlength{\parindent}{1em}
\addtolength{\oddsidemargin}{33.1pt}
\addtolength{\evensidemargin}{-9pt}
\setlength{\parskip}{0pt plus0pt minus0pt}
\addtolength{\headsep}{-8pt}


\setlength{\oddsidemargin}{68.1pt}
\setlength{\evensidemargin}{80pt}

\newcommand\makehalftitle{\begin{titlepage}%
\fontsize{10}{12pt}\selectfont
  \let\footnotesize\small
  \let\footnoterule\relax
  \let \footnote \thanks
  \null\mbox{}\\
\vskip7.25pc
  \begin{flushleft}\advance\leftskip36pt%
    {\fontsize{18}{24pt}\selectfont\sffamily\@title \par}%
    {\large
     \lineskip .75em%
      \par}%
      \vskip 1.5em%
    {\par}%       % Set date in \large size.
  \end{flushleft}\par
  \@thanks
  \vfil\null
  \end{titlepage}}%



%%%% Text after chapter opener, if not a section head
\newenvironment{chapterintro}{\vskip4pt\par\noindent\ignorespaces}{}


%%%% Numberlist; optional argument clears for 10
\newcounter{NumList}

\newlength{\NumWidth}

\newenvironment{numberlist}[1][1]{%
%%%%%%get optional argument to clear for ten
\settowidth{\NumWidth}{\upshape{#1}.\enspace}%
\begin{list}
{\leavevmode\hbox to \NumWidth{\hss\upshape\arabic{NumList}.\enspace}\hfil}
{\usecounter{NumList}%
\setlength{\topsep}{6pt plus3pt minus2pt}%
\setlength{\partopsep}{0pt}%
\setlength{\parsep}{0pt}%
\setlength{\itemsep}{0pt}%
\setlength{\leftmargin}{1em}%
\addtolength{\leftmargin}{\NumWidth}%
\setlength{\rightmargin}{0pt}%
\setlength{\listparindent}{1em}%
\setlength{\labelwidth}{\NumWidth}%
\setlength{\labelsep}{0pt}%
\setlength{\itemindent}{0pt}}}%
{\end{list}}

%%%% unnumberlist
\newenvironment{unnumberlist}{\begin{list}
{}%%%%no label
{%%%%%no counter
\setlength{\topsep}{6pt plus3pt minus2pt}%
\setlength{\partopsep}{0pt}%
\setlength{\parsep}{0pt}%
\setlength{\itemsep}{0pt}%
\setlength{\leftmargin}{2em}%
\setlength{\rightmargin}{0pt}%
\setlength{\listparindent}{0pt}%
\setlength{\labelwidth}{0pt}%
\setlength{\itemindent}{-1em}%
\setlength{\labelsep}{0pt}}}%%%%%
{\end{list}}


%%%%% bullet list
\newenvironment{bulletlist}{%
\settowidth{\NumWidth}{\hspace*{-.5pt}$\bullet$\enspace}%
\begin{list}%
{\leavevmode\hbox to \NumWidth{\hss\hspace*{-.5pt}\raisebox{1.19pt}[0pt][0pt]{$\bullet$}\enspace}\hfil}%
{\setlength{\topsep}{6pt plus3pt minus2pt}%
\setlength{\partopsep}{0pt}%
\setlength{\parsep}{0pt}%
\setlength{\itemsep}{0pt}%
\setlength{\leftmargin}{1em}%
\addtolength{\leftmargin}{\NumWidth}%
\setlength{\rightmargin}{0pt}%
\setlength{\listparindent}{1em}%
\setlength{\labelwidth}{\NumWidth}%
\setlength{\labelsep}{0pt}%
\setlength{\itemindent}{0pt}}}%
{\end{list}}



%%%%%preface
\newcommand{\prefacename}{Preface}
\newenvironment{thepreface}
               {%\cleardoublepage
\clearpage\if@twoside 
\ifodd\c@page\else
    \hbox{}\thispagestyle{empty}\newpage\if@twocolumn\hbox{}\newpage\fi\fi\fi
\if@twocolumn
                  \@restonecolfalse
                \else
                  \@restonecoltrue
                \fi
                \columnseprule \z@
                \columnsep 35\p@
                \chapter*{Preface}%\@makeschapterhead{Preface}%
\addcontentsline{toc}{chapter}{Preface}
                \thispagestyle{empty}\parindent1em
                \parskip\z@ \@plus .3\p@\relax                
\@mkboth{\MakeUppercase\prefacename}{\MakeUppercase\prefacename}%
                \noindent\ignorespaces}
               {\if@restonecol\onecolumn\else\clearpage\fi}




\def\l@chapter#1#2{%
  \ifnum \c@tocdepth >\m@ne
    \addpenalty{-\@highpenalty}%
    \vskip 1.0em \@plus\p@
    \setlength\@tempdima{1.5em}%
    \begingroup
      \parindent \z@ \rightskip \@pnumwidth
      \parfillskip -\@pnumwidth
      \leavevmode \sffamily
      \advance\leftskip\@tempdima
      \hskip -\leftskip
      \fontsize{9.5}{12pt}\selectfont\sffamily
#1\nobreak\hfil \nobreak\hb@xt@\@pnumwidth{\hss #2}\par
      \penalty\@highpenalty
    \endgroup
  \fi\vskip3pt\fontsize{8.75}{11pt}\selectfont}

\newif\ifappendix
\appendixfalse

\newcommand\appendix{\par\appendixtrue
  \setcounter{chapter}{0}%
  \setcounter{section}{0}%
  \gdef\@chapapp{\appendixname}%
  \gdef\thechapter{\@Alph\c@chapter}
}

\def\addcontentsline#1#2#3{%
  \addtocontents{#1}{\protect\contentsline{#2}{#3}{\fontsize{8.75}{11pt}\selectfont\sffamily\thepage}}}

\def\appaddcontentsline#1#2#3{%
  \addtocontents{#1}{\protect\contentsline{#2}{Appendix~#3}{\fontsize{8.75}{11pt}\selectfont\sffamily\thepage}}}

\def\chapaddcontentsline#1#2#3{%
  \addtocontents{#1}{\protect\contentsline{#2}{Chapter~#3}{\fontsize{8.75}{11pt}\selectfont\sffamily\thepage}}}

\def\@chapter[#1]#2{\ifnum \c@secnumdepth >\m@ne
                       \if@mainmatter
                         \refstepcounter{chapter}%
                         \typeout{\@chapapp\space\thechapter.}%
\ifappendix
                         \appaddcontentsline{toc}{chapter}%
                                   {\protect\numberline{\thechapter.}#1}%
\else
\chapaddcontentsline{toc}{chapter}%
                                   {%\protect\numberline
{\thechapter.\enspace}#1}%
\fi
                       \else
                         \appaddcontentsline{toc}{chapter}{#1}%
                       \fi
                    \else
                      \addcontentsline{toc}{chapter}{#1}%
                    \fi
                    \chaptermark{#1}%
                    \addtocontents{lof}{\protect\addvspace{10\p@}}%
                    \addtocontents{lot}{\protect\addvspace{10\p@}}%
                    \if@twocolumn
                      \@topnewpage[\@makechapterhead{#2}]%
                    \else
                      \@makechapterhead{#2}%
                      \@afterheading
                    \fi}


%%%%remove extra space in eqnarray...
\def\eqnarray{%
   \stepcounter{equation}%
   \def\@currentlabel{\p@equation\theequation}%
   \global\@eqnswtrue
   \m@th
   \global\@eqcnt\z@
   \tabskip\@centering
   \let\\\@eqncr
   $$\everycr{}\halign to\displaywidth\bgroup
       \hskip\@centering$\displaystyle\tabskip\z@skip{##}$\@eqnsel
      &\global\@eqcnt\@ne\,\hfil${##}$\hfil
      &\global\@eqcnt\tw@\,$\displaystyle{##}$\hfil\tabskip\@centering
      &\global\@eqcnt\thr@@ \hb@xt@\z@\bgroup\hss##\egroup
         \tabskip\z@skip
      \cr
}


%_%_%_%_%
%_%_%_%_% theorems, lemmas, etc.
%_%_%_%_%

%%% create theorems, etc. with upright font, no italic
\def\newtheoremup#1{%
  \@ifnextchar[{\@othmup{#1}}{\@nthmup{#1}}}
\def\@nthmup#1#2{%
  \@ifnextchar[{\@xnthmup{#1}{#2}}{\@ynthmup{#1}{#2}}}
\def\@xnthmup#1#2[#3]{%
  \expandafter\@ifdefinable\csname #1\endcsname
    {\@definecounter{#1}\@newctr{#1}[#3]%
     \expandafter\xdef\csname the#1\endcsname{%
       \expandafter\noexpand\csname the#3\endcsname \@thmcountersep
          \@thmcounterup{#1}}%
     \global\@namedef{#1}{\@thmup{#1}{#2}}%
     \global\@namedef{end#1}{\@endtheoremup}}}
\def\@ynthmup#1#2{%
  \expandafter\@ifdefinable\csname #1\endcsname
    {\@definecounter{#1}%
     \expandafter\xdef\csname the#1\endcsname{\@thmcounterup{#1}}%
     \global\@namedef{#1}{\@thmup{#1}{#2}}%
     \global\@namedef{end#1}{\@endtheoremup}}}
\def\@othmup#1[#2]#3{%
  \@ifundefined{c@#2}{\@nocounterr{#2}}%
    {\expandafter\@ifdefinable\csname #1\endcsname
    {\global\@namedef{the#1}{\@nameuse{the#2}}%
  \global\@namedef{#1}{\@thmup{#2}{#3}}%
  \global\@namedef{end#1}{\@endtheoremup}}}}
\def\@thmup#1#2{%
  \refstepcounter{#1}%
  \@ifnextchar[{\@ythmup{#1}{#2}}{\@xthmup{#1}{#2}}}
\def\@xthmup#1#2{%
  \@begintheoremup{#2}{\csname the#1\endcsname}\ignorespaces}
\def\@ythmup#1#2[#3]{%
  \@opargbegintheoremup{#2}{\csname the#1\endcsname}{#3}\ignorespaces}
\def\@thmcounterup#1{\noexpand\arabic{#1}}
\def\@thmcountersep{.}
\def\@begintheoremup#1#2{\trivlist
   \item[\hskip \labelsep{\bfseries #1\ #2}]\upshape}
\def\@opargbegintheoremup#1#2#3{\trivlist
      \item[\hskip \labelsep{\bfseries #1\ #2\ (#3)}]\upshape}
\def\@endtheoremup{\endtrivlist}


%_%_%_%_%
%_%_%_%_%Theorem-like structures
%_%_%_%_%

\if@mytheorems
\else
\newtheorem{theorem}{{\scshape Theorem}}[chapter]
\newtheorem{definition}[theorem]{{\scshape Definition}}
\newtheorem{lemma}[theorem]{{\scshape Lemma}}
\newtheorem{conjecture}[theorem]{{\scshape Conjecture}}
\newtheorem{corollary}[theorem]{{\scshape Corollary}}

%%%%% Example environment
\newcounter{example}[section]
\renewcommand{\theexample}{\thesection.\arabic{example}}
\newenvironment{example}[1][]{\vspace{6pt plus3pt minus2pt}\par%
\refstepcounter{example}%
\noindent\textbf{Example~\theexample}\hspace*{1em}\itshape\ignorespaces}{\par\vspace{6pt plus3pt minus2pt}}

%%%%% Remark environment
\newenvironment{remark}[1][Remark.]{\vspace{6pt plus3pt minus2pt}\par%
\noindent\textit{#1}\hspace*{1em}\ignorespaces}{\par\vspace{6pt plus3pt minus2pt}}

%%%%% Proposition
\newenvironment{proposition}{\begin{list}
{}%%%%no label
{%%%%%no counter
\setlength{\topsep}{6pt plus3pt minus2pt}%
\setlength{\partopsep}{0pt}%
\setlength{\parsep}{0pt}%
\setlength{\itemsep}{0pt}%
\setlength{\leftmargin}{1em}%
\setlength{\rightmargin}{1em}%
\setlength{\listparindent}{0pt}%
\setlength{\labelwidth}{0pt}%
\setlength{\itemindent}{0pt}%
\setlength{\labelsep}{0pt}}\item[]}%%%%%
{\end{list}}

\newenvironment{proof}[1][]{\vskip-\lastskip\par\vskip6pt plus2pt minus0pt\par%
\noindent\textit{Proof.}\enspace\ignorespaces}{\hfill$\Box$\par\vskip6pt plus2pt minus0pt}
\fi



%_%_%_%_% the book epigraph
\newenvironment{bookepigraph}{\fontsize{10}{12pt}\selectfont\parindent0pt\sffamily\thispagestyle{empty}\vspace*{5.75pc}\advance\leftskip4.25pc}{\par}

\newcommand{\epigraphsource}[1]{\vskip.5\baselineskip#1\par}


\endinput
%%
%% End of file `book.cls'.


\fboxsep0pt


\vbox to 10.25in{\hsize7.125in\parindent0pt%
\noindent\hbox to .5in{\vrule width .25in height .4pt depth 0pt\hfil \vrule height .5in depth -.25in}\hfill%
\hbox to .5in{\vrule height .5in depth -.25in\hfil\vrule width .25in height .4pt depth 0pt}
\vfill

\noindent\hbox to .5in{\vrule width .25in height .5in depth -35.6pt\hfil \vrule height .25in depth 0in}\hfill%
\hbox to .5in{\vrule height .25in depth 0in\hfil \vrule width .25in height .5in depth -35.6pt}}

