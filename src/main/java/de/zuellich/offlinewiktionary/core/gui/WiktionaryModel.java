package de.zuellich.offlinewiktionary.core.gui;

import de.zuellich.offlinewiktionary.core.archive.SeekEntry;
import de.zuellich.offlinewiktionary.core.archive.WiktionaryReader;
import de.zuellich.offlinewiktionary.core.resolution.AdjacentResolutionStrategy;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.nio.file.Path;
import java.util.Optional;
import java.util.TreeSet;
import javafx.beans.property.*;

public class WiktionaryModel {
  private TreeSet<SeekEntry> definitions;
  private WiktionaryReader wiktionaryReader = new WiktionaryReader(null);
  private final BooleanProperty isReady = new SimpleBooleanProperty(false);
  private final ReadOnlyObjectWrapper<Path> index = new ReadOnlyObjectWrapper<Path>(null);

  public WiktionaryModel() {}

  @SuppressFBWarnings(
      value = "EI",
      justification = "We do want to expose this property for use in JavaFX")
  public BooleanProperty isReadyProperty() {
    return isReady;
  }

  public void setDefinitions(TreeSet<SeekEntry> definitions) {
    this.definitions = new TreeSet<>(definitions);
    this.isReady.set(true);
  }

  public Optional<String> lookupDefinition(String query) {
    final SeekEntry possibleEntry = definitions.ceiling(new SeekEntry(0, 0, query));
    if (possibleEntry == null) {
      return Optional.empty();
    }

    return Optional.of(wiktionaryReader.retrieve(possibleEntry.bytesToSeek()));
  }

  @SuppressFBWarnings(
      value = "EI",
      justification = "We do want to expose this property for use in JavaFX")
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
}
