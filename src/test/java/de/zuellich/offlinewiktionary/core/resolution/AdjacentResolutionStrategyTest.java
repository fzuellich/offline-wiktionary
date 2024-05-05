package de.zuellich.offlinewiktionary.core.resolution;

import static org.junit.jupiter.api.Assertions.*;

import de.zuellich.offlinewiktionary.core.archive.WiktionaryReader;
import de.zuellich.offlinewiktionary.core.exception.NoArchiveFoundException;
import de.zuellich.offlinewiktionary.core.gui.WiktionaryModel;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.Test;

class AdjacentResolutionStrategyTest {

  @Test
  public void testThrowsIfNoArchiveIsFoundAdjacent() {
    final var model = new WiktionaryModel(new WiktionaryReader(null));
    try {
      final URL resourceURL =
          AdjacentResolutionStrategyTest.class.getResource(
              "/fixtures/archives/noAdjacentArchive/wiktionary-20240401-pages-articles-multistream-index.txt.bz2");
      final Path resourceAsPath = Paths.get(resourceURL.toURI());
      model.setIndexFile(resourceAsPath);
    } catch (NoArchiveFoundException e) {
      return;
    } catch (URISyntaxException e) {
      fail("Error getting path to fixture.");
      return;
    }

    fail("Desired exception has not been thrown!");
  }

  @Test
  public void testCanFindAdjacentArchive() {
    final var model = new WiktionaryModel(new WiktionaryReader(null));
    try {
      final URL resourceURL =
          AdjacentResolutionStrategyTest.class.getResource(
              "/fixtures/archives/adjacentArchive/wiktionary-20240401-pages-articles-multistream-index.txt.bz2");
      final Path resourceAsPath = Paths.get(resourceURL.toURI());
      model.setIndexFile(resourceAsPath);
    } catch (NoArchiveFoundException e) {
      fail("Couldn't find adjacent archive!");
    } catch (URISyntaxException e) {
      fail("Error getting path to fixture.");
    }
  }
}
