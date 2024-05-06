package de.zuellich.offlinewiktionary.core.markup;

import static de.zuellich.offlinewiktionary.core.markup.MarkupAssertions.assertHeadline;
import static de.zuellich.offlinewiktionary.core.markup.MarkupAssertions.assertText;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.Test;

class MarkupParserTest {

  @Test
  public void canParseSimpleText() {
    String input = "Hello World!";
    final MarkupParser parser = new MarkupParser();
    List<MarkupToken> result = parser.parse(input);
    assertEquals(1, result.size());

    MarkupToken parsedToken = result.getFirst();
    assertEquals(parsedToken.getType(), MarkupTokenType.TEXT);
    assertEquals("Hello World!", ((TextToken) parsedToken).value());
  }

  @Test
  public void canParseSimpleHeadline() {
    String input = "==Headline==";
    final MarkupParser parser = new MarkupParser();
    List<MarkupToken> result = parser.parse(input);
    assertEquals(1, result.size());

    MarkupToken parseToken = result.getFirst();
    assertEquals(MarkupTokenType.HEADING, parseToken.getType());
    HeadingToken headingToken = (HeadingToken) parseToken;
    assertEquals(2, headingToken.level());

    MarkupToken headingValue = headingToken.value();
    assertEquals(MarkupTokenType.TEXT, headingValue.getType());
    assertEquals("Headline", ((TextToken) headingValue).value());
  }

  @Test
  public void canParseSimpleDocument() {
    String document =
        """
                {{SomeMacro}}
                ==the heading==
                a paragraph
                ===another heading===
                {{Macro|With|Parameters}}""";
    final MarkupParser parser = new MarkupParser();
    List<MarkupToken> result = parser.parse(document);
    assertEquals(5, result.size());
    assertText(result.get(0), "{{SomeMacro}}\n");
    assertHeadline(result.get(1), 2, "the heading");
    assertText(result.get(2), """
                a paragraph
                """);
    assertHeadline(result.get(3), 3, "another heading");
    assertText(result.get(4), "{{Macro|With|Parameters}}");
  }

  public void testVariousOverflowAndUnderflowSituations() {}

  public void testVariousHalfOpenHalfClosedConstellations() {}
}
