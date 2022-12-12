package io.github.mschout.aoc;

import lombok.RequiredArgsConstructor;

import java.nio.file.Path;

@RequiredArgsConstructor
public abstract class AdventOfCodePuzzle {
  protected final Path inputFile;

  public abstract String partOne() throws Exception;

  public abstract String partTwo() throws Exception;
}
