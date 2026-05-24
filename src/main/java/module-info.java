module skyjet {
    requires transitive javafx.controls;
    requires transitive javafx.fxml;
    requires transitive javafx.graphics;
    requires transitive javafx.base;
    requires java.base;
    requires java.net.http;
    requires com.fasterxml.jackson.databind;

    opens app to javafx.graphics, javafx.fxml;
    opens controllers to javafx.fxml;
    opens models to javafx.base;
    opens services to com.fasterxml.jackson.databind;

    exports app;
    exports controllers;
    exports models;
    exports services;
}
