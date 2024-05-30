package de.zuellich.offlinewiktionary.core;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class LevenshteinDistanceMatcherTest {

  @Test
  public void testVerySimpleExamples() {
    LevenshteinDistanceMatcher matcher = LevenshteinDistanceMatcher.of("a");
    int distance = matcher.distanceTo("a");
    assertEquals(0, distance);

    distance = matcher.distanceTo("b");
    assertEquals(1, distance);

    distance = matcher.distanceTo("abc");
    assertEquals(2, distance);
  }

  @Test
  public void testDistanceIsZeroForEqualInputs() {
    LevenshteinDistanceMatcher matcher = LevenshteinDistanceMatcher.of("test");
    int distance = matcher.distanceTo("test");
    assertEquals(0, distance);
  }

  @Test
  public void testRecognizesInsertion() {
    LevenshteinDistanceMatcher matcher = LevenshteinDistanceMatcher.of("CA");
    int distance = matcher.distanceTo("ABC");
    assertEquals(3, distance);
  }

  @Test
  public void testRecognizesDeletion() {
    LevenshteinDistanceMatcher matcher = LevenshteinDistanceMatcher.of("ABC");
    int distance = matcher.distanceTo("AB");
    assertEquals(1, distance);
  }

  @Test
  public void testRecognizesSubstitution() {
    LevenshteinDistanceMatcher matcher = LevenshteinDistanceMatcher.of("ABC");
    int distance = matcher.distanceTo("ABD");
    assertEquals(1, distance);
  }
}
