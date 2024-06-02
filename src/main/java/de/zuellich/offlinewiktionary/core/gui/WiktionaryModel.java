package de.zuellich.offlinewiktionary.core.gui;

import de.zuellich.offlinewiktionary.core.LevenshteinDistanceMatcher;
import de.zuellich.offlinewiktionary.core.archive.SeekEntry;
import de.zuellich.offlinewiktionary.core.archive.WiktionaryReader;
import de.zuellich.offlinewiktionary.core.resolution.AdjacentResolutionStrategy;
import de.zuellich.offlinewiktionary.core.wiki.WikiPage;
import java.nio.file.Path;
import java.util.*;
import javafx.beans.property.*;

public class WiktionaryModel {
  /** Maximum number of suggestions to return. */
  private static final int NO_OF_SUGGESTIONS = 5;

  /** Only consider suggestions that are at most X changes from the original term. */
  private static final int MAX_EDIT_DISTANCE = 1;

  private TreeSet<SeekEntry> definitions = new TreeSet<>();
  private WiktionaryReader wiktionaryReader;
  private final BooleanProperty isReady = new SimpleBooleanProperty(false);
  private final ReadOnlyObjectWrapper<Path> index = new ReadOnlyObjectWrapper<Path>(null);

  public WiktionaryModel(WiktionaryReader reader) {
    this.wiktionaryReader = reader;
  }

  public BooleanProperty isReadyProperty() {
    return isReady;
  }

  public void setDefinitions(Set<SeekEntry> definitions) {
    this.definitions = new TreeSet<>(definitions);
    this.isReady.set(true);
  }

  public Optional<WikiPage> lookupDefinition(String query) {
    final SeekEntry possibleEntry = definitions.ceiling(SeekEntry.forQuery(query));
    if (possibleEntry == null) {
      return Optional.empty();
    }

    // Don't use equalsIgnoreCase here, we want to make sure we know the difference between a word
    // with lowercase letter
    // or uppercase letter.
    if (!possibleEntry.title().equals(query)) {
      return Optional.empty();
    }

    return wiktionaryReader.retrieve(possibleEntry);
  }

  public ReadOnlyObjectProperty<Path> indexProperty() {
    return index.getReadOnlyProperty();
  }

  public void setIndexFile(final Path indexFile) {
    final AdjacentResolutionStrategy resolutionStrategy = new AdjacentResolutionStrategy(indexFile);
    final AdjacentResolutionStrategy.ResolutionResult resolve = resolutionStrategy.resolve();
    Path archiveFile = resolve.archive();
    index.set(resolve.index());
    wiktionaryReader = new WiktionaryReader(archiveFile);
  }

  /** Return a list of terms that are close to the given term. */
  public Set<String> findSuggestions(String term) {
    /*
     * TODO Instead of calculating the distance for every term consider:
     * a) aborting calculation once the maximum is reached
     * b) use a trie data structure to avoid iterating all words
     */
    final Set<String> result = new HashSet<>(NO_OF_SUGGESTIONS);
    final LevenshteinDistanceMatcher of = LevenshteinDistanceMatcher.of(term);
    for (SeekEntry definition : definitions) {
      if (of.distanceTo(definition.title()) <= MAX_EDIT_DISTANCE) {
        result.add(definition.title());
        if (result.size() == NO_OF_SUGGESTIONS) {
          return result;
        }
      }
    }

    return result;
  }
}
