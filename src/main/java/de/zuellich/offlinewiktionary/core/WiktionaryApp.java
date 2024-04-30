package de.zuellich.offlinewiktionary.core;

import de.zuellich.offlinewiktionary.core.archive.SeekEntry;
import de.zuellich.offlinewiktionary.core.archive.WikiPageSAXParser;
import javafx.application.Application;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.TreeSet;

public class WiktionaryApp extends Application {
    class WiktionaryReader {

        public WiktionaryReader() {

        }

        public String retrieve(long byteOffset) {
            WikiPageSAXParser mine = new WikiPageSAXParser();
            try (final InputStream in = Files.newInputStream(Path.of("/home/fzuellich/Downloads/dewiktionary-20240401-pages-articles-multistream.xml.bz2"));
                 final BufferedInputStream bin = new BufferedInputStream(in);
            ) {
                long skipped = bin.skip(byteOffset);
                if (skipped == 0) {
                    return "EOF";
                }

                final CompressorInputStream stream = new CompressorStreamFactory().createCompressorInputStream(bin);

                SAXParserFactory factory = SAXParserFactory.newInstance();
                SAXParser saxParser = factory.newSAXParser();
                saxParser.parse(stream, mine);

                /*final BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
                StringBuilder resultBuilder = new StringBuilder();
                String currentLine = reader.readLine();
                while (!currentLine.trim().equals("</page>")) {
                    resultBuilder.append(currentLine);
                    currentLine = reader.readLine();
                }
                reader.close();
                stream.close();*/
                System.out.println(mine.getResult());
                return "TEST";
            } catch (CompressorException | IOException | ParserConfigurationException | SAXException e) {
                e.printStackTrace();
            }

            return "";
        }
    }

    class WiktionaryModel {
        private TreeSet<SeekEntry> definitions;
        private WiktionaryReader wiktionaryReader = new WiktionaryReader();
        private BooleanProperty isReady = new SimpleBooleanProperty(false);

        public BooleanProperty isReady() {
            return isReady;
        }

        public void setDefinitions(TreeSet<SeekEntry> definitions) {
            this.definitions = definitions;
            this.isReady.set(true);
        }

        public Optional<String> lookupDefinition(String query) {
            final SeekEntry possibleEntry = definitions.ceiling(new SeekEntry(0, 0, query));
            if (possibleEntry == null) {
                return Optional.empty();
            }

            return Optional.of(wiktionaryReader.retrieve(possibleEntry.bytesToSeek()));
        }
    }

    private final WiktionaryModel model = new WiktionaryModel();

    private String searchHandler(String query) {
        System.out.println("You searched for: " + query);
        System.out.println("Result:\n" + model.lookupDefinition(query));
        return model.lookupDefinition(query).orElse("Nothing found.");
    }

    private Parent createContent() {
        final Label status = new Label("Hello World");
        final Button anImport = new Button("Import");

        anImport.setOnAction(actionEvent -> {
            var task = new ImportTask(model);

            status.textProperty().bind(task.messageProperty());
            anImport.disableProperty().bind(task.runningProperty());

            new Thread(task).start();
        });

        Text definition = new Text("Hello");
        definition.setWrappingWidth(200);

        TextField search = new TextField();
        search.disableProperty().bind(model.isReady().not());
        search.setOnAction(value -> {
            String result = searchHandler(search.getText());
            definition.setText(result);
        });
        Button fireSearch = new Button("Search");
        fireSearch.disableProperty().bind(model.isReady().not());
        fireSearch.setOnAction(value -> {
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
