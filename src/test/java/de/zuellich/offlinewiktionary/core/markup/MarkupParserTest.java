package de.zuellich.offlinewiktionary.core.markup;

import static de.zuellich.offlinewiktionary.core.markup.TokenAssertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
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
    assertText(parsedToken, "Hello World!");
  }

  @Test
  public void canParseSimpleHeadline() {
    String input = "==Headline==";
    final MarkupParser parser = new MarkupParser();
    List<MarkupToken> result = parser.parse(input);
    assertEquals(1, result.size());

    MarkupToken parseToken = result.getFirst();
    assertHeadline(parseToken, 2, "Headline");
  }

  @Test
  public void canParseLink() {
    String input = "[[Link]]";
    final MarkupParser parser = new MarkupParser();
    final List<MarkupToken> result = parser.parse(input);
    assertEquals(1, result.size());
    assertLink(result.get(0), "Link", "Link");
  }

  @Test
  public void canParsePipedLink() {
    String input = "[[Link|Label]]";
    final MarkupParser parser = new MarkupParser();
    final List<MarkupToken> result = parser.parse(input);
    assertEquals(1, result.size());
    assertLink(result.get(0), "Label", "Link");
  }

  @Test
  public void canParseWordEndingLinks() {
    String input = "[[Link]]here But this is text!";
    final MarkupParser parser = new MarkupParser();
    List<MarkupToken> result = parser.parse(input);
    assertEquals(2, result.size());
    assertLink(result.get(0), "Linkhere", "Link");
    assertText(result.get(1), " But this is text!");

    result = parser.parse("[[Link]]. But not text.");
    assertEquals(2, result.size());
    assertLink(result.get(0), "Link", "Link");
    assertText(result.get(1), ". But not text.");

    result = parser.parse("[[Link|Label]], but not text.");
    assertEquals(2, result.size());
    assertLink(result.get(0), "Label", "Link");
    assertText(result.get(1), ", but not text.");

    result = parser.parse("[[Link|Label]], [[Another|Label2]].");
    assertEquals(4, result.size());
    assertLink(result.get(0), "Label", "Link");
    assertText(result.get(1), ", ");
    assertLink(result.get(2), "Label2", "Another");
    assertText(result.get(3), ".");
  }

  @Test
  public void canParseMacrosWithLinkArgument() {
    final MarkupParser parser = new MarkupParser();
    final List<MarkupToken> result = parser.parse("{{Macro|[[Link|Label]]}}");
    assertEquals(3, result.size());
    assertText(result.get(0), "{{Macro|");
    assertLink(result.get(1), "Label", "Link");
    assertText(result.get(2), "}}");
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

  @Test
  public void canParseRealDocument() {
    final MarkupParser parser = new MarkupParser();
    assertTimeoutPreemptively(
        Duration.ofMillis(2000),
        () -> {
          parser.parse(Fixtures.REAL_PAGE_MARKUP);
        });
  }

  public void testVariousOverflowAndUnderflowSituations() {}

  public void testVariousHalfOpenHalfClosedConstellations() {}
}
