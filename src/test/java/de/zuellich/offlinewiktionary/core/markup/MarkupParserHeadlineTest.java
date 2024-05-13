package de.zuellich.offlinewiktionary.core.markup;

import static de.zuellich.offlinewiktionary.core.markup.TokenAssertions.assertHeadline;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("MarkupParser")
public class MarkupParserHeadlineTest {

  @Test
  public void canParseSimpleHeadline() {
    String input = "==Headline==";
    final MarkupParser parser = new MarkupParser();
    List<MarkupToken> result = parser.parse(input);
    assertEquals(1, result.size());

    MarkupToken parseToken = result.getFirst();
    assertHeadline(parseToken, 2, "Headline");
  }
}
