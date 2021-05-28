module view {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.base;
    requires java.logging;
    requires java.desktop;
    requires org.json;
    requires org.eclipse.jgit;
    requires com.jfoenix;
    requires org.apache.commons.lang3;
    requires decimal4j;

    exports view;

    opens view to javafx.fxml;

}