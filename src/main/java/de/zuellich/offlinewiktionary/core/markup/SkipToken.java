package de.zuellich.offlinewiktionary.core.markup;

/** Indicate a token that shouldn't produce any content. */
public class SkipToken implements MarkupToken {
  private SkipToken() {}

  private static final SkipToken instance = new SkipToken();

  public static SkipToken of() {
    return instance;
  }
}
