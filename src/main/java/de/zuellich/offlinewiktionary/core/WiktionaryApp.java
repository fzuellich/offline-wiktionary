package de.zuellich.offlinewiktionary.core;

import de.zuellich.offlinewiktionary.core.gui.WiktionaryModel;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class WiktionaryApp extends Application {

  private final WiktionaryModel model = new WiktionaryModel(this);

  private String searchHandler(String query) {
    System.out.println("You searched for: " + query);
    System.out.println("Result:\n" + model.lookupDefinition(query));
    return model.lookupDefinition(query).orElse("Nothing found.");
  }

  private Parent createContent() {
    final Label status = new Label("Hello World");
    final Button anImport = new Button("Import");

    anImport.setOnAction(
        actionEvent -> {
          var task = new ImportTask(model);

          status.textProperty().bind(task.messageProperty());
          anImport.disableProperty().bind(task.runningProperty());

          new Thread(task).start();
        });

    Text definition = new Text("Hello");
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
  public void start(Stage primaryStage) throws Exception {
    primaryStage.setTitle("HEllo");
    primaryStage.setScene(new Scene(createContent(), 300, 300));
    primaryStage.show();
  }

  public static void main(String[] args) {
    launch(args);
  }
}
