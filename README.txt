README

1. Introduction
2. How-to
3. The game clock
4. External engines
5. Replay mode
6. Puzzles
7. Variant editor
8. AI self-play mode
9. Network play
10. Headless AI mode
11. PlayTaflOnline game downloader
12. Links
13. Version history

1. INTRODUCTION ---------------------------------------------------------------
OpenTafl is the old-fashioned computer implementation of the old-fashioned
Norse boardgame. At present, it supports local play against another human or
the built-in AI. Other features include support for play against external AI
engines which use the OpenTafl Engine Protocol, network play against humans,
and a full-featured rules variant designer which can capture nearly every
described tafl variation, both modern and historical.

You can follow the development of OpenTafl at soapbox.manywords.press/tag/tafl.

You can find the latest version of OpenTafl at softworks.manywords.press/opentafl.


2. HOW-TO ---------------------------------------------------------------------
Run the OpenTafl script file corresponding to your platform:
OpenTafl32.bat (Windows, 32-bit java)
OpenTafl64.bat (Windows, 64-bit java)
OpenTafl.sh (Linux and Mac)

To determine whether you have 32-bit or 64-bit Java installed, run this command
in a command prompt window:

java -version

If you see '64-bit' in the output from that command, you have 64-bit Java.

OpenTafl should run in most non-graphical terminal environments. Use the
--fallback flag to enable old-school terminal mode.


3. THE GAME CLOCK -------------------------------------------------------------
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


4. EXTERNAL ENGINES -----------------------------------------------------------
OpenTafl supports external artificial intelligence engines, using the OpenTafl
Engine Protocol. (You can find a link in the links section below, if you are
interested in developing an OpenTafl-compatible AI.)

Use the options menu to configure the external AI. You must select the .ini
file provided by your engine to instruct OpenTafl how to start it. If your
engine does not provide an .ini file, please contact your engine's author.
Note that the AI think time setting only affects OpenTafl.


5. SAVED GAMES AND REPLAY MODE ------------------------------------------------
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

Replay mode features variations, lines of play separate from the principal line
of play, which may be tracked, viewed, and commented upon in the same manner as
the principal line of play. When in replay mode, you can move pieces around the
board by using the 'variation' command. You can also jump to specific states by
using their addresses, as displayed in the 'history' command. To navigate
forward and backward, you can use the 'next' command with no argument to move
to the next state in the current line, or use the 'next' command with an
integer argument to move to the variation from the current state with the given
index (e.g. 'next 2' to move to the second variation from the current state).
The 'previous' command will always move you toward the first move of the game.

You can edit annotations on any board position by using the 'annotate' command
and typing into the box provided. Due to limitations in the UI framework, the
text box does not wrap. OpenTafl will collapse single newlines to wrapped text,
but retain double newlines to allow for paragraph separation. This behavior
will be improved in a future release.

You can also delete branches of play (including the principal line) by using
the 'delete' command. For further information on commands, see the in-game
help system.

The following is a quick guide to OpenTafl variation addresses. In replay mode,
each state is given a specific name. The first move of the game is 1a: that is,
the first move (a) of the first turn (1) in the game. The second move is 1b,
and (presuming the variant does not include the berserk rule) the third is 2a,
followed by 2b and so on. In replay mode, the 'jump' command will accept
either numbers, to jump to the start of the named turn, or a full state
address, to jump to the state named.

Variations are named using an extension of the following scheme. For instance,
consider a variation off of the fourth move of the game:

2b.1.1b

Read variation addresses from right to left. This is a variation replacing the
second move in the first turn of the first variation from the fourth move in
the game. Note that OpenTafl will display this as 2b.1.1a. ..... (variation
move), for ease of reading in the history display. 'jump' will accept either
address as a synonym for the state properly addressed by 2b.1.1b. Let's look
at a more complicated example:

7a.2.1a.1.1b.

Again reading right to left, this is the second move (b) in the first turn (1)
of the first variation (1) off of the first move (a) in the first turn (1) of
the second variation (2) off of the first move (a) of the seventh turn (7) of
the game. Again, OpenTafl will display this as 7a.2.1a.1.1a. ..... (variation
move), for readability and consistency, and 'jump' will accept either form of
the address.

