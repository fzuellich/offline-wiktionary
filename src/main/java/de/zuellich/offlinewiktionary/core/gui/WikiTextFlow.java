package de.zuellich.offlinewiktionary.core.gui;

import de.zuellich.offlinewiktionary.core.markup.*;
import java.util.ArrayList;
import java.util.Collection;
import javafx.scene.Node;
import javafx.scene.control.Hyperlink;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

/** Use JavaFX {@link javafx.scene.text.TextFlow} to display markdown. */
public class WikiTextFlow extends TextFlow {

  private final LinkClickHandler linkClickHandler;

  public WikiTextFlow(LinkClickHandler linkClickHandler) {
    this.linkClickHandler = linkClickHandler;
  }

  public void replaceChildren(Collection<MarkupToken> tokens) {
    final Collection<Node> nodes = tokensToChildren(tokens);
    this.getChildren().clear();
    this.getChildren().addAll(nodes);
  }

  private Collection<Node> tokensToChildren(Collection<MarkupToken> tokens) {
    ArrayList<Node> result = new ArrayList<>(tokens.size());
    for (MarkupToken token : tokens) {
      switch (token.getType()) {
        case LINK -> result.add(linkNode((LinkToken) token));
        case HEADING -> result.add(headingNode((HeadingToken) token));
        case INDENT -> result.add(indentNode((IndentToken) token));
        default -> result.add(textNode((TextToken) token));
      }
    }
    return result;
  }

  private Node linkNode(LinkToken token) {
    final Hyperlink hyperlink = new Hyperlink(token.label());
    hyperlink.setOnAction(
        v -> {
          linkClickHandler.handle(token.target());
        });
    return hyperlink;
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

  private Text indentNode(IndentToken token) {
    Text result = new Text();
    result.setText("\t".repeat(Math.min(token.level(), 10)));
    return result;
  }
}
