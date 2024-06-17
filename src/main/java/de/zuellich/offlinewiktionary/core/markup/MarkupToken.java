package de.zuellich.offlinewiktionary.core.markup;

import java.util.List;

public interface MarkupToken {
  static String toPlainText(List<MarkupToken> tokens) {
    StringBuilder result = new StringBuilder();
    for (MarkupToken token : tokens) {
      switch (token.getType()) {
        case TEXT -> result.append(((TextToken) token).value());
        case ITALIC -> result.append(toPlainText(((ItalicToken) token).value()));
        case BOLD -> result.append(toPlainText(((BoldToken) token).value()));
        case HEADING -> result.append(toPlainText(((HeadingToken) token).value()));
        case LINK -> result.append(toPlainText(((LinkToken) token).labelValue()));
        case INDENT -> result.append("\t".repeat(Math.min(((IndentToken) token).level(), 10)));
        case NULL -> {
          // Ensure we get notified by ErrorProne in case we are missing a case above
          // but we don't need to do anything for NULL so we do a useless "continue" and
          // everyone is happy.
          continue;
        }
      }
    }
    return result.toString();
  }

  public default MarkupTokenType getType() {
    return MarkupTokenType.NULL;
  }
}
