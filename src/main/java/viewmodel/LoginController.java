package viewmodel;

import javafx.animation.FadeTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import service.UserSession;
import javafx.scene.control.TextField;



public class LoginController {

    @FXML
    private TextField usernameTextField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private GridPane rootpane;
    public void initialize() {
        rootpane.setBackground(new Background(
                        createImage("https://edencoding.com/wp-content/uploads/2021/03/layer_06_1920x1080.png"),
                        null,
                        null,
                        null,
                        null,
                        null
                )

        );

        UserSession userSession = UserSession.getInstance();
        if(userSession !=null) {
            usernameTextField.setText(userSession.getUserName());
            passwordField.setText(userSession.getPassword());
        }


        rootpane.setOpacity(0);
        FadeTransition fadeOut2 = new FadeTransition(Duration.seconds(10), rootpane);
        fadeOut2.setFromValue(0);
        fadeOut2.setToValue(1);
        fadeOut2.play();
    }
    private static BackgroundImage createImage(String url) {
        return new BackgroundImage(
                new Image(url),
                BackgroundRepeat.REPEAT, BackgroundRepeat.NO_REPEAT,
                new BackgroundPosition(Side.LEFT, 0, true, Side.BOTTOM, 0, true),
                new BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO, true, true, false, true));
    }
    @FXML
    public void login(ActionEvent actionEvent) {

        UserSession userSession = UserSession.getInstance();
        String username = usernameTextField.getText();
        String password = passwordField.getText();
        if(userSession !=null) {
            if (!(userSession.getUserName().equals(username) && userSession.getPassword().equals(password))) {
                if (!(userSession.getUserName().equals(username))) {
                    usernameTextField.setText("");
                }
                if (!(userSession.getPassword().equals(password))) {
                    passwordField.setText("");
                }

            } else {
                try {
                    Parent root = FXMLLoader.load(getClass().getResource("/view/db_interface_gui.fxml"));
                    Scene scene = new Scene(root);
                    scene.getStylesheets().add(getClass().getResource("/css/lightTheme.css").toExternalForm());
                    Stage window = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
                    window.setScene(scene);
                    window.show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }


    }

    public void signUp(ActionEvent actionEvent) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/signUp.fxml"));
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/css/lightTheme.css").toExternalForm());
            Stage window = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            window.setScene(scene);
            window.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
