package de.zuellich.offlinewiktionary.core.markup;

import static de.zuellich.offlinewiktionary.core.markup.TokenAssertions.*;

import java.util.List;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("MarkupParser")
public class MarkupParserItalicTest {

  @Test
  public void canParseSimpleItalics() {
    List<MarkupToken> result = MarkupParser.parseString("''hello world''");
    assertTokensStrict(result, List.of(italic(List.of(text("hello world")))));
    result = MarkupParser.parseString("This is ''some'' text!");
    assertTokensStrict(
        result, List.of(text("This is "), italic(List.of(text("some"))), text(" text!")));
  }

  @Test
  public void canCombineWithOtherMarkup() {
    List<MarkupToken> result = MarkupParser.parseString("''[[Link]]''");
    assertTokensStrict(result, List.of(italic(List.of(link("Link")))));
    result = MarkupParser.parseString("This is ''[[more]]''!");
    assertTokensStrict(result, List.of(text("This is "), italic(List.of(link("more"))), text("!")));
  }
}
