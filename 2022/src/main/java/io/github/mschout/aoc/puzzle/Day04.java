package io.github.mschout.aoc.puzzle;

import io.github.mschout.aoc.App;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParentCommand;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

@Command(name = "day04", description = "Solve Day 4 Puzzle")
public class Day04 implements Callable<Integer> {
  @Option(names = "-f")
  private String file;

  @ParentCommand
  App app;

  @Override
  public Integer call() throws Exception {
    var assignments = Files.lines(Paths.get(file))
      .map(PairAssignment::fromInput)
      .toList();

    var containedAssignments = assignments.stream()
      .filter(a -> a.get(0).contains(a.get(1)) || a.get(0).isContainedBy(a.get(1)))
      .toList();

    System.out.println("There are " + containedAssignments.size() + " contained assignments");

    var overlappingAssignments = assignments.stream()
      .filter(a -> (a.get(0).overlaps(a.get(1))))
      .toList();

    overlappingAssignments.forEach(a -> System.out.println(a.toString()));
    System.out.println("There are " + overlappingAssignments.size() + " overlapping assignments");

    return 0;
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
      var bounds = Arrays.stream(rangeInput.split("\\-"))
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
