README

1. Introduction
OpenTafl is the old-fashioned computer implementation of the old-fashioned
Norse boardgame. At present, it supports local play against another human
and an AI. Future features will include network play and support for 
third-party AI engines, among others.

You can follow the development of OpenTafl at soapbox.manywords.press/tag/tafl.

You can find the latest version of OpenTafl at softworks.manywords.press/opentafl.

2. How-to
Run the OpenTafl script file corresponding to your platform:
OpenTafl32.bat (Windows, 32-bit java)
OpenTafl64.bat (Windows, 64-bit java)
OpenTafl.sh (Linux and Mac)

To determine if you have 32-bit or 64-bit Java installed, run this command in
a command prompt window:

java -version

If you see '64-bit' in the output from that command, you have 64-bit Java.

3. The game clock
Since there are no conventions for how to time tafl games, I've invented a few
of my own. The game clock in OpenTafl comprises a sudden-death main time, go-
style byo-yomi overtimes, and a Fisher increment. These function as follows:

    - Main time: when the main time expires, provided there are no overtimes
      configured, the game is over, and the player whose time expired loses.
    - Overtime: each player receives a certain number of overtime periods.
      If a player makes a move before an overtime period expires, the overtime
      period refills at the start of his next turn. Only when a player uses a
      full overtime does his stock of overtime periods decrease.
    - Increment: at the start of each turn, the increment is added to
      whichever form of timing is currently active. The increment, when applied
      to main time, can increase the amount of main time remaining above its
      starting value. Overtime periods always start at the overtime period
      length plus the increment, and can never be longer than that.

These options provide a fair amount of flexibility in terms of timing, and
can be combined in interesting ways. (A quick-play game might use overtime
exclusively, for instance.) Please try several things and let me know what
works best for you.

4. Version history

v0.1.9b (released xx/xx/xx):
- Implement game clock, along with AI time usage features
- Save settings between runs (OpenTafl creates settings.ini in the working
  directory)
- Genericize Lanterna-based terminal UI, so it can theoretically run in any
  terminal supported by Lanterna (note: it doesn't work for me in gnome-
  terminal, and for portability, Lanterna's Swing terminal emulator remains
  the default)

v0.1.8b (released 02/20/16):
- Speed and memory usage improvements
- AI difficulty set by thinking time instead of search depth
- If insufficient time to search to the next depth, stop at the current
  depth and search deeper, starting at the known best moves, to catch some
  horizon effects

v0.1.7.1b (released 02/19/16):
- Fixes for game UI layout for narrow screen sizes
- Implement threefold repetition rules, plus rules serializer support

v0.1.7b (released 02/17/16):
- Prep work for external engine support: split work into additional threads to
  avoid blocking certain other pieces, e.g. the game clock and the engine->
  host communication
- New portable cross-platform UI, built on the Lanterna terminal graphics
  library

v0.1.6.1b (released 02/09/16):
- Bugfix release for 0.1.6b
- Fix bug where OTN move records would show the moving taflman as the captured
  taflman
- Fix bug where OTN position strings would leave out the king after the first
  capture
- Fix bug where taflman move cache would be re-allocated for every state

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
