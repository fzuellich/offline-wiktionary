package de.zuellich.offlinewiktionary.core.markup;

import java.util.List;

public record LinkToken(List<MarkupToken> labelValue, String target) implements MarkupToken {
  @Override
  public MarkupTokenType getType() {
    return MarkupTokenType.LINK;
  }
}
