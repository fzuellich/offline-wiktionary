package de.zuellich.offlinewiktionary.core.markup;

public record TextToken(String value) implements MarkupToken {
  @Override
  public MarkupTokenType getType() {
    return MarkupTokenType.TEXT;
  }
}
