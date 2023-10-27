InkscapeSlide X: make PDF slideshows with Inkscape
==================================================

InkscapeSlide X is a utility that turns multi-layer [Inkscape](https://inkscape.org) vector graphic files into multi-page PDF documents that can be used as presentations. It is an updated replacement for the [inkscapeslide](https://github.com/abourget/inkscapeslide) script that was developed by [Alexandre Bourget](https://github.com/abourget) around 2008-2012. The author of InkscapeSlide X has been an avid user of the original script for more than ten years, and this new software aims to update and improve over the functionalities of inkscapeslide.

How it works
------------

1. Create an SVG document in Inkscape with multiple layers.
2. Add a new layer called "content", and put into that layer a single text box (not a flowrect). You can make the layer invisible or put the box outside of the page boundaries.
3. Type in this box the list of layers that make up each slide, where each line in the box corresponds to one slide.
4. Run InkscapeSlide X on the resulting SVG file. The output is a multi-page PDF document where each page is made out of the layers specified in the "content" text box.

Syntax
------

The following code snippet shows an example of the syntax to be used for the "content" text box.

```
Title
Background,L1,MyLayer
+L2

//Background,L3
L4,L5*0.5
```

As mentioned above, each line of the box corresponds to one page of the PDF output.

- The first line creates a page consisting of the single layer _Title_.

- The second line creates a page using the three layers called _Background_, _L1_ and _MyLayer_. When multiple layers compose a page, their names are separated by a comma. Whitespace between names, if any, is ignored. The layers will be placed on top of each other as they appear in the original document; the order in which the slides are listed on the line is therefore irrelevant.

- The third line creates a page whose content is relative to the previous one. The plus sign at the start of the line indicates that new layers are to be added to those of the page that came before. This is useful in the case of animations where the contents of the slide are progressively revealed. In that case, the page will thus be made of layers _Background_, _L1_ ,_ MyLayer_ and _L2_.

- Blank lines and lines starting with `//` are ignored. It is thus possible to "space out" the contents of the text box and add comments, for example to separate slides into sections. Thus the next two lines in the code example produce no slide.

- The last line in the example shows that layers can be included with a specific transparency (alpha); the `*0.5` after the name of slide _L5_ indicates that the layer will appear in that slide with an alpha value of 0.5 (regardless of the transparency level defined in the original SVG file).

Differences with respect to inkscapeslide
-----------------------------------------

The program is meant to be used as a drop-in replacement for the original script, so any SVG file that runs on the original *should* produce the same output using InkscapeSlide X. The improvements and upgrades in this new program include:

- A user-defined number of threads can be used to convert each slide of the presentation in parallel, resulting in a huge speed-up of the process. As an example, a file taking **3 minutes** to be processed by the original script can be dispatched in **6 seconds** in the new version (with 12 threads).

- InkscapeSlide X reads compressed (i.e. `.svgz`) files in addition to plain `.svg` files.

- The new program works with Inkscape version 1.x, instead of 0.x (which has a very different command-line interface that is incompatible with 1.x).

- The generation of each slide and its conversion into PDF is done in memory using pipes, instead of using temporary files on the hard drive. (This has the bonus of fixing a few quirks of the original program, such as the difficulty of dealing with file paths that contained spaces.) This also contributes to the speed-up.

- The original script merged each PDF slide with its own embedded fonts, which resulted in duplicates. This new program does the same but attempts to eliminate duplicates when it can (though this is not perfect), resulting in a smaller output file size (anecdotal evidence shows a reduction of about 30%).

- The `content` layer allows extended syntax, such as the use of `//` to ignore some lines in the text box (see details above). Additional syntactical extensions are expected in future versions.

About the author
----------------

The original inkscapeslide script was written in Python by [Alexandre Bourget](https://github.com/abourget). InkscapeSlide X is written by [Sylvain Hallé](https://leduotang.ca/sylvain), Full Professor at [Université du Québec à Chicoutimi](https://www.uqac.ca), Canada.