Old saved games can be deleted using the 'Delete a saved game' option in the
Extras menu.


6. PUZZLES --------------------------------------------------------------------
Some tafl puzzles come in the form of OpenTafl Notation rules strings, which
you can simply paste into the 'Load notation' dialog box and play as an
ordinary game.

OpenTafl also supports rich puzzles embedded into OpenTafl replay files. To
play such a puzzle, simply load a replay file containing a puzzle. OpenTafl
will prompt you to load such a puzzle as a puzzle or as a replay. Select the
former.

OpenTafl will present to you the game interface. If the puzzle author has
defined a prologue, you may use the 'next' and 'previous' commands to navigate
through the prologue. The 'history' command will display your progress in the
puzzle. You may use the 'jump' command to navigate to any state you have
revealed in the puzzle.

Once you have finished the prologue, if present, use the 'variation' command to
explore the puzzle. 'next', 'previous', 'jump', and 'history' may be used to
navigate.

OpenTafl's built-in tools provide everything you need to author a puzzle.
First, create a replay. Second, play out and annotate the branches you wish to
include in the puzzle. Finally, use the 'tags' command to set the puzzle mode,
selecting from one of the options below.

First: loose puzzles. Loose puzzles contain the principal line of play, along
with variations, all with annotations to guide and educate the puzzle player.
OpenTafl supports hints in a puzzle's comments, which will be hidden from the
player until requested. Loose puzzles allow the player to play out variations
which are not contained in the puzzle file, while notifying the player that
they have gone off of the beaten track.

Second: strict puzzles. Strict puzzles contain all lines of play the puzzle
author thinks are interesting, and the player may only explore those lines.
Variations off of those lines of play will be disallowed.


7. VARIANT EDITOR -------------------------------------------------------------
OpenTafl contains a full-featured graphical variant editor. It supports editing
every rules setting available to OpenTafl, as described in the OpenTafl
Notation specification's rules section. Further information on the variant
editor may be found in its built-in help window.


8. AI SELF-PLAY MODE ----------------------------------------------------------
For external AI developers, OpenTafl provides a mode by which two AIs, or two
versions of the same AI, can be made to play each other repeatedly, to judge
relative strength. Find the self-play menu item behind the 'Extras' option in
the main menu. Set the attacker engine, defender engine, and game clock in the
options menu, then select the self-play menu item. The iteration count is the
number of matches the self-play runner will run. A match is a two-game series.
The player who wins both games wins the match; if the players each win one
game, the player who wins in the fewest moves win. If the players tie, the
match is considered drawn.

At the end of the self-play matches, a summary will be displayed on screen.
Detailed results, including game records for every game, will be saved in the
'selfplay-results' subdirectory under the main OpenTafl directory.


9. NETWORK PLAY ---------------------------------------------------------------
OpenTafl can be played with humans and AIs from around the world, using its
tafl server functionality. Anyone may run an OpenTafl server by running the
OpenTafl command with the --server flag. An OpenTafl server is provided by
Many Words Softworks at intersect.manywords.press. The currently-selected
server may be changed in the options menu. Servers run on port 11541.

On connecting to a server, clients will be presented with a server login
dialog. Enter your credentials to log in or register a new account. Inactive
accounts are pruned weekly. (This interval is subject to change.)

After joining a server, clients will be presented with the server lobby screen,
comprising three windows. The focused window will be indicated by an underlined
title, which will also be rendered in capital letters. Press Tab to cycle 
window focus clockwise, or Shift-Tab to cycle focus counterclockwise.

The initially-focused window is the game list window. Use the arrow keys to
scroll, or press enter to select a game to join.

The second window, the Server Detail window, provides buttons to create
games, refresh the game list (the game list is automatically refreshed every
few seconds), and leave the server. The Server Information window also contains
a list of all players currently connected to the server.

The third window, the Lobby Chat window, shows the lobby chat. Use Page Up and
Page Down to scroll through the chat history. Type your messages in the text
entry box, and press Enter to send them.

