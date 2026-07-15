# Maze-Navigator

A Java simulation of an autonomous agent (`AverageBot`) exploring an unknown maze one room at a time, building its own map of the space as it goes, and finding an efficient path to the exit.

The bot doesn't get the maze layout up front. It only knows which doors are available from its current room. As it moves, it records which rooms and doors it has already used, tracks a "frontier" of rooms with unexplored doors, and avoids dead ends once they're identified. Once it reaches the end, it retraces its discovered map with BFS/A* to remove loops and shortcut the path it actually took, then reports how many steps it needed to explore versus the length of the optimized path.

## How it works

- **`Room`** is a node in the maze, identified by an ID. Tracks a shared move counter across the whole run.
- **`Door`** is a labeled edge connecting two rooms.
- **`CaveNavigator`** holds the maze layout and the bot's current position; exposes `getDoors()`, `move(door)`, and `atEnd()`.
- **`SerialLoader`** intends to deserialize a maze layout from a `.ser` file.
- **`AverageBot`**:
  1. Picks unexplored doors first, preferring ones that haven't led to a known dead end.
  2. Falls back to routing (A*) toward the most promising unexplored "frontier" room when the current room is fully explored.
  3. Once it reaches the end, replays its recorded path and searches for shortcuts using BFS between any two points on the path it took, removing loops and backtracking.
  4. Prints step counts and (optionally) the full path taken.
- **`AverageBotTester`** runs `AverageBot` on a maze file and prints a weighted score based on path length vs. exploration overhead.

## Current status / known limitation

`SerialLoader.deserialize()` currently just stores the file path, it doesn't yet parse the `.ser` maze files into a real layout. `CaveNavigator` builds a fixed 4-room test maze (`1 ↔ 2 ↔ 3 ↔ 4`) regardless of which maze file name is passed in. The `.ser` files under `MazeFiles/` (e.g. `M1.ser`–`M9.ser`, `C1.ser`, `C3.ser`, ...) are sample serialized maze data intended for future use once deserialization is wired up, passing different filenames to `AverageBot` won't currently change the maze it navigates.

## Getting Started

### Prerequisites
- JDK installed (`javac` / `java` on your PATH)

### Clone
```bash
git clone https://github.com/CoastMushrooms/Maze-Navigator
cd Maze-Navigator/MazeFiles
```

### Build + run
```bash
javac *.java
java AverageBotTester
```

Or use the included script from within `MazeFiles/`:
```bash
./run.sh
```

> Don't include the file extension when invoking the Java classloader, use `java AverageBotTester`.

To run the bot across every sample maze file instead of just one, run `AverageBot`'s own `main` method, which loops over `M1`–`M9`:
```bash
java AverageBot
```

## Project Structure

| File | Purpose |
|---|---|
| `AverageBot.java` | The exploring agent: frontier-based exploration, A*/BFS routing, path optimization |
| `AverageBotTester.java` | Entry point that runs the bot on a single maze and prints a weighted score |
| `CaveNavigator.java` | Maze state, current room, end room, and available doors |
| `Room.java` | Room node with ID and a shared static move counter |
| `Door.java` | Named edge between two rooms |
| `SerialLoader.java` | Placeholder for loading a maze layout from a `.ser` file |
| `M*.ser`, `C*.ser` | Sample serialized maze data for future use |
| `run.sh` | Convenience script to compile and run `AverageBotTester` |

## Roadmap
- Implement real deserialization in `SerialLoader` and wire `CaveNavigator` to build its graph from the loaded `.ser` maze data
- Support running the bot against every sample maze and comparing scores
- Add unit tests for `AverageBot`'s pathfinding and loop-removal logic
