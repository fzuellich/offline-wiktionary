package de.zuellich.offlinewiktionary.core.markup;

import java.util.*;

/** See https://www.mediawiki.org/wiki/Help:Formatting */
public class MarkupParser {
  // Ideally this would live in a configuration object based on locale, so it could mimic the logic
  // used by Wiki
  // even closer.
  /** Do not consider these characters as part of a word ending link. */
  private static final Set<Integer> WORD_LINK_BOUNDARIES =
      Set.of(
          Byte.toUnsignedInt(Character.CONTROL),
          Byte.toUnsignedInt(Character.OTHER_PUNCTUATION),
          Byte.toUnsignedInt(Character.START_PUNCTUATION),
          Byte.toUnsignedInt(Character.END_PUNCTUATION));

  private static final char[][] OPENING_TOKENS =
      new char[][] {
        new char[] {'=', '='},
        new char[] {'[', '['},
        new char[] {']', ']'},
      };
  private char[] input;
  private int pointer = 0;

  private final Stack<Integer> snapshot = new Stack<>();

  /**
   * We expect the pointer to look at the first character that should be part of the text '==text=='
   * ^- point to t '=text' ^- point to the =, as it's part of the text and not an operator
   */
  private TextToken parseText() {
    final StringBuilder value = new StringBuilder();
    char previous = '\u0000';
    outer:
    while (hasNextChar()) {
      char c = nextChar();
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

  /** Look at next character, but don't advance pointer */
  private char peekNextChar() {
    if (pointer == this.input.length - 1) {
      return '\u0000';
    }
    return this.input[pointer + 1];
  }

  /**
   * Advance pointer and return character under new position
   *
   * @return
   */
  private char nextChar() {
    if (pointer + 1 >= input.length) {
      throw new IllegalStateException(
          "Trying to advance pointer, even though there is no more character");
    }
    pointer++;
    return this.input[pointer];
  }

  /**
   * @return true if a call to nextChar produces another character.
   */
  private boolean hasNextChar() {
    return pointer
        < this.input.length - 1; // otherwise, nextChar increments pointer and returns "nothing"
  }

  private void snapshotPointer() {
    snapshot.push(pointer);
  }

  private void restorePointer() {
    pointer = snapshot.pop();
  }

  private void eraseSnapshot() {
    snapshot.pop();
  }

  /**
   * Ensure the supplied characters are encountered in order.
   *
   * @throws MatchException indicating the expected characters were not in encountered.
   */
  private void assertSequence(char... expectedChars) {
    for (char requiredChar : expectedChars) {
      char c = nextChar();
      if (c != requiredChar) {
        throw new MatchException(
            String.format("Expected to match '%s' but got '%s'", requiredChar, c), null);
      }
    }
  }

  /**
   * Read from input until one of the characters is encountered or end of input. After return the
   * pointer points to the last character BEFORE the delimiter. So nextChar returns the delimiter.
   */
  private String readTerminatedBy(Character... delimiters) {
    if (delimiters.length == 0) {
      throw new IllegalArgumentException("Need to supply at least one delimiter!");
    }

    List<Character> asList = Arrays.asList(delimiters);
    StringBuilder value = new StringBuilder();
    while (hasNextChar()) {
      char c = nextChar();
      if (asList.contains(c)) {
        pointer--;
        break;
      }
      value.append(c);
    }

    return value.toString();
  }

  private LinkToken parseLink() {
    snapshotPointer();
    try {
      assertSequence('[', '[');
      final String link = readTerminatedBy('|', ']');
      String label;
      char next = nextChar();
      if (next == '|') {
        label = readTerminatedBy(']');
        assertSequence(']', ']');
      } else if (next == ']') {
        assertSequence(']');
        label = link;
      } else {
        throw new MatchException(
            String.format("Expected either '|' or ']' but got '%s'.", next), null);
      }

      // Important: peekNextChar() has to be called after hasNextChar() otherwise an
      // ArrayIndexOutOfBounds
      // is thrown if there is no next char. We could think about working with \u0000 here, but that
      // might get
      // tricky in some circumstances.
      if (hasNextChar() && !WORD_LINK_BOUNDARIES.contains(Character.getType(peekNextChar()))) {
        String wordEnding = readTerminatedBy(' ');
        label = label + wordEnding;
      }

      eraseSnapshot();
      return new LinkToken(label, link);
    } catch (MatchException e) {
      restorePointer();
      return null;
    }
  }

  /**
   * Expect the pointer to point past the second '=', e.g. '== heading ==' ^- we want to point to
   * the whitespace or '===heading===' ^- we want to point to the character indicating an additional
   * level
   */
  private HeadingToken parseHeading() {
    MarkupToken contentToken = null;
    snapshotPointer();

    // increment what we previously checked
    int level = 0;
    while (hasNextChar() && nextChar() == '=') {
      level++;
    }
    if (level < 2) {
      restorePointer();
      return null;
    }

    // rewind last read character, it's not a =
    pointer--;
    contentToken = parseText();

    int requiredLevel = level;
    while (hasNextChar() && nextChar() == '=') {
      requiredLevel--;
    }

    if (requiredLevel != 0) {
      restorePointer();
      return null;
    }

    eraseSnapshot();
    return new HeadingToken(level, contentToken);
  }

  public List<MarkupToken> parse(String input) {
    this.input = input.toCharArray();
    this.pointer = -1;
    this.snapshot.clear();
    ArrayList<MarkupToken> tokens = new ArrayList<>();
    while (hasNextChar()) {
      final HeadingToken headingToken = parseHeading();
      if (headingToken != null) {
        tokens.add(headingToken);
        continue;
      }
      final LinkToken linkToken = parseLink();
      if (linkToken != null) {
        tokens.add(linkToken);
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
