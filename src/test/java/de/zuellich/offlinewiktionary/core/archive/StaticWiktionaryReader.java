package de.zuellich.offlinewiktionary.core.archive;

import de.zuellich.offlinewiktionary.core.wiki.WikiPage;
import java.util.Optional;

/**
 * Very simple class to mock WiktionaryReader behaviour without having to use a Mocking framework.
 */
public class StaticWiktionaryReader extends WiktionaryReader {
  private final Optional<WikiPage> staticResult;

  public StaticWiktionaryReader(Optional<WikiPage> staticResult) {
    super(null);
    this.staticResult = staticResult;
  }

  @Override
  public Optional<WikiPage> retrieve(SeekEntry entry) {
    return staticResult;
  }
}
