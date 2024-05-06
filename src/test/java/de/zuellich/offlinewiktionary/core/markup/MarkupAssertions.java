package de.zuellich.offlinewiktionary.core.markup;

import static org.junit.jupiter.api.Assertions.fail;

public class MarkupAssertions {

  static void assertHeadline(MarkupToken token, int requiredLevel) {
    if (token.getType() != MarkupTokenType.HEADING) {
      fail(
          String.format(
              "Expected token to be of type 'HEADING', but type is '%s'.", token.getType()));
    }

    HeadingToken headingToken = (HeadingToken) token;
    if (headingToken.level() != requiredLevel) {
      fail(
          String.format(
              "Expected heading with level %d but actual level is %d",
              requiredLevel, headingToken.level()));
    }
  }

  static void assertHeadline(MarkupToken token, int requiredLevel, String content) {
    if (token.getType() != MarkupTokenType.HEADING) {
      fail(
          String.format(
              "Expected token to be of type 'HEADING', but type is '%s'.", token.getType()));
    }

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
    if (token.getType() != MarkupTokenType.TEXT) {
      fail(String.format("Expected token type to be 'TEXT' but is '%s'", token.getType()));
    }

    final TextToken textToken = (TextToken) token;
    if (!textToken.value().equals(expectedText)) {
      fail(String.format("Expected text value '%s' but got '%s'", expectedText, textToken.value()));
    }
  }
}
