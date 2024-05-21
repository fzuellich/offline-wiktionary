package de.zuellich.offlinewiktionary.core.gui;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Stack;
import java.util.function.Consumer;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

public class LinkClickHandler {

  private String previousTerm = null;

  private final Stack<String> history = new Stack<>();

  private Consumer<String> clickHandler = (_target) -> {};
  private final BooleanProperty historyEmptyProperty = new SimpleBooleanProperty(true);

  public void handle(String target) {
    handle(target, false);
  }

  public void handle(String target, boolean skipHistory) {
    if (!skipHistory && previousTerm != null) {
      historyPush(previousTerm);
    }

    this.clickHandler.accept(target);
    previousTerm = target;
  }

  public void onClick(Consumer<String> handler) {
    this.clickHandler = handler;
  }

  public void historyPush(String item) {
    if (history.empty() || !history.peek().equals(item)) {
      // For now we don't add duplicates to the history, but fire the click handler
      history.push(item);
      historyEmptyProperty.set(history.empty());
    }
  }

  public void historyBack() {
    if (history.empty()) {
      return;
    }

    this.handle(history.pop(), true);
    historyEmptyProperty.set(history.empty());
  }

  @SuppressFBWarnings(
      value = "EI",
      justification = "We do want to expose this property for use in JavaFX")
  public BooleanProperty isHistoryEmptyProperty() {
    return historyEmptyProperty;
  }
}
