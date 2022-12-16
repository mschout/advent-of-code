package io.github.mschout.aoc.puzzle;

import com.google.gson.*;
import io.github.mschout.aoc.AdventOfCodePuzzle;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
public class Day13 extends AdventOfCodePuzzle {
  private JsonParser parser = new JsonParser();
  private final List<Pair<List<Object>, List<Object>>> packetPairs;

  public Day13(Path inputFile) throws IOException {
    super(inputFile);

    var input = Files.readString(inputFile);

    packetPairs = Arrays.stream(input.split("\n\n"))
      .map(this::parsePacketPair)
      .toList();
  }

  Pair<List<Object>, List<Object>> parsePacketPair(String input) {
    var packets = Arrays.stream(input.split("\n"))
      .map(this::parsePacket)
      .toList();

    assert packets.size() == 2;

    return Pair.of(packets.get(0), packets.get(1));
  }

  @Override
  public String partOne() throws Exception {
    var correctIndexes = new ArrayList<Integer>();

    for (int i = 0; i < packetPairs.size(); i++) {
      var pair = packetPairs.get(i);

      if (compare(pair.getLeft(), pair.getRight()) <= 0)
        correctIndexes.add(i + 1);
    }

    return String.valueOf(correctIndexes.stream().reduce(0, Integer::sum));
  }

  @Override
  public String partTwo() throws Exception {
    var packets = new ArrayList<>(
      packetPairs.stream().flatMap(p -> Stream.of(p.getLeft(), p.getRight())).toList());

    // Add divider packets
    var dividerPackets = List.of(
      parsePacket("[[2]]"),
      parsePacket("[[6]]"));

    packets.addAll(dividerPackets);

    // sort
    var sortedPackets = packets.stream().sorted(this::compare).toList();

    var key = dividerPackets.stream()
      .map(p -> sortedPackets.indexOf(p) + 1)
      .reduce((a, b) -> a * b)
      .orElseThrow();

    return String.valueOf(key);
  }

  private List<Object> parsePacket(String inputLine) {
    var json = JsonParser.parseString(inputLine);
    return parsePacketFromJsonArray(json.getAsJsonArray());
  }

  private List<Object> parsePacketFromJsonArray(JsonArray elements) {
    var items = new ArrayList<>();

    elements.forEach(e -> {
      if (e.isJsonArray())
        items.add(parsePacketFromJsonArray(e.getAsJsonArray()));
      else
        items.add(e.getAsInt());
    });

    return items;
  }

  private int compare(Object left, Object right) {

    if (left instanceof Integer leftInt && right instanceof Integer rightInt)
      return leftInt - rightInt;

    // If left list runs out of items first, it is in the right order.
    if (left instanceof List<?> leftList && right instanceof List<?> rightList) {
      if (leftList.isEmpty())
        return rightList.isEmpty() ? 0 : -1;

      var minSize = Math.min(leftList.size(), rightList.size());

      for (int i = 0; i < minSize; i++) {
        int check = compare(leftList.get(i), rightList.get(i));
        if (check != 0) return check;
      }

      // all elements matched.
      return leftList.size() - rightList.size();
    }

    // otherwise one side is an int and the other is a list.
    if (left instanceof Integer leftInt)
      return compare(List.of(leftInt), right);

    if (right instanceof Integer rightInt)
      return compare(left, List.of(rightInt));

    throw new RuntimeException("Should not happen");
  }
}
