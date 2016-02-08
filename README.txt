README

OpenTafl is the old-fashioned computer implementation of the old-fashioned
Norse boardgame. At present, it supports local play against another human
and an AI.

The AI is capable of playing Brandub to search depth 6 relatively easily,
and of playing it to depth 7 on a fast computer if you let it take some
time to think.

Future features will include network play and support for third-party AI
engines.

v0.1.6b (released 02/08/16):
- Further reductions in memory use (fixed minification of game tree states)
- Fixed bug in piece-type determination functions, where certain pieces
  were treated as several types of pieces for the purposes of some rules
- Reduced heap size arguments, owing to reduced memory usage, which may
  fix OpenTafl32 for some Windows users
- Improved no-color readability by switching from < to [ for attacking pieces
- Foundational work for OpenTafl Notation engine mode: implement rules string 
  generation and parsing

v0.1.5b (released 01/11/16):
- Memory usage reduced to about 1/3 of 0.1.4b
- Added readme and license file

v0.1.4b (released 01/07/16):
- Serious memory optimizations, which will nevertheless need further work

v0.1.3b (released 01/06/16):
- Fix Windows 32-bit memory size arguments (32-bit Windows can manage only
  about 1.5gb)
- Add license and readme files
- Add Tawlbwrdd, using the Bell layout given at Cyningstan.

v0.1.2b (released 12/31/15):
- Turn off evaluation function debug output

v0.1.1b (released 12/31/15):
- Fix huge memory leak
- Remove deepening table in favor of dual-purpose transposition table
- Clean up debug printing

v0.1b (12/29/15):
- Initial public release
