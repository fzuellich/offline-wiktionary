package de.zuellich.offlinewiktionary.core;

import de.zuellich.offlinewiktionary.core.archive.PageIndexParser;
import de.zuellich.offlinewiktionary.core.archive.SeekEntry;
import de.zuellich.offlinewiktionary.core.gui.WiktionaryModel;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import javafx.concurrent.Task;

public class ImportTask extends Task<Void> {
  private final WiktionaryModel model;

  public ImportTask(WiktionaryModel model) {
    this.model = model;
  }

  @Override
  protected Void call() throws Exception {
    final Path targetIndex = model.indexProperty().get();
    if (targetIndex == null) {
      throw new IllegalArgumentException("No target index set on model!");
    }

    updateMessage(String.format("Parsing '%s' ...", targetIndex));

    final InputStream in = Files.newInputStream(targetIndex);
    final Set<SeekEntry> definitions = new PageIndexParser().parse(in);
    model.setDefinitions(definitions);
    updateMessage(String.format("Viewing: '%s'", targetIndex));
    return null;
  }
}