On creating a game, clients will be presented with a dialog box providing
several options for the game to be created. Using the 'Rules' button, the
player may select a rules variant to play. Using the 'Load game' button, the
player may instead select a saved game to load. When loading a saved game,
OpenTafl attempts to restore the full state of the game, including its history,
as well as any time control settings. The 'Clock settings' label will display
the time control settings used in the saved game. When loading the game, if the
saved game contains information on the time remaining on the clocks, OpenTafl
will set the clocks according to that information. To disable time control in a
loaded game, use the clock settings window to set main time and overtime time
to 0.

The 'Other options' section of the game creation dialog allows the player to
define options which may be of use in competitive play. 'Combine spectator+
player chat', when checked, means that players can see spectator chat, and
spectators can see player chat. When unchecked, it means that players will not
be able to see spectator chat, though spectators can still see player chat.

'Allow replays and analysis', when checked, allows players in the game to use
the 'replay' and 'analyze' commands. When unchecked, 'replay' and 'analyze' are
disabled for players. Spectators can still use the commands.

Upon entering a game, the interface functions as it does during single-player
games. The 'chat' command may be used to interact with your opponent during a
game, according to the restrictions given above.


10. HEADLESS AI MODE -----------------------------------------------------------
OpenTafl can be started in headless AI mode, connecting an external engine to a
network server. The AI can be set to join a game already present on the server,
or to continuously host games. Whether joining games or hosting games, the AI
must use a game clock. The AI will save records of all games it plays in the
saved-games/headless-ai directory under the OpenTafl directory.


11. PLAYTAFLONLINE GAME DOWNLOADER --------------------------------------------
OpenTafl can download games from PlayTaflOnline.com for study and analysis. To
download a game, find the 'Download PlayTaflOnline game' option under the
'Extras' menu. Enter the PlayTaflOnline game number, and OpenTafl will
automatically download the PlayTaflOnline game file and convert it to OpenTafl
format.


12. LINKS ---------------------------------------------------------------------
http://softworks.manywords.press/opentafl (official website)
http://soapbox.manywords.press/tag/tafl (development blog)
https://bitbucket.org/Fishbreath/opentafl (source code, bug reports)
http://conclave.manywords.press/forum/softworks/opentafl/ (forum)
http://manywords.press/other-stuff/opentafl/opentafl-engine-protocol.txt (engine protocol specification)
http://manywords.press/other-stuff/opentafl/opentafl-notation-spec.txt (notation specification)


13. VERSION HISTORY -----------------------------------------------------------
v0.4.8.0b (released 01/09/20):
- Add the guard piece type, which can neither be captured nor take part in captures
- Bug prophylaxis

v0.4.7.2b (released 05/18/18):
- Fix error in human-readable rules for weak kings
- Fix bug in variant editor where selecting threefold loss created rules
  with threefold draw

v0.4.7.1b (released 03/07/18):
- Fix crash bug in 'rules' command output

v0.4.7.0b (released 11/03/17):
- Add mercenary taflmen, which switch sides when captured
- Add Linnaean capture
    - If a defender is next to the king, who is on the throne and surrounded
      on the other three sides by attackers, the defender may be captured
      against the throne
- Fix some crashes and bugs in variant editor relating to very large numbers
  of taflmen
- Fix potential crashes in OpenTafl with very large numbers of taflmen
- Allow variant editor to design 19x19 boards

v0.4.6.4b (released 04/23/17):
- Fix display bugs with board UI when keyboard-focused
    - When a taflman is selected and the cursor is elsewhere, spaces on which
      the taflman cannot stop will be rendered in light gray instead of dark
      blue
    - When the cursor is over an unselected taflman, spaces on which the
      taflman cannot stop will be rendered as dashes (passable) instead of dots
      (stoppable).
- In the OpenTafl server lobby, display increment times for games which have
  an increment time

v0.4.6.3b (released 04/18/17):
- Fix bug where the king could be captured by shieldwall
    - Per rules, the shieldwall should capture all other pieces but leave the
      king alive

v0.4.6.2b (released 04/16/17):
- Add support for using the keyboard UI to play variations in replays
- Fix a bug where the 'rules' command would incorrectly describe movement rules
  for pieces allowed neither to move through nor stop on a category of spaces
