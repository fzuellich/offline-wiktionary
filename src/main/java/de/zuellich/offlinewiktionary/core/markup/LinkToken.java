package de.zuellich.offlinewiktionary.core.markup;

public record LinkToken(String label, String target, boolean isExternal) implements MarkupToken {
  public LinkToken(String label, String target) {
    this(label, target, false);
  }
}
