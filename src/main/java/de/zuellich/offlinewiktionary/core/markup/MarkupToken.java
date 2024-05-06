package de.zuellich.offlinewiktionary.core.markup;

public interface MarkupToken {
  public default MarkupTokenType getType() {
    return MarkupTokenType.NULL;
  }
}
