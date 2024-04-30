package de.zuellich.offlinewiktionary.core.archive;

import java.util.Objects;

/**
 * Holds information where to find a page.
 */
public record SeekEntry(long bytesToSeek, int articleId, String title) implements Comparable<SeekEntry> {
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SeekEntry wikiEntry = (SeekEntry) o;
        return Objects.equals(title, wikiEntry.title);
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
        return "WikiEntry{" +
                "bytesToSeek=" + bytesToSeek +
                ", articleId=" + articleId +
                ", title='" + title + '\'' +
                '}';
    }
}
