package de.zuellich.offlinewiktionary.core.markup;

import static de.zuellich.offlinewiktionary.core.markup.TokenAssertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("MarkupParser")
class MarkupParserTest {

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
    assertToken(result.get(1), heading(2, text("the heading")));
    assertText(result.get(2), """
                a paragraph
                """);
    assertToken(result.get(3), heading(3, text("another heading")));
    assertText(result.get(4), "{{Macro|With|Parameters}}");
  }

  /**
   * Some tokens check if they are at the beginning of a line. At the beginning of the line means
   * either: - after a newline - at the beginning of input So to test if they are at the beginning
   * of the line is more complex than just checking if the last character was a '\n'.
   */
  @Test
  public void regressionTestNewlineOrBeginningOfInput() {
    final MarkupParser parser = new MarkupParser();
    final List<MarkupToken> result = parser.parse(": I'm at the beginning");
    assertTokens(result, List.of(indent(1), text(" I'm at the beginning")));
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

  /** Previously broke because of the second heading mixing italics */
  @Test
  public void regressionTestParseQuarksPage() {
    final List<MarkupToken> markupTokens = MarkupParser.parseString(Fixtures.QUARKS_PAGE_MARKUP);
    assertTrue(markupTokens.size() > 0);
  }

  /** Previously broke because of the mixture of italics and word-ending links */
  @Test
  public void regressionTestParseFriedePage() {
    final List<MarkupToken> markupTokens = MarkupParser.parseString(Fixtures.FRIEDE_PAGE_MARKUP);
    assertTrue(markupTokens.size() > 0);
  }

  /**
   * Previously there were circumstance where our text parser generated empty text tokens, that
   * would prevent some loops from terminating. Avoid this.
   */
  @Test
  public void regressionTestTerminatesWithoutEmptyText() {
    assertTimeoutPreemptively(
        Duration.ofMillis(10),
        () -> {
          final List<MarkupToken> result = MarkupParser.parseString("''test''");
          assertTokens(result, List.of(italic(List.of(text("test")))));
        });
  }

  @Test
  public void canParseRealDocument() {
    final MarkupParser parser = new MarkupParser();
    assertTimeoutPreemptively(
        Duration.ofMillis(50),
        () -> {
          parser.parse(Fixtures.REAL_PAGE_MARKUP);
        });
  }

  // @TODO
  public void testVariousOverflowAndUnderflowSituations() {}

  // @TODO
  public void testVariousHalfOpenHalfClosedConstellations() {}
}
