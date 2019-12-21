# Picross
Picross ([nonogram](https://en.wikipedia.org/wiki/Nonogram)) implementation inspired by the NDS Picross game.

The goal of the game is to complete the grid with filled tiles and cross tiles according to the restrictions given on the sides. A more detailed explanation is given below.

The program looks like this:    
![GUI](https://i.imgur.com/u7HStpO.png)

## Features
- Randomized Picross puzzles of a customizable size and density (can also be shared using the seed).
- Input support for both the mouse and the keyboard.
- A test mode where you can try out ideas before applying them to the board.
- Realtime feedback via the numbers on the side.
- A possible solution for each puzzle.

## Rules
_[See also the Wikipedia article.](https://en.wikipedia.org/wiki/Nonogram)_

The goal of the game is to change every tile in the grid to either a filled tile or a tile with a cross in it. To do this the numbers in front of each row and the number above each column give hits as to which tiles have to be filled. Each individual number represents a sequence of filled connected tiles on that row or column. These sequences appear in the same order as their associated numbers. So for example the hint <tt>2 3 2 1</tt> means that in order there is a sequence of 2 filled tiles, followed by a sequence of 3 filled tiles, followed by a sequence of 2 and finally followed by a sequence of 1 tile. Between these sequences are one or more cross tiles.

As you are playing the program will start graying out the numbers. When a number turns gray this means that you have enclosed a sequence of that length and it is connected via a sequence of filled and/or cross tiles to the side of the game board. If a number turns red this means that there is an error with the sequence representing the number that turned red. If all the numbers turn red this means that either too many or too few sequences are present on a line/column while the entire line is filled with filled tiles or cross tiles.

Filling the entire grid using these hints completes the game. Have fun!

## Controls
A list of controls for the game.

### Playing
General controls needed to play the game.

##### Mouse
- Left mouse button to fill a tile
- Right mouse button to place a cross
- Left click a filled tile to empty it
- Right click a cross to remove it

##### Keyboard
- W to move up
- S to move down
- A to move left
- D to move right
- Space bar to fill a tile
- Shift to place a cross
- Press space bar on a filled tile to empty it
- Press shift on a cross to remove it

### Moving
- Up arrow to move the view up (and board down)
- Down arrow to move the view down (and board up)
- Right arrow to move the view right (and board left)
- Left arrow to move the view left (and board right)
- Mouse scroll wheel to zoom in and out

### Test mode
- T to enter test mode
- C to leave test mode and save changes
- V to leave test mode and undo changes

### Check
- R to toggle showing the original solution (note that other solutions might also be valid)

### Other
- You can drag the game around with the mouse if you click and hold outside the grid (useful if numbers are offscreen)

## Downloads
_Requires Java 8 or higher_  
- [Windows executable](https://github.com/RoanH/Picross/releases/download/v1.1/Picross-v1.1.exe)    
- [Runnable Java Archive](https://github.com/RoanH/Picross/releases/download/v1.1/Picross-v1.1.jar)

All releases: [releases](https://github.com/RoanH/Picross/releases)<br>
GitHub repository: [repository](https://github.com/RoanH/Picross)<br>

## Development
This is an [Eclipse](https://www.eclipse.org/) + [Gradle](https://gradle.org/) project with [Util](https://github.com/RoanH/Util) as the only dependency.

## History
Project development started: 4th of October, 2019.