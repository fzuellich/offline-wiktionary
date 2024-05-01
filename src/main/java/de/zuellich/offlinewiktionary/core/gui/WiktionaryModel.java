package de.zuellich.offlinewiktionary.core.gui;

import de.zuellich.offlinewiktionary.core.WiktionaryApp;
import de.zuellich.offlinewiktionary.core.archive.SeekEntry;
import de.zuellich.offlinewiktionary.core.archive.WiktionaryReader;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Optional;
import java.util.TreeSet;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

public class WiktionaryModel {
  private final WiktionaryApp wiktionaryApp;
  private TreeSet<SeekEntry> definitions;
  private WiktionaryReader wiktionaryReader = new WiktionaryReader();
  private BooleanProperty isReady = new SimpleBooleanProperty(false);

  public WiktionaryModel(WiktionaryApp wiktionaryApp) {
    this.wiktionaryApp = wiktionaryApp;
  }

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
}