- Fix a bug where the OpenTafl AI would report an incorrect path through the
  game tree for a given node

v0.4.6.1b (released 04/15/17):
- Fix bug relating to loading built-in rules, along with associated crashes
- Fix related bug concerning deleting rules and options menu crashes

v0.4.6.0b (released 04/15/17):
- Add variant editor
- Board view is now focusable and allows in-game control by arrow keys
- Rearrange main menu for hopefully-better usability
- Extras menu now has a delete-saved-game file picker, for in-UI cleanup

v0.4.5.1b (released 02/11/17):
- Add support for new layouts in PlayTaflOnline JSON translator
- Add an 'Extras' menu item to hold less commonly-used features
- Move AI Selfplay to Extras
- Add Extras menu item for downloading PlayTaflOnline games
- OpenTafl now outputs game records with the starti: rules entry instead of the
  start: rules entry
    - starti is an inverted position record, representing the board from the
      highest rank (the top rank on the screen) to the lowest (the bottom)
    - This makes OpenTafl rules notation easier to read without a computer

v0.4.5.0b (released 12/31/16):
- Fix several search bugs
- Fix some remaining bugs in OpenTafl's play of repetitions
- Add piece square table to evaluation function
- Allow AI to play for a draw when the situation warrants it
- Fix a crash in the settings screen when cancelling entry of a new server
  address

v0.4.4.7b (released 10/25/16):
- Improve error tolerance when loading malformed OpenTafl notation
- Add more supported monospace fonts for graphical terminal mode, provide
  better guidance to users when no supported monospace fonts found

v0.4.4.6b (released 10/02/16):
- Fix some AI bugs relating to incomplete extension searches which could lead
  to dumb moves

v0.4.4.5b (released 09/28/16):
- Fix a number of bugs with replays and puzzles
- Add two new puzzles, remove one cooked puzzle

v0.4.4.4b (released 09/23/16):
- To match general convention, render a1 at the lower left instead of the upper
  left
- Add support for the Alfheim 17x17 layouts in the PlayTaflOnline json
  translator

v0.4.4.3b (released 09/16/16):
- Fix bug which caused bad play, strange capturing behavior, and crashes in
  Copenhagen games

v0.4.4.2b (released 09/16/16):
- Add a command-line translator for PlayTaflOnline json game records
- Fix 'rules' command output for speed limits
- Improve in-game UI layout on larger screens

v0.4.4.1b (released 09/13/16):
- Fix an occasional crash in the OpenTafl AI with certain time settings and
  horizon search

v0.4.4.0b (released 09/07/16):
- New stable release
- Puzzle mode added, along with two puzzles (viewable by 'load replay'); see
  section in README for more details
- Improve numbering/addressing of states in replays
    - In replays, jumping to a move always jumps to the position after that
      move has been made
    - Do 'jump 0' to get to the start of a replay
- Allow copying and pasting of game states to the clipboard for easy sharing
    - No hotkey support yet, sorry
    - Probably doesn't work in non-graphical mode

v0.4.3.6b (released 08/26/16):
- Release candidate for merge to stable branch
- Slightly improved move ordering/speed of move ordering

v0.4.3.5pre (released 08/24/16):
- AI improvements ongoing
- Implement the history heuristic
	- Essentially, the history heuristic says, "Moves which have been good
	  anywhere in the search at any time are likely to be good here, provided
	  they are legal"
	- Surprisingly, this works very well: with the history heuristic on, the AI
	  searches at least 20-30% fewer nodes because of better move ordering
- Further time usage improvements
	- OpenTafl should now bail out of the main search earlier, if it doesn't
	  expect to finish the next deepening step with the time it has remaining
	- This leaves it more time to do interesting extensions
	- Also, it makes the tree search much more correct: the previous behavior
	  would frequently miss non-obvious moves
- Better move ordering algorithm
	- Rather than sort the whole list, arrange the list by category: currently
	  killer moves, capturing moves, transposition moves, history moves, and
	  all remaining moves
	- Subject to change as I do benchmarking for nodes-to-depth
		- This is exciting: algorithmic improvements tend to yield much better
		  results as far as improving search depths than does pure optimization

