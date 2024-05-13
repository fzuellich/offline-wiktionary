package de.zuellich.offlinewiktionary.core.markup;

public record IndentToken(int level) implements MarkupToken {
  @Override
  public MarkupTokenType getType() {
    return MarkupTokenType.INDENT;
  }
}
