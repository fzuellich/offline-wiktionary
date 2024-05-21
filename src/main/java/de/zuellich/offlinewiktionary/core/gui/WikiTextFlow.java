package de.zuellich.offlinewiktionary.core.gui;

import de.zuellich.offlinewiktionary.core.markup.*;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
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

  private final LinkClickHandler linkClickHandler;
  private boolean italicsEnabled = false;

  public WikiTextFlow(LinkClickHandler linkClickHandler) {
    this.linkClickHandler = linkClickHandler;
  }

  public void replaceChildren(Collection<MarkupToken> tokens) {
    final Collection<Node> nodes = tokensToChildren(tokens);
    this.getChildren().clear();
    this.getChildren().addAll(nodes);
  }

  // If a token type isn't mapped yet, we rather skip it than to render something unintelligible.
  @SuppressFBWarnings("SF")
  private Collection<Node> tokensToChildren(Collection<MarkupToken> tokens) {
    ArrayList<Node> result = new ArrayList<>(tokens.size());
    for (MarkupToken token : tokens) {
      switch (token.getType()) {
        case LINK -> result.add(linkNode((LinkToken) token));
        case HEADING -> result.add(headingNode((HeadingToken) token));
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

  private Font getFont(double size) {
    if (!italicsEnabled) {
      return Font.font("System", size);
    }

    return Font.font("System", FontPosture.ITALIC, size);
  }

  private Collection<Node> italicNode(ItalicToken token) {
    italicsEnabled = true;
    final Collection<Node> nodes = tokensToChildren(token.value());
    italicsEnabled = false;
    return nodes;
  }

  private Node linkNode(LinkToken token) {
    final Hyperlink hyperlink = new Hyperlink(token.label());
    hyperlink.setFont(getFont(12));
    hyperlink.setOnAction(v -> linkClickHandler.handle(token.target()));
    return hyperlink;
  }

  private Node headingNode(HeadingToken token) {
    Text result = new Text();
    if (token.value() == null || token.value().getType() != MarkupTokenType.TEXT) {
      return result;
    }

    result.setText(((TextToken) token.value()).value());
    result.setFont(getFont(32));
    // TODO have another TextFlow internally to display things like Macros etc?
    return result;
  }

  private Text textNode(TextToken token) {
    Text result = new Text();
    result.setText(token.value());
    result.setFont(getFont(12));
    return result;
  }

  private Text indentNode(IndentToken token) {
    Text result = new Text();
    result.setText("\t".repeat(Math.min(token.level(), 10)));
    return result;
  }
}
