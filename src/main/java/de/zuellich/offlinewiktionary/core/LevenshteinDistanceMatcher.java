package de.zuellich.offlinewiktionary.core;

/** Following https://en.wikipedia.org/wiki/Damerau%E2%80%93Levenshtein_distance */
public class LevenshteinDistanceMatcher {
  private final String input;

  public LevenshteinDistanceMatcher(String input) {
    this.input = input;
  }

  public static LevenshteinDistanceMatcher of(String ca) {
    return new LevenshteinDistanceMatcher(ca);
  }

  public int distanceTo(String other) {
    int[][] d = new int[input.length() + 1][other.length() + 1];
    for (int i = 0; i <= input.length(); i++) {
      d[i][0] = i;
    }
    for (int i = 0; i <= other.length(); i++) {
      d[0][i] = i;
    }

    for (int i = 1; i <= input.length(); i++) {
      for (int j = 1; j <= other.length(); j++) {
        final int cost;
        if (character(input, i) != character(other, j)) {
          cost = 1;
        } else {
          cost = 0;
        }

        int deletion = d[i - 1][j] + 1;
        int insertion = d[i][j - 1] + 1;
        int substitution = d[i - 1][j - 1] + cost;
        d[i][j] = Math.min(deletion, Math.min(insertion, substitution));

        if (i > 1
            && j > i
            && character(input, i) == character(other, j - 1)
            && character(input, i - 1) == character(other, j)) {
          int transposition = d[i - 2][j - 2] + 1;
          d[i][j] = Math.min(d[i][j], transposition);
        }
      }
    }

    return d[input.length()][other.length()];
  }

  /**
   * To simplify calculations this method allows 1-based index access on the string. i.e. if you
   * want the first character of the string use '1' instead of '0'.
   *
   * @return The character at position index.
   */
  private static char character(String s, int pos) {
    int index = pos - 1;
    if (index < 0) {
      throw new IndexOutOfBoundsException();
    }

    return s.charAt(index);
  }
}
