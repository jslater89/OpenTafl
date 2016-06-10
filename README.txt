README

1. Introduction
2. How-to
3. The game clock
4. External engines
5. Replay mode
6. AI self-play mode
7. Network play
8. Links
9. Version history

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

Note that the AI search time setting supersedes the AI's time usage planning—
you can use the search time to cap the AI's thinking time, and thereby set
its difficulty.

4. External engines
OpenTafl supports external artificial intelligence engines, using the OpenTafl
Engine Protocol. (You can find a link in the links section below, if you are
interested in developing an OpenTafl-compatible AI.)

Use the options menu to configure the external AI. You must select the .ini
file provided by your engine to instruct OpenTafl how to start it. If your
engine does not provide an .ini file, please contact your engine's author.
Note that the AI think time setting only affects OpenTafl.

5. Saved games and replay mode
Games may be saved by entering 'save' during a game, or while viewing a replay.
Saved games may be loaded by selecting 'Load game' at the main menu, and
replays may be loaded by selecting the 'View Replay' menu item in the main
menu. Enter the 'help' command for more information on in-game replay commands.

You can start a new game at any point in a replay. Note that you will be unable
to return to the replay in this case; entering replay mode will replay the new
game, up to the point where the new game started, and featuring any moves you
have made beyond that point.

Replays which do not result in a completed game are functionally identical to
saved games, and may be loaded as such.

6. AI self-play mode
For external AI developers, OpenTafl provides a mode by which two AIs, or two
versions of the same AI, can be made to play each other repeatedly, to judge
relative strength. Start OpenTafl with the '--dev' switch to enable the AI
self-play menu item. Set the attacker engine, defender engine, and game clock
in the options menu, then select the self-play menu item. The iteration count
is the number of matches the self-play runner will run. A match is a two-game
series. The player who wins both games wins the match; if the players each win
one game, the player who wins in the fewest moves win. If the players tie, the
match is considered drawn.

At the end of the self-play matches, a summary will be displayed on screen.
Detailed results, including game records for every game, will be saved in the
'selfplay-results' subdirectory under the main OpenTafl directory.

7. Network play
OpenTafl can be played with humans and AIs (not yet implemented) from around
the world, using its tafl server functionality. Anyone may run an OpenTafl
server by running the OpenTafl command with the --server flag. An OpenTafl
server is provided by Many Words Softworks at intersect.manywords.press. The
currently-selected server may be changed in the options menu. Servers run on
port 11541.

On connecting to a server, clients will be presented with a server login
dialog. Enter your credentials to log in or register a new account. Inactive
accounts are pruned weekly. (This interval is subject to change.)

After joining a server, clients will be presented with the server lobby screen,
comprising three windows. The focused window will be indicated by asterisks
surrounding its title, which will also be rendered in capital letters. Press
Tab to cycle window focus clockwise, or Shift-Tab to cycle focus
counterclockwise.

The initially-focused window is the game list window. Use the arrow keys to
scroll, or press enter to select a game to join.

The second window, the Server Information window, provides buttons to create
games, refresh the game list (the game list is automatically refreshed every
few seconds), and leave the server. The Server Information window also contains
a list of all players currently connected to the server.

The third window, the Lobby Chat window, shows the lobby chat. Use Page Up and
Page Down to scroll through the chat history. Type your messages in the text
entry box, and press Enter to send them.

Upon entering a game, the interface functions as it does during single-player
games. The 'chat' command may be used to interact with your opponent during a
game.

8. Links
http://softworks.manywords.press/opentafl (official website)
http://soapbox.manywords.press/tag/tafl (development blog)
https://bitbucket.org/Fishbreath/opentafl (source code, bug reports)
http://conclave.manywords.press/forum/softworks/opentafl/ (forum)
http://manywords.press/other-stuff/opentafl/opentafl-engine-protocol.txt (engine protocol specification)
http://manywords.press/other-stuff/opentafl/opentafl-notation-spec.txt (network protocol specification)

9. Version history

v0.3.1.0b (released 06/11/16):
- Spectator mode
- Minor networking bugfixes

v0.3.0.1b (released 06/08/16):
- Hotfix for server-side bug with multiple games of different dimensions in
  progress

v0.3.0.0b (released 06/02/16):
- Add network play initial operational capabilities
    - Server mode and client-server interface
    - Server lobby UI
    - Multiplayer games

v0.2.5.3b (released 05/31/16):
- Fix bug in taflman location list, which led to missing king in position
  records

v0.2.5.2b (released 05/27/16):
- Fix crash on close when no external engines are configured

v0.2.5.1b (released 05/02/16):
- Add missing 15x15 variant (lost in the build)

v0.2.5b (released 05/01/16):
- Add 15x15 Tablut variant, layout courtesy of the forum at aagenielsen.dk
- Add option to render large boards (15x15 and up) using an alternate, smaller
  representation
