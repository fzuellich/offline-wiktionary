package de.zuellich.offlinewiktionary.core.wiki;

/** Basic data for Wiki pages and a Builder utility for incremental parsing. */
public record WikiPage(String title, Integer id, String format, String text) {
  public static class Builder {
    private String title;
    private Integer id;
    private String format;
    private String text;

    public void setTitle(String title) {
      this.title = title;
    }

    public void setId(Integer id) {
      this.id = id;
    }

    public void setFormat(String format) {
      this.format = format;
    }

    public void setText(String text) {
      this.text = text;
    }

    public WikiPage build() {
      return new WikiPage(title, id, format, text);
    }
  }
}
