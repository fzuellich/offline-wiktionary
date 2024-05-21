package de.zuellich.offlinewiktionary.core;

import de.zuellich.offlinewiktionary.core.archive.WiktionaryReader;
import de.zuellich.offlinewiktionary.core.gui.LinkClickHandler;
import de.zuellich.offlinewiktionary.core.gui.WikiTextFlow;
import de.zuellich.offlinewiktionary.core.gui.WiktionaryModel;
import de.zuellich.offlinewiktionary.core.markup.MarkupParser;
import de.zuellich.offlinewiktionary.core.markup.MarkupToken;
import de.zuellich.offlinewiktionary.core.wiki.WikiPage;
import java.io.File;
import java.util.List;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class WiktionaryApp extends Application {

  private final WiktionaryModel model = new WiktionaryModel(new WiktionaryReader(null));
  private final TextField search;
  private final WikiTextFlow wikiTextFlow;
  private final LinkClickHandler linkClickHandler;

  public WiktionaryApp() {
    search = new TextField();
    linkClickHandler = new LinkClickHandler();
    wikiTextFlow = new WikiTextFlow(linkClickHandler);
  }

  private String searchHandler(String query) {
    System.out.println("You searched for: " + query);
    System.out.println("Result:\n" + model.lookupDefinition(query));
    return model.lookupDefinition(query).map(WikiPage::text).orElse("Nothing found.");
  }

  private void displayTerm(String term) {
    search.setText(term);
    String result = searchHandler(term);
    final MarkupParser parser = new MarkupParser();
    final List<MarkupToken> parse = parser.parse(result);
    wikiTextFlow.replaceChildren(parse);
  }

  private Parent createContent(Stage primaryStage) {
    final FileChooser archiveChooser = new FileChooser();
    archiveChooser.setTitle("Select an index to open...");
    archiveChooser
        .getExtensionFilters()
        .add(
            new FileChooser.ExtensionFilter(
                "Wikimedia index", "*pages-articles-multistream-index.txt.bz2"));
    final Label status = new Label("No index opened");
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

    search.disableProperty().bind(model.isReadyProperty().not());

    linkClickHandler.onClick(this::displayTerm);

    search.setOnAction(value -> linkClickHandler.handle(search.getText()));
    Button back = new Button("Back");
    back.disableProperty().bind(linkClickHandler.isHistoryEmptyProperty());
    back.setOnAction(value -> linkClickHandler.historyBack());

    Button fireSearch = new Button("Search");
    fireSearch.disableProperty().bind(model.isReadyProperty().not());
    fireSearch.setOnAction(value -> linkClickHandler.handle(search.getText()));

    return new VBox(status, anImport, search, back, fireSearch, wikiTextFlow);
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
