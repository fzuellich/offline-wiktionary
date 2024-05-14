package de.zuellich.offlinewiktionary.core.markup;

import static org.junit.jupiter.api.Assertions.fail;

import java.util.List;
import java.util.function.Consumer;

public class TokenAssertions {

  public static void assertMatchingType(MarkupTokenType expected, MarkupToken token) {
    if (!expected.equals(token.getType())) {
      fail(
          String.format(
              "Expected token to be of type '%s', but got '%s'.", expected, token.getType()));
    }
  }

  static void assertHeadline(MarkupToken token, int requiredLevel) {
    assertMatchingType(MarkupTokenType.HEADING, token);

    HeadingToken headingToken = (HeadingToken) token;
    if (headingToken.level() != requiredLevel) {
      fail(
          String.format(
              "Expected heading with level %d but actual level is %d",
              requiredLevel, headingToken.level()));
    }
  }

  static void assertHeadline(MarkupToken token, int requiredLevel, String content) {
    assertMatchingType(MarkupTokenType.HEADING, token);

    final HeadingToken headingToken = (HeadingToken) token;
    if (headingToken.level() != requiredLevel) {
      fail(
          String.format(
              "Expected heading with level %d but actual level is %d",
              requiredLevel, headingToken.level()));
    }

    final MarkupToken value = headingToken.value();
    if (value.getType() != MarkupTokenType.TEXT) {
      fail(String.format("Expected heading value type to be 'TEXT' but is '%s'", value.getType()));
    }

    final TextToken textToken = (TextToken) value;
    if (!textToken.value().equals(content)) {
      fail(String.format("Expected heading value '%s' but got '%s'", content, textToken.value()));
    }
  }

  public static void assertText(MarkupToken token, String expectedText) {
    assertMatchingType(MarkupTokenType.TEXT, token);

    final TextToken textToken = (TextToken) token;
    if (!textToken.value().equals(expectedText)) {
      fail(String.format("Expected text value '%s' but got '%s'", expectedText, textToken.value()));
    }
  }

  public static void assertLink(MarkupToken token, String expectedLabelAndTarget) {
    assertLink(token, expectedLabelAndTarget, expectedLabelAndTarget);
  }

  public static void assertLink(MarkupToken token, String expectedLabel, String expectedTarget) {
    assertMatchingType(MarkupTokenType.LINK, token);
    final LinkToken linkToken = (LinkToken) token;
    if (!linkToken.label().equals(expectedLabel)) {
      fail(String.format("Expected label '%s' but got '%s'", expectedLabel, linkToken.label()));
    }
    if (!linkToken.target().equals(expectedTarget)) {
      fail(String.format("Expected target '%s' but got '%s'", expectedTarget, linkToken.target()));
    }
  }

  public static void assertIndent(MarkupToken token, int expectedLevel) {
    assertMatchingType(MarkupTokenType.INDENT, token);
    final IndentToken indentToken = (IndentToken) token;
    if (indentToken.level() != expectedLevel) {
      fail(
          String.format(
              "Expected indent level '%d' but got '%d'", expectedLevel, indentToken.level()));
    }
  }

  public static void assertTokensStrict(
      final List<MarkupToken> tokens, final List<Consumer<MarkupToken>> matchers) {
    if (tokens.size() != matchers.size()) {
      fail(
          String.format(
              "Token count (%d) doesn't match matchers count (%d).",
              tokens.size(), matchers.size()));
    }

    assertTokens(tokens, matchers);
  }

  public static Consumer<MarkupToken> text(String text) {
    return (MarkupToken token) -> {
      assertText(token, text);
    };
  }

  public static Consumer<MarkupToken> link(String expectedTextAndLabel) {
    return (MarkupToken token) -> {
      assertLink(token, expectedTextAndLabel);
    };
  }

  public static Consumer<MarkupToken> indent(int level) {
    return (MarkupToken token) -> {
      assertIndent(token, level);
    };
  }

  public static Consumer<MarkupToken> italic(List<Consumer<MarkupToken>> inner) {
    return (MarkupToken token) -> {
      assertMatchingType(MarkupTokenType.ITALIC, token);
      assertTokensStrict(((ItalicToken) token).value(), inner);
    };
  }

  /**
   * Iterate over the provided tokens and see if the supplied matchers match the corresponding
   * token. You can supply more tokens than matchers, but not the other way around. If you want to
   * be precise use {@link TokenAssertions#assertTokensStrict(List, List)}
   */
  public static void assertTokens(
      final List<MarkupToken> tokens, final List<Consumer<MarkupToken>> matchers) {
    if (matchers.size() > tokens.size()) {
      fail(
          String.format(
              "More matchers than tokens. Got %d tokens, but %d matchers.",
              tokens.size(), matchers.size()));
    }

    final int min = Math.min(matchers.size(), tokens.size());
    for (int i = 0; i < min; i++) {
      final MarkupToken token = tokens.get(i);
      final Consumer<MarkupToken> matcher = matchers.get(i);
      matcher.accept(token);
    }
  }
}
