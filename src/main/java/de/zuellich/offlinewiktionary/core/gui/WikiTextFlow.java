package de.zuellich.offlinewiktionary.core.gui;

import de.zuellich.offlinewiktionary.core.markup.*;
import java.util.ArrayList;
import java.util.Collection;
import javafx.scene.Node;
import javafx.scene.control.Hyperlink;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

/** Use JavaFX {@link javafx.scene.text.TextFlow} to display markdown. */
public class WikiTextFlow extends TextFlow {

  private static final int DEFAULT_FONT_SIZE = 12;
  private static final int HEADING_FONT_SIZE = 32;

  private final LinkClickHandler linkClickHandler;
  private boolean italicsEnabled = false;
  private int fontSize = DEFAULT_FONT_SIZE;

  public WikiTextFlow(LinkClickHandler linkClickHandler) {
    this.linkClickHandler = linkClickHandler;
  }

  public void replaceChildren(Collection<MarkupToken> tokens) {
    final Collection<Node> nodes = tokensToChildren(tokens);
    this.getChildren().clear();
    this.getChildren().addAll(nodes);
  }

  // If a token type isn't mapped yet, we rather skip it than to render something unintelligible.
  private Collection<Node> tokensToChildren(Collection<MarkupToken> tokens) {
    ArrayList<Node> result = new ArrayList<>(tokens.size());
    for (MarkupToken token : tokens) {
      switch (token.getType()) {
        case LINK -> result.add(linkNode((LinkToken) token));
        case HEADING -> result.addAll(headingNode((HeadingToken) token));
        case INDENT -> result.add(indentNode((IndentToken) token));
        case ITALIC -> result.addAll(italicNode((ItalicToken) token));
        case TEXT -> result.add(textNode((TextToken) token));
        case NULL -> {
          // Ensure we get notified by ErrorProne in case we are missing a case above
          // but we don't need to do anything for NULL so we do a useless "continue" and
          // everyone is happy.
          continue;
        }
      }
    }
    return result;
  }

  private Font getFont() {
    if (!italicsEnabled) {
      return Font.font("System", fontSize);
    }

    return Font.font("System", FontPosture.ITALIC, fontSize);
  }

  private Collection<Node> italicNode(ItalicToken token) {
    italicsEnabled = true;
    final Collection<Node> nodes = tokensToChildren(token.value());
    italicsEnabled = false;
    return nodes;
  }

  private Node linkNode(LinkToken token) {
    final Hyperlink hyperlink = new Hyperlink(token.label());
    hyperlink.setFont(getFont());
    hyperlink.setOnAction(v -> linkClickHandler.handle(token.target()));
    return hyperlink;
  }

  private Collection<Node> headingNode(HeadingToken token) {
    int prevSize = fontSize;
    fontSize = HEADING_FONT_SIZE;
    final Collection<Node> nodes = tokensToChildren(token.value());
    fontSize = prevSize;
    return nodes;
  }

  private Text textNode(TextToken token) {
    Text result = new Text();
    result.setText(token.value());
    result.setFont(getFont());
    return result;
  }

  private Text indentNode(IndentToken token) {
    Text result = new Text();
    result.setText("\t".repeat(Math.min(token.level(), 10)));
    return result;
  }
}
