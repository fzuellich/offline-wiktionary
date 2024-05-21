package de.zuellich.offlinewiktionary.core.archive;

import java.util.Objects;

/** Holds information where to find a page. */
public record SeekEntry(long bytesToSeek, int articleId, String title)
    implements Comparable<SeekEntry> {

  public static SeekEntry forQuery(String query) {
    return new SeekEntry(0, 0, query);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof SeekEntry wikiEntry)) return false;
    return Objects.equals(title, wikiEntry.title)
        && bytesToSeek == wikiEntry.bytesToSeek
        && articleId == wikiEntry.articleId;
  }

  @Override
  public int hashCode() {
    return Objects.hash(bytesToSeek, articleId, title);
  }

  @Override
  public int compareTo(SeekEntry o) {
    return title.compareTo(o.title);
  }

  @Override
  public String toString() {
    return "WikiEntry{"
        + "bytesToSeek="
        + bytesToSeek
        + ", articleId="
        + articleId
        + ", title='"
        + title
        + '\''
        + '}';
  }
}
