package io.github.mschout.aoc.puzzle;

import com.google.common.collect.Lists;
import io.github.mschout.aoc.AdventOfCodePuzzle;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Day08 extends AdventOfCodePuzzle {
  public Day08(Path inputFile) {
    super(inputFile);
  }

  @Override
  public String partOne() throws Exception {
    var grid = new TreeGrid(Files.lines(inputFile).toList());

    return "" + grid.countVisible();
  }

  @Override
  public String partTwo() throws Exception {
    var grid = new TreeGrid(Files.lines(inputFile).toList());
    return String.valueOf(grid.maxScenicScore());
  }

  static class TreeGrid {
    private final int size;
    private final int[][] treeHeights;

    TreeGrid(List<String> inputLines) {
      size = inputLines.size();
      treeHeights = new int[size][size];

      for (int y = 0; y < size; y++) {
        var heights = Lists.charactersOf(inputLines.get(y))
          .stream()
          .map(ch -> Integer.parseInt(ch.toString()))
          .toList();

        for (int x = 0; x < heights.size(); x++)
          treeHeights[y][x] = heights.get(x);
      }
    }

    int countVisible() {
      int visibleCount = 0;

      // since outer trees are all visible, only need to look at interior elements 1..n-1
      // in either direction
      for (int y = 0; y < size; y++) {
        for (int x = 0; x < size; x++) {
          if (isVisible(x, y)) visibleCount++;
        }
      }

      return visibleCount;
    }

    int maxScenicScore() {
      var scores = new ArrayList<Integer>();

      // we can exclude outer edges since those will have a score in one direction of 0
      for (int x = 1; x < size - 1; x++) {
        for (int y = 1; y < size - 1; y++)
          scores.add(scenicScore(x, y));
      }

      return scores.stream().max(Integer::compare).orElseThrow();
    }

    boolean isVisible(int x, int y) {
      return isVisibleX(x, y) || isVisibleY(x, y);
    }

    boolean isVisibleX(int x, int y) {
      // outside edge of grid is visible
      if (x == 0 || x == size - 1) return true;

      var treeHeight = getTreeHeight(x, y);

      return isVisibleInXRange(y, 0, x, treeHeight)
          || isVisibleInXRange(y, x + 1, size, treeHeight);
    }

    boolean isVisibleInXRange(int y, int fromx, int tox, int height) {
      for (int i = fromx; i < tox; i++) {
        if (getTreeHeight(i, y) >= height) return false;
      }

      return true;
    }

    boolean isVisibleY(int x, int y) {
      // outside edge of grid is visible
      if (y == 0 || y == size - 1) return true;

      var treeHeight = getTreeHeight(x, y);

      return isVisibleInYRange(x, 0, y, treeHeight)
          || isVisibleInYRange(x, y + 1, size, treeHeight);
    }

    private boolean isVisibleInYRange(int x, int fromy, int toy, int height) {
      for (int i = fromy; i < toy; i++)
        if (getTreeHeight(x, i) >= height) return false;

      return true;
    }

    int getTreeHeight(int x, int y) {
      return treeHeights[y][x];
    }

    int scenicScore(int x, int y) {
      return scenicScoreUp(x, y) * scenicScoreRight(x, y) * scenicScoreDown(x, y) * scenicScoreLeft(x, y);
    }

    private int scenicScoreUp(int x, int y) {
      var treeHeight = getTreeHeight(x, y);

      int score = 0;

      for (int i = y - 1; i >= 0; i--) {
        score++;

        if (getTreeHeight(x, i) >= treeHeight) break;
      }

      return score;
    }

    private int scenicScoreDown(int x, int y) {
      var treeHeight = getTreeHeight(x, y);

      int score = 0;

      for (int i = y + 1; i < size; i++) {
        score++;
        if (getTreeHeight(x, i) >= treeHeight) break;
      }

      return score;
    }

    private int scenicScoreLeft(int x, int y) {
      final var treeHeight = getTreeHeight(x, y);

      int score = 0;

      for (int i = x - 1; i >= 0; i--) {
        score++;
        if (getTreeHeight(i, y) >= treeHeight) break;
      }

      return score;
    }

    private int scenicScoreRight(int x, int y) {
      final var treeHeight = getTreeHeight(x, y);

      int score = 0;

      for (int i = x + 1; i < size; i++) {
        score++;
        if (getTreeHeight(i, y) >= treeHeight) break;
      }

      return score;
    }
  }
}