- Internal improvements to handling of external engine configuration files
- Prefer to display names from external engine configuration files over
  filenames
- Correctly shut down engines when AI self-play games finish

v0.2.4.8b (released 04/23/16):
- Fix saved-game bug: time-remaining tag would incorrectly show attacker time
  twice
- Fix saved-game loading bug: would incorrectly prefer starting time setting
  instead of most recent recorded time setting

v0.2.4.7b (released 04/21/16):
- Actually fix AI time use planning when down to overtime times, by reserving
  an extra half-second or second

v0.2.4.6b (released 04/20/16):
- Actually fix AI time use planning when think time is set to 0, since the
  previous fix was still broken

v0.2.4.5b (released 04/15/16):
- Provide external engines sufficient information to restart a game from replay
  after finishing a regular game
- Fix AI sometimes accidentally running out of time
- Fix AI time use planning when think time is set to 0 (0 means 'AI's choice')

v0.2.4.4b (released 04/13/16)
- Fix Fetlar: king should be strong (oops)
- Copenhagen: different variant names for standard and relaxed-shieldwall
- Add a commentary of an amateur game

v0.2.4.3b (released 04/10/16):
- Fix rules printing bugs relating to throne hostility when empty, and to king-
  only hostility

v0.2.4.2b (released 04/10/16):
- Tablut variants: kings are supposed to be armed
- Add two more Tim Millar commentaries

v0.2.4.1b (released 04/10/16):
- Foteviken tablut: king is strong everywhere, not just near the center

v0.2.4b
- Implementation of 'rules' command in the UI
- Scrolling message dialog fixes and improvements
- Add Tablut variants (Foteviken, with attacker camps, and standard)

v0.2.3.3b (released 04/08/16):
- Fix for berserk moves counting as repeated moves for the purposes of deciding
  victory
- Display fixes for berserk replays

v0.2.3.2b (released 04/07/16):
- AI improvements: fix coefficients
- AI improvements: prefer to develop pieces
- AI selfplay: fix cases where a match win would be reported as a draw

v0.2.3.1b (released 04/02/16);
- Hotfix for crash when saving a game after the game has ended

v0.2.3b (released 04/02/16):
- Finish implementation of external engine mode
    - Implement engine-initiated errors
    - Improve OpenTafl's handling of engines which fail to start
    - Instruct external engines to shut down when they are no longer needed
    - Improvements to OpenTafl's handling of engines used for analysis
- Internal UI overhaul
    - Organize Windows (terminal UI components) into Screens (logical groups of
      Windows with related functions)
- In-game UI fixes and improvements
    - Command entry window now has command buffer: up and down arrows show past
      commands
    - Status window now correctly handles multi-line additions when the status
      window string buffer fills up
- Copenhagen rules fixes
    - Edge fort escapes require an invincible structure
    - Attackers get the first move
- Fetlar rules fixes
    - Attackers get the first move
- External rules import
    - OpenTafl looks at external-rules.conf in its own directory, and loads any
      OpenTafl Notation rules records in it as built-in variants
- Saving and restoring games, replay mode
    - 'Save' command at any time in the game UI will save a game record, in
      either the 'saved-games' or 'saved-games/replays' folder, depending on
      whether the game has ended
    - Replays may contain commentary, which is displayed as the game is played
      back
    - Games and replays may be loaded from the main menu
    - Player may start a new game rooted at any point in a replay


v0.2.2b (released 03/28/16):
- Improvements to OpenTafl AI's time control handling
- Fix for OpenTafl incorrectly using the opponent's clock when playing as an
  engine
- Properly terminate the built-in AI, and any external AI engines, at the end
  of a game.
- AI self-play mode
- Game serializer (output only)
- Remaining:
    - Handling of engine-initiated errors

v0.2.1b (released 03/20/16):
- Ongoing implementation of external engine mode
- Newly implemented:
    - OpenTafl-initiated errors and error codes
    - Finish codes
    - Engine-initiated informational commands
    - 'status' command
    - 'clock' updates
    - 'analyze' command and related matters
    - 'simple-moves' command
    - Tests
- Remaining:
    - Handling of engine-initiated error codes

v0.2.0b (released 03/14/16):
- Functional but incomplete implementation of external engine mode
- Not yet implemented:
    - Tests
    - Errors and error codes
    - Finish codes
    - Engine-initiated informational commands (e.g. rules, move, position, &c.)
    - 'analyze' command and 'analysis' response handling
    - 'clock' commands after main time or overtime expiration
    - 'simple-moves' command
    - 'status' command


v0.1.9.1b (released 03/11/16):
- Possible bug-fixes for non-Swing terminals, which you, as a user, can't use
- Text fixes, which you, as a user, can't see
- Updated license

v0.1.9b (released 02/23/16):
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
