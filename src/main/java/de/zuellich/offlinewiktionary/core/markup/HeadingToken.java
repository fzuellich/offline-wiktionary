package de.zuellich.offlinewiktionary.core.markup;

import java.util.List;

public record HeadingToken(int level, List<MarkupToken> value) implements MarkupToken {
  @Override
  public MarkupTokenType getType() {
    return MarkupTokenType.HEADING;
  }
}
