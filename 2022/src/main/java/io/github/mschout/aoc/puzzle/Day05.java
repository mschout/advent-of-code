package io.github.mschout.aoc.puzzle;

import com.google.common.io.Files;
import io.github.mschout.aoc.App;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine.Command;
import picocli.CommandLine.ParentCommand;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

@Command(name = "day05", description = "Solve Day 5 Puzzle")
@Slf4j
public class Day05 implements Callable<Integer> {
  private BufferedReader inputReader;

  @ParentCommand
  private App app;

  @Override
  public Integer call() throws Exception {
    part1();
    part2();

    return 0;
  }

  void part1() throws IOException {
    inputReader = openInputFile();

    var stacks = parseStacks(inputReader);
    stacks.dump();

    inputReader.lines()
      .filter(s -> s.startsWith("move"))
      .map(CrateMovement::parse)
      .forEach(stacks::move);

    System.out.println("Part 1: " + stacks.getMessage());
    assert (stacks.getMessage().equals("TWSGQHNHL"));
  }

  void part2() throws IOException {
    inputReader = openInputFile();

    var stacks = parseStacks(inputReader);
    stacks.dump();

    inputReader.lines()
      .filter(s -> s.startsWith("move"))
      .map(CrateMovement::parse)
      .forEach(stacks::movePreservingOrder);

    System.out.println("Part 2: " + stacks.getMessage());
    assert (stacks.getMessage().equals("JNRSCDWPP"));
  }

  private CrateStacks parseStacks(BufferedReader inputReader) throws IOException {
    CrateStacks crateStacks = new CrateStacks(0);

    var inputStacks = inputReader.lines()
      .takeWhile(s -> !s.isBlank())
      .toList();

    for (int i = inputStacks.size() - 1; i >= 0; i--) {
      var line = inputStacks.get(i);
      if (i == inputStacks.size() - 1) {
        var numbers = line.split("\\s+");
        var numStacks = Integer.parseInt(numbers[numbers.length - 1]);
        crateStacks = new CrateStacks(numStacks);
      }
      else {
        for (int stackNo = 0; stackNo < crateStacks.getNumStacks(); stackNo++) {
          // letters are at position 1, 5, 9, ...
          var currentChar = line.charAt(4 * stackNo + 1);

          if (Character.isLetter(currentChar)) {
            crateStacks.pushItem(stackNo, currentChar);
          }
        }
      }
    }

    return crateStacks;
  }

  private Stack<String> readStacksInput(BufferedReader inputReader) throws IOException {
    var stacksInput = new Stack<String>();

    String line;
    while ((line = inputReader.readLine()) != null) {
      // bail out if we reached the end of the stacks definition
      if (line.isBlank()) break;

      stacksInput.push(line);
    }

    return stacksInput;
  }

  private BufferedReader openInputFile() throws FileNotFoundException {
    return Files.newReader(app.getInput(2022, 5).toFile(), StandardCharsets.UTF_8);
  }

  record CrateMovement(int count, int fromStack, int toStack) {
    // parse a movement line such as:
    // "move 2 from 3 to 4"
    static CrateMovement parse(String action) {
      var scanner = new Scanner(action);
      scanner.useDelimiter(" ");

      // move 2
      scanner.next();
      var count = scanner.nextInt();

      // from 3
      scanner.next();
      var from = scanner.nextInt() - 1;

      // to 4
      scanner.next();
      var to = scanner.nextInt() - 1;

      return new CrateMovement(count, from, to);
    }
  }

  class CrateStacks {
    @Getter
    private int numStacks;

    List<Stack<Character>> stacks = new ArrayList<>();

    CrateStacks(int numStacks) {
      this.numStacks = numStacks;

      for (int i = 0; i < numStacks; i++)
        stacks.add(new Stack<>());
    }

    void pushItem(int stackNumber, Character item) {
      stacks.get(stackNumber).push(item);
    }

    void move(CrateMovement action) {
      for (int i = 0; i < action.count; i++)
        stacks.get(action.toStack).push(stacks.get(action.fromStack).pop());

      System.out.printf("Moved %d from stack %d to stack %d\n", action.count, action.fromStack, action.toStack);
    }

    void movePreservingOrder(CrateMovement action) {
      // Push itmes onto a temp stack, then pop that to the destination. Result is moved
      // in same order
      var crane = new Stack<Character>();

      for (int i = 0; i < action.count; i++)
        crane.push(stacks.get(action.fromStack).pop());

      while (!crane.isEmpty())
        stacks.get(action.toStack).push(crane.pop());
    }

    String getMessage() {
      return stacks.stream()
        .map(s -> s.peek().toString())
        .collect(Collectors.joining());
    }

    void dump() {
      for (int i = 0; i < stacks.size(); i++) {
        System.out.printf("%d: %s\n", i + 1, stacks.get(i));
      }
    }
  }
}
