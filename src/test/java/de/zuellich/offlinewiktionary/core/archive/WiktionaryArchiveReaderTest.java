package de.zuellich.offlinewiktionary.core.archive;

import static org.junit.jupiter.api.Assertions.*;

import de.zuellich.offlinewiktionary.core.wiki.WikiPage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import javax.xml.parsers.ParserConfigurationException;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

class WiktionaryArchiveReaderTest {

  @Test
  public void testCanParseSimplePage()
      throws ParserConfigurationException, SAXException, IOException {
    final ByteArrayInputStream inputStream =
        new ByteArrayInputStream(Fixtures.SIMPLE_PAGE.getBytes(StandardCharsets.UTF_8));

    final WiktionaryArchiveReader wiktionaryArchiveReader = new WiktionaryArchiveReader();

    final Map<Integer, WikiPage> result = wiktionaryArchiveReader.parse(inputStream);
    assertEquals(1, result.size());
    final WikiPage pageResult = result.get(2682);
    assertNotNull(pageResult);
    assertEquals("love", pageResult.title());
    assertEquals(2682, pageResult.id());
    assertEquals("text/x-wiki", pageResult.format());
    assertTrue(pageResult.text().startsWith("{{also|Love|LoVe"));
    assertEquals(1269, pageResult.text().length());
  }

  @Test
  public void testCanParseMultipleConcatenatedPages()
      throws ParserConfigurationException, SAXException, IOException {
    final ByteArrayInputStream inputStream =
        new ByteArrayInputStream(Fixtures.CONCATENATED_PAGES.getBytes(StandardCharsets.UTF_8));

    final WiktionaryArchiveReader wiktionaryArchiveReader = new WiktionaryArchiveReader();
    final Map<Integer, WikiPage> result = wiktionaryArchiveReader.parse(inputStream);

    assertEquals(2, result.size());
    assertTrue(result.containsKey(6110313));
    assertTrue(result.containsKey(2682));
  }
}
