package de.zuellich.offlinewiktionary.core.resolution;

import de.zuellich.offlinewiktionary.core.exception.NoArchiveFoundException;
import de.zuellich.offlinewiktionary.core.exception.NoIndexFoundException;
import java.nio.file.Path;

/**
 * A resolution strategy that looks for the archive adjacent to the index file based on file name
 * semantics.
 */
public class AdjacentResolutionStrategy {

  public static record ResolutionResult(Path index, Path archive) {}

  private final Path index;

  public AdjacentResolutionStrategy(Path indexFile) {
    this.index = indexFile;
  }

  public ResolutionResult resolve() {
    if (!this.index.toFile().exists()) {
      throw new NoIndexFoundException();
    }

    final Path archive = Path.of(this.index.toString().replace("-index.txt.bz2", ".xml.bz2"));
    if (!archive.toFile().exists()) {
      throw new NoArchiveFoundException();
    }
    return new ResolutionResult(this.index, archive);
  }
}
