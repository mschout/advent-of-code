package io.github.mschout.aoc.puzzle;

import io.github.mschout.aoc.AdventOfCodePuzzle;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Pattern;

import static java.lang.Math.abs;

public class Day15 extends AdventOfCodePuzzle {
  private final Set<Sensor> sensors = new HashSet<>();
  private final Set<Point> notBeaconPoints = new HashSet<>();

  public Day15(Path inputFile) throws IOException {
    super(inputFile);

    var pattern = Pattern.compile("x=(-?\\d+), y=(-?\\d+)");

    try (var lineStream = Files.lines(inputFile)) {
      lineStream.forEach(line -> {
        var matcher = pattern.matcher(line);

        var points = new ArrayList<Point>();

        while (matcher.find()) {
          points.add(new Point(Long.parseLong(matcher.group(1)), Long.parseLong(matcher.group(2))));
        }

        assert points.size() == 2;

        sensors.add(new Sensor(points.get(0), points.get(0).distance(points.get(1))));
        notBeaconPoints.addAll(points);
      });
    }
  }

  @Override
  public String partOne() throws Exception {
    var y = 2_000_000;

    sensors.forEach(sensor -> {
      var dy = abs(sensor.y() - y);
      var numRowHashes = (sensor.distanceToBeacon() * 2 + 1) - (2 * dy);

      if (numRowHashes == 1) {
        notBeaconPoints.add(new Point(sensor.x(), y));
      }
      else if (numRowHashes > 0) {
        var offset = (numRowHashes - 1) / 2;
        for (long i = sensor.x() - offset; i < sensor.x() + offset; i++)
          notBeaconPoints.add(new Point(i, y));
      }
    });

    var answer = notBeaconPoints.stream()
      .filter(point -> point.y() == y)
      .count();

    return String.valueOf(answer);
  }

  // This one was tricky.
  //
  // We're told that:
  // - The beacon is somewhere in the [0,0] to [4000000,4000000] square
  // - No sensor is detecting the beacon
  // All we know from the input is where the beacon *CANT* be.
  // From this we conclude that there is exactly one point within this square that
  // contains the beacon, and
  // is not within range of any other sensor.

  // But how to find where it is? We will waste a lot of time if we check every single
  // point within the search
  // area.
  // Hint: walk just outside the perimeter of each sensor. For each point:
  // - Make sure that the current point's distance to the sensor is > sensor range
  // - and also it is within the (0,0) - (4_000_000,4_000_000) square.
  // - Check that the point is not within range of another sensor
  // If all of these are true, then this is the location of the distress beacon
  @Override
  public String partTwo() throws Exception {
    var point = findDistressBeacon();

    if (point == null) throw new RuntimeException("Did not find the distress beacon");

    return String.valueOf(point.x() * 4_000_000 + point.y());
  }

  private Point findDistressBeacon() {
    // Keep track of points we already checked.
    var visited = new HashSet<Point>();

    for (var sensor : sensors) {
      LinkedList<Point> pointsToVisit = new LinkedList<>();

      pointsToVisit.add(sensor.firstPerimeterPoint());

      while (!pointsToVisit.isEmpty()) {
        var point = pointsToVisit.remove();

        if (visited.contains(point))
          continue;

        visited.add(point);

        pointsToVisit.addAll(sensor.nextPerimeterPoints(point));

        // skip this point if it is outside the search grid.
        if (point.x() < 0 || point.y() < 0 || point.x() > 4_000_000 || point.y() > 4_000_000)
          continue;

        // If this location is not in range of another sensor we found the beacon.
        if (sensors.stream().noneMatch(s -> s.isPointInRange(point))) {
          return point;
        }
      }
    }

    // We didn't find it.
    return null;
  }

  record Point(long x, long y) {
    public long distance(Point other) {
      return abs(other.x - x) + abs(other.y - y);
    }
  }

  record Sensor(Point location, long distanceToBeacon) {
    public long x() {
      return location.x();
    }

    public long y() {
      return location().y();
    }

    public boolean isPointInRange(Point p) {
      return p.distance(location) <= distanceToBeacon;
    }

    // given an x position, return the next set of perimeter points. That is, starting
    // left to right, one or
    // two points that are just outside the perimeter of the sensor in either direction on
    // the y-axis.
    public Set<Point> nextPerimeterPoints(Point currentPoint) {
      var x = currentPoint.x();
      var minX = x() - distanceToBeacon - 1;
      var maxX = x() + distanceToBeacon + 1;

      x += 1;

      // If we are not even within the search grid, adjust X, or bail out if we went past
      // the end
      if (x < 0) x = 0;

      if (x > 4_000_000)
        return Collections.emptySet();

      // Outside of sensor perimeter, no points returned.
      if (x < minX || x > maxX)
        return Collections.emptySet();

      // outside left/right edge. Single point
      if (x == minX || x == maxX)
        return Set.of(new Point(x, y()));

      var points = new HashSet<Point>(2);

      // at minX, maxX, dy = 0 (the left most point of the diamond)
      // minX + 1 || maxX - 1, dy = 1
      // minX + 2 || maxX - 2, dy = 2 ... etc
      var dy = x - minX;

      // only consider points that are within the search area.
      if (y() + dy >= 0 && y() + dy <= 4_000_000)
        points.add(new Point(x, y() + dy));

      if (y() - dy >= 0 && y() - dy <= 4_000_000)
        points.add(new Point(x, y() - dy));

      return points;
    }

    // get left most point of the sensor perimeter. Following perimeter points can be
    // fetched with nextPerimeterPoints(p.x())
    public Point firstPerimeterPoint() {
      return new Point(x() - distanceToBeacon - 1, y());
    }
  }
}
