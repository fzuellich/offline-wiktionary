package de.zuellich.offlinewiktionary.core.archive;

import de.zuellich.offlinewiktionary.core.wiki.WikiPage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.SAXException;

/** Wraps the {@link WikiPageSAXParser} to parse wiki pages from a compressed archive. */
public class WiktionaryArchiveReader {
  private SAXParser parser;

  public WiktionaryArchiveReader() {}

  private SAXParser getParser() {
    if (parser == null) {
      SAXParserFactory factory = SAXParserFactory.newInstance();
      try {
        parser = factory.newSAXParser();
      } catch (ParserConfigurationException | SAXException e) {
        throw new RuntimeException("Something went wrong initializing the SAXParser");
      }
    }

    return parser;
  }

  public HashMap<Integer, WikiPage> parse(InputStream toWrap) throws IOException, SAXException {
    final WikiPageSAXParser wikiPageSAXParser = new WikiPageSAXParser();
    InputStream wrappedStream =
        new SequenceInputStream(
            Collections.enumeration(
                Arrays.asList(
                    new ByteArrayInputStream("<root>".getBytes(StandardCharsets.UTF_8)),
                    toWrap,
                    new ByteArrayInputStream("</root>".getBytes(StandardCharsets.UTF_8)))));
    getParser().parse(wrappedStream, wikiPageSAXParser);

    return wikiPageSAXParser.getResult();
  }
}
