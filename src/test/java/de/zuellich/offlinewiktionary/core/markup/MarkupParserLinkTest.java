package de.zuellich.offlinewiktionary.core.markup;

import static de.zuellich.offlinewiktionary.core.markup.TokenAssertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("MarkupParser")
public class MarkupParserLinkTest {

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
  public void canUseItalicsInLabel() {
    List<MarkupToken> result = MarkupParser.parseString("[[Link|Text with ''italic'']]");
    assertTokensStrict(
        result, List.of(link("Link", List.of(text("Text with "), italic(text("italic"))))));

    result = MarkupParser.parseString("''Italic [[Link|more ''italics'']]''");
    assertTokensStrict(
        result,
        italic(
            List.of(
                text("Italic "), link("Link", List.of(text("more "), italic(text("italics")))))));
  }
}
