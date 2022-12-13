package io.github.mschout.aoc.puzzle;

import com.google.common.collect.Lists;
import io.github.mschout.aoc.AdventOfCodePuzzle;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Day10 extends AdventOfCodePuzzle {
  private final CPU cpu = new CPU();

  public Day10(Path inputFile) {
    super(inputFile);
  }

  @Override
  public String partOne() throws Exception {
    try (var lines = Files.lines(inputFile)) {
      lines.forEach(cpu::processInstruction);
    }

    var totalStrength = cpu.getObservedSignalStrength().stream().reduce(0, Integer::sum);

    return String.valueOf(totalStrength);
  }

  @Override
  public String partTwo() throws Exception {
    return cpu.getDisplay();
  }

  @NoArgsConstructor
  static class CPU {
    @Getter
    private int cycles = 0;

    @Getter
    private int register = 1;

    @Getter
    private final List<Integer> observedSignalStrength = new ArrayList<>();

    private final StringBuilder crt = new StringBuilder();

    public void processInstruction(String instruction) {
      var fields = instruction.split("\\s+");

      switch (fields[0]) {
        case "noop" -> nextTick();
        case "addx" -> {
          nextTick(2);
          register += Integer.parseInt(fields[1]);
        }
        default -> throw new IllegalArgumentException("Unrecognized instruction: " + instruction);
      }
    }

    private void nextTick(int cycles) {
      for (int i = 0; i < cycles; i++)
        nextTick();
    }

    private void nextTick() {
      drawCrtPixel();

      cycles += 1;

      if ((cycles - 20) % 40 == 0) {
        observedSignalStrength.add(register * cycles);
      }
    }

    private void drawCrtPixel() {
      var spritePosition = register;
      var currentPixel = cycles % 40;

      // NOTE: I used unicode blocks, which are easier to readn than "#."
      if (spritePosition >= currentPixel - 1 && spritePosition <= currentPixel + 1)
        crt.append(Character.toString(0x2593));
      else
        crt.append(Character.toString(0x2591));
    }

    // Get what is currently showing on the screen
    public String getDisplay() {
      var display = new StringBuilder();
      var crtChars = Lists.charactersOf(crt.toString());

      for (int i = 0; i < crtChars.size(); i++) {
        if (i > 0 && (i % 40) == 0) display.append("\n");
        display.append(crtChars.get(i));
      }

      return "\n" + display;
    }
  }
}
