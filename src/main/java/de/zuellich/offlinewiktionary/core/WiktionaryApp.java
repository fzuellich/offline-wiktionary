package de.zuellich.offlinewiktionary.core;

import de.zuellich.offlinewiktionary.core.gui.WiktionaryModel;
import java.io.File;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class WiktionaryApp extends Application {

  private final WiktionaryModel model = new WiktionaryModel();

  private String searchHandler(String query) {
    System.out.println("You searched for: " + query);
    System.out.println("Result:\n" + model.lookupDefinition(query));
    return model.lookupDefinition(query).orElse("Nothing found.");
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

    Text definition = new Text("");
    definition.setWrappingWidth(200);

    TextField search = new TextField();
    search.disableProperty().bind(model.isReadyProperty().not());
    search.setOnAction(
        value -> {
          String result = searchHandler(search.getText());
          definition.setText(result);
        });
    Button fireSearch = new Button("Search");
    fireSearch.disableProperty().bind(model.isReadyProperty().not());
    fireSearch.setOnAction(
        value -> {
          String result = searchHandler(search.getText());
          definition.setText(result);
        });

    return new VBox(status, anImport, search, fireSearch, definition);
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
