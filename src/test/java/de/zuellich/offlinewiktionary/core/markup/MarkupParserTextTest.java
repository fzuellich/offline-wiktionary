package de.zuellich.offlinewiktionary.core.markup;

import static de.zuellich.offlinewiktionary.core.markup.TokenAssertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("MarkupParser")
public class MarkupParserTextTest {

  @Test
  public void canParseSimpleText() {
    MarkupParser parser = new MarkupParser();
    List<MarkupToken> result = parser.parse("Hello World!");
    assertTokensStrict(result, List.of(text("Hello World!")));

    result =
        parser.parse(
            """
                        We can also parse

                        multi-line text.""");
    assertEquals(3, result.size());
    assertTokensStrict(
        result, List.of(text("We can also parse\n"), text("\n"), text("multi-line text.")));

    // Empty lines?
    result = parser.parse("""
                Text
                """);
    assertTokensStrict(result, List.of(text("Text\n")));
  }

  @Test
  public void canParseTextWithColonsAfterOtherMarkup() {
    MarkupParser parser = new MarkupParser();
    List<MarkupToken> result = parser.parse("Hello [[World]]: this works great!");
    assertTokensStrict(result, List.of(text("Hello "), link("World"), text(": this works great!")));
  }
}
