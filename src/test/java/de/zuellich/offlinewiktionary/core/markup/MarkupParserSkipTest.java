package de.zuellich.offlinewiktionary.core.markup;

import static de.zuellich.offlinewiktionary.core.markup.TokenAssertions.assertTokensStrict;
import static de.zuellich.offlinewiktionary.core.markup.TokenAssertions.skip;

import java.util.List;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("MarkupParser")
public class MarkupParserSkipTest {
  @Test
  public void skipsVideoFiles() {
    List<MarkupToken> markupTokens = MarkupParser.parseString("[[:File:test.png]]");
    assertTokensStrict(markupTokens, skip());

    markupTokens = MarkupParser.parseString("[[File:test.png]]");
    assertTokensStrict(markupTokens, skip());
  }

  @Test
  public void skippingVideoFilesSupportsResizing() {
    final List<MarkupToken> markupTokens =
        MarkupParser.parseString(
            "[[File:Example.png|thumb|upright|alt=Example alt text|Example caption]]");
    assertTokensStrict(markupTokens, skip());
  }

  @Test
  public void skippingVideoFilesSupportsPipedLinks() {
    final List<MarkupToken> markupTokens = MarkupParser.parseString("[[File:Example.png]]s");
    assertTokensStrict(markupTokens, skip());
  }

  @Test
  public void skippingVideoFilesSupportsMarkup() {
    final List<MarkupToken> markupTokens =
        MarkupParser.parseString(
            "[[File:Smooth line - Paragliding Proximity Flying.webm|mini|[1] ''Gleitschirmfliegen'' aus der [[Luft]] [[betrachten|betrachtet]]]]");
    assertTokensStrict(markupTokens, skip());
  }
}
