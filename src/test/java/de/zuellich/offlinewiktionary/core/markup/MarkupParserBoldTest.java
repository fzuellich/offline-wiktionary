package de.zuellich.offlinewiktionary.core.markup;

import static de.zuellich.offlinewiktionary.core.markup.TokenAssertions.*;

import java.util.List;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("MarkupParser")
public class MarkupParserBoldTest {

  @Test
  public void canParseSimpleBoldText() {
    final List<MarkupToken> markupTokens = MarkupParser.parseString("'''Hello World'''");
    assertTokensStrict(markupTokens, bold(text("Hello World")));
  }

  @Test
  public void canMixWithItalics() {
    final List<MarkupToken> markupTokens = MarkupParser.parseString("'''''I''talic And Bold'''");
    assertTokensStrict(markupTokens, bold(List.of(italic(text("I")), text("talic And Bold"))));
  }

  @Test
  public void canCombineWithLinks() {
    List<MarkupToken> markupTokens = MarkupParser.parseString("'''[[Link]]'''");
    assertTokensStrict(markupTokens, bold(link("Link")));

    markupTokens = MarkupParser.parseString("[[Link|'''Bold''']]");
    assertTokensStrict(markupTokens, link("Link", bold(text("Bold"))));
  }
}
