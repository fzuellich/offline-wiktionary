package de.zuellich.offlinewiktionary.core.markup;

import java.util.*;
import java.util.function.Supplier;

/** See https://www.mediawiki.org/wiki/Help:Formatting */
public class MarkupParser {
  /**
   * Mixing bold and italic can lead to situations where we fail to parse the content of the tokens
   * correctly and would never return a result. Using the variable below we can implement a lazy
   * approach, where we rather close an existing token than start a new one.
   */
  private int boldItalicLevel = 0;

  // Ideally this would live in a configuration object based on locale, so it could mimic the logic
  // used by Wiki even closer.
  /** Do not consider these characters as part of a word ending link. */
  private static final Set<Integer> WORD_LINK_BOUNDARIES =
      Set.of(
          Byte.toUnsignedInt(Character.SPACE_SEPARATOR),
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

  private char[] input = new char[0];
  private int pointer = 0;

  private final ArrayDeque<Integer> snapshot = new ArrayDeque<>();

  private final List<Supplier<Optional<? extends MarkupToken>>> linkLabelTokenParsers;
  private final List<Supplier<Optional<? extends MarkupToken>>> italicContentTokenParsers;
  private final List<Supplier<Optional<? extends MarkupToken>>> boldContentTokenParsers;
  private final List<Supplier<Optional<? extends MarkupToken>>> textContentTokenParsers;
  private final List<Supplier<Optional<? extends MarkupToken>>> tokenParserPriority;

  /**
   * Helper function for brevity in tests. To allow for dependency injection consider the
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
    linkLabelTokenParsers = List.of(this::parseBoldToken, this::parseItalicToken, this::parseText);
    italicContentTokenParsers =
        List.of(this::parseBoldToken, this::parseVideo, this::parseLink, this::parseText);
    boldContentTokenParsers =
        List.of(this::parseItalicToken, this::parseVideo, this::parseLink, this::parseText);
    tokenParserPriority =
        List.of(
            this::parseIndent,
            this::parseHeading,
            this::parseBoldToken,
            this::parseItalicToken,
            this::parseVideo,
            this::parseLink,
            this::parseText);
    textContentTokenParsers =
        List.of(
            this::parseBoldToken,
            this::parseItalicToken,
            this::parseVideo,
            this::parseLink,
            this::parseText);
  }

  /**
   * We expect the pointer to look at the first character that should be part of the text '==text=='
   * ^- point to t '=text' ^- point to the =, as it's part of the text and not an operator
   */
  private Optional<TextToken> parseText() {
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
      return Optional.empty();
    }
    return Optional.of(new TextToken(value.toString()));
  }

