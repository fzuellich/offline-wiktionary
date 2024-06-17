Offline Wiktionary is an experiment to make MediaWiki exports available for offline reading.

## Usage

### JavaFX Application

Run `./gradlew run` to start the regular JavaFX based application.

### Fuzzer

To run the Fuzzer call `./gradlew runFuzzer --args 'pathToIndex
[pathToArchive]'`.

The Fuzzer is a small application that tries to parse all entries found in an
index file from a given archive. Entries that can't be parsed are written to
`./test-output.txt.bz2` in the format of an index file. Thus allowing you to
only parse failed entries for subsequent invocations.

Archive and index are expected to be compressed using BZIP2.

## Implementation details

### MediaWiki Grammar

Part of the project implements a PEG-based parser to convert MediaWiki markup to tokens that are then used to style the
page content in the app. Belows is the implemented grammar that supports only a tiny subset:

```text
Markup              <- Headline / Indent / Italic / TextContent
TextContent         <- Macro / Link / Text*

Italic              <- '\'\'' TextContent+ '\'\''

Headline            <- Headline2Start TextContent+ Headline2End
HeadlineStart       <- '=' '='+
HeadlineEnd         <- Headline2Start

Macro               <- MacroStart Text* MacroEnd
MacroStart          <- '{{'
MacroEnd            <- '}}'

Link                <- LinkStart LinkText ('|' LinkText)? LinkEnd (! ' ' & utf-8 symbol)?
LinkStart           <- '[['
LinkEnd             <- ']]'
LinkText            <- !'|' & !LinkEnd & utf-8 symbol

Indent              <- ':'+ TextContent

Text                <-  (!MacroStart & !MacroEnd 
                        & !HeadlineStart & !HeadlineEnd 
                        & !LinkStart & !LinkEnd) & utf-8 symbol
```
