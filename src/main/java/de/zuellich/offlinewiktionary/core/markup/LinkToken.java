package de.zuellich.offlinewiktionary.core.markup;

public record LinkToken(String label, String target) implements MarkupToken {
  @Override
  public MarkupTokenType getType() {
    return MarkupTokenType.LINK;
  }
}
