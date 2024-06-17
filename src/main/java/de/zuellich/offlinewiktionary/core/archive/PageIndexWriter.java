package de.zuellich.offlinewiktionary.core.archive;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collection;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorOutputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;

public class PageIndexWriter {
  public static void write(Collection<SeekEntry> entries, Path destination) {
    StringBuilder fileContent = new StringBuilder();
    for (SeekEntry e : entries) {
      fileContent.append(String.format("%d:%d:%s%n", e.bytesToSeek(), e.articleId(), e.title()));
    }

    try (final OutputStream out = Files.newOutputStream(destination, StandardOpenOption.CREATE);
        final BufferedOutputStream bout = new BufferedOutputStream(out);
        final CompressorOutputStream stream =
            new CompressorStreamFactory()
                .createCompressorOutputStream(CompressorStreamFactory.BZIP2, bout); ) {
      stream.write(fileContent.toString().getBytes(StandardCharsets.UTF_8));
    } catch (IOException | CompressorException e) {
      e.printStackTrace();
    }
  }
}
