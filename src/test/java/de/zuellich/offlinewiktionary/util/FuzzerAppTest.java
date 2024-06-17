package de.zuellich.offlinewiktionary.util;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;
import org.junit.jupiter.api.Test;

class FuzzerAppTest {

  @Test
  public void testBucketize() {
    final Collection<Integer> list = List.of(1, 2, 3, 4, 5);
    List<ArrayDeque<Integer>> buckets = FuzzerApp.bucketize(list, 1);

    assertEquals(1, buckets.size());
    assertEquals(5, buckets.get(0).size());

    buckets = FuzzerApp.bucketize(list, 2);
    assertEquals(2, buckets.size());
    assertEquals(2, buckets.get(0).size());
    assertEquals(3, buckets.get(1).size());

    buckets = FuzzerApp.bucketize(list, 3);
    assertEquals(3, buckets.size());
    assertEquals(1, buckets.get(0).size());
    assertEquals(1, buckets.get(1).size());
    assertEquals(3, buckets.get(2).size());

    buckets = FuzzerApp.bucketize(List.of(1), 10);
    assertEquals(1, buckets.size());

    buckets = FuzzerApp.bucketize(List.of(1, 2), 10);
    assertEquals(2, buckets.size());
  }
}
