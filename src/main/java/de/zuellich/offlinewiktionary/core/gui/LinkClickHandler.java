package de.zuellich.offlinewiktionary.core.gui;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.ArrayDeque;
import java.util.function.Consumer;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javax.annotation.Nullable;

public class LinkClickHandler {

  @Nullable private String previousTerm = null;

  private final ArrayDeque<String> history = new ArrayDeque<>();

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
    if (history.isEmpty() || !history.peekFirst().equals(item)) {
      // For now we don't add duplicates to the history, but fire the click handler
      history.addFirst(item);
      historyEmptyProperty.set(history.isEmpty());
    }
  }

  public void historyBack() {
    if (history.isEmpty()) {
      return;
    }

    this.handle(history.removeFirst(), true);
    historyEmptyProperty.set(history.isEmpty());
  }

  @SuppressFBWarnings(
      value = "EI",
      justification = "We do want to expose this property for use in JavaFX")
  public BooleanProperty isHistoryEmptyProperty() {
    return historyEmptyProperty;
  }
}
