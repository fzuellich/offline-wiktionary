package de.zuellich.offlinewiktionary.core.gui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.zuellich.offlinewiktionary.core.WiktionaryApp;
import java.util.regex.Matcher;
import org.junit.jupiter.api.Test;

public class WiktionaryAppTest {

  @Test
  public void canRewriteURLsInSearchBar() {
    Matcher matcher = WiktionaryApp.URL_MATCHER.matcher("https://de.wiktionary.org/wiki/Abl.");
    assertTrue(matcher.matches());
    assertEquals("Abl.", matcher.group(1));

    matcher = WiktionaryApp.URL_MATCHER.matcher("https://de.wiktionary.org/wiki/-s");
    assertTrue(matcher.matches());
    assertEquals("-s", matcher.group(1));

    matcher = WiktionaryApp.URL_MATCHER.matcher("https://de.wiktionary.org/wiki/ver-");
    assertTrue(matcher.matches());
    assertEquals("ver-", matcher.group(1));

    matcher =
        WiktionaryApp.URL_MATCHER.matcher(
            "https://de.wiktionary.org/wiki/nicht_aus_dem_Quark_kommen");
    assertTrue(matcher.matches());
    assertEquals("nicht_aus_dem_Quark_kommen", matcher.group(1));
  }
}
