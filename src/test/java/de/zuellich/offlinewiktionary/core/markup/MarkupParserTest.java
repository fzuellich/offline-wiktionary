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
    assertHeadline(result.get(1), 2, "the heading");
    assertText(result.get(2), """
                a paragraph
                """);
    assertHeadline(result.get(3), 3, "another heading");
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

  @Test
  public void canParseRealDocument() {
    final MarkupParser parser = new MarkupParser();
    assertTimeoutPreemptively(
        Duration.ofMillis(50),
        () -> {
          parser.parse(Fixtures.REAL_PAGE_MARKUP);
        });
  }

  public void testVariousOverflowAndUnderflowSituations() {}

  public void testVariousHalfOpenHalfClosedConstellations() {}
}
