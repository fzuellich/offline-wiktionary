package de.zuellich.offlinewiktionary.core.archive;

import de.zuellich.offlinewiktionary.core.wiki.WikiPage;
import java.util.HashMap;
import java.util.Objects;
import java.util.Stack;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

public class WikiPageSAXParser extends DefaultHandler {

  private HashMap<String, WikiPage> result = new HashMap<>();

  private final Stack<String> lastStartElement = new Stack<>();
  private WikiPage.Builder current = new WikiPage.Builder();

  private StringBuilder elementValue;

  @Override
  public void startDocument() {
    result = new HashMap<>();
  }

  @Override
  public void characters(char[] ch, int start, int length) {
    if (elementValue == null) {
      elementValue = new StringBuilder();
    } else {
      elementValue.append(ch, start, length);
    }
  }

  @Override
  public void startElement(String uri, String localName, String qName, Attributes attributes) {
    if ("page".equals(qName)) {
      current = new WikiPage.Builder();
    }
    lastStartElement.push(qName);
    elementValue = new StringBuilder();
  }

  @Override
  public void endElement(String uri, String localName, String qName) {
    // We don't need the current element, that's stored in qName already
    lastStartElement.pop();
    switch (qName) {
      case "title":
        if (Objects.equals(lastStartElement.peek(), "page")) {
          current.setTitle(elementValue.toString());
        }
        break;
      case "id":
        if (Objects.equals(lastStartElement.peek(), "page")) {
          current.setId(elementValue.toString());
        }
        break;
      case "text":
        if (Objects.equals(lastStartElement.peek(), "page")) {
          current.setText(elementValue.toString());
        }
        break;
      case "format":
        if (Objects.equals(lastStartElement.peek(), "page")) {
          current.setFormat(elementValue.toString());
        }
        break;
      case "page":
        final WikiPage page = current.build();
        result.put(page.id(), page);
      default:
        return;
    }
  }

  public HashMap<String, WikiPage> getResult() {
    return new HashMap<>(result);
  }
}
