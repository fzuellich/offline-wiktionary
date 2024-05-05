package de.zuellich.offlinewiktionary.core.gui;

import static org.junit.jupiter.api.Assertions.*;

import de.zuellich.offlinewiktionary.core.archive.SeekEntry;
import de.zuellich.offlinewiktionary.core.archive.StaticWiktionaryReader;
import de.zuellich.offlinewiktionary.core.wiki.WikiPage;
import java.util.Optional;
import java.util.TreeSet;
import org.junit.jupiter.api.Test;

class WiktionaryModelTest {

  @Test
  public void testMakeSureToMatchTitleOfSeekEntry() {
    // We return a WikiPage to ensure the test fails if logic is incorrect, avoids setting up a Mock
    // framework for now
    var wiktionaryMock =
        new StaticWiktionaryReader(Optional.of(new WikiPage("Never return this", -1, "x", "x")));
    var model = new WiktionaryModel(wiktionaryMock);

    // GIVEN we have a definition for gerkin
    var definition = new TreeSet<SeekEntry>();
    definition.add(new SeekEntry(0, 1, "gerkin"));
    model.setDefinitions(definition);

    // WHEN we search for garkin
    Optional<WikiPage> result = model.lookupDefinition("garkin");
    // THEN we don't want to find gerkin
    assertTrue(result.isEmpty());

    // WHEN we search for gfrkin
    result = model.lookupDefinition("gfrkin");
    // THEN we don't want to find gfrkin
    assertTrue(result.isEmpty());

    // WHEN we search for gerkin
    result = model.lookupDefinition("gerkin");
    // THEN we do get a call to the wiktionary reader
    assertTrue(result.isPresent());
  }
}
