package de.zuellich.offlinewiktionary.core.markup;

import javax.annotation.Nullable;

public record HeadingToken(int level, @Nullable MarkupToken value) implements MarkupToken {
  @Override
  public MarkupTokenType getType() {
    return MarkupTokenType.HEADING;
  }
}
