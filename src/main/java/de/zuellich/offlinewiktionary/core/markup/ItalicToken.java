package de.zuellich.offlinewiktionary.core.markup;

import java.util.List;

public record ItalicToken(List<MarkupToken> value) implements MarkupToken {

  public ItalicToken(List<MarkupToken> value) {
    this.value = List.copyOf(value);
  }

  @Override
  public List<MarkupToken> value() {
    return List.copyOf(this.value);
  }

  @Override
  public MarkupTokenType getType() {
    return MarkupTokenType.ITALIC;
  }
}
