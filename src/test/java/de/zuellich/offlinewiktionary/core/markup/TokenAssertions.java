package de.zuellich.offlinewiktionary.core.markup;

import static org.junit.jupiter.api.Assertions.fail;

public class TokenAssertions {

  private static void assertMatchingType(MarkupTokenType expected, MarkupToken token) {
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
}
