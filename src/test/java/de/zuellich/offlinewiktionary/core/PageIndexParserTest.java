package de.zuellich.offlinewiktionary.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import de.zuellich.offlinewiktionary.core.archive.PageIndexParser;
import de.zuellich.offlinewiktionary.core.archive.SeekEntry;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.TreeSet;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;
import org.junit.jupiter.api.Test;

class PageIndexParserTest {

  @Test
  public void testCanReadWellFormedArchive() throws IOException {
    var clearText = """
1234:12:Title of a page with whitespace
4567:34:Single""";
    var temp = new ByteArrayOutputStream();
    var compressor = new BZip2CompressorOutputStream(temp);
    compressor.write(clearText.getBytes(StandardCharsets.UTF_8));
    compressor.flush();
    compressor.close();
    temp.flush();

    var input = new ByteArrayInputStream(temp.toByteArray());
    var importer = new PageIndexParser();

    final TreeSet<SeekEntry> result = new TreeSet<>(importer.parse(input));
    input.close();

    assertEquals(2, result.size());
    assertNotNull(result.ceiling(new SeekEntry(1234, 12, "Title of a page with whitespace")));
    assertNotNull(result.ceiling(new SeekEntry(4567, 34, "Single")));
  }
}
