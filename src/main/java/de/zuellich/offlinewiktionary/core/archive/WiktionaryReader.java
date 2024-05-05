package de.zuellich.offlinewiktionary.core.archive;

import de.zuellich.offlinewiktionary.core.wiki.WikiPage;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Optional;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.xml.sax.SAXException;

public class WiktionaryReader {

  private final HashMap<Integer, WikiPage> cache = new HashMap<>();
  private final Path wiktionaryArchive;

  public WiktionaryReader(Path wiktionaryArchive) {
    this.wiktionaryArchive = wiktionaryArchive;
  }

  public Optional<WikiPage> retrieve(SeekEntry entry) {
    long byteOffset = entry.bytesToSeek();
    try (final InputStream in = Files.newInputStream(wiktionaryArchive);
        final BufferedInputStream bin = new BufferedInputStream(in); ) {
      long skipped = bin.skip(byteOffset);
      if (skipped == 0) {
        return Optional.empty();
      }

      final CompressorInputStream stream =
          new CompressorStreamFactory().createCompressorInputStream(bin);

      WiktionaryArchiveReader reader = new WiktionaryArchiveReader();
      final HashMap<Integer, WikiPage> result = reader.parse(stream);

      final WikiPage wikiPage = result.get(entry.articleId());

      if (wikiPage == null || !wikiPage.title().equalsIgnoreCase(entry.title())) {
        return Optional.empty();
      } else {
        return Optional.of(wikiPage);
      }
    } catch (CompressorException | IOException | SAXException e) {
      e.printStackTrace();
    }

    return Optional.empty();
  }
}
