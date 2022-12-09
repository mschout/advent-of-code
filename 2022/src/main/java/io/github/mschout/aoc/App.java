/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package io.github.mschout.aoc;

import com.google.common.base.Strings;
import io.github.mschout.aoc.puzzle.*;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Callable;

// TODO - can picocli autoload these somehow?
@Command(
  subcommands = {
      Day02.class,
      Day03.class,
      Day04.class,
      Day05.class,
      Day06.class
  })
public class App implements Callable<Integer> {
  private static final String INPUT_PATH = "src/main/resources/puzzle-input";

  public static void main(String[] args) {
    int exitCode = new CommandLine(new App()).execute(args);
    System.exit(exitCode);
  }

  @Override
  public Integer call() throws Exception {
    return null;
  }

  /**
   * Get path to the input file for the given puzzle day. If the file is not present, the
   * input will be downloaded from Advent of code on demand.
   * @param year
   * @param day
   * @return path to the input file
   */
  public Path getInput(int year, int day) {
    Path filename = Paths.get(INPUT_PATH, String.format("%04d/%02d.txt", year, day))
      .normalize()
      .toAbsolutePath();

    if (!Files.exists(filename))
      downloadInputFile(year, day, filename);

    return filename;
  }

  /**
   * Downloads the input file from advent of code. SESSION must be present in env
   * @param year
   * @param day
   * @param filename
   */
  private void downloadInputFile(int year, int day, Path filename) {
    String session = System.getenv("SESSION");
    if (Strings.isNullOrEmpty(session))
      throw new RuntimeException("SESSION must be set in ENV!");

    try {
      filename.getParent().toFile().mkdirs();

      HttpClient client = HttpClient.newBuilder()
        .version(HttpClient.Version.HTTP_1_1)
        .build();

      var uri = new URI(String.format("https://adventofcode.com/%d/day/%d/input", year, day));

      HttpRequest request = HttpRequest.newBuilder(uri)
        .header("Cookie", "session=" + session)
        .build();

      HttpResponse<String> response = client.send(request, BodyHandlers.ofString());

      if (response.statusCode() != 200)
        throw new RuntimeException("Failed to download input file, response code=" + response.statusCode());

      Files.writeString(filename, response.body());
    }
    catch (URISyntaxException | IOException | InterruptedException e) {
      throw new RuntimeException("Failed to download input file", e);
    }
  }
}
