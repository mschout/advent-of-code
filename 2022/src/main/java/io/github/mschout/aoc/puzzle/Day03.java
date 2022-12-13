package io.github.mschout.aoc.puzzle;

import com.google.common.collect.Lists;
import io.github.mschout.aoc.AdventOfCodePuzzle;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class Day03 extends AdventOfCodePuzzle {
  private final List<Rucksack> rucksacks = new ArrayList<>();

  public Day03(Path inputFile) {
    super(inputFile);
  }

  @Override
  public String partOne() throws Exception {
    try (var lines = Files.lines(inputFile)) {
      lines.forEach(line -> rucksacks.add(new Rucksack(line)));
    }

    // Part one
    var duplicatedSum = rucksacks.stream()
      .flatMap(s -> s.getDuplicatedItems().stream())
      .map(this::itemPriority)
      .reduce(0, Integer::sum);

    return duplicatedSum.toString();
  }

  @Override
  public String partTwo() throws Exception {
    var groups = Lists.partition(rucksacks, 3);

    var groupSum = groups.stream()
      .map(this::findCommonItem)
      .map(this::itemPriority)
      .reduce(0, Integer::sum);

    return groupSum.toString();
  }

  private Character findCommonItem(List<Rucksack> group) {
    // there is one common item in the gruop. Take possible items from first rucksack in
    // the group.
    for (var item : group.get(0).getItems()) {
      if (allElvesAreCarryingItem(group, item))
        return item;
    }

    throw new IllegalArgumentException("No common item found within the group");
  }

  private boolean allElvesAreCarryingItem(List<Rucksack> rucksacks, Character item) {
    for (var rs : rucksacks)
      if (!rs.contains(item)) return false;

    return true;
  }

  private int itemPriority(Character item) {
    if (Character.isUpperCase(item))
      return item - 'A' + 27;
    else
      return item - 'a' + 1;
  }

  static class Rucksack {
    private final List<Compartment> compartments;

    Rucksack(String items) {
      // Problem does not say where to split if the number of items is odd
      if ((items.length() % 2) != 0)
        throw new IllegalArgumentException("Items must be an even number of characters");

      compartments = List.of(
        new Compartment(items.substring(0, items.length() / 2)),
        new Compartment(items.substring(items.length() / 2)));
    }

    public Set<Character> getDuplicatedItems() {
      if (compartments.isEmpty())
        return Collections.emptySet();

      return compartments.get(0)
        .getItems()
        .stream()
        .filter(this::allCompartmentsContainItem)
        .collect(Collectors.toSet());
    }

    public boolean allCompartmentsContainItem(Character item) {
      if (compartments.isEmpty())
        return false;

      for (var compartment : compartments) {
        if (!compartment.contains(item))
          return false;
      }

      return true;
    }

    public boolean contains(Character item) {
      for (var compartment : compartments) {
        if (compartment.contains(item))
          return true;
      }

      return false;
    }

    public Set<Character> getItems() {
      var set = new HashSet<Character>();

      compartments.forEach(compartment -> set.addAll(compartment.getItems()));

      return set;
    }
  }

  static class Compartment {
    private final List<Character> items;

    Compartment(String items) {
      this.items = Lists.charactersOf(items);
    }

    public boolean contains(Character item) {
      return items.contains(item);
    }

    Set<Character> getItems() {
      return new HashSet<>(items);
    }
  }
}
