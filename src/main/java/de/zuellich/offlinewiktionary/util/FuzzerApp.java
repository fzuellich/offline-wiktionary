package de.zuellich.offlinewiktionary.util;

import de.zuellich.offlinewiktionary.core.archive.PageIndexParser;
import de.zuellich.offlinewiktionary.core.archive.SeekEntry;
import de.zuellich.offlinewiktionary.core.archive.WiktionaryReader;
import de.zuellich.offlinewiktionary.core.markup.MarkupParser;
import de.zuellich.offlinewiktionary.core.resolution.AdjacentResolutionStrategy;
import de.zuellich.offlinewiktionary.core.wiki.WikiPage;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class FuzzerApp {

  private static final int THREAD_POOL_SIZE =
      Math.max(2, Runtime.getRuntime().availableProcessors() - 4);
  private static final int NO_OF_BUCKETS = THREAD_POOL_SIZE;
  private final Path index;
  private final Path archive;

  private static class FuzzerTask implements Callable<Collection<SeekEntry>> {
    private final Path archive;
    private final ArrayDeque<SeekEntry> entries;

    public FuzzerTask(Path archive, ArrayDeque<SeekEntry> entries) {
      this.archive = archive;
      this.entries = entries;
    }

    @Override
    public Collection<SeekEntry> call() {
      final Collection<SeekEntry> result = new ArrayList<>();
      final WiktionaryReader wiktionaryReader = new WiktionaryReader(archive);

      while (!entries.isEmpty()) {
        final SeekEntry remove = entries.remove();
        final Optional<WikiPage> retrieve = wiktionaryReader.retrieve(remove);
        if (retrieve.isPresent()) {
          try {
            MarkupParser.parseString(retrieve.get().text());
          } catch (Exception e) {
            result.add(remove);
          }
        }
      }

      System.out.println("Done: " + result.size());
      return result;
    }
  }

  private FuzzerApp(Path index) {
    this.index = index;
    AdjacentResolutionStrategy resolutionStrategy = new AdjacentResolutionStrategy(index);
    this.archive = resolutionStrategy.resolve().archive();
  }

  public void run() throws IOException, InterruptedException {
    System.out.println("Reading index file...");
    Collection<SeekEntry> entries = readIndex();
    System.out.printf("Splitting into %d buckets...%n", NO_OF_BUCKETS);
    Collection<ArrayDeque<SeekEntry>> buckets = bucketize(entries, NO_OF_BUCKETS);
    final ExecutorService pool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
    List<FuzzerTask> result = new ArrayList<>(NO_OF_BUCKETS);
    for (ArrayDeque<SeekEntry> bucket : buckets) {
      System.out.printf("Submit task with %d elements%n", bucket.size());
      result.add(new FuzzerTask(archive, bucket));
    }

    final Collection<SeekEntry> allErrors = new ArrayList<>();
    final List<Future<Collection<SeekEntry>>> futures = pool.invokeAll(result);
    System.out.println("Waiting for computation...");
    for (Future<Collection<SeekEntry>> task : futures) {
      final Collection<SeekEntry> seekEntries = task.resultNow();
      allErrors.addAll(seekEntries);
    }

    pool.shutdown();
    System.out.printf("Number of entries that couldn't be parsed: %d%n", allErrors.size());
    writeErrors(allErrors, Path.of("./test-output.txt"));
  }

  /** Take an index file and read all entries */
  private Set<SeekEntry> readIndex() throws IOException {
    final InputStream in = Files.newInputStream(index);
    return new PageIndexParser()
        .parse(in).stream()
            .filter(e -> !e.title().startsWith("Wiktionary:"))
            .filter(e -> !e.title().startsWith("Verzeichnis:"))
            .filter(e -> !e.title().startsWith("Vorlage:"))
            .filter(e -> !e.title().startsWith("Modul:"))
            .filter(e -> !e.title().startsWith("MediaWiki:"))
            .filter(e -> !e.title().startsWith("Kategorie:"))
            .filter(e -> !e.title().startsWith("Hilfe:"))
            .filter(e -> !e.title().startsWith("Flexion:"))
            .filter(e -> !e.title().startsWith("Reim:"))
            .collect(Collectors.toSet());
  }

  /** Write a list of Seek entries to the given path. */
  private void writeErrors(Collection<SeekEntry> entries, Path output) {
    try (BufferedWriter writer = Files.newBufferedWriter(output, StandardOpenOption.CREATE)) {
      for (SeekEntry e : entries) {
        writer.write(String.format("%d;%d;%s%n", e.bytesToSeek(), e.articleId(), e.title()));
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /** Primitive implementation to split a collection into similarly sized sub collections. */
  public static <E> List<ArrayDeque<E>> bucketize(Collection<E> entries, int buckets) {
    var result = new ArrayList<ArrayDeque<E>>(buckets);
    final int desiredBucketSize = Math.floorDiv(entries.size(), buckets);

    ArrayDeque<E> bucket = new ArrayDeque<>(desiredBucketSize);
    for (E entry : entries) {
      bucket.add(entry);
      if (bucket.size() == desiredBucketSize) {
        result.add(bucket);
        if (result.size() < buckets) {
          bucket = new ArrayDeque<>(desiredBucketSize);
        }
      }
    }

    return result;
  }

  public static void main(String[] args) throws Exception {
    Path index = Path.of(args[0]);

    final FuzzerApp fuzzerApp = new FuzzerApp(index);
    fuzzerApp.run();
  }
}
