module ru.robar3.chatgb {
    requires javafx.controls;
    requires javafx.fxml;


    opens ru.robar3.chatgb to javafx.fxml;
    exports ru.robar3.chatgb;
}