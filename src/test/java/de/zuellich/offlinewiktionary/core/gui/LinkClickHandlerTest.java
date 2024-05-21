package de.zuellich.offlinewiktionary.core.gui;

import static org.mockito.Mockito.*;

import java.util.function.Consumer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class LinkClickHandlerTest {

  private final Consumer<String> navigationHandler;

  public LinkClickHandlerTest(@Mock Consumer<String> navigationHandler) {
    this.navigationHandler = navigationHandler;
  }

  @Test
  public void testGoBackWithEmptyHistory() {
    var handler = new LinkClickHandler();
    handler.onClick(navigationHandler);
    handler.historyBack();
    verifyNoInteractions(navigationHandler);
  }

  @Test
  public void testIgnoreDuplicateHistoryEntries() {
    var handler = new LinkClickHandler();
    handler.onClick(navigationHandler);
    handler.handle("peace");
    handler.handle("peace");
    handler.historyBack();
    handler.historyBack();
    verify(navigationHandler, times(3)).accept("peace");
  }

  @Test
  public void testDontGoBackPastInitialEntry() {
    var handler = new LinkClickHandler();
    handler.onClick(navigationHandler);
    handler.handle("peace");
    handler.historyBack();
    verify(navigationHandler, times(1)).accept("peace");
  }
}
