package de.zuellich.offlinewiktionary.core.markup;

import java.util.ArrayList;
import java.util.List;

/** See https://www.mediawiki.org/wiki/Help:Formatting */
public class MarkupParser {
  private char[] input;
  private int pointer = 0;
  private static final char[][] OPENING_TOKENS =
      new char[][] {
        new char[] {'=', '='},
        new char[] {'[', '['},
        new char[] {']', ']'},
      };

  /**
   * We expect the pointer to look at the first character that should be part of the text '==text=='
   * ^- point to t '=text' ^- point to the =, as it's part of the text and not an operator
   */
  private TextToken parseText() {
    final StringBuilder value = new StringBuilder();
    char previous = '\u0000';
    outer:
    for (char c = nextChar(); c != '\u0000'; c = nextChar()) {
      for (char[] openingToken : OPENING_TOKENS) {
        // We expect this to work for longer headlines (higher level) as well,
        // because our job is just to figure out if a new element starts, not
        // which one.
        if (openingToken[0] == previous && openingToken[1] == c) {
          value.deleteCharAt(value.length() - 1);
          pointer -= 2;
          break outer;
        }
      }
      if (c == '=' && previous == '=') {
        return new TextToken(value.toString());
      } else {
        value.append(c);
      }

      previous = c;
    }

    return new TextToken(value.toString());
  }

  private char nextChar() {
    pointer++;
    if (!thereIsInputLeft()) {
      return '\u0000';
    }
    return this.input[pointer];
  }

  private boolean thereIsInputLeft() {
    return pointer < this.input.length;
  }

  /**
   * Expect the pointer to point past the second '=', e.g. '== heading ==' ^- we want to point to
   * the whitespace or '===heading===' ^- we want to point to the character indicating an additional
   * level
   */
  private HeadingToken parseHeading() {
    MarkupToken contentToken = null;
    int startPointer = pointer;

    // increment what we previously checked
    int level = 0;
    while (nextChar() == '=') {
      level++;
    }
    if (level < 2) {
      pointer = startPointer;
      return null;
    }

    // rewind last read character, it's not a =
    pointer--;
    contentToken = parseText();

    int requiredLevel = level;
    while (nextChar() == '=') {
      requiredLevel--;
    }

    if (requiredLevel != 0) {
      pointer = startPointer;
      return null;
    }

    return new HeadingToken(level, contentToken);
  }

  public List<MarkupToken> parse(String input) {
    this.input = input.toCharArray();
    this.pointer = -1;
    ArrayList<MarkupToken> tokens = new ArrayList<>();
    while (thereIsInputLeft()) {
      final HeadingToken headingToken = parseHeading();
      if (headingToken != null) {
        tokens.add(headingToken);
        continue;
      }
      final TextToken textToken = parseText();
      if (textToken != null) {
        tokens.add(textToken);
        continue;
      }
    }
    return tokens;
  }
}
