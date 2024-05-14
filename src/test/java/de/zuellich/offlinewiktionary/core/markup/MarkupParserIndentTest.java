package de.zuellich.offlinewiktionary.core.markup;

import static de.zuellich.offlinewiktionary.core.markup.TokenAssertions.*;
import static de.zuellich.offlinewiktionary.core.markup.TokenAssertions.assertText;

import java.util.List;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("MarkupParser")
public class MarkupParserIndentTest {

  @Test
  public void canParseSimpleIndents() {
    final MarkupParser parser = new MarkupParser();
    List<MarkupToken> result = parser.parse(": Indent Level 1");
    assertTokensStrict(result, List.of(indent(1), text(" Indent Level 1")));

    result = parser.parse(":: Indent Level 2");
    assertTokensStrict(result, List.of(indent(2), text(" Indent Level 2")));

    result = parser.parse("::: Indent Level 3");
    assertTokensStrict(result, List.of(indent(3), text(" Indent Level 3")));
  }

  @Test
  public void canCombineWithOtherMarkup() {
    final MarkupParser parser = new MarkupParser();
    List<MarkupToken> result = parser.parse(": Another [[Example]]");
    assertTokensStrict(result, List.of(indent(1), text(" Another "), link("Example")));

    result = parser.parse("""
                : Line 1
                : Line 2 [[WithLink]]""");
    assertTokensStrict(
        result,
        List.of(indent(1), text(" Line 1\n"), indent(1), text(" Line 2 "), link("WithLink")));

    // We mostly encounter indent with a space in between the colon and text,
    // but these forms are also possible:
    result = parser.parse(":[[Link]]");
    assertTokensStrict(result, List.of(indent(1), link("Link")));

    result = parser.parse("Here : is no indent!");
    assertTokensStrict(result, List.of(text("Here : is no indent!")));
  }

  @Test
  public void respectWhitespace() {
    final MarkupParser parser = new MarkupParser();
    List<MarkupToken> result = parser.parse("::More");
    assertIndent(result.get(0), 2);
    assertText(result.get(1), "More");

    result = parser.parse(":  With extra whitespace");
    assertIndent(result.get(0), 1);
    assertText(result.get(1), "  With extra whitespace");
  }
}
