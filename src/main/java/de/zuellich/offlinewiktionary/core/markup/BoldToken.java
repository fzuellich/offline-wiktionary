package de.zuellich.offlinewiktionary.core.markup;

import java.util.List;

public record BoldToken(List<MarkupToken> value) implements MarkupToken {

  public BoldToken(List<MarkupToken> value) {
    this.value = List.copyOf(value);
  }

  @Override
  public MarkupTokenType getType() {
    return MarkupTokenType.BOLD;
  }

  @Override
  public List<MarkupToken> value() {
    return List.copyOf(this.value);
  }
}