  /**
   * Check if the current character matches c. If pointer has not moved yet, then false is returned.
   *
   * @param c character to check
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
   * Advance pointer and return character at new position
   *
   * @return character at the new position
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

  private String readTerminatedByType(Set<Integer> types) {
    if (types.size() == 0) {
      throw new IllegalArgumentException("Need to supply at least one Character type!");
    }

    StringBuilder value = new StringBuilder();
    while (hasNextChar()) {
      char c = nextChar();
      int type = Character.getType(c);
      if (types.contains(type)) {
        pointer--;
        break;
      }
      value.append(c);
    }

    return value.toString();
  }

  private boolean matchesFileTarget(String target) {
    return target.startsWith("File:") || target.startsWith(":File:");
  }

  private Optional<SkipToken> parseVideo() {
    snapshotPointer();
    try {
      consumeInOrder('[', '[');
      final String name = readTerminatedBy('|', ']');
      if (!matchesFileTarget(name)) {
        throw new MatchException("Expected a File link, but name doesn't match", null);
      }

      // consume until closing delimiters are found
      int pairs = 1;
      char previous = '\u0000';
      while (hasNextChar() && pairs > 0) {
        char current = nextChar();
        if (current == '[' && previous == '[') {
          previous =
              '\u0000'; // if we don't reset here, then we'll have bugs when encountering [[[[
          pairs++;
          continue;
        }

        if (current == ']'
            && previous
                == ']') { // if we don't reset here, then we'll have bugs when encountering ]]]]
          previous = '\u0000';
          pairs--;
          continue;
        }

        previous = current;
      }

      if (pairs > 0) {
        throw new MatchException(
            "Expected to find closing sequence ']]' but parenthesis seem unbalanced.", null);
      }

      // Now we need to check if we have a word-ending type of "link" (`[[File:test.png]]s`)
      readTerminatedByType(WORD_LINK_BOUNDARIES);
    } catch (MatchException e) {
      restorePointer();
      return Optional.empty();
    }

    eraseSnapshot();
    return Optional.of(SkipToken.of());
  }

  private Optional<LinkToken> parseLink() {
    snapshotPointer();
    try {
      consumeInOrder('[', '[');
      final String link = readTerminatedBy('|', ']');
      if (matchesFileTarget(link)) {
        // We might be able to improve performance by rewinding the pointer and handing control
        // directly to parseVideo?
        // Otherwise, we can also pay attention to have the parsers adjacent of each other.
        return Optional.empty();
      }

      List<MarkupToken> label;
      char next = nextChar();
      if (next == '|') {
        label = collectTokens(linkLabelTokenParsers);
        consumeInOrder(']', ']');
      } else if (next == ']') {
        // Support for the short form for links: [[Link]]
        consumeInOrder(']');
        String labelText = link;
        /*
         * We also want to support the special short form [[Link]]s -> label: Links, however to keep it simple we
         * don't support things like [[Link|Text]]s -> label: Texts. (Possible by just adding another token, but makes
         * it slightly messier.)
         *
         * Important: peekNextChar() has to be called after hasNextChar() otherwise an ArrayIndexOutOfBounds
         * is thrown if there is no next char. We could think about working with \u0000 here, but that
         * might get tricky in some circumstances.
         */
        String wordEnding = readTerminatedByType(WORD_LINK_BOUNDARIES);
        if (!wordEnding.isEmpty()) {
          labelText = labelText + wordEnding;
        }
        label = List.of(new TextToken(labelText));
      } else {
        throw new MatchException(
            String.format("Expected either '|' or ']' but got '%s'.", next), null);
      }

      eraseSnapshot();
      return Optional.of(new LinkToken(label, link));
    } catch (MatchException e) {
      restorePointer();
      return Optional.empty();
    }
  }

  /**
   * Expect the pointer to point past the second '=', e.g. '== heading ==' ^- we want to point to
   * the whitespace or '===heading===' ^- we want to point to the character indicating an additional
   * level
   */
  private Optional<HeadingToken> parseHeading() {
    snapshotPointer();

    if (hasNextChar() && peekNextChar() != '=') {
      return Optional.empty();
    }
    int level = consumeMatching('=');
    if (level < 2) {
      restorePointer();
      return Optional.empty();
    }

    List<MarkupToken> content = collectTokens(textContentTokenParsers);

    int requiredLevel = level;
    while (hasNextChar() && nextChar() == '=') {
      requiredLevel--;
    }

    if (requiredLevel != 0) {
      restorePointer();
      return Optional.empty();
    }

    eraseSnapshot();
    return Optional.of(new HeadingToken(level, content));
  }

  private Optional<IndentToken> parseIndent() {
    if ((currentCharMatches('\n') || pointer == -1) && peekNextChar() == ':') {
      int level = consumeMatching(':');
      return Optional.of(new IndentToken(level));
    }
    return Optional.empty();
  }

  private void assertHasAtLeastThreeChars(String message) {
    boolean hasAtLeastNCharacters = pointer < this.input.length - 3;
    if (!hasAtLeastNCharacters) {
      throw new MatchException(message, null);
    }
  }

  private Optional<BoldToken> parseBoldToken() {
    snapshotPointer();
    boldItalicLevel++;
    try {
      // If we don't check here, it's possible we see an exception when mixing bold and italics
      assertHasAtLeastThreeChars("Not enough characters to match beginning sequence for bold");
      consumeInOrder('\'', '\'', '\'');
      List<MarkupToken> value = collectTokens(boldContentTokenParsers);
      if (value.isEmpty()) {
        throw new MatchException("Found no content for bold block", null);
      }

      // if we don't throw an exception here, then we can't mix bold and italics, because sometimes
      // the markup will start with italics (e.g. '' '''bold''' other ''), and sometimes it will
      // start with bold ''' ''italic'' other''' which will cause consume in order to break
      assertHasAtLeastThreeChars("No more characters to find closing sequence for bold");
      consumeInOrder('\'', '\'', '\'');
      eraseSnapshot();
      boldItalicLevel--;
      return Optional.of(new BoldToken(value));
    } catch (MatchException e) {
      restorePointer();
      boldItalicLevel--;
      return Optional.empty();
    }
  }

  private Optional<ItalicToken> parseItalicToken() {
    snapshotPointer();
    boldItalicLevel++;
    try {
      consumeInOrder('\'', '\'');
      if (peekNextChar() == '\'' && boldItalicLevel > 1) {
        throw new MatchException("Looks like there is another bold move...", null);
      }
      List<MarkupToken> value = collectTokens(italicContentTokenParsers);
      if (value.isEmpty()) {
        /*
         * We don't support empty italics (i.e. ''''), instead we probably encountered the closing sequence for italics
         * and want to return control to the upper stages. Otherwise, we might also advance the pointer too much
         * when calling `consumeInOrder`. Maybe we have to refine this check by adding something like:
         * `value.isEmpty() && hasNoMoreCharacters()`?
         */
        throw new MatchException("Found no content for italics block", null);
      }
      if (!hasNextChar()) {
        /*
         * We might end up here, after parsing italics as text content for italics. However, we'll never find a closing
         * sequence and instead see an exception for advancing the pointer to far. We have to give control back to the
         * italics parent.
         */
        throw new MatchException("No more characters to find closing sequence for italics", null);
      }
      consumeInOrder('\'', '\'');
      boldItalicLevel--;
      eraseSnapshot();
      return Optional.of(new ItalicToken(value));
    } catch (MatchException e) {
      boldItalicLevel--;
      restorePointer();
      return Optional.empty();
    }
  }

  public List<MarkupToken> parse(String input) {
    this.input = input.toCharArray();
    this.pointer = -1;
    this.snapshot.clear();
    ArrayList<MarkupToken> tokens = new ArrayList<>();
    while (hasNextChar()) {
      final Optional<? extends MarkupToken> possibleNextToken = tryNextToken(tokenParserPriority);
      final MarkupToken markupToken =
          possibleNextToken.orElseThrow(
              () -> new IllegalStateException("No token produced. Should never happen."));
      tokens.add(markupToken);
    }
    return tokens;
  }

  /** Attempt to parse the next token using the supplied candidates in iteration order. */
  private Optional<? extends MarkupToken> tryNextToken(
      List<Supplier<Optional<? extends MarkupToken>>> candidates) {
    for (Supplier<Optional<? extends MarkupToken>> candidate : candidates) {
      Optional<? extends MarkupToken> nextToken = candidate.get();
      if (nextToken.isPresent()) {
        return nextToken;
      }
    }

    return Optional.empty();
  }

  /**
   * Given a list of parsers, try to parse a token until end of input. This may lead to an endless
   * loop in case of malformed input.
   */
  private List<MarkupToken> collectTokens(List<Supplier<Optional<? extends MarkupToken>>> parsers) {
    final List<MarkupToken> result = new ArrayList<>();
    while (hasNextChar()) {
      final Optional<? extends MarkupToken> possibleNextToken = tryNextToken(parsers);
      if (possibleNextToken.isEmpty()) {
        break;
      }

      result.add(possibleNextToken.get());
    }

    return result;
  }
}
