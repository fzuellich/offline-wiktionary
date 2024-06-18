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
    final String plainTextLabel = MarkupToken.toPlainText(linkToken.labelValue());
    if (!plainTextLabel.equals(expectedLabel)) {
      fail(String.format("Expected label '%s' but got '%s'", expectedLabel, plainTextLabel));
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

  /** Convenience method for cases where you only need to test a single token. */
  public static void assertTokensStrict(
      final List<MarkupToken> tokens, final Consumer<MarkupToken> matcher) {
    assertTokensStrict(tokens, List.of(matcher));
  }

  /** Convenience method for cases where you only need to test a single token. */
  public static void assertToken(final MarkupToken token, final Consumer<MarkupToken> matcher) {
    assertTokensStrict(List.of(token), List.of(matcher));
  }

  public static Consumer<MarkupToken> bold(List<Consumer<MarkupToken>> expectedValue) {
    return (MarkupToken token) -> {
      assertMatchingType(MarkupTokenType.BOLD, token);
      List<MarkupToken> value = ((BoldToken) token).value();
      assertTokensStrict(value, expectedValue);
    };
  }

  public static Consumer<MarkupToken> bold(Consumer<MarkupToken> expectedValue) {
    return bold(List.of(expectedValue));
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

  public static Consumer<MarkupToken> link(
      String expectedTarget, Consumer<MarkupToken> expectedLabel) {
    return link(expectedTarget, List.of(expectedLabel));
  }

  public static Consumer<MarkupToken> link(
      String expectedTarget, List<Consumer<MarkupToken>> expectedLabel) {
    return (MarkupToken token) -> {
      assertMatchingType(MarkupTokenType.LINK, token);
      LinkToken link = (LinkToken) token;
      if (!expectedTarget.equals(link.target())) {
        fail(String.format("Expected target '%s' but got '%s'", expectedTarget, link.target()));
      }
      assertTokensStrict(link.labelValue(), expectedLabel);
    };
  }

  public static Consumer<MarkupToken> skip() {
    return (MarkupToken token) -> {
      if (token != SkipToken.of()) {
        fail("Expected to find SkipToken, but found other.");
      }
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

  public static Consumer<MarkupToken> italic(Consumer<MarkupToken> content) {
    return (MarkupToken token) -> {
      assertMatchingType(MarkupTokenType.ITALIC, token);
      assertTokensStrict(((ItalicToken) token).value(), List.of(content));
    };
  }

  /**
   * @param expectedLevel Check that the heading has the specified level
   * @param expectedContent Strictly match that the heading content matches the provided matchers.
   */
  public static Consumer<MarkupToken> heading(
      int expectedLevel, List<Consumer<MarkupToken>> expectedContent) {
    return (MarkupToken token) -> {
      assertMatchingType(MarkupTokenType.HEADING, token);
      HeadingToken heading = (HeadingToken) token;
      if (heading.level() != expectedLevel) {
        fail(
            String.format(
                "Expected heading to have level %s, but got %s.", expectedLevel, heading.level()));
      }

      assertTokensStrict(heading.value(), expectedContent);
    };
  }

  /** Convenience method for testing when you only need to supply a single content token matcher. */
  public static Consumer<MarkupToken> heading(
      int expectedLevel, Consumer<MarkupToken> expectedContent) {
    return heading(expectedLevel, List.of(expectedContent));
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

    for (int i = 0; i < matchers.size(); i++) {
      final MarkupToken token = tokens.get(i);
      final Consumer<MarkupToken> matcher = matchers.get(i);
      matcher.accept(token);
    }
  }
}
