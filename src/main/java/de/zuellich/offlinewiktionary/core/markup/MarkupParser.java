package de.zuellich.offlinewiktionary.core.markup;

import java.util.*;
import java.util.function.Supplier;

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
        new char[] {'\'', '\''}
      };
  private final List<Supplier<MarkupToken>> textTokenParserPriority;
  private char[] input;
  private int pointer = 0;

  private final Stack<Integer> snapshot = new Stack<>();
  private final List<Supplier<MarkupToken>> tokenParserPriority;

  /**
   * Helper function for brevity in tests. Do allow for dependency injection consider the
   * straightforward way and create a new instance in the constructor.
   *
   * @param input String to parse
   * @return A list of parsed tokens.
   */
  public static List<MarkupToken> parseString(String input) {
    var parser = new MarkupParser();
    return parser.parse(input);
  }

  public MarkupParser() {
    tokenParserPriority =
        List.of(
            this::parseIndent,
            this::parseHeading,
            this::parseItalicToken,
            this::parseLink,
            this::parseText);
    textTokenParserPriority = List.of(this::parseLink, this::parseText);
  }

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

      value.append(c);
      previous = c;

      if (c == '\n') {
        break;
      }
    }

    // Don't return empty text tokens, otherwise some iterations might never terminate. For example:
    // ''test'' will always parse a text token ("test") and then terminate, because it encounters
    // the italics literal.
    // However, we are already in an italics literal and the control is only given back to the
    // italics parser, if we can
    // parse no more tokens. And generating empty TextTokens, that don't advance the pointer don't
    // fit here.
    if (value.isEmpty()) {
      return null;
    }
    return new TextToken(value.toString());
  }

  /**
   * Check if the previous character matches c. If pointer has not moved yet, then false is
   * returned.
   *
   * @param c
   * @return false if character is not matching, or pointer out of bounds.
   */
  private boolean currentCharMatches(char c) {
    if (pointer < 0) {
      return false;
    }

    return this.input[pointer] == c;
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
   * @throws MatchException indicating the expected characters were encountered.
   */
  private void consumeInOrder(char... expectedChars) {
    for (char requiredChar : expectedChars) {
      char c = nextChar();
      if (c != requiredChar) {
        throw new MatchException(
            String.format("Expected to match '%s' but got '%s'", requiredChar, c), null);
      }
    }
  }

  /**
   * Read characters as long as they match expected. Pointer is guaranteed to point to the last
   * matching character that was consumed. I.e. calling nextChar() returns the next non-matching
   * character.
   *
   * @return The number of matching characters consumed
   */
  private int consumeMatching(char expected) {
    int consumed = 0;
    // Important: we peek here, so we don't have to rewind the pointer if nextChar()
    // doesn't match our expectation
    while (hasNextChar() && peekNextChar() == expected) {
      nextChar();
      consumed++;
    }

    return consumed;
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

  private List<MarkupToken> parseTextContent() {
    final List<MarkupToken> result = new ArrayList<>();
    while (hasNextChar()) {
      final Optional<MarkupToken> possibleNextToken = nextTextContentToken();
      if (possibleNextToken.isEmpty()) {
        break;
      }

      result.add(possibleNextToken.get());
    }

    return result;
  }

  private LinkToken parseLink() {
    snapshotPointer();
    try {
      consumeInOrder('[', '[');
      final String link = readTerminatedBy('|', ']');
      String label;
      char next = nextChar();
      if (next == '|') {
        label = readTerminatedBy(']');
        consumeInOrder(']', ']');
      } else if (next == ']') {
        consumeInOrder(']');
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

    if (hasNextChar() && peekNextChar() != '=') {
      return null;
    }
    int level = consumeMatching('=');
    if (level < 2) {
      restorePointer();
      return null;
    }

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

  private IndentToken parseIndent() {
    if ((currentCharMatches('\n') || pointer == -1) && peekNextChar() == ':') {
      int level = consumeMatching(':');
      return new IndentToken(level);
    }
    return null;
  }

  private ItalicToken parseItalicToken() {
    snapshotPointer();
    try {
      consumeInOrder('\'', '\'');
      List<MarkupToken> value = parseTextContent();
      consumeInOrder('\'', '\'');
      eraseSnapshot();
      return new ItalicToken(value);
    } catch (MatchException e) {
      restorePointer();
      return null;
    }
  }

  private Optional<MarkupToken> nextTextContentToken() {
    for (Supplier<MarkupToken> tokenCandidate : textTokenParserPriority) {
      MarkupToken nextToken = tokenCandidate.get();
      if (nextToken != null) {
        return Optional.of(nextToken);
      }
    }

    return Optional.empty();
  }

  private Optional<MarkupToken> nextToken() {
    for (Supplier<MarkupToken> tokenCandidate : tokenParserPriority) {
      MarkupToken nextToken = tokenCandidate.get();
      if (nextToken != null) {
        return Optional.of(nextToken);
      }
    }

    return Optional.empty();
  }

  public List<MarkupToken> parse(String input) {
    this.input = input.toCharArray();
    this.pointer = -1;
    this.snapshot.clear();
    ArrayList<MarkupToken> tokens = new ArrayList<>();
    while (hasNextChar()) {
      final Optional<MarkupToken> possibleNextToken = nextToken();
      final MarkupToken markupToken =
          possibleNextToken.orElseThrow(
              () -> new IllegalStateException("No token produced. Should never happen."));
      tokens.add(markupToken);
    }
    return tokens;
  }
}
