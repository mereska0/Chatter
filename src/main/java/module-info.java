module com.example.demo {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.bootstrapfx.core;
    requires javafx.graphics;
    requires java.desktop;

    opens app to javafx.fxml;
    exports app;
    exports app.Gui;
    opens app.Gui to javafx.fxml;
    exports app.Server;
    opens app.Server to javafx.fxml;
}