v0.4.3.4pre (released 08/22/16):
- More AI improvements
	- Fix search bugs with the newly-implemented post-main-search searches
	- AI can now play out repetitions correctly
		- Although it's hard to say if this is an improvement, because
		  v0.4.3.3pre will happily play to the end of a losing repetition, and
		  so the strength comparison isn't exactly one-to-one
		- It does still greatly outperform v0.3.3.0b, though
	- Some minor speed improvements
	- Still better time use planning

v0.4.3.3pre (released 08/20/16):
- AI improvements, first prerelease
	- Fix some search bugs
	- Implement killer move heuristic
	- Better time use planning
	- AI now uses its post-main-search time more productively
	- AI can now play out repetitions

v0.4.2.4b (released 08/16/16):
- Fix some bugs with replays and annotations
- ~20% speed improvement, though memory use is slightly up

v0.4.2.3b (released 08/12/16):
- Fix bug where speed limits were applied to the opposite side

v0.4.2.2b (released 08/12/16):
- Fix bug where speed limits were not correctly serialized or read

v0.4.2.1b (released 08/12/16):
- Implement king hammer-only and anvil-only armed modes
- Change Magpie rules to match playtaflonline's Magpie rules

v0.4.2.0b (released 08/11/16):
- Implement some new rules
    - Middleweight king: the king is strong, but can be captured aginst an edge
    - Speed limits: pieces are limited in the number of spaces they can move
- Add Magpie tafl, brandub with 1-speed movement
- Fix a bug where saving an old replay would yield the incorrect king strength
- Fixed a bug where loading a replay with commentary on variations would fail
  to load said variation commentary

v0.4.1.0b (released 08/10/16):
- Support for variations in saved games
    - Rather handily, old saved games are forward-compatible, and new saved
      games are backward compatible
- UI for adding annotations to replays
    - Combined with the 'variation' command, it is now possible to use OpenTafl
      to author annotations
    - If you are interested in authoring annotations, or have written
      annotations and need somewhere to host them, please get in touch with me
- Minor bugfixes and tests to cover them
- Apache-license MoveAddress source code for people implementing OpenTafl
  notation
- Update OpenTafl notation spec to include variations in saved games

v0.4.0.0pre (released 08/07/16):
- Playable variations in replay mode!
    - This is a pre-release feature
    - See 'help' dialog when in replay for information on variation UI
    - Variations cannot currently be saved or loaded
        - Coming, but I wanted to get this out
    - Variations may cause OpenTafl to explode
        - Please report bugs if it does!

v0.3.3.3b (released 08/07/16):
- Fix bug where large network games would fail to load
- Network protocol version 7

v0.3.3.2b (released 07/27/16):
- Happy birthday, Dan
- In keeping with the sorts of things the aforementioned person is keen on,
  this release includes two UI improvements:
  - Network games without passwords no longer show a password entry box
  - The server login dialog now contains instructions for login/registration

v0.3.3.1b (released 07/25/16):
- Render rank and file names on both sides of the board
- Add advanced destination rendering mode: when enabled, 'info' command will
  show space names for allowable moves and captures, to help with mistyping
- Possible slight AI improvement
- Fix bug where saving a game previously loaded over the network would discard
  time-remaining information for past turns
- Fix bug where creating a network game from a save game file would not
  correctly update the create game dialog's clock setting text
- Minor code cleanup
- Network protocol version 6

v0.3.3.0b (released 07/15/16):
- Fix leaking resources when writing to the log file
- Allow network clients to load saved games
- Implement optional restrictions on in-game chat and use of replays/analysis
- Update clock immediately when entering a network game
- Network protocol version 5

v0.3.2.3b (released 07/13/16):
- Further improvements for loading rules/saved games from previous versions
- Integrate new Lanterna feature: ANSI underlining for highlighting text
- Logging now logs to a file for easier debug attachment

v0.3.2.2b (released 07/05/16):
- Fix bad 3.2.1b release package
- Fix occasional crash when the king is the only defending piece

v0.3.2.1b (released 07/02/16):
- Performance improvements (v0.3.x caused some slowdowns; v0.3.2.1b returns
  OpenTafl to performance levels as of about v0.2.5.3b)
