package io.github.mschout.aoc.puzzle;

import com.google.common.collect.Lists;
import io.github.mschout.aoc.AdventOfCodePuzzle;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Day12 extends AdventOfCodePuzzle {
  private final Grid mapGrid;

  public Day12(Path inputFile) throws IOException {
    super(inputFile);

    mapGrid = new Grid(inputFile);
  }

  @Override
  public String partOne() throws Exception {
    Map<Location, Integer> locationSteps = new HashMap<>();
    return String.valueOf(searchBFS(mapGrid.getDestination(), mapGrid.getStartLocation(), locationSteps));
  }

  @Override
  public String partTwo() throws Exception {
    Map<Location, Integer> locationSteps = new HashMap<>();

    // search the entire grid from destination
    searchBFS(mapGrid.getDestination(), null, locationSteps);

    var possibleStarts = mapGrid.getPossibleStartLocations();

    var minSteps = possibleStarts.stream()
      .map(locationSteps::get)
      .filter(Objects::nonNull) // some start locations can not reach the destination
      .sorted()
      .findFirst()
      .orElseThrow();

    return String.valueOf(minSteps);
  }

  private int searchBFS(Location startLocation, @Nullable Location destination, Map<Location, Integer> locationSteps) {
    Set<Location> visited = new HashSet<>();

    LinkedList<Location> toVisit = new LinkedList<>();

    mapGrid.setStartLocation(startLocation);

    toVisit.add(startLocation);

    // Start location is 0 steps
    locationSteps.put(startLocation, 0);

    while (!toVisit.isEmpty()) {
      var location = toVisit.remove();

      // skip if we already visited this node
      if (visited.contains(location)) continue;

      visited.add(location);

      var steps = locationSteps.get(location);

      // We reached the destination, return number of steps
      if (location.equals(destination)) return steps;

      var neighbors = mapGrid.getReachableNeighbors(location);

      // Update steps for the neighbor
      for (var neighbor : neighbors) {
        if (!locationSteps.containsKey(neighbor))
          locationSteps.put(neighbor, Integer.MAX_VALUE);

        // If we've found a shorter path to the neighbor, update it.
        if (locationSteps.get(neighbor) > steps + 1)
          locationSteps.put(neighbor, steps + 1);
      }

      // Add neighbors to the list of locations to visit
      toVisit.addAll(neighbors);
    }

    // We didn't find the destination
    return -1;
  }

  @Getter
  @Setter
  @EqualsAndHashCode
  static class Location {
    private final int x;
    private final int y;
    private final int elevation;
    // private int steps = Integer.MAX_VALUE;

    public Location(int x, int y, Character elevation) {
      this.x = x;
      this.y = y;

      // convert a-z elevations to ints, S = 'a', E = 'z'
      switch (elevation) {
        case 'S' -> this.elevation = 0;
        case 'E' -> this.elevation = 'z' - 'a';
        default -> this.elevation = elevation - 'a';
      }
    }

    boolean canClimbTo(Location other) {
      return other.elevation <= elevation + 1;
    }
  }

  static class Grid {
    private final Location[][] locations;

    @Getter
    @Setter
    private Location startLocation;

    @Getter
    private Location destination;

    Grid(Path inputFile) throws IOException {
      try (var lineStream = Files.lines(inputFile)) {
        var lines = lineStream.map(Lists::charactersOf).toList();

        locations = new Location[lines.get(0).size()][lines.size()];

        for (int y = 0; y < lines.size(); y++) {
          var lineChars = lines.get(y);

          for (int x = 0; x < lineChars.size(); x++) {
            var ch = lineChars.get(x);
            var location = new Location(x, y, ch);

            locations[x][y] = location;

            if (ch.equals('S')) startLocation = location;

            if (ch.equals('E')) destination = location;
          }
        }
      }
    }

    Location getLocation(int x, int y) {
      return locations[x][y];
    }

    // Find all possible start locations on the map. That is, nodes on the outer edges of
    // the map with elevation 0
    Set<Location> getPossibleStartLocations() {
      var startLocations = new HashSet<Location>();

      // top row
      startLocations.addAll(IntStream.range(0, maxX() + 1).mapToObj(x -> getLocation(x, 0)).toList());

      // left side
      startLocations.addAll(IntStream.range(0, maxY() + 1).mapToObj(y -> getLocation(0, y)).toList());

      // right side
      startLocations.addAll(IntStream.range(0, maxY() + 1).mapToObj(y -> getLocation(maxX(), y)).toList());

      // bottom row
      startLocations.addAll(IntStream.range(0, maxX() + 1).mapToObj(x -> getLocation(x, maxY())).toList());

      // Only include locations with elevation "0"
      return startLocations.stream().filter(l -> l.getElevation() == 0).collect(Collectors.toSet());
    }

    List<Location> getReachableNeighbors(Location location) {
      // we can go up, down, left, right, but only if those locations are on the map, and
      // elevation is no more than curent location + 1
      var neighbors = new ArrayList<Location>(4);

      getRelativeNeighbor(location, 0, 1).ifPresent(neighbors::add); // down
      getRelativeNeighbor(location, 0, -1).ifPresent(neighbors::add); // up
      getRelativeNeighbor(location, 1, 0).ifPresent(neighbors::add); // right
      getRelativeNeighbor(location, -1, 0).ifPresent(neighbors::add); // left

      // Only include neighbors that can climb to this location
      return neighbors.stream().filter(n -> n.canClimbTo(location)).toList();
    }

    private Optional<Location> getRelativeNeighbor(Location location, int dx, int dy) {
      var x = location.getX() + dx;
      var y = location.getY() + dy;

      Location neighbor = null;

      // check that requested location is on the map
      if (x >= 0 && y >= 0 && x <= maxX() && y <= maxY()) {
        neighbor = getLocation(x, y);
      }

      return Optional.ofNullable(neighbor);
    }

    int maxX() {
      return locations.length - 1;
    }

    int maxY() {
      return locations[0].length - 1;
    }
  }
}
