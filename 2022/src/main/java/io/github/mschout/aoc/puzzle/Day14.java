package io.github.mschout.aoc.puzzle;

import com.google.common.base.Splitter;
import io.github.mschout.aoc.AdventOfCodePuzzle;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.jooq.lambda.Seq;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Day14 extends AdventOfCodePuzzle {
  // TODO: probably should have called this wallLines or something.
  private final List<RockPath> rockPaths = new ArrayList<>();

  public Day14(Path inputFile) throws IOException {
    super(inputFile);

    try (var lineStream = Files.lines(inputFile)) {
      lineStream.map(RockPath::new).forEach(rockPaths::add);
    }
  }

  @Override
  public String partOne() throws Exception {
    var xVals = rockPaths.stream().flatMap(RockPath::stream).map(Point::getX).collect(Collectors.toSet());

    // We add one unit on either side to easier detect falling off.
    var xMin = Collections.min(xVals) - 1;
    var xMax = Collections.max(xVals) + 1;

    var width = xMax - xMin + 1;
    var height = rockPaths.stream()
      .flatMap(RockPath::stream)
      .map(Point::getY)
      .max(Integer::compare)
      .orElse(0);

    // To simplify, adjust all input points by xOffset so left starts at 0,0
    var normalizedRockPaths = rockPaths.stream().map(p -> p.normalize(xMin)).toList();

    var sandStartLocation = new Point(500 - xMin, 0);

    var cave = new Cave(normalizedRockPaths, width, height + 1, sandStartLocation);

    int unitsPlaced = 0;
    while (cave.addSand() != null) {
      unitsPlaced++;
    }

    cave.dump();

    return String.valueOf(unitsPlaced);
  }

  @Override
  public String partTwo() throws Exception {
    // TODO - should be able to use same width idea for both parts
    // for part two we must make sure we make the cave big enough so the floor can catch
    // all of the sand.
    // Assume sand start location (x=500) is the middle.
    // If the cave is 1 row high, we need width=1 to hold sand, 2 rows=3, 4 rows=5.
    // So width = (2 * height - 1); add +2 to allow one extra space on either side.
    // Now, assume x=500 is the middle of the cave. half of that xmax-xmin is the width
    // covering walls.

    // Height + 2 to account for floor
    var height = 1 + rockPaths.stream()
      .flatMap(RockPath::stream)
      .map(Point::getY)
      .max(Integer::compare)
      .orElse(0) + 2;

    var width = (2 * height - 1) + 2;

    var xOffset = 500 - (width / 2);

    // To simplify, adjust all input points by xOffset so left starts at 0,0
    var normalizedRockPaths = rockPaths.stream().map(p -> p.normalize(xOffset)).toList();

    var sandStartLocation = new Point(500 - xOffset, 0);

    var cave = new Cave(normalizedRockPaths, width, height, sandStartLocation);

    // fill in the floor
    cave.fillWall(new Point(0, cave.getHeight() - 1), new Point(cave.getWidth() - 1, cave.getHeight() - 1));

    // For this part, we can just to BFS from sand start location to find all reachable
    // locations within the cave.
    Set<Point> visited = new HashSet<>();
    LinkedList<Point> toVisit = new LinkedList<>();

    toVisit.add(cave.getSandStartLocation());

    while (!toVisit.isEmpty()) {
      var location = toVisit.remove();

      if (visited.contains(location)) continue;

      visited.add(location);

      toVisit.addAll(cave.getAdjacentEmptyLocations(location));

      cave.setMapValue(location, 'o');
    }

    // Every location we visited will have sand, so that's the answer.
    cave.dump();

    return String.valueOf(visited.size());
  }

  @AllArgsConstructor
  @Getter
  @EqualsAndHashCode
  static class Point {
    private int x;
    private int y;

    static Point parse(String xy) {
      var vals = Arrays.stream(xy.split(",")).map(Integer::parseInt).toList();
      return new Point(vals.get(0), vals.get(1));
    }

    @Override
    public String toString() {
      return String.format("(%d,%d)", x, y);
    }
  }

  static class RockPath extends ArrayList<Point> {
    RockPath(String inputLine) {
      super(Splitter.on(" -> ").splitToList(inputLine).stream().map(Point::parse).toList());
    }

    RockPath(List<Point> points) {
      super(points);
    }

    public RockPath normalize(int xAdjustment) {
      return new RockPath(
        stream().map(point -> new Point(point.getX() - xAdjustment, point.getY())).toList());
    }
  }

  static class Cave {
    private final char[][] floorMap;

    @Getter
    private final int width;

    @Getter
    private final int height;

    @Getter
    private final Point sandStartLocation;

    private final List<RockPath> walls;

    Cave(List<RockPath> walls, int width, int height, Point sandStartLocation) {
      this.walls = walls;
      floorMap = new char[width][height];

      this.width = width;
      this.height = height;
      this.sandStartLocation = sandStartLocation;

      initFloorMap();
    }

    public Point addSand() {
      var location = getNextOpenSandLocation(sandStartLocation);
      if (location != null)
        floorMap[location.getX()][location.getY()] = 'o';

      return location;
    }

    public Point getNextOpenSandLocation(Point currentLocation) {
      var x = currentLocation.getX();
      var y = currentLocation.getY();

      // If we are on the bottom row, we fell into the abyss.
      if (y == height - 1) return null;

      // If spot below is open, continue downward
      var location = new Point(x, y + 1);

      if (containsLocation(location) && isLocationEmpty(location))
        return getNextOpenSandLocation(location);

      // try down-left
      location = new Point(x - 1, y + 1);
      if (containsLocation(location) && isLocationEmpty(location))
        return getNextOpenSandLocation(location);

      // try down-right
      location = new Point(x + 1, y + 1);
      if (containsLocation(location) && isLocationEmpty(location))
        return getNextOpenSandLocation(location);

      // Otherwise, if the location we are currently on is empty, this is the next
      // location
      location = new Point(x, y);
      if (containsLocation(location) && floorMap[x][y] == 0)
        return new Point(x, y);

      // otherwise all possible locations are occupied. return null
      return null;
    }

    public boolean containsLocation(int x, int y) {
      return x >= 0 && x <= width - 1 && y >= 0 && y <= height - 1;
    }

    public boolean containsLocation(Point location) {
      return containsLocation(location.getX(), location.getY());
    }

    public boolean isLocationEmpty(Point location) {
      return floorMap[location.getX()][location.getY()] == 0;
    }

    public Set<Point> getAdjacentEmptyLocations(Point location) {
      // next locations are down, down-left, down-right
      var x = location.getX();
      var y = location.getY();

      // next possible locations, DOWN, DOWN-LEFT, DOWN-RIGHT
      var nextLocations = Stream.of(
        new Point(x, y + 1),
        new Point(x - 1, y + 1),
        new Point(x + 1, y + 1));

      // Only include empty spots that are within the cave.
      return nextLocations.filter(p -> containsLocation(p) && isLocationEmpty(p))
        .collect(Collectors.toSet());
    }

    public void dump() {
      for (int y = 0; y < height; y++) {
        for (int x = 0; x < width; x++) {
          char ch = floorMap[x][y];
          if (ch == 0) ch = '.';

          // Mark sand start location
          if (sandStartLocation.equals(new Point(x, y))) ch = '+';

          System.out.print(Character.valueOf(ch).toString());
        }

        System.out.println();
      }
    }

    private void initFloorMap() {
      // place walls
      for (var wall : walls) {
        Seq.seq(wall.stream())
          .window(0, 1)
          .forEach(window -> {
            // fill line from lag() to current point
            if (window.lag().isPresent())
              fillWall(window.lag().orElseThrow(), window.value());
          });
      }
    }

    public void fillWall(Point start, Point end) {
      var x = start.getX();
      var y = start.getY();

      if (x == end.getX()) {
        // vertical wall
        var yStart = Math.min(start.getY(), end.getY());
        var yEnd = Math.max(start.getY(), end.getY());

        for (int yv = yStart; yv <= yEnd; yv++)
          setMapValue(x, yv, '#');
      }
      else if (y == end.getY()) {
        // horizontal wall
        var xStart = Math.min(start.getX(), end.getX());
        var xEnd = Math.max(start.getX(), end.getX());

        for (int xv = xStart; xv <= xEnd; xv++)
          setMapValue(xv, y, '#');
      }
    }

    public void setMapValue(int x, int y, char value) {
      floorMap[x][y] = value;
    }

    public void setMapValue(Point location, char value) {
      floorMap[location.getX()][location.getY()] = value;
    }
  }
}
