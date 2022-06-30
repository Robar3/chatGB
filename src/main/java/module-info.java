module ru.robar3.chatgb {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires org.apache.commons.io;


    exports ru.robar3.chatgb;
    opens ru.robar3.chatgb to javafx.fxml;

}