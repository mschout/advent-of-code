package io.github.mschout.aoc;

import com.google.common.base.Strings;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.io.IOException;
import java.io.PrintStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RequiredArgsConstructor
public class AdventOfCode {
  private static final String INPUT_PATH = "src/main/resources/puzzle-input";
  private static final String RESULTS_PATH = "src/main/resources/puzzle-solutions";

  private final int year;

  private final int day;

  @Getter(lazy = true)
  private final Path inputPath = buildIntputPath();

  @Getter(lazy = true)
  private final PrintStream output = buildOutputStream();

  public int run() {
    try {
      var constructor = Class.forName(String.format("%s.puzzle.Day%02d", this.getClass().getPackageName(), day))
        .getDeclaredConstructor(Path.class);

      var puzzleDay = (AdventOfCodePuzzle) constructor.newInstance(getInputPath());

      var partOneSolution = puzzleDay.partOne();
      getOutput().println("Part One Answer: " + partOneSolution);

      var partTwoSolution = puzzleDay.partTwo();
      getOutput().println("Part Two Answer: " + partTwoSolution);
    }
    catch (Exception e) {
      System.err.println("Could not execute day " + day + e.getLocalizedMessage());
      e.printStackTrace();
      return 1;
    }

    return 0;
  }

  private Path buildIntputPath() {
    var inputPath = Paths.get(INPUT_PATH, String.format("%04d/%02d.txt", year, day));

    if (!inputPath.toFile().exists())
      downloadPuzzleInput(inputPath);

    return inputPath;
  }

  @SneakyThrows
  private PrintStream buildOutputStream() {
    var outputPath = Paths.get(RESULTS_PATH, String.format("%04d/%02d.txt", year, day));

    var parentDir = outputPath.getParent().toFile();
    if (!parentDir.exists())
      parentDir.mkdirs();

    return new PrintStream(Files.newOutputStream(outputPath));
  }

  // Fetch the puzzle input from advent of code and store in the input file
  private void downloadPuzzleInput(Path inputPath) {
    String session = System.getenv("SESSION");
    if (Strings.isNullOrEmpty(session))
      throw new RuntimeException("SESSION must be set in ENV!");

    try {
      inputPath.getParent().toFile().mkdirs();

      HttpClient client = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build();

      var uri = new URI(String.format("https://adventofcode.com/%d/day/%d/input", year, day));

      HttpRequest request = HttpRequest.newBuilder(uri)
        .header("Cookie", "session=" + session)
        .build();

      HttpResponse<String> response = client.send(request, BodyHandlers.ofString());

      if (response.statusCode() != 200)
        throw new RuntimeException("Failed to download input file, response code=" + response.statusCode());

      Files.writeString(inputPath, response.body());
    }
    catch (URISyntaxException | IOException | InterruptedException e) {
      throw new RuntimeException("Failed to download input file", e);
    }
  }
}
