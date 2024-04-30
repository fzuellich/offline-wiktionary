package de.zuellich.offlinewiktionary.core;

import de.zuellich.offlinewiktionary.core.archive.PageIndexParser;
import de.zuellich.offlinewiktionary.core.archive.SeekEntry;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.TreeSet;
import javafx.concurrent.Task;

public class ImportTask extends Task<Void> {
  private final WiktionaryApp.WiktionaryModel model;

  public ImportTask(WiktionaryApp.WiktionaryModel model) {
    this.model = model;
  }

  @Override
  protected Void call() throws Exception {
    updateMessage("Parsing...");

    final InputStream in =
        Files.newInputStream(
            Path.of(
                "/home/fzuellich/Downloads/dewiktionary-20240401-pages-articles-multistream-index.txt.bz2"));
    final TreeSet<SeekEntry> definitions = new PageIndexParser().parse(in);
    model.setDefinitions(definitions);
    updateMessage("Done!");
    return null;
  }
}
