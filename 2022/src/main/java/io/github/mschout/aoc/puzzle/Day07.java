package io.github.mschout.aoc.puzzle;

import com.google.common.base.Splitter;
import io.github.mschout.aoc.AdventOfCodePuzzle;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class Day07 extends AdventOfCodePuzzle {
  private final ElfDeviceFS device = new ElfDeviceFS();

  public Day07(Path inputFile) {
    super(inputFile);
  }

  @Override
  public String partOne() throws Exception {
    device.load(inputFile);

    return "" + device.getRoot().sumByMaxSize(100_000);
  }

  @Override
  public String partTwo() throws Exception {
    final var diskSize = 70_000_000;
    final var requiredFreeSpace = 30_000_000;

    final var currentFreeSpace = diskSize - device.getRoot().getSize();

    var minSizeToDelete = requiredFreeSpace - currentFreeSpace;

    var fileToDelete = device.getRoot().findFileToDelete(minSizeToDelete);

    return "" + fileToDelete.getSize();
  }

  @NoArgsConstructor
  @Getter
  class ElfDeviceFS {
    private final File root = new File("/");

    private File currentDir = root;

    public void load(Path input) throws IOException {
      Files.lines(input).forEach(line -> {
        if (line.startsWith("$")) {
          // we only need to do something if we are changing directory.
          // we can ignore "$ ls" lines.
          if (line.startsWith("$ cd")) {
            // change directory
            chdir(line.split("\\s+")[2]);
          }
        }
        else {
          // its output
          addFile(line);
        }
      });
    }

    void chdir(String filename) {
      if (filename.equals("..")) {
        currentDir = currentDir.getParent();
      }
      else {
        var file = findFile(filename).orElseThrow();

        if (!file.isDir())
          throw new IllegalArgumentException(filename + " is not a directory!");

        currentDir = file;
      }
    }

    void addFile(String inputLine) {
      var fields = Splitter.on(" ").splitToList(inputLine);

      if (fields.get(0).equals("dir"))
        currentDir.addSubFile(new File(fields.get(1)));
      else
        currentDir.addSubFile(new File(fields.get(1), Integer.parseInt(fields.get(0))));
    }

    private Optional<File> findFile(String name) {
      if (name.equals("/"))
        return Optional.of(getRoot());

      return currentDir.getSubFiles().stream().filter(file -> file.getName().equals(name)).findFirst();
    }
  }

  @Getter
  class File {
    private final String name;
    private final int size;
    private final boolean isDir;
    private File parent;
    private final Set<File> subFiles = new HashSet<>();

    File(String name, int size) {
      this.name = name;
      this.size = size;
      this.isDir = false;
    }

    File(String name) {
      this.name = name;
      this.size = 0;
      this.isDir = true;
    }

    void addSubFile(File file) {
      file.parent = this;
      subFiles.add(file);
    }

    int getSize() {
      // if its a directory call size on each item
      if (size == 0)
        return subFiles.stream().mapToInt(File::getSize).sum();

      return size;
    }

    int sumByMaxSize(int maxSize) {
      var currentFileSize = getSize();

      if (currentFileSize > maxSize)
        currentFileSize = 0;

      return currentFileSize + subFiles.stream()
        .filter(File::isDir)
        .mapToInt(file -> file.sumByMaxSize(maxSize))
        .sum();
    }

    List<File> allDirs() {
      var dirs = new ArrayList<File>();

      if (isDir)
        dirs.add(this);

      subFiles.stream()
        .filter(File::isDir)
        .forEach(dir -> dirs.addAll(dir.allDirs()));

      return dirs;
    }

    File findFileToDelete(int minSize) {
      var file = allDirs().stream()
        .filter(dir -> dir.getSize() >= minSize)
        .min(Comparator.comparingInt(File::getSize));

      return file.orElseThrow();
    }
  }
}
