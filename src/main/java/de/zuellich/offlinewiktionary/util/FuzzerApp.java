package de.zuellich.offlinewiktionary.util;

import de.zuellich.offlinewiktionary.core.archive.PageIndexParser;
import de.zuellich.offlinewiktionary.core.archive.PageIndexWriter;
import de.zuellich.offlinewiktionary.core.archive.SeekEntry;
import de.zuellich.offlinewiktionary.core.archive.WiktionaryReader;
import de.zuellich.offlinewiktionary.core.markup.MarkupParser;
import de.zuellich.offlinewiktionary.core.resolution.AdjacentResolutionStrategy;
import de.zuellich.offlinewiktionary.core.wiki.WikiPage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

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

  private FuzzerApp(Path index, @Nullable Path archivePath) {
    this.index = index;
    if (archivePath == null) {
      AdjacentResolutionStrategy resolutionStrategy = new AdjacentResolutionStrategy(index);
      this.archive = resolutionStrategy.resolve().archive();
    } else {
      this.archive = archivePath;
    }
  }

  public void run() throws IOException, InterruptedException {
    System.out.printf("Using archive file '%s'...%n", archive);
    System.out.printf("Reading index file '%s'...%n", index);
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
    System.out.println("Waiting for computation...");
    final List<Future<Collection<SeekEntry>>> futures = pool.invokeAll(result);
    for (Future<Collection<SeekEntry>> task : futures) {
      final Collection<SeekEntry> seekEntries = task.resultNow();
      allErrors.addAll(seekEntries);
    }

    pool.shutdown();
    System.out.printf("Number of entries that couldn't be parsed: %d%n", allErrors.size());
    PageIndexWriter.write(allErrors, Path.of("./test-output.txt.bz2"));
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

  /** Primitive implementation to split a collection into similarly sized sub collections. */
  public static <E> List<ArrayDeque<E>> bucketize(final Collection<E> entries, final int buckets) {
    // if we have fewer entries than buckets, we'll return no buckets. Math.min ensures we always
    // return at least the
    // amount of buckets possible if more was requested.
    final int amountOfBuckets = Math.min(entries.size(), buckets);
    var result = new ArrayList<ArrayDeque<E>>(amountOfBuckets);
    int desiredBucketSize = Math.floorDiv(entries.size(), amountOfBuckets);

    ArrayDeque<E> bucket = new ArrayDeque<>(desiredBucketSize);
    for (E entry : entries) {
      bucket.add(entry);
      if (bucket.size() == desiredBucketSize) {
        result.add(bucket);
        if (result.size() < amountOfBuckets) {
          bucket = new ArrayDeque<>(desiredBucketSize);
        }
      }
    }

    return result;
  }

  public static void main(String[] args) throws Exception {
    Path index = Path.of(args[0]);
    Path archive = null;
    if (args.length > 1) {
      archive = Path.of(args[1]);
    }

    final FuzzerApp fuzzerApp = new FuzzerApp(index, archive);
    fuzzerApp.run();
  }
}
