package com.example.marvel;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.io.IOException;

public class SearchController {

    private ObservableList<String> comicTitlesList = FXCollections.observableArrayList();
    private Object[][] comics; // Make sure you have this variable properly initialized

    @FXML
    private TableColumn<String, String> comicColumn;

    @FXML
    private TableView<String> comicTable;

    @FXML
    private TextField searchBox;

    @FXML
    private Button searchButton;

    @FXML
    void initialize() {
        comicColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue()));
    }

    @FXML
    void fetchData(ActionEvent event) {
        String character = searchBox.getText();
        String comicData = RequestsAndJson.getComicData(character);

        try {
            comics = RequestsAndJson.extractComicData(comicData);
            if (comics != null) {
                comicTitlesList.clear();
                for (Object[] comic : comics) {
                    String title = (String) comic[0];
                    title = title.replaceAll("#\\d+$", "");
                    comicTitlesList.add(title); // Title is at index 0
                }
                comicColumn.setText("Comics Found");
                comicTable.setItems(comicTitlesList);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void handleComicSelection(MouseEvent event) {
        int selectedComicIndex = comicTable.getSelectionModel().getSelectedIndex();
        if (selectedComicIndex >= 0) {
            loadComicDetailView(selectedComicIndex);
        }
    }

    private void loadComicDetailView(int index) {
        // Load the ComicDetailView.fxml file to create the detailed view scene
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("ComicDetailView.fxml"));
            Parent root = loader.load();

            // Access the controller of the loaded FXML
            ComicController comicController = loader.getController();

            // Populate the detailed view with the selected comic's data
            comicController.initializeComicDetails(
                    (String) comics[index][0], // Title
                    (String) comics[index][1], // Description
                    (String) comics[index][2], // Thumbnail URL
                    comics[index][3].toString(), // Characters
                    comics[index][4].toString()  // URLs
            );

            // Create a new scene and set it in your primary stage
            Scene scene = new Scene(root);
            Stage primaryStage = (Stage) searchButton.getScene().getWindow(); // Get the primary stage
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}