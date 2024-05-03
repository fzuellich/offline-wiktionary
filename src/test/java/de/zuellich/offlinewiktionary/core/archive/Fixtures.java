package de.zuellich.offlinewiktionary.core.archive;

public class Fixtures {
  // https://en.wiktionary.org/wiki/Special:Export/Peace
  private static final String PEACE_PAGE =
      """
            <page>
                <title>Peace</title>
                <ns>0</ns>
                <id>6110313</id>
                <revision>
                    <id>71008712</id>
                    <parentid>71008708</parentid>
                    <timestamp>2023-01-20T20:43:28Z</timestamp>
                    <contributor>
                        <username>Donnanz</username>
                        <id>1154150</id>
                    </contributor>
                    <minor/>
                    <comment>/* Proper noun */</comment>
                    <model>wikitext</model>
                    <format>text/x-wiki</format>
                    <text bytes="676" xml:space="preserve">
                        {{also|peace}} ==English== {{wp|Peace (disambiguation)}} ===Proper noun=== {{en-prop|~|Peaces}} # {{surname|en}}. # {{given name|en|female}} # The {{w|Municipal District of Peace No. 135}}, {{place|en|a=a|municipal district|in north-west|p/Alberta|c/Canada}}. ====Statistics==== * According to the 2010 United States Census, ''Peace'' is the 4022<sup>nd</sup> most common surname in the United States, belonging to 8841 individuals. ''Peace'' is most common among White (70.06%) and Black/African American (23.87%) individuals. ===Noun=== {{en-noun}} # {{lb|en|numismatic slang}} {{clip of|en|{{w|Peace dollar}}}} {{cln|en|numismatic slang}} {{c|en|Coins|United States}}
                    </text>
                    <sha1>cwwxclxaol837q9a3t0mspzv1ej9b3u</sha1>
                </revision>
            </page>""";
  // From https://en.wiktionary.org/wiki/Special:Export/love
  public static final String SIMPLE_PAGE =
      """
            <page>
                <title>love</title>
                <ns>0</ns>
                <id>2682</id>
                <revision>
                  <id>79034193</id>
                  <parentid>78503293</parentid>
                  <timestamp>2024-04-24T22:13:52Z</timestamp>
                  <contributor>
                    <username>Adelpine</username>
                    <id>472128</id>
                  </contributor>
                  <comment>Correcting some IPA pronunciations and adding speakers to one of them</comment>
                  <model>wikitext</model>
                  <format>text/x-wiki</format>
                  <text bytes="24072" xml:space="preserve">{{also|Love|LoVe|løve|lové|lóve|lóvé|lőve|лове}}
                    ==English==
                    {{wikipedia}}

                    ===Alternative forms===
                    * {{alter|en|loue||obsolete typography}}
                    * {{alter|en|luv}}

                    ===Pronunciation===
                    * {{enPR|lŭv}}
                    ** {{a|RP|GA|CA}} {{IPA|en|/lʌv/}}
                    *** {{audio|en|En-uk-love.ogg|Audio (RP)}}
                    *** {{audio|en|En-us-love.ogg|Audio (GA)}}
                    ** {{a|Australia}} {{IPA|en|/lav/|[läv~lɐv]}}
                    ** {{a|India}} {{IPA|en|/lʌv/|[lɘʋ]|[lɘv]}}
                    ** {{a|Northern England|Ireland}} {{IPA|en|/lʊv/}}
                    * {{rhymes|en|ʌv|s=1}}

                    ===Etymology 1===
                    {{root|en|ine-pro|*lewbʰ-|id=love}}
                    From {{inh|en|enm|love}}, {{m|enm|luve}}, from {{inh|en|ang|lufu}}, from {{inh|en|gmw-pro|*lubu}}, from {{inh|en|gem-pro|*lubō}}, from {{der|en|ine-pro|*lewbʰ-|t=love, care, desire}}.

                    The ''close of a letter'' sense is presumably a truncation of ''With love'' or the like.

                    The verb is from {{inh|en|enm|loven}}, {{m|enm|luvien}}, from {{inh|en|ang|lufian|t=to love}}, from {{inh|en|gmw-pro|*lubōn|t=to love}}, derived from the noun.

                    Eclipsed non-native {{noncog|en|amour|t=love}}, borrowed from {{noncog|fr|amour|t=love}}.

                    [Rest is omitted for testing]
                    </text>
                    <sha1>jzyafjzm9z4x2hse0fi1w5r9grm3h4x</sha1>
                </revision>
            </page>""";

  public static final String CONCATENATED_PAGES = PEACE_PAGE + SIMPLE_PAGE;
}
