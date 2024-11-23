package viewmodel;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.stage.Stage;
import javafx.fxml.FXML;
import service.UserSession;

import java.awt.*;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.stream.Stream;

public class SignUpController implements Initializable {
    @FXML
    private ComboBox<String> privileges;

    @FXML
    private TextField password;

    @FXML
    private TextField userName;

    enum level{High, low, NONE}

    private String userNameRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.edu$";

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle){
        ObservableList<String> levelList =
                FXCollections.observableArrayList(Stream.of(level.values())
                        .map(Enum::name).toList());
        privileges.setItems(levelList);
        privileges.setValue("High");
    }

    public void createNewAccount(ActionEvent actionEvent) {
        if(!userName.getText().matches(userNameRegex)){
            userName.setText("");
        }else {
            UserSession.getInstance(userName.getText(), password.getText(), privileges.getValue());

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setContentText("A user account has been created！");
            alert.showAndWait();
        }
    }

    public void goBack(ActionEvent actionEvent) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/login.fxml"));
            Scene scene = new Scene(root, 900, 600);
            scene.getStylesheets().add(getClass().getResource("/css/lightTheme.css").toExternalForm());
            Stage window = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            window.setScene(scene);
            window.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
