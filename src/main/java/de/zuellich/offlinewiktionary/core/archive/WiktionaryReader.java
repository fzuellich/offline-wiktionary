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

  public WiktionaryReader() {}

  public String retrieve(long byteOffset) {
    WikiPageSAXParser mine = new WikiPageSAXParser();
    try (final InputStream in =
            Files.newInputStream(
                Path.of(
                    "/home/fzuellich/Downloads/dewiktionary-20240401-pages-articles-multistream.xml.bz2"));
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

      /*final BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
      StringBuilder resultBuilder = new StringBuilder();
      String currentLine = reader.readLine();
      while (!currentLine.trim().equals("</page>")) {
          resultBuilder.append(currentLine);
          currentLine = reader.readLine();
      }
      reader.close();
      stream.close();*/
      System.out.println(mine.getResult());
      return "TEST";
    } catch (CompressorException | IOException | ParserConfigurationException | SAXException e) {
      e.printStackTrace();
    }

    return "";
  }
}