- Some stability improvements for loading rules from external-rules.conf
- Alea Evangelii added via external rules
- Fixed bug where only first character of row index would be displayed when
  'shrink large boards' option was enabled and in effect
- OpenTafl wil now function in headless terminal environments, thanks to the
  latest release by the Lanterna developer
- (For developers) Fixes/improvements for building on Windows

v0.3.2.0b (released 06/17/16):
- Headless AI mode
- Fewer debug prints, options for chattiness
- Many bugfixes re: networking
    - Spectator mode should work more reliably
    - No longer able to use replay mode to start a new game during a network
      game
- Network protocol version 4

v0.3.1.0b (released 06/12/16):
- Spectator mode
- Minor networking bugfixes
- Network protocol version 3

v0.3.0.1b (released 06/08/16):
- Hotfix for server-side bug with multiple games of different dimensions in
  progress
- Network protocol version 2

v0.3.0.0b (released 06/02/16):
- Add network play initial operational capabilities
    - Server mode and client-server interface
    - Server lobby UI
    - Multiplayer games
- Network protocol version 1

v0.2.5.3b (released 05/31/16):
- Fix bug in taflman location list, which led to missing king in position
  records

v0.2.5.2b (released 05/27/16):
- Fix crash on close when no external engines are configured

v0.2.5.1b (released 05/02/16):
- Add missing 15x15 variant (lost in the build)

v0.2.5.0b (released 05/01/16):
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

v0.2.4.0b
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

v0.2.3.0b (released 04/02/16):
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

v0.2.2.0b (released 03/28/16):
- Improvements to OpenTafl AI's time control handling
- Fix for OpenTafl incorrectly using the opponent's clock when playing as an
  engine
- Properly terminate the built-in AI, and any external AI engines, at the end
  of a game.
- AI self-play mode
- Game serializer (output only)
- Remaining:
    - Handling of engine-initiated errors

v0.2.1.0b (released 03/20/16):
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

v0.2.0.0b (released 03/14/16):
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

v0.1.9.0b (released 02/23/16):
- Implement game clock, along with AI time usage features
- Save settings between runs (OpenTafl creates settings.ini in the working
  directory)
- Genericize Lanterna-based terminal UI, so it can theoretically run in any
  terminal supported by Lanterna (note: it doesn't work for me in gnome-
  terminal, and for portability, Lanterna's Swing terminal emulator remains
  the default)

v0.1.8.0b (released 02/20/16):
- Speed and memory usage improvements
- AI difficulty set by thinking time instead of search depth
- If insufficient time to search to the next depth, stop at the current
  depth and search deeper, starting at the known best moves, to catch some
  horizon effects

v0.1.7.1b (released 02/19/16):
- Fixes for game UI layout for narrow screen sizes
- Implement threefold repetition rules, plus rules serializer support

v0.1.7.0b (released 02/17/16):
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

v0.1.6.0b (released 02/08/16):
- Further reductions in memory use (fixed minification of game tree states)
- Fixed bug in piece-type determination functions, where certain pieces
  were treated as several types of pieces for the purposes of some rules
- Reduced heap size arguments, owing to reduced memory usage, which may
  fix OpenTafl32 for some Windows users
- Improved no-color readability by switching from < to [ for attacking pieces
- Foundational work for OpenTafl Notation engine mode: implement rules string 
  generation and parsing

v0.1.5.0b (released 01/11/16):
- Memory usage reduced to about 1/3 of 0.1.4b
- Added readme and license file

v0.1.4.0b (released 01/07/16):
- Serious memory optimizations, which will nevertheless need further work

v0.1.3.0b (released 01/06/16):
- Fix Windows 32-bit memory size arguments (32-bit Windows can manage only
  about 1.5gb)
- Add license and readme files
- Add Tawlbwrdd, using the Bell layout given at Cyningstan.

v0.1.2.0b (released 12/31/15):
- Turn off evaluation function debug output

v0.1.1.0b (released 12/31/15):
- Fix huge memory leak
- Remove deepening table in favor of dual-purpose transposition table
- Clean up debug printing

v0.1.0.0b (12/29/15):
- Initial public release
