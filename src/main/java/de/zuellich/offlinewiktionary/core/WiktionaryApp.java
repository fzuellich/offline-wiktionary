package de.zuellich.offlinewiktionary.core;

import de.zuellich.offlinewiktionary.core.archive.WiktionaryReader;
import de.zuellich.offlinewiktionary.core.gui.LinkClickHandler;
import de.zuellich.offlinewiktionary.core.gui.WikiTextFlow;
import de.zuellich.offlinewiktionary.core.gui.WiktionaryModel;
import de.zuellich.offlinewiktionary.core.markup.*;
import de.zuellich.offlinewiktionary.core.wiki.WikiPage;
import java.io.File;
import java.util.List;
import java.util.Set;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class WiktionaryApp extends Application {

  private final WiktionaryModel model = new WiktionaryModel(new WiktionaryReader(null));
  private final TextField search;
  private final WikiTextFlow wikiTextFlow;
  private final LinkClickHandler linkClickHandler;
  private final Label status;

  public WiktionaryApp() {
    search = new TextField();
    linkClickHandler = new LinkClickHandler();
    wikiTextFlow = new WikiTextFlow(linkClickHandler);
    wikiTextFlow.setBackground(
        new Background(new BackgroundFill(Color.IVORY, CornerRadii.EMPTY, Insets.EMPTY)));
    status = new Label("No index opened");
  }

  private void displayTerm(String term) {
    search.setText(term);
    String result =
        model.lookupDefinition(term).map(WikiPage::text).orElseGet(() -> findSuggestions(term));
    final MarkupParser parser = new MarkupParser();
    final List<MarkupToken> parse = parser.parse(result);
    wikiTextFlow.replaceChildren(parse);
  }

  private String findSuggestions(String term) {
    Set<String> suggestions = model.findSuggestions(term);

    StringBuilder builder = new StringBuilder();
    builder.append("No results found.\n\n");
    if (!suggestions.isEmpty()) {
      builder.append("==Did you look for:==\n\n");
      for (String entry : suggestions) {
        builder.append(String.format("[[%s]]\n", entry));
      }
    }

    return builder.toString();
  }

  private Pane getImportBar(Stage primaryStage) {
    final FileChooser archiveChooser = new FileChooser();
    archiveChooser.setTitle("Select an index to open...");
    archiveChooser
        .getExtensionFilters()
        .add(
            new FileChooser.ExtensionFilter(
                "Wikimedia index", "*pages-articles-multistream-index.txt.bz2"));
    Button mirrorLink = new Button("Mirrors");
    mirrorLink.setTooltip(new Tooltip("Find Wiktionary XML dumps online"));
    mirrorLink.setOnAction(
        _ev -> {
          getHostServices().showDocument("https://dumps.wikimedia.org/mirrors.html");
        });

    final Button anImport = new Button("Open index");

    anImport.setOnAction(
        actionEvent -> {
          final File selected = archiveChooser.showOpenDialog(primaryStage);
          if (selected == null) {
            return;
          }

          model.setIndexFile(selected.toPath());

          var task = new ImportTask(model);

          status.textProperty().bind(task.messageProperty());
          anImport.disableProperty().bind(task.runningProperty());

          new Thread(task).start();
        });

    HBox result = new HBox(5, status, anImport, mirrorLink);
    HBox.setHgrow(status, Priority.ALWAYS);
    result.setAlignment(Pos.BASELINE_LEFT);

    return result;
  }

  private Pane getSearchBar() {
    Button back = new Button("Back");
    back.disableProperty().bind(linkClickHandler.isHistoryEmptyProperty());
    back.setOnAction(value -> linkClickHandler.historyBack());

    search.disableProperty().bind(model.isReadyProperty().not());
    search.setOnAction(value -> linkClickHandler.handle(search.getText()));

    Button fireSearch = new Button("Search");
    fireSearch.disableProperty().bind(model.isReadyProperty().not());
    fireSearch.setOnAction(value -> linkClickHandler.handle(search.getText()));

    HBox result = new HBox(5, back, search, fireSearch);
    HBox.setHgrow(search, Priority.ALWAYS);

    return result;
  }

  private Parent createContent(Stage primaryStage) {
    linkClickHandler.onClick(this::displayTerm);

    Pane importBar = getImportBar(primaryStage);
    Pane searchBar = getSearchBar();

    ScrollPane wikiTextScrollPane = new ScrollPane();
    wikiTextScrollPane.setContent(wikiTextFlow);
    // cause the text to wrap at the border instead of adding horizontal scrollbars
    wikiTextScrollPane.setFitToWidth(true);
    // ensure that the contained text element always stretches the full height, allowing for a
    // uniform background
    wikiTextScrollPane.setFitToHeight(true);
    VBox result = new VBox(importBar, searchBar, wikiTextScrollPane);
    VBox.setMargin(importBar, new Insets(5, 5, 10, 5));
    VBox.setMargin(searchBar, new Insets(5));
    VBox.setMargin(wikiTextScrollPane, new Insets(5));
    VBox.setVgrow(wikiTextScrollPane, Priority.ALWAYS);

    return result;
  }

  @Override
  public void start(Stage primaryStage) {
    primaryStage.setTitle("Offline Wiktionary");
    primaryStage.setScene(new Scene(createContent(primaryStage), 300, 300));
    primaryStage.show();
  }

  public static void main(String[] args) {
    launch(args);
  }
}
