package de.zuellich.offlinewiktionary.core.archive;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Scanner;
import java.util.TreeSet;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;

/**
 * Wikimedia exports support multi-stream files that contain multiple BZIP2 streams. To find data in
 * these files an index is published that contains line-separated entries of the form:
 * `bytesToSeek:articleId:pageTitle`. This parser extracts a {@link TreeSet} of these entries.
 *
 * <p>TODO: Handle exception properly
 */
public class PageIndexParser {

  /** A BZIP2 compressed input stream, will be wrapped in a {@link BufferedInputStream}. */
  public TreeSet<SeekEntry> parse(InputStream in) {
    final TreeSet<SeekEntry> result = new TreeSet<>();
    try (final BufferedInputStream bin = new BufferedInputStream(in);
        final CompressorInputStream stream =
            new CompressorStreamFactory().createCompressorInputStream(bin);
        final Scanner scanner = new Scanner(stream, Charset.defaultCharset()); ) {
      scanner.useDelimiter(":");
      while (scanner.hasNext()) {
        long bytesToSeek = scanner.nextLong();
        int articleId = scanner.nextInt();
        scanner.skip(":");
        String title = scanner.nextLine();
        result.add(new SeekEntry(bytesToSeek, articleId, title));
      }
    } catch (IOException | CompressorException e) {
      e.printStackTrace();
    }

    return result;
  }
}
