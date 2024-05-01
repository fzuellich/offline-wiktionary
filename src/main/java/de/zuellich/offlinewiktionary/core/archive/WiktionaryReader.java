package de.zuellich.offlinewiktionary.core.archive;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.xml.sax.SAXException;

public class WiktionaryReader {

  private final Path wiktionaryArchive;

  public WiktionaryReader(Path wiktionaryArchive) {
    this.wiktionaryArchive = wiktionaryArchive;
  }

  public String retrieve(long byteOffset) {
    WikiPageSAXParser mine = new WikiPageSAXParser();
    try (final InputStream in = Files.newInputStream(wiktionaryArchive);
        final BufferedInputStream bin = new BufferedInputStream(in); ) {
      long skipped = bin.skip(byteOffset);
      if (skipped == 0) {
        return "EOF";
      }

      final CompressorInputStream stream =
          new CompressorStreamFactory().createCompressorInputStream(bin);

      SAXParserFactory factory = SAXParserFactory.newInstance();
      SAXParser saxParser = factory.newSAXParser();
      saxParser.parse(stream, mine);

      return "TEST";
    } catch (CompressorException | IOException | ParserConfigurationException | SAXException e) {
      e.printStackTrace();
    }

    return "";
  }
}
