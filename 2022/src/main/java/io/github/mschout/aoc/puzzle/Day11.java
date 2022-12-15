package io.github.mschout.aoc.puzzle;

import com.google.common.collect.Lists;
import io.github.mschout.aoc.AdventOfCodePuzzle;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;
import java.util.function.LongBinaryOperator;
import java.util.stream.Collectors;

@Slf4j
public class Day11 extends AdventOfCodePuzzle {
  public Day11(Path inputFile) {
    super(inputFile);
  }

  @Override
  public String partOne() throws Exception {
    List<Monkey> monkeys = parseMonkeys(inputFile);

    for (int i = 0; i < 20; i++)
      for (var monkey : monkeys)
        monkey.takeTurn(monkeys, null);

    return computePuzzleAnswer(monkeys);
  }

  @Override
  public String partTwo() throws Exception {
    List<Monkey> monkeys = parseMonkeys(inputFile);

    // in part 2, no worry reduction happens. The way to keey worry manageable is to
    // multiply the test divisors of all
    // monkeys together. They happen to be prime, so this produces the least common
    // multiple that we can mod with the item worry to
    // keep it manageable.
    var lcm = monkeys.stream().map(m -> m.getTestDivisor()).reduce((a, b) -> a * b).orElseThrow();

    for (int i = 0; i < 10_000; i++)
      for (var monkey : monkeys)
        monkey.takeTurn(monkeys, lcm);

    return computePuzzleAnswer(monkeys);
  }

  private List<Monkey> parseMonkeys(Path inputFile) throws IOException {
    List<Monkey> monkeys = new ArrayList<>();

    try (var lineStream = Files.lines(inputFile)) {
      monkeys.addAll(
        Lists.partition(lineStream.toList(), 7)
          .stream()
          .map(Monkey::new)
          .toList());
    }

    return monkeys;
  }

  private String computePuzzleAnswer(List<Monkey> monkeys) {
    monkeys.forEach(m -> System.out.println("Monkey inspection count: " + m.getInspectionCount()));

    var answer = monkeys.stream()
      .mapToLong(Monkey::getInspectionCount)
      .boxed()
      .sorted(Comparator.reverseOrder())
      .limit(2)
      .reduce(1L, (a, b) -> a * b);

    return String.valueOf(answer);
  }

  @Slf4j
  static class Monkey {
    @Getter
    private final List<Long> items = new ArrayList<>();

    @Getter
    private int inspectionCount = 0;

    private final Function<Long, Long> operation;

    @Getter
    private final long testDivisor;

    private final int trueMonkey;
    private final int falseMonkey;

    Monkey(List<String> definition) {
      log.info("Parsing definition: {}", definition.stream().collect(Collectors.joining()));

      var startingItems = Arrays.stream(definition.get(1).split(": ")[1].split(", "))
        .map(Long::parseLong)
        .toList();

      items.addAll(startingItems);

      operation = parseOperation(definition.get(2));
      testDivisor = Long.parseLong(definition.get(3).split(" divisible by ")[1]);
      trueMonkey = Integer.parseInt(definition.get(4).split(" to monkey ")[1]);
      falseMonkey = Integer.parseInt(definition.get(5).split(" to monkey ")[1]);

      log.info("Parsed monkey: {}", this);
    }

    Function<Long, Long> parseOperation(String definition) {
      var opdef = definition.split(" = ")[1];

      var opTokens = opdef.split(" ");
      if (opTokens.length != 3) throw new IllegalArgumentException("Invalid operation def: " + opdef);

      // It is assumed operation is only "+" or "*"
      LongBinaryOperator op = opTokens[1].equals("+") ? Math::addExact : Math::multiplyExact;

      // RHS of the operation is either "old" or, a number
      if (opTokens[2].equals("old"))
        return old -> op.applyAsLong(old, old);
      else
        return old -> op.applyAsLong(old, Integer.parseInt(opTokens[2]));
    }

    void takeTurn(List<Monkey> monkeys, Long lcm) {
      while (!items.isEmpty()) {
        var item = items.remove(0);

        inspectionCount++;

        // apply monkey operation
        item = operation.apply(item);

        if (lcm == null) {
          log.info("Using releif strategy");
          item = item / 3; // relief, divid by 3
        }
        else {
          log.info("Using LCM strategy (LCM = {})", lcm);
          item = item % lcm; // no relief, keep worry managable.
        }

        // test, divisible
        if ((item % testDivisor) == 0) {
          log.info("TRUE Throwing item {} to monkey {}", item, trueMonkey);
          monkeys.get(trueMonkey).catchItem(item);
        }
        else {
          log.info("FALSE Throwing item {} to monkey {}", item, falseMonkey);
          monkeys.get(falseMonkey).catchItem(item);
        }
      }
    }

    public void catchItem(Long item) {
      items.add(item);
    }
  }
}
