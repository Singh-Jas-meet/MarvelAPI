module com.example.marvel {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.net.http;
    requires json.simple;


    opens com.example.marvel to javafx.fxml;
    exports com.example.marvel;
}