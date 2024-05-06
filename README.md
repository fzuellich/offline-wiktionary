Offline Wiktionary is an experiment to make MediaWiki exports available for offline reading.

## Implementation details

### MediaWiki Grammar

Part of the project implements a PEG-based parser to convert MediaWiki markup to tokens that are then used to style the
page content in the app. Belows is the implemented grammar:

```text
Markup              <- Headline / TextContent
TextContent         <- Macro / Link / Text*

Headline            <- Headline2Start TextContent+ Headline2End
HeadlineStart       <- '=' '='+
HeadlineEnd         <- Headline2Start

Macro               <- MacroStart Text* MacroEnd
MacroStart          <- '{{'
MacroEnd            <- '}}'

Link                <- LinkStart Text ('|' Text)? LinkEnd
LinkStart           <- '[['
LinkEnd             <- ']]'

Text                <-  (!MacroStart & !MacroEnd 
                        & !HeadlineStart & !HeadlineEnd 
                        & !LinkStart & !LinkEnd) & utf-8 symbol
```