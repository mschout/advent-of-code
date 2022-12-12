package io.github.mschout.aoc;

import picocli.CommandLine;
import picocli.CommandLine.Option;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.util.concurrent.Callable;

public class App implements Callable<Integer> {
  @Option(names = { "--year", "-y" }, description = "Puzzle Year")
  private int year = ZonedDateTime.now().get(ChronoField.YEAR);

  @Option(names = { "--day", "-d" }, description = "Puzzle Day", required = true)
  private int day;

  public static void main(String[] args) {
    int exitCode = new CommandLine(new App()).execute(args);
    System.exit(exitCode);
  }

  @Override
  public Integer call() throws Exception {
    var aoc = new AdventOfCode(year, day);

    return aoc.run();
  }
}
