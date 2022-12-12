package io.github.mschout.aoc.puzzle;

import io.github.mschout.aoc.AdventOfCodePuzzle;
import lombok.Getter;
import lombok.SneakyThrows;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

// TODO: refactor, Apache Commons has a Range type that could shorten this a bit.
public class Day04 extends AdventOfCodePuzzle {
  public Day04(Path inputFile) {
    super(inputFile);
  }

  @Getter(lazy = true)
  private final List<PairAssignment> assignments = buildPairAssignments();

  @SneakyThrows
  private List<PairAssignment> buildPairAssignments() {
    return Files.lines(inputFile).map(PairAssignment::fromInput).toList();
  }

  @Override
  public String partOne() {
    var containedAssignments = getAssignments().stream()
      .filter(a -> a.get(0).contains(a.get(1)) || a.get(0).isContainedBy(a.get(1)))
      .toList();

    return "" + containedAssignments.size();
  }

  @Override
  public String partTwo() {
    var overlappingAssignments = getAssignments().stream()
      .filter(a -> (a.get(0).overlaps(a.get(1))))
      .toList();

    return "" + overlappingAssignments.size();
  }

  record PairAssignment(List<SectionRange> ranges) {
    public static PairAssignment fromInput(String line) {
      var r = Arrays.stream(line.split(","))
        .map(SectionRange::fromInput)
        .toList();

      return new PairAssignment(r.get(0), r.get(1));
    }

    public PairAssignment(SectionRange firstRange, SectionRange secondRange) {
      this(List.of(firstRange, secondRange));
    }

    public SectionRange get(int idx) {
      return ranges.get(idx);
    }

    public String toString() {
      return ranges.get(0) + "-" + ranges.get(1);
    }
  }

  record SectionRange(int startSection, int endSection) {
    public static SectionRange fromInput(String rangeInput) {
      var bounds = Arrays.stream(rangeInput.split("-"))
        .map(Integer::parseInt)
        .toList();

      return new SectionRange(bounds.get(0), bounds.get(1));
    }

    boolean contains(SectionRange other) {
      return startSection <= other.startSection && endSection >= other.endSection;
    }

    boolean isContainedBy(SectionRange other) {
      return other.contains(this);
    }

    boolean overlaps(SectionRange other) {
      // Either this overlaps the other section, or, the other section overlaps this one.
      return (this.startSection <= other.startSection && this.endSection >= other.startSection)
          || (other.startSection <= startSection && other.endSection >= this.startSection);
    }
  }
}
