InkscapeSlide X is a utility that turns multi-layer [Inkscape](https://inkscape.org) vector graphic files into multi-page PDF documents that can be used as presentations. It is an updated replacement for the [inkscapeslide](https://github.com/abourget/inkscapeslide) script that was developed by [Alexandre Bourget](https://github.com/abourget) around 2008-2012.

The author of InkscapeSlide X has been an avid user of the original script for more than ten years, and this new software.

## Differences with respect to inkscapeslide

The program is meant to be used as a drop-in replacement for the original script, so any SVG file that runs on the original *should* produce the same output using InkscapeSlide X.

Among the improvements:

- A user-defined number of threads can be used to convert each slide of the presentation in parallel, resulting in a huge speed-up of the process. As an example, a file taking **3 minutes** to be processed by the original script can be dispatched in **6 seconds** in the new version (with 12 threads).

- InkscapeSlide X reads compressed (i.e. svgz) files in addition to plain svg files.

- The new program works with Inkscape version 1.x, instead of 0.x (which has a very different command-line interface that is incompatible with 1.x).

- The generation of each slide and its conversion into PDF is done in memory using pipes, instead of using temporary files on the hard drive. (This has the bonus of fixing a few quirks of the original program, such as the difficulty of dealing with file paths that contained spaces.)

- The original script merged each PDF slide with its own embedded fonts, which resulted in duplicates. This new program attempts to eliminate duplicates when it can (though this is not perfect), resulting in a smaller output file size (anecdotal evidence shows a reduction of about 30%).

- The `content` layer allows extended syntax, such as the use of `//` to ignore some lines in the text box. Additional syntactical extensions are expected in future versions.