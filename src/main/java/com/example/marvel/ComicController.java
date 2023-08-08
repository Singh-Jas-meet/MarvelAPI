package com.example.marvel;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class ComicController {

    @FXML
    private TextField charactersTextBox;

    @FXML
    private ImageView comicThumbnail;

    @FXML
    private Label comicTitleLabel;

    @FXML
    private TextArea descriptionTextBox;

    @FXML
    private TextField urlTextBox;

    public void initializeComicDetails(String title, String description, String thumbnailUrl, String characters, String urls) {
        comicTitleLabel.setText(title);
        descriptionTextBox.setText(description);

        // Load the thumbnail image from the provided URL and set it to comicThumbnail
        Image thumbnailImage = new Image(thumbnailUrl);
        comicThumbnail.setImage(thumbnailImage);

        charactersTextBox.setText(characters);
        urlTextBox.setText(urls);
    }
}
