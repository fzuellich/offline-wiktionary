package de.zuellich.offlinewiktionary.core.markup;

import static de.zuellich.offlinewiktionary.core.markup.TokenAssertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.Test;

class MarkupParserTest {

  @Test
  public void canParseSimpleText() {
    MarkupParser parser = new MarkupParser();
    List<MarkupToken> result = parser.parse("Hello World!");
    assertEquals(1, result.size());
    assertText(result.get(0), "Hello World!");

    result = parser.parse("Here : is no indent!");
    assertEquals(1, result.size());
    assertText(result.get(0), "Here : is no indent!");

    result =
        parser.parse("""
                We can also parse

                multi-line text.""");
    assertEquals(3, result.size());
    assertText(result.get(0), "We can also parse\n");
    assertText(result.get(1), "\n");
    assertText(result.get(2), "multi-line text.");

    // Empty lines?
    result = parser.parse("""
                Text
                """);
    assertEquals(1, result.size());
    assertText(result.get(0), "Text\n");
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

  /**
   * We used to forget to rewind the pointer after consuming characters, which would cause an
   * endless loop here.
   */
  @Test
  public void regressionTestRewindAfterConsumingSameCharacters() {
    final MarkupParser parser = new MarkupParser();
    final List<MarkupToken> parse =
        parser.parse(
            """
                {{Nebenformen}}
                :[[Friede]]
                """);

    assertEquals(4, parse.size());
    assertText(parse.get(0), "{{Nebenformen}}\n");
    assertIndent(parse.get(1), 1);
    assertLink(parse.get(2), "Friede", "Friede");
    assertText(parse.get(3), "\n");
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

  @Test
  public void canParseIndent() {
    final MarkupParser parser = new MarkupParser();
    List<MarkupToken> result = parser.parse(": Indent Level 1");
    assertIndent(result.get(0), 1);
    assertText(result.get(1), " Indent Level 1");

    result = parser.parse(":: Indent Level 2");
    assertIndent(result.get(0), 2);
    assertText(result.get(1), " Indent Level 2");

    result = parser.parse("::: Indent Level 3");
    assertIndent(result.get(0), 3);
    assertText(result.get(1), " Indent Level 3");

    result = parser.parse(": Another [[Example]]");
    assertIndent(result.get(0), 1);
    assertText(result.get(1), " Another ");
    assertLink(result.get(2), "Example", "Example");

    result = parser.parse(":  With extra whitespace");
    assertIndent(result.get(0), 1);
    assertText(result.get(1), "  With extra whitespace");

    result = parser.parse("""
                : Line 1
                : Line 2 [[WithLink]]""");
    assertIndent(result.get(0), 1);
    assertText(result.get(1), " Line 1\n");
    assertIndent(result.get(2), 1);
    assertText(result.get(3), " Line 2 ");
    assertLink(result.get(4), "WithLink");

    // We mostly encounter indent with a space in between the colon and text,
    // but these forms are also possible:
    result = parser.parse(":[[Link]]");
    assertIndent(result.get(0), 1);
    assertLink(result.get(1), "Link");

    result = parser.parse("::More");
    assertIndent(result.get(0), 2);
    assertText(result.get(1), "More");
  }

  public void testVariousOverflowAndUnderflowSituations() {}

  public void testVariousHalfOpenHalfClosedConstellations() {}
}
