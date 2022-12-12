package io.github.mschout.aoc.puzzle;

import com.google.common.collect.EvictingQueue;
import io.github.mschout.aoc.AdventOfCodePuzzle;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

@Slf4j
public class Day06 extends AdventOfCodePuzzle {
  public Day06(Path inputFile) {
    super(inputFile);
  }

  @Override
  public String partOne() throws Exception {
    var bytesRead = findStartSignal(4);
    return "" + bytesRead;
  }

  @Override
  public String partTwo() throws Exception {
    var bytesRead = findStartSignal(14);
    return "" + bytesRead;
  }

  private int findStartSignal(int markerLength) throws IOException {
    var input = Files.newBufferedReader(inputFile);

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
