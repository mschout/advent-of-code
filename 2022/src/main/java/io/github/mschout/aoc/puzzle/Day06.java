package io.github.mschout.aoc.puzzle;

import com.google.common.collect.EvictingQueue;
import io.github.mschout.aoc.App;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine.Command;
import picocli.CommandLine.ParentCommand;

import java.io.IOException;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;

@Command(name = "day06", description = "Solve Day 6 Puzzle")
@Slf4j
public class Day06 implements Callable<Integer> {
  @ParentCommand
  private App app;

  @Override
  public Integer call() throws Exception {
    part1();

    part2();

    return null;
  }

  private void part1() throws IOException {
    var bytesRead = findStartSignal(4);
    System.out.println("Found start signal after reading " + bytesRead + " bytes");
  }

  private void part2() throws IOException {
    var bytesRead = findStartSignal(14);
    System.out.println("Found start signal after reading " + bytesRead + " bytes");
  }

  private int findStartSignal(int markerLength) throws IOException {
    var input = Files.newBufferedReader(app.getInput(2022, 6));

    var signalDetector = new SignalStartDetector(markerLength);

    char c;
    while ((c = (char) input.read()) > 0) {
      signalDetector.add(c);

      if (signalDetector.haveStartSignal())
        return signalDetector.getBytesRead();
    }

    return -1;
  }

  class SignalStartDetector {
    private final EvictingQueue<Character> window;
    private final int windowSize;

    @Getter
    private int bytesRead = 0;

    SignalStartDetector(final int markerLength) {
      this.window = EvictingQueue.create(markerLength);
      this.windowSize = markerLength;
    }

    public boolean haveStartSignal() {
      Set<Character> uniqChars = new HashSet<>(window);

      return uniqChars.size() == windowSize;
    }

    public void add(Character c) {
      window.add(c);
      bytesRead++;
    }
  }
}
