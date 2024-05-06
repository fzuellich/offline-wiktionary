package de.zuellich.offlinewiktionary.core.gui;

import de.zuellich.offlinewiktionary.core.markup.*;
import java.util.ArrayList;
import java.util.Collection;
import javafx.scene.Node;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

/** Use JavaFX {@link javafx.scene.text.TextFlow} to display markdown. */
public class WikiTextFlow extends TextFlow {

  public WikiTextFlow() {}

  public void replaceChildren(Collection<MarkupToken> tokens) {
    final Collection<Node> nodes = tokensToChildren(tokens);
    this.getChildren().clear();
    this.getChildren().addAll(nodes);
  }

  private Collection<Node> tokensToChildren(Collection<MarkupToken> tokens) {
    ArrayList<Node> result = new ArrayList<>(tokens.size());
    for (MarkupToken token : tokens) {
      switch (token.getType()) {
        case TEXT -> result.add(textNode((TextToken) token));
        case LINK -> result.add(linkNode((LinkToken) token));
        case HEADING -> result.add(headingNode((HeadingToken) token));
      }
    }
    return result;
  }

  private Node linkNode(LinkToken token) {
    return Hyperlink(token.label());
  }

  private Node headingNode(HeadingToken token) {
    if (token.value().getType() != MarkupTokenType.TEXT) {
      return new Text();
    }
    Text result = new Text();
    result.setText(((TextToken) token.value()).value());
    result.setFont(Font.font(32));
    // TODO have another TextFlow internally to display things like Macros etc?
    return result;
  }

  private Text textNode(TextToken token) {
    Text result = new Text();
    result.setText(token.value());
    result.setFont(Font.font(12));
    return result;
  }
}
