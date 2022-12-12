package io.github.mschout.aoc.puzzle;

import io.github.mschout.aoc.AdventOfCodePuzzle;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static java.util.Map.entry;

// Part 1 Total Score: 9651
// Part 2 Total Score: 10560

public class Day02 extends AdventOfCodePuzzle {
  public Day02(Path input) {
    super(input);
  }

  // Rock=1, paper=2, scissors=3
  // Win=6, lose=0, draw=3
  Map<String, Integer> partOneOutcomes = Map.ofEntries(
    entry("A X", 1 + 3), // Rock vs Rock DRAW 1 + 3
    entry("A Y", 2 + 6), // Rock vs Paper WIN 2 + 6
    entry("A Z", 3 + 0), // Rock vs Scissors LOSE 3 + 0
    entry("B X", 1 + 0), // Paper vs Rock LOSE 1 + 0
    entry("B Y", 2 + 3), // Paper vs Paper DRAW 2 + 3
    entry("B Z", 3 + 6), // Paper vs Scissors WIN 3 + 6
    entry("C X", 1 + 6), // Scissors vs Rock WIN 1 + 6
    entry("C Y", 2 + 0), // Scissors vs Paper LOSE 2 + 0
    entry("C Z", 3 + 3) // Scissors vs Scissors DRAW 3 + 3
  );

  // Part Two, X=LOSE, Y=DRAW, Z=WIN
  Map<String, Integer> partTwoOutcomes = Map.ofEntries(
    entry("A X", 3 + 0), // Rock lose: chose scissors 3 + 0 = 3
    entry("A Y", 1 + 3), // Rock draw: choose Rock - 1 + 3 = 4
    entry("A Z", 2 + 6), // Rock win: choose paper 2 + 6 = 8
    entry("B X", 1 + 0), // Paper lose choose rock 1 + 0 = 1
    entry("B Y", 2 + 3), // Paper draw choose paper - 2 + 3 = 5
    entry("B Z", 3 + 6), // Paper win choose scissors: 3 + 6 = 9
    entry("C X", 2 + 0), // Scissors lose: choose paper 2 + 0 = 2
    entry("C Y", 3 + 3), // Scissors draw: choose scissors 3 + 3 = 6
    entry("C Z", 1 + 6) // Scissors win: choose rock: 1 + 6 = 7
  );

  @Override
  public String partOne() throws Exception {
    var score = Files.lines(inputFile)
      .map(line -> partOneOutcomes.get(line))
      .reduce(0, Integer::sum);

    return score.toString();
  }

  @Override
  public String partTwo() throws Exception {
    var score = Files.lines(inputFile)
      .map(line -> partTwoOutcomes.get(line))
      .reduce(0, Integer::sum);

    return score.toString();
  }
}
