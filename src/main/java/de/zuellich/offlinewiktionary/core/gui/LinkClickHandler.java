package de.zuellich.offlinewiktionary.core.gui;

import java.util.function.Consumer;

public class LinkClickHandler {

  private Consumer<String> clickHandler = (_target) -> {};

  public void handle(String target) {
    this.clickHandler.accept(target);
  }

  public void onClick(Consumer<String> handler) {
    this.clickHandler = handler;
  }
}
