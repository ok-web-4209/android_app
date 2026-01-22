# Golf Game App Flow

## Entry
- **Continue Season** is only enabled when at least one season exists.
- **New Season** requires at least one player on the roster and a season name.
- **Add/Remove Players** captures the player name only.
- **View/Output Statistics** supports CSV or Excel export.

## Continue Season
1. Select a season from a list.
2. Review previously played courses/games for the season.
3. Use **Add Course** to open Google Maps search, then define:
   - number of courses at the location
   - course names
   - hole count per course
4. Add players from the known list.
5. Start playing.

## Playing
- Select the starting hole.
- For each hole, record winners, losers, and hole-in-one flags.
- Navigate sequentially or jump to any hole.
- Finish game returns to main screen when all holes are recorded.

## Statistics
- **Player Statistics**: per-course score history, hole-in-one count, and wins overview.
- **Course Statistics**: ranking of best player scores by course.
- **Season Statistics**: standings overview with scores and hole-in-ones.

## Data Assumptions
- Higher score wins (wins minus losses).
- Scores can be edited until the game is finished.
