package de.zuellich.offlinewiktionary.core.markup;

public record HeadingToken(int level, MarkupToken value) implements MarkupToken {
  @Override
  public MarkupTokenType getType() {
    return MarkupTokenType.HEADING;
  }
}
