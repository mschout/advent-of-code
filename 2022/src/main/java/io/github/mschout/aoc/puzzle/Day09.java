package io.github.mschout.aoc.puzzle;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Iterables;
import com.google.common.collect.Table;
import io.github.mschout.aoc.AdventOfCodePuzzle;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.awt.geom.Point2D;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
public class Day09 extends AdventOfCodePuzzle {
  public Day09(Path inputFile) {
    super(inputFile);
  }

  @Override
  public String partOne() throws Exception {
    var rope = new Rope(2);

    try (var reader = Files.newBufferedReader(inputFile)) {
      reader.lines().forEach(line -> {
        var move = Move.parse(line);
        rope.moveHead(move);
      });
    }

    return String.valueOf(rope.getTailVisited().size());
  }

  @Override
  public String partTwo() throws Exception {
    var rope = new Rope(10);

    try (var reader = Files.newBufferedReader(inputFile)) {
      reader.lines().forEach(line -> {
        var move = Move.parse(line);
        rope.moveHead(move);
      });
    }

    return String.valueOf(rope.getTailVisited().size());
  }

  record Move(@Getter Day09.Move.Direction direction, @Getter int quantity) {
    enum Direction {
      UP,
      RIGHT,
      DOWN,
      LEFT
    }

    static Move parse(String input) {
      var fields = input.split("\\s+");
      if (fields.length != 2)
        throw new IllegalArgumentException("Bad move input: " + input);

      var direction = switch (fields[0]) {
        case "U" -> Direction.UP;
        case "R" -> Direction.RIGHT;
        case "D" -> Direction.DOWN;
        case "L" -> Direction.LEFT;
        default -> throw new IllegalArgumentException("Bad direction input: " + fields[0]);
      };

      return new Move(direction, Integer.parseInt(fields[1]));
    }
  }

  @Slf4j
  static class Rope {
    private final List<Point2D> rope;

    private final Point2D origin = new Point2D.Double(0, 0);

    @Getter
    private final Set<Point2D> tailVisited = new HashSet<>();

    // The possible knot moves, one square in any direction, or 0,0 to not move it.
    private final Table<Integer, Integer, Point2D> possibleMoves = HashBasedTable.create();

    Rope(int length) {
      rope = new ArrayList<>(length);

      for (int i = 0; i < length; i++)
        rope.add(new Point2D.Double(0, 0));

      tailVisited.add(origin);

      // precompute the possible 1x1 point move objects to save memory
      for (int x = -1; x <= 1; x++)
        for (int y = -1; y <= 1; y++)
          possibleMoves.put(x, y, new Point2D.Double(x, y));
    }

    public Point2D getTailLocation() {
      var tail = Iterables.getLast(rope);

      return new Point2D.Double(tail.getX(), tail.getY());
    }

    void moveHead(Move move) {
      for (int i = 0; i < move.quantity(); i++) {
        // for each quantity in the move, move all knots by one position
        for (int knotNum = 0; knotNum < rope.size(); knotNum++) {
          var currentKnot = rope.get(knotNum);

          if (knotNum == 0) // head knot
            moveByOne(currentKnot, move.direction());
          else // non-head knot
            moveKnotIfNecessary(currentKnot, rope.get(knotNum - 1));
        }

        tailVisited.add(getTailLocation());
      }
    }

    /**
     * Move the given knot by one in the given direction
     * @param knot the knot to move
     * @param direction the direction to move
     */
    private void moveByOne(Point2D knot, Move.Direction direction) {
      var x = knot.getX();
      var y = knot.getY();

      switch (direction) {
        case UP -> y++;
        case RIGHT -> x++;
        case DOWN -> y--;
        case LEFT -> x--;
      }

      knot.setLocation(x, y);
    }

    // Move the knot (if needed) so that it is within one square of the knot in front of
    // it
    void moveKnotIfNecessary(Point2D currentKnot, Point2D nextKnot) {
      var point = getRequiredKnotMove(currentKnot, nextKnot);

      if (!point.equals(origin)) {
        currentKnot.setLocation(currentKnot.getX() + point.getX(), currentKnot.getY() + point.getY());
        assert currentKnot.distance(nextKnot) < 2.0;
      }
    }

    /**
     * Returns one of the eight possible moves for the given knot as a point. eg: move
     * left one: (-1,0) dont move: (0,0) up-right: (1,1)
     * @param currentKnot The current knot in the rope to move
     * @param nextKnot The next knot closer to the head (or the head itself)
     * @return Point representing required move
     */
    private Point2D getRequiredKnotMove(Point2D currentKnot, Point2D nextKnot) {
      var distance = nextKnot.distance(currentKnot);

      if (distance >= 2.0) {
        var dx = nextKnot.getX() - currentKnot.getX();
        var dy = nextKnot.getY() - currentKnot.getY();

        var x = dx < 0 ? -1 : dx > 0 ? 1 : 0;
        var y = dy < 0 ? -1 : dy > 0 ? 1 : 0;

        return possibleMoves.get(x, y);
      }

      // no move required
      return possibleMoves.get(0, 0);
    }
  }
}
